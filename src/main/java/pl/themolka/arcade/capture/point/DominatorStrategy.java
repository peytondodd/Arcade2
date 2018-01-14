package pl.themolka.arcade.capture.point;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.goal.GoalHolder;

import java.util.Collection;
import java.util.Map;

public abstract class DominatorStrategy {
    public static final DominatorStrategy EVERYBODY = new EverybodyStrategy();
    public static final DominatorStrategy EXCLUSIVE = new ExclusiveStrategy();
    public static final DominatorStrategy LEAD = new LeadStrategy();
    public static final DominatorStrategy MAJORITY = new MajorityStrategy();
    public static final DominatorStrategy NOBODY = new NobodyStrategy();

    public static final Multimap<GoalHolder, GamePlayer> EMPTY_DOMINATORS = ArrayListMultimap.create();

    public abstract Multimap<GoalHolder, GamePlayer> getDominators(Multimap<GoalHolder, GamePlayer> competitors);
}

class EverybodyStrategy extends DominatorStrategy {
    @Override
    public Multimap<GoalHolder, GamePlayer> getDominators(Multimap<GoalHolder, GamePlayer> competitors) {
        return competitors;
    }
}

class ExclusiveStrategy extends DominatorStrategy {
    @Override
    public Multimap<GoalHolder, GamePlayer> getDominators(Multimap<GoalHolder, GamePlayer> competitors) {
        if (competitors.size() == 1) {
            return competitors;
        }

        return null;
    }
}

class LeadStrategy extends ExclusiveStrategy {
    @Override
    public Multimap<GoalHolder, GamePlayer> getDominators(Multimap<GoalHolder, GamePlayer> competitors) {
        Multimap<GoalHolder, GamePlayer> dominators = ArrayListMultimap.create();
        for (Map.Entry<GoalHolder, Collection<GamePlayer>> entry : competitors.asMap().entrySet()) {
            GoalHolder competitor = entry.getKey();
            Collection<GamePlayer> players = entry.getValue();

            int playerCount = players.size();
            int dominatorCount = 0;

            for (Map.Entry<GoalHolder, Collection<GamePlayer>> dominator : dominators.asMap().entrySet()) {
                // All competitors have same player counts - break the loop.
                dominatorCount = dominator.getValue().size();
                break;
            }

            if (playerCount < dominatorCount) {
                continue;
            } else if (playerCount > dominatorCount) {
                // Do not clear the map when player counts are equal.
                dominators.clear();
            }

            dominators.putAll(competitor, players);
        }

        return dominators;
    }
}

class MajorityStrategy extends DominatorStrategy {
    static final LeadStrategy lead = new LeadStrategy();

    @Override
    public Multimap<GoalHolder, GamePlayer> getDominators(Multimap<GoalHolder, GamePlayer> competitors) {
        Multimap<GoalHolder, GamePlayer> dominators = lead.getDominators(competitors);
        if (dominators.size() != 1) {
            return null;
        }

        GoalHolder dominator = null;
        Collection<GamePlayer> players = null;

        for (Map.Entry<GoalHolder, Collection<GamePlayer>> entry : dominators.asMap().entrySet()) {
            dominator = entry.getKey();
            players = entry.getValue();
            break;
        }

        if (dominator != null && players != null) {
            int playerCount = players.size();
            for (Map.Entry<GoalHolder, Collection<GamePlayer>> entry : competitors.asMap().entrySet()) {
                if (!entry.getKey().equals(dominator)) {
                    playerCount -= entry.getValue().size();

                    if (playerCount <= 0) {
                        break;
                    }
                }
            }

            if (playerCount > 0) {
                return dominators;
            }
        }

        return null;
    }
}

class NobodyStrategy extends DominatorStrategy {
    @Override
    public Multimap<GoalHolder, GamePlayer> getDominators(Multimap<GoalHolder, GamePlayer> competitors) {
        return null;
    }
}