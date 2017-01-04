/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcop;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Operation step to listen for keyboard commands.
 *
 * @author Douglas Lau
 */
public class OpListenKeyboard extends OpStep {

	/** Interval to update operation status */
	static private final long OP_STATUS_INTERVAL_MS = 30 * 1000;

	/** Time to update operation status */
	private long op_time = TimeSteward.currentTimeMillis();

	/** Keyboard logged in flag */
	private boolean logged_in = false;

	/** Most recent property request */
	private PelcoPProp prop;

	/** Selected video monitor */
	private int mon_num;

	/** Create a new listen keyboard step */
	public OpListenKeyboard() {
		setPolling(false);
	}

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		while (prop != null) {
			try {
				doPoll(op, tx_buf);
			}
			catch (InvalidMarkException e) {
				// dumb exception
			}
			if (prop instanceof LoginProp) {
				LoginProp lp = (LoginProp) prop;
				logged_in = lp.isSuccess();
			}
			prop = prop.next();
		}
		setPolling(false);
	}

	/** Poll the controller with one packeet */
	private void doPoll(Operation op, ByteBuffer tx_buf) throws IOException{
		prop.formatHead(tx_buf);
		prop.encodeQuery(op, tx_buf);
		prop.formatTail(tx_buf);
	}

	/** Parse data received from controller */
	@Override
	public void recv(Operation op, ByteBuffer rx_buf) throws IOException {
		try {
			doRecv(op, rx_buf);
		}
		catch (InvalidMarkException e) {
			// what a stupid exception
		}
		if (shouldUpdateOpStatus()) {
			op.updateStatus();
			op_time += OP_STATUS_INTERVAL_MS;
		}
	}

	/** Check if we should update the operation status */
	private boolean shouldUpdateOpStatus() {
		return TimeSteward.currentTimeMillis() >= op_time;
	}

	/** Parse received data */
	private void doRecv(Operation op, ByteBuffer rx_buf) throws IOException{
		try {
			prop = PelcoPProp.parse(rx_buf, logged_in, mon_num);
			prop.decodeQuery(op, rx_buf);
			prop.parseTail(rx_buf);
			setPolling(true);
			if (prop instanceof MonStatusProp) {
				MonStatusProp stat = (MonStatusProp) prop;
				mon_num = stat.getMonNumber();
			}
		}
		catch (BufferUnderflowException e) {
			rx_buf.reset();
		}
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return this;
	}
}
