/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2020  Minnesota Department of Transportation
 * Copyright (C) 2015-2017  SRF Consulting Group
 * Copyright (C) 2017       Iteris Inc.
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

import java.net.URI;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GpsHelper;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.GeoLocImpl;
import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.DMSPoller;
import us.mn.state.dot.tms.server.comm.GpsPoller;
import us.mn.state.dot.tms.server.comm.LCSPoller;
import us.mn.state.dot.tms.server.comm.SamplePoller;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;
import us.mn.state.dot.tms.server.comm.WeatherPoller;
import us.mn.state.dot.tms.utils.URIUtil;

/**
 * NTCIP Poller
 *
 * @author Douglas Lau
 * @author John L. Stanley
 * @author Michael Darter
 */
public class NtcipPoller extends ThreadedPoller implements DMSPoller, GpsPoller,
	LCSPoller, SamplePoller, WeatherPoller
{
	/** Get the default URI for a comm protocol */
	static private URI default_uri(CommProtocol cp) {
		if (cp == CommProtocol.NTCIP_A)
			return URIUtil.UDP;
		else
			return URIUtil.TCP;
	}

	/** NTCIP debug log */
	static private final DebugLog NTCIP_LOG = new DebugLog("ntcip2");

	/** Communication protocol */
	private final CommProtocol protocol;

	/** Create a new Ntcip poller */
	public NtcipPoller(CommLink link, CommProtocol cp) {
		super(link, default_uri(cp), NTCIP_LOG);
		protocol = cp;
	}

	/** Create a comm thread */
	@Override
	protected NtcipThread createCommThread(String uri, int timeout,
		int nrd)
	{
		return new NtcipThread(this, queue, scheme, uri, timeout, nrd,
			NTCIP_LOG, protocol);
	}

	/** Send a device request message to the sign */
	@SuppressWarnings("unchecked")
	@Override
	public void sendRequest(DMSImpl dms, DeviceRequest r) {
		switch (r) {
		case RESET_DEVICE:
			addOp(new OpResetDMS(dms));
			break;
		case QUERY_SETTINGS:
			addOp(new OpQueryDMSFonts(dms));
			break;
		case SEND_SETTINGS:
			if (SystemAttrEnum.DMS_UPDATE_FONT_TABLE.getBoolean())
				addOp(new OpSendDMSFonts(dms));
			addOp(new OpSendDMSDefaults(dms));
			break;
		case QUERY_CONFIGURATION:
			addOp(new OpQueryDMSConfiguration(dms));
			break;
		case QUERY_MESSAGE:
			addOp(new OpQueryDMSMessage(dms));
			break;
		case QUERY_STATUS:
			addOp(new OpQueryDMSStatus(dms));
			break;
		case QUERY_PIXEL_FAILURES:
			addOp(new OpTestDMSPixels(dms, false));
			break;
		case TEST_PIXELS:
			addOp(new OpTestDMSPixels(dms, true));
			break;
		case BRIGHTNESS_TOO_DIM:
			addOp(new OpUpdateDMSBrightness(dms,
				EventType.DMS_BRIGHT_LOW));
			break;
		case BRIGHTNESS_GOOD:
			addOp(new OpUpdateDMSBrightness(dms,
				EventType.DMS_BRIGHT_GOOD));
			break;
		case BRIGHTNESS_TOO_BRIGHT:
			addOp(new OpUpdateDMSBrightness(dms,
				EventType.DMS_BRIGHT_HIGH));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send a new message to the sign */
	@SuppressWarnings("unchecked")
	@Override
	public void sendMessage(DMSImpl dms, SignMessage sm, String owner) {
		if (dms.getMsgCurrent() == sm) {
			if (SignMessageHelper.isScheduledExpiring(sm))
				addOp(new OpUpdateDMSDuration(dms, sm));
		} else
			addOp(new OpSendDMSMessage(dms, sm, owner));
	}

	/** Send a request to the GPS */
	@SuppressWarnings("unchecked")
	@Override
	public void sendRequest(GpsImpl gps, DeviceRequest r) {
		switch (r) {
		case QUERY_GPS_LOCATION:
			GeoLoc loc = GpsHelper.lookupDeviceLoc(gps);
			if (loc instanceof GeoLocImpl) {
				addOp(new OpQueryGpsLocationNtcip(gps,
					(GeoLocImpl) loc));
			}
			break;
		default:
			; // Ignore other requests
		}
	}

	/** Send a device request message to an LCS array */
	@SuppressWarnings("unchecked")
	@Override
	public void sendRequest(LCSArrayImpl lcs_array, DeviceRequest r) {
		switch (r) {
		case SEND_SETTINGS:
			addOp(new OpSendLCSSettings(lcs_array));
			break;
		case QUERY_MESSAGE:
			addOp(new OpQueryLCSIndications(lcs_array));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Send new indications to an LCS array.
	 * @param lcs_array LCS array.
	 * @param ind New lane use indications.
	 * @param o User who deployed the indications. */
	@SuppressWarnings("unchecked")
	@Override
	public void sendIndications(LCSArrayImpl lcs_array, Integer[] ind,
		User o)
	{
		addOp(new OpSendLCSIndications(lcs_array, ind, o));
	}

	/** Reset controller.
	 * @param c Controller to poll. */
	@Override
	public void resetController(ControllerImpl c) {
		// FIXME
	}

	/** Send sample settings to a controller.
	 * @param c Controller to poll. */
	@Override
	public void sendSettings(ControllerImpl c) {
		// FIXME
	}

	/** Query sample data.
 	 * @param c Controller to poll.
 	 * @param p Sample period in seconds. */
	@SuppressWarnings("unchecked")
	@Override
	public void querySamples(ControllerImpl c, int p) {
		// Don't query samples on 5 minute poll
		if (c.getPollPeriodSec() == p)
			addOp(new OpQuerySamples(c, p));
	}

	/** Send a device request to a weather sensor */
	@SuppressWarnings("unchecked")
	@Override
	public void sendRequest(WeatherSensorImpl ws, DeviceRequest r) {
		switch (r) {
		case QUERY_STATUS:
			addOp(new OpQueryEssStatus(ws));
			break;
		case SEND_SETTINGS:
			addOp(new OpQueryEssSettings(ws));
			break;
		default:
			// Ignore other requests
			break;
		}
	}

	/** Start communication test */
	@SuppressWarnings("unchecked")
	@Override
	public void startTesting(ControllerImpl c) {
		addOp(new OpTestComm(c, NTCIP_LOG));
	}
}
