/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2019  Minnesota Department of Transportation
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

	/** Set the encoder make */
	void setMake(String m);

	/** Get the encoder make */
	String getMake();

	/** Set the encoder model */
	void setModel(String m);

	/** Get the encoder model */
	String getModel();

	/** Set the encoder config */
	void setConfig(String c);

	/** Get the encoder config */
	String getConfig();
}
