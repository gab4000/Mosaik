package fr.gab400.mosaik;

import dev.xernas.microscope.helper.PathHelper;
import dev.xernas.photon.PhotonAPI;
import dev.xernas.photon.api.IRenderer;
import dev.xernas.photon.api.Transform;
import dev.xernas.photon.api.framebuffer.IFramebuffer;
import dev.xernas.photon.api.model.IMesh;
import dev.xernas.photon.api.model.Model;
import dev.xernas.photon.api.shader.IShader;
import dev.xernas.photon.api.shader.Shader;
import dev.xernas.photon.api.texture.ITexture;
import dev.xernas.photon.api.window.Window;
import dev.xernas.photon.api.window.cursor.CursorShape;
import dev.xernas.photon.api.window.input.Key;
import dev.xernas.photon.exceptions.PhotonException;
import dev.xernas.photon.utils.MatrixUtils;
import dev.xernas.photon.utils.ShaderResource;
import fr.gab400.mosaik.models.CursorModel;
import fr.gab400.mosaik.models.TriangleModel;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class Mosaik {
	
	private final Window window;
	private final IRenderer<IFramebuffer, IShader, IMesh, ITexture> renderer;
	private final Transform.CameraTransform camera;
	private boolean running;
	private float fps;
	
	private IShader shader;
	private IMesh borderMesh, cursorMesh;
	private Vector2f cameraStartPos;
	private Vector2f cameraDeltaDir;
	private float zoomLevel;
	private Color actual;
	
	private final Grid grid;
	
	public Mosaik(Window window, Grid grid) {
		this.window = window;
		this.renderer = PhotonAPI.getRenderer(window, true);
		this.camera = new Transform.CameraTransform();
		this.grid = grid;
		this.cameraStartPos = new Vector2f();
		this.cameraDeltaDir = new Vector2f();
		this.zoomLevel = grid.getCellSize();
		this.actual = Color.BLUE;
	}
	
	public void run() throws PhotonException {
		start();
		
		float deltaTime;
		int frameCount = 0;
		float fpsTimer = 0f;
		while (running) {
			long startTime = System.nanoTime();
			
			window.update(renderer);
			if (!window.isOpen()) running = false;
			window.setTitle(window.getDefaultTitle() + " | FPS: " + Math.round(fps));
			
			// Input
			input();
			
			// Camera movement
			camera.move(new Vector3f(-cameraDeltaDir.x, -cameraDeltaDir.y, 0));
			cameraDeltaDir.set(0);
			
			window.getInput().resetScrollDelta();
			
			// Rendering
			renderer.clear(Globals.BACK_COLOR);
			renderer.render(shader, cursorMesh, () -> {
				shader.setUniform("projectionMatrix", MatrixUtils.createOrthoMatrix(window));
				Vector3f position = new Vector3f(
						getMousePos().add(0.04f, -0.04f),
						0f
				);
				shader.setUniform("modelMatrix", MatrixUtils.createTransformationMatrix(new Transform(position).scale(0.025f)));
				shader.setUniform("viewMatrix", MatrixUtils.create2DViewMatrix(camera));
				shader.setUniform("color", this.actual);
			});
			List<Grid.Cell> cells = grid.getCells();
			for (Grid.Cell cell : cells) {
				for (Grid.Cell.Border border : cell.getDivision()) {
					renderer.render(shader, borderMesh, () -> {
						shader.setUniform("projectionMatrix", MatrixUtils.createOrthoMatrix(window));
						Vector3f position = new Vector3f(
								border.getCell().getX() * (grid.getCellSize() + Grid.getCellSpacing()) - grid.getWorldWidth() / 2,
								border.getCell().getY() * (grid.getCellSize() + Grid.getCellSpacing()) - grid.getWorldHeight() / 2,
								0f
						);
						Vector3f rotation = new Vector3f(
								0,
								0,
								border.getId() * 90
						);
						shader.setUniform("modelMatrix", MatrixUtils.createTransformationMatrix(new Transform(position, rotation).scale(grid.getCellSize())));
						shader.setUniform("viewMatrix", MatrixUtils.create2DViewMatrix(camera));
						shader.setUniform("color", border.getColor());
					});
				}
			}
			
			if (fpsTimer >= 1f) {
				fps = frameCount / fpsTimer;
				fpsTimer = 0f;
				frameCount = 0;
			}
			
			// Time measurements
			frameCount++;
			long endTime = System.nanoTime();
			long frameTime = endTime - startTime;
			deltaTime = frameTime / (float) 1_000_000_000;
			fpsTimer += deltaTime;
		}
		
		clean();
	}
	
	private void input() throws PhotonException {
		// Managing camera movement with middle mouse button
		if (window.getInput().isPressing(Key.MOUSE_LEFT)) {
			window.setCursorShape(CursorShape.HAND);
			if (cameraStartPos == null) {
				cameraStartPos = window.getInput().getMouse().toWorldSpace(window);
			} else {
				Vector2f currentMousePos = window.getInput().getMouse().toWorldSpace(window);
				cameraDeltaDir = new Vector2f(currentMousePos).sub(cameraStartPos);
				cameraStartPos.set(currentMousePos);
			}
		} else {
			window.setCursorShape(CursorShape.ARROW);
			cameraStartPos = null;
		}
		// Managing zoom with mouse scroll
		if (window.getInput().getMouse().hasScrolled()) {
			float scrollAmount = window.getInput().getMouse().getScroll();
			zoomLevel += scrollAmount * Globals.ZOOM_LEVEL;
			if (zoomLevel < Globals.ZOOM_LEVEL) zoomLevel = Globals.ZOOM_LEVEL;
			grid.setCellSize(zoomLevel);
			
			Vector2f currentMousePos = window.getInput().getMouse().toWorldSpace(window);
			cameraDeltaDir = new Vector2f(currentMousePos.mul(zoomLevel).mul(- 1));
		}
		// Managing cells with mouses buttons
		else if (window.getInput().hasReleased(Key.MOUSE_LEFT)) {
			Vector2f mouseWorldPos = getMouseCameraPos();

			int cellX = (int) (mouseWorldPos.x / (grid.getCellSize() + Grid.getCellSpacing()));
			int cellY = (int) (mouseWorldPos.y / (grid.getCellSize() + Grid.getCellSpacing()));
			Grid.Cell cell = grid.getCellAt(cellX, cellY);
			if (cell == null) return;

			float x = mouseWorldPos.x / (grid.getCellSize() + Grid.getCellSpacing()) - cellX - 0.5f;
			float y = mouseWorldPos.y / (grid.getCellSize() + Grid.getCellSpacing()) - cellY - 0.5f;

			Grid.Cell.Border border = getBorderAt(cell, x, y);
			if (border == null) {
				System.err.println("Border is null!");
				return;
			}

			if (cell.isNotValid(border.withColor(this.actual).withActivated(true))) {
				System.err.println("Cell not valid!");
				return;
			}
			border.applyColor().applyActivated();
		}
		else if (window.getInput().hasReleased(Key.MOUSE_RIGHT)) {
			Vector2f mouseWorldPos = getMouseCameraPos();
			
			int cellX = (int) (mouseWorldPos.x / (grid.getCellSize() + Grid.getCellSpacing()));
			int cellY = (int) (mouseWorldPos.y / (grid.getCellSize() + Grid.getCellSpacing()));
			Grid.Cell cell = grid.getCellAt(cellX, cellY);
			if (cell == null) return;
			
			float x = mouseWorldPos.x / (grid.getCellSize() + Grid.getCellSpacing()) - cellX - 0.5f;
			float y = mouseWorldPos.y / (grid.getCellSize() + Grid.getCellSpacing()) - cellY - 0.5f;
			
			Grid.Cell.Border border = getBorderAt(cell, x, y);
			if (border == null) {
				System.err.println("Border is null!");
				return;
			}
			
			if (cell.isNotValid(border.withColor(Globals.GRID_COLOR).withActivated(false))) {
				System.err.println("Cell not valid!");
				return;
			}
			border.applyColor().applyActivated();
		}
		
		if (window.getInput().hasReleased(Key.KEY_ESCAPE)) {
			running = false;
		}
		
		if (window.getInput().hasReleased(Key.KEY_ARROW_UP)) {
			this.actual = Globals.BLUE_COLOR;
		}
		if (window.getInput().hasReleased(Key.KEY_ARROW_DOWN)) {
			this.actual = Globals.RED_COLOR;
		}
		if (window.getInput().hasReleased(Key.KEY_ARROW_RIGHT)) {
			this.actual = Globals.YELLOW_COLOR;
		}
		if (window.getInput().hasReleased(Key.KEY_ARROW_LEFT)) {
			this.actual = Globals.GREEN_COLOR;
		}
	}
	
	private void start() throws PhotonException {
		window.start();
		renderer.start();
		
		Shader gameShader = getShaderFromResources();
		Model borderModel = new TriangleModel();
		Model cursorModel = new CursorModel();
		shader = renderer.loadShader(gameShader);
		borderMesh = renderer.loadMesh(borderModel);
		cursorMesh = renderer.loadMesh(cursorModel);
		
		window.show();
		window.hideCursor();
		running = true;
	}
	
	private void clean() throws PhotonException {
		renderer.dispose();
		window.dispose();
	}
	
	private @Nullable Grid.Cell.Border getBorderAt(Grid.Cell cell, float x, float y) {
		Grid.Cell.Border border;
		
		if (checkTrianglePos(new float[]{x, y}, new float[]{-0.5f, 0.5f}, new float[]{0.5f, 0.5f}, new float[]{0, 0})) border = cell.getDivision()[0];
		else if (checkTrianglePos(new float[]{x, y}, new float[]{-0.5f, -0.5f}, new float[]{-0.5f, 0.5f}, new float[]{0, 0})) border = cell.getDivision()[1];
		else if (checkTrianglePos(new float[]{x, y}, new float[]{0.5f, -0.5f}, new float[]{-0.5f, -0.5f}, new float[]{0, 0})) border = cell.getDivision()[2];
		else if (checkTrianglePos(new float[]{x, y}, new float[]{0.5f, 0.5f}, new float[]{0.5f, -0.5f}, new float[]{0, 0})) border = cell.getDivision()[3];
		else border = null;
		
		return border;
	}
	
	private boolean checkTrianglePos(float[] point, float[] a, float[] b, float[] c) {
		return (checkPointPos(point, a, b) && checkPointPos(point, c, a) && checkPointPos(point, b , c))
				|| (!checkPointPos(point, a, b) && !checkPointPos(point, c, a) && !checkPointPos(point, b , c));
	}
	
	private boolean checkPointPos(float[] point, float[] a, float[] b) {
		return (a[0] - point[0]) * (b[1] - point[1]) >= (a[1] - point[1]) * (b[0] - point[0]);
	}
	
	private Vector2f getMouseCameraPos() {
		// Get mouse position in world space CONSIDERING CAMERA POSITION
		Vector2f mousePos = window.getInput().getMouse().toWorldSpace(window);
		mousePos.add(new Vector2f(camera.getPosition().x, camera.getPosition().y));
		mousePos.add(new Vector2f((grid.getWorldWidth() + grid.getCellSize() + Grid.getCellSpacing()) / 2, (grid.getWorldHeight() + grid.getCellSize() + Grid.getCellSpacing()) / 2));
		return mousePos;
	}
	
	private Vector2f getMousePos() {
		// Get mouse position in world space
		Vector2f mousePos = window.getInput().getMouse().toWorldSpace(window);
		mousePos.add(new Vector2f(camera.getPosition().x, camera.getPosition().y));
		return mousePos;
	}

	private boolean areNeighbors(Grid.Cell.Border border1, Grid.Cell.Border border2) {
		Grid.Cell.Border border1Neighbor = getNeighbor(border1);
		Grid.Cell.Border border2Neighbor = getNeighbor(border2);
		if (border1Neighbor == null || border2Neighbor == null) return false;

		return border1Neighbor == border2 && border2Neighbor == border1;
    }

	private byte getNeighborId(Grid.Cell.Border border) {
		return switch (border.getId()) {
			case 0 -> 2;
			case 1 -> 3;
			case 2 -> 0;
			case 3 -> 1;
            default -> throw new IllegalStateException("Unexpected value: " + border.getId());
        };
	}

	private @Nullable Grid.Cell.Border getNeighbor(Grid.Cell.Border border) {
		Grid.Cell cell = border.getCell();
		byte neighborId = getNeighborId(border);
		Grid.Cell neighborCell = null;
		switch (neighborId) {
			case 0 -> neighborCell = grid.getCellAt(cell.getX(), cell.getY() + 1);
			case 1 -> neighborCell = grid.getCellAt(cell.getX() - 1, cell.getY());
			case 2 -> neighborCell = grid.getCellAt(cell.getX(), cell.getY() - 1);
			case 3 -> neighborCell = grid.getCellAt(cell.getX() + 1, cell.getY());
		}
		if (neighborCell == null) {
			System.err.println("Neighbor cell is null!");
			return null;
		}
		return neighborCell.getDivision()[neighborId];
	}
	
	private Shader getShaderFromResources() throws PhotonException {
		try {
			Path vertexShaderPath = PathHelper.getResourcePath("shaders/game.vert");
			Path fragmentShaderPath = PathHelper.getResourcePath("shaders/game.frag");
			String vertexSource = PathHelper.getStringOf(vertexShaderPath);
			String fragmentSource = PathHelper.getStringOf(fragmentShaderPath);
			ShaderResource vertexResource = new ShaderResource(vertexShaderPath.getFileName().toString(), vertexSource);
			ShaderResource fragmentResource = new ShaderResource(fragmentShaderPath.getFileName().toString(), fragmentSource);
			return new Shader(vertexResource, fragmentResource);
		} catch (IOException | URISyntaxException e) {
			throw new PhotonException("Failed to load shader files from resources", e);
		}
	}
}
