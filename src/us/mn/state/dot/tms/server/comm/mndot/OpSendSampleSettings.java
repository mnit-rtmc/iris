/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Send sample settings to a 170 controller
 *
 * @author Douglas Lau
 */
public class OpSendSampleSettings extends Op170 {

	/** Create a new send sample settings operation */
	public OpSendSampleSettings(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}

	/** Create a new send sample settings operation */
	public OpSendSampleSettings(ControllerImpl c) {
		this(PriorityLevel.DOWNLOAD, c);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseOne() {
		return new SynchronizeClock();
	}

	/** Phase to synchronize the clock */
	protected class SynchronizeClock extends Phase<MndotProperty> {

		/** Synchronize the clock */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			mess.add(new SynchronizeProperty());
			mess.storeProps();
			return new QueryPromVersion();
		}
	}

	/** Set the controller firmware version */
	private void setVersion(int major, int minor) {
		String v = Integer.toString(major) + "." +
			Integer.toString(minor);
		controller.setVersion(v);
		if ((major < 4)
		 || (major == 4 && minor < 2)
		 || (major == 5 && minor < 4))
		{
			logError("BUGGY 170 firmware! (version " + v + ")");
		}
	}

	/** Phase to query the prom version */
	protected class QueryPromVersion extends Phase<MndotProperty> {

		/** Query the prom version */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			byte[] data = new byte[2];
			MemoryProperty ver_mem = new MemoryProperty(
				Address.PROM_VERSION, data);
			mess.add(ver_mem);
			mess.queryProps();
			setVersion(data[0], data[1]);
			return new QueueBitmap();
		}
	}

	/** Phase to set the queue detector bitmap */
	protected class QueueBitmap extends Phase<MndotProperty> {

		/** Set the queue detector bitmap */
		protected Phase<MndotProperty> poll(
			CommMessage<MndotProperty> mess) throws IOException
		{
			byte[] data = getQueueBitmap();
			MemoryProperty queue_mem = new MemoryProperty(
				Address.QUEUE_BITMAP, data);
			mess.add(queue_mem);
			mess.storeProps();
			return null;
		}
	}

	/** Get the queue detector bitmap */
	public byte[] getQueueBitmap() {
		byte[] bitmap = new byte[DETECTOR_INPUTS / 8];
		for (int inp = 0; inp < DETECTOR_INPUTS; inp++) {
			if (isQueueDetector(inp))
				bitmap[inp / 8] |= 1 << (inp % 8);
		}
		return bitmap;
	}

	/** Test if a detector input has a queue detector associated */
	private boolean isQueueDetector(int inp) {
		DetectorImpl d = controller.getDetectorAtPin(
			FIRST_DETECTOR_PIN + inp);
		return d != null && d.getLaneType() == LaneType.QUEUE.ordinal();
	}
}
