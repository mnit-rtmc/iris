/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2025  Minnesota Department of Transportation
 * Copyright (C) 2021  Iteris Inc.
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
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * A sign configuration defines the type and dimensions of a sign.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignConfigImpl extends BaseObjectImpl implements SignConfig,
	Comparable<SignConfigImpl>
{
	/** Create a unique sign config name */
	static private String createUniqueName(String template) {
		UniqueNameCreator unc = new UniqueNameCreator(template, 16,
			(n)->lookupSignConfig(n));
		return unc.createUniqueName();
	}

	/** Find existing or create a new sign config.
	 * @param fw Face width (mm).
	 * @param fh Face height (mm).
	 * @param bh Border -- horizontal (mm).
	 * @param bv Border -- vertical (mm).
	 * @param ph Pitch -- horizontal (mm).
	 * @param pv Pitch -- vertical (mm).
	 * @param pxw Pixel width.
	 * @param pxh Pixel height.
	 * @param cw Character width (0 means variable).
	 * @param ch Character height (0 means variable).
	 * @param mf Monochrome foreground color (24-bit).
	 * @param mb Monochrome background color (24-bit).
	 * @param cs Color scheme ordinal.
	 * @return Matching existing, or new sign config.
	 */
	static public SignConfigImpl findOrCreate(int fw, int fh, int bh, int bv,
		int ph, int pv, int pxw, int pxh, int cw, int ch, int mf, int mb,
		int cs)
	{
		if (fw <= 0 || fh <= 0 || bh < 0 || bv < 0 || ph <= 0 ||
		    pv <= 0 || pxw <= 0 || pxh <= 0 || cw < 0 || ch < 0)
			return null;
		SignConfig sc = SignConfigHelper.find(fw, fh, bh, bv, ph, pv,
			pxw, pxh, cw, ch, mf, mb, cs);
		if (sc instanceof SignConfigImpl)
			return (SignConfigImpl) sc;
		else {
			String template = "sc_" + pxw + "x" + pxh + "_%d";
			String n = createUniqueName(template);
			SignConfigImpl sci = new SignConfigImpl(n, fw, fh, bh,
				bv, ph, pv, pxw, pxh, cw, ch, mf, mb, cs,
				FontHelper.DEFAULT_FONT_NUM, null, null);
			return createNotify(sci);
		}
	}

	/** Notify clients of the new sign config */
	static private SignConfigImpl createNotify(SignConfigImpl sc) {
		try {
			sc.notifyCreate();
			return sc;
		}
		catch (SonarException e) {
			System.err.println("createNotify: " + e.getMessage());
			return null;
		}
	}

	/** Load all the sign configs */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, face_width, face_height, " +
			"border_horiz, border_vert, pitch_horiz, pitch_vert, " +
			"pixel_width, pixel_height, char_width, char_height, " +
			"monochrome_foreground, monochrome_background, " +
			"color_scheme, default_font, module_width, " +
			"module_height FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new SignConfigImpl(row));
			}
		});
	}

	/** Create a sign config */
	private SignConfigImpl(ResultSet row) throws SQLException {
		this(row.getString(1), // name
		     row.getInt(2),    // face_width
		     row.getInt(3),    // face_height
		     row.getInt(4),    // border_horiz
		     row.getInt(5),    // border_vert
		     row.getInt(6),    // pitch_horiz
		     row.getInt(7),    // pitch_vert
		     row.getInt(8),    // pixel_width
		     row.getInt(9),    // pixel_height
		     row.getInt(10),   // char_width
		     row.getInt(11),   // char_height
		     row.getInt(12),   // monochrome_foreground
		     row.getInt(13),   // monochrome_background
		     row.getInt(14),   // color_scheme
		     row.getInt(15),   // default_font
		     (Integer) row.getObject(16), // module_width
		     (Integer) row.getObject(17)  // module_height
		);
	}

	/** Create a sign config */
	private SignConfigImpl(String n, int fw, int fh, int bh, int bv, int ph,
		int pv, int pxw, int pxh, int cw, int ch, int mf, int mb,
		int cs, int df, Integer mw, Integer mh)
	{
		super(n);
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
		monochrome_foreground = mf;
		monochrome_background = mb;
		color_scheme = ColorScheme.fromOrdinal(cs);
		default_font = df;
		module_width = mw;
		module_height = mh;
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
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
		map.put("monochrome_foreground", monochrome_foreground);
		map.put("monochrome_background", monochrome_background);
		map.put("color_scheme", color_scheme.ordinal());
		map.put("default_font", default_font);
		map.put("module_width", module_width);
		map.put("module_height", module_height);
		return map;
	}

	/** Compare to another sign config */
	@Override
	public int compareTo(SignConfigImpl sc) {
		int c = Integer.compare(pixel_width, sc.pixel_width);
		if (c != 0)
			return c;
		c = Integer.compare(pixel_height, sc.pixel_height);
		if (c != 0)
			return c;
		else
			return name.compareTo(sc.name);
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

	/** Monochrome scheme foreground color (24-bit). */
	private final int monochrome_foreground;

	/** Get monochrome scheme foreground color (24-bit). */
	@Override
	public int getMonochromeForeground() {
		return monochrome_foreground;
	}

	/** Monochrome scheme background color (24-bit). */
	private final int monochrome_background;

	/** Get monochrome scheme background color (24-bit). */
	@Override
	public int getMonochromeBackground() {
		return monochrome_background;
	}

	/** DMS Color scheme */
	private final ColorScheme color_scheme;

	/** Get the color scheme (ordinal of ColorScheme) */
	@Override
	public int getColorScheme() {
		return color_scheme.ordinal();
	}

	/** Default font number */
	private int default_font;

	/** Set the default font number */
	@Override
	public void setDefaultFont(int df) {
		default_font = df;
	}

	/** Set the default font number */
	public void doSetDefaultFont(int df) throws TMSException {
		if (df != default_font) {
			store.update(this, "default_font", df);
			setDefaultFont(df);
		}
	}

	/** Get the default font number */
	@Override
	public int getDefaultFont() {
		return default_font;
	}

	/** Module width (pixels) */
	private Integer module_width;

	/** Get module width (pixels) */
	@Override
	public Integer getModuleWidth() {
		return module_width;
	}

	/** Set the module width (pixels) */
	@Override
	public void setModuleWidth(Integer mw) {
		module_width = mw;
	}

	/** Set the module width (pixels) */
	public void doSetModuleWidth(Integer mw) throws TMSException {
		if (!objectEquals(mw, module_width)) {
			store.update(this, "module_width", mw);
			setModuleWidth(mw);
		}
	}

	/** Module height (pixels) */
	private Integer module_height;

	/** Get height of the sign face (mm) */
	@Override
	public Integer getModuleHeight() {
		return module_height;
	}

	/** Set the module height (pixels) */
	@Override
	public void setModuleHeight(Integer mh) {
		module_height = mh;
	}

	/** Set the module height (pixels) */
	public void doSetModuleHeight(Integer mh) throws TMSException {
		if (!objectEquals(mh, module_height)) {
			store.update(this, "module_height", mh);
			setModuleHeight(mh);
		}
	}
}
