package pl.themolka.arcade.capture.flag.state;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bukkit.Location;
import pl.themolka.arcade.capture.flag.Flag;
import pl.themolka.arcade.capture.flag.FlagSpawn;
import pl.themolka.arcade.goal.GoalHolder;
import pl.themolka.arcade.match.Match;

public class SpawnedState extends FlagState.Permanent implements FlagState.PhysicalFlag {
    private final Location location;
    private final FlagSpawn spawn;

    public SpawnedState(Flag flag, Location location, FlagSpawn spawn) {
        super(flag);

        this.location = location;
        this.spawn = spawn;
    }

    @Override
    public FlagState copy() {
        return new SpawnedState(this.flag, this.location, this.spawn);
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public void heartbeat(long ticks, Match match, GoalHolder owner) {
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, TO_STRING_STYLE)
                .append("flag", this.flag)
                .append("location", this.location)
                .build();
    }

    public FlagSpawn getSpawn() {
        return this.spawn;
    }
}
