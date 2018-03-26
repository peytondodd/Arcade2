package pl.themolka.arcade.match;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.bossbar.BarPriority;
import pl.themolka.arcade.bossbar.BossBar;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.session.ArcadePlayer;
import pl.themolka.arcade.session.ArcadeSound;
import pl.themolka.arcade.task.PrintableCountdown;
import pl.themolka.arcade.util.Percentage;

import java.time.Duration;

public class MatchStartCountdown extends PrintableCountdown {
    public static final int BAR_PRIORITY = BarPriority.HIGHEST;

    private final ArcadePlugin plugin;

    private boolean cannotStart;
    private final Match match;

    public MatchStartCountdown(ArcadePlugin plugin, Match match) {
        super(plugin.getTasks(), null);
        this.plugin = plugin;

        this.match = match;
        this.setBossBar(new BossBar(BarColor.GREEN, BarStyle.SOLID));
    }

    @Override
    public void onCancel() {
        this.removeBossBars(this.plugin.getPlayers());
    }

    @Override
    public void onDone() {
        this.removeBossBars(this.plugin.getPlayers());
        this.getMatch().start(false);
    }

    @Override
    public void onTick(long ticks) {
        Game game = this.plugin.getGames().getCurrentGame();
        if (game == null) {
            return;
        } else if (this.getProgress() > 1) {
            return;
        }

        String message = this.getPrintMessage(this.getStartMessage());
        this.getBossBar().setProgress(Percentage.finite(this.getProgress()));
        this.getBossBar().setText(new TextComponent(message));

        for (ArcadePlayer online : this.plugin.getPlayers()) {
            GamePlayer player = online.getGamePlayer();
            if (player != null) {
                player.getBossBarContext().addBossBar(this.getBossBar(), BAR_PRIORITY);
            }
        }

        this.getBossBar().setVisible(true);
    }

    @Override
    public void onUpdate(long seconds, long secondsLeft) {
        MatchStartCountdownEvent event = new MatchStartCountdownEvent(this.plugin, this.match, this);
        this.plugin.getEventBus().publish(event);

        if (!this.getMatch().isForceStart() && (event.isCanceled() || this.cannotStart)) {
            this.cannotStart = false;
            this.cancelCountdown();
            return;
        } else if (!this.isPrintable(secondsLeft)) {
            return;
        }

        Game game = this.plugin.getGames().getCurrentGame();
        if (game == null) {
            return;
        }

        this.printMessage();
        this.printCount();
        this.playSound();
    }

    public int countStart(int seconds) {
        this.setDuration(Duration.ofSeconds(seconds));

        if (this.isTaskRunning()) {
            this.cancelCountdown();
        }

        MatchStartCountdownEvent event = new MatchStartCountdownEvent(this.plugin, this.match, this);
        this.plugin.getEventBus().publish(event);

        if (!event.isCanceled()) {
            this.cancelCountdown();
            return this.countSync();
        }

        return -1;
    }

    private String getCancelMessage() {
        String message = ChatColor.GREEN + "Start countdown has been canceled";

        if (!this.getMatch().isForceStart() && this.cannotStart) {
            return message + " due the match cannot start.";
        }
        return message + ".";
    }

    public Match getMatch() {
        return this.match;
    }

    private String getStartMessage() {
        String message = ChatColor.GREEN + "Match starting in " + ChatColor.GOLD + ChatColor.BOLD +
                FIELD_TIME_LEFT + ChatColor.RESET + ChatColor.GREEN + ".";

        if (this.isDone()) {
            return message + "..";
        }
        return message;
    }

    private void playSound() {
        long left = this.getLeftSeconds();

        ArcadeSound sound = null;
        if (left == 0) {
            sound = ArcadeSound.STARTED;
        } else if (left == 1 || left == 2 || left == 3) {
            sound = ArcadeSound.STARTING;
        }

        if (sound != null) {
            for (ArcadePlayer player : this.plugin.getPlayers()) {
                player.play(sound);
            }
        }
    }

    private void printCount() {
        long left = this.getLeftSeconds();

        String text = null;
        if (left == 0) {
            text = ChatColor.RED + ChatColor.UNDERLINE.toString() + "GO!";
        } else if (left == 1 || left == 2 || left == 3) {
            text = ChatColor.YELLOW + Long.toString(left);
        }

        if (text != null) {
            String start = ChatColor.GREEN + ChatColor.ITALIC.toString() + "The match has started.";

            for (ArcadePlayer online : this.plugin.getPlayers()) {
                GamePlayer player = online.getGamePlayer();
                if (player == null) {
                    continue;
                }

                if (!this.getMatch().getObservers().contains(online)) {
                    player.getBukkit().sendTitle(text, "", 3, 5, 30);
                } else if (left == 0) {
                    player.getBukkit().sendTitle(text, start, 3, 60, 10);
                }
            }
        }
    }

    private void printMessage() {
        String message = this.getPrintMessage(this.getStartMessage());
        for (ArcadePlayer player : this.plugin.getPlayers()) {
            player.getPlayer().send(message);
        }

        this.plugin.getLogger().info(ChatColor.stripColor(message));
    }
}
