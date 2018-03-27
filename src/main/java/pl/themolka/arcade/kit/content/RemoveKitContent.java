package pl.themolka.arcade.kit.content;

import pl.themolka.arcade.config.Ref;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.kit.Kit;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.NestedParserName;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.ParserResult;
import pl.themolka.arcade.parser.Produces;

public class RemoveKitContent implements KitContent<Kit> {
    private final Kit result;

    protected RemoveKitContent(Config config) {
        this.result = config.result().get();
    }

    @Override
    public void apply(GamePlayer player) {
        for (KitContent<?> content : this.result.getContent()) {
            if (content instanceof RemovableKitContent) {
                ((RemovableKitContent) content).remove(player);
            }
        }
    }

    @Override
    public Kit getResult() {
        return this.result;
    }

    @NestedParserName({"remove-kit", "removekit", "remove", "detach"})
    @Produces(Config.class)
    public static class ContentParser extends BaseContentParser<Config>
                                      implements InstallableParser {
        private Parser<Ref> kitParser;

        @Override
        public void install(ParserContext context) throws ParserNotSupportedException {
            super.install(context);
            this.kitParser = context.type(Ref.class);
        }

        @Override
        protected ParserResult<Config> parseNode(Node node, String name, String value) throws ParserException {
            Ref<Kit> kit = this.kitParser.parse(node).orFail();

            return ParserResult.fine(node, name, value, new Config() {
                public Ref<Kit> result() { return kit; }
            });
        }
    }

    public interface Config extends KitContent.Config<RemoveKitContent, Kit> {
        @Override
        default RemoveKitContent create(Game game) {
            return new RemoveKitContent(this);
        }
    }
}