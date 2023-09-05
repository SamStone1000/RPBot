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

/**
 * 
 */
public class SlashManager extends ListenerAdapter {

	private Map<String, SlashCommand> commands = new HashMap<>();

	public void init(CommandListUpdateAction commands) {
		commands.addCommands(Commands.slash("time", "Produces a Discord timestamp from input").addOption(
				OptionType.STRING, "input", "Input can take the form of relative inputs prefixed with a +/-", true));
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		commands.get(event.getName()).onSlashCommand(event);
	}

	public void registerSlashCommand(String key, SlashCommand value) {
		commands.put(key, value);
	}
}
