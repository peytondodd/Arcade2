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

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.dom.Property;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.Produces;

import java.util.ArrayList;
import java.util.List;

@Produces(BookMeta.class)
class BookMetaParser extends ItemMetaParser.Nested<BookMeta> {
    public static final int PAGE_LIMIT = 99;

    private Parser<String> authorParser;
    private Parser<BookMeta.Generation> generationParser;
    private Parser<String> titleParser;
    private Parser<String> pageParser;

    @Override
    public void install(ParserContext context) throws ParserNotSupportedException {
        this.authorParser = context.type(String.class);
        this.generationParser = context.type(BookMeta.Generation.class);
        this.titleParser = context.type(String.class);
        this.pageParser = context.type(String.class);
    }

    @Override
    public BookMeta parse(Node root, ItemStack itemStack, BookMeta itemMeta) throws ParserException {
        Node node = root.firstChild("book");
        if (node != null) {
            Property author = root.property("author", "signed", "signed-as");
            if (author != null) {
                itemMeta.setAuthor(this.authorParser.parse(author).orFail());
            }

            Property generation = root.property("generation");
            if (generation != null) {
                itemMeta.setGeneration(this.generationParser.parse(generation).orFail());
            }

            Property title = root.property("title", "name");
            if (title != null) {
                itemMeta.setTitle(this.titleParser.parse(title).orFail());
            }

            List<String> pages = new ArrayList<>();
            for (Node page : node.children("page")) {
                if (pages.size() > PAGE_LIMIT) {
                    throw this.fail(page, "Whoops! Book page limit of " + PAGE_LIMIT + " exceed, sorry!");
                }

                pages.add(this.pageParser.parse(page).orFail());
            }

            itemMeta.setPages(pages);
        }

        return itemMeta;
    }
}
