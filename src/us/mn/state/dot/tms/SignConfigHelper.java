/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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

/**
 * Helper for dealing with sign configurations.
 *
 * @author Douglas Lau
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
	 * @param dt DMS type.
	 * @param p Portable flag.
	 * @param t Technology.
	 * @param sa Sign access.
	 * @param l Legend.
	 * @param bt Beacon type.
	 * @param mk Software make.
	 * @param md Software model.
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
	 * @param cs Color scheme ordinal.
	 * @param mf Monochrome foreground color (24-bit).
	 * @param mb Monochrome background color (24-bit).
	 * @return Matching sign config, or null if not found. */
	static public SignConfig find(DMSType dt, boolean p, String t,
		String sa, String l, String bt, String mk, String md, int fw,
		int fh, int bh, int bv, int ph, int pv, int pxw, int pxh,
		int cw, int ch, int cs, int mf, int mb)
	{
		int dti = dt.ordinal();
		Iterator<SignConfig> it = iterator();
		while (it.hasNext()) {
			SignConfig sc = it.next();
			if (sc.getDmsType() == dti &&
			    sc.getPortable() == p &&
			    t.equals(sc.getTechnology()) &&
			    sa.equals(sc.getSignAccess()) &&
			    l.equals(sc.getLegend()) &&
			    bt.equals(sc.getBeaconType()) &&
			    mk.equals(sc.getSoftwareMake()) &&
			    md.equals(sc.getSoftwareModel()) &&
			    sc.getFaceWidth() == fw &&
			    sc.getFaceHeight() == fh &&
			    sc.getBorderHoriz() == bh &&
			    sc.getBorderVert() == bv &&
			    sc.getPitchHoriz() == ph &&
			    sc.getPitchVert() == pv &&
			    sc.getPixelWidth() == pxw &&
			    sc.getPixelHeight() == pxh &&
			    sc.getCharWidth() == cw &&
			    sc.getCharHeight() == ch &&
			    sc.getColorScheme() == cs &&
			    sc.getMonochromeForeground() == mf &&
			    sc.getMonochromeBackground() == mb)
			{
				return sc;
			}
		}
		return null;
	}

	/** Check if a font is usable for a sign configuration */
	static public boolean isFontUsable(SignConfig sc, Font f) {
		return isFontWidthUsable(sc, f) && isFontHeightUsable(sc, f);
	}

	/** Check if a font width is usable for a sign configuration. */
	static private boolean isFontWidthUsable(SignConfig sc, Font f) {
		if (f.getWidth() > sc.getPixelWidth())
			return false;
		if (isCharMatrix(sc)) {
			// char-matrix signs must match font width
			// and must not have character spacing
			return f.getWidth() == sc.getCharWidth()
			    && f.getCharSpacing() == 0;
		} else {
			// line- or full-matrix signs must have char spacing
			return f.getCharSpacing() > 0;
		}
	}

	/** Check for character-matrix sign */
	static private boolean isCharMatrix(SignConfig sc) {
		return sc.getCharWidth() > 0;
	}

	/** Check if a font height is usable */
	static private boolean isFontHeightUsable(SignConfig sc, Font f) {
		if (f.getHeight() > sc.getPixelHeight())
			return false;
		if (isFullMatrix(sc)) {
			// full-matrix signs must have line spacing
			return f.getLineSpacing() > 0;
		} else {
			// char- or line-matrix signs must match font height
			// and must not have line spacing
			return f.getHeight() == sc.getCharHeight()
			    && f.getLineSpacing() == 0;
		}
	}

	/** Check for full-matrix sign */
	static private boolean isFullMatrix(SignConfig sc) {
		return sc.getCharHeight() <= 0;
	}
}
