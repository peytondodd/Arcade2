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

package pl.themolka.arcade.region.tracker;

import org.bukkit.Location;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.region.Region;

import java.util.Objects;

public class RegionTrackerFilter implements PlayerTrackerFilter {
    private final Region region;

    public RegionTrackerFilter(Region region) {
        this.region = Objects.requireNonNull(region, "region cannot be null");
    }

    @Override
    public boolean canTrack(GamePlayer player, Location at) {
        return this.region.contains(at);
    }

    public Region getRegion() {
        return this.region;
    }
}
