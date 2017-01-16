package pl.themolka.arcade.game;

import org.bukkit.entity.Player;
import pl.themolka.arcade.metadata.Metadata;
import pl.themolka.arcade.metadata.MetadataContainer;
import pl.themolka.arcade.module.Module;
import pl.themolka.arcade.session.ArcadePlayer;

import java.util.Set;
import java.util.UUID;

public class GamePlayer implements Metadata {
    private String displayName;
    private final transient Game game;
    private final MetadataContainer metadata = new MetadataContainer();
    private boolean participating;
    private ArcadePlayer player;
    private final String username;
    private final UUID uuid;

    public GamePlayer(Game game, ArcadePlayer player) {
        this(game, player.getUsername(), player.getUuid());

        this.setPlayer(player);
    }

    public GamePlayer(Game game, String username, UUID uuid) {
        this.game = game;
        this.username = username;
        this.uuid = uuid;
    }

    @Override
    public Object getMetadata(Class<? extends Module<?>> owner, String key, Object def) {
        return this.metadata.getMetadata(owner, key, def);
    }

    @Override
    public Set<String> getMetadataKeys() {
        return this.metadata.getMetadataKeys();
    }

    @Override
    public void setMetadata(Class<? extends Module<?>> owner, String key, Object metadata) {
        this.metadata.setMetadata(owner, key, metadata);
    }

    public Player getBukkit() {
        if (this.isOnline()) {
            return this.getPlayer().getBukkit();
        }

        return null;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Game getGame() {
        return this.game;
    }

    public ArcadePlayer getPlayer() {
        return this.player;
    }

    public String getUsername() {
        return this.username;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public boolean hasDisplayName() {
        return this.displayName != null || (this.isOnline() && this.getPlayer().getDisplayName() != null);
    }

    public boolean isOnline() {
        return this.player != null;
    }

    public boolean isParticipating() {
        return this.participating;
    }

    public void resetDisplayName() {
        this.setDisplayName(null);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;

        this.getPlayer().setDisplayName(displayName);
    }

    public void setParticipating(boolean participating) {
        this.participating = participating;
    }

    public void setPlayer(ArcadePlayer player) {
        this.player = player;
    }
}
