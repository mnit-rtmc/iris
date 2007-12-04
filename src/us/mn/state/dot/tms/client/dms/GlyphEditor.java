/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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

import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.client.toast.TmsForm;

/**
 * GlyphEditor is a panel for editing font glyphs
 *
 * @author Douglas Lau
 */
public class GlyphEditor extends JPanel {

	/** Icon to display an "off" pixel */
	static protected final Icon PIXEL_OFF = new PixelIcon(false);

	/** Icon to display an "on" pixel */
	static protected final Icon PIXEL_ON = new PixelIcon(true);

	/** Glyph data */
	protected FontForm.GlyphData gdata;

	/** Character height */
	protected int height;

	/** Character width */
	protected int width;

	/** Grid panel */
	protected final JPanel gpanel = new JPanel();

	/** Grid layout */
	protected GridLayout grid;

	/** Pixel toggle buttons */
	protected JToggleButton[] p_button;

	/** "Narrow" button */
	protected final JButton narrow = new JButton("<<");

	/** "Wide" button */
	protected final JButton wide = new JButton(">>");

	/** Apply button */
	protected final JButton apply = new JButton("Apply Changes");

	/** Create a box with glue on either side of a component */
	static protected Box createGlueBox(JComponent c) {
		Box box = Box.createHorizontalBox();
		box.add(Box.createGlue());
		box.add(c);
		box.add(Box.createGlue());
		return box;
	}

	/** Create a glyph editor */
	public GlyphEditor() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder(
			"Selected Character"));
		add(Box.createGlue());
		add(createGlueBox(gpanel));
		add(Box.createVerticalStrut(TmsForm.VGAP));
		Box box = Box.createHorizontalBox();
		box.add(Box.createGlue());
		box.add(narrow);
		box.add(Box.createGlue());
		box.add(wide);
		box.add(Box.createGlue());
		add(box);
		add(Box.createVerticalStrut(TmsForm.VGAP));
		add(createGlueBox(apply));
		add(Box.createGlue());
	}

	/** Create a pixel toggle button */
	protected JToggleButton createPixelButton() {
		JToggleButton b = new JToggleButton(PIXEL_OFF);
		b.setSelectedIcon(PIXEL_ON);
		b.setBorder(null);
		b.setContentAreaFilled(false);
		b.setMargin(new Insets(0, 0, 0, 0));
		return b;
	}

	/** Set the glyph to edit */
	protected void _setGlyph() {
		height = gdata.graphic.getHeight();
		width = gdata.graphic.getWidth();
		grid = new GridLayout(height, width);
		gpanel.setLayout(grid);
		p_button = new JToggleButton[height * width];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				int i = y * width + x;
				p_button[i] = createPixelButton();
				p_button[i].setSelected(
					gdata.bmap.getPixel(x, y) > 0);
				gpanel.add(p_button[i]);
			}
		}
	}

	/** Set the glyph to edit */
	public void setGlyph(FontForm.GlyphData g) {
		gdata = g;
		gpanel.removeAll();
		if(gdata != null)
			_setGlyph();
		gpanel.setMaximumSize(gpanel.getPreferredSize());
		revalidate();
	}
}
