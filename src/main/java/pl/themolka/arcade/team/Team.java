package pl.themolka.arcade.team;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.game.Game;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.goal.Goal;
import pl.themolka.arcade.goal.GoalCreateEvent;
import pl.themolka.arcade.match.Match;
import pl.themolka.arcade.match.MatchState;
import pl.themolka.arcade.match.MatchWinner;
import pl.themolka.arcade.scoreboard.ScoreboardContext;
import pl.themolka.arcade.session.ArcadePlayer;
import pl.themolka.arcade.util.StringId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Team implements MatchWinner, StringId {
    public static final int NAME_MAX_LENGTH = 16;

    private static final Random random = new Random();

    private final ArcadePlugin plugin;

    private org.bukkit.scoreboard.Team bukkit;
    private final TeamChannel channel;
    private ChatColor color;
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
    private final List<TeamSpawn> spawns = new ArrayList<>();

    public Team(ArcadePlugin plugin, String id) {
        this.plugin = plugin;

        this.channel = new TeamChannel(plugin, this);
        this.channel.setFormat(TeamChannel.TEAM_FORMAT);
        this.id = id;
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
    public String getId() {
        return this.id;
    }

    @Override
    public boolean contains(Player bukkit) {
        return this.contains(this.plugin.getPlayer(bukkit));
    }

    @Override
    public boolean contains(ArcadePlayer player) {
        return this.contains(player.getGamePlayer());
    }

    @Override
    public boolean contains(GamePlayer player) {
        return this.hasPlayer(player);
    }

    @Override
    public List<Goal> getGoals() {
        return this.goals;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getTitle() {
        return this.getPrettyName();
    }

    @Override
    public boolean isWinning() {
        return this.areGoalsScored();
    }

    @Override
    public boolean removeGoal(Goal goal) {
        return this.goals.remove(goal);
    }

    @Override
    public void sendGoalMessage(String message) {
        for (GamePlayer player : this.getOnlineMembers()) {
            player.sendAction(message);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Team && ((Team) obj).getId().equals(this.getId());
    }

    public boolean addSpawn(TeamSpawn spawn) {
        return this.spawns.add(spawn);
    }

    public boolean areGoalsScored() {
        for (Goal goal : this.getGoals()) {
            if (!goal.isCompleted(this)) {
                return false;
            }
        }

        return true;
    }

    public org.bukkit.scoreboard.Team getBukkit() {
        return this.bukkit;
    }

    public TeamChannel getChannel() {
        return this.channel;
    }

    public ChatColor getColor() {
        return this.color;
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
        return this.maxPlayers;
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
        return this.getColor() + this.getName() + ChatColor.RESET;
    }

    public TeamSpawn getRandomSpawn() {
        if (this.spawns.isEmpty()) {
            return null;
        }

        return this.spawns.get(random.nextInt(this.spawns.size()));
    }

    public Location getRandomSpawnLocation() {
        TeamSpawn spawn = this.getRandomSpawn();
        if (spawn != null) {
            return spawn.getSpawnLocation();
        }

        return null;
    }

    public List<TeamSpawn> getSpawns() {
        return this.spawns;
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
        return this.getOnlineMembers().size() >= this.getMaxPlayers();
    }

    public boolean isObservers() {
        return false;
    }

    public boolean isObserving() {
        return !this.isPlaying();
    }

    public boolean isOverfill() {
        return this.getOnlineMembers().size() >= this.getSlots();
    }

    public boolean isParticipating() {
        return !this.isObservers();
    }

    public boolean isPlaying() {
        return this.getMatch().getState().equals(MatchState.RUNNING);
    }

    public int getSlots() {
        return this.slots;
    }

    public void join(GamePlayer player) {
        this.join(player, true);
    }

    public void join(GamePlayer player, boolean message) {
        if (!player.isOnline() || this.isFull()) {
            return;
        }

        PlayerJoinTeamEvent event = new PlayerJoinTeamEvent(this.plugin, player, this);
        this.plugin.getEventBus().publish(event);

        if (!event.isCanceled()) {
            this.members.add(player);
            this.onlineMembers.add(player);

            player.getPlayer().resetFull();

            player.setMetadata(TeamsModule.class, TeamsModule.METADATA_TEAM, this);
            player.setParticipating(this.isParticipating());
            player.setCurrentChannel(this.getChannel());
            player.setDisplayName(this.getColor() + player.getUsername() + ChatColor.RESET);

            if (message) {
                player.getPlayer().sendSuccess("You joined the " + this.getPrettyName() + ChatColor.GREEN + ".");
            }

            this.plugin.getEventBus().publish(new PlayerJoinedTeamEvent(this.plugin, player, this));
        }
    }

    public void leave(GamePlayer player) {
        if (!player.isOnline()) {
            return;
        }

        PlayerLeaveTeamEvent event = new PlayerLeaveTeamEvent(this.plugin, player, this);
        this.plugin.getEventBus().publish(event);

        if (!event.isCanceled()) {
            this.members.remove(player);
            this.onlineMembers.remove(player);

            player.removeMetadata(TeamsModule.class, TeamsModule.METADATA_TEAM);
            player.setParticipating(false);
            player.setCurrentChannel(null);
            player.resetDisplayName();

            this.plugin.getEventBus().publish(new PlayerLeftTeamEvent(this.plugin, player, this));
        }
    }

    public void leaveServer(GamePlayer player) {
        this.onlineMembers.remove(player);
    }

    public boolean removeSpawn(TeamSpawn spawn) {
        return this.spawns.remove(spawn);
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

    public void setColor(ChatColor color) {
        this.color = color;
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

    public void setSpawns(List<TeamSpawn> spawns) {
        this.spawns.addAll(spawns);
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
        bukkit.setPrefix(team.getColor().toString());
        bukkit.setDisplayName(team.getName());
        bukkit.setSuffix(ChatColor.RESET.toString());

        return bukkit;
    }
}
