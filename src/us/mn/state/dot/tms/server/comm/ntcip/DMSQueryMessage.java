/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to query the current message on a DMS
 *
 * @author Douglas Lau
 */
public class DMSQueryMessage extends DMSOperation {

	/** Create a new DMS query status object */
	public DMSQueryMessage(DMSImpl d) {
		super(DEVICE_DATA, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryMessageSource();
	}

	/** Source table (memory type) or the currently displayed message */
	protected final DmsMsgTableSource source = new DmsMsgTableSource();

	/** Process the message table source from the sign controller */
	protected Phase processMessageSource() {
		DMS_LOG.log(dms.getName() + ": " + source);
		SignMessage m = dms.getMessageCurrent();
		if(DmsMessageMemoryType.isBlank(source.getMemory())) {
			/* The sign is blank. If IRIS says there should
			 * be a message on the sign, that's wrong and
			 * needs to be updated */
			if(!SignMessageHelper.isBlank(m))
				setCurrentMessage("", null);
		} else {
			/* The sign is not blank. If IRIS says it
			 * should be blank, then we need to query the
			 * current message on the sign. */
			if(SignMessageHelper.isBlank(m))
				return new QueryCurrentMessage();
		}
		return null;
	}

	/** Set the current message on the sign */
	protected void setCurrentMessage(String multi, Integer duration) {
		try {
			// FIXME: this should be on SONAR thread
			SignMessage sm = dms.createMessage(multi,
				getPriority(multi), duration);
			dms.setMessageCurrent(sm, null);
		}
		catch(SonarException e) {
			e.printStackTrace();
		}
	}

	/** Get the message priority for a MULTI string */
	protected DMSMessagePriority getPriority(String multi) {
		MultiString ms = new MultiString(multi);
		if(ms.isBlank())
			return DMSMessagePriority.BLANK;
		else
			return DMSMessagePriority.OTHER_SYSTEM;
	}

	/** Phase to query the current message source */
	protected class QueryMessageSource extends Phase {

		/** Query the current message source */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(source);
			mess.getRequest();
			return processMessageSource();
		}
	}

	/** Phase to query the current message */
	protected class QueryCurrentMessage extends Phase {

		/** Query the current message */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageMultiString multi = new DmsMessageMultiString(
				DmsMessageMemoryType.CURRENT_BUFFER, 1);
			mess.add(multi);
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.CURRENT_BUFFER, 1);
			mess.add(status);
			DmsMessageTimeRemaining time =
				new DmsMessageTimeRemaining();
			mess.add(time);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + multi);
			DMS_LOG.log(dms.getName() + ": " + status);
			DMS_LOG.log(dms.getName() + ": " + time);
			if(status.isValid() && time.getInteger() > 0) {
				Integer d = parseDuration(time.getInteger());
				setCurrentMessage(multi.getValue(), d);
			}
			return null;
		}
	}
}
