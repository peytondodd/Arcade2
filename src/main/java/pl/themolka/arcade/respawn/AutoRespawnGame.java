package pl.themolka.arcade.respawn;

import net.engio.mbassy.listener.Handler;
import pl.themolka.arcade.config.Ref;
import pl.themolka.arcade.event.Priority;
import pl.themolka.arcade.filter.Filter;
import pl.themolka.arcade.filter.Filters;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GameModule;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.game.IGameModuleConfig;
import pl.themolka.arcade.life.PlayerDeathEvent;
import pl.themolka.arcade.time.Time;

public class AutoRespawnGame extends GameModule {
    private final Filter filter;
    private final Time cooldown;

    @Deprecated
    public AutoRespawnGame() {
        this.filter = Filters.undefined();
        this.cooldown = PlayerDeathEvent.DEFAULT_AUTO_RESPAWN_COOLDOWN;
    }

    protected AutoRespawnGame(Config config) {
        this.filter = Filters.secure(config.filter().getIfPresent());
        this.cooldown = config.cooldown();
    }

    public boolean canAutoRespawn(GamePlayer victim) {
        return this.filter.filter(victim).isNotFalse();
    }

    public Filter getFilter() {
        return this.filter;
    }

    public Time getCooldown() {
        return this.cooldown;
    }

    @Handler(priority = Priority.LAST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (this.canAutoRespawn(event.getVictim())) {
            event.setAutoRespawn(true, this.cooldown);
        }
    }

    public interface Config extends IGameModuleConfig<AutoRespawnGame> {
        default Ref<Filter> filter() { return Ref.empty(); }
        default Time cooldown() { return PlayerDeathEvent.DEFAULT_AUTO_RESPAWN_COOLDOWN; }

        @Override
        default AutoRespawnGame create(Game game) {
            return new AutoRespawnGame(this);
        }
    }
}
