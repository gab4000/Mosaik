package fr.gab400.mosaik.models;

import dev.xernas.photon.api.model.Model;

public class CursorModel extends Model {
	public CursorModel() {
		final float angle = (float) (-125 * Math.PI / 180);
		final float angle2 = (float) (115 * Math.PI / 180);
		final float mul2 = 1.75f;
		super(new Vertex[]{
				new Vertex(0, 0, 0, 0, 0, 0, 0, 0),
				new Vertex(1, 0, 0, 0, 0, 0, 0, 0),
				new Vertex((float) (mul2 * Math.cos(angle2)), (float) (mul2 * Math.sin(angle2)), 0, 0, 0, 0, 0, 0),
				new Vertex((float) Math.cos(angle), (float) Math.sin(angle), 0, 0, 0, 0, 0, 0)
		}, new int[]{0, 1, 2, 2, 0, 3});
	}
}
