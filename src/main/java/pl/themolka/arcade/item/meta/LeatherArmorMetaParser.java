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

package pl.themolka.arcade.item.meta;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.dom.Property;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.Produces;

@Produces(LeatherArmorMeta.class)
class LeatherArmorMetaParser extends ItemMetaParser.Nested<LeatherArmorMeta>
                             implements InstallableParser {
    private Parser<Color> colorParser;

    @Override
    public void install(ParserContext context) throws ParserNotSupportedException {
        this.colorParser = context.type(Color.class);
    }

    @Override
    public LeatherArmorMeta parse(Node root, ItemStack itemStack, LeatherArmorMeta itemMeta) throws ParserException {
        Node node = root.firstChild("leather-armor", "leather", "armor");
        if (node != null) {
            Property color = root.property("color");
            if (color != null) {
                itemMeta.setColor(this.colorParser.parse(color).orFail());
            }
        }

        return itemMeta;
    }
}
