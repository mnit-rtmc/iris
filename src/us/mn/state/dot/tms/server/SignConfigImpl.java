/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.TMSException;

/**
 * A sign configuration defines the type and dimensions of a sign.
 *
 * @author Douglas Lau
 */
public class SignConfigImpl extends BaseObjectImpl implements SignConfig {

	/** Last allocated sign config ID */
	static private int last_id = 0;

	/** Create a unique sign config name */
	static private synchronized String createUniqueName() {
		String n = createNextName();
		while (namespace.lookupObject(SONAR_TYPE, n) != null)
			n = createNextName();
		return n;
	}

	/** Create the next system config name */
	static private String createNextName() {
		last_id++;
		// Check if the ID has rolled over to negative numbers
		if (last_id < 0)
			last_id = 0;
		return "cfg_" + last_id;
	}

	/** Load all the sign configs */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, SignConfigImpl.class);
		store.query("SELECT name, dms_type, portable, technology, " +
			"sign_access, legend, beacon_type, face_width, " +
			"face_height, border_horiz, border_vert, pitch_horiz, "+
			"pitch_vert, pixel_width, pixel_height, char_width, " +
			"char_height, default_font FROM iris." +
			SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new SignConfigImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("dms_type", dms_type.ordinal());
		map.put("portable", portable);
		map.put("technology", technology);
		map.put("sign_access", sign_access);
		map.put("legend", legend);
		map.put("beacon_type", beacon_type);
		map.put("face_width", face_width);
		map.put("face_height", face_height);
		map.put("border_horiz", border_horiz);
		map.put("border_vert", border_vert);
		map.put("pitch_horiz", pitch_horiz);
		map.put("pitch_vert", pitch_vert);
		map.put("pixel_width", pixel_width);
		map.put("pixel_height", pixel_height);
		map.put("char_width", char_width);
		map.put("char_height", char_height);
		map.put("default_font", default_font);
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

	/** Create a sign config */
	private SignConfigImpl(ResultSet row) throws SQLException {
		this(row.getString(1),		// name
		     row.getInt(2),		// dms_type
		     row.getBoolean(3),		// portable
		     row.getString(4),		// technology
		     row.getString(5),		// sign_access
		     row.getString(6),		// legend
		     row.getString(7),		// beacon_type
		     row.getInt(8),		// face_width
		     row.getInt(9),		// face_height
		     row.getInt(10),		// border_horiz
		     row.getInt(11),		// border_vert
		     row.getInt(12),		// pitch_horiz
		     row.getInt(13),		// pitch_vert
		     row.getInt(14),		// pixel_width
		     row.getInt(15),		// pixel_height
		     row.getInt(16),		// char_width
		     row.getInt(17),		// char_height
		     row.getString(18)		// default_font
		);
	}

	/** Create a sign config */
	private SignConfigImpl(String n, int dt, boolean p, String t, String sa,
		String l, String bt, int fw, int fh, int bh, int bv, int ph,
		int pv, int pxw, int pxh, int cw, int ch, String df)
	{
		super(n);
		dms_type = DMSType.fromOrdinal(dt);
		portable = p;
		technology = t;
		sign_access = sa;
		legend = l;
		beacon_type = bt;
		face_width = fw;
		face_height = fh;
		border_horiz = bh;
		border_vert = bv;
		pitch_horiz = ph;
		pitch_vert = pv;
		pixel_width = pxw;
		pixel_height = pxh;
		char_width = cw;
		char_height = ch;
		default_font = FontHelper.lookup(df);
	}

	/** Create a new sign config (by IRIS) */
	public SignConfigImpl(int dt, boolean p, String t, String sa, String l,
		String bt, int fw, int fh, int bh, int bv, int ph, int pv,
		int pxw, int pxh, int cw, int ch, String df)
	{
		this(createUniqueName(), dt, p, t, sa, l, bt, fw, fh, bh, bv,
		     ph, pv, pxw, pxh, cw, ch, df);
	}

	/** DMS type enum value */
	private final DMSType dms_type;

	/** Get DMS type */
	@Override
	public int getDmsType() {
		return dms_type.ordinal();
	}

	/** Portable flag */
	private final boolean portable;

	/** Get portable flag */
	@Override
	public boolean getPortable() {
		return portable;
	}

	/** Sign technology description */
	private final String technology;

	/** Get sign technology description */
	@Override
	public String getTechnology() {
		return technology;
	}

	/** Sign access description */
	private final String sign_access;

	/** Get sign access description */
	@Override
	public String getSignAccess() {
		return sign_access;
	}

	/** Sign legend string */
	private final String legend;

	/** Get sign legend */
	@Override
	public String getLegend() {
		return legend;
	}

	/** Beacon type description */
	private final String beacon_type;

	/** Get beacon type description */
	@Override
	public String getBeaconType() {
		return beacon_type;
	}

	/** Width of the sign face (mm) */
	private final int face_width;

	/** Get width of the sign face (mm) */
	@Override
	public int getFaceWidth() {
		return face_width;
	}

	/** Height of sign face (mm) */
	private final int face_height;

	/** Get height of the sign face (mm) */
	@Override
	public int getFaceHeight() {
		return face_height;
	}

	/** Horizontal border (mm) */
	private final int border_horiz;

	/** Get horizontal border (mm) */
	@Override
	public int getBorderHoriz() {
		return border_horiz;
	}

	/** Vertical border (mm) */
	private final int border_vert;

	/** Get vertical border (mm) */
	@Override
	public int getBorderVert() {
		return border_vert;
	}

	/** Horizontal pitch (mm) */
	private final int pitch_horiz;

	/** Get horizontal pitch (mm) */
	@Override
	public int getPitchHoriz() {
		return pitch_horiz;
	}

	/** Vertical pitch (mm) */
	private final int pitch_vert;

	/** Get vertical pitch (mm) */
	@Override
	public int getPitchVert() {
		return pitch_vert;
	}

	/** Sign width in pixels */
	private final int pixel_width;

	/** Get sign width (pixels) */
	@Override
	public int getPixelWidth() {
		return pixel_width;
	}

	/** Sign height (pixels) */
	private final int pixel_height;

	/** Get sign height (pixels) */
	@Override
	public int getPixelHeight() {
		return pixel_height;
	}

	/** Character width (pixels; 0 means variable) */
	private final int char_width;

	/** Get character width (pixels) */
	@Override
	public int getCharWidth() {
		return char_width;
	}

	/** Character height (pixels; 0 means variable) */
	private final int char_height;

	/** Get character height (pixels) */
	@Override
	public int getCharHeight() {
		return char_height;
	}

	/** Default font */
	private Font default_font;

	/** Set the default font */
	@Override
	public void setDefaultFont(Font f) {
		default_font = f;
	}

	/** Set the default font */
	public void doSetDefaultFont(Font f) throws TMSException {
		if (f != default_font) {
			store.update(this, "default_font", f);
			setDefaultFont(f);
		}
	}

	/** Get the default font */
	@Override
	public Font getDefaultFont() {
		return default_font;
	}
}
