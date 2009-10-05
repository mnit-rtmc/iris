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
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to update changeable messages on a DMS.
 *
 * @author Douglas Lau
 */
abstract public class OpDMSMessage extends OpDMS {

	/** Flag to avoid phase loops */
	protected boolean modify = true;

	/** Sign message */
	protected final SignMessage message;

	/** Message number (row in changeable message table) */
	protected final int msg_num;

	/** Message CRC */
	protected final int message_crc;

	/** Create a new DMS command message object */
	public OpDMSMessage(DMSImpl d, SignMessage m, int n) {
		super(COMMAND, d);
		message = m;
		msg_num = n;
		message_crc = DmsMessageCRC.calculate(m.getMulti(), 0, 0);
	}

	/** Phase to set the status to modify request */
	protected class ModifyRequest extends Phase {

		/** Set the status to modify request */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			status.setEnum(DmsMessageStatus.Enum.modifyReq);
			mess.add(status);
			try {
				DMS_LOG.log(dms.getName() + ":= " + status);
				mess.setRequest();
			}
			catch(SNMP.Message.GenError e) {
				if(modify) {
					modify = false;
					return new InitialStatus();
				} else
					throw e;
			}
			return new SetMultiString();
		}
	}

	/** Phase to get the initial message status */
	protected class InitialStatus extends Phase {

		/** Get the initial message status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			mess.add(status);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			if(status.isModifying())
				return new SetMultiString();
			else
				return new ModifyRequest();
		}
	}

	/** Phase to set the message MULTI string */
	protected class SetMultiString extends Phase {

		/** Set the message MULTI string */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageMultiString multi = new DmsMessageMultiString(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			DmsMessageBeacon beacon = new DmsMessageBeacon(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			DmsMessagePixelService srv = new DmsMessagePixelService(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			multi.setString(message.getMulti());
			beacon.setInteger(0);
			srv.setInteger(0);
			mess.add(multi);
			mess.add(beacon);
			mess.add(srv);
			DMS_LOG.log(dms.getName() + ":= " + multi);
			DMS_LOG.log(dms.getName() + ":= " + beacon);
			DMS_LOG.log(dms.getName() + ":= " + srv);
			mess.setRequest();
			return new ValidateRequest();
		}
	}

	/** Phase to set the status to validate request */
	protected class ValidateRequest extends Phase {

		/** Set the status to modify request */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			status.setEnum(DmsMessageStatus.Enum.validateReq);
			mess.add(status);
			try {
				DMS_LOG.log(dms.getName() + ":= " + status);
				mess.setRequest();
			}
			catch(SNMP.Message.GenError e) {
				return new ValidateMessageError();
			}
			return new FinalStatus();
		}
	}

	/** Phase to get the final message status */
	protected class FinalStatus extends Phase {

		/** Get the final message status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			DmsMessageCRC crc = new DmsMessageCRC(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			mess.add(status);
			mess.add(crc);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			DMS_LOG.log(dms.getName() + ": " + crc);
			if(!status.isValid())
				return new ValidateMessageError();
			if(message_crc != crc.getInteger()) {
				DMS_LOG.log(dms.getName() + " Message CRC " +
					message_crc);
				throw new ControllerException("Message CRC: " +
					message_crc + ", " + crc.getInteger());
			}
			return nextPhase();
		}
	}

	/** Phase to get the validate message error */
	protected class ValidateMessageError extends Phase {

		/** Get the validate message error */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsValidateMessageError error =
				new DmsValidateMessageError();
			DmsMultiSyntaxError m_err = new DmsMultiSyntaxError();
			DmsMultiSyntaxErrorPosition e_pos =
				new DmsMultiSyntaxErrorPosition();
			mess.add(error);
			mess.add(m_err);
			mess.add(e_pos);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + error);
			DMS_LOG.log(dms.getName() + ": " + m_err);
			DMS_LOG.log(dms.getName() + ": " + e_pos);
			if(error.isSyntaxMulti())
				errorStatus = m_err.toString();
			else if(error.isError())
				errorStatus = error.toString();
			return null;
		}
	}

	/** Get the next phase of the operation */
	abstract protected Phase nextPhase();
}
