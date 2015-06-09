/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2015  Minnesota Department of Transportation
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

	/** SONAR type name */
	String SONAR_TYPE = "graphic";

	/** Set the graphic number */
	void setGNumber(Integer n);

	/** Get the graphic number */
	Integer getGNumber();

	/** Set the bits-per-pixel (1, 8, 24) */
	void setBpp(int b);

	/** Get the bits-per-pixel */
	int getBpp();

	/** Set the height (pixels) */
	void setHeight(int h);

	/** Get the height (pixels) */
	int getHeight();

	/** Set the width (pixels) */
	void setWidth(int w);

	/** Get the width (pixels) */
	int getWidth();

	/** Set the pixel data (base64 encoded).  For 24-bit, uses BGR. */
	void setPixels(String p);

	/** Get the pixel data (base64 encoded).  For 24-bit, uses BGR. */
	String getPixels();
}
