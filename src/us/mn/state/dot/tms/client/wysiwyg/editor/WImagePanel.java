/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.editor;

import javax.swing.JPanel;

import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;
import us.mn.state.dot.tms.utils.wysiwyg.WRaster;

import static us.mn.state.dot.tms.client.widget.Widgets.UI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;


/**
 * Panel for displaying a WYSIWYG DMS image.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WImagePanel extends JPanel {
	
	private int width;
	private int height;
	private WPage pg;
	private WRaster wr;
	private BufferedImage image = null;
	
	public WImagePanel(int w, int h) {
		width = w;
		height = h;
		Dimension d = UI.dimension(width, height);
		setMinimumSize(d);
		setPreferredSize(d);
	}
	
	public void setPage(WPage p) {
		pg = p;
		
		// get the raster object, set the image size, and get the image
		wr = pg.getRaster();
		
		try {
			wr.setWysiwygImageSize(width, height);
			image = wr.getWysiwygImage();
		} catch (InvalidMsgException e) {
			// TODO do something with this
			image = null;
		}
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (image != null)
			g.drawImage(image, 0, 0, null);
	}
}
