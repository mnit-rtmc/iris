/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.SignDetail;
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

	/** Check if DMS supports pixel service object */
	protected boolean supportsPixelService() {
		SignDetail sd = dms.getSignDetail();
		return (sd != null) && sd.getPixelServiceFlag();
	}

	/** Create a new DMS operation */
	public OpDMS(PriorityLevel p, DMSImpl d) {
		super(p, d);
		dms = d;
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			dms.requestConfigure();
		else
			dms.setConfigure(false);
		super.cleanup();
	}

	/** Check if DMS make contains a value.
	 *
	 * NOTE: value must be all lower-case */
	private boolean isMakeContaining(String value) {
		assert value.equals(value.toLowerCase());
		SignDetail sd = dms.getSignDetail();
		String make = (sd != null) ? sd.getSoftwareMake() : null;
		return (make != null) && make.toLowerCase().contains(value);
	}

	/** Check if DMS make is ADDCO */
	protected boolean isAddco() {
		return isMakeContaining("addco");
	}

	/** Check if DMS make is American Signal */
	protected boolean isAmericanSignal() {
		return isMakeContaining("american signal");
	}

	/** Check if DMS make is LEDSTAR */
	protected boolean isLedstar() {
		return isMakeContaining("ledstar");
	}

	/** Check if DMS make is Skyline */
	protected boolean isSkyline() {
		return isMakeContaining("skyline");
	}

	/** Check if DMS type is character matrix */
	protected boolean isCharMatrix() {
		SignDetail sd = dms.getSignDetail();
		return (sd != null) &&
			(sd.getDmsType() == DMSType.VMS_CHAR.ordinal());
	}
}
