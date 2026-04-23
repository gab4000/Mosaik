package fr.gab400.mosaik;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
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

	public boolean areNeighbors(Cell.Border border1, Cell.Border border2) {
		Cell.Border border1Neighbor = getNeighbor(border1);
		Cell.Border border2Neighbor = getNeighbor(border2);
		if (border1Neighbor == null || border2Neighbor == null) {
			System.err.println("One of neighbors is null!");
			return false;
		}

		return border1Neighbor == border2 && border2Neighbor == border1;
	}

	public @Nullable Grid.Cell.Border getNeighbor(Cell.Border border) {
		Cell cell = border.getCell();
		byte neighborId = getNeighborId(border);
		Cell neighborCell = null;
		switch (neighborId) {
			case 0 -> neighborCell = getCellAt(cell.getX(), cell.getY() - 1);
			case 1 -> neighborCell = getCellAt(cell.getX() + 1, cell.getY());
			case 2 -> neighborCell = getCellAt(cell.getX(), cell.getY() + 1);
			case 3 -> neighborCell = getCellAt(cell.getX() - 1, cell.getY());
		}
		if (neighborCell == null) {
			return null;
		}
		return neighborCell.getDivision()[neighborId];
	}

	public byte getNeighborId(Cell.Border border) {
		return switch (border.getId()) {
			case 0 -> 2;
			case 1 -> 3;
			case 2 -> 0;
			case 3 -> 1;
			default -> throw new IllegalStateException("Unexpected value: " + border.getId());
		};
	}

	public Cell.Border[] getLineOrColumn(Cell.Border border) {
		List<Cell> cells = new ArrayList<>();
		switch (border.getId()) {
			case 0, 2 -> {
				for (int i=0;i< grid.length;i++) {
					cells.add(getCellAt(i, border.getCell().getY()));
				}
			}
			case 1, 3 -> {
				for (int i=0;i< grid[0].length;i++) {
					cells.add(getCellAt(border.getCell().getX(), i));
				}
			}
            default -> throw new IllegalStateException("Unexpected value: " + border.getId());
        }
		
		Cell.Border[] borders = new Cell.Border[cells.size()];
		for (Cell cell : cells) {
			borders[cells.indexOf(cell)] = cell.getDivision()[border.getId()];
		}
		return borders;
	}

	public void enableCell(Vector2f mouseWorldPos, Color color) {
		int cellX = (int) (mouseWorldPos.x / (getCellSize() + Grid.getCellSpacing()));
		int cellY = (int) (mouseWorldPos.y / (getCellSize() + Grid.getCellSpacing()));
		Grid.Cell cell = getCellAt(cellX, cellY);
		if (cell == null) return;

		float x = mouseWorldPos.x / (getCellSize() + Grid.getCellSpacing()) - cellX - 0.5f;
		float y = mouseWorldPos.y / (getCellSize() + Grid.getCellSpacing()) - cellY - 0.5f;

		Grid.Cell.Border border = Utils.getBorderAt(cell, x, y);
		if (border == null) {
			System.err.println("Border is null!");
			return;
		}

		if (cell.isNotValid(border.withColor(color).withActivated(true))) {
			System.err.println("Cell not valid!");
			return;
		}
		Grid.Cell.Border neighbor = getNeighbor(border);
		if (neighbor != null) {
			if (neighbor.getCell().isNotValid(neighbor.withColor(color).withActivated(true))) {
				System.err.println("Neighbor cell not valid!");
				return;
			}
			neighbor.applyColor().applyActivated();
		} else {
			Grid.Cell.Border[] line = getLineOrColumn(border);
			for (Grid.Cell.Border border1 : line) {
				if (border1.getCell().isNotValid(border1.withColor(color).withActivated(true))) {
					System.err.println("Line " + border1.getCell().getX() + " " + border1.getCell().getY() + " cell not valid!");
					return;
				}
			}
			for (Grid.Cell.Border border1 : line) {
				border1.applyColor().applyActivated();
			}
			return;
		}
		border.applyColor().applyActivated();
	}

	public void disableCell(Vector2f mouseWorldPos) {
		int cellX = (int) (mouseWorldPos.x / (getCellSize() + Grid.getCellSpacing()));
		int cellY = (int) (mouseWorldPos.y / (getCellSize() + Grid.getCellSpacing()));
		Grid.Cell cell = getCellAt(cellX, cellY);
		if (cell == null) return;

		float x = mouseWorldPos.x / (getCellSize() + Grid.getCellSpacing()) - cellX - 0.5f;
		float y = mouseWorldPos.y / (getCellSize() + Grid.getCellSpacing()) - cellY - 0.5f;

		Grid.Cell.Border border = Utils.getBorderAt(cell, x, y);
		if (border == null) {
			System.err.println("Border is null!");
			return;
		}

		if (cell.isNotValid(border.withColor(Globals.GRID_COLOR).withActivated(false))) {
			System.err.println("Cell not valid!");
			return;
		}
		Grid.Cell.Border neighbor = getNeighbor(border);
		if (neighbor != null) {
			if (neighbor.getCell().isNotValid(neighbor.withColor(Globals.GRID_COLOR).withActivated(false))) {
				System.err.println("Neighbor cell not valid!");
				return;
			}
			neighbor.applyColor().applyActivated();
		} else {
			Grid.Cell.Border[] line = getLineOrColumn(border);
			for (Grid.Cell.Border border1 : line) {
				if (border1.getCell().isNotValid(border1.withColor(Globals.GRID_COLOR).withActivated(false))) {
					System.err.println("Line " + border1.getCell().getX() + " " + border1.getCell().getY() + " cell not valid!");
					return;
				}
			}
			for (Grid.Cell.Border border1 : line) {
				border1.applyColor().applyActivated();
			}
			return;
		}
		border.applyColor().applyActivated();
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
			for (byte i=0;i<division.length;i++) division[i] = new Border(this, i);
		}

		public boolean isNotValid(Border changed) {
    		if (changed.isOActivated()) {
        		return Arrays.stream(getDivision())
                	.filter(border -> border.getId() != changed.getId())
                	.anyMatch(border -> border.getColor().equals(changed.getOColor())); // Utilise .equals()
    		}
    	return false;
		}

		@Getter
		public static class Border {
			private final Cell cell;
			private final byte id;
			private boolean activated;
			private Color color;
			
			private Color oColor;
			private boolean oActivated;
			
			public Border(Cell cell, byte id) {
				this.cell = cell;
				this.id = id;
				this.activated = false;
				this.color = Globals.GRID_COLOR;
			}
			
			public Border withColor(Color color) {
				this.oColor = color;
				return this;
			}
			
			public Border color(Color color) {
				this.color = color;
				return this;
			}
			
			public Border applyColor() {
				this.color = oColor;
				return this;
			}
			
			public Border withActivated(boolean activated) {
				this.oActivated = activated;
				return this;
			}
			
			public Border activated(boolean activated) {
				this.activated = activated;
				return this;
			}
			
			public Border applyActivated() {
				this.activated = oActivated;
				return this;
			}
		}
	}
}
