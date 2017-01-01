package pl.themolka.arcade.team;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.jdom2.Element;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.match.Match;
import pl.themolka.arcade.match.Observers;

public class XMLObserversTeam extends XMLTeam {
    public static Observers parse(Element xml, ArcadePlugin plugin, Match match) {
        Observers observers = new Observers(plugin, match);

        if (xml != null) {
            ChatColor color = parseColor(xml);
            if (color != null) {
                observers.setColor(color);
            }

            DyeColor dyeColor = parseDyeColor(xml);
            if (dyeColor != null) {
                observers.setDyeColor(dyeColor);
            }

            String name = parseName(xml);
            if (name != null) {
                observers.setName(name);
            }
        }

        return observers;
    }
}
