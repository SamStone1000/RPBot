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

import java.util.Arrays;

import stone.rpbot.slash.conway.ConwayManager.Direction;

/**
 * 
 */
public class SimpleGameOfLife implements GameOfLife {

	private State[][] oldBoard;
	private State[][] newBoard;

	private int width;
	private int height;

	/**
	 * @param i
	 */
	public SimpleGameOfLife(int size) {
		this.width = size;
		this.height = size;
		this.oldBoard = new State[size][size];
		for (int i = 0; i < size; i++)
		{
			Arrays.fill(oldBoard[i], State.DEAD);
		}
		this.newBoard = new State[size][size];
	}

	@Override
	public void updateState() {
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				Coordinate coord = new Coordinate(x, y);
				newBoard[coord.getY()][coord.getX()] = updateCell(coord);
			}
		}
		State[][] temp = oldBoard;
		oldBoard = newBoard;
		newBoard = temp;
	}

	/**
	 * @param cell
	 */
	private State updateCell(Coordinate cell) {
		int liveCount = 0;
		for (int y = -1; y <= 1; y++)
			for (int x = -1; x <= 1; x++)
			{
				Coordinate scan = cell.clone();
				scan.move(Direction.UP, y);
				scan.move(Direction.LEFT, x);
				if (getState(scan) == State.ALIVE)
					if (!cell.equals(scan))
						liveCount++;
			}
		if (getState(cell) == State.ALIVE)
		return State.fromBoolean(liveCount >= 2 && liveCount <= 3);
		else
			return State.fromBoolean(liveCount == 3);
	}

	@Override
	public State getState(Coordinate coord) {
		if (coord.getX() >= width)
			return State.DEAD;
		if (coord.getY() >= height)
			return State.DEAD;
		if (coord.getX() < 0)
			return State.DEAD;
		if (coord.getY() < 0)
			return State.DEAD;
		return oldBoard[coord.getY()][coord.getX()];
	}

	@Override
	public void setState(Coordinate coord, State state) {
		oldBoard[coord.getY()][coord.getX()] = state;
	}

	@Override
	public char[] draw() {
		char[] board = new char[width * height + height];
		int i = 0;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				Coordinate current = new Coordinate(x, y);
				char cell;
				if (oldBoard[current.getY()][current.getX()] == State.ALIVE)
					cell = '■';
				else
					cell = '□';
				board[i] = cell;
				i++;
			}
			board[i] = '\n';
			i++;
		}
		return board;
	}

	private int to1D(Coordinate coord) {
		return coord.getY() * height + coord.getX();
	}

}
