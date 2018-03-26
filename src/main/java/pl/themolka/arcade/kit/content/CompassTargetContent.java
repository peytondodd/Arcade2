package pl.themolka.arcade.kit.content;

import org.bukkit.util.Vector;
import pl.themolka.arcade.config.Ref;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.NestedParserName;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.ParserResult;
import pl.themolka.arcade.parser.Produces;

public class CompassTargetContent implements KitContent<Vector> {
    private final Vector result;

    protected CompassTargetContent(Config config) {
        this.result = config.result().get();
    }

    @Override
    public boolean isApplicable(GamePlayer player) {
        return KitContent.testBukkit(player);
    }

    @Override
    public void apply(GamePlayer player) {
        player.getBukkit().setCompassTarget(this.result.toLocation(null));
    }

    @Override
    public Vector getResult() {
        return this.result;
    }

    @NestedParserName({"compass-target", "compasstarget", "compass"})
    @Produces(Config.class)
    public static class ContentParser extends BaseContentParser<Config>
                                      implements InstallableParser {
        private Parser<Vector> targetParser;

        @Override
        public void install(ParserContext context) throws ParserNotSupportedException {
            super.install(context);
            this.targetParser = context.type(Vector.class);
        }

        @Override
        protected ParserResult<Config> parseNode(Node node, String name, String value) throws ParserException {
            Vector target = this.targetParser.parse(node).orFail();

            return ParserResult.fine(node, name, value, new Config() {
                public Ref<Vector> result() { return Ref.ofProvided(target); }
            });
        }
    }

    public interface Config extends KitContent.Config<CompassTargetContent, Vector> {
        @Override
        default CompassTargetContent create(Game game) {
            return new CompassTargetContent(this);
        }
    }
}
