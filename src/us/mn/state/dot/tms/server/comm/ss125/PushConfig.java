/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

/**
 * Data push configuration.
 *
 * @author Douglas Lau
 */
public class PushConfig {

	/** Port to send push data */
	static public enum Port {
		RS485, RS232, EXP1, EXP2;
		static public Port fromOrdinal(int o) {
			for(Port p: Port.values()) {
				if(p.ordinal() == o)
					return p;
			}
			return RS485;
		}
	}

	/** Protocol to send push data */
	static public enum Protocol {
		Z1, SS105, SS105_MULTI, RTMS;
		static public Protocol fromOrdinal(int o) {
			for(Protocol p: Protocol.values()) {
				if(p.ordinal() == o)
					return p;
			}
			return Z1;
		}
	}

	/** Port to use for data push */
	private Port port;

	/** Get the port to use for data push */
	public Port getPort() {
		return port;
	}

	/** Protocol to use for data push */
	private Protocol protocol;

	/** Get the protocol to use for data push */
	public Protocol getProtocol() {
		return protocol;
	}

	/** Enable flag */
	private boolean enable;

	/** Get the enable flag */
	public boolean getEnable() {
		return enable;
	}

	/** Set the enable flag */
	public void setEnable(boolean e) {
		enable = e;
	}

	/** Sub ID for destination of push */
	private int dest_sub_id;

	/** Get destination sub ID */
	public int getDestSubID() {
		return dest_sub_id;
	}

	/** Destination ID of push */
	private int dest_id;

	/** Get the destination ID */
	public int getDestID() {
		return dest_id;
	}

	/** Create a new push configuration */
	public PushConfig(Port p, Protocol pr, boolean e, int dsid, int did) {
		port = p;
		protocol = pr;
		enable = e;
		dest_sub_id = dsid;
		dest_id = did;
	}

	/** Get a string representation of the push config */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("enable:");
		sb.append(getEnable());
		if(getEnable()) {
			sb.append(",port:");
			sb.append(getPort());
			sb.append(",protocol:");
			sb.append(getProtocol());
			sb.append(",dest_sub_id:");
			sb.append(getDestSubID());
			sb.append(",dest_id:");
			sb.append(getDestID());
		}
		return sb.toString();
	}
}
