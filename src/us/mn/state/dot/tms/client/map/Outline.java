/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.client.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

/**
 * A symbol outline
 *
 * @author Douglas Lau
 */
public class Outline {

	/** Cap to render endpoints */
	static private final int CAP = BasicStroke.CAP_ROUND;

	/** Join to render line joins */
	static private final int JOIN = BasicStroke.JOIN_BEVEL;

	/** Create a solid outline */
	static public Outline createSolid(Color c, float w) {
		return new Outline(c, w, null);
	}

	/** Create a dotted outline */
	static public Outline createDotted(Color c, float w) {
		float[] d = new float[] { w, w * 2 };
		return new Outline(c, w, d);
	}

	/** Create a dashed outline */
	static public Outline createDashed(Color c, float w) {
		float[] d = new float[] { w * 3, w * 2 };
		return new Outline(c, w, d);
	}

	/** Create a dash-dot outline */
	static public Outline createDashDotted(Color c, float w) {
		float[] d = new float[] { w * 3, w * 2, w, w * 2 };
		return new Outline(c, w, d);
	}

	/** Create a dash-dot-dot outline */
	static public Outline createDashDotDotted(Color c, float w) {
		float[] d = new float[] { w * 3, w * 2, w, w * 2, w, w * 2 };
		return new Outline(c, w, d);
	}

	/** Create a new outline */
	private Outline(Color c, float w, float[] d) {
		color = c;
		width = w;
		dash = d;
	}

	/** Color to render the outline */
	public final Color color;

	/** Width of the outline */
	private final float width;

	/** Dash pattern */
	private final float[] dash;

	/** Get the outline stroke */
	public Stroke getStroke(float scale) {
		float w = width * scale;
		if (dash != null) {
			float[] d = new float[dash.length];
			for (int i = 0; i < d.length; i++)
				d[i] = dash[i] * scale;
			return new BasicStroke(w, CAP, JOIN, 1, d, 0);
		} else
			return new BasicStroke(w, CAP, JOIN);
	}
}
