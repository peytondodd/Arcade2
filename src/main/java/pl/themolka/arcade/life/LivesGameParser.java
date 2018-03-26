package pl.themolka.arcade.life;

import org.bukkit.Sound;
import pl.themolka.arcade.config.Ref;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.game.GameModuleParser;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.ParserResult;
import pl.themolka.arcade.parser.Produces;
import pl.themolka.arcade.team.Team;

@Produces(LivesGame.Config.class)
public class LivesGameParser extends GameModuleParser<LivesGame, LivesGame.Config>
                             implements InstallableParser {
    private Parser<Integer> livesParser;
    private Parser<Ref> fallbackParser;
    private Parser<Boolean> announceParser;
    private Parser<Sound> soundParser;

    public LivesGameParser() {
        super(LivesGame.class);
    }

    @Override
    public Node define(Node source) {
        return source.firstChild("lives", "life");
    }

    @Override
    public void install(ParserContext context) throws ParserNotSupportedException {
        super.install(context);
        this.livesParser = context.type(Integer.class);
        this.fallbackParser = context.type(Ref.class);
        this.announceParser = context.type(Boolean.class);
        this.soundParser = context.enumType(Sound.class);
    }

    @Override
    protected ParserResult<LivesGame.Config> parseNode(Node node, String name, String value) throws ParserException {
        ParserResult<Integer> livesResult = this.livesParser.parse(node);
        int lives = node.getName().equals("life") ? livesResult.orDefault(1) : livesResult.orFail();
        // ^ 1 is the default if the node name is singular form

        Ref<Team> fallbackTeam = this.fallbackParser.parse(node.property("fallback", "return")).orDefault(Ref.empty());
        boolean announce = this.announceParser.parse(node.property("announce", "message")).orDefault(true);
        Sound sound = this.soundParser.parse(node.property("sound")).orDefault(LivesGame.DEFAULT_SOUND);

        return ParserResult.fine(node, name, value, new LivesGame.Config() {
            public int lives() { return lives; }
            public Ref<Team> fallbackTeam() { return fallbackTeam; }
            public boolean announce() { return announce; }
            public Sound sound() { return sound; }
        });
    }
}