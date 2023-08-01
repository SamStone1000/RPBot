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
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import stone.rpbot.slash.PersistanceManager.State;
import stone.rpbot.slash.PersistantCommand;
import stone.rpbot.slash.conway.GameOfLife.Coordinate;

/**
 * 
 */
public class ConwayManager implements PersistantCommand {

	public static final String NAME = "conway";
	public static final String SIZE_OPTION = "size";

	public static final String UP = "up";
	public static final String DOWN = "down";
	public static final String LEFT = "left";
	public static final String RIGHT = "right";
	public static final String START = "start";
	public static final String TOGGLE = "toggle";
	public static final String STOP = "stop";

	/**
		 * 
		 */
	public enum Direction {
		UP, DOWN, LEFT, RIGHT;

		public static Direction fromString(String str) {
			switch (str) {
			case "up":
				return UP;
			case "down":
				return DOWN;
			case "left":
				return LEFT;
			case "right":
				return RIGHT;
			default:
				return null;
			}
		}
	}

	private SimpleGameOfLife game;
	private Coordinate cursor = new Coordinate(0, 0);
	private Message message;
	private boolean running = true;

	public ConwayManager(SlashCommandInteractionEvent event) {
		int size = event.getOption(SIZE_OPTION).getAsInt();
		this.game = new SimpleGameOfLife(size);
		MessageCreateBuilder initialMessage = new MessageCreateBuilder();
		initialMessage.setContent(new String(game.draw()));
		ActionRow row = ActionRow.of(// IMPROVE THIS
				Button.of(ButtonStyle.SECONDARY, START, "Start"),
				Button.of(ButtonStyle.PRIMARY, UP, Emoji.fromUnicode("⬆️")),
				Button.of(ButtonStyle.SECONDARY, TOGGLE, "Toggle"));

		ActionRow row2 = ActionRow.of(Button.of(ButtonStyle.PRIMARY, LEFT, Emoji.fromUnicode("⬅️")),
				Button.of(ButtonStyle.PRIMARY, DOWN, Emoji.fromUnicode("⬇️")),
				Button.of(ButtonStyle.PRIMARY, RIGHT, Emoji.fromUnicode("➡️")),
				Button.of(ButtonStyle.DANGER, STOP, "Stop Game"));
		initialMessage.setComponents(row, row2);
		this.message = event.getChannel().sendMessage(initialMessage.build()).complete();
		drawWithCursor();
	}

	public void moveCursor(Direction dir) {
		cursor.nudge(dir);
	}

	public void toggleCursor() {
		game.setState(cursor, game.getState(cursor).inverse());
	}

	public void start() {
		this.message = message.editMessageComponents(ActionRow.of(Button.of(ButtonStyle.DANGER, STOP, "Stop Game")))
				.complete();
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
		char[] board = game.draw();
		char[] monoBoard = new char[board.length + 6];
		System.arraycopy(board, 0, monoBoard, 3, board.length);
		for (int i = 0; i < 3; i++)
		{
			monoBoard[i] = '`';
			monoBoard[board.length + i + 3] = '`';
		}
		message.editMessage(CharBuffer.wrap(monoBoard)).queue((message) ->
		{
			this.message = message;
		});
	}

	private void drawWithCursor() {
		char[] board = game.draw();
		char[] monoBoard = new char[board.length + 6];
		int position = game.to1D(cursor);
		int height = game.getHeight();
		int newLines = position / height;
		position += newLines;
		board[position] = '+';

		System.arraycopy(board, 0, monoBoard, 3, board.length);
		for (int i = 0; i < 3; i++)
		{
			monoBoard[i] = '`';
			monoBoard[board.length + i + 3] = '`';
		}
		message.editMessage(CharBuffer.wrap(monoBoard)).queue((message) ->
		{
			this.message = message;
		});
	}

	public static void init(CommandListUpdateAction commands) {
		commands.addCommands(Commands.slash("conway", "Starts a game of life").addOption(OptionType.INTEGER,
				SIZE_OPTION, "The length of the sides of the square that makes up the game board", true));
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
		start();
		message.editMessage("Message is dead. Rice soup is not big.").queue();
	}

	@Override
	public long getMessageIdLong() {
		return message.getIdLong();
	}

	@Override
	public State onButtonInteraction(String buttonId) {
		Direction dir = Direction.fromString(buttonId);
		if (dir != null)
		{
			moveCursor(dir);
			drawWithCursor();
			return State.BUILDING;
		}
		else
		{
			switch (buttonId) {
			case TOGGLE:
				toggleCursor();
				return State.BUILDING;
			case START:
				return State.RUNNING;
			case STOP:
				message.editMessage("Message is dead. Rice soup is not big.")
						.queue();
				running = false;
				return State.DEAD;
			}
		}
		return State.DEAD;
	}
}
