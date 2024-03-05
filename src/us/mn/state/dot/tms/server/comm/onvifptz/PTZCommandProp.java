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
		cmds = new ArrayList<String[]>();
		user = u;
		pass = p;
	}

	/** Adds ptz command to callback */
	public void addPanTiltZoom(float p, float t, float z) {
		cmds.add(new String[] {
			"ptz",
			String.valueOf(p),
			String.valueOf(t),
			String.valueOf(z)
		});
	}

	/** Sets message to store preset { storepreset, token, name } */
	public void addStorePreset(int p) {
		cmds.add(new String[] {
			"storepreset",
			String.valueOf(p),
			"Preset" + p
		});
	}

	/** Sets message to recall preset */
	public void addRecallPreset(int p) {
		cmds.add(new String[] {
			"recallpreset",
			String.valueOf(p)
		});
	}

	/** Sets message to move the focus */
	public void addFocus(int f) {
		cmds.add(new String[] {
			"movefocus",
			String.valueOf(f)
		});
	}

	/** Adds iris command to callback */
	public void addIris(int i) {
		cmds.add(new String[] {
			"iris",
			String.valueOf(i)
		});
	}

	/** Adds wiper oneshot to callback */
	public void addWiperOneshot() {
		cmds.add(new String[] { "wiper" });
	}

	/** Adds reboot to callback */
	public void addReboot() {
		cmds.add(new String[] { "reboot" });
	}

	/** Sets message to auto focus */
	public void addAutoFocus(boolean on) {
		cmds.add(new String[] {
			"autofocus",
			on ? "Auto" : "Manual"
		});
	}

	/** Sets message to auto iris */
	public void addAutoIris(boolean on) {
		cmds.add(new String[] {
			"autoiris",
			on ? "Auto" : "Manual"
		});
	}

	/** Appends autoiris and autofocus to callback */
	public void addAutoIrisAndFocus() {
		cmds.add(new String[] { "autoiris", "Auto" });
		cmds.add(new String[] { "autofocus", "Auto" });
	}
}
