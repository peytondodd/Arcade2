package pl.themolka.arcade.match;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.goal.Goal;
import pl.themolka.arcade.session.ArcadePlayer;

import java.util.Collections;
import java.util.List;

public class DrawMatchWinner implements MatchWinner {
    public static final String DRAW_WINNER_ID = "_draw-match-winner";

    protected  DrawMatchWinner() {
    }

    @Override
    public boolean addGoal(Goal goal) {
        return false;
    }

    @Override
    public boolean contains(Player bukkit) {
        return true;
    }

    @Override
    public boolean contains(ArcadePlayer player) {
        return this.contains(player.getGamePlayer());
    }

    @Override
    public boolean contains(GamePlayer player) {
        return player.isParticipating();
    }

    @Override
    public List<Goal> getGoals() {
        return Collections.emptyList();
    }

    @Override
    public String getId() {
        return DRAW_WINNER_ID;
    }

    @Override
    public String getMessage() {
        return this.getTitle() + ChatColor.GREEN + "!";
    }

    @Override
    public String getName() {
        return "Score Draw";
    }

    @Override
    public String getTitle() {
        return ChatColor.GREEN + ChatColor.BOLD.toString() + this.getName() + ChatColor.RESET;
    }

    @Override
    public boolean isWinning() {
        return true;
    }

    @Override
    public boolean removeGoal(Goal goal) {
        return false;
    }

    @Override
    public void sendGoalMessage(String message) {
    }
}
