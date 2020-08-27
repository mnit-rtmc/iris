/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.server.comm.axisptz.AxisPTZPoller;
import us.mn.state.dot.tms.server.comm.canoga.CanogaPoller;
import us.mn.state.dot.tms.server.comm.cbw.CBWPoller;
import us.mn.state.dot.tms.server.comm.cohuptz.CohuPTZPoller;
import us.mn.state.dot.tms.server.comm.dinrelay.DinRelayPoller;
import us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller;
import us.mn.state.dot.tms.server.comm.dr500.DR500Poller;
import us.mn.state.dot.tms.server.comm.dxm.DXMPoller;
import us.mn.state.dot.tms.server.comm.e6.E6Poller;
import us.mn.state.dot.tms.server.comm.g4.G4Poller;
import us.mn.state.dot.tms.server.comm.redlion.RedLionPoller;
import us.mn.state.dot.tms.server.comm.incfeed.IncFeedPoller;
import us.mn.state.dot.tms.server.comm.infinova.InfinovaPoller;
import us.mn.state.dot.tms.server.comm.manchester.ManchesterPoller;
import us.mn.state.dot.tms.server.comm.mndot.MndotPoller;
import us.mn.state.dot.tms.server.comm.monstream.MonStreamPoller;
import us.mn.state.dot.tms.server.comm.msgfeed.MsgFeedPoller;
import us.mn.state.dot.tms.server.comm.ntcip.NtcipPoller;
import us.mn.state.dot.tms.server.comm.org815.Org815Poller;
import us.mn.state.dot.tms.server.comm.pelcod.PelcoDPoller;
import us.mn.state.dot.tms.server.comm.pelcop.PelcoPPoller;
import us.mn.state.dot.tms.server.comm.sierragx.SierraGxPoller;
import us.mn.state.dot.tms.server.comm.ss105.SS105Poller;
import us.mn.state.dot.tms.server.comm.ss125.SS125Poller;
import us.mn.state.dot.tms.server.comm.stc.STCPoller;
import us.mn.state.dot.tms.server.comm.streambed.StreambedPoller;
import us.mn.state.dot.tms.server.comm.viconptz.ViconPTZPoller;

/**
 * A factory for creating device poller objects.
 *
 * @author Douglas Lau
 */
public class DevicePollerFactory {

	/** Create a device poller */
	static public DevicePoller create(String name, CommProtocol protocol,
		int ids)
	{
		switch (protocol) {
		case AXIS_PTZ:
			return new AxisPTZPoller(name, ids);
		case BANNER_DXM:
			return new DXMPoller(name, ids);
		case CANOGA:
			return new CanogaPoller(name, ids);
		case CBW:
			return new CBWPoller(name, ids);
		case COHU_PTZ:
			return new CohuPTZPoller(name, protocol, ids);
		case DIN_RELAY:
			return new DinRelayPoller(name, ids);
		case DMSXML:
			return new DmsXmlPoller(name, ids);
		case DR_500:
			return new DR500Poller(name, ids);
		case HYSECURITY_STC:
			return new STCPoller(name, ids);
		case INC_FEED:
			return new IncFeedPoller(name, ids);
		case INFINOVA_D_PTZ:
			return new InfinovaPoller(name, ids);
		case MANCHESTER_PTZ:
			return new ManchesterPoller(name, ids);
		case MNDOT_4:
		case MNDOT_5:
			return new MndotPoller(name, protocol, ids);
		case MON_STREAM:
			return new MonStreamPoller(name, ids);
		case MSG_FEED:
			return new MsgFeedPoller(name, ids);
		case NTCIP_A:
		case NTCIP_B:
		case NTCIP_C:
			return new NtcipPoller(name, protocol, ids);
		case ORG_815:
			return new Org815Poller(name, ids);
		case PELCO_D_PTZ:
			return new PelcoDPoller(name, ids);
		case PELCO_P:
			return new PelcoPPoller(name, ids);
		case RTMS_G4:
			return new G4Poller(name, ids);
		case SS_105:
			return new SS105Poller(name, ids);
		case SS_125:
			return new SS125Poller(name, ids);
		case TRANSCORE_E6:
			return new E6Poller(name, ids);
		case VICON_PTZ:
			return new ViconPTZPoller(name, ids);
		case SIERRA_GX:
			return new SierraGxPoller(name, ids);
		case GPS_REDLION:
			return new RedLionPoller(name, ids);
		case COHU_HELIOS_PTZ:
			return new CohuPTZPoller(name, protocol, ids);
		case STREAMBED:
			return new StreambedPoller(name, ids);
		default:
			return null;
		}
	}

	/** Don't allow instantiation */
	private DevicePollerFactory() { }
}
