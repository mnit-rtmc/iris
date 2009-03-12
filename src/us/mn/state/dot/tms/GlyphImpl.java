/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * A glyph defines the graphic used for a single code point in a font.
 *
 * @author Douglas Lau
 */
public class GlyphImpl extends BaseObjectImpl implements Glyph {

	/** Load all the glyphs */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading DMS glyphs...");
		namespace.registerType(SONAR_TYPE, GlyphImpl.class);
		store.query("SELECT name, font, code_point, graphic " +
			"FROM glyph;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GlyphImpl(
					row.getString(1),	// name
					row.getString(2),	// font
					row.getInt(3),		// code_point
					row.getString(4)	// graphic
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("font", font);
		map.put("code_point", codePoint);
		map.put("graphic", graphic);
		return map;
	}

	/** Destroy a glyph */
	public void doDestroy() throws TMSException {
		super.doDestroy();
		if(font != null)
			font.removeGlyph(codePoint, this);
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new glyph */
	public GlyphImpl(String n) {
		super(n);
		font = null;
		codePoint = 0;
		graphic = null;
	}

	/** Lookup a font in the SONAR namespace */
	static protected FontImpl lookupFont(String f) {
		return (FontImpl)namespace.lookupObject(Font.SONAR_TYPE, f);
	}

	/** Lookup a graphic in the SONAR namespace */
	static protected GraphicImpl lookupGraphic(String g) {
		return (GraphicImpl)namespace.lookupObject(Graphic.SONAR_TYPE,
			g);
	}

	/** Create a glyph from database lookup */
	protected GlyphImpl(String n, String f, int p, String g)
		throws TMSException
	{
		this(n);
		if(f != null) {
			font = lookupFont(f);
			if(font != null)
				font.addGlyph(p, this);
		}
		codePoint = p;
		if(g != null)
			graphic = lookupGraphic(g);
	}

	/** Font to which the glyph belongs */
	protected FontImpl font;

	/** Set the font */
	public void setFont(Font f) {
		font = (FontImpl)f;
	}

	/** Set the font */
	public void doSetFont(Font f) throws TMSException {
		if(f == font)
			return;
		if(f == null)
			throw new ChangeVetoException("Font cannot be null");
		((FontImpl)f).addGlyph(codePoint, this);
		store.update(this, "font", f.getName());
		if(font != null)
			font.removeGlyph(codePoint, this);
		setFont(f);
	}

	/** Get the font */
	public Font getFont() {
		return font;
	}

	/** Code point in the font */
	protected int codePoint;

	/** Set the code point */
	public void setCodePoint(int p) {
		codePoint = p;
	}

	/** Set the code point */
	public void doSetCodePoint(int p) throws TMSException {
		if(p == codePoint)
			return;
		if(font != null) {
			font.addGlyph(p, this);
			font.removeGlyph(codePoint, this);
		}
		store.update(this, "code_point", p);
		setCodePoint(p);
	}

	/** Get the code point */
	public int getCodePoint() {
		return codePoint;
	}

	/** Graphic image of glyph */
	protected GraphicImpl graphic;

	/** Set the graphic */
	public void setGraphic(Graphic g) {
		graphic = (GraphicImpl)g;
	}

	/** Set the graphic */
	public void doSetGraphic(Graphic g) throws TMSException {
		if(g == graphic)
			return;
		store.update(this, "graphic", g.getName());
		setGraphic(g);
	}

	/** Get the graphic */
	public Graphic getGraphic() {
		return graphic;
	}
}
