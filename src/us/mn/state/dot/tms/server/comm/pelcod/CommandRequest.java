/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcod;

/**
 * A request to command a camera
 *
 * @author Douglas Lau
 */
public class CommandRequest extends Request {

	/** Bit flag to command a pan right */
	static protected final byte PAN_RIGHT = 1 << 1;

	/** Bit flag to command a pan left */
	static protected final byte PAN_LEFT = 1 << 2;

	/** Bit flag to command a tilt up */
	static protected final byte TILT_UP = 1 << 3;

	/** Bit flag to command a tilt down */
	static protected final byte TILT_DOWN = 1 << 4;

	/** Bit flag to command a zoom in */
	static protected final byte ZOOM_IN = 1 << 5;

	/** Bit flag to command a zoom out */
	static protected final byte ZOOM_OUT = 1 << 6;

	/** Requested pan value (-63 to 63) (64 means turbo) */
	protected final int pan;

	/** Requested tilt value (-63 to 63) */
	protected final int tilt;

	/** Requested zoom value (-1 to 1) */
	protected final int zoom;

	/** Create a new command request */
	public CommandRequest(int p, int t, int z) {
		pan = p;
		tilt = t;
		zoom = z;
	}

	/** Get bit flags to control panning */
	protected byte getPanFlags() {
		if(pan < 0)
			return PAN_LEFT;
		else if(pan > 0)
			return PAN_RIGHT;
		else
			return 0;
	}

	/** Get bit flags to control tilting */
	protected byte getTiltFlags() {
		if(tilt < 0)
			return TILT_DOWN;
		else if(tilt > 0)
			return TILT_UP;
		else
			return 0;
	}

	/** Get bit flags to control zooming */
	protected byte getZoomFlags() {
		if(zoom < 0)
			return ZOOM_OUT;
		else if(zoom > 0)
			return ZOOM_IN;
		else
			return 0;
	}

	/** Get the bit flags to control pan/tilt/zoom functions */
	protected byte getPTZFlags() {
		return (byte)(getPanFlags() | getTiltFlags() | getZoomFlags());
	}

	/** Calculate the checksum of a message */
	protected byte calculateChecksum(byte[] message) {
		int i;
		byte checksum = 0;
		for(i = 1; i < 6; i++)
			checksum += message[i];
		return checksum;
	}

	/** Format the request for the specified receiver address */
	public byte[] format(int drop) {
		byte[] message = new byte[7];
		message[0] = (byte)0xFF;
		message[1] = (byte)drop;
		message[2] = 0;
		message[3] = getPTZFlags();
		message[4] = (byte)Math.abs(pan);
		message[5] = (byte)Math.abs(tilt);
		message[6] = calculateChecksum(message);
		return message;
	}
}
