/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2017  Minnesota Department of Transportation
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
 * Video encoder type.
 *
 * @author Douglas Lau
 */
public interface EncoderType extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "encoder_type";

	/** Set the HTTP path */
	void setHttpPath(String p);

	/** Get the HTTP path*/
	String getHttpPath();

	/** Set the RTSP path */
	void setRtspPath(String p);

	/** Get the RTSP path*/
	String getRtspPath();
}
