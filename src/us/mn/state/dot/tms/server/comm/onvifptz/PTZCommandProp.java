/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2023  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.onvifptz;

import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.text.DecimalFormat;

/**
 * PTZ command property.
 *
 * @author Ethan Beauclaire
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class PTZCommandProp extends OnvifProp {

	/** Create a new PTZ command property */
	public PTZCommandProp(String u, String p) {
		user = u;
		pass = p;
	}

	/** Snaps components x and y to the nearest eighth of a circle */
	private static double[] snapAngle(float x, float y) {
		double angle = Math.atan2(y, x);

		// circle divided into eighths
		double division = 2 * Math.PI / 8;

		double snappedAngle = Math.round(angle / division) * division;

		// create a deadzone outside the unit circle
		// otherwise, x or y can be greater than 1 even when originally <=1
		double length = Math.min(1, Math.sqrt(x * x + y * y));
		double snappedX = length * Math.cos(snappedAngle);
		double snappedY = length * Math.sin(snappedAngle);

		return new double[] { snappedX, snappedY };
	}

	/** Adds ptz command to callback */
	public void addPanTiltZoom(float p, float t, float z) {
		double[] snappedPanTilt = snapAngle(p, t);
		DecimalFormat df = new DecimalFormat("0.#");
		String roundedP = df.format(snappedPanTilt[0]);
		String roundedT = df.format(snappedPanTilt[1]);
		String roundedZ = df.format(z);

		log("Queueing PTZ: " + roundedP + ", " + roundedT + ", " + roundedZ);
		cmd = new String[] {
			"ptz",
			roundedP,
			roundedT,
			roundedZ,
		};
	}

	/** Sets message to store preset { storepreset, token, name } */
	public void addStorePreset(int p) {
		cmd = new String[] {
			"storepreset",
			"Preset" + p
		};
	}

	/** Sets message to recall preset */
	public void addRecallPreset(int p) {
		cmd = new String[] {
			"recallpreset",
			"Preset" + p
		};
	}

	/** Sets message to move the focus */
	public void addFocus(int f) {
		cmd = new String[] {
			"movefocus",
			String.valueOf(f)
		};
	}

	/** Adds iris command to callback */
	public void addIris(int i) {
		cmd = new String[] {
			"iris",
			String.valueOf(i)
		};
	}

	/** Adds wiper oneshot to callback */
	public void addWiperOneshot() {
		cmd = new String[] { "wiper" };
	}

	/** Adds reboot to callback */
	public void addReboot() {
		cmd = new String[] { "reboot" };
	}

	/** Sets message to auto focus */
	public void addAutoFocus(boolean on) {
		cmd = new String[] {
			"autofocus",
			on ? "Auto" : "Manual"
		};
	}

	/** Sets message to auto iris */
	public void addAutoIris(boolean on) {
		cmd = new String[] {
			"autoiris",
			on ? "Auto" : "Manual"
		};
	}

	/** Adds autoiris and autofocus to callback */
	public void addAutoIrisAndFocus() {
		cmd = new String[] { "autoirisfocus", "Auto" };
	}

	/** Adds initialize to callback, for bindings/tokens */
	public void addInitialize() {
		cmd = new String[] { "initialize" };
	}
}
