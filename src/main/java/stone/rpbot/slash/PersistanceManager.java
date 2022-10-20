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
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import stone.rpbot.slash.conway.ConwayManager;

/**
 * 
 */
public class PersistanceManager extends ListenerAdapter {
	
	ThreadPoolExecutor executor;
	Map<String, PersistantCommandFactory> commands = new HashMap<>();

	public PersistanceManager() {

	}

	public void init() {
		commands.put(ConwayManager.NAME, ConwayManager::new);
	}

	public void register(String name, String desc, PersistantCommandFactory fact, CommandListUpdateAction update) {
		commands.put(name, fact);

	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

	}
}
