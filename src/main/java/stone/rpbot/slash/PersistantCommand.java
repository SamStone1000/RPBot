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

import stone.rpbot.slash.PersistanceManager.State;

/**
 * 
 */
public interface PersistantCommand extends Runnable {

	/**
	 * @return
	 */
	public long getMessageIdLong();

	public static String getName() {
		return null;
	};

	public static String getSubCommandName() {
		return null;
	};

	public static String getSubCommandGroupName() {
		return null;
	}

	/**
	 * @param buttonId
	 * @return
	 */
	public State onButtonInteraction(String buttonId);;
}
