package pl.themolka.arcade.match;

import net.engio.mbassy.listener.Handler;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;
import pl.themolka.arcade.command.CommandContext;
import pl.themolka.arcade.command.CommandException;
import pl.themolka.arcade.command.GameCommands;
import pl.themolka.arcade.command.GeneralCommands;
import pl.themolka.arcade.command.Sender;
import pl.themolka.arcade.event.Priority;
import pl.themolka.arcade.game.CycleCountdown;
import pl.themolka.arcade.game.GameManager;
import pl.themolka.arcade.game.GameModule;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.game.RestartCountdown;
import pl.themolka.arcade.game.ServerDescriptionEvent;
import pl.themolka.arcade.goal.GoalCompleteEvent;
import pl.themolka.arcade.task.Countdown;
import pl.themolka.arcade.time.Time;
import pl.themolka.arcade.time.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatchGame extends GameModule {
    private boolean autoCycle;
    private boolean autoStart;
    private int defaultStartCountdown;
    private Match match;
    private Observers observers;
    private MatchStartCountdown startCountdown;

    public MatchGame(boolean autoCycle, boolean autoStart, int defaultStartCountdown, Observers observers) {
        this.autoCycle = autoCycle;
        this.autoStart = autoStart;
        this.defaultStartCountdown = defaultStartCountdown;
        this.observers = observers;
    }

    @Override
    public void onEnable() {
        this.match = new Match(this.getPlugin(), this.getGame(), this.getObservers());
        this.getObservers().setMatch(this.getMatch());

        this.startCountdown = new MatchStartCountdown(this.getPlugin(), this.getMatch());
        this.getStartCountdown().setGame(this.getGame());

        Team bukkit = Observers.createBukkitTeam(this.getGame().getScoreboard().getScoreboard(), this.getObservers());
        this.getObservers().setBukkit(bukkit);

        this.getGame().setMetadata(MatchModule.class, MatchModule.METADATA_MATCH, this.getMatch());

        for (GamePlayer player : this.getGame().getPlayers()) {
            this.getObservers().join(player, false);
        }
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

    public Match getMatch() {
        return this.match;
    }

    public Observers getObservers() {
        return this.observers;
    }

    public MatchStartCountdown getStartCountdown() {
        return this.startCountdown;
    }

    public void handleBeginCommand(Sender sender, int seconds, boolean force) {
        String message = "Starting";
        if (force) {
            message = "Force starting";
        }

        if (this.getMatch().isStarting()) {
            sender.sendSuccess(message + " the match in " + seconds + " seconds...");
            this.getMatch().setForceStart(force);
            this.startCountdown(seconds);
        } else {
            throw new CommandException("The match is not in the starting state.");
        }
    }

    public void handleEndCommand(Sender sender, boolean auto, String winnerQuery, boolean draw) {
        if (this.getMatch().isRunning()) {
            MatchWinner winner = null;
            if (auto) {
                winner = this.getMatch().getWinner();

                if (winner == null) {
                    throw new CommandException("No winners are currently winning.");
                }
            } else if (draw) {
                winner = this.getMatch().getDrawWinner();
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

    public List<String> handleEndCompleter(Sender sender, CommandContext context) {
        String request = context.getParams(0);
        if (request == null) {
            request = "";
        }

        List<String> results = new ArrayList<>();
        for (MatchWinner winner : this.getMatch().getWinnerList()) {
            if (winner.getName().toLowerCase().startsWith(request.toLowerCase())) {
                results.add(winner.getName());
            }
        }

        return results;
    }

    public boolean isAutoCycle() {
        return this.autoCycle;
    }

    public boolean isAutoStart() {
        return this.autoStart;
    }

    public int startCountdown(int seconds) {
        this.getStartCountdown().cancelCountdown();
        if (!this.getStartCountdown().isTaskRunning()) {
            return this.getStartCountdown().countStart(seconds);
        }

        return this.getStartCountdown().getTaskId();
    }

    @Handler(priority = Priority.HIGH)
    public void onCycleCommand(GeneralCommands.CycleCommandEvent event) {
        if (this.getMatch().isRunning()) {
            event.getSender().sendError("The match is currently running. Type /end to end the match.");
            event.setCanceled(true);
        }
    }

    @Handler(priority = Priority.LOWEST)
    public void onGoalScore(GoalCompleteEvent event) {
        if (!event.isCanceled()) {
            this.getMatch().refreshWinners();
        }
    }

    @Handler(priority = Priority.HIGHEST)
    public void onJoinWhenMatchEnded(GameCommands.JoinCommandEvent event) {
        if (this.getMatch().isCycling()) {
            event.getSender().sendError("The match has ended. " + ChatColor.GOLD + "Please wait until the server cycle.");
            event.setCanceled(true);
            event.setJoined(false);
        }
    }

    @Handler(priority = Priority.LAST)
    public void onMatchCountdownAutoStart(GameCommands.JoinCommandEvent event) {
        if (!event.isCanceled() && event.hasJoined() && !this.getStartCountdown().isTaskRunning() && this.isAutoStart()) {
            MatchStartCountdownEvent countdownEvent = new MatchStartCountdownEvent(this.getPlugin(), this.getMatch(), this.startCountdown);
            if (!countdownEvent.isCanceled()) {
                this.startCountdown(this.getDefaultStartCountdown());
            }
        }
    }

    @Handler(priority = Priority.LOWEST)
    public void onCycleCountdownAutoStart(MatchEndedEvent event) {
        if (!this.isAutoCycle()) {
            // auto cycle is disabled
            return;
        }

        GameManager games = event.getPlugin().getGames();

        Countdown countdown;
        if (games.isNextRestart()) {
            countdown = games.getRestartCountdown();
            if (!countdown.isTaskRunning()) {
                ((RestartCountdown) countdown).setDefaultDuration();
            }
        } else {
            countdown = games.getCycleCountdown();
            if (!countdown.isTaskRunning()) {
                ((CycleCountdown) countdown).setDefaultDuration();
            }
        }

        if (!countdown.isTaskRunning()) {
            countdown.countSync();
        }
    }

    @Handler(priority = Priority.NORMAL)
    public void onMatchTimeDescribe(GameCommands.GameCommandEvent event) {
        Time time = Time.ZERO;
        switch (this.getMatch().getState()) {
            case RUNNING:
                time = Time.now().minus(Time.of(this.getMatch().getStartTime()));
                break;
            case CYCLING:
                time = Time.of(this.getMatch().getTime());
                break;
            default:
                break;
        }

        event.getSender().send(ChatColor.GREEN + "Time: " + ChatColor.DARK_AQUA + TimeUtils.prettyTime(time));
    }

    @Handler(priority = Priority.NORMAL)
    public void onServerDescription(ServerDescriptionEvent event) {
        String map = ChatColor.GREEN + ChatColor.BOLD.toString() + this.getGame().getMap().getMapInfo().getName() + ChatColor.RESET;
        String cycle = ChatColor.GREEN + ChatColor.BOLD.toString() + this.getPlugin().getGames().getQueue().getNextMap() + ChatColor.RESET;

        String result = null;
        switch (this.getMatch().getState()) {
            case STARTING:
                if (this.getStartCountdown() == null) {
                    result = ChatColor.GREEN + "Starting " + map + ChatColor.GREEN + " soon...";
                    break;
                }

                long startLeft = this.getStartCountdown().getLeftSeconds();
                result = ChatColor.GREEN + "Starting " + map + ChatColor.GREEN + " in " + startLeft + " second(s)...";
                break;
            case RUNNING:
                if (this.getMatch().getStartTime() == null) {
                    break;
                }

                result = ChatColor.LIGHT_PURPLE + "Playing " + map + ChatColor.LIGHT_PURPLE + " for " +
                        TimeUtils.prettyTime(Time.now().minus(Time.of(this.getMatch().getStartTime()))) + ChatColor.LIGHT_PURPLE + "...";
            case CYCLING:
                if (this.getPlugin().getGames().isNextRestart()) {
                    if (this.getPlugin().getGames().getRestartCountdown() == null) {
                        result = ChatColor.RED + "Restarting soon...";
                        break;
                    }

                    long restartLeft = this.getPlugin().getGames().getRestartCountdown().getLeftSeconds();
                    result = ChatColor.RED + "Restarting in " + ChatColor.BOLD + restartLeft + ChatColor.RESET + " second(s)...";
                    break;
                } else if (this.getPlugin().getGames().getCycleCountdown() == null) {
                    break;
                }

                long cycleLeft = this.getPlugin().getGames().getCycleCountdown().getLeftSeconds();
                result = ChatColor.AQUA + "Cycling to " + cycle + ChatColor.AQUA + " in " + cycleLeft + " second(s)...";
                break;
        }

        if (result != null) {
            event.setDescription(result);
        }
    }
}
