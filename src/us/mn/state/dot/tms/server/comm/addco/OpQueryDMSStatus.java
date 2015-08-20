/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.addco;

import java.io.IOException;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This operation queries the status of a DMS.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSStatus extends OpAddco {

	/** Check if voltage of power supply is failed */
	static private boolean isVoltageFailed(float volts) {
		return volts < 4 || volts > 24;
	}

	/** Check if voltage of power supply is out of spec */
	static private boolean isVoltageOutOfSpec(float volts) {
		return volts < 8 || volts > 18;
	}

	/** Get power supply status */
	static private String getPowerSupplyStatus(float volts) {
		if (isVoltageFailed(volts))
			return "powerFail";
		else if (isVoltageOutOfSpec(volts))
			return "voltageOutOfSpec";
		else
			return "noError";
	}

	/** Information property */
	private final InfoProperty info = new InfoProperty();

	/** Create a new DMS query status object */
	public OpQueryDMSStatus(DMSImpl d) {
		super(PriorityLevel.DEVICE_DATA, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<AddcoProperty> phaseTwo() {
		return new QueryInfo();
	}

	/** Phase to query the information property */
	private class QueryInfo extends Phase<AddcoProperty> {

		/** Query the DMS information property */
		protected Phase<AddcoProperty> poll(CommMessage mess)
			throws IOException
		{
			mess.add(info);
			mess.queryProps();
			dms.setPowerStatus(new String[] {
				formatPowerStatus()
			});
			return null;
		}
	}

	/** Get the power status for the power supply */
	private String formatPowerStatus() {
		float volts = info.getVolts();
		StringBuilder sb = new StringBuilder();
		sb.append("#1,");
		sb.append("ledSupply,");	// 1203v2 dmsPowerType
		sb.append(getPowerSupplyStatus(volts));
		sb.append(',');
		sb.append(volts);
		sb.append(" volts");
		return sb.toString();
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			setMaintStatus(formatMaintStatus());
			setErrorStatus("");
		}
		super.cleanup();
	}

	/** Format the new maintenance status */
	private String formatMaintStatus() {
		if (isVoltageFailed(info.getVolts()))
			return "POWER";
		else
			return "";
	}
}
