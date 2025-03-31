/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.LcsStateHelper;
import us.mn.state.dot.tms.MsgPattern;
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

	/** Test if a message pattern matches a multi string.
	 * @param pat Message pattern.
	 * @param ms MULTI string to compare.
	 * @return true if they match. */
	static protected boolean match(MsgPattern pat, String ms) {
		String re = createRegex(addGraphicIds(pat.getMulti()));
		return Pattern.matches(re, addGraphicIds(ms));
	}

	/** Create a regex which matches any speed advisory values */
	static private String createRegex(String ms) {
		MultiBuilder re = new MultiBuilder() {
			@Override
			public void addSpeedAdvisory() {
				// Add unquoted regex to match 2 digits
				addSpan("\\E[0-9].\\Q");
			}
		};
		// Start quoting for regex
		re.addSpan("\\Q");
		new MultiString(ms).parse(re);
		// End quoting for regex
		re.addSpan("\\E");
		return re.toString();
	}

	/** Parse a MULTI string and add graphic version IDs.
	 * @param ms Original MULTI string.
	 * @return MULTI string with graphic IDs added. */
	static protected String addGraphicIds(String ms) {
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

	/** Put FAULTS into controller status */
	@Override
	protected void putCtrlFaults(String fault, String msg) {
		if (fault != null)
			logError(fault + ": " + msg);
		super.putCtrlFaults(fault, msg);
	}

	/** Get the hardware make */
	protected String getHardwareMake() {
		return ControllerHelper.getSetup(controller, "hw", "make");
	}

	/** Get the hardware model */
	protected String getHardwareModel() {
		return ControllerHelper.getSetup(controller, "hw", "model");
	}

	/** Get the software make */
	protected String getSoftwareMake() {
		return ControllerHelper.getSetup(controller, "sw", "make");
	}

	/** Get the software model */
	protected String getSoftwareModel() {
		return ControllerHelper.getSetup(controller, "sw", "model");
	}

	/** Check if DMS make contains a value.
	 *
	 * NOTE: value must be all lower-case */
	private boolean isMakeContaining(String value) {
		assert value.equals(value.toLowerCase());
		String make = getSoftwareMake();
		return (make != null) && make.toLowerCase().contains(value);
	}

	/** Check if software make is ADDCO */
	protected boolean isAddco() {
		return isMakeContaining("addco");
	}

	/** Check if software make is American Signal */
	protected boolean isAmericanSignal() {
		return isMakeContaining("american signal");
	}

	/** Check if software make is LEDSTAR */
	protected boolean isLedstar() {
		return isMakeContaining("ledstar");
	}

	/** Check if software make is Skyline */
	protected boolean isSkyline() {
		return isMakeContaining("skyline");
	}

	/** Check if software model is Vaisala LX */
	protected boolean isVaisalaLx() {
		return getSoftwareModel().contains("LX");
	}

	/** Get the firmware version */
	protected String getVersion() {
		return ControllerHelper.getSetup(controller, "version");
	}

	/** Lookup a sign message number */
	protected int lookupMsgNum(String ms) {
		Iterator<LcsState> it = LcsStateHelper.iterator();
		while (it.hasNext()) {
			LcsState ls = it.next();
			if (controller == ls.getController()) {
				MsgPattern pat = ls.getMsgPattern();
				if (pat != null && match(pat, ms)) {
					Integer num = ls.getMsgNum();
					if (num != null)
						return num;
				}
			}
		}
		return 1;
	}
}
