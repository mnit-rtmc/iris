/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import java.util.ArrayList;
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgSource;
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
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;
import us.mn.state.dot.tms.server.comm.snmp.BadValue;
import us.mn.state.dot.tms.server.comm.snmp.GenError;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;
import us.mn.state.dot.tms.server.comm.snmp.SNMP;
import us.mn.state.dot.tms.utils.Base64;
import us.mn.state.dot.tms.utils.MultiSyntaxError;

/**
 * Operation to send a message to a DMS and activate it.
 *
 * <pre>
 * .    Crazy phase transition diagram:
 * |
 * |            .-----------------------------------------------------------.
 * |            +                                                           |
 * |--+ MsgModifyReq ----+ ChkMsgModifying         + QueryGraphicsConfig    |
 * |                         |                     | FindGraphicNumber      |
 * |                         |                     | CheckGraphic           |
 * |      ModifyMsg +--------'                     |   SetGraphicNotUsed    |
 * |         |                                     | SetGraphicModifying    |
 * |         +                                     | VerifyGraphicModifying |
 * |      MsgValidateReq --+ QueryValidateMsgErr   | CreateGraphic          |
 * |         |                 +      |            | SendGraphicBlock       |
 * |         +                 |      |            | ValidateGraphic        |
 * |      ChkMsgValid ---------'      +            | VerifyGraphicReady     |
 * |         |                   QueryMultiSyntaxErr VerifyGraphicID        |
 * |         +                     +      |                  |              |
 * |--+ ActivateMsg -------.       |      +              (ActivateMsg) or --|
 * |         |             |       |    QueryOtherMultiErr                  |
 * |         +             +       |                                        |
 * |      SetLossMsgs    QueryActivateMsgErr -------------------------------'
 * |         +             +          |
 * |         |             |          +
 * '--+ ActivateBlankMsg --'    QueryLedstarActivateErr
 * </pre>
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
		return new ASN1Enum<DmsMessageStatus>(DmsMessageStatus.class,
			dmsMessageStatus.node, mem.ordinal(), n);
	}

	/** Make a new DmsGraphicStatus enum */
	static private ASN1Enum<DmsGraphicStatus> makeGStatus(int row) {
		return new ASN1Enum<DmsGraphicStatus>(DmsGraphicStatus.class,
			dmsGraphicStatus.node, row);
	}

	/** Sign message */
	private final SignMessage message;

	/** MULTI string */
	private final String multi;

	/** Message number (row in changeable message table).  This is normally
	 * 1 for uncached messages.  If a number greater than 1 is used, an
	 * attempt will be made to activate that message -- if that fails, the
	 * changeable message table will be updated and then the message will
	 * be activated.  This allows complex messages to remain cached and
	 * activated quickly. */
	private final int msg_num;

	/** Message CRC */
	private final int message_crc;

	/** Communication loss message */
	private final MessageIDCode comm_msg = new MessageIDCode(
		dmsCommunicationsLossMessage.node);

	/** Long power recovery message */
	private final MessageIDCode long_msg = new MessageIDCode(
		dmsLongPowerRecoveryMessage.node);

	/** Flag to avoid phase loops */
	private boolean msg_validated = false;

	/** Iterator of graphics in the sign message */
	private final Iterator<Graphic> graphics;

	/** List of DmsGraphicStatus for each row in table */
	private final ArrayList<ASN1Enum<DmsGraphicStatus>> g_stat =
		new ArrayList<ASN1Enum<DmsGraphicStatus>>();

	/** Color scheme supported (for graphics) */
	private final ASN1Enum<DmsColorScheme> color_scheme = new ASN1Enum<
		DmsColorScheme>(DmsColorScheme.class, dmsColorScheme.node);

	/** Number of graphics supported */
	private final ASN1Integer max_graphics = dmsGraphicMaxEntries.makeInt();

	/** Size of graphic blocks (in bytes) */
	private final ASN1Integer block_size = dmsGraphicBlockSize.makeInt();

	/** Get the message duration */
	private int getDuration() {
		return getDuration(message.getDuration());
	}

	/** Create a new send DMS message operation */
	public OpSendDMSMessage(DMSImpl d, SignMessage sm) {
		super(PriorityLevel.COMMAND, d);
		message = sm;
		multi = parseMulti(sm.getMulti());
		msg_num = lookupMsgNum(multi);
		message_crc = DmsMessageCRC.calculate(multi,
			sm.getBeaconEnabled(), 0);
		graphics = GraphicHelper.lookupMulti(multi);
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
		dms.setMsgNext(message);
		if (SignMessageHelper.isBlank(message))
			return new ActivateBlankMsg();
		else if (msg_num > 1)
			return new ActivateMsg();
		else
			return new MsgModifyReq();
	}

	/** Phase to activate a blank message */
	protected class ActivateBlankMsg extends Phase {

		/** Activate a blank message */
		@SuppressWarnings("unchecked")
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
			catch (NoSuchName e) {
				// Some Ledstar signs will return NoSuchName
				// when trying to set dmsActivateMessage with
				// the "wrong" community name (Public).
				setErrorStatus("READ ONLY (NoSuchName)");
				return null;
			}
			catch (GenError e) {
				return new QueryActivateMsgErr();
			}
			dms.setMsgCurrentNotify(message);
			return new SetLossMsgs();
		}
	}

	/** Phase to set message status to modify request */
	protected class MsgModifyReq extends Phase {

		/** Set message status to modify request */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsMessageStatus> status = makeStatus(
				DmsMessageMemoryType.changeable, msg_num);
			status.setEnum(DmsMessageStatus.modifyReq);
			mess.add(status);
			try {
				logStore(status);
				mess.storeProps();
			}
			catch (BadValue e) {
				// This should only happen if the message
				// status is "validating" ...
				return new ChkMsgModifying();
			}
			catch (GenError e) {
				// This should never happen (but of
				// course, it does for some vendors)
				return new ChkMsgModifying();
			}
			return new ChkMsgModifying();
		}
	}

	/** Phase to check message status is modifying */
	protected class ChkMsgModifying extends Phase {

		/** Query the message status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsControlMode> mode = new ASN1Enum<
				DmsControlMode>(DmsControlMode.class,
				dmsControlMode.node);
			ASN1Enum<DmsMessageStatus> status = makeStatus(
				DmsMessageMemoryType.changeable, msg_num);
			mess.add(mode);
			mess.add(status);
			mess.queryProps();
			logQuery(mode);
			logQuery(status);
			if (mode.getEnum() != DmsControlMode.central) {
				// If we modify a message when the sign is in
				// 'local' mode, we will get a GEN error.
				// It's better if we don't even try.
				setErrorStatus(mode.toString());
				return null;
			}
			if (status.getEnum() == DmsMessageStatus.modifying)
				return new ModifyMsg();
			else {
				setErrorStatus(status.toString());
				return null;
			}
		}
	}

	/** Phase to modify the message */
	protected class ModifyMsg extends Phase {

		/** Modify the message */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1String ms = new ASN1String(dmsMessageMultiString
				.node,DmsMessageMemoryType.changeable.ordinal(),
				msg_num);
			ASN1Integer beacon = dmsMessageBeacon.makeInt(
				DmsMessageMemoryType.changeable, msg_num);
			ASN1Integer srv = dmsMessagePixelService.makeInt(
				DmsMessageMemoryType.changeable, msg_num);
			ASN1Enum<DmsMsgPriority> prior = new ASN1Enum<
				DmsMsgPriority>(DmsMsgPriority.class,
				dmsMessageRunTimePriority.node,
				DmsMessageMemoryType.changeable.ordinal(),
				msg_num);
			ms.setString(multi);
			beacon.setInteger(message.getBeaconEnabled() ? 1 : 0);
			srv.setInteger(0);
			prior.setInteger(message.getRunTimePriority());
			mess.add(ms);
			mess.add(beacon);
			mess.add(srv);
			mess.add(prior);
			logStore(ms);
			logStore(beacon);
			logStore(srv);
			logStore(prior);
			mess.storeProps();
			return new MsgValidateReq();
		}
	}

	/** Phase to set message status to validate request */
	protected class MsgValidateReq extends Phase {

		/** Set message status to validate request */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsMessageStatus> status = makeStatus(
				DmsMessageMemoryType.changeable, msg_num);
			status.setEnum(DmsMessageStatus.validateReq);
			mess.add(status);
			try {
				logStore(status);
				mess.storeProps();
			}
			catch (GenError e) {
				return new QueryValidateMsgErr(status);
			}
			return new ChkMsgValid();
		}
	}

	/** Phase to check message status is valid */
	protected class ChkMsgValid extends Phase {

		/** Query the message validity */
		@SuppressWarnings("unchecked")
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
				setErrorStatus(ms);
				return null;
			}
			msg_validated = true;
			return new ActivateMsg();
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
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsValidateMessageError> error = new ASN1Enum<
				DmsValidateMessageError>(
				DmsValidateMessageError.class,
				dmsValidateMessageError.node);
			mess.add(error);
			mess.queryProps();
			logQuery(error);
			switch (error.getEnum()) {
			case syntaxMULTI:
				return new QueryMultiSyntaxErr();
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
	protected class ActivateMsg extends Phase {

		/** Activate the message */
		@SuppressWarnings("unchecked")
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
			catch (NoSuchName e) {
				// Some Ledstar signs will return NoSuchName
				// when trying to set dmsActivateMessage with
				// the "wrong" community name (Public).
				setErrorStatus("READ ONLY (NoSuchName)");
				return null;
			}
			catch (GenError e) {
				return new QueryActivateMsgErr();
			}
			dms.setMsgCurrentNotify(message);
			return new SetLossMsgs();
		}
	}

	/** Phase to query an activate message error */
	protected class QueryActivateMsgErr extends Phase {

		/** Query an activate message error */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsActivateMsgError> error = new ASN1Enum<
				DmsActivateMsgError>(DmsActivateMsgError.class,
				dmsActivateMsgError.node);
			mess.add(error);
			mess.queryProps();
			logQuery(error);
			switch (error.getEnum()) {
			case syntaxMULTI:
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
				// If the message has not been validated
				// yet, go back and do that.
				if (!msg_validated)
					return new MsgModifyReq();
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
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<MultiSyntaxError> m_err = new ASN1Enum<
				MultiSyntaxError>(MultiSyntaxError.class,
				dmsMultiSyntaxError.node);
			ASN1Integer e_pos=dmsMultiSyntaxErrorPosition.makeInt();
			mess.add(m_err);
			mess.add(e_pos);
			mess.queryProps();
			logQuery(m_err);
			logQuery(e_pos);
			switch (m_err.getEnum()) {
			case other:
				return new QueryOtherMultiErr(m_err);
			case graphicID:
			case graphicNotDefined:
				/* Note: if the graphics iterator is empty,
				 *       then just fail the operation. */
				if (graphics.hasNext())
					return new QueryGraphicsConfig();
				// else fall through to default case ...
			default:
				setErrorStatus(m_err.toString());
				return null;
			}
		}
	}

	/** Phase to query an other MULTI error */
	protected class QueryOtherMultiErr extends Phase {

		/** MULTI syntax error */
		private final ASN1Enum<MultiSyntaxError> m_err;

		/** Other error string */
		private final ASN1String o_err = new ASN1String(
			dmsMultiOtherErrorDescription.node);

		/** Create a phase to query an other MULTI error */
		protected QueryOtherMultiErr(ASN1Enum<MultiSyntaxError> er) {
			m_err = er;
		}

		/** Query an other MULTI error */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(o_err);
			try {
				mess.queryProps();
				logQuery(o_err);
				if (isGraphicError() && graphics.hasNext())
					return new QueryGraphicsConfig();
				setErrorStatus(o_err.toString());
			}
			catch (NoSuchName e) {
				// For 1203v1, dmsMultiOtherErrorDescription
				// had not been defined...
				setErrorStatus(m_err.toString());
			}
			return null;
		}

		/** Check if 'other' error is a graphic error */
		private boolean isGraphicError() {
			// NOTE: there is no standard MultiSyntaxError defined
			//       for graphics which do not fit, similar to
			//       textTooBig.  Ledstar, for one, sets
			//	 dmsMultiSyntaxError to 'other' and
			//	 dmsMultiOtherErrorDescription to
			//	 "Graphic off right edge of sign".
			//	 Let's just check if "graphic" is in the string.
			return o_err.toString().toLowerCase().contains(
				"graphic");
		}
	}

	/** Phase to query a ledstar activate message error */
	protected class QueryLedstarActivateErr extends Phase {

		/** Query a Ledstar activate message error */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Flags<LedActivateMsgError> error = new ASN1Flags<
				LedActivateMsgError>(LedActivateMsgError.class,
				ledActivateMsgError.node);
			mess.add(error);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// must not be a Ledstar sign ...
				return null;
			}
			logQuery(error);
			setErrorStatus(error.toString());
			return null;
		}
	}

	/** Phase to set the comm and power loss messages */
	protected class SetLossMsgs extends Phase {

		/** Set the comm and power loss messages */
		@SuppressWarnings("unchecked")
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
	private boolean isScheduledIndefinite() {
		int src = message.getSource();
		return SignMsgSource.schedule.checkBit(src) &&
		      (message.getDuration() == null) &&
		      !SignMsgSource.operator.checkBit(src);
	}

	/** Set the comm loss and power recovery msgs */
	private void setCommAndPower() {
		comm_msg.setMemoryType(DmsMessageMemoryType.changeable);
		comm_msg.setNumber(msg_num);
		comm_msg.setCrc(message_crc);
		long_msg.setMemoryType(DmsMessageMemoryType.changeable);
		long_msg.setNumber(msg_num);
		long_msg.setCrc(message_crc);
	}

	/** Set the comm loss and power recovery msgs to blank */
	private void setCommAndPowerBlank() {
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
		dms.setMsgNext(null);
		super.cleanup();
	}

	/** Phase to query the graphics configuration */
	private class QueryGraphicsConfig extends Phase {

		/** Query the graphics configuration */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(color_scheme);
			mess.add(max_graphics);
			mess.add(block_size);
			mess.queryProps();
			logQuery(color_scheme);
			logQuery(max_graphics);
			logQuery(block_size);
			initGraphicStatus();
			return nextGraphicPhase();
		}
	}

	/** Get the first phase of the next graphic */
	private Phase nextGraphicPhase() {
		if (graphics.hasNext()) {
			Graphic g = graphics.next();
			String e = checkGraphic(g);
			if (e != null) {
				setErrorStatus(e);
				return null;
			}
			return new FindGraphicNumber(g);
		} else {
			/* If the message has already been validated,
			 * we can activate it now. */
			if (msg_validated)
				return new ActivateMsg();
			else
				return new MsgModifyReq();
		}
	}

	/** Test if a graphic should be sent to the DMS */
	private String checkGraphic(Graphic g) {
		Integer g_num = g.getGNumber();
		if (null == g_num || g_num < 1 || g_num > 255)
			return "Invalid graphic number";
		int bpp = color_scheme.getEnum().bpp;
		if (g.getBpp() != 1 && g.getBpp() != bpp)
			return "Invalid graphic depth";
		SignConfig sc = dms.getSignConfig();
		if (null == sc)
			return "Unknown DMS dimensions";
		int w = sc.getPixelWidth();
		int h = sc.getPixelHeight();
		if (g.getWidth() > w || g.getHeight() > h)
			return "Invalid graphic size";
		return null;
	}

	/** Initialize the graphic status list */
	private void initGraphicStatus() {
		g_stat.clear();
		for (int i = 0; i < max_graphics.getInteger(); i++) {
			ASN1Enum<DmsGraphicStatus> e = makeGStatus(i + 1);
			e.setEnum(DmsGraphicStatus.undefined);
			g_stat.add(e);
		}
	}

	/** Set the status of one graphic row.  Once a row has been set to
	 * readyForUseReq, it will not be set to another status.
	 * @param row Row in graphic table.
	 * @param st Graphic status. */
	private void setGraphicStatus(int row, DmsGraphicStatus st) {
		ASN1Enum<DmsGraphicStatus> e = g_stat.get(row - 1);
		if (e.getEnum() != DmsGraphicStatus.readyForUseReq)
			e.setEnum(st);
	}

	/** Check if a graphic row is available */
	private boolean isGraphicAvailable(int row) {
		ASN1Enum<DmsGraphicStatus> e = g_stat.get(row - 1);
		switch (e.getEnum()) {
		case notUsed:
		case modifying:
			return true;
		default:
			return false;
		}
	}

	/** Check if a graphic row is ready for use */
	private boolean isGraphicReady(int row) {
		ASN1Enum<DmsGraphicStatus> e = g_stat.get(row - 1);
		return e.getEnum() == DmsGraphicStatus.readyForUse;
	}

	/** Get the last available graphic row.  If no rows are available,
	 * get the last usable graphic row. */
	private int getAvailableGraphic() {
		int mg = max_graphics.getInteger();
		for (int i = mg; i > 0; i--) {
			if (isGraphicAvailable(i))
				return i;
		}
		for (int i = mg; i > 0; i--) {
			if (isGraphicReady(i))
				return i;
		}
		return 0;
	}

	/** Phase to find row with specific graphic number.  Starting with the
	 * row which equals the number, search in reverse order. */
	private class FindGraphicNumber extends Phase {
		private final Graphic graphic;
		private final int g_num;
		private int row;
		private FindGraphicNumber(Graphic g) {
			graphic = g;
			Integer gn = graphic.getGNumber();
			g_num = (gn != null) ? gn : 0;
			row = grow(g_num);
		}
		private int grow(int i) {
			int mg = max_graphics.getInteger();
			return (i > 0) ? Math.min(i, mg) : mg;
		}

		/** Query the graphic number for one graphic */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			if (g_num < 1) {
				setErrorStatus("Bad graphic #: " + g_num);
				return null;
			}
			ASN1Integer number = dmsGraphicNumber.makeInt(row);
			ASN1Enum<DmsGraphicStatus> status = makeGStatus(row);
			mess.add(number);
			mess.add(status);
			mess.queryProps();
			logQuery(number);
			logQuery(status);
			int gn = number.getInteger();
			if (gn == g_num)
				return new CheckGraphic(graphic, row, status);
			setGraphicStatus(row, status.getEnum());
			// Check previous row
			row = grow(row - 1);
			if (row != grow(g_num))
				return this;
			// No graphic with dmsGraphicNumber equal to g_num
			// was found -- just pick an available row
			row = getAvailableGraphic();
			if (row > 0) {
				return new CheckGraphic(graphic, row,
					g_stat.get(row - 1));
			} else {
				setErrorStatus("Graphic table full");
				return null;
			}
		}
	}

	/** Phase to check a graphic */
	private class CheckGraphic extends Phase {
		private final Graphic graphic;
		private final int row;
		private final ASN1Enum<DmsGraphicStatus> status;
		private CheckGraphic(Graphic g, int r,
			ASN1Enum<DmsGraphicStatus> s)
		{
			graphic = g;
			row = r;
			status = s;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer gid = dmsGraphicID.makeInt(row);
			mess.add(gid);
			mess.queryProps();
			logQuery(gid);
			if (isIDCorrect(graphic, gid.getInteger()))
				return nextGraphicPhase();
			switch (status.getEnum()) {
			case modifying:
			case calculatingID:
			case readyForUse:
				return new SetGraphicNotUsed(graphic, row);
			case notUsed:
				return new SetGraphicModifying(graphic, row);
			default:
				setErrorStatus(status.toString());
				return null;
			}
		}
	}

	/** Phase to set a graphic to notUsed status */
	private class SetGraphicNotUsed extends Phase {
		private final Graphic graphic;
		private final int row;
		private SetGraphicNotUsed(Graphic g, int r) {
			graphic = g;
			row = r;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsGraphicStatus> status = makeGStatus(row);
			status.setEnum(DmsGraphicStatus.notUsedReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new SetGraphicModifying(graphic, row);
		}
	}

	/** Phase to set a graphic to modifying status */
	private class SetGraphicModifying extends Phase {
		private final Graphic graphic;
		private final int row;
		private SetGraphicModifying(Graphic g, int r) {
			graphic = g;
			row = r;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsGraphicStatus> status = makeGStatus(row);
			status.setEnum(DmsGraphicStatus.modifyReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyGraphicModifying(graphic, row);
		}
	}

	/** Phase to verify the graphic status is modifying */
	private class VerifyGraphicModifying extends Phase {
		private final Graphic graphic;
		private final int row;
		private VerifyGraphicModifying(Graphic g, int r) {
			graphic = g;
			row = r;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsGraphicStatus> status = makeGStatus(row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			if (status.getEnum() != DmsGraphicStatus.modifying) {
				setErrorStatus(status.toString());
				return null;
			}
			return new CreateGraphic(graphic, row);
		}
	}

	/** Phase to create a graphic */
	private class CreateGraphic extends Phase {
		private final Graphic graphic;
		private final int row;
		private CreateGraphic(Graphic g, int r) {
			graphic = g;
			row = r;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer number = dmsGraphicNumber.makeInt(row);
			ASN1String name = new ASN1String(dmsGraphicName.node,
				row);
			ASN1Integer height = dmsGraphicHeight.makeInt(row);
			ASN1Integer width = dmsGraphicWidth.makeInt(row);
			ASN1Enum<DmsColorScheme> type = new ASN1Enum<
				DmsColorScheme>(DmsColorScheme.class,
				dmsGraphicType.node, row);
			ASN1Integer trans_enabled =
				dmsGraphicTransparentEnabled.makeInt(row);
			ASN1OctetString trans_color = new ASN1OctetString(
				dmsGraphicTransparentColor.node, row);
			number.setInteger(graphic.getGNumber());
			name.setString(graphic.getName());
			height.setInteger(graphic.getHeight());
			width.setInteger(graphic.getWidth());
			type.setEnum(DmsColorScheme.fromBpp(graphic.getBpp()));
			trans_enabled.setInteger(1);
			if (graphic.getBpp() == 24) {
				trans_color.setOctetString(
					new byte[] { 0, 0, 0 });
			} else
				trans_color.setOctetString(new byte[] { 0 });
			mess.add(number);
			mess.add(name);
			mess.add(height);
			mess.add(width);
			mess.add(type);
			mess.add(trans_enabled);
			mess.add(trans_color);
			logStore(number);
			logStore(name);
			logStore(height);
			logStore(width);
			logStore(type);
			logStore(trans_enabled);
			logStore(trans_color);
			mess.storeProps();
			return new SendGraphicBlock(graphic, row);
		}
	}

	/** Phase to send a block of a graphic */
	private class SendGraphicBlock extends Phase {
		private final Graphic graphic;
		private final int row;
		private final byte[] bitmap;
		private int block;

		/** Create a phase to send graphic blocks */
		private SendGraphicBlock(Graphic g, int r) throws IOException {
			graphic = g;
			row = r;
			bitmap = Base64.decode(g.getPixels());
			block = 1;
		}

		/** Send a graphic block */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1OctetString block_bitmap = new ASN1OctetString(
				dmsGraphicBlockBitmap.node, row, block);
			block_bitmap.setOctetString(createBlock());
			mess.add(block_bitmap);
			logStore(block_bitmap);
			mess.storeProps();
			if (block * block_size.getInteger() < bitmap.length) {
				block++;
				if (block % 20 == 0 && !controller.isFailed())
					setSuccess(true);
				return this;
			} else
				return new ValidateGraphic(graphic, row);
		}

		/** Create a graphic block */
		private byte[] createBlock() {
			int bsize = block_size.getInteger();
			int pos = (block - 1) * bsize;
			int blen = Math.min(bsize, bitmap.length - pos);
			byte[] bdata = new byte[blen];
			System.arraycopy(bitmap, pos, bdata, 0, blen);
			return bdata;
		}
	}

	/** Phase to validate the graphic */
	private class ValidateGraphic extends Phase {
		private final Graphic graphic;
		private final int row;
		private ValidateGraphic(Graphic g, int r) {
			graphic = g;
			row = r;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsGraphicStatus> status = makeGStatus(row);
			status.setEnum(DmsGraphicStatus.readyForUseReq);
			mess.add(status);
			logStore(status);
			mess.storeProps();
			return new VerifyGraphicReady(graphic, row);
		}
	}

	/** Phase to verify the graphic status is ready for use */
	private class VerifyGraphicReady extends Phase {

		/** Time to stop checking if the graphic is ready for use */
		private final long expire = TimeSteward.currentTimeMillis() +
			10 * 1000;

		private final Graphic graphic;
		private final int row;
		private VerifyGraphicReady(Graphic g, int r) {
			graphic = g;
			row = r;
		}

		/** Verify the graphic status is ready for use */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsGraphicStatus> status = makeGStatus(row);
			mess.add(status);
			mess.queryProps();
			logQuery(status);
			if (status.getEnum() == DmsGraphicStatus.readyForUse)
				return new VerifyGraphicID(graphic, row);
			if (TimeSteward.currentTimeMillis() < expire)
				return this;
			else {
				setErrorStatus("Graphic not ready: " + status);
				return null;
			}
		}
	}

	/** Phase to verify a graphic ID */
	private class VerifyGraphicID extends Phase {
		private final Graphic graphic;
		private final int row;
		private VerifyGraphicID(Graphic g, int r) {
			graphic = g;
			row = r;
		}
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer gid = dmsGraphicID.makeInt(row);
			mess.add(gid);
			mess.queryProps();
			logQuery(gid);
			if (!isIDCorrect(graphic, gid.getInteger())) {
				setErrorStatus("Graphic ID incorrect");
				return null;
			}
			setGraphicStatus(row, DmsGraphicStatus.readyForUseReq);
			return nextGraphicPhase();
		}
	}

	/** Compare the graphic ID */
	private boolean isIDCorrect(Graphic graphic, int g) throws IOException {
		GraphicInfoList gil = new GraphicInfoList(graphic);
		return g == gil.getCrcSwapped();
	}
}
