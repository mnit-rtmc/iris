/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  SRF Consulting Group
 * Copyright (C) 2018-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.Dimension;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import us.mn.state.dot.tms.Graphic;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Renders a static graphic for a DMS.
 *
 * @author Michael Janson
 * @author Douglas Lau
 */
public class StaticGraphicPanel extends JLabel {

	/** Create a new static graphic panel.
	 * @param w Pixel width of image.
	 * @param h Pixel height of image. */
	public StaticGraphicPanel(int w, int h) {
		setHorizontalAlignment(SwingConstants.TRAILING);
		setSizes(w, h);
	}

	/** Set the image size */
	private void setSizes(int width, int height) {
		Dimension d = UI.dimension(width, height);
		setMinimumSize(d);
		setPreferredSize(d);
	}

	/** Set the graphic displayed */
	public void setGraphic(Graphic g) {
		if (g != null) {
			Image img = imageScaled(g);
			setIcon(new ImageIcon(img));
		} else
			setIcon(null);
	}

	/** Create a scaled image */
	private Image imageScaled(Graphic g) {
		Image img = GraphicImage.create(g);
		double wp = g.getWidth();
		double hp = g.getHeight();
		if (wp > 0 && hp > 0) {
			double sx = getWidth() / wp;
			double sy = getHeight() / hp;
			double scale = Math.min(sx, sy);
			int w = (int) (wp * scale);
			int h = (int) (hp * scale);
			return img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
		} else
			return img;
	}
}
