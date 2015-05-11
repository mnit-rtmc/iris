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
package us.mn.state.dot.tms.server.comm.dr500;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Controller operation to send settings to a DR-500.
 *
 * @author Douglas Lau
 */
public class OpSendSensorSettings extends OpDR500 {

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(ControllerImpl c) {
		super(PriorityLevel.DOWNLOAD, c);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<DR500Property> phaseOne() {
		return new QuerySysInfo();
	}

	/** Phase to query the system information */
	protected class QuerySysInfo extends Phase<DR500Property> {

		/** Query the system information */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			SysInfoProperty si = new SysInfoProperty();
			mess.add(si);
			mess.queryProps();
			controller.setVersion(si.getVersion());
			return null;
		}
	}
}
