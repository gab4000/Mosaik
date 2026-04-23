package fr.gab400.mosaik;

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
import fr.gab400.mosaik.models.CursorModel;
import fr.gab400.mosaik.models.TriangleModel;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
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
	private PossibilityCalculator calculator;   // ← Ajouté

	private int frameCount = 0;
	private float fpsTimer = 0f;

	public Mosaik(Window window, Grid grid) {
		this.window = window;
		this.renderer = PhotonAPI.getRenderer(window, true);
		this.camera = new Transform.CameraTransform();
		this.grid = grid;
		this.cameraStartPos = new Vector2f();
		this.cameraDeltaDir = new Vector2f();
		this.zoomLevel = grid.getCellSize();
		this.actual = Color.BLUE;
		
		// Initialisation du calculateur de possibilités
		this.calculator = new PossibilityCalculator(grid);
	}
	
	public void run() throws PhotonException {
		start();

		while (running) {
			loop();
		}
		
		clean();
	}

	private void loop() throws PhotonException {
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
		render();

		if (fpsTimer >= 1f) {
			fps = frameCount / fpsTimer;
			fpsTimer = 0f;
			frameCount = 0;
		}

		frameCount++;
		long endTime = System.nanoTime();
		long frameTime = endTime - startTime;
		fpsTimer += frameTime / (float) 1_000_000_000;
	}
	
	private void input() throws PhotonException {

		// Déplacement caméra (clic gauche maintenu)
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

		// Zoom avec molette
		if (window.getInput().getMouse().hasScrolled()) {
			float scrollAmount = window.getInput().getMouse().getScroll();
			zoomLevel += scrollAmount * Globals.ZOOM_LEVEL;
			if (zoomLevel < Globals.ZOOM_LEVEL) zoomLevel = Globals.ZOOM_LEVEL;
			grid.setCellSize(zoomLevel);
			
			Vector2f currentMousePos = window.getInput().getMouse().toWorldSpace(window);
			cameraDeltaDir = new Vector2f(currentMousePos.mul(zoomLevel).mul(-1));
		}
		// Clic gauche = activer bordure
		else if (window.getInput().hasReleased(Key.MOUSE_LEFT)) {
			grid.enableCell(getMouseCameraPos(), this.actual);
		}
		// Clic droit = désactiver bordure
		else if (window.getInput().hasReleased(Key.MOUSE_RIGHT)) {
			grid.disableCell(getMouseCameraPos());
		}
		
		// === NOUVELLE FONCTIONNALITÉ : Calcul des possibilités ===
		if (window.getInput().hasReleased(Key.KEY_P)) {
			System.out.println("\n🔄 Calcul du nombre maximal de possibilités en cours...");

			long start = System.currentTimeMillis();
			long possibilities = calculator.calculateMaxPossibilities();
			long time = System.currentTimeMillis() - start;

			System.out.println("══════════════════════════════════════════════");
			System.out.println("Nombre maximal de façons de compléter la mosaïque : " + possibilities);
			System.out.println("Temps de calcul : " + time + " ms");
			System.out.println("══════════════════════════════════════════════");

			// Affichage temporaire dans le titre de la fenêtre
			window.setTitle(window.getDefaultTitle() + " | Possibilités : " + possibilities);
		}
		
		if (window.getInput().hasReleased(Key.KEY_ESCAPE)) {
			running = false;
		}
		
		// Changement de couleur avec flèches
		if (window.getInput().hasReleased(Key.KEY_ARROW_UP))    this.actual = Globals.BLUE_COLOR;
		if (window.getInput().hasReleased(Key.KEY_ARROW_DOWN))  this.actual = Globals.RED_COLOR;
		if (window.getInput().hasReleased(Key.KEY_ARROW_RIGHT)) this.actual = Globals.YELLOW_COLOR;
		if (window.getInput().hasReleased(Key.KEY_ARROW_LEFT))  this.actual = Globals.GREEN_COLOR;
	}
	
	private void start() throws PhotonException {
		window.start();
		renderer.start();
		
		Shader gameShader = Utils.getShaderFromResources();
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
	
	private void render() throws PhotonException {
		renderer.clear(Globals.BACK_COLOR);
		
		// Curseur
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
		
		// Bordures de la grille
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
					Vector3f rotation = new Vector3f(0, 0, border.getId() * 90);
					shader.setUniform("modelMatrix", MatrixUtils.createTransformationMatrix(new Transform(position, rotation).scale(grid.getCellSize())));
					shader.setUniform("viewMatrix", MatrixUtils.create2DViewMatrix(camera));
					shader.setUniform("color", border.getColor());
				});
			}
		}
	}
	
	private Vector2f getMouseCameraPos() {
		Vector2f mousePos = window.getInput().getMouse().toWorldSpace(window);
		mousePos.add(new Vector2f(camera.getPosition().x, camera.getPosition().y));
		mousePos.add(new Vector2f((grid.getWorldWidth() + grid.getCellSize() + Grid.getCellSpacing()) / 2, 
		                          (grid.getWorldHeight() + grid.getCellSize() + Grid.getCellSpacing()) / 2));
		return mousePos;
	}
	
	private Vector2f getMousePos() {
		Vector2f mousePos = window.getInput().getMouse().toWorldSpace(window);
		mousePos.add(new Vector2f(camera.getPosition().x, camera.getPosition().y));
		return mousePos;
	}
}