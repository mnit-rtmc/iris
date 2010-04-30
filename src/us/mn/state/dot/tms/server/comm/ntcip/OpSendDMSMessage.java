/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
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

	/** Flag to indicate message row has been updated */
	protected boolean row_updated = false;

	/** Create a new send DMS message operation */
	public OpSendDMSMessage(DMSImpl d, SignMessage sm, User o) {
		this(d, sm, o, 1);
	}

	/** Create a new send DMS message operation */
	public OpSendDMSMessage(DMSImpl d, SignMessage sm, User o, int mn) {
		super(d, sm, mn);
		owner = o;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		dms.setMessageNext(message);
		if(SignMessageHelper.isBlank(message))
			return new BlankMessage();
		else if(msg_num > 1)
			return new ActivateMessage();
		else {
			row_updated = true;
			return new ModifyRequest();
		}
	}

	/** Get the next phase of the operation */
	protected Phase nextPhase() {
		return new ActivateMessage();
	}

	/** Get the message duration */
	protected int getDuration() {
		return getDuration(message.getDuration());
	}

	/** Phase to activate a blank message */
	protected class BlankMessage extends Phase {

		/** Blank the message */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsActivateMessage act = new DmsActivateMessage();
			act.setDuration(DURATION_INDEFINITE);
			act.setPriority(MAX_MESSAGE_PRIORITY);
			act.setMemoryType(DmsMessageMemoryType.Enum.blank);
			act.setNumber(1);
			act.setCrc(0);
			act.setAddress(0);
			mess.add(act);
			try {
				DMS_LOG.log(dms.getName() + ":= " + act);
				mess.storeProps();
			}
			catch(SNMP.Message.GenError e) {
				return new QueryActivateMsgError();
			}
			// FIXME: this should happen on SONAR thread
			dms.setMessageCurrent(message, owner);
			return new SetPostActivationStuff();
		}
	}

	/** Phase to activate the message */
	protected class ActivateMessage extends Phase {

		/** Activate the message */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsActivateMessage act = new DmsActivateMessage();
			act.setDuration(getDuration());
			act.setPriority(MAX_MESSAGE_PRIORITY);
			act.setMemoryType(DmsMessageMemoryType.Enum.changeable);
			act.setNumber(msg_num);
			act.setCrc(message_crc);
			act.setAddress(0);
			mess.add(act);
			try {
				DMS_LOG.log(dms.getName() + ":= " + act);
				mess.storeProps();
			}
			catch(SNMP.Message.GenError e) {
				return new QueryActivateMsgError();
			}
			// FIXME: this should happen on SONAR thread
			dms.setMessageCurrent(message, owner);
			return new SetPostActivationStuff();
		}
	}

	/** Phase to query the activate message error */
	protected class QueryActivateMsgError extends Phase {

		/** Query the activate message error */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsActivateMsgError error = new DmsActivateMsgError();
			mess.add(error);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + error);
			switch(error.getEnum()) {
			case syntaxMULTI:
				setErrorStatus(error.toString());
				return new QueryMultiSyntaxError();
			case other:
				setErrorStatus(error.toString());
				return new LedstarActivateError();
			case messageMemoryType:
				// For original 1203v1, blank memory type was
				// not defined.  This will cause a blank msg
				// to be stored in changeable msg #1.
			case messageStatus:
			case messageNumber:
			case messageCRC:
				if(!row_updated) {
					row_updated = true;
					return new ModifyRequest();
				}
				// else fall through to default case ...
			default:
				setErrorStatus(error.toString());
				return null;
			}
		}
	}

	/** Phase to query the MULTI syntax error */
	protected class QueryMultiSyntaxError extends Phase {

		/** Query the MULTI syntax error */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsMultiSyntaxError m_err = new DmsMultiSyntaxError();
			DmsMultiSyntaxErrorPosition e_pos =
				new DmsMultiSyntaxErrorPosition();
			mess.add(m_err);
			mess.add(e_pos);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + m_err);
			DMS_LOG.log(dms.getName() + ": " + e_pos);
			setErrorStatus(m_err.toString());
			return null;
		}
	}

	/** Phase to get the ledstar activate message error */
	protected class LedstarActivateError extends Phase {

		/** Get the Ledstar activate message error */
		protected Phase poll(CommMessage mess) throws IOException {
			LedActivateMsgError error = new LedActivateMsgError();
			mess.add(error);
			try {
				mess.queryProps();
			}
			catch(SNMP.Message.NoSuchName e) {
				// must not be a Ledstar sign ...
				return null;
			}
			DMS_LOG.log(dms.getName() + ": " + error);
			setErrorStatus(error.toString());
			return null;
		}
	}

	/** Phase to set the post-activation objects */
	protected class SetPostActivationStuff extends Phase {

		/** Set the post-activation objects */
		protected Phase poll(CommMessage mess) throws IOException {
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
			mess.storeProps();
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
		comm_msg.setCrc(message_crc);
		long_msg.setMemoryType(DmsMessageMemoryType.Enum.changeable);
		long_msg.setNumber(msg_num);
		long_msg.setCrc(message_crc);
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

	/** Cleanup the operation */
	public void cleanup() {
		dms.setMessageNext(null);
		super.cleanup();
	}
}
