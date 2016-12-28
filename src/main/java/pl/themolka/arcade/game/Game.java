package pl.themolka.arcade.game;

import org.bukkit.World;
import org.jdom2.Element;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.map.ArcadeMap;
import pl.themolka.arcade.map.MapParserException;
import pl.themolka.arcade.module.Module;
import pl.themolka.arcade.module.ModuleContainer;
import pl.themolka.arcade.task.Countdown;
import pl.themolka.arcade.task.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Game {
    private final ArcadePlugin plugin;

    private final ArcadeMap map;
    private final GameModuleContainer modules = new GameModuleContainer();
    private final Map<UUID, GamePlayer> players = new HashMap<>();
    private final List<Task> taskList = new ArrayList<>();
    private final World world;

    public Game(ArcadePlugin plugin, ArcadeMap map, World world) {
        this.plugin = plugin;

        this.map = map;
        this.world = world;

        this.readModules(map.getConfiguration().getChild("modules"));
    }

    public void addPlayer(GamePlayer player) {
        this.players.put(player.getUuid(), player);
    }

    public int addAsyncTask(Task task) {
        if (this.taskList.add(task)) {
            return this.plugin.getTasks().scheduleAsync(task);
        }

        return -1;
    }

    public int addSyncTask(Task task) {
        if (this.taskList.add(task)) {
            return this.plugin.getTasks().scheduleSync(task);
        }

        return -1;
    }

    public void enableModule(GameModule module) {
        // TODO load before

        if (!this.hasDependencies(module)) {
            return;
        }

        module.registerListeners();
        module.onEnable();
    }

    public List<Countdown> getCountdowns() {
        List<Countdown> results = new ArrayList<>();
        for (Task task : this.getTasks()) {
            if (task instanceof Countdown) {
                results.add((Countdown) task);
            }
        }

        return results;
    }

    public ArcadeMap getMap() {
        return this.map;
    }

    public GameModuleContainer getModules() {
        return this.modules;
    }

    public GamePlayer getPlayer(String username) {
        for (GamePlayer player : this.getPlayers()) {
            if (player.getUsername().equalsIgnoreCase(username)) {
                return player;
            }
        }

        return null;
    }

    public GamePlayer getPlayer(UUID uuid) {
        return this.players.get(uuid);
    }

    public Collection<GamePlayer> getPlayers() {
        return this.players.values();
    }

    public List<Countdown> gerRunningCountdowns() {
        List<Countdown> results = new ArrayList<>();
        for (Task task : this.getTasks()) {
            if (task.isTaskRunning() && task instanceof Countdown) {
                results.add((Countdown) task);
            }
        }

        return results;
    }

    public List<Task> getRunningTasks() {
        List<Task> results = new ArrayList<>();
        for (Task task : this.getTasks()) {
            if (task.isTaskRunning()) {
                results.add(task);
            }
        }

        return results;
    }

    public List<Task> getTasks() {
        return this.taskList;
    }

    public World getWorld() {
        return this.world;
    }

    public boolean hasDependencies(GameModule module) {
        for (Class<? extends Module<?>> dependency : module.getModule().getDependency()) {
            if (!this.getModules().contains(dependency)) {
                return false;
            }
        }

        return true;
    }

    public boolean removeTask(Task task) {
        return this.taskList.remove(task);
    }

    public void start() {
        for (GameModule module : this.getModules().getModules()) {
            this.enableModule(module);
        }

        this.plugin.getEvents().post(new GameStartEvent(this.plugin, this));
    }

    public void stop() {
        this.plugin.getEvents().post(new GameStopEvent(this.plugin, this));

        for (GameModule module : this.getModules().getModules()) {
            module.onDisable();
            module.destroy();
        }

        for (Task task : this.getTasks()) {
            task.cancelTask();
        }
    }

    private GameModule readModule(Element xml) throws MapParserException {
        ModuleContainer container = this.plugin.getModules();
        if (container.contains(xml.getName())) {
            throw new MapParserException("module not found");
        }

        Module<?> module = this.plugin.getModules().getModuleById(xml.getName());
        try {
            Object object = module.buildGameModule(xml);
            if (object == null) {
                return null;
            }

            if (!object.getClass().isAssignableFrom(GameModule.class)) {
                throw new MapParserException("game module must extend " + GameModule.class.getName());
            }

            GameModule game = (GameModule) object;
            game.initialize(this.plugin, this, module);
            return game;
        } catch (Throwable th) {
            throw new MapParserException("could not build game module", th);
        }
    }

    private void readModules(Element parent) {
        for (Element xml : parent.getChildren()) {
            try {
                GameModule module = this.readModule(xml);
                if (module != null) {
                    this.getModules().register(module);
                }
            } catch (MapParserException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not load module '" + xml.getName() + "': " + ex.getMessage(), ex);
            }
        }
    }
}
