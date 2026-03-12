package fr.gab400.mosaik;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Grid {
	
	private final Cell[][] grid;
	@Getter
	private final List<Cell> cells = new ArrayList<>();
	@Getter
	private static final float cellSpacing = Globals.CELL_SPACING;
	@Getter
	@Setter
	private float cellSize;
	
	public Grid(int sizeX, int sizeY, float cellSize) {
		this.grid = new Cell[sizeX][sizeY];
		for (int x=0;x<sizeX;x++) {
			for (int y=0;y<sizeY;y++) {
				Cell cell = new Cell(x, y);
				grid[x][y] = cell;
				this.cells.add(cell);
			}
		}
		this.cellSize = cellSize;
	}
	
	public float getWorldWidth() {
		return grid.length * (cellSize + cellSpacing);
	}
	
	public float getWorldHeight() {
		return grid[0].length * (cellSize + cellSpacing);
	}

	public @Nullable Cell getCellAt(int x, int y) {
		if (x >= grid.length || y >= grid[0].length || x < 0 || y < 0) {
			return null;
		}
		return grid[x][y];
	}
	
	@Getter
	@Setter
	public static class Cell {
		private final int x;
		private final int y;
		private boolean enabled;
		private final Border[] division;
		
		public Cell(int x, int y) {
			this.x = x;
			this.y = y;
			this.enabled = false;
			
			this.division = new Border[4];
			for (byte i=0;i<4;i++) division[i] = new Border(this, i);
		}

		public boolean isValid() {
			return division[0].getColor() != division[1].getColor()
					&& division[0].getColor() != division[2].getColor()
					&& division[0].getColor() != division[3].getColor()
					&& division[1].getColor() != division[2].getColor()
					&& division[1].getColor() != division[3].getColor()
					&& division[2].getColor() != division[3].getColor();
		}
		
		@Getter
		public static class Border {
			private final Cell cell;
			private final byte id;
			@Setter
			private boolean activated;
			@Setter
			private Color color;
			
			public Border(Cell cell, byte id) {
				this.cell = cell;
				this.id = id;
				this.activated = false;
				this.color = Globals.GRID_COLOR;
			}
		}
	}
}
