/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.PrecipitationType;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Controller operation to query weather conditions from an ORG-815.
 *
 * @author Douglas Lau
 */
public class OpQueryConditions extends OpOrg815 {

	/** Create a new operation to query conditions */
	public OpQueryConditions(WeatherSensorImpl ws) {
		super(PriorityLevel.DATA_30_SEC, ws);
	}

	/** Create the second phase of the operation */
	protected Phase<Org815Property> phaseTwo() {
		return new QueryConditions();
	}

	/** Phase to query the conditions */
	protected class QueryConditions extends Phase<Org815Property> {

		/** Query the conditions */
		protected Phase<Org815Property> poll(
			CommMessage<Org815Property> mess) throws IOException
		{
			long now = TimeSteward.currentTimeMillis();
			ConditionsProperty cond = new ConditionsProperty();
			mess.add(cond);
			mess.queryProps();
			sensor.setAirTempNotify(null);
			sensor.setWindSpeedNotify(null);
			sensor.setWindDirNotify(null);
			sensor.setVisibilityNotify(null);
			sensor.updateAccumulation(
				Math.round(cond.getAccumulation() * 1000), now);
			PrecipitationType pt = cond.getPrecipitationType();
			if(pt != null)
				sensor.setPrecipitationType(pt, now);
			sensor.setStampNotify(now);
			if(cond.shouldReset())
				return new ResetAccumulator();
			else
				return null;
		}
	}

	/** Phase to reset the precipitation accumulator */
	protected class ResetAccumulator extends Phase<Org815Property> {

		/** Reset the accumulator */
		protected Phase<Org815Property> poll(
			CommMessage<Org815Property> mess) throws IOException
		{
			ResetProperty reset = new ResetProperty();
			mess.add(reset);
			mess.storeProps();
			sensor.resetAccumulation();
			return null;
		}
	}
}
