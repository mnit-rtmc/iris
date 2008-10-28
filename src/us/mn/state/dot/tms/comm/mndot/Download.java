/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.mndot;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorImpl;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterImpl;
import us.mn.state.dot.tms.StratifiedPlanImpl;
import us.mn.state.dot.tms.SystemPolicy;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.TMSObjectImpl;
import us.mn.state.dot.tms.WarningSignImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.comm.DownloadRequestException;
import us.mn.state.dot.tms.comm.MeterPoller;

/**
 * Download configuration data to a 170 controller
 *
 * @author Douglas Lau
 */
public class Download extends Controller170Operation implements TimingTable {

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
		controller.setError("OK");
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
			controller.setError("CABINET STYLE " + dips);
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
			return new ResetWatchdogMonitor();
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
			return new ResetWatchdogMonitor();
		}
	}

	/** Phase to reset the watchdog monitor */
	protected class ResetWatchdogMonitor extends Phase {

		/** Reset the watchdog monitor */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = {Address.WATCHDOG_RESET};
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS + 2, data));
			mess.setRequest();
			return new ClearWatchdogMonitor();
		}
	}

	/** Phase to clear the watchdog monitor */
	protected class ClearWatchdogMonitor extends Phase {

		/** Clear the watchdog monitor */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = new byte[1];
			mess.add(new MemoryRequest(
				Address.SPECIAL_FUNCTION_OUTPUTS + 2, data));
			mess.setRequest();
			return new SetCommFail();
		}
	}

	/** Phase to set the comm fail time */
	protected class SetCommFail extends Phase {

		/** Set the comm fail time */
		protected Phase poll(AddressedMessage mess) throws IOException {
			byte[] data = {MeterPoller.COMM_FAIL_THRESHOLD};
			mess.add(new MemoryRequest(Address.COMM_FAIL, data));
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
			if(meter1 != null) {
				sendTimingTables(mess,
					Address.METER_1_TIMING_TABLE, meter1);
				return new ClearVerifies1();
			}
			WarningSignImpl warn =
				controller.getActiveWarningSign();
			if(warn != null) {
				sendWarningSignTiming(mess,
					Address.METER_1_TIMING_TABLE);
			}
			return new SetTimingTable2();
		}
	}

	/** Phase to clear the meter verifies for the first ramp meter */
	protected class ClearVerifies1 extends Phase {

		/** Clear the meter verifies for the first ramp meter */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int address = Address.RAMP_METER_DATA +
				Address.OFF_POLICE_PANEL;
			mess.add(new MemoryRequest(address, new byte[1]));
			mess.setRequest();
			return new SetTimingTable2();
		}
	}

	/** Phase to set the timing table for the second ramp meter */
	protected class SetTimingTable2 extends Phase {

		/** Set the timing table for the second ramp meter */
		protected Phase poll(AddressedMessage mess) throws IOException {
			if(meter2 != null) {
				sendTimingTables(mess,
					Address.METER_2_TIMING_TABLE, meter2);
				return new ClearVerifies2();
			} else
				return null;
		}
	}

	/** Phase to clear the meter verifies for the second ramp meter */
	protected class ClearVerifies2 extends Phase {

		/** Clear the meter verifies for the second ramp meter */
		protected Phase poll(AddressedMessage mess) throws IOException {
			int address = Address.RAMP_METER_DATA +
				Address.OFF_POLICE_PANEL + Address.OFF_METER_2;
			mess.add(new MemoryRequest(address, new byte[1]));
			mess.setRequest();
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(!success)
			controller.setError(null);
		super.cleanup();
	}

	/** Send both AM and PM timing tables to the specified ramp meter */
	protected void sendTimingTables(AddressedMessage mess, int address,
		RampMeterImpl meter) throws IOException
	{
		int[] red = {1, 1};
		int[] rate = {MeterRate.FLASH, MeterRate.FLASH};
		int[] start = {AM_START_TIME, PM_START_TIME};
		int[] stop = {AM_START_TIME, PM_START_TIME};
		updateTimingTables(meter, red, rate, start, stop);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BCD.OutputStream bcd = new BCD.OutputStream(os);
		for(int t = TimingPlan.AM; t <= TimingPlan.PM; t++) {
			bcd.write16Bit(STARTUP_GREEN);
			bcd.write16Bit(STARTUP_YELLOW);
			bcd.write16Bit(TMSObjectImpl.getPolicyValue(
				SystemPolicy.METER_GREEN_TIME));
			bcd.write16Bit(TMSObjectImpl.getPolicyValue(
				SystemPolicy.METER_YELLOW_TIME));
			bcd.write16Bit(HOV_PREEMPT);
			for(int i = 0; i < 6; i++)
				bcd.write16Bit(red[t]);
			bcd.write8Bit(rate[t]);
			bcd.write16Bit(start[t]);
			bcd.write16Bit(stop[t]);
		}
		mess.add(new MemoryRequest(address, os.toByteArray()));
		mess.setRequest();
	}

	/** Send timing table for a warning sign */
	protected void sendWarningSignTiming(AddressedMessage mess, int address)
		throws IOException
	{
		int[] times = {AM_START_TIME, PM_START_TIME};
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BCD.OutputStream bcd = new BCD.OutputStream(os);
		for(int t = TimingPlan.AM; t <= TimingPlan.PM; t++) {
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

	/** Update one timing table with a stratified plan */
	protected void updateTable(RampMeterImpl meter, StratifiedPlanImpl p,
		int[] red, int[] rate, int[] start, int[] stop)
	{
		for(int t = TimingPlan.AM; t <= TimingPlan.PM; t++) {
			if(p.checkPeriod(t)) {
				int sta = p.getStartTime();
				int sto = p.getStopTime();
				red[t] = meter.calculateRedTime(
					meter.getTarget(sta));
				rate[t] = MeterRate.TOD;
				start[t] = 100 * (sta / 60) + sta % 60;
				stop[t] = 100 * (sto / 60) + sto % 60;
			}
		}
	}

	/** Update the timing tables with active timing plans */
	protected void updateTimingTables(RampMeterImpl meter, int[] red,
		int[] rate, int[] start, int[] stop)
	{
		if(meter.getControlMode() != RampMeter.MODE_CENTRAL)
			return;
		TimingPlan[] plans = meter.getTimingPlans();
		for(int i = 0; i < plans.length; i++) {
			if(plans[i] instanceof StratifiedPlanImpl) {
				StratifiedPlanImpl p =
					(StratifiedPlanImpl)plans[i];
				updateTable(meter, p, red, rate, start, stop);
			}
		}
	}
}
