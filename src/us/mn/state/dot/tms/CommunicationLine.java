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

import java.rmi.RemoteException;

/**
 * The CommunicationLine interface is the remote interface to the
 * CommunicationLineImpl class.
 *
 * @author Douglas Lau, Tim Johnson
 */
public interface CommunicationLine extends TMSObject {

	/** Get line number index */
	public int getIndex() throws RemoteException;

	/** Set text description */
	public void setDescription(String description) throws TMSException,
		RemoteException;

	/** Get text description */
	public String getDescription() throws RemoteException;

	/** Set serial port name */
	public void setPort(String port) throws TMSException,
		RemoteException;

	/** Get serial port name */
	public String getPort() throws RemoteException;

	/** Set the bit rate */
	public void setBitRate(int bitRate) throws TMSException,
		RemoteException;

	/** Get the bit rate */
	public int getBitRate() throws RemoteException;

	/** NTCIP Class B serial communication protocol */
	public int PROTO_NTCIP_B = 0;

	/** Mn/DOT 4-bit serial 170 communication protocol */
	public int PROTO_MNDOT_4 = 1;

	/** Mn/DOT 5-bit serial 170 communication protocol */
	public int PROTO_MNDOT_5 = 2;

	/** Wavetronix SmartSensor serial data protocol */
	public int PROTO_SMART_SENSOR = 3;

	/** 3M Canoga serial communication protocol */
	public int PROTO_CANOGA = 4;

	/** Vicon video matrix switch serial communication protocol */
	public int PROTO_VICON = 5;

	/** Pelco D camera control protocol */
	public int PROTO_PELCO = 6;

	/** NTCIP Class C communication protocol */
	public int PROTO_NTCIP_C = 7;

	/** Manchester camera control protocol */
	public int PROTO_MANCHESTER = 8;

	/** DMS Lite protocol */
	public int PROTO_DMSLITE = 9;

	/** CAWS protocol */
	public int PROTO_CAWS = 10;

	/** Protocol string constants */
	public String[] PROTOCOLS = {
		"NTCIP Class B",
		"Mn/DOT 170 (4-bit)",
		"Mn/DOT 170 (5-bit)",
		"SmartSensor",
		"Canoga",
		"Vicon",
		"Pelco D",
		"NTCIP Class C",
		"Manchester",
		"DMS Lite",	// Caltrans D10
		"CAWS",		// Caltrans D10
	};

	/** Set the communication protocol */
	public void setProtocol(short protocol) throws TMSException,
		RemoteException;

	/** Get the communication protocol */
	public short getProtocol() throws RemoteException;

	/** Set the polling timeout (milliseconds) */
	public void setTimeout(int timeout) throws TMSException,
		RemoteException;

	/** Get the polling timeout (milliseconds) */
	public int getTimeout() throws RemoteException;

	/** Get the communication port status */
	public String getStatus() throws RemoteException;

	/** Get the current line load */
	public float getLoad() throws RemoteException;

	/** Get a controller by drop */
	public Controller getController(short drop) throws RemoteException;

	/** Get the controllers defined for this communication line */
	public Controller[] getControllers() throws RemoteException;

	/** Get the circuits assigned to this communication line */
	public Circuit[] getCircuits() throws RemoteException;
}
