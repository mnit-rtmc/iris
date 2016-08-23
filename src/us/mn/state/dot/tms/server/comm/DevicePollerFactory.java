/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.server.comm.addco.AddcoPoller;
import us.mn.state.dot.tms.server.comm.canoga.CanogaPoller;
import us.mn.state.dot.tms.server.comm.cbw.CBWPoller;
import us.mn.state.dot.tms.server.comm.cohuptz.CohuPTZPoller;
import us.mn.state.dot.tms.server.comm.dinrelay.DinRelayPoller;
import us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller;
import us.mn.state.dot.tms.server.comm.dr500.DR500Poller;
import us.mn.state.dot.tms.server.comm.e6.E6Poller;
import us.mn.state.dot.tms.server.comm.g4.G4Poller;
import us.mn.state.dot.tms.server.comm.incfeed.IncFeedPoller;
import us.mn.state.dot.tms.server.comm.infinova.InfinovaPoller;
import us.mn.state.dot.tms.server.comm.manchester.ManchesterPoller;
import us.mn.state.dot.tms.server.comm.mndot.MndotPoller;
import us.mn.state.dot.tms.server.comm.msgfeed.MsgFeedPoller;
import us.mn.state.dot.tms.server.comm.ntcip.NtcipPoller;
import us.mn.state.dot.tms.server.comm.org815.Org815Poller;
import us.mn.state.dot.tms.server.comm.pelco.PelcoPoller;
import us.mn.state.dot.tms.server.comm.pelcod.PelcoDPoller;
import us.mn.state.dot.tms.server.comm.ss105.SS105Poller;
import us.mn.state.dot.tms.server.comm.ss125.SS125Poller;
import us.mn.state.dot.tms.server.comm.ssi.SsiPoller;
import us.mn.state.dot.tms.server.comm.stc.STCPoller;
import us.mn.state.dot.tms.server.comm.viconptz.ViconPTZPoller;

/**
 * A factory for creating device poller objects.
 *
 * @author Douglas Lau
 */
public class DevicePollerFactory {

	/** Create a device poller */
	static public DevicePoller create(String name, CommProtocol protocol) {
		switch (protocol) {
		case ADDCO:
			return new AddcoPoller(name);
		case CANOGA:
			return new CanogaPoller(name);
		case CBW:
			return new CBWPoller(name);
		case COHU_PTZ:
			return new CohuPTZPoller(name);
		case DIN_RELAY:
			return new DinRelayPoller(name);
		case DMSXML:
			return new DmsXmlPoller(name);
		case DR_500:
			return new DR500Poller(name);
		case HYSECURITY_STC:
			return new STCPoller(name);
		case INC_FEED:
			return new IncFeedPoller(name);
		case INFINOVA_D_PTZ:
			return new InfinovaPoller(name);
		case MANCHESTER_PTZ:
			return new ManchesterPoller(name);
		case MNDOT_4:
		case MNDOT_5:
			return new MndotPoller(name, protocol);
		case MSG_FEED:
			return new MsgFeedPoller(name);
		case NTCIP_A:
		case NTCIP_B:
		case NTCIP_C:
			return new NtcipPoller(name, protocol);
		case ORG_815:
			return new Org815Poller(name);
		case PELCO_D_PTZ:
			return new PelcoDPoller(name);
		case PELCO_SWITCHER:
			return new PelcoPoller(name);
		case RTMS_G4:
			return new G4Poller(name);
		case SS_105:
			return new SS105Poller(name);
		case SS_125:
			return new SS125Poller(name);
		case SSI:
			return new SsiPoller(name);
		case TRANSCORE_E6:
			return new E6Poller(name);
		case VICON_PTZ:
			return new ViconPTZPoller(name);
		default:
			return null;
		}
	}

	/** Don't allow instantiation */
	private DevicePollerFactory() { }
}
