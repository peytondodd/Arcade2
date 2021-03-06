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

package pl.themolka.arcade.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import pl.themolka.arcade.ArcadePlugin;
import pl.themolka.arcade.session.ArcadePlayer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BukkitCommands extends Commands implements CommandExecutor, TabCompleter {
    public static final String BUKKIT_COMMAND_PREFIX = "/";

    private final ArcadePlugin plugin;

    private CommandMap bukkitCommandMap;
    private Set<HelpTopic> helpTopics = new TreeSet<>(HelpTopicComparator.helpTopicComparatorInstance());
    private final String name;

    public BukkitCommands(ArcadePlugin plugin, String name) {
        super(plugin.getConsole());
        this.plugin = plugin;

        try {
            this.bukkitCommandMap = this.getBukkitCommandMap();
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }

        this.name = name;

        this.plugin.getServer().getHelpMap().addTopic(this.createHelpIndex());
    }

    @Override
    public void registerCommand(Command command) {
        super.registerCommand(command);

        this.injectCommand(this.getPrefixName(), command);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        this.handleCommand(this.findSender(sender), this.getCommand(label), label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        List<String> completer = this.handleCompleter(this.findSender(sender), this.getCommand(label), label, args);
        if (completer == null || completer.isEmpty()) {
            completer = new ArrayList<>();
            for (ArcadePlayer online : this.plugin.getPlayers()) {
                completer.add(online.getUsername());
            }
        }

        Collections.sort(completer);
        return completer;
    }

    public Set<HelpTopic> getHelpTopics() {
        return this.helpTopics;
    }

    private org.bukkit.command.Command createBukkitCommand(Command command) {
        List<String> aliases = new ArrayList<>();
        for (int i = 1; i < command.getName().length; i++) {
            aliases.add(command.getName()[i]);
        }

        org.bukkit.command.Command performer = new CommandPerformer(command.getCommand());
        performer.setAliases(aliases);
        performer.setDescription(command.getDescription());
        performer.setPermission(command.getPermission());
        performer.setUsage(command.getUsage());
        return performer;
    }

    private IndexHelpTopic createHelpIndex() {
        return new IndexHelpTopic(
                this.getPluginName(),
                "Wszystkie komendy " + this.getPluginName(),
                null,
                this.getHelpTopics()
        );
    }

    private Sender findSender(CommandSender sender) {
        if (sender instanceof Player) {
            Sender result = this.plugin.getPlayer(((Player) sender).getUniqueId());
            if (result != null) {
                return result;
            }
        }

        return this.getConsole();
    }

    private CommandMap getBukkitCommandMap() throws ReflectiveOperationException {
        Field field = this.plugin.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (CommandMap) field.get(this.plugin.getServer());
    }

    private String getPluginName() {
        return this.name;
    }

    private String getPrefixName() {
        return this.getPluginName().toLowerCase();
    }

    private void injectCommand(String prefix, Command command) {
        org.bukkit.command.Command performer = this.createBukkitCommand(command);

        this.bukkitCommandMap.register(prefix, performer);
        this.helpTopics.add(new GenericCommandHelpTopic(performer));
    }

    private class CommandPerformer extends org.bukkit.command.Command {
        protected CommandPerformer(String name) {
            super(name);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            return onCommand(sender, this, label, args);
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
            return onTabComplete(sender, this, label, args);
        }
    }
}
