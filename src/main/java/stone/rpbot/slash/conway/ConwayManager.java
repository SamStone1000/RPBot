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

import java.nio.CharBuffer;
import java.util.Scanner;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import stone.rpbot.slash.PersistantCommand;
import stone.rpbot.slash.conway.GameOfLife.Coordinate;

/**
 * 
 */
public class ConwayManager implements PersistantCommand {

	public static final String NAME = "conway";
	public static final String SIZE_OPTION = "size";

	/**
		 * 
		 */
	public enum Direction {
		UP, DOWN, LEFT, RIGHT;
	}

	private SimpleGameOfLife game;
	private Coordinate cursor = new Coordinate(0, 0);
	private Message message;
	private boolean running = true;

	public ConwayManager(SlashCommandInteractionEvent event) {
		int size = event.getOption(SIZE_OPTION).getAsInt();
		this.game = new SimpleGameOfLife(size);
		event.getChannel().sendMessage(game.draw());
	}

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
		message.editMessage(CharBuffer.wrap(game.draw())).queue((message) ->
		{
			this.message = message;
		});
	}

	private void drawWithCursor() {
		char[] board = game.draw();
		int position = game.to1D(cursor);
		int height = game.getHeight();
		int newLines = position / height;
		position += newLines;
		board[position] = '*';
		message.editMessage(CharBuffer.wrap(board)).queue((message) ->
		{
			this.message = message;
		});
	}

	public static void init(CommandListUpdateAction commands) {
		commands.addCommands(Commands.slash("conway", "Starts a game of life"));
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Size?");
		ConwayManager manager = null;
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

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(SlashCommandInteractionEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getMessageIdLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean onButtonInteraction(ButtonInteractionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
