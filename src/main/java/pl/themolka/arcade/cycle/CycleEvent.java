package pl.themolka.arcade.cycle;

import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.game.GameEvent;
import pl.themolka.arcade.map.OfflineMap;

public class CycleEvent extends GameEvent {
    private final OfflineMap nextMap;

    public CycleEvent(ArcadePlugin plugin, OfflineMap nextMap) {
        super(plugin);

        this.nextMap = nextMap;
    }

    public OfflineMap getNextMap() {
        return this.nextMap;
    }
}