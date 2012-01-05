/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;

/**
 * Operation to send a new message to a DMS and activate it.
 *
 * @author Douglas Lau
 */
public class OpSendDMSMessage extends OpDMS {

	/** Maximum message priority */
	static protected final int MAX_MESSAGE_PRIORITY = 255;

	/** Communication loss message */
	protected final DmsCommunicationsLossMessage comm_msg =
		new DmsCommunicationsLossMessage();

	/** Long power recovery message */
	protected final DmsLongPowerRecoveryMessage long_msg =
		new DmsLongPowerRecoveryMessage();

	/** Flag to avoid phase loops */
	protected boolean modify_requested = false;

	/** Sign message */
	protected final SignMessage message;

	/** Message number (row in changeable message table).  This is normally
	 * 1 for uncached messages.  If a number greater than 1 is used, an
	 * attempt will be made to activate that message -- if that fails, the
	 * changeable message table will be updated and then the message will
	 * be activated.  This allows complex messages to remain cached and
	 * activated quickly. */
	protected final int msg_num;

	/** Message CRC */
	protected final int message_crc;

	/** User who deployed the message */
	protected final User owner;

	/** Get the message duration */
	protected int getDuration() {
		return getDuration(message.getDuration());
	}

	/** Create a new send DMS message operation */
	public OpSendDMSMessage(DMSImpl d, SignMessage sm, User o, int mn) {
		super(PriorityLevel.COMMAND, d);
		message = sm;
		owner = o;
		msg_num = mn;
		message_crc = DmsMessageCRC.calculate(sm.getMulti(), 0, 0);
	}

	/** Create a new send DMS message operation */
	public OpSendDMSMessage(DMSImpl d, SignMessage sm, User o) {
		this(d, sm, o, 1);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		dms.setMessageNext(message);
		if(SignMessageHelper.isBlank(message))
			return new ActivateBlankMsg();
		else if(msg_num > 1)
			return new ActivateMessage();
		else
			return new ModifyRequest();
	}

	/** Phase to activate a blank message */
	protected class ActivateBlankMsg extends Phase {

		/** Activate a blank message */
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
				return new QueryActivateMsgErr();
			}
			dms.setMessageCurrent(message, owner);
			return new SetPostActivationStuff();
		}
	}

	/** Phase to set the status to modify request */
	protected class ModifyRequest extends Phase {

		/** Set the status to modify request */
		protected Phase poll(CommMessage mess) throws IOException {
			modify_requested = true;
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			status.setEnum(DmsMessageStatus.Enum.modifyReq);
			mess.add(status);
			try {
				DMS_LOG.log(dms.getName() + ":= " + status);
				mess.storeProps();
			}
			catch(SNMP.Message.BadValue e) {
				// This should only happen if the message
				// status is "validating" ...
				return new QueryMsgStatus();
			}
			catch(SNMP.Message.GenError e) {
				// This should never happen (but of
				// course, it does for some vendors)
				return new QueryMsgStatus();
			}
			return new ModifyMessage();
		}
	}

	/** Phase to query the message status */
	protected class QueryMsgStatus extends Phase {

		/** Query the message status */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			mess.add(status);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + status);
			if(status.isModifying())
				return new ModifyMessage();
			else if(!modify_requested)
				return new ModifyRequest();
			else {
				setErrorStatus(status.toString());
				return null;
			}
		}
	}

	/** Phase to modify the message */
	protected class ModifyMessage extends Phase {

		/** Modify the message */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsMessageMultiString multi = new DmsMessageMultiString(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			DmsMessageBeacon beacon = new DmsMessageBeacon(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			DmsMessagePixelService srv = new DmsMessagePixelService(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			DmsMessageRunTimePriority prior =
				new DmsMessageRunTimePriority(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			multi.setString(message.getMulti());
			beacon.setInteger(0);
			srv.setInteger(0);
			prior.setInteger(message.getRunTimePriority());
			mess.add(multi);
			mess.add(beacon);
			mess.add(srv);
			mess.add(prior);
			DMS_LOG.log(dms.getName() + ":= " + multi);
			DMS_LOG.log(dms.getName() + ":= " + beacon);
			DMS_LOG.log(dms.getName() + ":= " + srv);
			DMS_LOG.log(dms.getName() + ":= " + prior);
			mess.storeProps();
			return new ValidateRequest();
		}
	}

	/** Phase to set the status to validate request */
	protected class ValidateRequest extends Phase {

		/** Set the status to validate request */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			status.setEnum(DmsMessageStatus.Enum.validateReq);
			mess.add(status);
			try {
				DMS_LOG.log(dms.getName() + ":= " + status);
				mess.storeProps();
			}
			catch(SNMP.Message.GenError e) {
				return new QueryValidateMsgErr();
			}
			return new QueryMsgValidity();
		}
	}

	/** Phase to query the message validity */
	protected class QueryMsgValidity extends Phase {

		/** Query the message validity */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsMessageStatus status = new DmsMessageStatus(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			DmsMessageCRC crc = new DmsMessageCRC(
				DmsMessageMemoryType.Enum.changeable, msg_num);
			mess.add(status);
			mess.add(crc);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + status);
			DMS_LOG.log(dms.getName() + ": " + crc);
			if(!status.isValid())
				return new QueryValidateMsgErr();
			if(message_crc != crc.getInteger()) {
				String ms = "Message CRC: " +
					Integer.toHexString(message_crc) + ", "+
					Integer.toHexString(crc.getInteger());
				DMS_LOG.log(dms.getName() + ": " + ms);
				setErrorStatus(ms);
				return null;
			}
			return new ActivateMessage();
		}
	}

	/** Phase to query a validate message error */
	protected class QueryValidateMsgErr extends Phase {

		/** Query a validate message error */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsValidateMessageError error =
				new DmsValidateMessageError();
			DmsMultiSyntaxError m_err = new DmsMultiSyntaxError();
			DmsMultiSyntaxErrorPosition e_pos =
				new DmsMultiSyntaxErrorPosition();
			mess.add(error);
			mess.add(m_err);
			mess.add(e_pos);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + error);
			DMS_LOG.log(dms.getName() + ": " + m_err);
			DMS_LOG.log(dms.getName() + ": " + e_pos);
			if(error.isSyntaxMulti())
				setErrorStatus(m_err.toString());
			else if(error.isError())
				setErrorStatus(error.toString());
			return null;
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
				return new QueryActivateMsgErr();
			}
			dms.setMessageCurrent(message, owner);
			return new SetPostActivationStuff();
		}
	}

	/** Phase to query an activate message error */
	protected class QueryActivateMsgErr extends Phase {

		/** Query an activate message error */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsActivateMsgError error = new DmsActivateMsgError();
			mess.add(error);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + error);
			switch(error.getEnum()) {
			case syntaxMULTI:
				setErrorStatus(error.toString());
				return new QueryMultiSyntaxErr();
			case other:
				setErrorStatus(error.toString());
				return new QueryLedstarActivateErr();
			case messageMemoryType:
				// For original 1203v1, blank memory type was
				// not defined.  This will cause a blank msg
				// to be stored in changeable msg #1.
			case messageStatus:
			case messageNumber:
			case messageCRC:
				// This message doesn't exist in the table,
				// so go back and modify the table.
				if(!modify_requested)
					return new ModifyRequest();
				// else fall through to default case ...
			default:
				setErrorStatus(error.toString());
				return null;
			}
		}
	}

	/** Phase to query a MULTI syntax error */
	protected class QueryMultiSyntaxErr extends Phase {

		/** Query a MULTI syntax error */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsMultiSyntaxError m_err = new DmsMultiSyntaxError();
			DmsMultiSyntaxErrorPosition e_pos =
				new DmsMultiSyntaxErrorPosition();
			mess.add(m_err);
			mess.add(e_pos);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + m_err);
			DMS_LOG.log(dms.getName() + ": " + e_pos);
			if(m_err.isOther())
				return new QueryOtherMultiErr(m_err);
			else {
				setErrorStatus(m_err.toString());
				return null;
			}
		}
	}

	/** Phase to query an other MULTI error */
	protected class QueryOtherMultiErr extends Phase {

		/** MULTI syntax error */
		protected final DmsMultiSyntaxError m_err;

		/** Create a phase to query an other MULTI error */
		protected QueryOtherMultiErr(DmsMultiSyntaxError er) {
			m_err = er;
		}

		/** Query an other MULTI error */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsMultiOtherErrorDescription o_err =
				new DmsMultiOtherErrorDescription();
			mess.add(o_err);
			try {
				mess.queryProps();
				DMS_LOG.log(dms.getName() + ": " + o_err);
				setErrorStatus(o_err.toString());
			}
			catch(SNMP.Message.NoSuchName e) {
				// For 1203v1, dmsMultiOtherErrorDescription
				// had not been defined...
				setErrorStatus(m_err.toString());
			}
			return null;
		}
	}

	/** Phase to query a ledstar activate message error */
	protected class QueryLedstarActivateErr extends Phase {

		/** Query a Ledstar activate message error */
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
