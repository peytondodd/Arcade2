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
import pl.themolka.arcade.dom.Element;
import pl.themolka.arcade.parser.ElementParser;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.Produces;
import pl.themolka.arcade.parser.Result;

import java.util.Collections;
import java.util.Set;

@Produces(AttributeModifier.Operation.class)
public class OperationParser extends ElementParser<AttributeModifier.Operation>
                             implements InstallableParser {
    private Parser<String> operationParser;
    private Parser<AttributeModifier.Operation> enumParser;
    private Parser<Integer> vanillaIdParser;

    @Override
    public void install(ParserContext context) throws ParserNotSupportedException {
        this.operationParser = context.text();
        this.enumParser = context.enumType(AttributeModifier.Operation.class);
        this.vanillaIdParser = context.type(Integer.class);
    }

    @Override
    public Set<Object> expect() {
        return Collections.singleton("attribute modifier operation, such as 'add'");
    }

    @Override
    protected Result<AttributeModifier.Operation> parseElement(Element element, String name, String value) throws ParserException {
        String operationInput = this.operationParser.parseWithDefinition(element, name, value).orDefaultNull();
        AttributeModifier.Operation operation = this.getOperation(operationInput);

        if (operation == null) {
            operation = this.parseOperationEnum(element, name, value);
        }

        if (operation == null) {
            operation = this.parseOperationVanillaId(element, name, value);
        }

        if (operation == null) {
            throw this.fail(element, name, value, "Unknown attribute modifier operation type");
        }

        return Result.fine(element, name, value, operation);
    }

    protected AttributeModifier.Operation getOperation(String input) {
        switch (input.toLowerCase()) {
            case "add": return AttributeModifier.Operation.ADD_NUMBER;
            case "base": return AttributeModifier.Operation.ADD_SCALAR;
            case "multiply": return AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            default: return null;
        }
    }

    protected AttributeModifier.Operation parseOperationEnum(Element element, String name, String value) throws ParserException {
        return this.enumParser.parseWithDefinition(element, name, value).orNull();
    }

    protected AttributeModifier.Operation parseOperationVanillaId(Element element, String name, String value) throws ParserException {
        Integer vanillaId = this.vanillaIdParser.parseWithDefinition(element, name, value).orNull();
        return vanillaId != null && isValidOperationVanillaId(vanillaId)
                ? AttributeModifier.Operation.fromOpcode(vanillaId)
                : null;
    }

    private static boolean isValidOperationVanillaId(int vanillaId) {
        return vanillaId >= 0 && vanillaId < AttributeModifier.Operation.values().length;
    }
}
