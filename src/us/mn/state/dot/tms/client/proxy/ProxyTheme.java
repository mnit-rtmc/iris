/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.Color;
import java.awt.Shape;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.SonarObject;

/**
 * Theme for SONAR proxy objects on map
 *
 * @author Douglas Lau
 */
public class ProxyTheme<T extends SonarObject> extends StyledTheme {

	/** Outline color */
	static protected final Color OUTLINE_COLOR = new Color(0, 0, 0, 128);

	/** Outline for stroking traffic devices */
	static public final Outline OUTLINE = Outline.createSolid(
		OUTLINE_COLOR, 1);

	/** Outline for stroking inactive traffic devices */
	static public final Outline OUTLINE_INACTIVE = Outline.createSolid(
		OUTLINE_COLOR, 1);

	/** Outline for stroking locked traffic devices */
	static public final Outline OUTLINE_LOCKED = Outline.createSolid(
		Color.RED, 2);

	/** Color to display inactive devices */
	static public final Color COLOR_INACTIVE = new Color(0, 0, 0, 32);

	/** Color to display "no controller" devices */
	static public final Color COLOR_NO_CONTROLLER =
		new Color(255, 255, 255, 64);

	/** Color to display failed devices */
	static public final Color COLOR_FAILED = Color.GRAY;

	/** Color to display unavailable devices */
	static public final Color COLOR_UNAVAILABLE = Color.BLACK;

	/** Color to display available devices */
	static public final Color COLOR_AVAILABLE = new Color(96, 96, 255);

	/** Color to display deployed devices */
	static public final Color COLOR_DEPLOYED = Color.YELLOW;

	/** Color to display scheduled devices */
	static public final Color COLOR_SCHEDULED = new Color(240, 128, 0);

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Default style */
	protected Style dstyle;

	/** Create a new SONAR proxy theme */
	public ProxyTheme(ProxyManager<T> m, String n, Shape s) {
		super(n, s);
		manager = m;
	}

	/** Add a style to the theme */
	public void addStyle(String name, Color color, Outline outline) {
		Style style = new Style(name, outline, color);
		addStyle(style);
	}

	/** Add a style to the theme */
	public void addStyle(String name, Color color) {
		addStyle(name, color, OUTLINE);
	}

	/** Add a default style to the theme */
	public void addStyle(String name) {
		dstyle = new Style(name, null, null);
		addStyle(dstyle);
	}

	/** Get an appropriate style for the given map object */
	public Style getStyle(MapObject o) {
		T proxy = manager.findProxy(o);
		if(proxy != null)
			return getStyle(proxy);
		else
			return dstyle;
	}

	/** Get an appropriate style for the given proxy object */
	protected Style getStyle(T proxy) {
		// FIXME: combine styles when it applies (locked meters)
		for(Style st: styles) {
			if(manager.checkStyle(st.getLabel(), proxy))
				return st;
		}
		return dstyle;
	}

	/** Get tooltip text for the given map object */
	public String getTip(MapObject o) {
		T proxy = manager.findProxy(o);
		if(proxy != null)
			return manager.getDescription(proxy);
		else
			return null;
	}
}
