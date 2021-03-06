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

import org.bukkit.attribute.AttributeModifier;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.NodeParser;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.Produces;
import pl.themolka.arcade.parser.Result;
import pl.themolka.arcade.util.FastUUID;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Produces(AttributeModifier.class)
public class AttributeModifierParser extends NodeParser<AttributeModifier>
                                     implements InstallableParser {
    public static final String NAMESPACE = FixedAttributeKey.DEFAULT_NAMESPACE;
    public static final String SEPARATOR = FixedAttributeKey.NAMESPACE_SEPARATOR;

    private Parser<AttributeModifier.Operation> operationParser;
    private Parser<Double> amountParser;

    private final FastUUID fastUUID = new FastUUID();

    @Override
    public void install(ParserContext context) throws ParserNotSupportedException {
        this.operationParser = context.type(AttributeModifier.Operation.class);
        this.amountParser = context.type(Double.class);
    }

    @Override
    public Set<Object> expect() {
        return Collections.singleton("attribute modifier");
    }

    @Override
    protected Result<AttributeModifier> parsePrimitive(Node node, String name, String value) throws ParserException {
        AttributeModifier.Operation operation = this.operationParser.parse(node.property("operation"))
                .orDefault(AttributeModifier.Operation.ADD_NUMBER);
        double amount = this.amountParser.parseWithDefinition(node, name, value).orFail();

        return Result.fine(node, name, value, this.createModifier(this.fastUUID.random(), operation, amount));
    }

    protected AttributeModifier createModifier(UUID uniqueId, AttributeModifier.Operation operation, double amount) {
        return new AttributeModifier(uniqueId, this.computeName(uniqueId), amount, operation);
    }

    protected String computeName(UUID uniqueId) {
        // eg. 'arcade.2b5f34f6-fb05-4852-a86c-2e03bccbdf89'
        return FixedAttributeKey.computeKey(NAMESPACE, SEPARATOR, uniqueId.toString());
    }
}
