package pl.themolka.arcade.goal;

import pl.themolka.arcade.game.Game;

public abstract class SimpleInteractableGoal extends SimpleGoal implements InteractableGoal {
    private final GoalContributionContext contributions = new GoalContributionContext();

    public SimpleInteractableGoal(Game game, GoalHolder owner) {
        super(game, owner);
    }

    @Override
    public GoalContributionContext getContributions() {
        return this.contributions;
    }

    @Override
    public boolean isUntouched() {
        return !this.isCompleted() && (super.isUntouched() || this.getContributions().isEmpty());
    }
}