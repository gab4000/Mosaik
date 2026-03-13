package fr.gab400.mosaik;

import dev.xernas.photon.Library;
import dev.xernas.photon.PhotonAPI;
import dev.xernas.photon.api.window.Window;
import dev.xernas.photon.exceptions.PhotonException;

public class Main {
	static void main() throws PhotonException {
		PhotonAPI.init(Library.OPENGL_4_5, "Mosaik", "0.2.0", true);
		
		Window window = new Window("Mosaik", Globals.WIDTH, Globals.HEIGHT);
		Mosaik mosaik = new Mosaik(window, new Grid(Globals.GRID_WIDTH, Globals.GRID_HEIGHT, Globals.CELL_SIZE));
		
		mosaik.run();
	}
}
