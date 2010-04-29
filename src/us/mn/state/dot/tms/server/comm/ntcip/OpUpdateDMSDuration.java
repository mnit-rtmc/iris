/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to update the duration of the current DMS message.
 *
 * @author Douglas Lau
 */
public class OpUpdateDMSDuration extends OpDMS {

	/** Sign message to update */
	protected final SignMessage message;

	/** Create a new DMS update duration operation */
	public OpUpdateDMSDuration(DMSImpl d, SignMessage m) {
		super(COMMAND, d);
		message = m;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new SetTimeRemaining();
	}

	/** Get the message duration */
	protected int getDuration() {
		return getDuration(message.getDuration());
	}

	/** Phase to set message time remaining */
	protected class SetTimeRemaining extends Phase {

		/** Set the message time remaining */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsMessageTimeRemaining remaining =
				new DmsMessageTimeRemaining(getDuration());
			mess.add(remaining);
			DMS_LOG.log(dms.getName() + ":= " + remaining);
			mess.setRequest();
			return null;
		}
	}
}
