package pl.themolka.arcade.capture.wool;

import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.event.Cancelable;
import pl.themolka.arcade.goal.GoalHolder;

public class WoolPlaceEvent extends WoolEvent implements Cancelable {
    private boolean cancel;
    private GoalHolder completer;

    public WoolPlaceEvent(ArcadePlugin plugin, Wool wool, GoalHolder completer) {
        super(plugin, wool);

        this.completer = completer;
    }

    @Override
    public boolean isCanceled() {
        return this.cancel;
    }

    @Override
    public void setCanceled(boolean cancel) {
        this.cancel = cancel;
    }

    public GoalHolder getCompleter() {
        return this.completer;
    }

    public void setCompleter(GoalHolder completer) {
        this.completer = completer;
    }
}