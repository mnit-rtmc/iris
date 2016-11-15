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
import us.mn.state.dot.tms.BitmapGraphic;
import static us.mn.state.dot.tms.DMSMessagePriority.*;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Operation to query the current message on an Addco DMS.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSMessage extends OpAddco {

	/** DMS message property */
	private final MessageProperty msg_prop;

	/** Create a new DMS query status object */
	public OpQueryDMSMessage(DMSImpl d) {
		super(PriorityLevel.DEVICE_DATA, d);
		msg_prop = new MessageProperty(d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<AddcoProperty> phaseTwo() {
		return new QueryMessage();
	}

	/** Phase to query the current message */
	private class QueryMessage extends Phase<AddcoProperty> {

		/** Query the current message */
		protected Phase<AddcoProperty> poll(
			CommMessage<AddcoProperty> mess) throws IOException
		{
			mess.add(msg_prop);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			setCurrentMessage();
		super.cleanup();
	}

	/** Set the current message on the sign */
	private void setCurrentMessage() {
		String multi = getMulti();
		if (!multi.equals(getMultiCurrent())) {
			BitmapGraphic[] bmaps = msg_prop.getBitmaps();
			setCurrentMessage(createSignMessage(multi, bmaps));
		}
	}

	/** Get multi string from message property */
	private String getMulti() {
		String multi = msg_prop.getMulti();
		// FIXME: should only strip non-default page times
		return new MultiString(multi).stripPageTime();
	}

	/** Get current multi string on sign */
	private String getMultiCurrent() {
		return dms.getMsgCurrent().getMulti();
	}

	/** Create a sign message for the sign */
	private SignMessage createSignMessage(String multi,
		BitmapGraphic[] bmaps)
	{
		if (bmaps.length == 0 || new MultiString(multi).isBlank())
			return dms.createMsgBlank();
		else {
			// NOTE: this is most commonly called when IRIS server
			//       is restarted.  Set run-time priority to PSA
			//       so that scheduled messages will take over.
			return dms.createMsgRendered(multi, false, bmaps,
				OTHER_SYSTEM, PSA, null);
		}
	}

	/** Set the current message on the sign */
	private void setCurrentMessage(SignMessage sm) {
		if (sm != null)
			dms.setMsgCurrent(sm);
		else
			setErrorStatus("MSG RENDER FAILED");
	}
}
