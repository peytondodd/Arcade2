/*
 * Copyright 2018 Aleksander Jagiełło
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.themolka.arcade.match;

import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.event.Cancelable;

public class MatchEndEvent extends MatchEvent implements Cancelable {
    private boolean cancel;
    private final boolean forceEnd;
    private final MatchWinner winner;

    public MatchEndEvent(ArcadePlugin plugin, Match match, MatchWinner winner, boolean forceEnd) {
        super(plugin, match);

        this.forceEnd = forceEnd;
        this.winner = winner;
    }

    @Override
    public boolean isCanceled() {
        return this.cancel;
    }

    @Override
    public void setCanceled(boolean cancel) {
        this.cancel = cancel;
    }

    public MatchWinner getWinner() {
        return this.winner;
    }

    public boolean isForceEnd() {
        return this.forceEnd;
    }
}
