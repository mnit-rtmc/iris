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
			"line_spacing, char_spacing, version_id FROM " +
			"iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new FontImpl(
					row.getString(1),	// name
					row.getInt(2),		// f_number
					row.getInt(3),		// height
					row.getInt(4),		// width
					row.getInt(5),		// line_spacing
					row.getInt(6),		// char_spacing
					row.getInt(7)		// version_id
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("f_number", f_number);
		map.put("height", height);
		map.put("width", width);
		map.put("line_spacing", lineSpacing);
		map.put("char_spacing", charSpacing);
		map.put("version_id", versionID);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new font */
	public FontImpl(String n) {
		super(n);
		f_number = FontHelper.findUnusedFontNumber();
	}

	/** Create a new font */
	protected FontImpl(String n, int num, int h, int w, int ls, int cs,
		int v)
	{
		this(n);
		f_number = num;
		height = h;
		width = w;
		lineSpacing = ls;
		charSpacing = cs;
		versionID = v;
	}

	/** Font number */
	protected int f_number;

	/** Set the font number */
	public void setNumber(int n) {
		f_number = n;
	}

	/** Set the font number */
	public void doSetNumber(int n) throws TMSException {
		if(n == f_number)
			return;
		if(n < 1 || n > 255)
			throw new ChangeVetoException("Invalid number");
		store.update(this, "f_number", n);
		setNumber(n);
	}

	/** Get the font number */
	public int getNumber() {
		return f_number;
	}

	/** Font height (in pixels) */
	protected int height = 7;

	/** Set the font height (pixels) */
	public void setHeight(int h) {
		height = h;
	}

	/** Set the font height (pixels) */
	public void doSetHeight(int h) throws TMSException {
		if(h == height)
			return;
		if(h < 4 || h > 24)
			throw new ChangeVetoException("Invalid height");
		store.update(this, "height", h);
		setHeight(h);
	}

	/** Get the font height (pixels) */
	public int getHeight() {
		return height;
	}

	/** Font width (in pixels) */
	protected int width = 5;

	/** Set the font width (pixels) */
	public void setWidth(int w) {
		width = w;
	}

	/** Set the font width (pixels) */
	public void doSetWidth(int w) throws TMSException {
		if(w == width)
			return;
		if(w < 0 || w > 12)
			throw new ChangeVetoException("Invalid width");
		store.update(this, "width", w);
		setWidth(w);
	}

	/** Get the font width (pixels) */
	public int getWidth() {
		return width;
	}

	/** Default horizontal spacing between characters (in pixels) */
	protected int charSpacing = 0;

	/** Set the default horizontal spacing between characters (pixels) */
	public void setCharSpacing(int s) {
		charSpacing = s;
	}

	/** Set the default horizontal spacing between characters (pixels) */
	public void doSetCharSpacing(int s) throws TMSException {
		if(s == charSpacing)
			return;
		if(s < 0 || s > 9)
			throw new ChangeVetoException("Invalid spacing");
		store.update(this, "char_spacing", s);
		setCharSpacing(s);
	}

	/** Get the default horizontal spacing between characters (pixels) */
	public int getCharSpacing() {
		return charSpacing;
	}

	/** Default vetical spacing between lines (in pixels) */
	protected int lineSpacing = 0;

	/** Set the default vertical spacing between lines (pixels) */
	public void setLineSpacing(int s) {
		lineSpacing = s;
	}

	/** Set the default vertical spacing between lines (pixels) */
	public void doSetLineSpacing(int s) throws TMSException {
		if(s == lineSpacing)
			return;
		if(s < 0 || s > 9)
			throw new ChangeVetoException("Invalid spacing");
		store.update(this, "line_spacing", s);
		setLineSpacing(s);
	}

	/** Get the default vertical spacing between lines (pixels) */
	public int getLineSpacing() {
		return lineSpacing;
	}

	/** Font version ID */
	protected int versionID = 0;

	/** Set the font version ID */
	public void setVersionID(int v) {
		versionID = v;
	}

	/** Set the font version ID */
	public void doSetVersionID(int v) throws TMSException {
		if(v == versionID)
			return;
		store.update(this, "version_id", v);
		setVersionID(v);
	}

	/** Get the font version ID */
	public int getVersionID() {
		return versionID;
	}
}
