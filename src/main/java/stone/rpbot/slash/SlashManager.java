/**
 * This file is part of RPBot. 
 * Copyright (c) 2022, Stone, All rights reserved.
 * 
 * RPBot is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * RPBot is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with RPBot. If not, see <https://www.gnu.org/licenses/>.
 */
package stone.rpbot.slash;

import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import stone.rpbot.slash.commands.CommandMan;
import stone.rpbot.slash.commands.CommandTime;

/**
 * 
 */
public class SlashManager extends ListenerAdapter {

    private Map<String, SlashCommand> commands = new HashMap<>();

    public void init(CommandListUpdateAction commands) {
        registerSlashCommand(commands, new CommandMan(this));
        registerSlashCommand(commands, new CommandTime());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        commands.get(event.getName()).onSlashCommand(event);
    }

    public void registerSlashCommand(CommandListUpdateAction action, SlashCommand command) {
        this.commands.put(command.getName(), command);
        action.addCommands(command.getCommandData());
    }

    public SlashCommand getSlashCommand(String key) {
        return commands.get(key);
    }
}
