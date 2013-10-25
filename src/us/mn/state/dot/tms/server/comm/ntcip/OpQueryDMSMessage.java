/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to query the current message on a DMS.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSMessage extends OpDMS {

	/** Create a new DMS query status object */
	public OpQueryDMSMessage(DMSImpl d) {
		super(PriorityLevel.DEVICE_DATA, d);
	}

	/** Create the second phase of the operation */
	@Override protected Phase phaseTwo() {
		return new QueryMessageSource();
	}

	/** Source table (memory type) or the currently displayed message */
	private final DmsMsgTableSource source = new DmsMsgTableSource();

	/** Process the message table source from the sign controller */
	private Phase processMessageSource() {
		SignMessage sm = dms.getMessageCurrent();
		/* We have to test isBlank before isValid, because some
		 * signs use 'undefined' source for blank messages. */
		if(source.getMemoryType().isBlank()) {
			/* The sign is blank. If IRIS says there should
			 * be a message on the sign, that's wrong and
			 * needs to be updated */
			if(!SignMessageHelper.isBlank(sm))
				setCurrentMessage(dms.createBlankMessage());
		} else if(source.getMemoryType().isValid()) {
			/* The sign is not blank. If IRIS says it
			 * should be blank, then we need to query the
			 * current message on the sign. */
			if(SignMessageHelper.isBlank(sm))
				return new QueryCurrentMessage();
			/* Compare the CRC of the message on the sign to the
			 * CRC of the message IRIS knows about */
			int crc = DmsMessageCRC.calculate(sm.getMulti(), 0, 0);
			if(crc != source.getCrc())
				return new QueryCurrentMessage();
		} else {
			/* The source table is not valid.  What??! */
			logError("INVALID SOURCE");
			setErrorStatus(source.toString());
		}
		return null;
	}

	/** Phase to query the current message source */
	protected class QueryMessageSource extends Phase {

		/** Query the current message source */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(source);
			mess.queryProps();
			logQuery(source);
			return processMessageSource();
		}
	}

	/** Phase to query the current message */
	protected class QueryCurrentMessage extends Phase {

		/** Query the current message */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsMessageMultiString multi = new DmsMessageMultiString(
				DmsMessageMemoryType.Enum.currentBuffer, 1);
			DmsMessageRunTimePriority prior =
				new DmsMessageRunTimePriority(
				DmsMessageMemoryType.Enum.currentBuffer, 1);
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.currentBuffer, 1);
			DmsMessageTimeRemaining time =
				new DmsMessageTimeRemaining();
			mess.add(multi);
			mess.add(prior);
			mess.add(status);
			mess.add(time);
			mess.queryProps();
			logQuery(multi);
			logQuery(prior);
			logQuery(status);
			logQuery(time);
			if(status.isValid()) {
				Integer d = parseDuration(time.getInteger());
				setCurrentMessage(multi.getValue(),
					prior.getEnum(), d);
			} else {
				logError("INVALID STATUS");
				setErrorStatus(status.toString());
			}
			return null;
		}
	}

	/** Set the current message on the sign */
	private void setCurrentMessage(String multi, DMSMessagePriority p,
		Integer duration)
	{
		setCurrentMessage(dms.createMessage(multi, p, p, duration));
	}

	/** Set the current message on the sign */
	private void setCurrentMessage(SignMessage sm) {
		if(sm != null)
			dms.setMessageCurrent(sm, null);
	}
}
