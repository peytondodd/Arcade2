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

package pl.themolka.arcade.region;

import pl.themolka.arcade.config.Ref;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.game.GameModuleParser;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.ParserUtils;
import pl.themolka.arcade.parser.Produces;
import pl.themolka.arcade.parser.Result;

import java.util.LinkedHashSet;
import java.util.Set;

@Produces(RegionsGame.Config.class)
public class RegionsGameParser extends GameModuleParser<RegionsGame, RegionsGame.Config>
                               implements InstallableParser {
    private Parser<AbstractRegion.Config> regionParser;

    public RegionsGameParser() {
        super(RegionsGame.class);
    }

    @Override
    public Node define(Node source) {
        return source.firstChild("regions", "region");
    }

    @Override
    public void install(ParserContext context) throws ParserNotSupportedException {
        super.install(context);
        this.regionParser = context.type(AbstractRegion.Config.class);
    }

    @Override
    protected Result<RegionsGame.Config> parseNode(Node node, String name, String value) throws ParserException {
        Set<AbstractRegion.Config<?>> regions = new LinkedHashSet<>();
        for (Node regionNode : node.children()) {
            regions.add(this.regionParser.parse(regionNode).orFail());
        }

        if (ParserUtils.ensureNotEmpty(regions)) {
            throw this.fail(node, name, value, "No regions defined");
        }

        return Result.fine(node, name, value, new RegionsGame.Config() {
            public Ref<Set<AbstractRegion.Config<?>>> regions() { return Ref.ofProvided(regions); }
        });
    }
}
