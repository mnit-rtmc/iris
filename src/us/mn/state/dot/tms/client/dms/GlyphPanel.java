/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.Session;
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

	/** User session */
	private final Session session;

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

	/** Glyph editor */
	private final GlyphEditor geditor = new GlyphEditor();

	/** "Narrow" button */
	private final JButton narrow_btn = new JButton(
		new IAction("font.glyph.narrow")
	{
		protected void doActionPerformed(ActionEvent e) {
			narrowPressed();
		}
	});

	/** "Widen" button */
	private final JButton widen_btn = new JButton(
		new IAction("font.glyph.widen")
	{
		protected void doActionPerformed(ActionEvent e) {
			widenPressed();
		}
	});

	/** Apply button */
	private final JButton apply_btn = new JButton(
		new IAction("font.glyph.apply")
	{
		protected void doActionPerformed(ActionEvent e) {
			applyPressed();
		}
	});

	/** Create a glyph panel */
	public GlyphPanel(Session s) {
		super(new GridBagLayout());
		session = s;
		glyphs = s.getSonarState().getDmsCache().getGlyphs();
		graphics = s.getSonarState().getGraphics();
		ginfo = new GlyphInfo();
		updateButtonPanel();
		setBorder(BorderFactory.createTitledBorder(
			I18N.get("font.glyph.selected")));
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets = UI.insets();
		bag.gridwidth = 3;
		bag.gridx = 0;
		bag.gridy = 0;
		bag.weightx = 1;
		bag.weighty = 1;
		bag.fill = GridBagConstraints.BOTH;
		add(geditor, bag);
		bag.gridwidth = 1;
		bag.gridy = 1;
		bag.fill = GridBagConstraints.NONE;
		bag.weightx = 0.3333;
		bag.weighty = 0;
		add(narrow_btn, bag);
		bag.gridx = 1;
		add(apply_btn, bag);
		bag.gridx = 2;
		add(widen_btn, bag);
	}

	/** Dispose of the panel */
	public void dispose() {
		geditor.dispose();
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
	}

	/** Update button enabled states */
	private void updateButtonPanel() {
		GlyphInfo gi = ginfo;
		BitmapGraphic bg = bmap;
		boolean e = fontHeight() > 0 && canAddAndUpdate();
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
		if (gi.bmap != null) {
			BitmapGraphic bg = gi.bmap.createBlankCopy();
			bg.copy(gi.bmap);
			return bg;
		} else
			return new BitmapGraphic(0, fontHeight());
	}

	/** Set the glyph to edit */
	private void setBitmap(BitmapGraphic bg) {
		bmap = bg;
		geditor.setBitmap(bg);
		updateButtonPanel();
	}

	/** Narrow buton pressed */
	private void narrowPressed() {
		BitmapGraphic bg = bmap;
		if (bg.getWidth() > 0) {
			BitmapGraphic b = new BitmapGraphic(bg.getWidth() - 1,
				bg.getHeight());
			b.copy(bg);
			setBitmap(b);
		}
	}

	/** Widen buton pressed */
	private void widenPressed() {
		BitmapGraphic bg = bmap;
		if (bg.getWidth() < MAX_GLYPH_WIDTH) {
			BitmapGraphic b = new BitmapGraphic(bg.getWidth() + 1,
				bg.getHeight());
			b.copy(bg);
			setBitmap(b);
		}
	}

	/** Apply button pressed */
	private void applyPressed() {
		GlyphInfo gi = ginfo;
		BitmapGraphic bg = bmap;
		if (gi.exists())
			updateGlyph(gi, bg);
		else if (bg.getWidth() > 0)
			createGlyph(gi, bg);
	}

	/** Update an existing Glyph */
	private void updateGlyph(GlyphInfo gi, BitmapGraphic bg) {
		assert gi.exists();
		if (bg.getWidth() > 0) {
			gi.graphic.setWidth(bg.getWidth());
			gi.graphic.setPixels(bg.getEncodedPixels());
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
		if (c > 0 && f != null) {
			String name = f.getName() + "_" + c;
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("bpp", 1);
			attrs.put("height", bg.getHeight());
			attrs.put("width", bg.getWidth());
			attrs.put("pixels", bg.getEncodedPixels());
			graphics.createObject(name, attrs);
			attrs.clear();
			attrs.put("font", f);
			attrs.put("codePoint", c);
			attrs.put("graphic", name);
			glyphs.createObject(name, attrs);
		}
	}

	/** Check if the user can write a glyph */
	private boolean canAddAndUpdate() {
		return session.canAdd(Glyph.SONAR_TYPE) &&
		       session.canAdd(Graphic.SONAR_TYPE) &&
		       session.canWrite(Graphic.SONAR_TYPE, "width") &&
		       session.canWrite(Graphic.SONAR_TYPE, "pixels");
	}
}
