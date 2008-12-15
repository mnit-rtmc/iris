/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
 * The Font interface defines all the attributes of a DMS pixel font.  These
 * fonts are used for VMS messages, and are downloaded to NTCIP sign
 * controllers.
 *
 * @author Douglas Lau
 */
public interface Font extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "font";

	/** Set the font number */
	void setNumber(int n);

	/** Get the font number */
	int getNumber();

	/** Set the font height (pixels) */
	void setHeight(int h);

	/** Get the font height (pixels) */
	int getHeight();

	/** Set the font width (pixels; 0 for proportional) */
	void setWidth(int w);

	/** Get the font width (pixels; 0 for proportional) */
	int getWidth();

	/** Set the default vertical spacing between lines (pixels) */
	void setLineSpacing(int s);

	/** Get the default vertical spacing between lines (pixels) */
	int getLineSpacing();

	/** Set the default horizontal spacing between characters (pixels) */
	void setCharSpacing(int s);

	/** Get the default horizontal spacing between characters (pixels) */
	int getCharSpacing();

	/** Set the font version ID (NTCIP function) */
	void setVersionID(int v);

	/** Get the font version ID (NTCIP function) */
	int getVersionID();
}
