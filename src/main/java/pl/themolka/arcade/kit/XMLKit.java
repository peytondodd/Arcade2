package pl.themolka.arcade.kit;

import org.jdom2.Element;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.xml.XMLParser;

public class XMLKit extends XMLParser {
    public static Kit parseKit(ArcadePlugin plugin, Element xml) {
        String id = xml.getAttributeValue("id");
        if (id == null) {
            return null;
        }

        Kit kit = new Kit(plugin, id);
        for (Element contentElement : xml.getChildren()) {
            KitContent<?> content = KitContentParser.parseForName(contentElement.getName(), contentElement);
            if (content != null) {
                kit.addContent(content);
            }
        }

        return kit;
    }
}