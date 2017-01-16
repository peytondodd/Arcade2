package pl.themolka.arcade.kit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.session.ArcadeSound;
import pl.themolka.arcade.xml.XMLLocation;
import pl.themolka.arcade.xml.XMLSound;

public class SoundContent implements KitContent<Sound> {
    private Location location;
    private float pitch = ArcadeSound.DEFAULT_PITCH;
    private final Sound result;
    private float volume = ArcadeSound.DEFAULT_VOLUME;

    public SoundContent(Sound result) {
        this.result = result;
    }

    @Override
    public void apply(GamePlayer player) {
        Location location = this.getLocation();
        if (location == null) {
            location = player.getBukkit().getLocation();
        }

        player.getPlayer().play(this.getResult(), location, this.getVolume(), this.getPitch());
    }

    @Override
    public Sound getResult() {
        return this.result;
    }

    public Location getLocation() {
        return this.location;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getVolume() {
        return this.volume;
    }

    public boolean hasLocation() {
        return this.location != null;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public static class Parser implements KitContentParser<SoundContent> {
        @Override
        public SoundContent parse(Element xml) throws DataConversionException {
            Sound sound = XMLSound.parse(xml.getTextNormalize());
            if (sound != null) {
                SoundContent content = new SoundContent(sound);
                content.setLocation(XMLLocation.parse(xml));

                Attribute pitch = xml.getAttribute("sound-pitch");
                if (pitch != null) {
                    content.setPitch(pitch.getFloatValue());
                }

                Attribute volume = xml.getAttribute("sound-volume");
                if (volume != null) {
                    content.setVolume(volume.getFloatValue());
                }

                return content;
            }

            return null;
        }
    }
}
