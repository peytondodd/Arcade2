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

package pl.themolka.arcade.spawn;

public interface Direction {
    Direction CONSTANT = new ConstantDirection();
    Direction ENTITY = new EntityDirection();
    Direction RELATIVE = new RelativeDirection();
    Direction TRANSLATE = new TranslateDirection();

    default float getValue(float constant, float entity) {
        return entity;
    }
}

class ConstantDirection implements Direction {
    @Override
    public float getValue(float constant, float entity) {
        return constant;
    }
}

class EntityDirection implements Direction {
    @Override
    public float getValue(float constant, float entity) {
        return entity;
    }
}

class RelativeDirection implements Direction {
    static final float ZERO = 0F;

    @Override
    public float getValue(float constant, float entity) {
        return entity != ZERO ? -entity : ZERO;
    }
}

class TranslateDirection implements Direction {
    @Override
    public float getValue(float constant, float entity) {
        return entity + constant;
    }
}
