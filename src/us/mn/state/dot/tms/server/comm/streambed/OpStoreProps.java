/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.streambed;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Streambed operation to store properties.
 *
 * @author Douglas Lau
 */
public class OpStoreProps extends OpStep {

	/** UTF-8 charset */
	static private final Charset UTF8 = Charset.forName("UTF-8");

	/** ASCII group separator */
	static private final byte GROUP_SEP = 0x1D;

	/** ASCII record separator */
	static private final byte RECORD_SEP = 0x1E;

	/** ASCII record separator as string */
	static private final String RECORD_SEP_STR = new String(
		new byte[] { RECORD_SEP }, UTF8
	);

	/** ASCII unit separator */
	static private final byte UNIT_SEP = 0x1F;

	/** Command string */
	private final String command;

	/** Command buffer */
	private final ByteBuffer buf = ByteBuffer.allocate(1024);

	/** Does this op need more work? */
	private boolean more = true;

	/** Create a new store properties operation */
	public OpStoreProps(String cmd) {
		command = cmd;
		buf.put(cmd.getBytes(UTF8));
	}

	/** Add one parameter */
	public void addParam(String name, String value) {
		buf.put(RECORD_SEP);
		buf.put(name.getBytes(UTF8));
		buf.put(UNIT_SEP);
		buf.put(value.getBytes(UTF8));
	}

	/** Add an integer parameter */
	public void addParam(String name, int value) {
		addParam(name, Integer.toString(value));
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) {
		buf.flip();
		tx_buf.put(buf);
		tx_buf.put(GROUP_SEP);
		setPolling(false);
	}

	/** Parse data received from controller */
	@Override
	public void recv(Operation op, ByteBuffer rx_buf) throws IOException {
		byte[] b = new byte[1024];
		int len = Math.min(rx_buf.remaining(), b.length);
		rx_buf.get(b, 0, len);
		doRecv(op.getController(), new String(b, 0, len, UTF8));
	}

	/** Parse received messages */
	private void doRecv(ControllerImpl ctrl, String msgs)
		throws IOException
	{
		for (String msg : msgs.split(RECORD_SEP_STR)) {
			parseMsg(ctrl, msg);
		}
	}

	/** Parse one received message */
	private void parseMsg(ControllerImpl ctrl, String msg)
		throws IOException
	{
		if (msg.equals(command))
			more = false;
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return (more) ? this : null;
	}
}
