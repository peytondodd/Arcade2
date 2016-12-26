package pl.themolka.arcade.map;

import org.bukkit.Difficulty;
import org.bukkit.World;

public class ArcadeMap {
    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.PEACEFUL;
    public static final World.Environment DEFAULT_ENVIRONMENT = World.Environment.NORMAL;

    private final OfflineMap mapInfo;

    private Difficulty difficulty;
    private World.Environment environment;
    private boolean pvp;
    private World world;
    private String worldName;

    public ArcadeMap(OfflineMap mapInfo) {
        this.mapInfo = mapInfo;
    }

    public OfflineMap getMapInfo() {
        return this.mapInfo;
    }

    public Difficulty getDifficulty() {
        if (this.hasEnvironment()) {
            return this.difficulty;
        }

        return DEFAULT_DIFFICULTY;
    }

    public World.Environment getEnvironment() {
        if (this.hasEnvironment()) {
            return this.environment;
        }

        return DEFAULT_ENVIRONMENT;
    }

    public World getWorld() {
        return this.world;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public boolean hasDifficulty() {
        return this.difficulty != null;
    }

    public boolean hasEnvironment() {
        return this.environment != null;
    }

    public boolean isPvp() {
        return this.pvp;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setEnvironment(World.Environment environment) {
        this.environment = environment;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }
}
