/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.Base64;

/**
 * A graphic is an image which can be displayed on a DMS
 *
 * @author Douglas Lau
 */
public class GraphicImpl extends BaseObjectImpl implements Graphic {

	/** Load all the graphics */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GraphicImpl.class);
		store.query("SELECT name, g_number, color_scheme, height, " +
			"width, transparent_color, pixels FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GraphicImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("g_number", g_number);
		map.put("color_scheme", color_scheme);
		map.put("height", height);
		map.put("width", width);
		map.put("transparent_color", transparent_color);
		map.put("pixels", pixels);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new graphic */
	public GraphicImpl(String n) {
		super(n);
		g_number = 0;
		color_scheme = ColorScheme.MONOCHROME_1_BIT.ordinal();
		height = 0;
		width = 0;
		transparent_color = null;
		pixels = "";
	}

	/** Create a graphic from database lookup */
	private GraphicImpl(ResultSet row) throws SQLException {
		this(row.getString(1),          // name
		     row.getInt(2),             // g_number
		     row.getInt(3),             // color_scheme
		     row.getInt(4),             // height
		     row.getInt(5),             // width
		     (Integer) row.getObject(6),// transparent_color
		     row.getString(7)           // pixels
		);
	}

	/** Create a graphic from database lookup */
	private GraphicImpl(String n, int g, int cs, int h, int w, Integer tc,
		String p)
	{
		super(n);
		g_number = g;
		color_scheme = cs;
		height = h;
		width = w;
		transparent_color = tc;
		pixels = p;
	}

	/** Graphic number */
	private int g_number;

	/** Set the graphic number */
	@Override
	public void setGNumber(int g) {
		g_number = g;
	}

	/** Set the graphic number */
	public void doSetGNumber(int g) throws TMSException {
		if (g != g_number) {
			if (g < 1 || g > MAX_NUMBER)
				throw new ChangeVetoException("Invalid g_number");
			store.update(this, "g_number", g);
			setGNumber(g);
		}
	}

	/** Get the graphic number */
	@Override
	public int getGNumber() {
		return g_number;
	}

	/** DMS Color scheme (ordinal of ColorScheme) */
	private final int color_scheme;

	/** Get the color scheme (ordinal of ColorScheme) */
	@Override
	public int getColorScheme() {
		return color_scheme;
	}

	/** Height (number of pixels) */
	private int height;

	/** Get the height (pixels) */
	@Override
	public int getHeight() {
		return height;
	}

	/** Width (number of pixels) */
	private int width;

	/** Get the width (pixels) */
	@Override
	public int getWidth() {
		return width;
	}

	/** Transparent color */
	private Integer transparent_color;

	/** Set the transparent color */
	@Override
	public void setTransparentColor(Integer tc) {
		transparent_color = tc;
	}

	/** Set the transparent color */
	public void doSetTransparentColor(Integer tc) throws TMSException {
		if (tc != transparent_color) {
			store.update(this, "transparent_color", tc);
			setTransparentColor(tc);
		}
	}

	/** Get the transparent color */
	@Override
	public Integer getTransparentColor() {
		return transparent_color;
	}

	/** Pixel data (base64 encoded).  For 24-bit, uses BGR. */
	private String pixels;

	/** Get the pixel data (base64 encoded).  For 24-bit, uses BGR. */
	@Override
	public String getPixels() {
		return pixels;
	}
}
