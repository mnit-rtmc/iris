/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.WarningSignImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DownloadRequestException;

/**
 * Download configuration data to a 170 controller
 *
 * @author Douglas Lau
 */
public class Download extends Controller170Operation {

	/** HOV preempt time (tenths of a second) (obsolete) */
	static protected final int HOV_PREEMPT = 80;

	/** AM midpoint time (BCD; minute of day) */
	static protected final int AM_MID_TIME = 730;

	/** PM midpoint time (BCD; minute of day) */
	static protected final int PM_MID_TIME = 1630;

	/** Flag to perform a level-1 restart */
	protected final boolean restart;

	/** Create a new download operation */
	public Download(ControllerImpl c) {
		this(c, false);
	}

	/** Create a new download operation */
	public Download(ControllerImpl c, boolean r) {
		super(DOWNLOAD, c);
		restart = r;
	}

	/** Handle an exception */
	public void handleException(IOException e) {
		if(e instanceof DownloadRequestException)
			return;
		else
			super.handleException(e);
	}

	/** Begin the download operation */
	public void begin() {
		if(restart)
			phase = new Level1Restart();
		else
			phase = new SynchronizeClock();
	}

	/** Phase to restart the controller */
	protected class Level1Restart extends Phase {

		/** Restart the controller */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new Level1Request());
			mess.setRequest();
			return new SynchronizeClock();
		}
	}

	/** Phase to synchronize the clock */
	protected class SynchronizeClock extends Phase {

		/** Synchronize the clock */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new SynchronizeRequest());
			mess.setRequest();
			return new CheckCabinetType();
		}
	}

	/** Phase to check the cabinet type */
	protected class CheckCabinetType extends Phase {

		/** Check the cabinet type */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = new byte[1];
			mess.add(new MemoryRequest(Address.CABINET_TYPE, data));
			mess.getRequest();
			checkCabinetStyle(data[0]);
			return new QueryPromVersion();
		}
	}

	/** Check the dip switch settings against the selected cabinet style */
	protected void checkCabinetStyle(int dips) {
		Integer d = lookupDips();
		if(d != null && d != dips)
			errorStatus = "CABINET STYLE " + dips;
	}

	/** Lookup the correct dip switch setting to the controller */
	protected Integer lookupDips() {
		Cabinet cab = controller.getCabinet();
		if(cab != null) {
			CabinetStyle style = cab.getStyle();
			if(style != null)
				return style.getDip();
		}
		return null;
	}

	/** Set the controller firmware version */
	protected void setVersion(int major, int minor) {
		String v = Integer.toString(major) + "." +
			Integer.toString(minor);
		controller.setVersion(v);
		if(major < 4 || (major == 4 && minor < 2) ||
			(major == 5 && minor < 4))
		{
			System.err.println("BUGGY 170 firmware! (version " +
				v + ") at " + controller.toString());
		}
	}

	/** Phase to query the prom version */
	protected class QueryPromVersion extends Phase {

		/** Query the prom version */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = new byte[2];
			mess.add(new MemoryRequest(Address.PROM_VERSION, data));
			mess.getRequest();
			setVersion(data[0], data[1]);
			if(data[0] > 4 || data[1] > 0)
				return new ResetDetectors();
			else
				return new QueueBitmap();
		}
	}

	/** Phase to reset the detectors */
	protected class ResetDetectors extends Phase {

		/** Reset the detectors */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = {Address.DETECTOR_RESET};
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS - 1, data));
			mess.setRequest();
			return new ClearDetectors();
		}
	}

	/** Phase to clear the detector reset */
	protected class ClearDetectors extends Phase {

		/** Clear the detector reset */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = new byte[1];
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS - 1, data));
			mess.setRequest();
			return new QueueBitmap();
		}
	}

	/** Phase to set the queue detector bitmap */
	protected class QueueBitmap extends Phase {

		/** Set the queue detector bitmap */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = getQueueBitmap();
			mess.add(new MemoryRequest(Address.QUEUE_BITMAP, data));
			mess.setRequest();
			return new SetTimingTable1();
		}
	}

	/** Get the queue detector bitmap */
	public byte[] getQueueBitmap() {
		byte[] bitmap = new byte[DETECTOR_INPUTS / 8];
		for(int inp = 0; inp < DETECTOR_INPUTS; inp++) {
			DetectorImpl det = controller.getDetectorAtPin(
				FIRST_DETECTOR_PIN + inp);
			if(det != null &&
				det.getLaneType() == LaneType.QUEUE.ordinal())
			{
				bitmap[inp / 8] |= 1 << (inp % 8);
			}
		}
		return bitmap;
	}

	/** Phase to set the timing table for the first ramp meter */
	protected class SetTimingTable1 extends Phase {

		/** Set the timing table for the first ramp meter */
		protected Phase poll(AddressedMessage mess) throws IOException {
			WarningSignImpl warn =
				controller.getActiveWarningSign();
			if(warn != null) {
				sendWarningSignTiming(mess,
					Address.METER_1_TIMING_TABLE);
			}
			return null;
		}
	}

	/** Send timing table for a warning sign */
	protected void sendWarningSignTiming(AddressedMessage mess, int address)
		throws IOException
	{
		int[] times = {AM_MID_TIME, PM_MID_TIME};
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BCD.OutputStream bcd = new BCD.OutputStream(os);
		for(int t = Calendar.AM; t <= Calendar.PM; t++) {
			bcd.write16Bit(1);		// Startup GREEN
			bcd.write16Bit(1);		// Startup YELLOW
			bcd.write16Bit(3);		// Metering GREEN
			bcd.write16Bit(1);		// Metering YELLOW
			bcd.write16Bit(HOV_PREEMPT);
			for(int i = 0; i < 6; i++)
				bcd.write16Bit(1);	// Metering RED
			bcd.write8Bit(MeterRate.FLASH);	// TOD rate
			bcd.write16Bit(times[t]);	// TOD start time
			bcd.write16Bit(times[t]);	// TOD stop time
		}
		mess.add(new MemoryRequest(address, os.toByteArray()));
		mess.setRequest();
	}
}
