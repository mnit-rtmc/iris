/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.DMSType.VMS_FULL;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.SignConfigImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query the configuration of a DMS.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSConfiguration extends OpAddco {

	/** Create a new DMS query configuration object */
	public OpQueryDMSConfiguration(DMSImpl d) {
		super(PriorityLevel.DOWNLOAD, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<AddcoProperty> phaseTwo() {
		return new QueryDimensions();
	}

	/** Phase to query the DMS dimensions */
	private class QueryDimensions extends Phase<AddcoProperty> {

		/** Query the DMS dimensions */
		protected Phase<AddcoProperty> poll(CommMessage mess)
			throws IOException
		{
			return null;
		}
	}

	/** Set sign config for addco brick signs */
	private void setSignConfig() {
		SignConfigImpl sc = SignConfigImpl.findOrCreate(
			VMS_FULL.ordinal(), false, "LED", "FRONT", "noLegend",
			"none", 1620, 500, 75, 75, 41, 44, 36, 8, 0, 0);
		if (sc != null)
			dms.setSignConfigNotify(sc);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		setSignConfig();
		dms.setConfigure(isSuccess());
		super.cleanup();
	}
}
