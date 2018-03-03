package pl.themolka.arcade.potion;

import org.bukkit.Color;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jdom2.Attribute;
import org.jdom2.Element;
import pl.themolka.arcade.time.Time;
import pl.themolka.arcade.time.XMLTime;
import pl.themolka.arcade.xml.XMLColor;
import pl.themolka.arcade.xml.XMLParser;

/**
 * @deprecated {@link PotionEffectParser}
 */
@Deprecated
public class XMLPotionEffect extends XMLParser {
    public static PotionEffect parse(Element xml) {
        PotionEffectBuilder builder = new PotionEffectBuilder()
                .ambient(parseAmbient(xml))
                .amplifier(parseAmplifier(xml))
                .color(parseColor(xml))
                .duration(parseDuration(xml))
                .particles(parseParticles(xml))
                .type(parseType(xml));

        return builder.build();
    }

    private static boolean parseAmbient(Element xml) {
        return parseBoolean(xml.getAttributeValue("ambient"), false);
    }

    private static int parseAmplifier(Element xml) {
        return parseInt(xml.getAttributeValue("amplifier"), 1);
    }

    private static Color parseColor(Element xml) {
        return XMLColor.parse(xml);
    }

    private static int parseDuration(Element xml) {
        Attribute attribute = xml.getAttribute("duration");
        if (attribute != null) {
            Time time = XMLTime.parse(attribute, Time.SECOND);

            if (!time.isForever()) {
                try {
                    return Math.toIntExact(time.toTicks());
                } catch (ArithmeticException ignored) {
                }
            }

            return Integer.MAX_VALUE;
        }

        return Math.toIntExact(Time.SECOND.toTicks());
    }

    private static boolean parseParticles(Element xml) {
        return parseBoolean(xml.getAttributeValue("particles"), true);
    }

    private static PotionEffectType parseType(Element xml) {
        return PotionEffectType.getByName(parseEnumValue(xml.getValue()));
    }
}
