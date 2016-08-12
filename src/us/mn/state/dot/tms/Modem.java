/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
 * Copyright (C) 2015  SRF Consulting Group
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
 * A Modem represents an old-skool analog modem.
 *
 * @author Douglas Lau
 */
public interface Modem extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "modem";

	/** Set config string */
	void setConfig(String c);

	/** Get config string */
	String getConfig();

	/** Set the remote URI */
	void setUri(String u);

	/** Get the remote URI */
	String getUri();

	/** Set the connection timeout (milliseconds) */
	void setTimeout(int t);

	/** Get the connection timeout (milliseconds) */
	int getTimeout();

	/** Get the modem state (ordinal of ModemState) */
	int getState();
	
	/** Set the modem enabled boolean */
	void setEnabled(boolean b);
	
	/** Get the modem enabled boolean */
	boolean getEnabled();
}
