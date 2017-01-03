package pl.themolka.arcade.item;

import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import pl.themolka.arcade.potion.XMLPotionEffect;
import pl.themolka.arcade.xml.XMLColor;
import pl.themolka.arcade.xml.XMLParser;

public class XMLItemMeta extends XMLParser {
    public static ItemMeta parse(Element xml, ItemMeta source) {
        if (source instanceof BookMeta) {
            return parseBook(xml, (BookMeta) source);
        } else if (source instanceof EnchantmentStorageMeta) {
            return parseEnchantmentStorage(xml, (EnchantmentStorageMeta) source);
        } else if (source instanceof FireworkMeta) {
            return parseFirework(xml, (FireworkMeta) source);
        } else if (source instanceof FireworkEffectMeta) {
            return parseFireworkEffect(xml, (FireworkEffectMeta) source);
        } else if (source instanceof LeatherArmorMeta) {
            return parseLeatherArmor(xml, (LeatherArmorMeta) source);
        } else if (source instanceof MapMeta) {
            return parseMap(xml, (MapMeta) source);
        } else if (source instanceof PotionMeta) {
            return parsePotion(xml, (PotionMeta) source);
        } else if (source instanceof SkullMeta) {
            return parseSkull(xml, (SkullMeta) source);
        }

        return source;
    }

    public static BookMeta parseBook(Element xml, BookMeta source) {
        Element book = xml.getChild("book");
        if (book != null) {
            Attribute author = book.getAttribute("author");
            if (author != null) {
                source.setAuthor(parseMessage(author.getValue()));
            }

            Attribute title = book.getAttribute("title");
            if (title != null) {
                source.setTitle(parseMessage(title.getValue()));
            }

            for (Element page : book.getChildren("page")) {
                source.addPage(parseMessage(page.getTextNormalize()));
            }
        }

        return source;
    }

    public static EnchantmentStorageMeta parseEnchantmentStorage(Element xml, EnchantmentStorageMeta source) {
        Element book = xml.getChild("enchanted-book");
        if (book != null) {
            for (Element enchantment : book.getChildren("enchantment")) {
                Enchantment type = Enchantment.getByName(XMLParser.parseEnumValue(enchantment.getTextNormalize()));
                int level = 1;

                try {
                    Attribute levelAttribute = enchantment.getAttribute("level");
                    if (levelAttribute != null) {
                        level = levelAttribute.getIntValue();
                    }
                } catch (DataConversionException ignored) {
                }

                source.addStoredEnchant(type, level, false);
            }
        }

        return source;
    }

    public static FireworkMeta parseFirework(Element xml, FireworkMeta source) {
        return source;
    }

    public static FireworkEffectMeta parseFireworkEffect(Element xml, FireworkEffectMeta source) {
        return source;
    }

    public static LeatherArmorMeta parseLeatherArmor(Element xml, LeatherArmorMeta source) {
        Element leatherColor = xml.getChild("leather-color");
        if (leatherColor != null) {
            Color color = XMLColor.parse(leatherColor.getTextNormalize());
            if (color != null) {
                source.setColor(color);
            }
        }

        return source;
    }

    public static MapMeta parseMap(Element xml, MapMeta source) {
        return source;
    }

    public static PotionMeta parsePotion(Element xml, PotionMeta source) {
        for (Element effectElement : xml.getChildren("potion-effect")) {
            PotionEffect effect = XMLPotionEffect.parse(effectElement);
            if (effect != null) {
                source.addCustomEffect(effect, false);
            }
        }

        return source;
    }

    public static SkullMeta parseSkull(Element xml, SkullMeta source) {
        Element skull = xml.getChild("skull");
        if (skull != null) {
            Attribute owner = skull.getAttribute("owner");
            if (owner != null) {
                source.setOwner(owner.getValue());
            }
        }

        return source;
    }
}