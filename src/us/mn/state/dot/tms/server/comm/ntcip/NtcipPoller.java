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

import java.io.EOFException;
import java.util.LinkedList;
import java.util.Map;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.LaneUseGraphic;
import us.mn.state.dot.tms.LaneUseGraphicHelper;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.Messenger;

/**
 * NtcipPoller
 *
 * @author Douglas Lau
 */
public class NtcipPoller extends MessagePoller implements DMSPoller, LCSPoller {

	/** SNMP message protocol */
	protected final SNMP snmp = new SNMP();

	/** Create a new Ntcip poller */
	public NtcipPoller(String n, Messenger m) {
		super(n, m);
	}

	/** Create a new message for the specified controller */
	public AddressedMessage createMessage(ControllerImpl c)
		throws EOFException
	{
		return snmp.new Message(messenger.getOutputStream(c),
			messenger.getInputStream(c));
	}

	/** Check if a drop address is valid */
	public boolean isAddressValid(int drop) {
		// FIXME: this doesn't belong here
		return drop > 0 && drop <= HDLC.NTCIP_MAX_ADDRESS;
	}

	/** Send a device request message to the sign */
	public void sendRequest(DMSImpl dms, DeviceRequest r) {
		switch(r) {
		case RESET_DEVICE:
			new OpResetDMS(dms).start();
			break;
		case SEND_SETTINGS:
			new OpSendDMSFonts(dms).start();
			new OpSendDMSDefaults(dms).start();
			break;
		case QUERY_CONFIGURATION:
			new OpQueryDMSConfiguration(dms).start();
			break;
		case QUERY_MESSAGE:
			new OpQueryDMSMessage(dms).start();
			break;
		case QUERY_STATUS:
			new OpQueryDMSStatus(dms).start();
			break;
		case QUERY_PIXEL_FAILURES:
			new OpTestDMSPixels(dms, false).start();
			break;
		case TEST_PIXELS:
			new OpTestDMSPixels(dms, true).start();
			break;
		case TEST_LAMPS:
			new OpTestDMSLamps(dms).start();
			break;
		case BRIGHTNESS_GOOD:
		case BRIGHTNESS_TOO_DIM:
		case BRIGHTNESS_TOO_BRIGHT:
			new OpUpdateDMSBrightness(dms, r).start();
			break;
		case SEND_LEDSTAR_SETTINGS:
			new OpSendDMSLedstar(dms).start();
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send a new message to the sign */
	public void sendMessage(DMSImpl dms, SignMessage m, User o)
		throws InvalidMessageException
	{
		if(shouldSetTimeRemaining(dms, m))
			new OpUpdateDMSDuration(dms, m, o).start();
		else
			new OpSendDMSMessage(dms, m, o).start();
	}

	/** Check if we should just set the message time remaining */
	protected boolean shouldSetTimeRemaining(DMSImpl dms, SignMessage m) {
		return SignMessageHelper.isDurationZero(m) ||
		       isMessageDeployed(dms, m);
	}

	/** Check if the message is already deployed on the sign */
	protected boolean isMessageDeployed(DMSImpl dms, SignMessage m) {
		return m.getMulti().equals(dms.getMessageCurrent().getMulti());
	}

	/** Send a device request message to an LCS array */
	public void sendRequest(LCSArrayImpl lcs_array, DeviceRequest r) {
		switch(r) {
		case SEND_SETTINGS:
			sendLCSSettings(lcs_array);
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send settings to an array of LCS devices */
	protected void sendLCSSettings(LCSArrayImpl lcs_array) {
		Map<String, Integer> gm = LaneUseGraphicHelper.createMapping();
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		for(LCS lcs: lcss) {
			DMS dms = DMSHelper.lookup(lcs.getName());
			if(dms instanceof DMSImpl)
				new OpSendDMSGraphics((DMSImpl)dms, gm).start();
		}
	}

	/** Send new indications to an LCS array.
	 * @param lcs_array LCS array.
	 * @param ind New lane use indications.
	 * @param o User who deployed the indications. */
	public void sendIndications(LCSArrayImpl lcs_array, Integer[] ind,
		User o)
	{
		LinkedList<OpDMS> ops = new LinkedList<OpDMS>();
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		if(lcss.length != ind.length) {
			System.err.println("NTCIP: LCS array length invalid");
			return;
		}
		for(int i = 0; i < lcss.length; i++) {
			DMS dms = DMSHelper.lookup(lcss[i].getName());
			if(dms instanceof DMSImpl) {
				OpDMS op = createGraphicOperation((DMSImpl)dms,
					ind[i], o);
				if(op != null) {
					ops.add(op);
					continue;
				}
			}
			System.err.println("NTCIP: sendIndications aborted");
			return;
		}
		for(OpDMS op: ops)
			op.start();
	}

	/** Create an operation to set an indication on a DMS */
	protected OpDMS createGraphicOperation(DMSImpl dms, int ind, User o) {
		String ms = createIndicationMulti(dms, ind);
		if(ms != null && ms.length() > 0) {
			try {
				SignMessage sm = dms.createMessage(ms,
					DMSMessagePriority.OPERATOR, null);
				return new OpSendDMSMessage(dms, sm, o);
			}
			catch(SonarException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/** Create a MULTI string for a lane use indication */
	protected String createIndicationMulti(DMS dms, int ind) {
		MultiString ms = new MultiString();
		for(LaneUseGraphic g:
			LaneUseGraphicHelper.getIndicationGraphics(ind))
		{
			if(ms.toString().length() > 0)
				ms.addPage();
			int x = calculateGraphicX(dms, g.getGraphic());
			int y = caluclateGraphicY(dms, g.getGraphic());
			if(x > 0 && y > 0)
				ms.addGraphic(g.getGNumber(), x, y);
		}
		return ms.toString();
	}

	/** Calculate the X position of a graphic */
	static protected int calculateGraphicX(DMS dms, Graphic g) {
		return 1 + (dms.getWidthPixels() - g.getWidth()) / 2;
	}

	/** Calculate the Y position of a graphic */
	static protected int caluclateGraphicY(DMS dms, Graphic g) {
		return 1 + (dms.getHeightPixels() - g.getHeight()) / 2;
	}
}
