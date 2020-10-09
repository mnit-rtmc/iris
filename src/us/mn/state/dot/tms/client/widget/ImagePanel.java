/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Panel for displaying an image.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class ImagePanel extends JPanel {
	
	/** Width of the image */
	protected int width;
	
	/** Height of the image */
	protected int height;
	
	/** Image being displayed */
	protected BufferedImage image;
	
	/** Create a new ImagePanel with width w and height h. */
	public ImagePanel(int w, int h) {
		width = w;
		height = h;
		setLayout(new FlowLayout(FlowLayout.CENTER));
		setPreferredSize(new Dimension(width, height));
	}
	
	/** Draw the image on the panel. */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null)
			g.drawImage(image, 0, 0, width, height, null);
	}
	
	/** Set the image that will be displayed. */
	public void setImage(BufferedImage img) {
		image = img;
		repaint();
	}
	
	/** Set the size of the image. */
	public void setImageSize(int w, int h) {
		width = w;
		height = h;
		setPreferredSize(new Dimension(width, height));
		repaint();
	}
}
