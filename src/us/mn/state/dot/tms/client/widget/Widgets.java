/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
import java.awt.Font;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * UI Widget stuff.
 *
 * @author Douglas Lau
 */
public class Widgets {

	/** Standard horizontal gap between components */
	static private final int HGAP = 4;

	/** Standard vertical gap between components */
	static private final int VGAP = 4;

	/** Current widget state */
	static public Widgets UI = new Widgets(1);

	/** Initialize the widget state */
	static public void init(float s) {
		UI = new Widgets(s);
		scaleLookAndFeel(s);
	}

	/** Horizontal gap between components */
	public final int hgap;

	/** Vertical gap between components */
	public final int vgap;

	/** Empty border to put around panels */
	public final EmptyBorder border;

	/** Create widget state */
	private Widgets(float s) {
		hgap = Math.round(HGAP * s);
		vgap = Math.round(VGAP * s);
		border = new EmptyBorder(vgap, hgap, vgap, hgap);
	}

	/** Create insets with proper gaps */
	public Insets insets() {
		return new Insets(vgap, hgap, vgap, hgap);
	};

	/** Scale the look-and-feel */
	static private void scaleLookAndFeel(float scale) {
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		HashSet<Object> keys = new HashSet<Object>(defaults.keySet());
		Iterator<Object> it = keys.iterator();
		while(it.hasNext()) {
			Object key = it.next();
			Font f = scaleFont(key, scale);
			if(f != null)
				defaults.put(key, f);
			Insets i = scaleInsets(key, scale);
			if(i != null)
				defaults.put(key, i);
			Dimension d = scaleDimension(key, scale);
			if(d != null)
				defaults.put(key, d);
		}
	}

	/** Scale a font from the look-and-feel */
	static private Font scaleFont(Object key, float scale) {
		Font font = UIManager.getFont(key);
		if(font != null)
			return font.deriveFont(scale * font.getSize2D());
		else
			return null;
	}

	/** Scale an insets from the look-and-feel */
	static private Insets scaleInsets(Object key, float scale) {
		Insets insets = UIManager.getInsets(key);
		if(insets != null) {
			return new Insets(Math.round(insets.top * scale),
				Math.round(insets.left * scale),
				Math.round(insets.bottom * scale),
				Math.round(insets.right * scale));
		} else
			return null;
	}

	/** Scale a dimension from the look-and-feel */
	static private Dimension scaleDimension(Object key, float scale) {
		Dimension d = UIManager.getDimension(key);
		if(d != null) {
			return new Dimension(Math.round(d.width * scale),
				Math.round(d.height * scale));
		} else
			return null;
	}
}
