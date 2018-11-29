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

package pl.themolka.arcade.attribute;

import org.bukkit.attribute.ItemAttributeModifier;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.NodeParser;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.Produces;
import pl.themolka.arcade.parser.Result;

import java.util.Collections;
import java.util.Set;

/**
 * 1K :D
 */
@Produces(BoundedItemModifier.class)
public class BoundedItemModifierParser extends NodeParser<BoundedItemModifier>
                                       implements InstallableParser {
    private Parser<AttributeKey> keyParser;
    private Parser<ItemAttributeModifier> itemModifierParser;

    @Override
    public void install(ParserContext context) throws ParserNotSupportedException {
        this.keyParser = context.type(AttributeKey.class);
        this.itemModifierParser = context.type(ItemAttributeModifier.class);
    }

    @Override
    public Set<Object> expect() {
        return Collections.singleton("bounded (keyed) item attribute modifier");
    }

    @Override
    protected Result<BoundedItemModifier> parseNode(Node node, String name, String value) throws ParserException {
        AttributeKey key = this.keyParser.parse(node.property("attribute", "attribute-key", "attributekey", "attr", "key")).orFail();
        ItemAttributeModifier itemModifier = this.itemModifierParser.parseWithDefinition(node, name, value).orFail();

        return Result.fine(node, name, value, new BoundedItemModifier(key, itemModifier));
    }
}
