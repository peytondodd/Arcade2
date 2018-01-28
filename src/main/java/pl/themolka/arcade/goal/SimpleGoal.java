package pl.themolka.arcade.goal;

import org.apache.commons.lang3.builder.ToStringStyle;
import pl.themolka.arcade.game.Game;

public abstract class SimpleGoal implements Goal {
    // The "ToStringStyle.SHORT_PREFIX_STYLE" strings are long and unreadable here.
    public static final ToStringStyle TO_STRING_STYLE = ToStringStyle.MULTI_LINE_STYLE;

    private final Game game;

    private boolean completed = false;
    private GoalHolder completedBy = null;
    private String name = null;
    private GoalHolder owner;
    private boolean touched = false;

    public SimpleGoal(Game game, GoalHolder owner) {
        this.game = game;

        this.owner = owner;
    }

    @Override
    public String getColoredName() {
         return (this.hasOwner() ? this.getOwner().getColor().toChat().toString() : "") + this.getName();
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    @Override
    public String getName() {
        return this.hasName() ? this.name : this.getDefaultName();
    }

    @Override
    public GoalHolder getOwner() {
        return this.owner;
    }

    @Override
    public boolean isCompletableBy(GoalHolder completer) {
        return Goal.completableByNonOwner(this, completer);
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }

    @Override
    public boolean isCompleted(GoalHolder completer) {
        return this.isCompleted() && (this.completedBy == null || this.completedBy.equals(completer));
    }

    @Override
    public boolean isUntouched() {
        return !this.isCompleted() && !this.touched;
    }

    @Override
    public void setCompleted(boolean completed, GoalHolder completer) {
        if (completed) {
            this.setCompleted(completer);
        } else {
            this.setCompleted(false);
            this.reset();
        }
    }

    protected abstract void complete(GoalHolder completer);

    public GoalHolder getCompletedBy() {
        return this.completedBy;
    }

    public abstract String getDefaultName();

    public boolean hasOwner() {
        return this.owner != null;
    }

    public boolean hasName() {
        return this.name != null;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;

        if (!completed) {
            this.completedBy = null;
        }
    }

    public void setCompleted(GoalHolder completer) {
        this.setCompleted(true);
        this.completedBy = completer;

        this.complete(completer);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(GoalHolder owner) {
        this.owner = owner;
    }

    public void setTouched(boolean touched) {
        this.touched = touched;
    }
}