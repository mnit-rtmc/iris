/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
 * Copyright (C) 2016-2017  SRF Consulting Group
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
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;

/**
 * Operation to query the current message on a DMS.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public class OpQueryDMSMessage extends OpDMS {

	/** MULTI string for current buffer */
	private final ASN1String ms = new ASN1String(dmsMessageMultiString
		.node, DmsMessageMemoryType.currentBuffer.ordinal(), 1);

	/** Beacon setting for current buffer */
	private final ASN1Integer beacon = dmsMessageBeacon.makeInt(
		DmsMessageMemoryType.currentBuffer, 1);

	/** Message priority for current buffer */
	private final ASN1Enum<DmsMsgPriority> prior = new ASN1Enum<
		DmsMsgPriority>(DmsMsgPriority.class, dmsMessageRunTimePriority
		.node, DmsMessageMemoryType.currentBuffer.ordinal(), 1);

	/** Message status for current buffer */
	private final ASN1Enum<DmsMessageStatus> status = new ASN1Enum<
		DmsMessageStatus>(DmsMessageStatus.class, dmsMessageStatus.node,
		DmsMessageMemoryType.currentBuffer.ordinal(), 1);

	/** Message time remaining */
	private final ASN1Integer time = dmsMessageTimeRemaining.makeInt();

	/** Create a new DMS query status object */
	public OpQueryDMSMessage(DMSImpl d) {
		super(PriorityLevel.DEVICE_DATA, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryMessageSource();
	}

	/** Source table (memory type) or the currently displayed message */
	private final MessageIDCode source = new MessageIDCode(
		dmsMsgTableSource.node);

	/** Process the message table source from the sign controller */
	private Phase processMessageSource() {
		DmsMessageMemoryType mem_type = source.getMemoryType();
		if (mem_type != null) {
			/* We have to test isBlank before "valid", because some
			 * signs use 'undefined' source for blank messages. */
			if (mem_type.isBlank())
				return processMessageBlank();
			else if (mem_type.valid)
				return processMessageValid();
		}
		return processMessageInvalid();
	}

	/** Process a blank message source from the sign controller */
	private Phase processMessageBlank() {
		/* The sign is blank.  If IRIS thinks there is a message on it,
		 * that's wrong and needs to be updated. */
		if (!dms.isMsgBlank()) {
			/* Maybe the current msg just expired */
			boolean oper_expire = SignMessageHelper
				.isOperatorExpiring(dms.getMsgCurrent());
			SignMessage sm = dms.createMsgBlank();
			setMsgCurrent(sm);
			/* User msg just expired -- set it to blank */
			if (oper_expire)
				dms.setMsgUser(sm);
		}
		return null;
	}

	/** Process a valid message source from the sign controller */
	private Phase processMessageValid() {
		/* The sign is not blank.  If IRIS thinks it is blank, then
		 * we need to query the current message on the sign. */
		if (dms.isMsgBlank())
			return new QueryCurrentMessage();
		/* Compare the CRC of the message on the sign to the
		 * CRC of the message IRIS knows about */
		SignMessage sm = dms.getMsgCurrent();
		String multi = parseMulti(sm.getMulti());
		int crc = DmsMessageCRC.calculate(multi, sm.getBeaconEnabled(),
			0);
		if (crc != source.getCrc())
			return new QueryCurrentMessage();
		else {
			setMsgCurrent(sm);
			return null;
		}
	}

	/** Process an invalid message source from the sign controller */
	private Phase processMessageInvalid() {
		/* The source table is not valid.  This condition has been
		 * observed in old Skyline signs after being powered down for
		 * extended periods of time.  It can be cleared up by sending
		 * settings operation. */
		setErrorStatus("INVALID SOURCE: " + source);
		return null;
	}

	/** Phase to query the current message source */
	protected class QueryMessageSource extends Phase {

		/** Query the current message source */
		@SuppressWarnings("unchecked")
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
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ms);
			if (dms.getSupportsBeaconObject())
				mess.add(beacon);
			else
				beacon.setInteger(0);
			mess.add(prior);
			mess.add(status);
			mess.add(time);
			mess.queryProps();
			logQuery(ms);
			if (dms.getSupportsBeaconObject())
				logQuery(beacon);
			logQuery(prior);
			logQuery(status);
			logQuery(time);
			setMsgCurrent();
			return null;
		}
	}

	/** Set the current message on the sign */
	private void setMsgCurrent() {
		if (status.getEnum() == DmsMessageStatus.valid) {
			Integer d = parseDuration(time.getInteger());
			DmsMsgPriority rp = prior.getEnum();
			/* If it's null, IRIS didn't send it ... */
			if (null == rp)
				rp = DmsMsgPriority.OTHER_SYSTEM;
			setMsgCurrent(ms.getValue(), beacon.getInteger(), rp,d);
		} else
			setErrorStatus("INVALID STATUS: " + status);
	}

	/** Set the current message on the sign */
	private void setMsgCurrent(String multi, int be, DmsMsgPriority p,
		Integer duration)
	{
		int src = p.getSource();
		setMsgCurrent(dms.createMsg(multi, (be == 1), p, p, src, null,
			duration));
	}

	/** Set the current message on the sign */
	private void setMsgCurrent(SignMessage sm) {
		if (sm != null) {
			dms.setMsgCurrentNotify(sm);
			/* IRIS may have restarted -- recover user msg */
			if (sm.getSource() == SignMsgSource.operator.bit())
				dms.setMsgUser(sm);
		} else
			setErrorStatus("MSG RENDER FAILED");
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			dms.msgQueried();
		super.cleanup();
	}
}
