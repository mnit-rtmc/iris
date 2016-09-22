/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.PriorityQueue;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.URIUtil;

/**
 * BasePoller is a class polling devices using a selector.
 *
 * @author Douglas Lau
 */
public class BasePoller implements DevicePoller {

	/** Read timed out message */
	static private final String TIMEOUT = "READ TIMED OUT";

	/** Drain an operation queue */
	static private void drainQueue(PriorityQueue<Operation> queue) {
		for (Operation op: queue) {
			op.handleEvent(EventType.QUEUE_DRAINED, "DRAINED");
			op.destroy();
		}
		queue.clear();
	}

	/** Get a message describing an exception */
	static private String ex_msg(Exception e) {
		String m = e.getMessage();
		if (m != null && m.length() > 0)
			return m;
		else
			return e.getClass().getSimpleName();
	}

	/** Buffer size */
	static private final int BUF_SZ = 1 << 12;

	/** Scheduler for processing comm operations */
	static private final Scheduler COMM = new Scheduler("comm");

	/** Scheduler for logging to debug logs */
	static private final Scheduler LOGGER = new Scheduler("logger");

	/** Poller (comm link) name */
	private final String name;

	/** Default URI scheme */
	private final URI scheme;

	/** Protocol logger */
	private final DebugLog logger;

	/** Polling queue */
	private final PriorityQueue<Operation> p_queue =
		new PriorityQueue<Operation>();

	/** Response queue */
	private final PriorityQueue<Operation> r_queue =
		new PriorityQueue<Operation>();

	/** Transmit buffer */
	private final ByteBuffer tx_buf;

	/** Receive buffer */
	private final ByteBuffer rx_buf;

	/** Destroyed flag */
	private boolean destroyed = false;

	/** Create a base poller */
	protected BasePoller(String n, URI s) {
		name = n;
		scheme = s;
		logger = new DebugLog(n + ".log");
		tx_buf = ByteBuffer.allocate(BUF_SZ);
		rx_buf = ByteBuffer.allocate(BUF_SZ);
		log("CREATED");
	}

	/** Destroy the poller */
	@Override
	public void destroy() {
		destroyed = true;
		drainQueues();
		closeChannel();
		log("DESTROYED");
	}

	/** Drain the queues */
	private void drainQueues() {
		synchronized (p_queue) {
			drainQueue(p_queue);
		}
		synchronized (r_queue) {
			drainQueue(r_queue);
		}
	}

	/** Get the transmit buffer */
	public ByteBuffer getTxBuffer() {
		return tx_buf;
	}

	/** Get the receive buffer */
	public ByteBuffer getRxBuffer() {
		return rx_buf;
	}

	/** Remote URI */
	private String uri = "";

	/** Set the remote URI */
	@Override
	public synchronized void setUri(String u) {
		uri = u;
		closeChannel();
	}

	/** Receive timeout (ms) */
	private int timeout;

	/** Set the receive timeout (ms) */
	@Override
	public void setTimeout(int rt) {
		timeout = rt;
	}

	/** Poller status */
	private String status = "INIT";

	/** Get the poller status */
	@Override
	public String getStatus() {
		return status;
	}

	/** Set the poller status */
	private void setStatus(String s) {
		status = s;
	}

	/** Add an operation to the device poller */
	protected final void addOp(Operation op) {
		if (addQueue(op)) {
			if (op.isPolling())
				schedulePoll();
			else
				scheduleTimeout(op);
		}
	}

	/** Add an operation to a queue */
	private boolean addQueue(Operation op) {
		boolean r = doAddQueue(op);
		if (r)
			ensureOpen();
		else
			drop(op);
		return r;
	}

	/** Add an operation to a queue */
	private boolean doAddQueue(Operation op) {
		if (destroyed || op.isDone())
			return false;
		if (op.isPolling()) {
			synchronized (p_queue) {
				return p_queue.add(op);
			}
		} else {
			op.setRemaining(timeout);
			synchronized (r_queue) {
				return r_queue.add(op);
			}
		}
	}

	/** Drop an operation */
	private void drop(Operation op) {
		if (logger.isOpen())
			log("DROPPING " + op);
		op.destroy();
	}

	/** Ensure that the channel is open */
	private synchronized void ensureOpen() {
		if (!isConnected())
			openChannel();
	}

	/** Check if the poller is currently connected */
	@Override
	public synchronized boolean isConnected() {
		return (skey != null)
		    && (skey.isValid())
		    && (skey.channel().isOpen());
	}

	/** Open the channel */
	private synchronized void openChannel() {
		try {
			CommSelector sel = SelectorThread.getSelector();
			skey = sel.createChannel(this, createURI());
			clearTxBuf();
			clearRxBuf();
			setStatus("OPEN");
		}
		catch (IOException | URISyntaxException e) {
			handleException(e);
			skey = null;
		}
	}

	/** Clear the transmit buffer */
	private void clearTxBuf() {
		synchronized (tx_buf) {
			tx_buf.clear();
		}
	}

	/** Clear the receive buffer */
	private void clearRxBuf() {
		synchronized (rx_buf) {
			rx_buf.clear();
		}
	}

	/** Create remote URI */
	private URI createURI() throws URISyntaxException {
		return scheme.resolve(URIUtil.create(uri));
	}

	/** Handle an exception */
	public void handleException(Exception e) {
		if (logger.isOpen())
			log("Exception -- " + ex_msg(e));
	}

	/** Write a message to the protocol log */
	public void log(final String msg) {
		if (logger.isOpen()) {
			LOGGER.addJob(new Job() {
				@Override public void perform() {
					logger.log(name + " " + msg);
				}
			});
		}
	}

	/** Selection key for channel */
	private SelectionKey skey;

	/** Close the channel */
	private synchronized void closeChannel() {
		setStatus("CLOSED");
		if ((skey != null) && skey.isValid()) {
			// Tell selector to close the channel
			skey.attach(null);
			skey.interestOps(SelectionKey.OP_WRITE);
			skey.selector().wakeup();
		}
		skey = null;
	}

	/** Schedule poll of operation */
	private void schedulePoll() {
		COMM.addJob(new Job() {
			@Override public void perform() {
				pollOperation();
			}
		});
	}

	/** Poll an operation */
	private void pollOperation() {
		Operation op = pollQueue();
		if (op != null)
			pollOperation(op);
		updateInterest(getInterest());
	}

	/** Get the first operation on the poll queue */
	private Operation pollQueue() {
		synchronized (p_queue) {
			return p_queue.poll();
		}
	}

	/** Poll one operation */
	private void pollOperation(Operation op) {
		try {
			synchronized (tx_buf) {
				op.poll(tx_buf);
				if (logger.isOpen())
					log("SEND " + formatBuf(tx_buf));
			}
		}
		catch (ProtocolException e) {
			op.setFailed();
			op.setMaintStatus(ex_msg(e));
		}
		catch (IOException e) {
			op.handleEvent(EventType.COMM_ERROR, ex_msg(e));
			closeChannel();
		}
		addOp(op);
	}

	/** Format the contents of a buffer */
	private String formatBuf(ByteBuffer buf) {
		synchronized (buf) {
			return HexString.format(buf.array(),
			                        buf.position(),
			                        ':');
		}
	}

	/** Get the interest ops */
	private int getInterest() {
		return needsWrite()
		     ? SelectionKey.OP_READ | SelectionKey.OP_WRITE
		     : SelectionKey.OP_READ;
	}

	/** Check if the transmit buffer needs writing */
	private boolean needsWrite() {
		synchronized (tx_buf) {
			return tx_buf.position() > 0;
		}
	}

	/** Update interest ops */
	private synchronized void updateInterest(int ops) {
		if (skey != null) {
			skey.interestOps(ops);
			skey.selector().wakeup();
		}
	}

	/** Schedule timeout of operation */
	private void scheduleTimeout(final Operation op) {
		long rt = op.getRemaining();
		if (rt <= timeout) {
			int t = (rt > 0) ? (int) rt : 0;
			COMM.addJob(new Job(t) {
				@Override public void perform() {
					checkTimeout(op);
				}
			});
		}
	}

	/** Check if an operation has timed out */
	private void checkTimeout(Operation op) {
		long rt = op.getRemaining();
		if (rt <= 0 && removeResp(op)) {
			op.handleEvent(EventType.POLL_TIMEOUT_ERROR, TIMEOUT);
			addOp(op);
		}
	}

	/** Remove an operation from the response queue */
	private boolean removeResp(Operation op) {
		synchronized (r_queue) {
			return r_queue.remove(op);
		}
	}

	/** Check for response in receive buffer */
	public void checkResponse() {
		COMM.addJob(new Job() {
			@Override public void perform() {
				parseResponse();
			}
		});
	}

	/** Parse response in receive buffer */
	private void parseResponse() {
		Operation op = respQueue();
		if (op != null)
			respOperation(op);
		clearRxBuf();
	}

	/** Get the first operation on the response queue */
	private Operation respQueue() {
		synchronized (r_queue) {
			return r_queue.poll();
		}
	}

	/** Parse response data */
	private void respOperation(Operation op) {
		try {
			op.resp(rx_buf);
		}
		catch (ProtocolException e) {
			op.setFailed();
			op.setMaintStatus(ex_msg(e));
		}
		catch (ChecksumException e) {
			op.handleEvent(EventType.CHECKSUM_ERROR, ex_msg(e));
		}
		catch (ParsingException e) {
			op.handleEvent(EventType.PARSING_ERROR, ex_msg(e));
		}
		catch (ControllerException e) {
			String msg = ex_msg(e);
			op.handleEvent(EventType.CONTROLLER_ERROR, msg);
			op.setFailed();
			op.setMaintStatus(msg);
		}
		catch (IOException e) {
			op.handleEvent(EventType.COMM_ERROR, ex_msg(e));
			closeChannel();
		}
		addOp(op);
	}

	/** Stop polling if idle */
	@Override
	public void stopPollingIfIdle() {
		if (isPollEmpty() && isRespEmpty())
			closeChannel();
	}

	/** Check if the poll queue is empty */
	private boolean isPollEmpty() {
		synchronized (p_queue) {
			return p_queue.isEmpty();
		}
	}

	/** Check if the resp queue is empty */
	private boolean isRespEmpty() {
		synchronized (r_queue) {
			return r_queue.isEmpty();
		}
	}
}
