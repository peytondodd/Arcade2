package pl.themolka.arcade.kit.content;

import pl.themolka.arcade.config.Ref;
import pl.themolka.arcade.dom.Node;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.match.Observers;
import pl.themolka.arcade.parser.InstallableParser;
import pl.themolka.arcade.parser.NestedParserName;
import pl.themolka.arcade.parser.Parser;
import pl.themolka.arcade.parser.ParserContext;
import pl.themolka.arcade.parser.ParserException;
import pl.themolka.arcade.parser.ParserNotSupportedException;
import pl.themolka.arcade.parser.ParserResult;
import pl.themolka.arcade.parser.Produces;
import pl.themolka.arcade.team.Team;

public class TeamContent implements RemovableKitContent<Team> {
    private final Team result;
    private final boolean announce;

    protected TeamContent(Config config) {
        this.result = config.result().getOrDefault(this.defaultValue());
        this.announce = config.announce();
    }

    @Override
    public boolean isApplicable(GamePlayer player) {
        return KitContent.test(player);
    }

    @Override
    public void attach(GamePlayer player, Team value) {
        if (value != null) {
            value.join(player, this.announce(), true);
        }
    }

    @Override
    public Team defaultValue() {
        return null;
    }

    @Override
    public Team getResult() {
        return this.result;
    }

    public boolean announce() {
        return this.announce;
    }

    @NestedParserName("team")
    @Produces(Config.class)
    public static class ContentParser extends BaseRemovableContentParser<Config>
                                      implements InstallableParser {
        private Parser<Boolean> announceParser;
        private Parser<Ref> teamParser;

        @Override
        public void install(ParserContext context) throws ParserNotSupportedException {
            super.install(context);
            this.announceParser = context.type(Boolean.class);
            this.teamParser = context.type(Ref.class);
        }

        @Override
        protected ParserResult<Config> parseNode(Node node, String name, String value) throws ParserException {
            Ref<Team> team = this.reset(node) ? Config.DEFAULT_TEAM : this.teamParser.parse(node).orFail();
            boolean announce = this.announceParser.parse(node.property("announce", "message")).orDefault(Config.DEFAULT_ANNOUNCE);

            return ParserResult.fine(node, name, value, new Config() {
                public Ref<Team> result() { return team; }
                public boolean announce() { return announce; }
            });
        }
    }

    public interface Config extends RemovableKitContent.Config<TeamContent, Team> {
        Ref<Team> DEFAULT_TEAM = Ref.of(Observers.OBSERVERS_TEAM_ID);
        boolean DEFAULT_ANNOUNCE = true;

        default Ref<Team> result() { return DEFAULT_TEAM; }
        default boolean announce() { return DEFAULT_ANNOUNCE; }

        @Override
        default TeamContent create(Game game) {
            return new TeamContent(this);
        }
    }
}
