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

package pl.themolka.arcade.util;

import java.util.Random;
import java.util.UUID;

public class FastUUID extends RandomSource<UUID> {
    public FastUUID() {
    }

    public FastUUID(long seed) {
        super(seed);
    }

    public FastUUID(Random random) {
        super(random);
    }

    @Override
    public UUID random() {
        return new UUID(this.random.nextLong(), this.random.nextLong());
    }
}
