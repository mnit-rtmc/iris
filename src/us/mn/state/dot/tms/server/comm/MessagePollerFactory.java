/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
import java.net.URL;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.server.comm.canoga.CanogaPoller;
import us.mn.state.dot.tms.server.comm.dmsxml.DmsXmlPoller;
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
import us.mn.state.dot.tms.server.comm.vicon.ViconPoller;
import us.mn.state.dot.tms.server.comm.viconptz.ViconPTZPoller;

/**
 * A factory for creating message poller threads.
 *
 * @author Douglas Lau
 */
public class MessagePollerFactory {

	/** Create an inet socket address */
	static protected InetSocketAddress createSocketAddress(String url)
		throws IOException
	{
		String[] s = url.split(":");
		if(s.length != 2)
			throw new IOException("INVALID SOCKET ADDRESS");
		int p = parsePort(s[1]);
		return new InetSocketAddress(s[0], p);
	}

	/** Parse the port number */
	static protected int parsePort(String p) throws IOException {
		try {
			int i = Integer.parseInt(p);
			if(i >= 0 && i <= 65535)
				return i;
		}
		catch(NumberFormatException e) {
			// Fall out
		}
		throw new IOException("INVALID PORT: " + p);
	}

	/** Name of comm link */
	protected final String name;

	/** Communication protocol */
	protected final CommProtocol protocol;

	/** URL of comm link */
	protected final String url;

	/** Create a new message poller factory */
	public MessagePollerFactory(String n, CommProtocol cp, String u) {
		name = n;
		protocol = cp;
		url = u;
	}

	/** Create a message poller */
	public MessagePoller create() throws IOException {
		switch(protocol) {
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
		case VICON_SWITCHER:
			return createViconPoller();
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
		default:
			throw new ProtocolException("INVALID PROTOCOL");
		}
	}

	/** Create a socket messenger */
	protected Messenger createSocketMessenger() throws IOException {
		return new SocketMessenger(createSocketAddress(url));
	}

	/** Create a datagram messenger */
	protected Messenger createDatagramMessenger() throws IOException {
		return new DatagramMessenger(createSocketAddress(url));
	}

	/** Create an http file messenger */
	protected Messenger createHttpFileMessenger() throws IOException {
		return new HttpFileMessenger(new URL(url));
	}

	/** Create an NTCIP Class A poller */
	protected MessagePoller createNtcipAPoller() throws IOException {
		return new NtcipPoller(name, createDatagramMessenger());
	}

	/** Create an NTCIP Class B poller */
	protected MessagePoller createNtcipBPoller() throws IOException {
		HDLCMessenger hdlc = new HDLCMessenger(createSocketMessenger());
		return new NtcipPoller(name, hdlc);
	}

	/** Create an NTCIP Class C poller */
	protected MessagePoller createNtcipCPoller() throws IOException {
		return new NtcipPoller(name, createSocketMessenger());
	}

	/** Create a MnDOT poller */
	protected MessagePoller createMndotPoller() throws IOException {
		return new MndotPoller(name, createSocketMessenger(), protocol);
	}

	/** Create an SS105 poller */
	protected MessagePoller createSS105Poller() throws IOException {
		return new SS105Poller(name, createSocketMessenger());
	}

	/** Create an SS125 poller */
	protected MessagePoller createSS125Poller() throws IOException {
		return new SS125Poller(name, createSocketMessenger());
	}

	/** Create a Canoga poller */
	protected MessagePoller createCanogaPoller() throws IOException {
		return new CanogaPoller(name, createSocketMessenger());
	}

	/** Create a Vicon poller */
	protected MessagePoller createViconPoller() throws IOException {
		return new ViconPoller(name, createSocketMessenger());
	}

	/** Create a PelcoD poller */
	protected MessagePoller createPelcoDPoller() throws IOException {
		return new PelcoDPoller(name, createDatagramMessenger());
	}

	/** Create a Manchester poller */
	protected MessagePoller createManchesterPoller() throws IOException {
		return new ManchesterPoller(name, createDatagramMessenger());
	}

	/** Create a DMS XML poller */
	protected MessagePoller createDmsXmlPoller() throws IOException {
		return new DmsXmlPoller(name, createSocketMessenger());
	}

	/** Create a MSG FEED poller */
	protected MessagePoller createMsgFeedPoller() throws IOException {
		return new MsgFeedPoller(name, createHttpFileMessenger());
	}

	/** Create a Pelco video switch poller */
	protected MessagePoller createPelcoPoller() throws IOException {
		return new PelcoPoller(name, createSocketMessenger());
	}

	/** Create a Vicon PTZ poller */
	protected MessagePoller createViconPTZPoller() throws IOException {
		return new ViconPTZPoller(name, createDatagramMessenger());
	}

	/** Create a ORG-815 precipitation sensor poller */
	protected MessagePoller createOrg815Poller() throws IOException {
		return new Org815Poller(name, createSocketMessenger());
	}
}
