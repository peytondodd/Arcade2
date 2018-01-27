package pl.themolka.arcade.life;

import org.bukkit.Sound;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GameModule;
import pl.themolka.arcade.match.Match;
import pl.themolka.arcade.match.MatchGame;
import pl.themolka.arcade.match.MatchModule;
import pl.themolka.arcade.module.Module;
import pl.themolka.arcade.module.ModuleInfo;
import pl.themolka.arcade.xml.XMLParser;
import pl.themolka.arcade.xml.XMLSound;

@ModuleInfo(id = "Lives",
        dependency = {
                MatchModule.class})
public class LivesModule extends Module<LivesGame> {
    public static final Sound DEFAULT_SOUND = Sound.ENTITY_IRONGOLEM_DEATH;

    @Override
    public LivesGame buildGameModule(Element xml, Game game) throws JDOMException {
        int lives = XMLParser.parseInt(xml.getChildTextNormalize("lives"), 1);
        if (lives > 0) {
            GameModule matchGame = game.getModule(MatchModule.class);
            if (matchGame == null || !(matchGame instanceof MatchGame)) {
                return null;
            }

            Match match = ((MatchGame) matchGame).getMatch();
            boolean broadcast = XMLParser.parseBoolean(xml.getChildTextNormalize("broadcast"), true);
            Sound sound = XMLSound.parse(xml.getChildTextNormalize("sound"), DEFAULT_SOUND);

            return new LivesGame(match, lives, broadcast, sound);
        }

        return null;
    }
}
