/*
 * Copyright 2018 Aleksander Jagiełło
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.themolka.arcade.channel;

import net.engio.mbassy.listener.Handler;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.themolka.arcade.event.Priority;
import pl.themolka.arcade.game.GamePlayer;
import pl.themolka.arcade.session.ArcadePlayer;
import pl.themolka.arcade.session.ArcadeSound;

public class ChannelListeners implements Listener {
    public static final String CHANNEL_MENTION_KEY = "@";
    public static final int CHANNEL_MENTION_LIMIT = 1;
    public static final String CHANNEL_SPY_PERMISSION = "arcade.channel.spy";

    private final ChannelsGame game;

    public ChannelListeners(ChannelsGame game) {
        this.game = game;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ArcadePlayer player = this.game.getPlugin().getPlayer(event.getPlayer());
        ChatChannel channel = this.game.getChannelFor(player);

        if (!player.getChatState().chat()) {
            player.sendError("You may not chat because your chat is disabled.");
            event.setCancelled(true);
            return;
        }

        String message = event.getMessage();
        if (message.startsWith(GlobalChatChannel.GLOBAL_CHANNEL_KEY)) { // global
            channel = this.game.getGlobalChannel();
            message = message.substring(1);
        }

        channel.sendChatMessage(player, message.trim());
        event.setCancelled(true); // this breaks other plugins :/
    }

    @Handler(priority = Priority.LOW)
    public void onChannelMention(ChannelMessageEvent event) {
        if (event.isCanceled()) {
            return;
        }

        int mentioned = 0;
        for (String split : event.getMessage().split(" ")) {
            if (mentioned >= CHANNEL_MENTION_LIMIT ||
                    !split.startsWith(CHANNEL_MENTION_KEY)) {
                break;
            }

            String username = split.substring(1);
            if (username.isEmpty() ||
                    username.length() < ArcadePlayer.USERNAME_MIN_LENGTH ||
                    username.length() > ArcadePlayer.USERNAME_MAX_LENGTH) {
                continue;
            }

            GamePlayer player = this.game.getGame().findPlayer(username);
            if (player == null) {
                continue;
            } else if (!event.getChannel().hasMember(player)) {
                event.getAuthor().sendError("Player " + player.getFullName() +
                        ChatColor.RED + " is not a member of this channel.");
                continue;
            }

            event.setMessage(event.getMessage()
                    .replace(split, player.getFullName()));

            // notify
            player.sendInfo("You have been mentioned by " +
                    event.getAuthorName());
            player.getPlayer().play(ArcadeSound.CHAT_MENTION);

            mentioned++;
        }
    }

    @Handler(priority = Priority.LAST)
    public void onChannelSpy(ChannelMessageEvent event) {
        if (event.isCanceled() ||
                event.getChannel() instanceof GlobalChatChannel) {
            return;
        }

        String message = ChatColor.DARK_AQUA + ChatColor.ITALIC.toString() +
                "[Spy]" + ChatColor.RESET + " " + ChatColor.GRAY +
                event.getAuthorName() + ChatColor.RESET +
                ChatColor.GRAY + ": " + event.getMessage();
        for (ArcadePlayer online : this.game.getPlugin().getPlayers()) {
            GamePlayer player = online.getGamePlayer();
            if (player == null || event.getChannel().hasMember(player)) {
                continue;
            }

            if (player.hasPermission(CHANNEL_SPY_PERMISSION) &&
                    !player.isParticipating()) {
                player.sendChat(message);
            }
        }
    }
}
