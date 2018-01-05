package pl.themolka.arcade.leak.core;

import net.engio.mbassy.listener.Handler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.util.Vector;
import pl.themolka.arcade.channel.Messageable;
import pl.themolka.arcade.event.BlockTransformEvent;
import pl.themolka.arcade.event.Priority;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.goal.GoalHolder;
import pl.themolka.arcade.goal.GoalProgressEvent;
import pl.themolka.arcade.leak.LeakGame;
import pl.themolka.arcade.leak.Leakable;
import pl.themolka.arcade.region.CuboidRegion;
import pl.themolka.arcade.region.Region;
import pl.themolka.arcade.region.RegionBounds;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Core extends Leakable implements Listener {
    public static final int DEFAULT_DETECTOR_LEVEL = 5;
    public static final String DEFAULT_GOAL_NAME = "Core";
    public static final Liquid DEFAULT_LIQUID = Liquid.LAVA;
    public static final Material DEFAULT_MATERIAL = Material.OBSIDIAN;
    public static final String DETECTOR_REGION_SUFFIX = "_detector-region";

    private final List<Vector> breaked = new ArrayList<>();
    private Region detector;
    private Liquid liquid;
    private List<Material> material;
    private Region region;
    private final List<Block> snapshot = new ArrayList<>();

    public Core(LeakGame game, String id) {
        this(game, null, id);
    }

    public Core(LeakGame game, GoalHolder owner, String id) {
        super(game, owner, id);
    }

    @Override
    public String getDefaultName() {
        return DEFAULT_GOAL_NAME;
    }

    @Override
    public String getGoalInteractMessage(String interact) {
        String owner = "";
        if (this.hasOwner()) {
            owner = ChatColor.GOLD + this.getOwner().getTitle() + ChatColor.YELLOW + "'s ";
        }

        return ChatColor.GOLD + interact + ChatColor.YELLOW + " broke a piece of " +
                owner + ChatColor.GOLD + ChatColor.BOLD + ChatColor.ITALIC +
                this.getColoredName() + ChatColor.RESET + ChatColor.YELLOW + ".";
    }

    @Override
    public void leak(GoalHolder completer) {
        String owner = "";
        if (this.hasOwner()) {
            owner = ChatColor.GOLD + this.getOwner().getTitle() + ChatColor.YELLOW + "'s ";
        }

        String message = owner + ChatColor.GOLD + ChatColor.BOLD + ChatColor.ITALIC +
                this.getColoredName() + ChatColor.RESET + ChatColor.YELLOW + " has leaked" +
                this.getContributions().getContributorsPretty() + ChatColor.YELLOW + ".";

        this.game.getMatch().sendGoalMessage(message);
        this.setCompleted(null, true);
    }

    @Override
    public void resetLeakable() {
        this.breaked.clear();
    }

    public boolean addSnapshot(Block block) {
        return block.isLiquid() && this.snapshot.add(block);
    }

    public boolean addSnapshot(List<Block> blocks) {
        int index = 0;
        for (Block block : blocks) {
            if (this.addSnapshot(block)) {
                index++;
            }
        }

        return index == blocks.size();
    }

    /**
     * Events called in this method:
     *   - LeakableBreakEvent (cancelable)
     *   - GoalProgressEvent
     */
    public boolean breakPiece(GoalHolder breaker, GamePlayer player, Block block) {
        String interactMessage = breaker.getTitle();
        if (player != null) {
            interactMessage = player.getDisplayName();
        }

        CoreBreakEvent event = new CoreBreakEvent(this.game.getPlugin(), this, breaker, block, player);
        this.game.getPlugin().getEventBus().publish(event);

        if (event.isCanceled()) {
            return false;
        }

        double oldProgress = this.getProgress();

        block.setType(Material.AIR);

        this.breaked.add(new Vector(block.getX(), block.getY(), block.getZ()));
        if (player != null) {
            this.getContributions().addContributor(player);
        }

        breaker.sendGoalMessage(this.getGoalInteractMessage(interactMessage));

        GoalProgressEvent.call(this.game.getPlugin(), this, oldProgress);
        return true;
    }

    public void build(Liquid liquid, Region region, int detectorLevel) {
        // region
        List<Block> blocks = region.getBlocks(); // may take a while
        this.setRegion(region);

        // detector
        if (detectorLevel == 0) {
            detectorLevel = DEFAULT_DETECTOR_LEVEL;
        }
        int distance = detectorLevel * 4;

        RegionBounds bounds = region.getBounds();
        Vector min = bounds.getMin().clone().subtract(distance, 0, distance).setY(Region.MIN_HEIGHT);
        Vector max = bounds.getMax().clone().add(distance, 0, distance).setY(bounds.getMax().getY() -
                (bounds.getMax().getY() - bounds.getMin().getY()) - detectorLevel);
        this.setDetector(new CuboidRegion(region.getId() + DETECTOR_REGION_SUFFIX, region.getMap(), min, max));

        // liquid
        if (liquid == null) {
            liquid = this.findLiquid(blocks); // may take a while
        }
        if (liquid == null) {
            this.game.getLogger().log(Level.SEVERE, "No liquid found in core " + this.getName() + ".");
        }

        this.setLiquid(liquid);
        this.addSnapshot(this.createSnapshot(blocks)); // may take a while
    }

    public void clearSnapshot() {
        this.snapshot.clear();
    }

    public boolean contains(Block block) {
        Vector vector = new Vector(block.getX(), block.getY(), block.getZ());
        return this.matchMaterial(block.getType()) &&
                this.getRegion().contains(block) &&
                !this.breaked.contains(vector);
    }

    public List<Block> createSnapshot(List<Block> of) {
        List<Block> results = new ArrayList<>();
        for (Block block : of) {
            if (this.getLiquid().accepts(block.getType())) {
                results.add(block);
            }
        }

        return results;
    }

    public Liquid findLiquid(List<Block> blocks) {
        for (Block block : blocks) {
            Liquid liquid = Liquid.find(block.getType());
            if (liquid != null) {
                return liquid;
            }
        }

        return null;
    }

    public Region getDetector() {
        return this.detector;
    }

    public Liquid getLiquid() {
        return this.liquid;
    }

    public List<Material> getMaterial() {
        return this.material;
    }

    public Region getRegion() {
        return this.region;
    }

    public List<Block> getSnapshot() {
        return this.snapshot;
    }

    public boolean isSnapshot(Block block) {
        return this.snapshot.contains(block);
    }

    public boolean matchMaterial(Material material) {
        for (Material type : this.getMaterial()) {
            if (type.equals(material)) {
                return true;
            }
        }

        return false;
    }

    public boolean removeSnapshot(Block block) {
        return this.snapshot.remove(block);
    }

    public int replaceLiquid(Liquid newLiquid) {
        if (this.getLiquid().equals(newLiquid)) {
            return 0;
        }

        int index = 0;
        for (Block block : this.getSnapshot()) {
            for (Material liquid : this.getLiquid().getMaterials()) {
                if (block.getType().equals(liquid)) {
                    block.setType(newLiquid.getLiquid());
                    index++;
                    break;
                }
            }
        }

        this.setLiquid(newLiquid);

        return index;
    }

    public void setDetector(Region detector) {
        this.detector = detector;
    }

    public void setLiquid(Liquid liquid) {
        this.liquid = liquid;
    }

    public void setLiquid(Material material) {
        this.setLiquid(Liquid.find(material));
    }

    public void setMaterial(List<Material> material) {
        this.material = material;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    //
    // Listeners
    //

    @Handler(priority = Priority.NORMAL)
    public void detectBreak(BlockTransformEvent event) {
        if (event.isCanceled()) {
            return;
        }

        Block block = event.getBlock();
        if (!event.getNewState().getMaterial().equals(Material.AIR) || this.isLeaked() ||
                !this.getRegion().contains(block) || this.getLiquid().accepts(event.getNewState().getMaterial())) {
            return;
        }

        GamePlayer player = event.getGamePlayer();
        if (player == null) {
            event.setCanceled(true);
            return;
        }

        GoalHolder winner = this.game.getMatch().findWinnerByPlayer(player);
        if (this.getOwner().equals(winner)) {
            event.setCanceled(true);
            player.sendError("You may not damage your own " + ChatColor.GOLD +
                    this.getColoredName() + Messageable.INFO_COLOR + ".");
            return;
        }

        if (!this.breakPiece(winner, player, block)) {
            event.setCanceled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void detectLeak(BlockFromToEvent event) {
        if (this.isLeaked() || !this.getDetector().contains(event.getToBlock())) {
            return;
        }

        Material newType = event.getBlock().getType();
        if (this.getLiquid().accepts(newType)) {
            // the core has leaked
            this.leak(null);
        }
    }

    @Handler(priority = Priority.HIGHER)
    public void detectLiquidFlow(BlockTransformEvent event) {
        if (event.isCanceled()) {
            return;
        }

        Liquid liquid = this.getLiquid();
        if (liquid.accepts(event.getNewState().getMaterial())) {
            return;
        }

        if (this.getSnapshot().contains(event.getBlock())) {
            event.setCanceled(true);

            if (event.hasPlayer()) {
                event.getPlayer().sendError("You may not build inside " + ChatColor.GOLD +
                        this.getColoredName() + Messageable.ERROR_COLOR + ".");
            }
        }
    }
}