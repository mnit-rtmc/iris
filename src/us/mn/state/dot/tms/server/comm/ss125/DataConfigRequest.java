/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
 * Data Configuration Request.
 *
 * @author Douglas Lau
 */
public class DataConfigRequest extends Request {

	/** Data config request ID */
	static protected final byte MSG_ID = 0x03;

	/** Format the body of a GET request */
	byte[] formatBodyGet() throws IOException {
		byte[] body = new byte[3];
		body[0] = MSG_ID;
		body[1] = SUB_ID_DONT_CARE;
		body[2] = REQ_READ;
		return body;
	}

	/** Format the body of a SET request */
	byte[] formatBodySet() throws IOException {
		byte[] body = new byte[28];
		body[0] = MSG_ID;
		body[1] = SUB_ID_DONT_CARE;
		body[2] = REQ_WRITE;
		format16(interval, body, 3);
		if(mode != null)
			format8(mode.ordinal(), body, 5);
		event_push.format(body, 6);
		interval_push.format(body, 12);
		presence_push.format(body, 18);
		format16Fixed(default_sep, body, 24);
		format16Fixed(default_size, body, 26);
		return body;
	}

	/** Parse the payload of a GET response */
	void parsePayload(byte[] rbody) throws IOException {
		if(rbody.length != 28)
			throw new ParsingException("BODY LENGTH");
		interval = parse16(rbody, 3);
		mode = StorageMode.fromOrdinal(rbody[5]);
		event_push = PushConfig.parse(rbody, 6);
		interval_push = PushConfig.parse(rbody, 12);
		presence_push = PushConfig.parse(rbody, 18);
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
			return null;
		}
	}

	/** Interval storage mode */
	protected StorageMode mode;

	/** Get the interval storage mode */
	public StorageMode getMode() {
		return mode;
	}

	/** Set the interval storage mode */
	public void setMode(StorageMode m) {
		mode = m;
	}

	/** Data push configuration */
	static public class PushConfig {
		public PushPort port;
		public PushProtocol protocol;
		public boolean enable;
		public int dest_sub_id;
		public int dest_id;

		static protected PushConfig parse(byte[] rbody, int pos)
			throws IOException
		{
			PushConfig pc = new PushConfig();
			pc.port = PushPort.fromOrdinal(rbody[pos]);
			pc.protocol = PushProtocol.fromOrdinal(rbody[pos + 1]);
			pc.enable = parseBoolean(rbody[pos + 2]);
			pc.dest_sub_id = parse8(rbody[pos + 3]);
			pc.dest_id = parse16(rbody, 4);
			return pc;
		}

		void format(byte[] body, int pos) {
			if(port != null)
				format8(port.ordinal(), body, pos);
			if(protocol != null)
				format8(protocol.ordinal(), body, pos + 1);
			formatBool(enable, body, pos + 2);
			format8(dest_sub_id, body, pos + 3);
			format16(dest_id, body, pos + 4);
		}
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

	/** Port to send push data */
	static public enum PushPort {
		RS485, RS232, EXP1, EXP2;
		static public PushPort fromOrdinal(int o) {
			for(PushPort p: PushPort.values()) {
				if(p.ordinal() == o)
					return p;
			}
			return null;
		}
	}

	/** Protocol to send push data */
	static public enum PushProtocol {
		Z1, SS105, SS105_MULTI, RTMS;
		static public PushProtocol fromOrdinal(int o) {
			for(PushProtocol p: PushProtocol.values()) {
				if(p.ordinal() == o)
					return p;
			}
			return null;
		}
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
}
