/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.File;
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.GateArmArrayHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.DevelCfg;

/**
 * The Gate Arm System contains static methods which act on the entire gate
 * arm system.
 *
 * @author Douglas Lau
 */
public class GateArmSystem {

	/** Don't instantiate */
	private GateArmSystem() { }

	/** Log a message to stderr */
	static private void logStderr(String msg) {
		System.err.println(TimeSteward.currentDateTimeString(true) +
			" Gate Arm System " + msg);
	}

	/** Path to configuration enable file.  This must not be a system
	 * attribute for security reasons. */
	static private final File CONFIG_ENABLE_FILE = new File(
		DevelCfg.get("gates.enable.file",
		 	"/var/lib/iris/gate_arm_enable"));

	/** Config delete flag.  This will only get set to false if
	 * CONFIG_ENABLE_FILE cannot be deleted.  The only way to enable the
	 * gate arm system after that happens is to restart the IRIS server. */
	static private boolean DELETE_FLAG = true;

	/** Is gate arm system enabled? */
	static private boolean ENABLED = false;

	/** Test whether gate arm system is enabled */
	static private boolean isEnabledFile() {
		return DELETE_FLAG &&
		       CONFIG_ENABLE_FILE.isFile() &&
		       CONFIG_ENABLE_FILE.canRead() &&
		       CONFIG_ENABLE_FILE.canWrite();
	}

	/** Enable or disable gate arm system */
	static private void setEnabled(boolean e) {
		ENABLED = e;
		Iterator<GateArmArray> it = GateArmArrayHelper.iterator();
		while (it.hasNext()) {
			GateArmArray g = it.next();
			if (g instanceof GateArmArrayImpl) {
				GateArmArrayImpl ga = (GateArmArrayImpl) g;
				ga.setSystemEnable(e);
			}
		}
	}

	/** Disable gate arm configuration */
	static public void disable(String name, String reason) {
		if (isEnabledFile())
			DELETE_FLAG = CONFIG_ENABLE_FILE.delete();
		if (ENABLED)
			setEnabled(false);
		String msg = reason + ": " + name;
		logStderr(CONFIG_ENABLE_FILE.toString() + " " + msg +
			" " + getAdvice());
		sendEmailAlert("Gate arm system disabled (" + msg + ")");
	}

	/** Get advice to log */
	static private String getAdvice() {
		return DELETE_FLAG ? "(touch to re-enable)"
		      : "(check permissions and restart to re-enable)";
	}

	/** Check if config is enabled (and enable if required) */
	static public boolean checkEnabled() {
		if (ENABLED)
			return true;
		if (isEnabledFile()) {
			setEnabled(true);
			return true;
		} else
			return false;
	}

	/** Check a device request for valid gate arm requests */
	static public DeviceRequest checkRequest(DeviceRequest dr) {
		// NOTE: always call checkEnabled
		boolean e = checkEnabled();
		switch (dr) {
		case QUERY_STATUS:
			// Allow querying status even when system disabled
			return dr;
		case SEND_SETTINGS:
		case RESET_DEVICE:
		case DISABLE_SYSTEM:
			return e ? dr : null;
		default:
			return null;
		}
	}

	/** Send an email alert */
	static public void sendEmailAlert(String msg) {
		EmailHandler.send(EventType.GATE_ARM_SYSTEM, "Gate Arm ALERT",
			msg);
	}

	/** Check all gate arm open interlocks for one road.
	 * @param r Road to check. */
	static public void checkInterlocks(Road r) {
		int d = openGateDirection(r);
		Iterator<GateArmArray> it = GateArmArrayHelper.iterator();
		while (it.hasNext()) {
			GateArmArray g = it.next();
			if (g instanceof GateArmArrayImpl) {
				GateArmArrayImpl ga = (GateArmArrayImpl) g;
				Road gr = ga.getRoad();
				if (gr == r)
					ga.setOpenDirection(d);
			}
		}
	}

	/** Get valid gate open direction for the specified road.  If gates are
	 * open in more than one direction, then no direction is valid.
	 * @param r Road to check.
	 * @return Ordinal of valid gate Direction; 0 for any, -1 for none. */
	static private int openGateDirection(Road r) {
		int d = 0;
		boolean found = false;
		Iterator<GateArmArray> it = GateArmArrayHelper.iterator();
		while (it.hasNext()) {
			GateArmArray g = it.next();
			if (g instanceof GateArmArrayImpl) {
				GateArmArrayImpl ga = (GateArmArrayImpl) g;
				if (ga.isPossiblyOpen()) {
					Road gr = ga.getRoad();
					if (gr == r) {
						int gd = ga.getRoadDir();
						if (found && d != gd)
							return -1;
						else {
							found = true;
							d = gd;
						}
					}
				}
			}
		}
		return d;
	}

	/** Update all gate arm array dependencies.
	 * NOTE: this does three linear scans of all gate arm arrays */
	static public void updateDependencies() {
		beginDependencies();
		checkDependencies();
		commitDependencies();
	}

	/** Begin dependency transaction for all gate arm arrays */
	static private void beginDependencies() {
		Iterator<GateArmArray> it = GateArmArrayHelper.iterator();
		while (it.hasNext()) {
			GateArmArray g = it.next();
			if (g instanceof GateArmArrayImpl) {
				GateArmArrayImpl ga = (GateArmArrayImpl) g;
				ga.beginDependencies();
			}
		}
	}

	/** Check dependencies for all gate arm arrays */
	static private void checkDependencies() {
		Iterator<GateArmArray> it = GateArmArrayHelper.iterator();
		while (it.hasNext()) {
			GateArmArray g = it.next();
			if (g instanceof GateArmArrayImpl) {
				GateArmArrayImpl ga = (GateArmArrayImpl) g;
				ga.checkDependencies();
			}
		}
	}

	/** Commit dependency transaction for all gate arm arrays */
	static private void commitDependencies() {
		Iterator<GateArmArray> it = GateArmArrayHelper.iterator();
		while (it.hasNext()) {
			GateArmArray g = it.next();
			if (g instanceof GateArmArrayImpl) {
				GateArmArrayImpl ga = (GateArmArrayImpl) g;
				ga.commitDependencies();
			}
		}
	}

	/** Check all gate arm arrays for a GeoLoc change.  This needs to be
	 * fast, but the current algorithm is a linear scan...
	 * @param loc GeoLoc to check.
	 * @param reason Reason for check. */
	static public void checkDisable(GeoLoc loc, String reason) {
		Iterator<GateArmArray> it = GateArmArrayHelper.iterator();
		while (it.hasNext()) {
			GateArmArray g = it.next();
			if (g instanceof GateArmArrayImpl) {
				GateArmArrayImpl ga = (GateArmArrayImpl) g;
				if (loc == ga.getGeoLoc()) {
					disable(loc.getName(), reason);
					break;
				}
			}
		}
	}
}
