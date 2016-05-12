/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
 * Copyright (C) 2012  Iteris Inc.
 * Copyright (C) 2014  AHMCT, University of California
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.server.ModemImpl;
import us.mn.state.dot.tms.server.comm.addco.AddcoPoller;
import us.mn.state.dot.tms.server.comm.addco.AddcoMessenger;
import us.mn.state.dot.tms.server.comm.canoga.CanogaPoller;
import us.mn.state.dot.tms.server.comm.cbw.CBWPoller;
import us.mn.state.dot.tms.server.comm.cohuptz.CohuPTZPoller;
import us.mn.state.dot.tms.server.comm.dinrelay.DinRelayPoller;
import us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller;
import us.mn.state.dot.tms.server.comm.dr500.DR500Poller;
import us.mn.state.dot.tms.server.comm.e6.E6Poller;
import us.mn.state.dot.tms.server.comm.g4.G4Poller;
import us.mn.state.dot.tms.server.comm.incfeed.IncFeedPoller;
import us.mn.state.dot.tms.server.comm.infinova.InfinovaMessenger;
import us.mn.state.dot.tms.server.comm.manchester.ManchesterPoller;
import us.mn.state.dot.tms.server.comm.mndot.MndotPoller;
import us.mn.state.dot.tms.server.comm.msgfeed.MsgFeedPoller;
import us.mn.state.dot.tms.server.comm.ntcip.HDLCMessenger;
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
 * @author Michael Darter
 * @author Travis Swanston
 */
public class DevicePollerFactory {

	/** Default URI for UDP sockets */
	static private final String UDP = "udp:/";

	/** Default URI for TCP sockets */
	static private final String TCP = "tcp:/";

	/** Create a device poller */
	static public DevicePoller create(String name, CommProtocol protocol,
		String uri) throws IOException
	{
		DevicePollerFactory factory = new DevicePollerFactory(name,
			protocol, uri);
		return factory.create();
	}

	/** Name of comm link */
	private final String name;

	/** Communication protocol */
	private final CommProtocol protocol;

	/** URI of comm link */
	private final String uri;

	/** Create a new device poller factory */
	private DevicePollerFactory(String n, CommProtocol cp, String u) {
		name = n;
		protocol = cp;
		uri = u;
	}

	/** Create a device poller */
	private DevicePoller create() throws IOException {
		switch (protocol) {
		case NTCIP_A:
			return createNtcipAPoller();
		case NTCIP_B:
			return createNtcipBPoller();
		case NTCIP_C:
			return createNtcipCPoller();
		case MNDOT_4:
		case MNDOT_5:
			return createMndotPoller();
		case SS_105:
			return createSS105Poller();
		case SS_125:
			return createSS125Poller();
		case CANOGA:
			return createCanogaPoller();
		case PELCO_D_PTZ:
			return createPelcoDPoller();
		case MANCHESTER_PTZ:
			return createManchesterPoller();
		case DMSXML:
			return createDmsXmlPoller();
		case MSG_FEED:
			return createMsgFeedPoller();
		case PELCO_SWITCHER:
			return createPelcoPoller();
		case VICON_PTZ:
			return createViconPTZPoller();
		case ORG_815:
			return createOrg815Poller();
		case INFINOVA_D_PTZ:
			return createInfinovaDPoller();
		case RTMS_G4:
			return createRtmsG4Poller();
		case SSI:
			return createSsiPoller();
		case DIN_RELAY:
			return createDinRelayPoller();
		case HYSECURITY_STC:
			return createSTCPoller();
		case COHU_PTZ:
			return createCohuPTZPoller();
		case DR_500:
			return createDR500Poller();
		case ADDCO:
			return createAddcoPoller();
		case TRANSCORE_E6:
			return createE6Poller();
		case CBW:
			return createCBWPoller();
		case INC_FEED:
			return createIncFeedPoller();
		default:
			throw new ProtocolException("INVALID PROTOCOL");
		}
	}

	/** Create a socket messenger */
	private Messenger createSocketMessenger(String d_uri)
		throws IOException
	{
		try {
			URI u = createURI(d_uri);
			if ("tcp".equals(u.getScheme()))
				return createStreamMessenger(u);
			else if ("udp".equals(u.getScheme()))
				return createDatagramMessenger(u);
			else if ("modem".equals(u.getScheme()))
				return createModemMessenger(u);
			else
				throw new IOException("INVALID URI SCHEME");
		}
		catch (URISyntaxException e) {
			throw new IOException("INVALID URI");
		}
	}

	/** Create the URI with a default URI scheme */
	private URI createURI(String d_uri) throws URISyntaxException {
		return URI.create(d_uri).resolve(createURI());
	}

	/** Create the URI */
	private URI createURI() throws URISyntaxException {
		try {
			return new URI(uri);
		}
		catch (URISyntaxException e) {
			// If the URI begins with a host IP address,
			// we need to prepend a couple of slashes
			return new URI("//" + uri);
		}
	}

	/** Create a TCP stream messenger */
	private Messenger createStreamMessenger(URI u) throws IOException {
		return new StreamMessenger(createSocketAddress(u));
	}

	/** Create a UDP datagram messenger */
	private Messenger createDatagramMessenger(URI u) throws IOException {
		return new DatagramMessenger(createSocketAddress(u));
	}

	/** Create an inet socket address */
	private InetSocketAddress createSocketAddress(URI u) throws IOException{
		String host = u.getHost();
		int p = u.getPort();
		try {
			return new InetSocketAddress(host, p);
		}
		catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}

	/** Create a modem messenger */
	private Messenger createModemMessenger(URI u) throws IOException {
		ModemImpl modem = ModemMessenger.getModem();
		if (modem != null) {
			return new ModemMessenger(new StreamMessenger(
				createSocketAddress(createModemURI(modem))),
				modem, u.getHost());
		} else
			throw new IOException("No modem available");
	}

	/** Create a URI for the modem */
	private URI createModemURI(ModemImpl modem) throws IOException {
		try {
			return modem.createURI();
		}
		catch (URISyntaxException e) {
			throw new IOException("INVALID MODEM URI");
		}
	}

	/** Create an http file messenger */
	private HttpFileMessenger createHttpFileMessenger() throws IOException {
		return new HttpFileMessenger(new URL(uri));
	}

	/** Create an NTCIP Class A poller */
	private DevicePoller createNtcipAPoller() throws IOException {
		return new NtcipPoller(name, createSocketMessenger(UDP));
	}

	/** Create an NTCIP Class B poller */
	private DevicePoller createNtcipBPoller() throws IOException {
		HDLCMessenger hdlc = new HDLCMessenger(
			createSocketMessenger(TCP));
		return new NtcipPoller(name, hdlc);
	}

	/** Create an NTCIP Class C poller */
	private DevicePoller createNtcipCPoller() throws IOException {
		return new NtcipPoller(name, createSocketMessenger(TCP));
	}

	/** Create a MnDOT poller */
	private DevicePoller createMndotPoller() throws IOException {
		return new MndotPoller(name, createSocketMessenger(TCP));
	}

	/** Create an SS105 poller */
	private DevicePoller createSS105Poller() throws IOException {
		return new SS105Poller(name, createSocketMessenger(TCP));
	}

	/** Create an SS125 poller */
	private DevicePoller createSS125Poller() throws IOException {
		return new SS125Poller(name, createSocketMessenger(TCP));
	}

	/** Create a Canoga poller */
	private DevicePoller createCanogaPoller() throws IOException {
		return new CanogaPoller(name, createSocketMessenger(TCP));
	}

	/** Create a PelcoD poller */
	private DevicePoller createPelcoDPoller() throws IOException {
		return new PelcoDPoller(name, createSocketMessenger(UDP));
	}

	/** Create a Manchester poller */
	private DevicePoller createManchesterPoller() throws IOException {
		return new ManchesterPoller(name, createSocketMessenger(UDP));
	}

	/** Create a DMS XML poller */
	private DevicePoller createDmsXmlPoller() throws IOException {
		return new DmsXmlPoller(name, createSocketMessenger(TCP));
	}

	/** Create a MSG FEED poller */
	private DevicePoller createMsgFeedPoller() throws IOException {
		return new MsgFeedPoller(name, createHttpFileMessenger());
	}

	/** Create a incident feed poller */
	private DevicePoller createIncFeedPoller() throws IOException {
		return new IncFeedPoller(name, createHttpFileMessenger());
	}

	/** Create a Pelco video switch poller */
	private DevicePoller createPelcoPoller() throws IOException {
		return new PelcoPoller(name, createSocketMessenger(TCP));
	}

	/** Create a Vicon PTZ poller */
	private DevicePoller createViconPTZPoller() throws IOException {
		return new ViconPTZPoller(name, createSocketMessenger(UDP));
	}

	/** Create a ORG-815 precipitation sensor poller */
	private DevicePoller createOrg815Poller() throws IOException {
		return new Org815Poller(name, createSocketMessenger(TCP));
	}

	/** Create an Infinova D PTZ poller */
	private DevicePoller createInfinovaDPoller() throws IOException {
		return new PelcoDPoller(name, new InfinovaMessenger(
			createSocketMessenger(TCP)));
	}

	/** Create an SSI poller */
	private DevicePoller createSsiPoller() throws IOException {
		return new SsiPoller(name, createHttpFileMessenger());
	}

	/** Create an RTMS G4 poller */
	private DevicePoller createRtmsG4Poller() throws IOException {
		return new G4Poller(name, createSocketMessenger(TCP));
	}

	/** Create a DIN relay poller */
	private DevicePoller createDinRelayPoller() throws IOException {
		return new DinRelayPoller(name, createHttpFileMessenger());
	}

	/** Create a new Control By Web poller */
	private DevicePoller createCBWPoller() throws IOException {
		return new CBWPoller(name, createHttpFileMessenger());
	}

	/** Create a STC poller */
	private DevicePoller createSTCPoller() throws IOException {
		return new STCPoller(name, createSocketMessenger(TCP));
	}

	/** Create a Cohu PTZ poller */
	private DevicePoller createCohuPTZPoller() throws IOException {
		return new CohuPTZPoller(name, createSocketMessenger(TCP));
	}

	/** Create a DR-500 poller */
	private DevicePoller createDR500Poller() throws IOException {
		return new DR500Poller(name, createSocketMessenger(TCP));
	}

	/** Create an ADDCO poller */
	private DevicePoller createAddcoPoller() throws IOException {
		return new AddcoPoller(name, new AddcoMessenger(
			createSocketMessenger(TCP)));
	}

	/** Create a TransCore E6 poller */
	private DevicePoller createE6Poller() throws IOException {
		return new E6Poller(name, createPacketMessenger());
	}

	/** Create a packet messenger */
	private PacketMessenger createPacketMessenger() throws IOException {
		try {
			URI u = createURI(UDP);
			return new PacketMessenger(createSocketAddress(u));
		}
		catch (URISyntaxException e) {
			throw new IOException("INVALID URI");
		}
	}
}
