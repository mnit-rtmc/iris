/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2015  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
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
		store.query("SELECT name, g_number, bpp, height, width, " +
			"pixels FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GraphicImpl(
					row.getString(1),	// name
					(Integer)row.getObject(2), // g_number
					row.getInt(3),		// bpp
					row.getInt(4),		// height
					row.getInt(5),		// width
					row.getString(6)	// pixels
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("g_number", g_number);
		map.put("bpp", bpp);
		map.put("height", height);
		map.put("width", width);
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
		g_number = null;
		bpp = 1;
		height = 0;
		width = 0;
		pixels = "";
	}

	/** Create a graphic from database lookup */
	protected GraphicImpl(String n, Integer g, int b, int h, int w,
		String p)
	{
		this(n);
		g_number = g;
		bpp = b;
		height = h;
		width = w;
		pixels = p;
	}

	/** Graphic number */
	private Integer g_number;

	/** Set the graphic number */
	@Override
	public void setGNumber(Integer g) {
		g_number = g;
	}

	/** Set the graphic number */
	public void doSetGNumber(Integer g) throws TMSException {
		if (g == g_number)
			return;
		if (g != null && (g < 1 || g > 255))
			throw new ChangeVetoException("Invalid g_number");
		store.update(this, "g_number", g);
		setGNumber(g);
	}

	/** Get the graphic number */
	@Override
	public Integer getGNumber() {
		return g_number;
	}

	/** Bits per pixel */
	private int bpp;

	/** Set the bits-per-pixel (1, 8, 24) */
	@Override
	public void setBpp(int b) {
		bpp = b;
	}

	/** Set the bits-per-pixel (1, 8, 24) */
	public void doSetBpp(int b) throws TMSException {
		if (b == bpp)
			return;
		if (b != 1 && b != 8 && b != 24)
			throw new ChangeVetoException("Invalid bpp");
		store.update(this, "bpp", b);
		setBpp(b);
	}

	/** Get the bits-per-pixel */
	@Override
	public int getBpp() {
		return bpp;
	}

	/** Height (number of pixels) */
	private int height;

	/** Set the height (pixels) */
	@Override
	public void setHeight(int h) {
		height = h;
	}

	/** Set the height (pixels) */
	public void doSetHeight(int h) throws TMSException {
		if (h == height)
			return;
		store.update(this, "height", h);
		setHeight(h);
	}

	/** Get the height (pixels) */
	@Override
	public int getHeight() {
		return height;
	}

	/** Width (number of pixels) */
	private int width;

	/** Set the width (pixels) */
	@Override
	public void setWidth(int w) {
		width = w;
	}

	/** Set the width (pixels) */
	public void doSetWidth(int w) throws TMSException {
		if (w == width)
			return;
		store.update(this, "width", w);
		setWidth(w);
	}

	/** Get the width (pixels) */
	@Override
	public int getWidth() {
		return width;
	}

	/** Pixel data (base64 encoded).  For 24-bit, uses BGR. */
	private String pixels;

	/** Set the pixel data (base64 encoded).  For 24-bit, uses BGR. */
	@Override
	public void setPixels(String p) {
		pixels = p;
	}

	/** Set the pixel data (base64 encoded) */
	public void doSetPixels(String p) throws TMSException {
		if (p.equals(pixels))
			return;
		try {
			Base64.decode(p);
		}
		catch (IOException e) {
			throw new ChangeVetoException("Invalid Base64 data");
		}
		store.update(this, "pixels", p);
		setPixels(p);
	}

	/** Get the pixel data (base64 encoded).  For 24-bit, uses BGR. */
	@Override
	public String getPixels() {
		return pixels;
	}
}
