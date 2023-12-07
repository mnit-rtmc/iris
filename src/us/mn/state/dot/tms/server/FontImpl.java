/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * The FontImpl class defines all the attributes of a pixel font. These
 * fonts are used for VMS messages, and are downloaded to NTCIP sign
 * controllers.
 *
 * @author Douglas Lau
 */
public class FontImpl extends BaseObjectImpl implements Font {

	/** Load all the fonts */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, FontImpl.class);
		store.query("SELECT name, f_number, height, width, " +
			"line_spacing, char_spacing FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new FontImpl(
					row.getString(1),	// name
					row.getInt(2),		// f_number
					row.getInt(3),		// height
					row.getInt(4),		// width
					row.getInt(5),		// line_spacing
					row.getInt(6)		// char_spacing
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("f_number", f_number);
		map.put("height", height);
		map.put("width", width);
		map.put("line_spacing", lineSpacing);
		map.put("char_spacing", charSpacing);
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

	/** Create a new font */
	public FontImpl(String n) {
		super(n);
		f_number = FontHelper.findUnusedFontNumber();
	}

	/** Create a new font */
	private FontImpl(String n, int num, int h, int w, int ls, int cs) {
		this(n);
		f_number = num;
		height = h;
		width = w;
		lineSpacing = ls;
		charSpacing = cs;
	}

	/** Font number */
	private int f_number;

	/** Set the font number */
	@Override
	public void setNumber(int n) {
		f_number = n;
	}

	/** Set the font number */
	public void doSetNumber(int n) throws TMSException {
		if (n != f_number) {
			store.update(this, "f_number", n);
			setNumber(n);
		}
	}

	/** Get the font number */
	@Override
	public int getNumber() {
		return f_number;
	}

	/** Font height (in pixels) */
	private int height = 7;

	/** Set the font height (pixels) */
	@Override
	public void setHeight(int h) {
		height = h;
	}

	/** Set the font height (pixels) */
	public void doSetHeight(int h) throws TMSException {
		if (h != height) {
			store.update(this, "height", h);
			setHeight(h);
		}
	}

	/** Get the font height (pixels) */
	@Override
	public int getHeight() {
		return height;
	}

	/** Font width (in pixels) */
	private int width = 5;

	/** Set the font width (pixels) */
	@Override
	public void setWidth(int w) {
		width = w;
	}

	/** Set the font width (pixels) */
	public void doSetWidth(int w) throws TMSException {
		if (w != width) {
			store.update(this, "width", w);
			setWidth(w);
		}
	}

	/** Get the font width (pixels) */
	@Override
	public int getWidth() {
		return width;
	}

	/** Default horizontal spacing between characters (in pixels) */
	private int charSpacing = 0;

	/** Set the default horizontal spacing between characters (pixels) */
	@Override
	public void setCharSpacing(int s) {
		charSpacing = s;
	}

	/** Set the default horizontal spacing between characters (pixels) */
	public void doSetCharSpacing(int s) throws TMSException {
		if (s != charSpacing) {
			store.update(this, "char_spacing", s);
			setCharSpacing(s);
		}
	}

	/** Get the default horizontal spacing between characters (pixels) */
	@Override
	public int getCharSpacing() {
		return charSpacing;
	}

	/** Default vetical spacing between lines (in pixels) */
	private int lineSpacing = 0;

	/** Set the default vertical spacing between lines (pixels) */
	@Override
	public void setLineSpacing(int s) {
		lineSpacing = s;
	}

	/** Set the default vertical spacing between lines (pixels) */
	public void doSetLineSpacing(int s) throws TMSException {
		if (s != lineSpacing) {
			store.update(this, "line_spacing", s);
			setLineSpacing(s);
		}
	}

	/** Get the default vertical spacing between lines (pixels) */
	@Override
	public int getLineSpacing() {
		return lineSpacing;
	}
}
