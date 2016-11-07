/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.org815;

import java.io.IOException;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Controller operation to query settings of an ORG-815.
 *
 * @author Douglas Lau
 */
public class OpQuerySettings extends OpOrg815 {

	/** Create a new operation to query settings */
	public OpQuerySettings(WeatherSensorImpl ws) {
		super(PriorityLevel.DOWNLOAD, ws);
	}

	/** Create the second phase of the operation */
	protected Phase<Org815Property> phaseTwo() {
		return new QueryVersion();
	}

	/** Phase to query the version */
	protected class QueryVersion extends Phase<Org815Property> {

		/** Query the version */
		protected Phase<Org815Property> poll(
			CommMessage<Org815Property> mess) throws IOException
		{
			VersionProperty version = new VersionProperty();
			mess.add(version);
			mess.queryProps();
			controller.setVersionNotify(version.toString());
			return null;
		}
	}
}
