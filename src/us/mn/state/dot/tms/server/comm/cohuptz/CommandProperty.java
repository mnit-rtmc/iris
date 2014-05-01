/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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

package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A property to command a camera
 *
 * @author Travis Swanston
 */
public class CommandProperty extends CohuPTZProperty {

	/** Requested pan value [-1..1] */
	protected final float pan;

	/** Requested tilt value [-1..1] */
	protected final float tilt;

	/** Requested zoom value [-1..1] */
	protected final float zoom;

	/** absolute value of PTZ movement threshold */
	static protected final float PTZ_THRESH = 0.001F;

	/** Create a new command property */
	public CommandProperty(float p, float t, float z) {
		pan = p;
		tilt = t;
		zoom = z;
	}

	/**
	 * Encode a STORE request
	 *
	 * TODO: In order to handle stops correctly, we are issuing pan, tilt,
	 * and zoom commands on every operation.  Consider how this could be
	 * designed better.
	 *
	 * NOTE: Cohu PTZ supports variable-speed zoom, but we have chosen not
	 * to implement it at this time.
	 */
	@Override
	public void encodeStore(OutputStream os, int drop) throws IOException {

		byte[] panCmd  = new byte[2];
		byte[] tiltCmd = new byte[2];
		byte[] zoomCmd = new byte[2];

		if (Math.abs(pan) < PTZ_THRESH) {
			panCmd[0] = (byte)0x50;
			panCmd[1] = (byte)0x53;
		}
		else if (pan < 0) {
			panCmd[0] = (byte)0x6c;
			panCmd[1] = getPTSpeedByte(pan);
		}
		else if (pan > 0) {
			panCmd[0] = (byte)0x72;
			panCmd[1] = getPTSpeedByte(pan);
		}

		if (Math.abs(tilt) < PTZ_THRESH) {
			tiltCmd[0] = (byte)0x54;
			tiltCmd[1] = (byte)0x53;
		}
		else if (tilt < 0) {
			tiltCmd[0] = (byte)0x64;
			tiltCmd[1] = getPTSpeedByte(tilt);
		}
		else if (tilt > 0) {
			tiltCmd[0] = (byte)0x75;
			tiltCmd[1] = getPTSpeedByte(tilt);
		}

		if (Math.abs(zoom) < PTZ_THRESH) {
			zoomCmd[0] = (byte)0x5a;
			zoomCmd[1] = (byte)0x53;
		}
		else if (zoom < 0) {
			zoomCmd[0] = (byte)0x5a;
			zoomCmd[1] = (byte)0x4f;
		}
		else if (zoom > 0) {
			zoomCmd[0] = (byte)0x5a;
			zoomCmd[1] = (byte)0x49;
		}

		byte[] panMsg = new byte[5];
		panMsg[0] = (byte)0xf8;
		panMsg[1] = (byte)drop;
		panMsg[2] = panCmd[0];
		panMsg[3] = panCmd[1];
		panMsg[4] = calculateChecksum(panMsg,4);

		byte[] tiltMsg = new byte[5];
		tiltMsg[0] = (byte)0xf8;
		tiltMsg[1] = (byte)drop;
		tiltMsg[2] = tiltCmd[0];
		tiltMsg[3] = tiltCmd[1];
		tiltMsg[4] = calculateChecksum(tiltMsg,4);

		byte[] zoomMsg = new byte[5];
		zoomMsg[0] = (byte)0xf8;
		zoomMsg[1] = (byte)drop;
		zoomMsg[2] = zoomCmd[0];
		zoomMsg[3] = zoomCmd[1];
		zoomMsg[4] = calculateChecksum(zoomMsg,4);

		os.write(panMsg);
		os.write(tiltMsg);
		os.write(zoomMsg);
	}

}
