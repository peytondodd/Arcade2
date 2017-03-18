package pl.themolka.arcade.team;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.channel.ChatChannel;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.game.PlayerApplicable;
import pl.themolka.arcade.goal.Goal;
import pl.themolka.arcade.goal.GoalCreateEvent;
import pl.themolka.arcade.match.Match;
import pl.themolka.arcade.match.MatchWinner;
import pl.themolka.arcade.scoreboard.ScoreboardContext;
import pl.themolka.arcade.session.ArcadePlayer;
import pl.themolka.arcade.util.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A group with defined players in it.
 */
public class Team implements MatchWinner {
    public static final int NAME_MAX_LENGTH = 16;

    private final ArcadePlugin plugin;

    private org.bukkit.scoreboard.Team bukkit;
    private final TeamChannel channel;
    private ChatColor chatColor;
    private DyeColor dyeColor;
    private boolean friendlyFire;
    private final List<Goal> goals = new ArrayList<>();
    private final String id;
    private Match match;
    private int maxPlayers;
    private final List<GamePlayer> members = new ArrayList<>();
    private int minPlayers;
    private String name;
    private final List<GamePlayer> onlineMembers = new ArrayList<>();
    private int slots;

    private final Map<TeamApplyEvent, List<PlayerApplicable>> applyMap = new HashMap<>();

    public Team(ArcadePlugin plugin, String id) {
        this.plugin = plugin;

        this.channel = new TeamChannel(plugin, this);
        this.channel.setFormat(TeamChannel.TEAM_FORMAT);
        this.id = id;
    }

    public Team(Team original) {
        this(original.plugin, original.getId());

        this.setBukkit(original.getBukkit());
        this.setChatColor(original.getChatColor());
        this.setDyeColor(original.getDyeColor());
        this.setFriendlyFire(original.isFriendlyFire());
        this.setMatch(original.getMatch());
        this.setMaxPlayers(original.getMaxPlayers());
        this.setMinPlayers(original.getMinPlayers());
        this.setName(original.getName());
        this.setSlots(original.getSlots());
    }

    @Override
    public boolean addGoal(Goal goal) {
        if (this.hasGoal(goal)) {
            return false;
        }

        this.plugin.getEventBus().publish(new GoalCreateEvent(this.plugin, goal));
        return this.goals.add(goal);
    }

    @Override
    public boolean contains(Player bukkit) {
        return this.contains(this.plugin.getPlayer(bukkit));
    }

    @Override
    public boolean contains(ArcadePlayer player) {
        return player.getGamePlayer() != null && this.contains(player.getGamePlayer());
    }

    @Override
    public boolean contains(GamePlayer player) {
        return this.hasPlayer(player);
    }

    @Override
    public Color getColor() {
        return Color.ofChat(this.getChatColor());
    }

    @Override
    public List<Goal> getGoals() {
        return new ArrayList<>(this.goals);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean hasGoal(Goal goal) {
        return this.goals.contains(goal);
    }

    @Override
    public boolean removeGoal(Goal goal) {
        return this.goals.remove(goal);
    }

    @Override
    public void sendGoalMessage(String message) {
        this.plugin.getLogger().info("[" + this.getName() + "] (Goal) " + ChatColor.stripColor(message));
        this.getChannel().send(ChatColor.YELLOW + message);
        this.getChannel().sendAction(ChatColor.YELLOW + message);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Team && ((Team) obj).getId().equals(this.getId());
    }

    public void addApplyContent(TeamApplyEvent event, PlayerApplicable apply) {
        List<PlayerApplicable> value = this.getApplyContent(event);
        value.add(apply);

        this.applyMap.put(event, value);
    }

    public void addApplyContentToAll(PlayerApplicable apply) {
        for (TeamApplyEvent event : TeamApplyEvent.values()) {
            this.addApplyContent(event, apply);
        }
    }

    public void apply(GamePlayer player, TeamApplyEvent event) {
        for (PlayerApplicable apply : this.getApplyContent(event)) {
            apply.apply(player);
        }
    }

    public void applyToAll(TeamApplyEvent event) {
        for (ArcadePlayer player : this.plugin.getPlayers()) {
            if (player.getGamePlayer() != null) {
                this.apply(player.getGamePlayer(), event);
            }
        }
    }

    public List<PlayerApplicable> getApplyContent(TeamApplyEvent event) {
        return this.applyMap.getOrDefault(event, new ArrayList<>());
    }

    public org.bukkit.scoreboard.Team getBukkit() {
        return this.bukkit;
    }

    public TeamChannel getChannel() {
        return this.channel;
    }

    public ChatColor getChatColor() {
        return this.chatColor;
    }

    public DyeColor getDyeColor() {
        return this.dyeColor;
    }

    public Game getGame() {
        return this.getMatch().getGame();
    }

    public Match getMatch() {
        return this.match;
    }

    public int getMaxPlayers() {
        if (this.maxPlayers >= this.slots) {
            return this.maxPlayers;
        }

        return this.slots;
    }

    public List<GamePlayer> getMembers() {
        return this.members;
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public List<GamePlayer> getOnlineMembers() {
        return this.onlineMembers;
    }

    public String getPrettyName() {
        return this.getTitle();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public boolean hasPlayer(GamePlayer player) {
        return this.getMembers().contains(player);
    }

    public boolean isFriendlyFire() {
        return this.friendlyFire;
    }

    public boolean isFull() {
        return this.getOnlineMembers().size() >= this.getSlots();
    }

    public boolean isObservers() {
        return false;
    }

    public boolean isObserving() {
        return !this.isPlaying();
    }

    public boolean isOverfilled() {
        return this.getOnlineMembers().size() >= this.getMaxPlayers();
    }

    public boolean isParticipating() {
        return !this.isObservers();
    }

    public boolean isPlaying() {
        return this.getMatch().isRunning();
    }

    public int getSlots() {
        if (this.slots <= this.maxPlayers) {
            return this.slots;
        }

        return this.maxPlayers;
    }

    public boolean join(GamePlayer player) {
        return this.join(player, true);
    }

    public boolean join(GamePlayer player, boolean message) {
        if (!player.isOnline() || this.isFull() || this.hasPlayer(player)) {
            return false;
        }

        PlayerJoinTeamEvent event = new PlayerJoinTeamEvent(this.plugin, player, this);
        this.plugin.getEventBus().publish(event);

        if (event.isCanceled()) {
            return false;
        }

        // handle
        this.members.add(player);
        this.onlineMembers.add(player);

        player.setMetadata(TeamsModule.class, TeamsModule.METADATA_TEAM, this);
        player.setChatColor(this.getChatColor());
        player.setCurrentChannel(this.getCurrentChannel());
        player.setDisplayName(this.getChatColor() + player.getUsername() + ChatColor.RESET);
        player.setParticipating(this.getMatch().isRunning() && this.isParticipating());

        // handle it AFTER the setParticipating(...) method
        if (this.getMatch().isRunning()) {
            player.reset();

            player.getPlayer().getPermissions().clearGroups();
            player.getPlayer().getPermissions().refresh();
        }

        this.plugin.getLogger().info(player.getUsername() + " joined team '" + this.getName() + "' (" + this.getId() + ")");
        if (message) {
            player.getPlayer().sendSuccess("You joined " + this.getPrettyName() + ChatColor.GREEN + ".");
        }

        this.plugin.getEventBus().publish(new PlayerJoinedTeamEvent(this.plugin, player, this));
        return true;
    }

    public boolean joinForce(GamePlayer player) {
        return this.join(player, true);
    }

    public boolean leave(GamePlayer player) {
        if (!player.isOnline() || !this.hasPlayer(player)) {
            return false;
        }

        PlayerLeaveTeamEvent event = new PlayerLeaveTeamEvent(this.plugin, player, this);
        this.plugin.getEventBus().publish(event);

        if (event.isCanceled()) {
            return false;
        }

        // handle
        this.members.remove(player);
        this.onlineMembers.remove(player);

        player.removeMetadata(TeamsModule.class, TeamsModule.METADATA_TEAM);
        player.setChatColor(null);
        player.setCurrentChannel(null);
        player.setParticipating(false);
        player.resetDisplayName();

        // handle it AFTER the setParticipating method
        if (this.getMatch().isRunning()) {
            player.reset();

            player.getPlayer().getPermissions().clearGroups();
            player.getPlayer().getPermissions().refresh();
        }

        this.plugin.getLogger().info(player.getUsername() + " left team '" + this.getName() + "' (" + this.getId() + ")");

        this.plugin.getEventBus().publish(new PlayerLeftTeamEvent(this.plugin, player, this));
        return true;
    }

    public boolean leaveForce(GamePlayer player) {
        return this.leave(player);
    }

    public void leaveServer(GamePlayer player) {
        this.onlineMembers.remove(player);
    }

    public void send(String message) {
        for (GamePlayer player : this.getOnlineMembers()) {
            player.getPlayer().send(message);
        }
    }

    public void setBukkit(org.bukkit.scoreboard.Team bukkit) {
        this.bukkit = bukkit;

        this.updateBukkitTeam();
    }

    public void setChatColor(ChatColor chatColor) {
        this.chatColor = chatColor;
    }

    public void setDyeColor(DyeColor dyeColor) {
        this.dyeColor = dyeColor;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public void setName(String name) {
        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Name too long (" + name.length() + " > " + NAME_MAX_LENGTH + ")");
        }

        this.name = name;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    private ChatChannel getCurrentChannel() {
        if (this.getMatch().isRunning()) {
            return this.getChannel();
        } else {
            return null;
        }
    }

    private void updateBukkitTeam() {
        if (this.getBukkit() != null) {
            this.getBukkit().setAllowFriendlyFire(this.isFriendlyFire());
            this.getBukkit().setOption(
                    org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
                    org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }
    }

    public static org.bukkit.scoreboard.Team createBukkitTeam(Scoreboard board, Team team) {
        String id = team.getId();
        if (id.length() > ScoreboardContext.TEAM_MAX_LENGTH) {
            id = id.substring(0, ScoreboardContext.TEAM_MAX_LENGTH);
        }

        org.bukkit.scoreboard.Team bukkit = board.registerNewTeam(id);
        bukkit.setPrefix(team.getChatColor().toString());
        bukkit.setDisplayName(team.getName());
        bukkit.setSuffix(ChatColor.RESET.toString());

        return bukkit;
    }
}
