/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * Helper for dealing with sign configurations.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignConfigHelper extends BaseHelper {

	/** Do not allow objects of this class */
	private SignConfigHelper() {
		assert false;
	}

	/** Lookup the sign config with the specified name */
	static public SignConfig lookup(String name) {
		return (SignConfig) namespace.lookupObject(
			SignConfig.SONAR_TYPE, name);
	}

	/** Get a sign config iterator */
	static public Iterator<SignConfig> iterator() {
		return new IteratorWrapper<SignConfig>(namespace.iterator(
			SignConfig.SONAR_TYPE));
	}

	/** Find a sign config with matching attributes.
	 * @param fw Face width (mm).
	 * @param fh Face height (mm).
	 * @param bh Border -- horizontal (mm).
	 * @param bv Border -- vertical (mm).
	 * @param ph Pitch -- horizontal (mm).
	 * @param pv Pitch -- vertical (mm).
	 * @param pxw Pixel width.
	 * @param pxh Pixel height.
	 * @param cw Character width.
	 * @param ch Character height.
	 * @param mf Monochrome foreground color (24-bit).
	 * @param mb Monochrome background color (24-bit).
	 * @param cs Color scheme ordinal.
	 * @return Matching sign config, or null if not found. */
	static public SignConfig find(int fw, int fh, int bh, int bv, int ph,
		int pv, int pxw, int pxh, int cw, int ch, int mf, int mb,
		int cs)
	{
		Iterator<SignConfig> it = iterator();
		while (it.hasNext()) {
			SignConfig sc = it.next();
			if (sc.getFaceWidth() == fw &&
			    sc.getFaceHeight() == fh &&
			    sc.getBorderHoriz() == bh &&
			    sc.getBorderVert() == bv &&
			    sc.getPitchHoriz() == ph &&
			    sc.getPitchVert() == pv &&
			    sc.getPixelWidth() == pxw &&
			    sc.getPixelHeight() == pxh &&
			    sc.getCharWidth() == cw &&
			    sc.getCharHeight() == ch &&
			    sc.getMonochromeForeground() == mf &&
			    sc.getMonochromeBackground() == mb &&
			    sc.getColorScheme() == cs)
			{
				return sc;
			}
		}
		return null;
	}

	/** Check for character-matrix sign */
	static public boolean isCharMatrix(SignConfig sc) {
		return (sc != null) && (sc.getCharWidth() > 0);
	}

	/** Check for full-matrix sign */
	static public boolean isFullMatrix(SignConfig sc) {
		return (sc != null) && (sc.getCharHeight() <= 0);
	}

	/** Get a set of all signs in with a sign config */
	static public Set<DMS> getAllSigns(SignConfig sc) {
		TreeSet<DMS> signs = new TreeSet<DMS>(
			new NumericAlphaComparator<DMS>());
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			if (dms.getSignConfig() == sc)
				signs.add(dms);
		}
		return signs;
	}

	/** Create a raster builder for a sign config */
	static public RasterBuilder createRasterBuilder(SignConfig sc) {
		if (sc != null) {
			int pw = sc.getPixelWidth();
			int ph = sc.getPixelHeight();
			int cw = sc.getCharWidth();
			int ch = sc.getCharHeight();
			int fn = sc.getDefaultFont();
			ColorScheme cs = ColorScheme.fromOrdinal(
				sc.getColorScheme());
			return new RasterBuilder(pw, ph, cw, ch, fn, cs);
		} else
			return null;
	}

	/** Get default text rectangle for a sign config */
	static public TextRect textRect(SignConfig sc) {
		if (sc != null) {
			int width = sc.getPixelWidth();
			int height = sc.getPixelHeight();
			int c_height = sc.getCharHeight();
			int fn = sc.getDefaultFont();
			return new TextRect(1, 1, 1, width, height, c_height,
				fn, true);
		} else
			return null;
	}
}
