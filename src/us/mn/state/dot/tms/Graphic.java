/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2019  Minnesota Department of Transportation
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
 * A graphic is an image which can be displayed on a DMS.
 *
 * @author Douglas Lau
 */
public interface Graphic extends SonarObject {

	/** Maximum allowed graphic number */
	int MAX_NUMBER = 999;

	/** Maximum allowed graphic width */
	int MAX_WIDTH = 240;

	/** Maximum allowed graphic height */
	int MAX_HEIGHT = 144;

	/** SONAR type name */
	String SONAR_TYPE = "graphic";

	/** Set the graphic number */
	void setGNumber(int n);

	/** Get the graphic number */
	int getGNumber();

	/** Get the color scheme (ordinal of ColorScheme) */
	int getColorScheme();

	/** Get the height (pixels) */
	int getHeight();

	/** Get the width (pixels) */
	int getWidth();

	/** Set the transparent color */
	void setTransparentColor(Integer tc);

	/** Get the transparent color */
	Integer getTransparentColor();

	/** Get the pixel data (base64 encoded).  For 24-bit, uses BGR. */
	String getPixels();
}
