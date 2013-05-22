/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.JLabel;

/**
 * A text shape is a shape which can be drawn on a Graphics context.
 *
 * @author Douglas Lau
 */
public class TextShape {

	/** Border around shapes */
	static private final float SHAPE_BORDER = 0.1f;

	/** Create a text shape */
	static public Shape create(Font font, String text) {
		FontRenderContext frc = new FontRenderContext(
			new AffineTransform(), false, false);
		GlyphVector vec = font.createGlyphVector(frc, text);
		Shape s = vec.getGlyphOutline(0);
		Rectangle2D rect = s.getBounds2D();
		AffineTransform a = new AffineTransform();
		a.translate(SHAPE_BORDER, SHAPE_BORDER);
		a.scale((1 - 2 * SHAPE_BORDER) / rect.getWidth(),
			(1 - 2 * SHAPE_BORDER) / rect.getHeight());
		a.translate(-rect.getX(), -rect.getY());
		return a.createTransformedShape(s);
	}

	/** Create a text shape */
	static public Shape create(String text) {
		return create(new JLabel().getFont(), text);
	}

	/** Don't allow instantiation */
	private TextShape() { }
}
