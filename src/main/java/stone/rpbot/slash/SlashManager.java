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

import stone.rpbot.slash.say.CommandSay;

/**
 * 
 */
public class SlashManager extends ListenerAdapter {

	private Map<String, SlashCommand> commands = new HashMap<>();

	public void init(CommandListUpdateAction commands) {
		registerSlashCommand("man", new ManCommand(this));
                registerSlashCommand("say", new CommandSay());
                registerSlashCommand("song", new CommandSong());
		commands.addCommands(
				Commands.slash("time", "Produces a Discord timestamp from input").addOption(OptionType.STRING, "input",
						"Input can take the form of absolute inputs or relative inputs prefixed with a +/-", true));
		commands.addCommands(Commands.slash("man", "Gets manual for specified command").addOption(OptionType.STRING,
				ManCommand.OPTION_COMMAND, "The name of the command to get the manual for", true));
                commands.addCommands(Commands.slash("say", "funny astronaut voice").addOption(OptionType.STRING, "text", "Text to say", true));
                commands.addCommands(Commands.slash("song", "queue"));
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		commands.get(event.getName()).onSlashCommand(event);
	}

	public void registerSlashCommand(String key, SlashCommand value) {
		commands.put(key, value);
	}

	public SlashCommand getSlashCommand(String key) {
		return commands.get(key);
	}
}
