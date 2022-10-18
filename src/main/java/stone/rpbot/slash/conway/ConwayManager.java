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
package stone.rpbot.slash.conway;

import java.util.Scanner;

import net.dv8tion.jda.api.entities.Message;
import stone.rpbot.slash.conway.GameOfLife.Coordinate;

/**
 * 
 */
public class ConwayManager {

	/**
		 * 
		 */
	public enum Direction {
		UP, DOWN, LEFT, RIGHT;
	}

	private GameOfLife game;
	private Coordinate cursor = new Coordinate(0, 0);
	private Message message;
	private boolean running = true;

	public void moveCursor(Direction dir) {
		cursor.nudge(dir);
	}

	public void toggleCursor() {
		game.setState(cursor, game.getState(cursor).inverse());
	}

	public void start() {
		while (running)
		{
			long currentTime = System.currentTimeMillis();
			long nextTick = currentTime + 1000l;
			game.updateState();
			redraw();
			long millisToSleep = nextTick - System.currentTimeMillis();
			try
			{
				Thread.sleep(millisToSleep);
			} catch (InterruptedException | IllegalArgumentException e) // just continue on to the next tick then
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	private void redraw() {
		System.out.println(game.draw());
	}

	public ConwayManager(int size, Message message) {
		this.game = new SimpleGameOfLife(size);
		this.message = message;
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Size?");
		ConwayManager manager = new ConwayManager(scanner.nextInt());
		boolean prepping = true;
		do
		{
			manager.redraw();
			System.out.println(manager.cursor);
			System.out.println("Manipulate board");
			switch (scanner.next()) {
			case "w":
				manager.moveCursor(Direction.UP);
				break;
			case "a":
				manager.moveCursor(Direction.LEFT);
				break;
			case "s":
				manager.moveCursor(Direction.DOWN);
				break;
			case "d":
				manager.moveCursor(Direction.RIGHT);
				break;
			case "t":
				manager.toggleCursor();
				break;
			case "done":
				prepping = false;
				break;
			}

		} while (prepping);
		manager.start();
	}
}
