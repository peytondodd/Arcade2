package pl.themolka.arcade.game;

import org.apache.commons.io.FileUtils;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.event.Event;
import pl.themolka.arcade.map.ArcadeMap;
import pl.themolka.arcade.map.MapManager;
import pl.themolka.arcade.map.MapParser;
import pl.themolka.arcade.map.MapParserException;
import pl.themolka.arcade.map.MapQueue;
import pl.themolka.arcade.map.MapQueueFillEvent;
import pl.themolka.arcade.map.OfflineMap;
import pl.themolka.arcade.session.ArcadePlayer;
import pl.themolka.arcade.settings.Settings;
import pl.themolka.arcade.time.Time;
import pl.themolka.arcade.time.XMLTime;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Level;

public class SimpleGameManager implements GameManager {
    public static final int DEFAULT_MAX_GAME_ID = 15;

    private final ArcadePlugin plugin;

    private Game currentGame;
    private final CycleCountdown cycleCountdown;
    private int gameId;
    private int maxGameId = DEFAULT_MAX_GAME_ID;
    private boolean nextRestart;
    private final RestartCountdown restartCountdown;
    private MapQueue queue = new MapQueue();

    public SimpleGameManager(ArcadePlugin plugin) {
        this.plugin = plugin;

        this.cycleCountdown = new CycleCountdown(plugin, this.readCycleCountdown(plugin.getSettings()));
        this.restartCountdown = new RestartCountdown(plugin, this.readCycleCountdown(plugin.getSettings()));
        this.setDefaultMaxGameId();
    }

    @Override
    public Game createGame(ArcadeMap map) throws IOException {
        MapManager maps = this.plugin.getMaps();

        this.plugin.getLogger().info("Accessing the '" + map.getMapInfo().getDirectory().getName() + "' directory...");
        File[] copied = maps.copyFiles(map);

        StringBuilder copiedFiles = new StringBuilder();
        for (int i = 0; i < copied.length; i++) {
            File file = copied[i];
            copiedFiles.append(file.getName());

            if (file.isDirectory()) {
                copiedFiles.append("[d]");
            } else if (file.isFile()) {
                copiedFiles.append("[f]");
            }

            if (i != copied.length - 1) {
                copiedFiles.append(", ");
            }
        }

        this.plugin.getLogger().info("Copied " + copied.length + " map files - " + copiedFiles.toString() + ".");

        this.plugin.getLogger().info("Generating new unique world '" + map.getWorldName() + "' for map '" + map.getMapInfo().getName() + "'...");
        World world = maps.createWorld(map);

        Game game = new Game(this.plugin, map, world);
        map.setGame(game);

        map.getSpawn().setWorld(world);

        String mapErrors = null;
        for (ArcadePlayer player : this.plugin.getPlayers()) {
            player.setGamePlayer(new GamePlayer(game, player));
            game.addPlayer(player.getGamePlayer());

            if (game.hasErrors()) {
                if (mapErrors == null) {
                    mapErrors = "Oh noo... this map contains some errors - please fix them!";
                    for (int i = 0; i < game.getErrors().size(); i++) {
                        mapErrors += "\nError #" + (i + 1) + ": " + game.getErrors().get(i).toString();
                    }
                }

                player.sendError(mapErrors);
            }
        }

        this.resetPlayers(game);
        return game;
    }

    @Override
    public Game createGame(OfflineMap map) throws IOException, MapParserException {
        MapParser parser = this.plugin.getMaps().getParser().newInstance();
        parser.readFile(map.getSettings());

        return this.createGame(parser.parseArcadeMap(this.plugin, map));
    }

    @Override
    public void cycle(OfflineMap target) {
        Instant now = Instant.now();
        if (target == null) {
            OfflineMap next = this.getQueue().takeNextMap();
            if (next == null) {
                this.plugin.getLogger().severe("Map queue was empty");
                return;
            }

            target = next;

            // refill queue if it's empty
            if (!this.getQueue().hasNextMap()) {
                this.fillDefaultQueue();
            }
        }

        this.plugin.getLogger().info("Cycling to '" + target.getName() + "' from '" + target.getDirectory().getName() + "'...");
        try {
            Game game = this.createGame(target);

            if (this.currentGame != null) {
                this.destroyGame(this.getCurrentGame());

                this.gameId++;
                this.plugin.getServerSession().getContent().setLastGameId(this.getGameId());
            }

            this.setCurrentGame(game);
            game.start();

            if (this.getGameId() >= this.getMaxGameId()) {
                this.setNextRestart(true);
            }
        } catch (MapParserException ex) {
            Game game = this.getCurrentGame();
            this.plugin.getLogger().log(Level.SEVERE, "Could not cycle to '" + target.getName() + "': " + ex.getMessage());

            if (game != null) {
                game.addError(ex);
            }

            this.cycleNext();
            return;
        } catch (Throwable th) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not cycle to '" + target.getName() + "'", th);

            this.cycleNext();
            return;
        }

        this.plugin.getLogger().info("Cycled in '" + (Instant.now().toEpochMilli() - now.toEpochMilli()) + "' ms.");
    }

    @Override
    public void cycleRestart() {
        CycleRestartEvent event = new CycleRestartEvent(this.plugin);
        this.postEvent(event);

        if (!event.isCanceled()) {
            this.plugin.getLogger().info("Restarting the server...");

            String reason = "Restarting the server...";
            for (Player online : new ArrayList<>(this.plugin.getServer().getOnlinePlayers())) {
                online.kickPlayer(reason);
            }

            this.plugin.getServer().shutdown();
        }
    }

    @Override
    public void destroyGame(Game game) {
        GameDestroyEvent worldEvent = new GameDestroyEvent(this.plugin, game);
        this.postEvent(worldEvent);

        game.stop();

        File directory = new File(this.plugin.getMaps().getWorldContainer(), game.getMap().getWorldName());
//        this.serializeGame(new File(directory, Game.JSON_FILENAME), game);

        this.plugin.getMaps().destroyWorld(game.getWorld(), worldEvent.isSaveWorld());

        GameDestroyedEvent destroyedEvent = new GameDestroyedEvent(this.plugin, game);
        this.postEvent(destroyedEvent);

        if (!destroyedEvent.isSaveDirectory()) {
            FileUtils.deleteQuietly(directory);
        }
    }

    @Override
    public void fillDefaultQueue() {
        Element queueElement = this.plugin.getSettings().getData().getChild("queue");
        if (queueElement == null) {
            queueElement = new Element("queue");
        }

        for (Element mapElement : queueElement.getChildren("map")) {
            String directory = mapElement.getAttributeValue("directory");
            String mapName = mapElement.getTextNormalize();

            OfflineMap map = null;
            if (directory != null) {
                map = this.plugin.getMaps().getContainer().getMapByDirectory(directory);
            } else if (mapName != null) {
                map = this.plugin.getMaps().getContainer().getMap(mapName);
            }

            if (map != null) {
                queue.addMap(map);
            }
        }

        this.postEvent(new MapQueueFillEvent(this.plugin, this.getQueue()));
    }

    @Override
    public Game getCurrentGame() {
        return this.currentGame;
    }

    @Override
    public CycleCountdown getCycleCountdown() {
        return this.cycleCountdown;
    }

    @Override
    public int getGameId() {
        return this.gameId;
    }

    @Override
    public int getMaxGameId() {
        return this.maxGameId;
    }

    @Override
    public MapQueue getQueue() {
        return this.queue;
    }

    @Override
    public RestartCountdown getRestartCountdown() {
        return this.restartCountdown;
    }

    @Override
    public boolean isNextRestart() {
        return this.nextRestart;
    }

    @Override
    public void resetPlayers(Game newGame) {
        for (GamePlayer player : newGame.getPlayers()) {
            if (!player.isOnline()) {
                continue;
            }

            player.getPlayer().resetFull();
            player.getBukkit().teleport(newGame.getMap().getSpawn());
        }
    }

    @Override
    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;

        this.getCycleCountdown().setGame(currentGame);
        this.getRestartCountdown().setGame(currentGame);
    }

    @Override
    public void setDefaultMaxGameId() {
        Element queueElement = plugin.getSettings().getData().getChild("queue");
        if (queueElement == null) {
            return;
        }

        Attribute attribute = queueElement.getAttribute("restart-after");
        if (attribute == null) {
            return;
        }

        try {
            this.setMaxGameId(attribute.getIntValue());
        } catch (DataConversionException ignored) {
        }
    }

    @Override
    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    @Override
    public void setMaxGameId(int maxGameId) {
        this.maxGameId = maxGameId;
    }

    @Override
    public void setNextRestart(boolean nextRestart) {
        this.nextRestart = nextRestart;
    }

    @Override
    public void serializeGame(File file, Game game) {
        try {
            this.plugin.serializeJsonFile(file, game);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private void postEvent(Event event) {
        this.plugin.getEventBus().publish(event);
    }

    private Duration readCycleCountdown(Settings settings) {
        Element cycle = settings.getData().getChild("cycle");

        if (cycle != null) {
            Attribute countdown = cycle.getAttribute("countdown");
            if (countdown != null) {
                Time time = XMLTime.parse(countdown);
                if (time != null) {
                    return time.toDuration();
                }
            }
        }

        return CycleCountdown.DEFAULT_DURATION;
    }
}
