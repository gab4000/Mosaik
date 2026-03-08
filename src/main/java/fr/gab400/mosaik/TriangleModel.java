package fr.gab400.mosaik;

import dev.xernas.photon.api.model.Model;

public class TriangleModel extends Model {
	
	public TriangleModel() {
		super(new Vertex[]{
				new Vertex(-0.5f, 0.5f, 0, 0, 0, 0, 0, 0),
				new Vertex(0.5f, 0.5f, 0, 0, 0, 0, 0, 0),
				new Vertex(0, 0, 0, 0, 0, 0, 0, 0) // A(0;1), B(1;1), C(1/2;1/2)
		}, new int[]{0, 1, 2});
	}
}
