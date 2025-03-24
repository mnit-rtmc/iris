/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.MeterPoller;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Send device settings to a 170 controller
 *
 * @author Douglas Lau
 */
public class OpSendDeviceSettings extends Op170Device {

	/** Set the controller firmware version */
	static private String formatVersion(int major, int minor) {
		return Integer.toString(major) + "." + Integer.toString(minor);
	}

	/** Check for buggy 170 firmware version */
	static private boolean isVersionBuggy(int major, int minor) {
		return (major < 4)
		    || (major == 4 && minor < 2)
		    || (major == 5 && minor < 4);
	}

	/** Create a new device settings operation */
	public OpSendDeviceSettings(DeviceImpl d) {
		super(PriorityLevel.SETTINGS, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseTwo() {
		return new QueryPromVersion();
	}

	/** Phase to query the prom version */
	private class QueryPromVersion extends Phase<MndotProperty> {

		/** Query the prom version */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			byte[] data = new byte[2];
			MemoryProperty ver_mem = new MemoryProperty(
				Address.PROM_VERSION, data);
			mess.add(ver_mem);
			mess.queryProps();
			String v = formatVersion(data[0], data[1]);
			controller.setVersionNotify(v);
			if (isVersionBuggy(data[0], data[1]))
				putCtrlFaults("prom", "BUGGY firmware");
			return new ResetWatchdogMonitor();
		}
	}

	/** Phase to reset the watchdog monitor */
	private class ResetWatchdogMonitor extends Phase<MndotProperty> {

		/** Reset the watchdog monitor */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			byte[] data = {Address.WATCHDOG_BITS};
			mess.add(new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS + 2, data));
			mess.storeProps();
			return new ClearWatchdogMonitor();
		}
	}

	/** Phase to clear the watchdog monitor */
	private class ClearWatchdogMonitor extends Phase<MndotProperty> {

		/** Clear the watchdog monitor */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			byte[] data = new byte[1];
			mess.add(new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS + 2, data));
			mess.storeProps();
			return new SetCommFail();
		}
	}

	/** Phase to set the comm fail time */
	private class SetCommFail extends Phase<MndotProperty> {

		/** Set the comm fail time */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			byte[] data = {MeterPoller.COMM_FAIL_THRESHOLD};
			mess.add(new MemoryProperty(Address.COMM_FAIL, data));
			mess.storeProps();
			return null;
		}
	}
}
