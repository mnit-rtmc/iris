/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MultiSyntaxError;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.LedActivateMsgError;
import static us.mn.state.dot.tms.server.comm.ntcip.mibledstar.MIB.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Flags;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;
import us.mn.state.dot.tms.server.comm.snmp.SNMP;

/**
 * Operation to send a message to a DMS and activate it.
 *
 * .    Possible phase transitions:
 * |
 * |              .---------------------------------------------.
 * |              +                                             |
 * |--+ ModifyRequest ----------+ QueryMsgStatus                |
 * |           |                    |      |                    |
 * |           +                    |      +                    |
 * |        ModifyMessage +---------'    QueryControlMode       |
 * |               |                                            |
 * |               +                                            |
 * |        ValidateRequest ----+ QueryValidateMsgErr           |
 * |               |                +                           |
 * |               +                |                           |
 * |        QueryMsgValidity -------'  QueryLedstarActivateErr  |
 * |           |                           +                    |
 * |           +                           |                    |
 * |--+ ActivateMessage --------+ QueryActivateMsgErr ----------'
 * |           |                      +      |
 * |           +                      |      +
 * |        SetPostActivationStuff    |  QueryMultiSyntaxErr
 * |           +                      |      |
 * |           |                      |      +
 * '--+ ActivateBlankMsg -------------'  QueryOtherMultiErr
 *
 * @author Douglas Lau
 */
public class OpSendDMSMessage extends OpDMS {

	/** Maximum message priority */
	static private final int MAX_MESSAGE_PRIORITY = 255;

	/** Make a new DmsMessageStatus enum */
	static private ASN1Enum<DmsMessageStatus> makeStatus(
		DmsMessageMemoryType mem, int n)
	{
		return new ASN1Enum<DmsMessageStatus>(dmsMessageStatus.node,
			mem.ordinal(), n);
	}

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
	private final int message_crc;

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
		message_crc = DmsMessageCRC.calculate(sm.getMulti(),
			sm.getBeaconEnabled(), 0);
	}

	/** Create a new send DMS message operation */
	public OpSendDMSMessage(DMSImpl d, SignMessage sm, User o) {
		this(d, sm, o, 1);
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
	protected Phase phaseTwo() {
		dms.setMessageNext(message);
		if (SignMessageHelper.isBlank(message))
			return new ActivateBlankMsg();
		else if (msg_num > 1)
			return new ActivateMessage();
		else
			return new ModifyRequest();
	}

	/** Phase to activate a blank message */
	protected class ActivateBlankMsg extends Phase {

		/** Activate a blank message */
		protected Phase poll(CommMessage mess) throws IOException {
			MessageActivationCode act = new MessageActivationCode(
				dmsActivateMessage.node);
			act.setDuration(DURATION_INDEFINITE);
			act.setPriority(MAX_MESSAGE_PRIORITY);
			act.setMemoryType(DmsMessageMemoryType.blank);
			act.setNumber(1);
			act.setCrc(0);
			act.setAddress(0);
			mess.add(act);
			try {
				logStore(act);
				mess.storeProps();
			}
			catch (SNMP.Message.GenError e) {
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
			ASN1Enum<DmsMessageStatus> status = makeStatus(
				DmsMessageMemoryType.changeable, msg_num);
			status.setEnum(DmsMessageStatus.modifyReq);
			mess.add(status);
			try {
				logStore(status);
				mess.storeProps();
			}
			catch (SNMP.Message.BadValue e) {
				// This should only happen if the message
				// status is "validating" ...
				return new QueryMsgStatus();
			}
			catch (SNMP.Message.GenError e) {
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
			ASN1Enum<DmsMessageStatus> status = makeStatus(
				DmsMessageMemoryType.changeable, msg_num);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			if (status.getEnum() == DmsMessageStatus.modifying)
				return new ModifyMessage();
			else if (!modify_requested)
				return new ModifyRequest();
			else if (status.getEnum() == DmsMessageStatus.valid) {
				/* Some ledstar signs prevent dmsMessageStatus
				 * from changing to modifyReq when in 'local'
				 * dmsControlMode. */
				return new QueryControlMode();
			} else {
				setErrorStatus(status.toString());
				return null;
			}
		}
	}

	/** Phase to modify the message */
	protected class ModifyMessage extends Phase {

		/** Modify the message */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1String multi = new ASN1String(dmsMessageMultiString
				.node,DmsMessageMemoryType.changeable.ordinal(),
				msg_num);
			ASN1Integer beacon = dmsMessageBeacon.makeInt(
				DmsMessageMemoryType.changeable, msg_num);
			ASN1Integer srv = dmsMessagePixelService.makeInt(
				DmsMessageMemoryType.changeable, msg_num);
			ASN1Enum<DMSMessagePriority> prior = new ASN1Enum<
				DMSMessagePriority>(dmsMessageRunTimePriority
				.node,DmsMessageMemoryType.changeable.ordinal(),
				msg_num);
			multi.setString(message.getMulti());
			beacon.setInteger(message.getBeaconEnabled() ? 1 : 0);
			srv.setInteger(0);
			prior.setInteger(message.getRunTimePriority());
			mess.add(multi);
			mess.add(beacon);
			mess.add(srv);
			mess.add(prior);
			logStore(multi);
			logStore(beacon);
			logStore(srv);
			logStore(prior);
			mess.storeProps();
			return new ValidateRequest();
		}
	}

	/** Phase to query the control mode */
	protected class QueryControlMode extends Phase {

		/** Query the control mode */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsControlMode> mode = new ASN1Enum<
				DmsControlMode>(dmsControlMode.node);
			mess.add(mode);
			mess.queryProps();
			logQuery(mode);
			setErrorStatus(mode.toString());
			return null;
		}
	}

	/** Phase to set the status to validate request */
	protected class ValidateRequest extends Phase {

		/** Set the status to validate request */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsMessageStatus> status = makeStatus(
				DmsMessageMemoryType.changeable, msg_num);
			status.setEnum(DmsMessageStatus.validateReq);
			mess.add(status);
			try {
				logStore(status);
				mess.storeProps();
			}
			catch (SNMP.Message.GenError e) {
				return new QueryValidateMsgErr(status);
			}
			return new QueryMsgValidity();
		}
	}

	/** Phase to query the message validity */
	protected class QueryMsgValidity extends Phase {

		/** Query the message validity */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsMessageStatus> status = makeStatus(
				DmsMessageMemoryType.changeable, msg_num);
			ASN1Integer crc = dmsMessageCRC.makeInt(
				DmsMessageMemoryType.changeable, msg_num);
			mess.add(status);
			mess.add(crc);
			mess.queryProps();
			logQuery(status);
			logQuery(crc);
			if (status.getEnum() != DmsMessageStatus.valid)
				return new QueryValidateMsgErr(status);
			if (message_crc != crc.getInteger()) {
				String ms = "Message CRC: " +
					Integer.toHexString(message_crc) + ", "+
					Integer.toHexString(crc.getInteger());
				logError(ms);
				setErrorStatus(ms);
				return null;
			}
			return new ActivateMessage();
		}
	}

	/** Phase to query a validate message error */
	protected class QueryValidateMsgErr extends Phase {

		/** Status code which triggered validate error */
		private final ASN1Enum<DmsMessageStatus> status;

		/** Create a query validate message error phase */
		protected QueryValidateMsgErr(ASN1Enum<DmsMessageStatus> s) {
			status = s;
		}

		/** Query a validate message error */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsValidateMessageError> error = new ASN1Enum<
				DmsValidateMessageError>(
				dmsValidateMessageError.node);
			ASN1Enum<MultiSyntaxError> m_err = new ASN1Enum<
				MultiSyntaxError>(dmsMultiSyntaxError.node);
			ASN1Integer e_pos=dmsMultiSyntaxErrorPosition.makeInt();
			mess.add(error);
			mess.add(m_err);
			mess.add(e_pos);
			mess.queryProps();
			logQuery(error);
			logQuery(m_err);
			logQuery(e_pos);
			switch (error.getEnum()) {
			case syntaxMULTI:
				setErrorStatus(m_err.toString());
				break;
			case other:
			case beacons:
			case pixelService:
				setErrorStatus(error.toString());
				break;
			default:
				// This should never happen, but of course it
				// does in some cases with Addco signs.
				setErrorStatus(status.toString());
			}
			return null;
		}
	}

	/** Phase to activate the message */
	protected class ActivateMessage extends Phase {

		/** Activate the message */
		protected Phase poll(CommMessage mess) throws IOException {
			MessageActivationCode act = new MessageActivationCode(
				dmsActivateMessage.node);
			act.setDuration(getDuration());
			act.setPriority(MAX_MESSAGE_PRIORITY);
			act.setMemoryType(DmsMessageMemoryType.changeable);
			act.setNumber(msg_num);
			act.setCrc(message_crc);
			act.setAddress(0);
			mess.add(act);
			try {
				logStore(act);
				mess.storeProps();
			}
			catch (SNMP.Message.GenError e) {
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
			ASN1Enum<DmsActivateMsgError> error = new ASN1Enum<
				DmsActivateMsgError>(dmsActivateMsgError.node);
			mess.add(error);
			mess.queryProps();
			logQuery(error);
			switch (error.getEnum()) {
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
				if (!modify_requested)
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
			ASN1Enum<MultiSyntaxError> m_err = new ASN1Enum<
				MultiSyntaxError>(dmsMultiSyntaxError.node);
			ASN1Integer e_pos=dmsMultiSyntaxErrorPosition.makeInt();
			mess.add(m_err);
			mess.add(e_pos);
			mess.queryProps();
			logQuery(m_err);
			logQuery(e_pos);
			if (m_err.getEnum() == MultiSyntaxError.other)
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
		private final ASN1Enum<MultiSyntaxError> m_err;

		/** Create a phase to query an other MULTI error */
		protected QueryOtherMultiErr(ASN1Enum<MultiSyntaxError> er) {
			m_err = er;
		}

		/** Query an other MULTI error */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1String o_err = new ASN1String(
				dmsMultiOtherErrorDescription.node);
			mess.add(o_err);
			try {
				mess.queryProps();
				logQuery(o_err);
				setErrorStatus(o_err.toString());
			}
			catch (SNMP.Message.NoSuchName e) {
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
			ASN1Flags<LedActivateMsgError> error = new ASN1Flags<
				LedActivateMsgError>(ledActivateMsgError.node);
			mess.add(error);
			try {
				mess.queryProps();
			}
			catch (SNMP.Message.NoSuchName e) {
				// must not be a Ledstar sign ...
				return null;
			}
			logQuery(error);
			setErrorStatus(error.toString());
			return null;
		}
	}

	/** Phase to set the post-activation objects */
	protected class SetPostActivationStuff extends Phase {

		/** Set the post-activation objects */
		protected Phase poll(CommMessage mess) throws IOException {
			// NOTE: setting dmsMessageTimeRemaining should not
			//       be necessary.  I don't really know why it's
			//       done here -- probably to work around some
			//       stupid sign bug.  It may no longer be needed.
			ASN1Integer time = dmsMessageTimeRemaining.makeInt();
			time.setInteger(getDuration());
			if (isScheduledIndefinite())
				setCommAndPower();
			else
				setCommAndPowerBlank();
			mess.add(time);
			mess.add(comm_msg);
			mess.add(long_msg);
			logStore(time);
			logStore(comm_msg);
			logStore(long_msg);
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
		comm_msg.setMemoryType(DmsMessageMemoryType.changeable);
		comm_msg.setNumber(msg_num);
		comm_msg.setCrc(message_crc);
		long_msg.setMemoryType(DmsMessageMemoryType.changeable);
		long_msg.setNumber(msg_num);
		long_msg.setCrc(message_crc);
	}

	/** Set the comm loss and power recovery msgs to blank */
	protected void setCommAndPowerBlank() {
		comm_msg.setMemoryType(DmsMessageMemoryType.blank);
		comm_msg.setNumber(1);
		comm_msg.setCrc(0);
		long_msg.setMemoryType(DmsMessageMemoryType.blank);
		long_msg.setNumber(1);
		long_msg.setCrc(0);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		dms.setMessageNext(null);
		super.cleanup();
	}
}
