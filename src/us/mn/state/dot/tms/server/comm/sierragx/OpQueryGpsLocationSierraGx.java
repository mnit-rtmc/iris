/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
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
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * This operation logs into a Sierra Wireless GX GPS modem
 * and then queries the GPS coordinates of the modem.
 *
 * @author John L. Stanley
 */
public class OpQueryGpsLocationSierraGx extends OpDevice<SierraGxProperty> {

	protected final TestLoginModeProperty propTestLoginNode;
	protected final SendUsernameProperty  propSendUsername;
	protected final SendPasswordProperty  propSendPassword;
	protected final GpsLocationProperty   propQueryGps;

	/** GPS modem to talk to */
	private final GpsImpl gps;

	/** Create a new login object */
	public OpQueryGpsLocationSierraGx(GpsImpl g) {
		super(PriorityLevel.DEVICE_DATA, g);
		gps = g;
		propTestLoginNode = new TestLoginModeProperty();
		propSendUsername  = new SendUsernameProperty(g.getUn());
		propSendPassword  = new SendPasswordProperty(g.getPw());
		propQueryGps      = new GpsLocationProperty();
	}

	//----------------------------------------------

	/** Create the second phase of the operation */
	@Override
	protected Phase<SierraGxProperty> phaseTwo() {
		boolean bUseLogin = (gps.getUn() != null) || (gps.getPw() != null);
		if (bUseLogin)
			return new DoCheckNeedLogin();
		return (propQueryGps == null)
				? null
				: (new DoQueryGps());
	}

	/** Phase to check if we need to log into the modem */
	protected class DoCheckNeedLogin extends Phase<SierraGxProperty> {

		protected Phase poll(CommMessage<SierraGxProperty> mess)
			throws IOException
		{
			mess.add(propTestLoginNode);
			mess.queryProps();
//			if (!isSuccess())
//				return null;  // error exit

			if (propTestLoginNode.gotLoginPrompt()) {
				return new DoSendUsername();
			}

			//FIXME: Find some way to force a disconnect from here...
			return null;
		}
	}

	/** Phase to send username */
	protected class DoSendUsername extends Phase<SierraGxProperty> {

		protected Phase poll(CommMessage<SierraGxProperty> mess)
			throws IOException
		{
			mess.add(propSendUsername);
			mess.queryProps();
			if (!propSendUsername.gotValidResponse() || !isSuccess())
				return null;  // error exit

			if (propSendUsername.gotPwPrompt()) {
				return new DoSendPassword();
			}

			setErrorStatus("Login Failed");
			return null;
		}
	}

	/** Phase to send password */
	protected class DoSendPassword extends Phase<SierraGxProperty> {

		protected Phase poll(CommMessage<SierraGxProperty> mess)
			throws IOException
		{
			mess.add(propSendPassword);
			mess.queryProps();
			if (!propSendPassword.gotValidResponse() || !isSuccess())
				return null;  // error exit

			if (propSendPassword.getLoginFinished())
				return new DoQueryGps();

			setErrorStatus("Login Failed");
			return null;
		}
	}

	/** Phase to query GPS location */
	protected class DoQueryGps extends Phase<SierraGxProperty> {

		@SuppressWarnings({ "unchecked", "rawtypes", "synthetic-access" })
		protected Phase poll(CommMessage<SierraGxProperty> mess)
			throws IOException
		{
			mess.add(propQueryGps);
			mess.queryProps();
			if (!propQueryGps.gotValidResponse() || !isSuccess())
				return null;

			if (!propQueryGps.gotGpsLock()) {
				setErrorStatus("No GPS Lock");
				return null;
			}

			if (addSample(propQueryGps.getLat(), propQueryGps.getLon())) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return this;  // ask for another sample
			}

			filterSamplesAndSave();
			return null;
		}
	}
}
