package pl.themolka.arcade.score;

import pl.themolka.arcade.config.Ref;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.game.GameModuleParser;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.ParserResult;
import pl.themolka.arcade.parser.ParserUtils;
import pl.themolka.arcade.parser.Produces;

import java.util.LinkedHashSet;
import java.util.Set;

@Produces(ScoreGame.Config.class)
public class ScoreGameParser extends GameModuleParser<ScoreGame, ScoreGame.Config>
                             implements InstallableParser {
    private Parser<Score.Config> scoreParser;
    private Parser<ScoreBox.Config> scoreBoxParser;

    public ScoreGameParser() {
        super(ScoreGame.class);
    }

    @Override
    public Node define(Node source) {
        return source.firstChild("score", "scores");
    }

    @Override
    public void install(ParserContext context) throws ParserNotSupportedException {
        super.install(context);
        this.scoreParser = context.type(Score.Config.class);
        this.scoreBoxParser = context.type(ScoreBox.Config.class);
    }

    @Override
    protected ParserResult<ScoreGame.Config> parseNode(Node node, String name, String value) throws ParserException {
        Set<Score.Config> scores = new LinkedHashSet<>();
        for (Node scoreNode : node.children("score")) {
            scores.add(this.scoreParser.parse(scoreNode).orFail());
        }

        if (ParserUtils.ensureNotEmpty(scores)) {
            throw this.fail(node, name, value, "No scores defined");
        }

        Set<ScoreBox.Config> scoreBoxes = new LinkedHashSet<>();
        for (Node scoreBoxNode : node.children("score-box", "scorebox")) {
            scoreBoxes.add(this.scoreBoxParser.parse(scoreBoxNode).orFail());
        }

        return ParserResult.fine(node, name, value, new ScoreGame.Config() {
            public Ref<Set<Score.Config>> scores() { return Ref.ofProvided(scores); }
            public Ref<Set<ScoreBox.Config>> scoreBoxes() { return Ref.ofProvided(scoreBoxes); }
        });
    }
}
