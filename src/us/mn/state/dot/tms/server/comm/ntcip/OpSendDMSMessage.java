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
public class OpSendDMSMessage extends OpDMSMessage {

	/** Maximum message priority */
	static protected final int MAX_MESSAGE_PRIORITY = 255;

	/** Communication loss message */
	protected final DmsCommunicationsLossMessage comm_msg =
		new DmsCommunicationsLossMessage();

	/** Long power recovery message */
	protected final DmsLongPowerRecoveryMessage long_msg =
		new DmsLongPowerRecoveryMessage();

	/** User who deployed the message */
	protected final User owner;

	/** Create a new DMS command message object */
	public OpSendDMSMessage(DMSImpl d, SignMessage m, User o) {
		super(d, m, 1);
		owner = o;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new ModifyRequest();
	}

	/** Get the next phase of the operation */
	protected Phase nextPhase() {
		return new ActivateMessage();
	}

	/** Get the message duration */
	protected int getDuration() {
		return getDuration(message.getDuration());
	}

	/** Phase to activate the message */
	protected class ActivateMessage extends Phase {

		/** Activate the message */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsActivateMessage act = new DmsActivateMessage();
			act.setDuration(getDuration());
			act.setPriority(MAX_MESSAGE_PRIORITY);
			act.setMemoryType(DmsMessageMemoryType.Enum.changeable);
			act.setNumber(msg_num);
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
			return new SetPostActivationStuff();
		}
	}

	/** Phase to get the activate message error */
	protected class ActivateMessageError extends Phase {

		/** Get the activate message error */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsActivateMsgError error = new DmsActivateMsgError();
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

	/** Phase to set the post-activation objects */
	protected class SetPostActivationStuff extends Phase {

		/** Set the post-activation objects */
		protected Phase poll(AddressedMessage mess) throws IOException {
			// NOTE: setting DmsMessageTimeRemaining should not
			//       be necessary.  I don't really know why it's
			//       done here -- probably to work around some
			//       stupid sign bug.  It may no longer be needed.
			DmsMessageTimeRemaining time =
				new DmsMessageTimeRemaining();
			time.setInteger(getDuration());
			if(isScheduledIndefinite())
				setCommAndPower();
			else
				setCommAndPowerBlank();
			mess.add(time);
			mess.add(comm_msg);
			mess.add(long_msg);
			DMS_LOG.log(dms.getName() + ":= " + time);
			DMS_LOG.log(dms.getName() + ":= " + comm_msg);
			DMS_LOG.log(dms.getName() + ":= " + long_msg);
			mess.setRequest();
			return null;
		}
	}

	/** Check if the message is scheduled and has indefinite duration */
	protected boolean isScheduledIndefinite() {
		return message.getScheduled() && message.getDuration() == null;
	}

	/** Set the comm loss and power recovery msgs */
	protected void setCommAndPower() {
		comm_msg.setMemoryType(DmsMessageMemoryType.Enum.changeable);
		comm_msg.setNumber(msg_num);
		comm_msg.setCrc(messageCRC);
		long_msg.setMemoryType(DmsMessageMemoryType.Enum.changeable);
		long_msg.setNumber(msg_num);
		long_msg.setCrc(messageCRC);
	}

	/** Set the comm loss and power recovery msgs to blank */
	protected void setCommAndPowerBlank() {
		comm_msg.setMemoryType(DmsMessageMemoryType.Enum.blank);
		comm_msg.setNumber(1);
		comm_msg.setCrc(0);
		long_msg.setMemoryType(DmsMessageMemoryType.Enum.blank);
		long_msg.setNumber(1);
		long_msg.setCrc(0);
	}
}
