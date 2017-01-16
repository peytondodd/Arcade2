package pl.themolka.arcade.session;

import net.engio.mbassy.listener.Handler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.event.Event;
import pl.themolka.arcade.event.PluginReadyEvent;
import pl.themolka.arcade.event.Priority;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GamePlayer;

public class Sessions extends pl.themolka.commons.session.Sessions<ArcadeSession> implements Listener {
    private final ArcadePlugin plugin;

    public Sessions(ArcadePlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.insertSession(this.createSession(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.removeSession(this.destroySession(event.getPlayer()));
    }

    @Handler(priority = Priority.FIRST)
    public void onPluginReady(PluginReadyEvent event) {
        int i = 0;
        for (Player online : event.getServer().getOnlinePlayers()) {
            this.insertSession(this.createSession(online));
            i++;
        }

        if (i > 0) {
            event.getPlugin().getLogger().info("Registered " + i + " online player(s).");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        ArcadePlayer player = this.plugin.getPlayer(event.getPlayer().getUniqueId());

        Game game = this.plugin.getGames().getCurrentGame();
        if (game != null) {
            ArcadePlayerRespawnEvent respawnEvent = new ArcadePlayerRespawnEvent(this.plugin, player);
            respawnEvent.setRespawnPosition(game.getMap().getSpawn());

            this.postEvent(respawnEvent);
            if (respawnEvent.getRespawnPosition() != null) {
                event.setRespawnLocation(respawnEvent.getRespawnPosition());
            }
        }
    }

    public ArcadeSession createSession(Player bukkit) {
        ArcadePlayer player = new ArcadePlayer(bukkit);

        Game game = this.plugin.getGames().getCurrentGame();
        if (game != null) {
            GamePlayer gamePlayer = game.getPlayer(bukkit.getUniqueId());
            if (gamePlayer == null) {
                gamePlayer = new GamePlayer(game, player);
            }

            gamePlayer.setPlayer(player);
            player.setGamePlayer(gamePlayer);
            game.addPlayer(gamePlayer);

            player.getBukkit().teleport(game.getMap().getSpawn());
        }

        this.postEvent(new ArcadePlayerJoinEvent(this.plugin, player));

        ArcadeSession session = new ArcadeSession(player);
        player.setSession(session);

        this.plugin.addPlayer(player);
        return session;
    }

    public ArcadeSession destroySession(Player bukkit) {
        ArcadeSession session = (ArcadeSession) this.getSession(bukkit.getUniqueId());
        session.getRepresenter().getGamePlayer().setPlayer(null); // make offline

        this.plugin.removePlayer(session.getRepresenter());
        this.postEvent(new ArcadePlayerQuitEvent(this.plugin, session.getRepresenter()));
        return session;
    }

    private void postEvent(Event event) {
        this.plugin.getEventBus().publish(event);
    }
}
