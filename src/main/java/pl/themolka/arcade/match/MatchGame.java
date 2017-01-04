package pl.themolka.arcade.match;

import net.engio.mbassy.listener.Handler;
import org.bukkit.ChatColor;
import pl.themolka.arcade.command.GameCommands;
import pl.themolka.arcade.event.Priority;
import pl.themolka.arcade.game.CycleCountdown;
import pl.themolka.arcade.game.GameModule;
import pl.themolka.arcade.session.ArcadePlayer;
import pl.themolka.commons.command.CommandContext;
import pl.themolka.commons.command.CommandException;
import pl.themolka.commons.session.Session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatchGame extends GameModule {
    private boolean autoStart;
    private int defaultStartCountdown;
    private final DrawMatchWinner drawWinner = new DrawMatchWinner();
    private Match match;
    private Observers observers;
    private MatchStartCountdown startCountdown;

    public MatchGame(boolean autoStart, int defaultStartCountdown, Observers observers) {
        this.autoStart = autoStart;
        this.defaultStartCountdown = defaultStartCountdown;
        this.observers = observers;
    }

    @Override
    public void onEnable() {
        this.match = new Match(this.getPlugin(), this.getGame(), this.getObservers());
        this.getObservers().setMatch(this.getMatch());

        this.getGame().setMetadata(MatchModule.class, MatchModule.METADATA_MATCH, this.getMatch());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public List<Object> onListenersRegister(List<Object> register) {
        return Arrays.asList(new MatchListeners(this), new ObserverListeners(this));
    }

    public int getDefaultStartCountdown() {
        return this.defaultStartCountdown;
    }

    public DrawMatchWinner getDrawWinner() {
        return this.drawWinner;
    }

    public Match getMatch() {
        return this.match;
    }

    public Observers getObservers() {
        return this.observers;
    }

    public MatchStartCountdown getStartCountdown() {
        return this.startCountdown;
    }

    public void handleBeginCommand(Session<ArcadePlayer> sender, int seconds, boolean force) {
        String message = "Starting";
        if (force) {
            message = "Force starting";
        }

        if (this.getMatch().getState().equals(MatchState.STARTING)) {
            sender.sendSuccess(message + " the match in " + seconds + " seconds...");
            this.startCountdown(seconds);
        } else {
            throw new CommandException("The match is not in the starting state.");
        }
    }

    public void handleEndCommand(Session<ArcadePlayer> sender, boolean auto, String winnerQuery, boolean draw) {
        if (this.getMatch().getState().equals(MatchState.RUNNING)) {
            MatchWinner winner = null;
            if (auto) {
                winner = this.getMatch().findWinner();

                if (winner == null) {
                    throw new CommandException("No winners are currently winning.");
                }
            } else if (draw) {
                winner = this.getDrawWinner();
            } else if (winnerQuery != null) {
                winner = this.getMatch().findWinner(winnerQuery);

                if (winner == null) {
                    throw new CommandException("No winners found from the given query.");
                }
            }

            sender.sendSuccess("Force ending the match...");
            this.getMatch().end(winner, true);
        } else {
            throw new CommandException("The match is not currently running.");
        }
    }

    public List<String> handleEndCompleter(Session<ArcadePlayer> sender, CommandContext context) {
        List<String> results = new ArrayList<>();
        for (MatchWinner winner : this.getMatch().getWinnerList()) {
            results.add(winner.getName());
        }

        return results;
    }

    public boolean isAutoStart() {
        return this.autoStart;
    }

    @Handler(priority = Priority.HIGHEST)
    public void onJoinWhenMatchEnded(GameCommands.JoinCommandEvent event) {
        if (this.getMatch().getState().equals(MatchState.CYCLING)) {
            event.getSender().sendError("The match has ended. " + ChatColor.GOLD + "Please wait until the server cycle.");
            event.setCanceled(true);
        }
    }

    @Handler(priority = Priority.LOWEST)
    public void onMatchCountdownAutoStart(GameCommands.JoinCommandEvent event) {
        if (!event.isCanceled() && this.isAutoStart()) {
            MatchStartCountdownEvent countdownEvent = new MatchStartCountdownEvent(this.getPlugin(), this.getMatch(), this.startCountdown);
            if (countdownEvent != null) {
                this.startCountdown(this.getDefaultStartCountdown());
            }
        }
    }

    @Handler(priority = Priority.LOWEST)
    public void onCycleCountdownAutoStart(MatchEndedEvent event) {
        CycleCountdown countdown = event.getPlugin().getGames().getCycleCountdown();

        if (!countdown.isTaskRunning()) {
            countdown.setDefaultDuration();
            countdown.countSync();
        }
    }

    @Handler(priority = Priority.NORMAL)
    public void onMatchTimeDescribe(GameCommands.GameCommandEvent event) {
        String time;
        if (this.getMatch().getStartTime() == null) {
            return;
        } else if (this.getMatch().getTime() != null) {
            time = this.getMatch().getTime().toString();
        } else {
            time = this.getMatch().getStartTime().toString();
        }

        event.getSender().send(ChatColor.DARK_PURPLE + "Time: " + ChatColor.DARK_AQUA + time);
    }

    public int startCountdown(int seconds) {
        if (this.getStartCountdown() == null) {
            this.startCountdown = new MatchStartCountdown(this.getPlugin(), this.match);
            this.getStartCountdown().setGame(this.getGame());
        }

        if (!this.getStartCountdown().isTaskRunning()) {
            return this.getStartCountdown().countStart(seconds);
        }

        return this.getStartCountdown().getTaskId();
    }
}
