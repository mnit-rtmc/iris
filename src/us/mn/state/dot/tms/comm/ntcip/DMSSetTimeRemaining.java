/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * Operation to set the time remaining for the current DMS message
 *
 * @author Douglas Lau
 */
public class DMSSetTimeRemaining extends DMSOperation {

	/** Sign message to update */
	protected final SignMessageImpl message;

	/** User who deployed the message */
	protected final User owner;

	/** Create a new DMS set time remaining operation */
	public DMSSetTimeRemaining(DMSImpl d, SignMessage m, User o) {
		super(COMMAND, d);
		message = (SignMessageImpl)m;
		owner = o;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new SetTimeRemaining();
	}

	/** Get the message duration */
	protected int getDuration() {
		if(message.isBlank())
			return 0;
		else
			return getDuration(message.getDuration());
	}

	/** Phase to set message time remaining */
	protected class SetTimeRemaining extends Phase {

		/** Set the message time remaining */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageTimeRemaining remaining =
				new DmsMessageTimeRemaining(getDuration());
			mess.add(remaining);
			mess.setRequest();
			// FIXME: this should happen on SONAR thread
			dms.setMessageCurrent(message, owner);
			return null;
		}
	}
}
