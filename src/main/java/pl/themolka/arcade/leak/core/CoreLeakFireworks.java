package pl.themolka.arcade.leak.core;

import net.engio.mbassy.listener.Handler;
import org.bukkit.Location;
import pl.themolka.arcade.event.Priority;
import pl.themolka.arcade.goal.GoalFireworkHandler;
import pl.themolka.arcade.region.RegionBounds;
import pl.themolka.arcade.util.Color;

public class CoreLeakFireworks extends GoalFireworkHandler {
    // We are unable to fire fireworks based on competitor's color.
    public static final Color FIREWORK_COLOR = Color.WHITE;

    public CoreLeakFireworks(boolean enabled) {
        super(enabled);
    }

    @Handler(priority = Priority.LAST)
    public void onCoreLeak(CoreLeakEvent event) {
        if (this.isEnabled() && !event.isCanceled()) {
            RegionBounds bounds = event.getCore().getRegion().getBounds();
            for (Location at : this.getRegionCorners(bounds)) {
                this.fireComplete(at, FIREWORK_COLOR);
            }
        }
    }
}
