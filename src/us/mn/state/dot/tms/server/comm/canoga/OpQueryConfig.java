/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.canoga;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query the configuration of a Canoga card
 *
 * @author Douglas Lau
 */
public class OpQueryConfig extends OpCanoga {

	/** Canoga card serial number */
	private final SerialNumberProperty serial_number =
		new SerialNumberProperty();

	/** Canoga firmware version */
	private final VersionProperty version = new VersionProperty();

	/** Create an operation to query the Canoga configuration */
	public OpQueryConfig(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}

	/** Create an operation to query the Canoga configuration */
	public OpQueryConfig(ControllerImpl c) {
		this(PriorityLevel.DOWNLOAD, c);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<CanogaProperty> phaseOne() {
		return new QuerySerialNumber();
	}

	/** Phase to query the serial number */
	protected class QuerySerialNumber extends Phase<CanogaProperty> {

		/** Query the serial number */
		protected Phase<CanogaProperty> poll(
			CommMessage<CanogaProperty> mess) throws IOException
		{
			mess.add(serial_number);
			mess.queryProps();
			return new QueryVersion();
		}
	}

	/** Phase to query the firmware version */
	protected class QueryVersion extends Phase<CanogaProperty> {

		/** Query the firmware version */
		protected Phase<CanogaProperty> poll(
			CommMessage<CanogaProperty> mess) throws IOException
		{
			mess.add(version);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			controller.setVersion(version.getValue() + " (" +
				serial_number.getValue() + ")");
		}
		super.cleanup();
	}
}
