package pl.themolka.arcade.capture.point;

import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.capture.point.state.CapturingState;
import pl.themolka.arcade.capture.point.state.PointState;
import pl.themolka.arcade.event.Cancelable;
import pl.themolka.arcade.goal.GoalHolder;

public class PointCapturingEvent extends PointStateEvent implements Cancelable {
    private boolean cancel;
    private final GoalHolder capturing;

    public PointCapturingEvent(ArcadePlugin plugin, Point point, PointState oldState, CapturingState newState) {
        super(plugin, point, oldState, newState);

        this.capturing = newState.getCapturer();
    }

    @Override
    public boolean isCanceled() {
        return this.cancel;
    }

    @Override
    public void setCanceled(boolean cancel) {
        this.cancel = cancel;
    }

    public GoalHolder getCapturing() {
        return this.capturing;
    }
}
