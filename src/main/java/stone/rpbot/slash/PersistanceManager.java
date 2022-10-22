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
import java.util.concurrent.ThreadPoolExecutor;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import stone.rpbot.slash.conway.ConwayManager;

/**
 * 
 */
public class PersistanceManager extends ListenerAdapter {
	
	ThreadPoolExecutor executor;
	Map<String, PersistantCommandFactory> commands = new HashMap<>();
	Map<Long, PersistantCommand> runningCommands = new HashMap<>();

	public PersistanceManager() {

	}

	public void init() {
		register(ConwayManager.NAME, ConwayManager::new);
	}

	public void register(String name, PersistantCommandFactory fact) {
		commands.put(name, fact);
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (executor.getActiveCount() < executor.getMaximumPoolSize())
		{
			event.reply("Loading Persistant Message ...").setEphemeral(true)
					.queue();
			PersistantCommand command = commands.get(event.getName()).build(event);
			runningCommands.put(command.getMessageIdLong(), command);
			executor.execute(command);
		}
		else
		{
			event.reply(
					"The PersistanceManager thread pool is full! (too many persistant messages are running currently, wait for one of the messages to finish)")
					.setEphemeral(true).queue();
		}

	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		Long messageId = event.getMessageIdLong();
		if (runningCommands.get(messageId).onButtonInteraction(event))
		{
			runningCommands.remove(messageId);
		}
	}
}
