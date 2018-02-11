package pl.themolka.arcade.spawn;

import org.bukkit.EntityLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class SpawnAgent extends ForwardingSpawn implements Directional {
    private final Spawn spawn;
    private final Entity entity;
    private Direction direction;

    private SpawnAgent(Spawn spawn, Entity entity, Direction direction) {
        this.spawn = spawn;
        this.entity = entity;
        this.direction = direction;
    }

    @Override
    protected Spawn delegate() {
        return this.getSpawn();
    }

    @Override
    public Location getLocation() {
        Vector vector = this.getVector();
        World world = this.getWorld();

        if (vector != null && world != null) {
            return vector.toLocation(world, this.getYaw(), this.getPitch());
        }

        return null;
    }

    @Override
    public float getYaw() {
        float entityYaw = this.getEntityLocation().getYaw();
        return this.direction.getYaw(entityYaw, super.getYaw());
    }

    @Override
    public float getPitch() {
        float entityPitch = this.getEntityLocation().getPitch();
        return this.direction.getPitch(entityPitch, super.getPitch());
    }

    public Spawn getSpawn() {
        return this.spawn;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public EntityLocation getEntityLocation() {
        return this.entity.getEntityLocation();
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Location spawn() {
        if (!this.entity.isDead()) {
            Location to = this.getLocation();
            if (to != null) {
                boolean ok = this.entity.teleport(this.normalize(to), this.cause());

                this.entity.setFallDistance(0F); // reset fall distance
                return ok ? to : null;
            }
        }

        return null;
    }

    /**
     * Normalize the given location so it is safe for players. The location
     * is not safe by default, if the player is spawning on eg. a carpet.
     */
    private Location normalize(Location location) {
        return location.clone().add(0.0D, 0.1D, 0.0D);
    }

    private PlayerTeleportEvent.TeleportCause cause() {
        PlayerTeleportEvent.TeleportCause cause = this.getSpawnCause(this.entity);
        return cause != null ? cause : null;
    }

    //
    // Instancing
    //

    public static SpawnAgent create(Spawn spawn, Entity entity, Direction direction) {
        return new SpawnAgent(spawn, entity, direction);
    }
}