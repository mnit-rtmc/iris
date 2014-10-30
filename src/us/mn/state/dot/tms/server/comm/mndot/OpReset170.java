/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Send a level-1 restart request to a 170 controller
 *
 * @author Douglas Lau
 */
public class OpReset170 extends Op170 {

	/** Create a new send level-1 restart operation */
	public OpReset170(ControllerImpl c) {
		super(PriorityLevel.DOWNLOAD, c);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<MndotProperty> phaseOne() {
		return new ResetDetectors();
	}

	/** Phase to reset the detectors */
	protected class ResetDetectors extends Phase<MndotProperty> {

		/** Reset the detectors */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			// Enabling the detector-reset pin can cause some
			// detector cards to have "chattering" volume, along
			// with "occupancy plateaus".  This can happen if there
			// is a comm error during the ClearDetectors phase.
			byte[] data = {Address.DETECTOR_RESET};
			MemoryProperty reset_mem = new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS - 1, data);
			mess.add(reset_mem);
			mess.storeProps();
			return new ClearDetectors();
		}
	}

	/** Phase to clear the detector reset */
	protected class ClearDetectors extends Phase<MndotProperty> {

		/** Clear the detector reset */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			byte[] data = new byte[1];
			MemoryProperty reset_mem = new MemoryProperty(
				Address.SPECIAL_FUNCTION_OUTPUTS - 1, data);
			mess.add(reset_mem);
			mess.storeProps();
			return new Level1Restart();
		}
	}

	/** Phase to restart the controller */
	protected class Level1Restart extends Phase<MndotProperty> {

		/** Restart the controller */
		protected Phase<MndotProperty> poll(CommMessage mess)
			throws IOException
		{
			mess.add(new Level1Property());
			mess.storeProps();
			return null;
		}
	}
}
