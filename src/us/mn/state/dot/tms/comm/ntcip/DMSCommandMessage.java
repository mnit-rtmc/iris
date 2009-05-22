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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.comm.AddressedMessage;
import us.mn.state.dot.tms.server.DMSImpl;

/**
 * Operation to command a new message on a DMS.
 *
 * @author Douglas Lau
 */
public class DMSCommandMessage extends DMSOperation {

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
	public DMSCommandMessage(DMSImpl d, SignMessage m, User o) {
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
			mess.add(new DmsMessageStatus(
				DmsMessageMemoryType.CHANGEABLE, 1,
				DmsMessageStatus.MODIFY_REQ));
			try {
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
				DmsMessageMemoryType.CHANGEABLE, 1);
			mess.add(status);
			mess.getRequest();
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
			mess.add(new DmsMessageMultiString(
				DmsMessageMemoryType.CHANGEABLE, 1,
				message.getMulti().toString()));
			mess.setRequest();
			return new ValidateRequest();
		}
	}

	/** Phase to set the status to validate request */
	protected class ValidateRequest extends Phase {

		/** Set the status to modify request */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new DmsMessageStatus(
				DmsMessageMemoryType.CHANGEABLE, 1,
				DmsMessageStatus.VALIDATE_REQ));
			try {
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
			mess.add(error);
			DmsMultiSyntaxError multi = new DmsMultiSyntaxError();
			mess.add(multi);
			mess.getRequest();
			if(error.isSyntaxMulti())
				errorStatus = multi.toString();
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
				DmsMessageMemoryType.CHANGEABLE, 1);
			mess.add(status);
			DmsMessageCRC crc = new DmsMessageCRC(
				DmsMessageMemoryType.CHANGEABLE, 1);
			mess.add(crc);
			mess.getRequest();
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
			mess.add(new DmsActivateMessage(getDuration(),
				MAX_MESSAGE_PRIORITY,
				DmsMessageMemoryType.CHANGEABLE, 1,
				messageCRC, 0));
			try {
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
			mess.add(error);
			DmsMultiSyntaxError multi = new DmsMultiSyntaxError();
			mess.add(multi);
			mess.getRequest();
			switch(error.getInteger()) {
				case DmsActivateMsgError.SYNTAX_MULTI:
					errorStatus = multi.toString();
					break;
				case DmsActivateMsgError.OTHER:
					// FIXME: ADDCO does this too ...
					return new LedstarActivateError();
				default:
					errorStatus = error.toString();
					break;
			}
			return null;
		}
	}

	/** Phase to get the ledstar activate message error */
	protected class LedstarActivateError extends Phase {

		/** Get the Ledstar activate message error */
		protected Phase poll(AddressedMessage mess) throws IOException {
			LedActivateMsgError error = new LedActivateMsgError();
			mess.add(error);
			mess.getRequest();
			errorStatus = error.toString();
			return null;
		}
	}

	/** Phase to set the message time remaining */
	protected class TimeRemaining extends Phase {

		/** Set the message time remaining */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(new DmsMessageTimeRemaining(getDuration()));
			mess.setRequest();
			return null;
		}
	}
}
