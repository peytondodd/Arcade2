package pl.themolka.arcade.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.map.ArcadeMap;
import pl.themolka.arcade.util.Forwarding;

import java.util.List;
import java.util.Random;

public abstract class ForwardingRegion extends Forwarding<Region> implements Region {
    @Override
    public boolean contains(Block block) {
        return this.delegate().contains(block);
    }

    @Override
    public boolean contains(BlockVector vector) {
        return this.delegate().contains(vector);
    }

    @Override
    public boolean contains(Entity entity) {
        return this.delegate().contains(entity);
    }

    @Override
    public boolean contains(Location location) {
        return this.delegate().contains(location);
    }

    @Override
    public boolean contains(Region region) {
        return this.delegate().contains(region);
    }

    @Override
    public boolean contains(Vector vector) {
        return this.delegate().contains(vector);
    }

    @Override
    public boolean contains(double x, double z) {
        return this.delegate().contains(x, z);
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return this.delegate().contains(x, y, z);
    }

    @Override
    public boolean contains(int x, int z) {
        return this.delegate().contains(x, z);
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return this.delegate().contains(x, y, z);
    }

    @Override
    public List<Block> getBlocks() {
        return this.delegate().getBlocks();
    }

    @Override
    public RegionBounds getBounds() {
        return this.delegate().getBounds();
    }

    @Override
    public Vector getCenter() {
        return this.delegate().getCenter();
    }

    @Override
    public Game getGame() {
        return this.delegate().getGame();
    }

    @Override
    public double getHighestY() {
        return this.delegate().getHighestY();
    }

    @Override
    public ArcadeMap getMap() {
        return this.delegate().getMap();
    }

    @Override
    public ArcadePlugin getPlugin() {
        return this.delegate().getPlugin();
    }

    @Override
    public Vector getRandomVector() {
        return this.delegate().getRandomVector();
    }

    @Override
    public Vector getRandomVector(int limit) {
        return this.delegate().getRandomVector(limit);
    }

    @Override
    public Vector getRandomVector(Random random) {
        return this.delegate().getRandomVector(random);
    }

    @Override
    public Vector getRandomVector(Random random, int limit) {
        return this.delegate().getRandomVector(random, limit);
    }

    @Override
    public World getWorld() {
        return this.delegate().getWorld();
    }

    @Override
    public boolean containsRound(BlockVector vector) {
        return this.delegate().containsRound(vector);
    }

    @Override
    public boolean containsZero(BlockVector vector) {
        return this.delegate().containsZero(vector);
    }

    //
    // StringId
    //

    @Override
    public String getId() {
        return this.delegate().getId();
    }
}
