/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to be performed on a dynamic message sign
 *
 * @author Douglas Lau
 */
abstract public class OpDMS extends OpNtcip {

	/** Special duration value for indefinite duration */
	static protected final int DURATION_INDEFINITE = 65535;

	/** Filter message duration (valid for NTCIP) */
	static protected int getDuration(Integer d) {
		if (d == null || d >= DURATION_INDEFINITE)
			return DURATION_INDEFINITE;
		else if (d < 0)
			return 0;
		else
			return d;
	}

	/** Parse an NTCIP duration value */
	static protected Integer parseDuration(int d) {
		if (d <= 0 || d >= DURATION_INDEFINITE)
			return null;
		else
			return d;
	}

	/** DMS to operate */
	protected final DMSImpl dms;

	/** Check if DMS supports beacon activation object */
	protected boolean supportsBeaconActivation() {
		SignDetail sd = dms.getSignDetail();
		return (sd != null) && sd.getBeaconActivationFlag();
	}

	/** Get flash beacon flag for a sign message */
	protected boolean getFlashBeacon(SignMessage sm) {
		return supportsBeaconActivation() && (sm != null)
		      ? sm.getFlashBeacon()
		      : false;
	}

	/** Check if DMS supports pixel service object */
	protected boolean supportsPixelService() {
		SignDetail sd = dms.getSignDetail();
		return (sd != null) && sd.getPixelServiceFlag();
	}

	/** Get pixel service flag for a sign message */
	protected boolean getPixelService(SignMessage sm) {
		return supportsPixelService() && (sm != null)
		      ? sm.getPixelService()
		      : false;
	}

	/** Create a new DMS operation */
	public OpDMS(PriorityLevel p, DMSImpl d) {
		super(p, d);
		dms = d;
	}

	/** DMS status */
	private final JSONObject status = new JSONObject();

	/** Put an object into DMS status */
	protected final void putStatus(String key, Object value) {
		try {
			status.putOnce(key, value);
		}
		catch (JSONException e) {
			logError("putStatus: " + e.getMessage() + ", " + key);
		}
	}

	/** Put FAULTS into sign status */
	protected void putFaults(Object value) {
		putStatus(DMS.FAULTS, value);
	}

	/** Put FAULTS into controller status */
	@Override
	protected void putCtrlFaults(String fault, String msg) {
		putFaults((fault != null) ? "controller" : null);
		super.putCtrlFaults(fault, msg);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			if (!status.isEmpty())
				dms.setStatusNotify(status.toString());
			dms.requestConfigure();
		} else
			dms.setConfigure(false);
		super.cleanup();
	}

	/** Check if DMS type is character matrix */
	protected boolean isCharMatrix() {
		SignDetail sd = dms.getSignDetail();
		return (sd != null) &&
			(sd.getDmsType() == DMSType.VMS_CHAR.ordinal());
	}
}
