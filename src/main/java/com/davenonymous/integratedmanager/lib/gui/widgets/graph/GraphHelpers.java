package com.davenonymous.integratedmanager.lib.gui.widgets.graph;

import org.joml.Vector2f;

import javax.annotation.Nonnull;

public class GraphHelpers {

	public static class SpiralIterator {
		private int x = 0;
		private int y = 0;
		private int dx = 0;
		private int dy = -1;

		private Vector2f elementSize;
		private Vector2f offset;

		public SpiralIterator() {
			this(1, new Vector2f(0, 0));
		}

		public SpiralIterator(Vector2f offset) {
			this(1, offset);
		}

		public SpiralIterator(int elementSize) {
			this(elementSize, new Vector2f(0, 0));
		}

		public SpiralIterator(int elementSize, Vector2f offset) {
			this.elementSize = new Vector2f(elementSize, elementSize);
			this.offset = offset;
		}

		public @Nonnull Vector2f next() {
			Vector2f position = new Vector2f(x, y);
			if (x == y || (x < 0 && x == -y) || (x > 0 && x == 1 - y)) {
				int temp = dx;
				dx = -dy;
				dy = temp;
			}
			x += dx;
			y += dy;

			return position.mul(elementSize).add(offset);
		}
	}
}
