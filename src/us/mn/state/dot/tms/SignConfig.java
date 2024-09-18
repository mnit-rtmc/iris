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

import us.mn.state.dot.sonar.SonarObject;

/**
 * A sign configuration defines the dimensions of a sign.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface SignConfig extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "sign_config";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = DMS.SONAR_TYPE;

	/** Get width of the sign face (mm) */
	int getFaceWidth();

	/** Get height of the sign face (mm) */
	int getFaceHeight();

	/** Get horizontal border (mm) */
	int getBorderHoriz();

	/** Get vertical border (mm) */
	int getBorderVert();

	/** Get horizontal pitch (mm) */
	int getPitchHoriz();

	/** Get vertical pitch (mm) */
	int getPitchVert();

	/** Get sign width (pixels) */
	int getPixelWidth();

	/** Get sign height (pixels) */
	int getPixelHeight();

	/** Get character width (pixels; 0 means variable) */
	int getCharWidth();

	/** Get character height (pixels; 0 means variable) */
	int getCharHeight();

	/** Get monochrome scheme foreground color (24-bit). */
	int getMonochromeForeground();

	/** Get monochrome scheme background color (24-bit). */
	int getMonochromeBackground();

	/** Get the color scheme (ordinal of ColorScheme) */
	int getColorScheme();

	/** Set the default font number */
	void setDefaultFont(int df);

	/** Get the default font number */
	int getDefaultFont();

	/** Get module width (pixels) */
	Integer getModuleWidth();

	/** Set module width(pixels) */
	void setModuleWidth(Integer mw);

	/** Get module height (pixels) */
	Integer getModuleHeight();

	/** Set module height (pixels) */
	void setModuleHeight(Integer mh);
}
