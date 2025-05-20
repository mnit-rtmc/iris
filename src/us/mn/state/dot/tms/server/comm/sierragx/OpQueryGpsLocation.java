/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
 * Copyright (C) 2018-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.sierragx;

import java.io.IOException;
import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This operation logs into a Sierra Wireless GX GPS modem
 * and then queries the GPS coordinates of the modem.
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
public class OpQueryGpsLocation extends OpDevice<SierraGxProperty> {

	/** GPS device */
	private final GpsImpl gps;

	/** Flag to bypass the jitter filter */
	private final boolean jitter_bypass;

	/** GPS location property */
	private final GpsLocationProperty gps_prop;

	/** Create a new query GPS operation */
	public OpQueryGpsLocation(GpsImpl g, boolean jb) {
		super(PriorityLevel.POLL_LOW, g);
		gps = g;
		jitter_bypass = jb;
		gps_prop = new GpsLocationProperty();
		gps.setLatestPollNotify();
	}

	/** Get the username parsed from controller password as "user:pass" */
	private String getUsername() {
		String p = getController().getPassword();
		if (p != null && p.length() > 0) {
			String[] up = p.split(":", 2);
			return up[0];
		} else
			return null;
	}

	/** Get the password parsed from controller password as "user:pass" */
	private String getPassword() {
		String p = getController().getPassword();
		if (p != null && p.length() > 0) {
			String[] up = p.split(":", 2);
			return (up.length > 1) ? up[1] : null;
		} else
			return null;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<SierraGxProperty> phaseTwo() {
		return (getUsername() != null)
		      ? new CheckNeedLogin()
		      : new QueryGps();
	}

	/** Phase to check if we need to log into the modem */
	private class CheckNeedLogin extends Phase<SierraGxProperty> {

		protected Phase<SierraGxProperty> poll(
			CommMessage<SierraGxProperty> mess) throws IOException
		{
			TestLoginModeProperty prop = new TestLoginModeProperty();
			mess.add(prop);
			mess.queryProps();
			return prop.gotLoginPrompt()
			      ? new SendUsername()
			      : null;
		}
	}

	/** Phase to send username */
	private class SendUsername extends Phase<SierraGxProperty> {

		protected Phase<SierraGxProperty> poll(
			CommMessage<SierraGxProperty> mess) throws IOException
		{
			SendUsernameProperty prop = new SendUsernameProperty(
				getUsername());
			mess.add(prop);
			mess.queryProps();
			if (prop.gotPwPrompt())
				return new SendPassword();
			else
				throw new ControllerException("Login Failed");
		}
	}

	/** Phase to send password */
	private class SendPassword extends Phase<SierraGxProperty> {

		protected Phase<SierraGxProperty> poll(
			CommMessage<SierraGxProperty> mess) throws IOException
		{
			SendPasswordProperty prop = new SendPasswordProperty(
				getPassword());
			mess.add(prop);
			mess.queryProps();
			if (prop.getLoginFinished())
				return new QueryGps();
			else
				throw new ControllerException("Login Failed");
		}
	}

	/** Phase to query GPS location */
	private class QueryGps extends Phase<SierraGxProperty> {

		protected Phase<SierraGxProperty> poll(
			CommMessage<SierraGxProperty> mess) throws IOException
		{
			mess.add(gps_prop);
			mess.queryProps();
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			updateGpsLocation();
		super.cleanup();
	}

	/** Update the GPS location */
	private void updateGpsLocation() {
		if (gps_prop.gotValidResponse()) {
			if (gps_prop.gotGpsLock()) {
				gps.saveDeviceLocation(gps_prop.getLat(),
					gps_prop.getLon(), jitter_bypass);
			} else
				putCtrlFaults("gps", "No GPS Lock");
		}
	}
}
