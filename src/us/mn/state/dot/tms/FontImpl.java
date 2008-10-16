/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
		System.err.println("Loading DMS fonts...");
		namespace.registerType(SONAR_TYPE, FontImpl.class);
		store.query("SELECT name, height, width, line_spacing, " +
			"char_spacing, version_id FROM font;",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new FontImpl(
					row.getString(1),	// name
					row.getInt(2),		// height
					row.getInt(3),		// width
					row.getInt(4),		// line_spacing
					row.getInt(5),		// char_spacing
					row.getInt(6)		// version_id
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("height", height);
		map.put("width", width);
		map.put("line_spacing", lineSpacing);
		map.put("char_spacing", charSpacing);
		map.put("version_id", versionID);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new font */
	public FontImpl(String n) {
		super(n);
	}

	/** Create a new font */
	protected FontImpl(String n, int h, int w, int ls, int cs, int v) {
		this(n);
		height = h;
		width = w;
		lineSpacing = ls;
		charSpacing = cs;
		versionID = v;
	}

	/** Mapping of code points to glyphs */
	protected final HashMap<Integer, GlyphImpl> glyphs =
		new HashMap<Integer, GlyphImpl>();

	/** Add a glyph */
	public void addGlyph(int p, GlyphImpl g) throws TMSException {
		synchronized(glyphs) {
			if(glyphs.containsKey(p))
				throw new ChangeVetoException("Glyph exists");
			glyphs.put(p, g);
		}
	}

	/** Remove a glyph */
	public void removeGlyph(int p, GlyphImpl g) {
		synchronized(glyphs) {
			glyphs.remove(p);
		}
	}

	/** Check if the font has any glyphs */
	protected boolean hasGlyphs() throws TMSException {
		synchronized(glyphs) {
			return !glyphs.isEmpty();
		}
	}

	/** Get a mapping of all the glyphs */
	public SortedMap<Integer, GlyphImpl> getGlyphs() {
		synchronized(glyphs) {
			return new TreeMap<Integer, GlyphImpl>(glyphs);
		}
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
		if(hasGlyphs())
			throw new ChangeVetoException("Glyphs exist");
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
		if(hasGlyphs())
			throw new ChangeVetoException("Glyphs exist");
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

	/** Lookup the glyph associated with a code point */
	protected GlyphImpl getGlyph(int code_point) {
		synchronized(glyphs) {
			return glyphs.get(code_point);
		}
	}

	/** Lookup the graphic associated with a code point */
	protected GraphicImpl getGraphic(int code_point)
		throws InvalidMessageException
	{
		GlyphImpl glyph = getGlyph(code_point);
		if(glyph != null) {
			GraphicImpl graphic = (GraphicImpl)glyph.getGraphic();
			if(graphic != null)
				return graphic;
			throw new InvalidMessageException("Invalid graphic");
		}
		throw new InvalidMessageException("Invalid code point: " +
			code_point);
	}

	/** Test if the font matches a specified character height/width */
	protected boolean matches(int h, int w) {
		if(h == 0)
			return w == getWidth();
		else
			return (h == height) && (w == getWidth());
	}

	/** Test if the font matches the specified parameters */
	public boolean matches(int h, int w, int ls) {
		if(ls == 0 || ls == lineSpacing)
			return matches(h, w);
		else
			return false;
	}
}
