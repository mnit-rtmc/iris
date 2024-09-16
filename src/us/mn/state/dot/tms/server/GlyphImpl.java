/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.Base64;

/**
 * A glyph defines the bitmap used for a single code point in a font.
 *
 * @author Douglas Lau
 */
public class GlyphImpl extends BaseObjectImpl implements Glyph {

	/** Load all the glyphs */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GlyphImpl.class);
		store.query("SELECT name, font, code_point, width, pixels " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GlyphImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("font", font);
		map.put("code_point", code_point);
		map.put("width", width);
		map.put("pixels", pixels);
		return map;
	}

	/** Create a glyph from database lookup */
	private GlyphImpl(ResultSet row) throws SQLException {
		this(row.getString(1),  // name
		     row.getString(2),  // font
		     row.getInt(3),     // code_point
		     row.getInt(4),     // width
		     row.getString(5)   // pixels
		);
	}

	/** Create a glyph from database lookup */
	private GlyphImpl(String n, String f, int cp, int w, String p) {
		super(n);
		font = FontHelper.lookup(f);
		code_point = cp;
		width = w;
		pixels = p;
	}

	/** Create a new glyph */
	public GlyphImpl(String n) {
		super(n);
	}

	/** Font to which the glyph belongs */
	private Font font;

	/** Get the font */
	@Override
	public Font getFont() {
		return font;
	}

	/** Code point in the font */
	private int code_point;

	/** Get the code point */
	@Override
	public int getCodePoint() {
		return code_point;
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
		if (w != width) {
			store.update(this, "width", w);
			setWidth(w);
		}
	}

	/** Get the width (pixels) */
	@Override
	public int getWidth() {
		return width;
	}

	/** Pixel data (base64 encoded). */
	private String pixels;

	/** Set the pixel data (base64 encoded). */
	@Override
	public void setPixels(String p) {
		pixels = p;
	}

	/** Set the pixel data (base64 encoded) */
	public void doSetPixels(String p) throws TMSException {
		if (objectEquals(p, pixels))
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

	/** Get the pixel data (base64 encoded). */
	@Override
	public String getPixels() {
		return pixels;
	}
}
