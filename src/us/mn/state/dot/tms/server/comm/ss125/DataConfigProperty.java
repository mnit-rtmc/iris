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

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Data Configuration Property.
 *
 * @author Douglas Lau
 */
public class DataConfigProperty extends SS125Property {

	/** Message ID for data config request */
	protected MessageID msgId() {
		return MessageID.DATA_CONFIG;
	}

	/** Format a QUERY request */
	protected byte[] formatQuery() throws IOException {
		byte[] body = new byte[4];
		formatBody(body, MessageType.READ);
		return body;
	}

	/** Format a STORE request */
	protected byte[] formatStore() throws IOException {
		byte[] body = new byte[29];
		formatBody(body, MessageType.WRITE);
		format16(body, 3, interval);
		format8(body, 5, mode.ordinal());
		formatPushConfig(body, 6, event_push);
		formatPushConfig(body, 12, interval_push);
		formatPushConfig(body, 18, presence_push);
		format16Fixed(body, 24, default_sep);
		format16Fixed(body, 26, default_size);
		return body;
	}

	/** Parse a QUERY response */
	protected void parseQuery(byte[] rbody) throws IOException {
		if(rbody.length != 29)
			throw new ParsingException("BODY LENGTH");
		interval = parse16(rbody, 3);
		mode = StorageMode.fromOrdinal(rbody[5]);
		event_push = parsePushConfig(rbody, 6);
		interval_push = parsePushConfig(rbody, 12);
		presence_push = parsePushConfig(rbody, 18);
		default_sep = parse16Fixed(rbody, 24);
		default_size = parse16Fixed(rbody, 26);
	}

	/** Data interval (seconds) */
	protected int interval;

	/** Get the data interval (seconds) */
	public int getInterval() {
		return interval;
	}

	/** Set the data interval (seconds) */
	public void setInterval(int i) {
		interval = i;
	}

	/** Data storage mode */
	static public enum StorageMode {
		DISABLED, CIRCULAR, FILL_ONCE;
		static public StorageMode fromOrdinal(int o) {
			for(StorageMode sm: StorageMode.values()) {
				if(sm.ordinal() == o)
					return sm;
			}
			return DISABLED;
		}
	}

	/** Interval storage mode */
	protected StorageMode mode = StorageMode.DISABLED;

	/** Get the interval storage mode */
	public StorageMode getMode() {
		return mode;
	}

	/** Set the interval storage mode */
	public void setMode(StorageMode m) {
		mode = m;
	}

	/** Parse one push configuration */
	static private PushConfig parsePushConfig(byte[] rbody, int pos)
		throws IOException
	{
		PushConfig.Port port = PushConfig.Port.fromOrdinal(
			parse8(rbody, pos));
		PushConfig.Protocol protocol = PushConfig.Protocol.fromOrdinal(
			parse8(rbody, pos + 1));
		boolean enable = parseBool(rbody, pos + 2);
		int dest_sub_id = parse8(rbody, pos + 3);
		int dest_id = parse16(rbody, pos + 4);
		return new PushConfig(port, protocol, enable, dest_sub_id,
			dest_id);
	}

	/** Format one push configuration */
	static private void formatPushConfig(byte[] body, int pos,
		PushConfig pc)
	{
		format8(body, pos, pc.getPort().ordinal());
		format8(body, pos + 1, pc.getProtocol().ordinal());
		formatBool(body, pos + 2, pc.getEnable());
		format8(body, pos + 3, pc.getDestSubID());
		format16(body, pos + 4, pc.getDestID());
	}

	/** Config for event data push */
	protected PushConfig event_push;

	/** Get event push config */
	public PushConfig getEventPush() {
		return event_push;
	}

	/** Config for interval data push */
	protected PushConfig interval_push;

	/** Get the interval push config */
	public PushConfig getIntervalPush() {
		return interval_push;
	}

	/** Config for presence data push */
	protected PushConfig presence_push;

	/** Get the presence push config */
	public PushConfig getPresencePush() {
		return presence_push;
	}

	/** Default loop separation */
	protected float default_sep;

	/** Get the default loop separation */
	public float getDefaultSeparation() {
		return default_sep;
	}

	/** Default loop size */
	protected float default_size;

	/** Get the default loop size */
	public float getDefaultSize() {
		return default_size;
	}

	/** Get a string representation of the property */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("interval:");
		sb.append(getInterval());
		sb.append(",mode:");
		sb.append(getMode());
		sb.append(",event_push:");
		sb.append(getEventPush());
		sb.append(",interval_push:");
		sb.append(getIntervalPush());
		sb.append(",presence_push:");
		sb.append(getPresencePush());
		sb.append(",def_sep:");
		sb.append(getDefaultSeparation());
		sb.append(",def_size:");
		sb.append(getDefaultSize());
		return sb.toString();
	}
}
