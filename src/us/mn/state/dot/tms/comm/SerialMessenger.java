/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import java.io.IOException;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

/**
 * A SerialMessenger is a class which can poll a field controller and get the
 * response using a serial (RS-232) line.
 *
 * @author Douglas Lau
 */
public class SerialMessenger extends Messenger {

	/** Serial port device name */
	protected final String device;

	/** Receive threshold (number of bytes) */
	protected final int threshold;

	/** Parity parameter */
	protected final int parity;

	/** Communications API serial port */
	protected SerialPort port;

	/** Baud rate */
	protected int baud = 9600;

	/** Receive timeout (ms) */
	protected int timeout = 750;

	/** Set the baud rate */
	public synchronized void setBaudRate(int b) throws IOException {
		baud = b;
		if(port != null)
			setPortParameters();
	}

	/** Set the receive timeout */
	public synchronized void setTimeout(int t) throws IOException {
		timeout = t;
		if(port != null)
			setPortParameters();
	}

	/** Create a new messenger */
	public SerialMessenger(String d, int t, int p) {
		device = d;
		threshold = t;
		parity = p;
	}

	/** Get the comm port identifier */
	protected CommPortIdentifier getPortIdentifier() throws IOException {
		try {
			return CommPortIdentifier.getPortIdentifier(device);
		}
		catch(NoSuchPortException e) {
			throw new IOException("UNABLE TO OPEN PORT");
		}
	}

	/** Open the serial port */
	protected SerialPort openPort(CommPortIdentifier port_id)
		throws IOException
	{
		try {
			return (SerialPort)port_id.open("Messenger:" + device,
				2000);
		}
		catch(PortInUseException e) {
			throw new IOException(e.getMessage());
		}
	}

	/** Open the serial port */
	public void open() throws IOException {
		port = openPort(getPortIdentifier());
		input = port.getInputStream();
		output = port.getOutputStream();
		try {
			setPortParameters();
		}
		catch(IOException e) {
			close();
			throw e;
		}
	}

	/** Try to set the port parameters for the serial line */
	protected void trySetPortParameters()
		throws UnsupportedCommOperationException
	{
		if(threshold > 0)
			port.enableReceiveThreshold(threshold);
		else
			port.disableReceiveThreshold();
		port.enableReceiveTimeout(timeout);
		port.setSerialPortParams(baud, SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1, parity);
	}

	/** Set the port parameters for the serial line */
	protected void setPortParameters() throws IOException {
		if(port != null) {
			try {
				trySetPortParameters();
			}
			catch(UnsupportedCommOperationException e) {
				throw new IOException(e.getMessage());
			}
		}
	}

	/** Close the serial port */
	public synchronized void close() {
		if(port != null)
			port.close();
		port = null;
		input = null;
		output = null;
	}
}
