/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2021  Minnesota Department of Transportation
 * Copyright (C) 2015-2021  SRF Consulting Group
 * Copyright (C) 2012-2021  Iteris Inc.
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

import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommLinkHelper;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.GateStyle;
import us.mn.state.dot.tms.server.comm.axisptz.AxisPTZPoller;
import us.mn.state.dot.tms.server.comm.canoga.CanogaPoller;
import us.mn.state.dot.tms.server.comm.cap.CapPoller;
import us.mn.state.dot.tms.server.comm.cbw.CBWPoller;
import us.mn.state.dot.tms.server.comm.clearguide.ClearGuidePoller;
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
import us.mn.state.dot.tms.server.comm.ndorv5.GateNdorV5Poller;
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
	static public DevicePoller create(CommLink link) {
		CommProtocol protocol = CommLinkHelper.getProtocol(link);
		switch (protocol) {
		case AXIS_PTZ:
			return new AxisPTZPoller(link);
		case BANNER_DXM:
			return new DXMPoller(link);
		case CANOGA:
			return new CanogaPoller(link);
		case CBW:
			return new CBWPoller(link);
		case COHU_PTZ:
			return new CohuPTZPoller(link, protocol);
		case DIN_RELAY:
			return new DinRelayPoller(link);
		case DMSXML:
			return new DmsXmlPoller(link);
		case DR_500:
			return new DR500Poller(link);
		case HYSECURITY_STC:
			if (GateStyle.isMnDOT())
				return new STCPoller(link);
			else
				return null;
		case INC_FEED:
			return new IncFeedPoller(link);
		case INFINOVA_D_PTZ:
			return new InfinovaPoller(link);
		case MANCHESTER_PTZ:
			return new ManchesterPoller(link);
		case MNDOT_4:
		case MNDOT_5:
			return new MndotPoller(link, protocol);
		case MON_STREAM:
			return new MonStreamPoller(link);
		case MSG_FEED:
			return new MsgFeedPoller(link);
		case NTCIP_A:
		case NTCIP_B:
		case NTCIP_C:
			return new NtcipPoller(link, protocol);
		case ORG_815:
			return new Org815Poller(link);
		case PELCO_D_PTZ:
			return new PelcoDPoller(link);
		case PELCO_P:
			return new PelcoPPoller(link);
		case RTMS_G4:
		case RTMS_G4_VLOG:
			return new G4Poller(link, protocol);
		case SS_105:
			return new SS105Poller(link);
		case SS_125:
		case SS_125_VLOG:
			return new SS125Poller(link, protocol);
		case TRANSCORE_E6:
			return new E6Poller(link);
		case VICON_PTZ:
			return new ViconPTZPoller(link);
		case GATE_NDOR5:
			if (GateStyle.isNDOT())
				return new GateNdorV5Poller(link);
			else
				return null;
		case SIERRA_GX:
			return new SierraGxPoller(link);
		case GPS_REDLION:
			return new RedLionPoller(link);
		case COHU_HELIOS_PTZ:
			return new CohuPTZPoller(link, protocol);
		case STREAMBED:
			return new StreambedPoller(link);
		case CAP:
			return new CapPoller(link);
		case CLEARGUIDE:
			return new ClearGuidePoller(link);
		default:
			return null;
		}
	}

	/** Don't allow instantiation */
	private DevicePollerFactory() { }
}
