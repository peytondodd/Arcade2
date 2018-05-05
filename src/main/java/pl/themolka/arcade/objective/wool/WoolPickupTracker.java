package pl.themolka.arcade.objective.wool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.engio.mbassy.listener.Handler;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import pl.themolka.arcade.event.Priority;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.game.Participator;
import pl.themolka.arcade.goal.GoalProgressEvent;
import pl.themolka.arcade.life.PlayerDeathEvent;
import pl.themolka.arcade.team.PlayerLeaveTeamEvent;
import pl.themolka.arcade.util.FinitePercentage;

public class WoolPickupTracker implements Listener {
    private final Wool wool;

    private final Multimap<Participator, GamePlayer> pickups = HashMultimap.create();

    public WoolPickupTracker(Wool wool) {
        this.wool = wool;
    }

    public boolean pickup(ItemStack itemStack, GamePlayer picker) {
        if (this.wool.isCompleted() || !WoolUtils.isWool(itemStack)) {
            return false;
        }

        Participator participator = null; // TODO
        if (participator == null || !this.wool.isCompletableBy(participator)) {
            return false;
        }

        boolean firstParticipatorPickup = !this.pickups.containsKey(participator);
        boolean firstPickerPickup = !this.pickups.containsValue(picker);
        FinitePercentage oldProgress = this.wool.getProgress();

        WoolPickupEvent event = new WoolPickupEvent(this.wool, firstParticipatorPickup,firstPickerPickup, itemStack, participator, picker);
        this.wool.getPlugin().getEventBus().publish(event);

        if (event.isCanceled() || firstPickerPickup) {
            return false;
        }

        this.wool.getContributions().addContributor(picker);
        participator.sendGoalMessage(this.createPickupMessage(picker));

        this.pickups.put(participator, picker);
        GoalProgressEvent.call(this.wool, picker, oldProgress);
        return true;
    }

    public void resetAllPickups() {
        this.pickups.clear();
    }

    public void resetPickupsFor(GamePlayer player) {
        Participator participator = null; // TODO
        if (participator != null) {
            this.pickups.remove(participator, player);
        }
    }

    //
    // Listeners
    //

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void pickupBetweenInventories(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (this.isWool(item)) {
            return;
        }

        HumanEntity human = event.getWhoClicked();
        if (human instanceof Player) {
            GamePlayer picker = this.wool.getGame().getPlayer((Player) human);
            if (picker != null && picker.isParticipating()) {
                this.pickup(item, picker);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void pickupFromGround(PlayerPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (!this.isWool(item)) {
            return;
        }

        GamePlayer picker = this.wool.getGame().getPlayer(event.getPlayer());
        if (picker != null && picker.isParticipating()) {
            this.pickup(item, picker);
        }
    }

    @Handler(priority = Priority.LAST)
    public void resetPickups(PlayerLeaveTeamEvent event) {
        if (!event.isCanceled()) {
            this.resetPickupsFor(event.getGamePlayer());
        }
    }

    @Handler(priority = Priority.LAST)
    public void resetPickups(PlayerDeathEvent event) {
        this.resetPickupsFor(event.getVictim());
    }

    //
    // Misc
    //

    private String createPickupMessage(GamePlayer picker) {
        return ChatColor.GOLD + picker.getDisplayName() + ChatColor.YELLOW + " picked up " +
                this.wool.describeOwner() + this.wool.describeObjective() + ChatColor.YELLOW + ".";
    }

    private boolean isWool(ItemStack item) {
        return WoolUtils.isWool(item, this.wool.getColor());
    }
}