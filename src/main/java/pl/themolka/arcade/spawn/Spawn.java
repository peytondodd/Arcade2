package pl.themolka.arcade.spawn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import pl.themolka.arcade.config.Unique;
import pl.themolka.arcade.game.IGameConfig;
import pl.themolka.arcade.util.StringId;

public interface Spawn extends Directional, StringId {
    PlayerTeleportEvent.TeleportCause DEFAULT_SPAWN_CAUSE = PlayerTeleportEvent.TeleportCause.ENDER_PEARL;

    default Location getLocation() {
        Vector vector = this.getVector();
        World world = this.getWorld();

        if (vector != null && world != null) {
            return vector.toLocation(world, this.getYaw(), this.getPitch());
        }

        return null;
    }

    default PlayerTeleportEvent.TeleportCause getSpawnCause() {
        return DEFAULT_SPAWN_CAUSE;
    }

    default PlayerTeleportEvent.TeleportCause getSpawnCause(Entity entity) {
        return this.getSpawnCause();
    }

    default Vector getVector() {
        Location location = this.getLocation();
        return location != null ? location.toVector() : null;
    }

    default World getWorld() {
        Location location = this.getLocation();
        return location != null ? location.getWorld() : null;
    }

    interface Config<T extends Spawn> extends IGameConfig<T>, Unique {
    }
}
