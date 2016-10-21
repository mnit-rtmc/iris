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
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Comparator;
import java.util.HashSet;
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

	/** Set of owned operations */
	private final HashSet<Operation> op_set = new HashSet<Operation>();

	/** Polling queue */
	private final PriorityQueue<Operation> p_queue =
		new PriorityQueue<Operation>(11, new Comparator<Operation>()
	{
		@Override public int compare(Operation a, Operation b) {
			// NOTE: don't allow duplicates of "equal" operations
			int cc = a.compareTo(b);
			if (0 == cc)
				return 0;
			// NOTE: these mutable values should never change
			//       while the operations are in the queue
			int c = Integer.signum(a.getPriority().ordinal()
			                     - b.getPriority().ordinal());
			if (c != 0)
				return c;
			c = Integer.signum(a.getRuns() - b.getRuns());
			if (c != 0)
				return c;
			else
				return cc;
		}
	});

	/** Receive queue */
	private final PriorityQueue<Operation> r_queue =
		new PriorityQueue<Operation>(11, new Comparator<Operation>()
	{
		@Override public int compare(Operation a, Operation b) {
			// NOTE: don't allow duplicates of "equal" operations
			int cc = a.compareTo(b);
			if (0 == cc)
				return 0;
			// NOTE: the expire time should not change
			//       while the operations are in the queue
			int c = Long.signum(a.getExpire() - b.getExpire());
			if (c != 0)
				return c;
			else
				return cc;
		}
	});

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
		synchronized (op_set) {
			op_set.clear();
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
		if (addWorking(op)) {
			if (logger.isOpen())
				log("ADDING " + op);
			addQueue(op);
		} else {
			if (logger.isOpen())
				log("SKIPPING " + op);
			op.destroy();
		}
	}

	/** Add an operation to the working set */
	private boolean addWorking(Operation op) {
		synchronized (op_set) {
			return op_set.add(op);
		}
	}

	/** Add an operation to a queue */
	private void addQueue(Operation op) {
		if (shouldDrop(op))
			drop(op);
		else {
			doAddQueue(op);
			ensureOpen();
		}
	}

	/** Check if an operation should be dropped */
	private boolean shouldDrop(Operation op) {
		return destroyed || op.isDone();
	}

	/** Drop an operation */
	private void drop(Operation op) {
		if (logger.isOpen())
			log("DROPPING " + op);
		op.destroy();
		removeWorking(op);
	}

	/** Remove an operation from the working set */
	private void removeWorking(Operation op) {
		synchronized (op_set) {
			if (!op_set.remove(op)) {
				// This should never happen
				log("ERR SET " + op);
			}
		}
	}

	/** Add an operation to a queue */
	private void doAddQueue(Operation op) {
		if (op.isPolling())
			addPollQueue(op);
		else
			addRecvQueue(op);
	}

	/** Add an operation to the poll queue */
	private void addPollQueue(Operation op) {
		synchronized (p_queue) {
			if (!p_queue.add(op)) {
				// This should never happen
				log("ERR POLL " + op);
			}
		}
		schedulePoll();
	}

	/** Add an operation to the receive queue */
	private void addRecvQueue(Operation op) {
		synchronized (r_queue) {
			if (!r_queue.add(op)) {
				// This should never happen
				log("ERR RECV " + op);
			}
		}
		scheduleTimeout(op);
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
				int off = tx_buf.position();
				op.poll(tx_buf);
				if (logger.isOpen())
					log("SEND " + formatBuf(tx_buf, off));
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
		catch (BufferOverflowException e) {
			op.setFailed();
			op.handleEvent(EventType.COMM_ERROR, ex_msg(e));
			closeChannel();
		}
		addQueue(op);
	}

	/** Format the contents of a buffer */
	private String formatBuf(ByteBuffer buf, int off) {
		synchronized (buf) {
			return HexString.format(buf.array(), off,
			                        buf.position() - off,
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
		op.setRemaining(timeout);
		COMM.addJob(new Job(timeout) {
			@Override public void perform() {
				checkTimeout(op);
			}
		});
	}

	/** Check if an operation has timed out */
	private void checkTimeout(Operation op) {
		long rt = op.getRemaining();
		if (rt <= 0 && removeRecv(op)) {
			op.handleEvent(EventType.POLL_TIMEOUT_ERROR, TIMEOUT);
			addQueue(op);
		}
	}

	/** Remove an operation from the receive queue */
	private boolean removeRecv(Operation op) {
		synchronized (r_queue) {
			return r_queue.remove(op);
		}
	}

	/** Check for data in receive buffer */
	public void checkReceive() {
		COMM.addJob(new Job() {
			@Override public void perform() {
				parseReceive();
			}
		});
	}

	/** Parse data in receive buffer */
	private void parseReceive() {
		Operation op = recvQueue();
		if (op != null)
			recvOperation(op);
		clearRxBuf();
	}

	/** Get the first operation on the receive queue */
	private Operation recvQueue() {
		synchronized (r_queue) {
			return r_queue.poll();
		}
	}

	/** Parse received data */
	private void recvOperation(Operation op) {
		try {
			synchronized (rx_buf) {
				if (logger.isOpen())
					log("RECV " + formatBuf(rx_buf, 0));
				rx_buf.flip();
				op.recv(rx_buf);
				rx_buf.compact();
			}
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
		addQueue(op);
	}

	/** Stop polling if idle */
	@Override
	public void stopPollingIfIdle() {
		if (isPollEmpty() && isRecvEmpty())
			closeChannel();
	}

	/** Check if the poll queue is empty */
	private boolean isPollEmpty() {
		synchronized (p_queue) {
			return p_queue.isEmpty();
		}
	}

	/** Check if the recv queue is empty */
	private boolean isRecvEmpty() {
		synchronized (r_queue) {
			return r_queue.isEmpty();
		}
	}
}
