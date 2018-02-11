package pl.themolka.arcade.kit;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.xml.XMLParser;

public class XMLKit extends XMLParser {
    public static Kit parse(ArcadePlugin plugin, Element xml) {
        String id = xml.getAttributeValue("id");
        if (id == null) {
            return null;
        }

        Kit kit = new Kit(plugin, id);
        for (Element contentElement : xml.getChildren()) {
            try {
                KitContent<?> content = KitContentType.parseForName(contentElement.getName(), contentElement);
                if (content != null) {
                    kit.addContent(content);
                }
            } catch (DataConversionException ex) {
                return null;
            }
        }

        Attribute inheritAttribute = xml.getAttribute("inherit");
        if (inheritAttribute != null) {
            for (String inherit : parseArray(inheritAttribute.getValue())) {
                kit.addInherit(inherit);
            }
        }

        return kit;
    }
}
