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
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;

/**
 * Send sample settings to a 170 controller
 *
 * @author Douglas Lau
 */
public class OpSendSampleSettings extends Op170 {

	/** Create a new send sample settings operation */
	public OpSendSampleSettings(ControllerImpl c) {
		super(DOWNLOAD, c);
	}

	/** Begin the send sample settings operation */
	public void begin() {
		phase = new SynchronizeClock();
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
			setErrorStatus("CABINET STYLE " + dips);
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
			return null;
		}
	}

	/** Get the queue detector bitmap */
	public byte[] getQueueBitmap() {
		byte[] bitmap = new byte[DETECTOR_INPUTS / 8];
		for(int inp = 0; inp < DETECTOR_INPUTS; inp++) {
			if(isQueueDetector(inp))
				bitmap[inp / 8] |= 1 << (inp % 8);
		}
		return bitmap;
	}

	/** Test if a detector input has a queue detector associated */
	protected boolean isQueueDetector(int inp) {
		DetectorImpl d = controller.getDetectorAtPin(
			FIRST_DETECTOR_PIN + inp);
		return d != null && d.getLaneType() == LaneType.QUEUE.ordinal();
	}
}
