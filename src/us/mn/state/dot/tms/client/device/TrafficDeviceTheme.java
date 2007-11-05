/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.device;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.client.proxy.LocationProxy;

/**
 * Theme for traffic devices on map
 *
 * @author Douglas Lau
 */
abstract public class TrafficDeviceTheme extends StyledTheme {

	/** Outline for stroking traffic devices */
	static protected final Outline OUTLINE = Outline.createSolid(
		Color.BLACK, 30);

	/** Outline for stroking traffic devices */
	static protected final Outline INACTIVE_OUTLINE = Outline.createSolid(
		Color.BLACK, 10);

	/** Color to display inactive devices */
	static protected final Color COLOR_INACTIVE = new Color(0, 0, 0, 32);

	/** Color to display failed devices */
	static protected final Color COLOR_FAILED = Color.GRAY;

	/** Color to display unavailable devices */
	static protected final Color COLOR_UNAVAILABLE = Color.BLACK;

	/** Color to display available devices */
	static protected final Color COLOR_AVAILABLE = new Color(96, 96, 255);

	/** Color to display deployed devices */
	static protected final Color COLOR_DEPLOYED = Color.YELLOW;

	/** Default style */
	protected Style dstyle;

	/** Create a new traffic device theme */
	protected TrafficDeviceTheme(String n, Shape s) {
		super(n, s);
	}

	/** Map of status code to styles */
	protected final Map<Integer, Style> sty_map =
		new HashMap<Integer, Style>();

	/** Add a style to the theme */
	protected void addStyle(int status, String name, Color color,
		Outline outline)
	{
		Style style = new Style(name, outline, color);
		sty_map.put(status, style);
		if(dstyle == null)
			dstyle = style;
		addStyle(style);
	}

	/** Add a style to the theme */
	protected void addStyle(int status, String name, Color color) {
		addStyle(status, name, color, OUTLINE);
	}

	/** Get the style for a given status code */
	protected Style getStyle(int status) {
		Style sty = sty_map.get(status);
		if(sty != null)
			return sty;
		else
			return dstyle;
	}

	/** Get an appropriate style for the given map object */
	public Style getStyle(MapObject o) {
		return getStyle(getStatusCode(o));
	}

	/** Get the status code for a given map object */
	protected int getStatusCode(MapObject o) {
		if(o instanceof TrafficDeviceProxy)
			return ((TrafficDeviceProxy)o).getStatusCode();
		else
			return TrafficDevice.STATUS_INACTIVE;
	}

	/** Get the symbol for a given status code */
	public Symbol getSymbol(int status) {
		Style sty = getStyle(status);
		return getSymbol(sty.getLabel());
	}

	/** Get tooltip text for the given map object */
	public String getTip(MapObject o) {
		if(o instanceof TrafficDeviceProxy) {
			TrafficDeviceProxy proxy = (TrafficDeviceProxy)o;
			LocationProxy loc = (LocationProxy)proxy.getLocation();
			return proxy.getId() + " - " +
				loc.getDescription();
		} else
			return null;
	}
}
