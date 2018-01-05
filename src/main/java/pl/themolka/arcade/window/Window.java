package pl.themolka.arcade.window;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.item.ItemStackBuilder;

import java.util.logging.Level;

public class Window extends SimpleWindowListener {
    public static final ItemStack CLOSE_ITEM = new ItemStackBuilder()
            .type(Material.EYE_OF_ENDER)
            .displayName(ChatColor.GREEN + ChatColor.UNDERLINE.toString() + "Close")
            .build();

    public static final int ROW_LIMIT = 6;
    public static final int SLOTS_PER_ROW = 9;

    private final ArcadePlugin plugin;

    private final String name;
    private final Inventory container;
    private final int rows;

    public Window(ArcadePlugin plugin, int rows, String name) {
        this.plugin = plugin;

        this.rows = Math.min(rows, ROW_LIMIT);
        this.name = name;
        this.container = plugin.getServer().createInventory(null, this.getSlots(), name);
    }

    public final boolean click(GamePlayer player, ClickType click, int slot) {
        try {
            return this.onClick(player, click, slot, this.getContainer().getItem(slot));
        } catch (Throwable th) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not click window.", th);
        }

        return false;
    }

    public final boolean close(GamePlayer player) {
        try {
            if (this.onClose(player)) {
                player.getBukkit().closeInventory();
                return true;
            }
        } catch (Throwable th) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not close window.", th);
        }

        return false;
    }

    public final void create() {
        try {
            this.onCreate();
        } catch (Throwable th) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not create window.", th);
        }
    }

    public Inventory getContainer() {
        return this.container;
    }

    public String getName() {
        return this.name;
    }

    public int getRows() {
        return this.rows;
    }

    public int getSlots() {
        return this.rows * SLOTS_PER_ROW;
    }

    public final boolean open(GamePlayer player) {
        try {
            if (this.onOpen(player)) {
                player.getBukkit().openInventory(this.getContainer());
                return true;
            }
        } catch (Throwable th) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not open window.", th);
        }

        return false;
    }
}