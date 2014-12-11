/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * A glyph defines the graphic used for a single code point in a font.
 *
 * @author Douglas Lau
 */
public class GlyphImpl extends BaseObjectImpl implements Glyph {

	/** Load all the glyphs */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GlyphImpl.class);
		store.query("SELECT name, font, code_point, graphic " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
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

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
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

	/** Create a glyph from database lookup */
	protected GlyphImpl(String n, String f, int p, String g)
		throws TMSException
	{
		this(n);
		if(f != null)
			font = FontHelper.lookup(f);
		codePoint = p;
		if(g != null)
			graphic = GraphicHelper.lookup(g);
	}

	/** Font to which the glyph belongs */
	protected Font font;

	/** Get the font */
	public Font getFont() {
		return font;
	}

	/** Code point in the font */
	protected int codePoint;

	/** Get the code point */
	public int getCodePoint() {
		return codePoint;
	}

	/** Graphic image of glyph */
	protected Graphic graphic;

	/** Set the graphic */
	public void setGraphic(Graphic g) {
		graphic = g;
	}

	/** Set the graphic */
	public void doSetGraphic(Graphic g) throws TMSException {
		if(g == graphic)
			return;
		store.update(this, "graphic", g);
		setGraphic(g);
	}

	/** Get the graphic */
	public Graphic getGraphic() {
		return graphic;
	}
}
