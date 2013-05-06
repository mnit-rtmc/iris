/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * GlyphPanel is a panel for editing font glyphs.  It includes the glyph editor
 * plus buttons for adjusting the width and draw mode.
 *
 * @author Douglas Lau
 */
public class GlyphPanel extends JPanel {

	/** Maximum width of glyphs */
	static private final int MAX_GLYPH_WIDTH = 16;

	/** Icon to display an "off" pixel */
	static private final Icon PIXEL_OFF = new PixelIcon(false);

	/** Icon to display an "on" pixel */
	static private final Icon PIXEL_ON = new PixelIcon(true);

	/** Glyph type cache */
	private final TypeCache<Glyph> glyphs;

	/** Graphic type cache */
	private final TypeCache<Graphic> graphics;

	/** Current font */
	private Font font;

	/** Glyph info */
	private GlyphInfo ginfo;

	/** Working bitmap graphic */
	private BitmapGraphic bmap = new BitmapGraphic(0, 0);

	/** Grid panel */
	private final JPanel gpanel = new JPanel();

	/** Pixel toggle buttons */
	private JToggleButton[] p_button;

	/** "Narrow" button */
	private final JButton narrow_btn = new JButton(
		new IAction("font.glyph.narrow")
	{
		protected void do_perform() {
			narrowPressed();
		}
	});

	/** "Widen" button */
	private final JButton widen_btn = new JButton(
		new IAction("font.glyph.widen")
	{
		protected void do_perform() {
			widenPressed();
		}
	});

	/** Apply button */
	private final JButton apply_btn = new JButton(
		new IAction("font.glyph.apply")
	{
		protected void do_perform() {
			applyPressed();
		}
	});

	/** Create a box with glue on either side of a component */
	static private Box createGlueBox(JComponent c) {
		Box box = Box.createHorizontalBox();
		box.add(Box.createGlue());
		box.add(c);
		box.add(Box.createGlue());
		return box;
	}

	/** Create a glyph panel */
	public GlyphPanel(TypeCache<Glyph> gl, TypeCache<Graphic> gr) {
		glyphs = gl;
		graphics = gr;
		ginfo = new GlyphInfo();
		updateButtons();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder(
			I18N.get("font.glyph.selected")));
		add(Box.createGlue());
		add(createGlueBox(gpanel));
		add(Box.createVerticalStrut(UI.vgap));
		Box box = Box.createHorizontalBox();
		box.add(Box.createGlue());
		box.add(narrow_btn);
		box.add(Box.createGlue());
		box.add(widen_btn);
		box.add(Box.createGlue());
		add(box);
		add(Box.createVerticalStrut(UI.vgap));
		add(createGlueBox(apply_btn));
		add(Box.createGlue());
	}

	/** Create a pixel toggle button */
	private JToggleButton createPixelButton() {
		JToggleButton b = new JToggleButton(PIXEL_OFF);
		b.setSelectedIcon(PIXEL_ON);
		b.setBorder(null);
		b.setContentAreaFilled(false);
		b.setMargin(new Insets(0, 0, 0, 0));
		return b;
	}

	/** Set the font */
	public void setFont(Font f) {
		font = f;
		setGlyph(new GlyphInfo());
	}

	/** Set the glyph to edit */
	public void setGlyph(GlyphInfo gi) {
		assert gi != null;
		ginfo = gi;
		setBitmap(glyphBitmap(gi));
		repaint();
	}

	/** Update button enabled states */
	private void updateButtons() {
		GlyphInfo gi = ginfo;
		BitmapGraphic bg = bmap;
		boolean e = fontHeight() > 0;
		narrow_btn.setEnabled(e && bg.getWidth() > 0);
		widen_btn.setEnabled(e && bg.getWidth() < MAX_GLYPH_WIDTH);
		apply_btn.setEnabled(e);
	}

	/** Get the font height */
	private int fontHeight() {
		Font f = font;
		return f != null ? f.getHeight() : 0;
	}

	/** Get the glyph bitmap */
	private BitmapGraphic glyphBitmap(GlyphInfo gi) {
		assert gi != null;
		return (gi.bmap != null)
		     ? gi.bmap
		     : new BitmapGraphic(0, fontHeight());
	}

	/** Set the glyph to edit */
	private void setBitmap(BitmapGraphic bg) {
		gpanel.removeAll();
		bmap = bg;
		updateButtons();
		if(bg.getWidth() < 1) {
			repaint();
			return;
		}
		gpanel.setLayout(new GridLayout(bg.getHeight(), bg.getWidth()));
		p_button = new JToggleButton[bg.getHeight() * bg.getWidth()];
		for(int y = 0; y < bg.getHeight(); y++) {
			for(int x = 0; x < bg.getWidth(); x++) {
				int i = y * bg.getWidth() + x;
				p_button[i] = createPixelButton();
				p_button[i].setSelected(
					bg.getPixel(x, y).isLit());
				gpanel.add(p_button[i]);
			}
		}
		gpanel.setMaximumSize(gpanel.getPreferredSize());
		revalidate();
	}

	/** Update the bitmap with the current pixel button state */
	private void updateBitmap() {
		BitmapGraphic bg = bmap;
		for(int y = 0; y < bg.getHeight(); y++) {
			for(int x = 0; x < bg.getWidth(); x++) {
				int i = y * bg.getWidth() + x;
				DmsColor p = DmsColor.BLACK;
				if(p_button[i].isSelected())
					p = DmsColor.AMBER;
				bg.setPixel(x, y, p);
			}
		}
	}

	/** Narrow buton pressed */
	private void narrowPressed() {
		BitmapGraphic bg = bmap;
		if(bg.getWidth() > 0) {
			updateBitmap();
			BitmapGraphic b = new BitmapGraphic(bg.getWidth() - 1,
				bg.getHeight());
			b.copy(bg);
			setBitmap(b);
		}
	}

	/** Widen buton pressed */
	private void widenPressed() {
		BitmapGraphic bg = bmap;
		if(bg.getWidth() < MAX_GLYPH_WIDTH) {
			updateBitmap();
			BitmapGraphic b = new BitmapGraphic(bg.getWidth() + 1,
				bg.getHeight());
			b.copy(bg);
			setBitmap(b);
		}
	}

	/** Apply button pressed */
	private void applyPressed() {
		updateBitmap();
		GlyphInfo gi = ginfo;
		BitmapGraphic bg = bmap;
		if(gi.exists())
			updateGlyph(gi, bg);
		else if(bg.getWidth() > 0)
			createGlyph(gi, bg);
	}

	/** Update an existing Glyph */
	private void updateGlyph(GlyphInfo gi, BitmapGraphic bg) {
		assert gi.exists();
		if(bg.getWidth() > 0) {
			gi.graphic.setWidth(bg.getWidth());
			gi.graphic.setPixels(Base64.encode(bg.getPixels()));
		} else {
			gi.glyph.destroy();
			gi.graphic.destroy();
			setGlyph(new GlyphInfo(gi.code_point, null));
		}
	}

	/** Create a new Glyph */
	private void createGlyph(GlyphInfo gi, BitmapGraphic bg) {
		assert !gi.exists();
		int c = gi.code_point;
		Font f = font;
		if(c > 0 && f != null) {
			String name = f.getName() + "_" + c;
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("bpp", 1);
			attrs.put("height", bg.getHeight());
			attrs.put("width", bg.getWidth());
			attrs.put("pixels", Base64.encode(bg.getPixels()));
			graphics.createObject(name, attrs);
			attrs.clear();
			attrs.put("font", f);
			attrs.put("codePoint", c);
			attrs.put("graphic", name);
			glyphs.createObject(name, attrs);
		}
	}
}
