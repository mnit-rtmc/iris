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
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send a message to a DMS.
 *
 * @author Douglas Lau
 */
public class OpSendDMSMessage extends OpAddco {

	/** Sign message */
	private final SignMessage message;

	/** DMS message property */
	private final MessageProperty msg_prop;

	/** Create a new send DMS message operation */
	public OpSendDMSMessage(DMSImpl d, SignMessage sm) {
		super(PriorityLevel.COMMAND, d);
		message = sm;
		msg_prop = new MessageProperty(d, sm);
	}

	/** Operation equality test */
	@Override
	public boolean equals(Object o) {
		if (o instanceof OpSendDMSMessage) {
			OpSendDMSMessage op = (OpSendDMSMessage)o;
			return dms == op.dms && SignMessageHelper.isEquivalent(
			       message, op.message);
		} else
			return false;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<AddcoProperty> phaseTwo() {
		dms.setMessageNext(message);
		return new SendMsg();
	}

	/** Phase to send the message */
	private class SendMsg extends Phase<AddcoProperty> {

		/** Send the message */
		protected Phase<AddcoProperty> poll(
			CommMessage<AddcoProperty> mess) throws IOException
		{
			mess.add(msg_prop);
			mess.storeProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			dms.setMessageCurrent(message);
		dms.setMessageNext(null);
		super.cleanup();
	}
}
