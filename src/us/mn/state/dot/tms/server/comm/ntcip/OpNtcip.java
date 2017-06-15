/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
import java.util.Iterator;
import java.util.regex.Pattern;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LaneUseMulti;
import us.mn.state.dot.tms.LaneUseMultiHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.GraphicInfoList;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Object;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Operation for NTCIP device.
 *
 * @author Douglas Lau
 */
abstract public class OpNtcip extends OpDevice {

	/** NTCIP debug log */
	static private final DebugLog NTCIP_LOG = new DebugLog("ntcip");

	/** Lookup a sign message number */
	static protected int lookupMsgNum(String ms) {
		LaneUseMulti lum = findLaneUseMulti(ms);
		if (lum != null) {
			Integer msg_num = lum.getMsgNum();
			if (msg_num != null)
				return msg_num;
		}
		return 1;
	}

	/** Lookup an LCS indication on a sign message */
	static protected Integer lookupIndication(SignMessage sm) {
		String m = sm.getMulti();
		MultiString ms = new MultiString(m);
		if (ms.isBlank())
			return LaneUseIndication.DARK.ordinal();
		LaneUseMulti lum = findLaneUseMulti(parseMulti(m));
		if (lum != null)
			return lum.getIndication();
		else
			return null;
	}

	/** Find a lane-use MULTI which matches a MULTI string */
	static private LaneUseMulti findLaneUseMulti(String multi) {
		Iterator<LaneUseMulti> it = LaneUseMultiHelper.iterator();
		while (it.hasNext()) {
			LaneUseMulti lum = it.next();
			QuickMessage qm = lum.getQuickMessage();
			if (qm != null && match(qm, multi))
				return lum;
		}
		return null;
	}

	/** Test if a quick message matches a multi string.
	 * @param qm Quick message.
	 * @param multi MULTI string to compare.
	 * @return true if they match. */
	static private boolean match(QuickMessage qm, String multi) {
		String re = createRegex(parseMulti(qm.getMulti()));
		return Pattern.matches(re, multi);
	}

	/** Create a regex which matches any speed advisory values */
	static private String createRegex(String qm) {
		MultiBuilder re = new MultiBuilder() {
			@Override
			public void addSpeedAdvisory() {
				// Add unquoted regex to match 2 digits
				addSpan("\\E[0-9].\\Q");
			}
		};
		// Start quoting for regex
		re.addSpan("\\Q");
		new MultiString(qm).parse(re);
		// End quoting for regex
		re.addSpan("\\E");
		return re.toString();
	}

	/** Parse a MULTI string and add graphic version IDs.
	 * @param ms Original MULTI string.
	 * @return MULTI string with graphic IDs added. */
	static protected String parseMulti(String ms) {
		MultiBuilder mb = new MultiBuilder() {
			@Override
			public void addGraphic(int g_num, Integer x, Integer y,
				String g_id)
			{
				if (null == g_id) {
					g_id = calculateGraphicID(g_num);
					if (g_id != null) {
						x = (x != null) ? x : 1;
						y = (y != null) ? y : 1;
					}
				}
				super.addGraphic(g_num, x, y, g_id);
			}
		};
		new MultiString(ms).parse(mb);
		return mb.toString();
	}

	/** Calculate the graphic ID.
	 * @param g_num Graphic number.
	 * @return 4-digit hexadecimal graphic ID, or null. */
	static private String calculateGraphicID(int g_num) {
		Graphic g = GraphicHelper.find(g_num);
		if (g != null) {
			try {
				GraphicInfoList gil = new GraphicInfoList(g);
				int gid = gil.getCrcSwapped();
				return HexString.format(gid, 4);
			}
			catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	/** Log a msg */
	protected void log(String msg) {
		if (NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + ": " + msg);
	}

	/** Log an error msg */
	protected void logError(String msg) {
		if (NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + "! " + msg);
	}

	/** Log a property query */
	protected void logQuery(ASN1Object prop) {
		if (NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + ": " + prop);
	}

	/** Log a property store */
	protected void logStore(ASN1Object prop) {
		if (NTCIP_LOG.isOpen())
			NTCIP_LOG.log(device.getName() + ":= " + prop);
	}

	/** Create a new NTCIP operation */
	protected OpNtcip(PriorityLevel p, DeviceImpl d) {
		super(p, d);
	}

	/** Set the error status message.  If non-null, the controller "error"
	 * attribute is set to this message when the operation completes. */
	@Override
	public void setErrorStatus(String s) {
		if (s != null && s.length() > 0)
			logError(s);
		super.setErrorStatus(s);
	}
}
