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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;

/**
 * Operation to command a new message on a DMS.
 *
 * @author Douglas Lau
 */
public class OpSendDMSMessage extends OpDMS {

	/** Maximum message priority */
	static protected final int MAX_MESSAGE_PRIORITY = 255;

	/** Flag to avoid phase loops */
	protected boolean modify = true;

	/** Sign message */
	protected final SignMessage message;

	/** User who deployed the message */
	protected final User owner;

	/** Message CRC */
	protected int messageCRC;

	/** Create a new DMS command message object */
	public OpSendDMSMessage(DMSImpl d, SignMessage m, User o) {
		super(COMMAND, d);
		message = m;
		owner = o;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		if(dms.checkPriority(message.getPriority()))
			return new ModifyRequest();
		else
			return null;
	}

	/** Get the message duration */
	protected int getDuration() {
		return getDuration(message.getDuration());
	}

	/** Phase to set the status to modify request */
	protected class ModifyRequest extends Phase {

		/** Set the status to modify request */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, 1);
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
				DmsMessageMemoryType.Enum.changeable, 1);
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
				DmsMessageMemoryType.Enum.changeable, 1);
			multi.setString(message.getMulti().toString());
			mess.add(multi);
			DMS_LOG.log(dms.getName() + ":= " + multi);
			mess.setRequest();
			return new ValidateRequest();
		}
	}

	/** Phase to set the status to validate request */
	protected class ValidateRequest extends Phase {

		/** Set the status to modify request */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, 1);
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

	/** Phase to get the validate message error */
	protected class ValidateMessageError extends Phase {

		/** Get the validate message error */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsValidateMessageError error = new
				DmsValidateMessageError();
			DmsMultiSyntaxError m_err = new DmsMultiSyntaxError();
			mess.add(error);
			mess.add(m_err);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + error);
			DMS_LOG.log(dms.getName() + ": " + m_err);
			if(error.isSyntaxMulti())
				errorStatus = m_err.toString();
			else if(error.isError())
				errorStatus = error.toString();
			return null;
		}
	}

	/** Phase to get the final message status */
	protected class FinalStatus extends Phase {

		/** Get the final message status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, 1);
			DmsMessageCRC crc = new DmsMessageCRC(
				DmsMessageMemoryType.Enum.changeable, 1);
			mess.add(status);
			mess.add(crc);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + status);
			DMS_LOG.log(dms.getName() + ": " + crc);
			if(!status.isValid())
				return new ValidateMessageError();
			messageCRC = crc.getInteger();
			return new ActivateMessage();
		}
	}

	/** Phase to activate the message */
	protected class ActivateMessage extends Phase {

		/** Activate the message */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsActivateMessage act = new DmsActivateMessage();
			act.setDuration(getDuration());
			act.setPriority(MAX_MESSAGE_PRIORITY);
			act.setMemoryType(DmsMessageMemoryType.Enum.changeable);
			act.setNumber(1);
			act.setCrc(messageCRC);
			act.setAddress(0);
			mess.add(act);
			try {
				DMS_LOG.log(dms.getName() + ":= " + act);
				mess.setRequest();
			}
			catch(SNMP.Message.GenError e) {
				return new ActivateMessageError();
			}
			// FIXME: this should happen on SONAR thread
			dms.setMessageCurrent(message, owner);
			return new TimeRemaining();
		}
	}

	/** Phase to get the activate message error */
	protected class ActivateMessageError extends Phase {

		/** Get the activate message error */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsActivateMsgError error = new DmsActivateMsgError();
			DmsMultiSyntaxError m_err = new DmsMultiSyntaxError();
			mess.add(error);
			mess.add(m_err);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + error);
			DMS_LOG.log(dms.getName() + ": " + m_err);
			switch(error.getEnum()) {
			case syntaxMULTI:
				errorStatus = m_err.toString();
				return null;
			case other:
				errorStatus = error.toString();
				return new LedstarActivateError();
			default:
				errorStatus = error.toString();
				return null;
			}
		}
	}

	/** Phase to get the ledstar activate message error */
	protected class LedstarActivateError extends Phase {

		/** Get the Ledstar activate message error */
		protected Phase poll(AddressedMessage mess) throws IOException {
			LedActivateMsgError error = new LedActivateMsgError();
			mess.add(error);
			try {
				mess.getRequest();
			}
			catch(SNMP.Message.NoSuchName e) {
				// must not be a Ledstar sign ...
				return null;
			}
			DMS_LOG.log(dms.getName() + ": " + error);
			errorStatus = error.toString();
			return null;
		}
	}

	/** Phase to set the message time remaining */
	protected class TimeRemaining extends Phase {

		/** Set the message time remaining */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsMessageTimeRemaining time =
				new DmsMessageTimeRemaining();
			time.setInteger(getDuration());
			mess.add(time);
			DMS_LOG.log(dms.getName() + ":= " + time);
			mess.setRequest();
			return null;
		}
	}
}
