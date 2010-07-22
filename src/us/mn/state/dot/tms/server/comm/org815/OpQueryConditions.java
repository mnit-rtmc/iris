/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Controller operation to query weather conditions from an ORG-815.
 *
 * @author Douglas Lau
 */
public class OpQueryConditions extends OpOrg815 {

	/** Create a new operation to query conditions */
	public OpQueryConditions(ControllerImpl c) {
		super(PriorityLevel.DATA_30_SEC, c);
	}

	/** Begin the query conditions operation */
	public boolean begin() {
		phase = new QueryConditions();
		return true;
	}

	/** Phase to query the conditions */
	protected class QueryConditions extends Phase {

		/** Query the conditions */
		protected Phase poll(CommMessage mess) throws IOException {
			ConditionsProperty cond = new ConditionsProperty();
			mess.add(cond);
			mess.queryProps();
			ORG815_LOG.log(controller.getName() + ": " + cond);
			if(cond.shouldReset())
				return new ResetAccumulator();
			else
				return null;
		}
	}

	/** Phase to reset the precipitation accumulator */
	protected class ResetAccumulator extends Phase {

		/** Reset the accumulator */
		protected Phase poll(CommMessage mess) throws IOException {
			ResetProperty reset = new ResetProperty();
			mess.add(reset);
			ORG815_LOG.log(controller.getName() + ": " + reset);
			mess.storeProps();
			return null;
		}
	}
}
