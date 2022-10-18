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

import stone.rpbot.slash.conway.ConwayManager.Direction;

/**
 * 
 */
public interface GameOfLife {

	public void updateState();

	public State getState(Coordinate coord);

	public void setState(Coordinate coord, State state);

	public static class Coordinate implements Cloneable {
		private int x;
		private int y;

		public Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void setX(int x) {
			this.x = x;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getX() {
			return x;
		};

		public int getY() {
			return y;
		}

		public void move(Direction dir, int distance) {
			switch (dir) {
			case DOWN:
				y += distance;
				break;
			case LEFT:
				x -= distance;
				break;
			case RIGHT:
				x += distance;
				break;
			case UP:
				y -= distance;
				break;
			default:
				break;

			}
		}

		public void nudge(Direction dir) {
			move(dir, 1);
		}

		@Override
		public Coordinate clone() {
			return new Coordinate(this.x, this.y);
		}

		@Override
		public String toString() {
			return String.format("{%d, %d)", x, y);
		}

		@Override
		public boolean equals(Object obj) {
			Coordinate other = (Coordinate) obj;
			return this.x == other.x && this.y == other.y;
		}
	}

	public enum State {
		ALIVE(true), DEAD(false);

		private boolean state;

		/**
		 * @param b
		 */
		State(boolean b) {
			this.state = b;
		}

		/**
		 * @param b
		 * @return
		 */
		static State fromBoolean(boolean b) {
			if (b)
				return ALIVE;
			else
				return DEAD;
		}

		State inverse() {
			if (state)
				return DEAD;
			else
				return ALIVE;
		}
	}

	/**
	 * @return
	 */
	public char[] draw();
}
