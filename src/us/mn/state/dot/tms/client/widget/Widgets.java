/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
 * Copyright (C) 2009-2014  AHMCT, University of California
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * UI Widget stuff.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class Widgets {

	/** Standard horizontal gap between components */
	static private final int HGAP = 3;

	/** Standard vertical gap between components */
	static private final int VGAP = 3;

	/** Current widget state */
	static public Widgets UI = new Widgets(1);

	/** Initialize the widget state */
	static public void init(float s) {
		UI = new Widgets(s);
		tweakLookAndFeel();
		scaleLookAndFeel();
	}

	/** Tweak the look and feel */
	static private void tweakLookAndFeel() {
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);
		UIManager.put("ComboBox.disabledForeground",
			new javax.swing.plaf.ColorUIResource(Color.GRAY));
		UIManager.put("TextField.inactiveForeground",
			 new javax.swing.plaf.ColorUIResource(Color.GRAY));
		UIManager.put("TextArea.inactiveForeground",
			 new javax.swing.plaf.ColorUIResource(Color.GRAY));
	}

	/** Scale the look-and-feel */
	static private void scaleLookAndFeel() {
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		HashSet<Object> keys = new HashSet<Object>(defaults.keySet());
		Iterator<Object> it = keys.iterator();
		while(it.hasNext()) {
			Object key = it.next();
			Font f = scaleFont(key);
			if(f != null)
				defaults.put(key, f);
			Dimension d = scaleDimension(key);
			if(d != null)
				defaults.put(key, d);
		}
	}

	/** Scale a font from the look-and-feel */
	static private Font scaleFont(Object key) {
		Font font = UIManager.getFont(key);
		if(font != null)
			return font.deriveFont(UI.scale * font.getSize2D());
		else
			return null;
	}

	/** Scale a dimension from the look-and-feel */
	static private Dimension scaleDimension(Object key) {
		Dimension d = UIManager.getDimension(key);
		if(d != null)
			return dimension(d.width, d.height);
		else
			return null;
	}

	/** Scale a dimension */
	static public Dimension dimension(int w, int h) {
		return new Dimension(UI.scaled(w), UI.scaled(h));
	}

	/**
	 * Create a font derived from the look-and-feel.
	 * @param key The font key name from which to derive
	 * @param style The desired AWT Font style constant
	 * @param scaling The desired scaling factor
	 * @return The derived font
	 */
	static public Font deriveFont(Object key, int style, double scaling) {
		Font font = UIManager.getFont(key);
		if (font == null)
			return null;
		AffineTransform at = AffineTransform.getScaleInstance(
			scaling, scaling);
		Font newFont = font.deriveFont(style, at);
		return newFont;
	}

	/** Get a scaled value */
	public int scaled(int d) {
		return Math.round(d * scale);
	}

	/** Scale factor */
	public final float scale;

	/** Horizontal gap between components */
	public final int hgap;

	/** Vertical gap between components */
	public final int vgap;

	/** Empty border to put around panels */
	public final EmptyBorder border;

	/** Create widget state */
	private Widgets(float s) {
		scale = s;
		hgap = scaled(HGAP);
		vgap = scaled(VGAP);
		border = new EmptyBorder(vgap, hgap, vgap, hgap);
	}

	/** Create insets with proper gaps */
	public Insets insets() {
		return new Insets(vgap, hgap, vgap, hgap);
	};

	/** Create button insets with proper gaps */
	public Insets buttonInsets() {
		return new Insets(0, hgap, 0, hgap);
	};

	/** Create a panel border */
	public Border panelBorder() {
		// Don't scale border
		return new EmptyBorder(VGAP, HGAP, VGAP, HGAP);
	}

	/** Create a panel border */
	public Border noTopBorder() {
		// Don't scale border
		return new EmptyBorder(0, HGAP, VGAP, HGAP);
	}

	/** Create a button border */
	public Border buttonBorder() {
		return BorderFactory.createCompoundBorder(
		       BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
		       BorderFactory.createEmptyBorder(0, hgap, 0, hgap));
	}

	/** Create a cell renderer border */
	public Border cellRendererBorder() {
		return BorderFactory.createCompoundBorder(
		       BorderFactory.createEmptyBorder(1, 1, 1, 1),
		       BorderFactory.createRaisedBevelBorder());
	}
}
