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

package pl.themolka.arcade.cycle;

import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.map.OfflineMap;

public class CycleStartEvent extends CycleEvent {
    private final int seconds;

    public CycleStartEvent(ArcadePlugin plugin, OfflineMap nextMap, int seconds) {
        super(plugin, nextMap);

        this.seconds = seconds;
    }

    public int getSeconds() {
        return this.seconds;
    }
}
