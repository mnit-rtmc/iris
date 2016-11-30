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
package us.mn.state.dot.tms;

import us.mn.state.dot.sonar.SonarObject;

/**
 * A sign configuration defines the type and dimensions of a sign.
 *
 * @author Douglas Lau
 */
public interface SignConfig extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "sign_config";

	/** Get DMS type */
	int getDmsType();

	/** Get portable flag */
	boolean getPortable();

	/** Get sign technology description */
	String getTechnology();

	/** Get sign access description */
	String getSignAccess();

	/** Get sign legend */
	String getLegend();

	/** Get beacon type description */
	String getBeaconType();

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

	/** Set the default font */
	void setDefaultFont(Font f);

	/** Get the default font */
	Font getDefaultFont();
}
