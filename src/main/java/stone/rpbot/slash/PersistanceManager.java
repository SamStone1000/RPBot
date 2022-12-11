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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import stone.rpbot.slash.conway.ConwayManager;

/**
 * 
 */
public class PersistanceManager extends ListenerAdapter {
	
	ExecutorService executor;
	Map<String, PersistantCommandFactory> commands = new HashMap<>();
	Map<Long, PersistantCommand> runningCommands = new HashMap<>();

	public PersistanceManager() {
		this.executor = Executors.newCachedThreadPool();
	}

	public void init() {
		register(ConwayManager.NAME, ConwayManager::new);
	}

	public void register(String name, PersistantCommandFactory fact) {
		commands.put(name, fact);
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (commands.containsKey(event.getName()))
				{
			event.reply("Loading Persistant Message ...").setEphemeral(true)
					.queue();
			PersistantCommand command = commands.get(event.getName()).build(event);
			runningCommands.put(command.getMessageIdLong(), command);
				}

	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		event.deferEdit().queue();
		Long messageId = event.getMessageIdLong();
		PersistantCommand command = runningCommands.get(messageId);
		State commandState = command.onButtonInteraction(event.getComponentId());
		switch (commandState) {
		case RUNNING:
			executor.execute(command);
			break;
		case DEAD:
			runningCommands.remove(messageId);
			break;
		}
	}

	public class Builder {

	}

	public enum State {
		BUILDING, RUNNING, DEAD;
	}
}
