/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
 * A CommLink is a network connection for device communication.
 *
 * @author Douglas Lau
 */
public interface CommLink extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "comm_link";

	/** NTCIP Class B protocol */
	int PROTO_NTCIP_B = 0;

	/** Mn/DOT 170 4-bit protocol */
	int PROTO_MNDOT_4 = 1;

	/** Mn/DOT 170 5-bit protocol */
	int PROTO_MNDOT_5 = 2;

	/** Wavetronix SmartSensor data protocol */
	int PROTO_SMART_SENSOR = 3;

	/** 3M Canoga data protocol */
	int PROTO_CANOGA = 4;

	/** Vicon video matrix switch protocol */
	int PROTO_VICON = 5;

	/** Pelco D camera control protocol */
	int PROTO_PELCO_D = 6;

	/** NTCIP Class C protocol */
	int PROTO_NTCIP_C = 7;

	/** Manchester camera control protocol */
	int PROTO_MANCHESTER = 8;

	/** DMS Lite protocol */
	int PROTO_DMSLITE = 9;

	/** AWS protocol */
	int PROTO_AWS = 10;

	/** NTCIP Class A protocol */
	int PROTO_NTCIP_A = 11;

	/** Pelco video matrix switch protocol */
	int PROTO_PELCO = 12;

	/** Vicon camera control protocol */
	int PROTO_VICON_PTZ = 13;

	/** Protocol string constants */
	String[] PROTOCOLS = {
		"NTCIP Class B",
		"Mn/DOT 170 (4-bit)",
		"Mn/DOT 170 (5-bit)",
		"SmartSensor",
		"Canoga",
		"Vicon",
		"Pelco D",
		"NTCIP Class C",
		"Manchester",
		"DMS Lite",
		"AWS",
		"NTCIP Class A",
		"Pelco video switch",
		"Vicon PTZ"
	};

	/** Set text description */
	void setDescription(String d);

	/** Get text description */
	String getDescription();

	/** Set the remote URL */
	void setUrl(String u);

	/** Get the remote URL */
	String getUrl();

	/** Set the communication protocol */
	void setProtocol(short p);

	/** Get the communication protocol */
	short getProtocol();

	/** Set the polling timeout (milliseconds) */
	void setTimeout(int t);

	/** Get the polling timeout (milliseconds) */
	int getTimeout();

	/** Get the communication port status */
	String getStatus();

	/** Get the current line load */
	float getLoad();
}
