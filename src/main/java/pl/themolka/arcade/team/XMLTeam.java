package pl.themolka.arcade.team;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.game.PlayerApplicable;
import pl.themolka.arcade.kit.KitsGame;
import pl.themolka.arcade.map.ArcadeMap;
import pl.themolka.arcade.match.MatchApplyContext;
import pl.themolka.arcade.team.apply.TeamKitApply;
import pl.themolka.arcade.team.apply.TeamSpawnsApply;
import pl.themolka.arcade.util.Color;
import pl.themolka.arcade.xml.XMLChatColor;
import pl.themolka.arcade.xml.XMLDyeColor;
import pl.themolka.arcade.xml.XMLParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLTeam extends XMLParser {
    private static final Map<String, MatchApplyContext.EventType> EVENTS = new HashMap<>();

    static {
        EVENTS.put("start", MatchApplyContext.EventType.MATCH_START);
        EVENTS.put("match-start", MatchApplyContext.EventType.MATCH_START);

        EVENTS.put("join", MatchApplyContext.EventType.PLAYER_PLAY);
        EVENTS.put("player-join", MatchApplyContext.EventType.PLAYER_PLAY);
        EVENTS.put("play", MatchApplyContext.EventType.PLAYER_PLAY);
        EVENTS.put("player-player", MatchApplyContext.EventType.PLAYER_PLAY);

        EVENTS.put("respawn", MatchApplyContext.EventType.PLAYER_RESPAWN);
        EVENTS.put("player-respawn", MatchApplyContext.EventType.PLAYER_RESPAWN);
    }

    public static Team parse(ArcadeMap map, Element xml, ArcadePlugin plugin, KitsGame kits) {
        String name = parseName(xml);
        ChatColor color = parseColor(xml);
        DyeColor dye = parseDyeColor(xml);
        boolean friendly = parseFriendlyFire(xml);
        int min = parseMinPlayers(xml);
        int slots = parseSlots(xml);
        int overfill = parseMaxPlayers(xml);

        if (color == null) {
            color = Color.randomChat();
        }

        if (dye == null) {
            dye = Color.ofChat(color).toDye();
        }

        if (slots == 0) {
            slots = Integer.MAX_VALUE;
        }

        if (overfill == 0) {
            if (slots == Integer.MAX_VALUE) {
                overfill = slots;
            } else {
                // overfill = slots + 25%
                overfill = slots + (slots / 4);
            }
        }

        Team team = new TeamBuilder(plugin, parseId(xml))
                .color(color)
                .dyeColor(dye)
                .friendlyFire(friendly)
                .maxPlayers(overfill)
                .minPlayers(min)
                .name(name)
                .slots(slots)
                .build();

        for (Element applyItem : xml.getChildren("apply")) {
            ApplyResultEntry entry = parseApply(map, applyItem, kits);

            if (entry != null) {
                for (MatchApplyContext.EventType event : entry.getEvents()) {
                    team.getApplyContext().addContent(entry.getContentUnite(), event);
                }
            }
        }

        return team;
    }

    public static String parseId(Element xml) {
        Attribute attribute = xml.getAttribute("id");
        if (attribute != null) {
            return attribute.getValue();
        }

        return RandomStringUtils.randomAlphanumeric(5);
    }

    public static ChatColor parseColor(Element xml) {
        Attribute attribute = xml.getAttribute("color");
        if (attribute != null) {
            return XMLChatColor.parse(attribute);
        }

        return null;
    }

    public static DyeColor parseDyeColor(Element xml) {
        Attribute attribute = xml.getAttribute("dye-color");
        if (attribute != null) {
            return XMLDyeColor.parse(attribute);
        }

        return null;
    }

    public static boolean parseFriendlyFire(Element xml) {
        return XMLParser.parseBoolean(xml.getAttributeValue("friendly-fire"), false) ||
                XMLParser.parseBoolean(xml.getAttributeValue("friendlyfire"), false);
    }

    public static int parseMaxPlayers(Element xml) {
        Attribute attribute = xml.getAttribute("overfill");
        if (attribute != null) {
            try {
                return attribute.getIntValue();
            } catch (DataConversionException ignored) {
            }
        }

        return 0;
    }

    public static int parseMinPlayers(Element xml) {
        Attribute attribute = xml.getAttribute("min-players");
        if (attribute != null) {
            try {
                return attribute.getIntValue();
            } catch (DataConversionException ignored) {
            }
        }

        return 0;
    }

    public static String parseName(Element xml) {
        Attribute attribute = xml.getAttribute("name");
        if (attribute != null) {
            String name = attribute.getValue();
            if (name.length() <= Team.NAME_MAX_LENGTH) {
                return name;
            }
        }

        return RandomStringUtils.randomAlphanumeric(5);
    }

    public static int parseSlots(Element xml) {
        Attribute attribute = xml.getAttribute("slots");
        if (attribute != null) {
            try {
                return attribute.getIntValue();
            } catch (DataConversionException ignored) {
            }
        }

        return 0;
    }

    //
    // Applicable
    //

    public static ApplyResultEntry parseApply(ArcadeMap map, Element xml, KitsGame kits) {
        // content
        List<PlayerApplicable> content = new ArrayList<>();
        for (Element apply : xml.getChildren()) {
            PlayerApplicable result = parseApplyItem(map, apply, kits);

            if (result != null) {
                content.add(result);
            }
        }

        if (content.isEmpty()) {
            return null;
        }

        // events
        List<MatchApplyContext.EventType> events = new ArrayList<>();
        for (Attribute attribute : xml.getAttributes()) {
            String name = attribute.getName().toLowerCase();
            MatchApplyContext.EventType event = EVENTS.get(name);

            if (event != null && parseBoolean(attribute.getValue(), false)) {
                events.add(event);
            }
        }

        if (events.isEmpty()) {
            return null;
        }

        return new ApplyResultEntry(content, events);
    }

    private static PlayerApplicable parseApplyItem(ArcadeMap map, Element xml, KitsGame kits) {
        switch (xml.getName().toLowerCase()) {
            case "kit":
                return TeamKitApply.parse(map, xml, kits);
            case "spawns":
                return TeamSpawnsApply.parse(map, xml);
            default:
                return null;
        }
    }

    public static class ApplyResultEntry {
        private final List<PlayerApplicable> content;
        private final List<MatchApplyContext.EventType> events;

        private ApplyResultEntry(List<PlayerApplicable> content, List<MatchApplyContext.EventType> events) {
            this.content = content;

            if (events != null && !events.isEmpty()) {
                this.events = events;
            } else {
                this.events = Collections.singletonList(MatchApplyContext.NONE);
            }
        }

        public List<PlayerApplicable> getContent() {
            return new ArrayList<>(this.content);
        }

        public PlayerApplicable getContentUnite() {
            return new PlayerApplicable() {
                @Override
                public void apply(GamePlayer player) {
                    for (PlayerApplicable content : getContent()) {
                        content.apply(player);
                    }
                }
            };
        }

        public List<MatchApplyContext.EventType> getEvents() {
            return new ArrayList<>(this.events);
        }
    }
}
