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
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Font;
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

	/** Current font */
	protected Font font;

	/** Glyph data */
	protected FontForm.GlyphData gdata;

	/** Working bitmap graphic */
	protected BitmapGraphic bmap;

	/** Grid panel */
	protected final JPanel gpanel = new JPanel();

	/** Pixel toggle buttons */
	protected JToggleButton[] p_button;

	/** "Narrow" button */
	protected final JButton narrow = new JButton("<<");

	/** "Widen" button */
	protected final JButton widen = new JButton(">>");

	/** Apply button */
	protected final JButton apply = new JButton("Apply Changes");

	/** Font form */
	protected final FontForm font_form;

	/** Create a box with glue on either side of a component */
	static protected Box createGlueBox(JComponent c) {
		Box box = Box.createHorizontalBox();
		box.add(Box.createGlue());
		box.add(c);
		box.add(Box.createGlue());
		return box;
	}

	/** Create a glyph editor */
	public GlyphEditor(FontForm form, boolean admin) {
		font_form = form;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder(
			"Selected Character"));
		add(Box.createGlue());
		add(createGlueBox(gpanel));
		if(admin) {
			add(Box.createVerticalStrut(TmsForm.VGAP));
			Box box = Box.createHorizontalBox();
			box.add(Box.createGlue());
			box.add(narrow);
			narrow.setEnabled(false);
			new ActionJob(this, narrow) {
				public void perform() {
					narrowPressed();
				}
			};
			box.add(Box.createGlue());
			box.add(widen);
			widen.setEnabled(false);
			new ActionJob(this, widen) {
				public void perform() {
					widenPressed();
				}
			};
			box.add(Box.createGlue());
			add(box);
			add(Box.createVerticalStrut(TmsForm.VGAP));
			add(createGlueBox(apply));
			apply.setEnabled(false);
			new ActionJob(this, apply) {
				public void perform() {
					applyPressed();
				}
			};
		}
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

	/** Set the font which owns the glyph */
	public void setFont(Font f) {
		font = f;
	}

	/** Set the glyph to edit */
	public void setGlyph(FontForm.GlyphData g) {
		gdata = g;
		gpanel.removeAll();
		if(gdata != null)
			setBitmap(gdata.bmap);
		else {
			int h = 0;
			if(font != null)
				h = font.getHeight();
			setBitmap(new BitmapGraphic(0, h));
		}
		apply.setEnabled(font != null);
	}

	/** Set the glyph to edit */
	protected void setBitmap(BitmapGraphic b) {
		gpanel.removeAll();
		bmap = b;
		if(b.width < 1) {
			narrow.setEnabled(false);
			widen.setEnabled(font != null);
			return;
		}
		gpanel.setLayout(new GridLayout(b.height, b.width));
		p_button = new JToggleButton[b.height * b.width];
		for(int y = 0; y < b.height; y++) {
			for(int x = 0; x < b.width; x++) {
				int i = y * b.width + x;
				p_button[i] = createPixelButton();
				p_button[i].setSelected(b.getPixel(x, y) > 0);
				gpanel.add(p_button[i]);
			}
		}
		gpanel.setMaximumSize(gpanel.getPreferredSize());
		narrow.setEnabled(true);
		widen.setEnabled(true);
		revalidate();
	}

	/** Update the bitmap with the current pixel button state */
	protected void updateBitmap() {
		BitmapGraphic b = bmap;
		for(int y = 0; y < b.height; y++) {
			for(int x = 0; x < b.width; x++) {
				int i = y * b.width + x;
				int p = 0;
				if(p_button[i].isSelected())
					p = 1;
				b.setPixel(x, y, p);
			}
		}
	}

	/** Narrow buton pressed */
	protected void narrowPressed() {
		if(bmap.width > 0) {
			updateBitmap();
			BitmapGraphic b = new BitmapGraphic(bmap.width - 1,
				bmap.height);
			b.copy(bmap);
			setBitmap(b);
		}
	}

	/** Widen buton pressed */
	protected void widenPressed() {
		if(bmap.width < 12) {
			updateBitmap();
			BitmapGraphic b = new BitmapGraphic(bmap.width + 1,
				bmap.height);
			b.copy(bmap);
			setBitmap(b);
		}
	}

	/** Update an existing Glyph */
	protected void updateGlyph() {
		if(bmap.width > 0) {
			gdata.graphic.setWidth(bmap.width);
			gdata.graphic.setPixels(Base64.encode(
				bmap.getBitmap()));
		} else {
			gdata.glyph.destroy();
			gdata.graphic.destroy();
			setGlyph(null);
		}
	}

	/** Apply button pressed */
	protected void applyPressed() {
		updateBitmap();
		if(gdata != null)
			updateGlyph();
		else if(bmap.width > 0)
			font_form.createGlyph();
	}
}
