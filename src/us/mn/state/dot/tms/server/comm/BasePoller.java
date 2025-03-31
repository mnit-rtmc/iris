/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2024  Minnesota Department of Transportation
 * Copyright (C) 2017       SRF Consulting Group
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
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.Work;
import us.mn.state.dot.sched.Worker;
import us.mn.state.dot.tms.CommConfig;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.SString;
import us.mn.state.dot.tms.utils.URIUtil;

/**
 * BasePoller is a class polling devices using a selector.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
abstract public class BasePoller implements DevicePoller {

	/** Maximum message length */
	static private final int MAX_MSG_LEN = 64;

	/** Filter a message */
	static private String filterMsg(String m) {
		return SString.truncate(m, MAX_MSG_LEN);
	}

	/** Drain an operation queue */
	static private void drainQueue(Collection<Operation> queue) {
		for (Operation op: queue) {
			op.handleEvent(EventType.QUEUE_DRAINED);
			op.destroy();
		}
		queue.clear();
	}

	/** Get a message describing an exception */
	static private String ex_msg(Exception e) {
		String m = e.getMessage();
		if (m != null && m.length() > 0)
			return filterMsg(m);
		else
			return e.getClass().getSimpleName();
	}

	/** Buffer size */
	static private final int BUF_SZ = 1 << 12;

	/** Poll error logger */
	static private final DebugLog POLL_ERR = new DebugLog("poll_err");

	/** Exception handler */
	static private final ExceptionHandler HANDLER = new ExceptionHandler() {
		@Override public boolean handle(Exception e) {
			e.printStackTrace();
			POLL_ERR.log("Exception: " + ex_msg(e));
			return true;
		}
	};

	/** Scheduler for processing comm operations */
	static private final Scheduler COMM = new Scheduler("commx", HANDLER);

	/** Worker for logging to debug logs */
	static private final Worker LOGGER = new Worker("logger", HANDLER);

	/** Poller (comm link) name */
	private final String name;

	/** Default URI scheme */
	private final URI scheme;

	/** Remote URI */
	private final String uri;

	/** Receive timeout (ms) */
	private final int timeout_ms;

	/** Flag to close channel on timeout */
	private final boolean close_on_timeout;

	/** Comm idle disconnect seconds */
	private final int idle_disconnect_sec;

	/** Protocol logger */
	private final DebugLog logger;

	/** Set of owned operations.  All access must be synchronized. */
	private final HashSet<Operation> op_set = new HashSet<Operation>();

	/** Polling queue.  All access must be guarded by the op_set lock. */
	private final PriorityQueue<Operation> p_queue =
		new PriorityQueue<Operation>(11, new Comparator<Operation>()
	{
		@Override public int compare(Operation a, Operation b) {
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
				return a.compareTo(b);
		}
	});

	/** Receive queue of operations */
	private final ConcurrentSkipListSet<Operation> r_queue =
		new ConcurrentSkipListSet<Operation>(new Comparator<Operation>()
	{
		@Override public int compare(Operation a, Operation b) {
			// NOTE: the expire time should not change
			//       while the operations are in the queue
			int c = Long.signum(a.getExpire() - b.getExpire());
			if (c != 0)
				return c;
			else
				return a.compareTo(b);
		}
	});

	/** Transmit buffer */
	private final ByteBuffer tx_buf;

	/** Receive buffer */
	private final ByteBuffer rx_buf;

	/** Destroyed flag */
	private boolean destroyed = false;

	/** Create a base poller */
	protected BasePoller(CommLink link, URI s, boolean cot) {
		CommConfig cc = link.getCommConfig();
		name = link.getName();
		scheme = s;
		uri = link.getUri();
		timeout_ms = cc.getTimeoutMs();
		close_on_timeout = cot;
		idle_disconnect_sec = cc.getIdleDisconnectSec();
		logger = new DebugLog(name + ".log");
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
		synchronized (op_set) {
			drainQueue(p_queue);
			drainQueue(r_queue);
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

	/** Connected state */
	private boolean connected = false;

	/** Get the connected state */
	@Override
	public boolean isConnected() {
		return connected;
	}

	/** Set the connected state */
	private void setConnected(boolean c) {
		connected = c;
	}

	/** Add an operation to the device poller */
	protected final void addOp(final Operation op) {
		COMM.addJob(new Job() {
			@Override public String getName() {
				return "addOp";
			}
			@Override public void perform() {
				doAddOp(op);
			}
		});
	}

	/** Add an operation to the device poller (on comm thread) */
	private void doAddOp(Operation op) {
		if (addWorking(op)) {
			if (logger.isOpen())
				log("ADDING " + op);
			tryAddQueue(op);
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

	/** Try to add an operation to a queue */
	private void tryAddQueue(Operation op) {
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
			p_queue.remove(op);
			r_queue.remove(op);
			if (!op_set.remove(op)) {
				// This should never happen
				elog("ERR SET " + op);
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
		synchronized (op_set) {
			if (!p_queue.add(op)) {
				// This should never happen
				elog("ERR POLL " + op);
			}
		}
		schedulePoll();
	}

	/** Add an operation to the receive queue */
	private void addRecvQueue(Operation op) {
		// r_queue is sorted by expire time
		op.setRemaining(timeout_ms);
		if (!r_queue.add(op)) {
			// This should never happen
			elog("ERR RECV " + op);
		}
		scheduleTimeout();
	}

	/** Schedule a timeout check */
	private void scheduleTimeout() {
		COMM.addJob(new Job(timeout_ms) {
			@Override public String getName() {
				return "scheduleTimeout";
			}
			@Override public void perform() {
				checkTimeout();
			}
		});
	}

	/** Check if the first operation has timed out */
	private void checkTimeout() {
		Iterator<Operation> it = r_queue.iterator();
		if (it.hasNext())
			checkTimeout(it.next());
	}

	/** Check if an operation has timed out */
	private void checkTimeout(Operation op) {
		long rt = op.getRemaining();
		if (rt <= 0 && r_queue.remove(op)) {
			op.handleEvent(EventType.POLL_TIMEOUT_ERROR);
			if (close_on_timeout && op.isDone()) {
				elog("CLOSE DUE TO TIMEOUT");
				closeChannel();
			}
			tryAddQueue(op);
		}
	}

	/** Ensure that the channel is open */
	private void ensureOpen() {
		if (!isKeyValid())
			openChannel();
	}

	/** Check if the selection key is valid */
	private boolean isKeyValid() {
		SelectionKey sk = skey;
		return (sk != null)
		    && (sk.isValid())
		    && (sk.channel().isOpen());
	}

	/** Get max seconds an idle connection should be left open
	 * (0 indicates indefinite). */
	@Override
	public int getIdleDisconnectSec() {
		return idle_disconnect_sec;
	}

	/** Open the channel */
	private void openChannel() {
		CommSelector sel = SelectorThread.getSelector();
		if (sel != null)
			openChannel(sel);
		else
			elog("No CommSelector");
	}

	/** Open the channel */
	private void openChannel(CommSelector sel) {
		synchronized (tx_buf) {
			closeChannel();
			try {
				URI uri = createURI();
				openChannel(sel, uri);
			}
			catch (IOException | URISyntaxException e) {
				handleException(e);
			}
		}
	}

	/** Open the channel */
	private void openChannel(CommSelector sel, URI uri) throws IOException {
		skey = sel.createChannel(this, uri);
		clearTxBuf();
		clearRxBuf();
		setConnected(true);
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
			if (logger.isOpen() && rx_buf.position() > 0) {
				String hex = HexString.format(rx_buf.array(), 0,
					rx_buf.position(), ':');
				log("RECV " + hex);
			}
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
		if (POLL_ERR.isOpen())
			elog("Exception -- " + ex_msg(e));
		synchronized (tx_buf) {
			// Don't need to close in this case
			if (skey != null) {
				skey.attach(null);
				skey = null;
			}
		}
	}

	/** Write a message to the protocol log */
	public void log(final String msg) {
		if (logger.isOpen()) {
			LOGGER.addWork(new Work() {
				@Override public void perform() {
					logger.log(name + " " + msg);
				}
			});
		}
	}

	/** Write a message to the error log */
	private void elog(final String msg) {
		if (POLL_ERR.isOpen()) {
			LOGGER.addWork(new Work() {
				@Override public void perform() {
					POLL_ERR.log(name + " " + msg);
				}
			});
		}
	}

	/** Selection key for channel.  Access synchronized on tx_buf. */
	private SelectionKey skey;

	/** Close the channel */
	private void closeChannel() {
		synchronized (tx_buf) {
			if (skey != null) {
				closeChannel(skey);
				skey = null;
			}
		}
	}

	/** Close the channel */
	private void closeChannel(SelectionKey sk) {
		setConnected(false);
		// Tell selector to close the channel
		sk.attach(null);
		if (sk.isValid())
			updateInterest(SelectionKey.OP_WRITE);
		else {
			try {
				sk.channel().close();
			}
			catch (IOException e) {
				elog("CLOSE: " + ex_msg(e));
			}
		}
	}

	/** Schedule poll of operation */
	private void schedulePoll() {
		COMM.addJob(new Job() {
			@Override public String getName() {
				return "schedulePoll";
			}
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
		synchronized (op_set) {
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
			op.putCtrlFaults("other", ex_msg(e));
		}
		catch (IOException e) {
			op.handleEvent(EventType.COMM_ERROR);
			closeChannel();
		}
		catch (BufferOverflowException e) {
			op.setFailed();
			op.handleEvent(EventType.COMM_ERROR);
			closeChannel();
		}
		finally {
			tryAddQueue(op);
		}
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
	public int getInterest() {
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
	private void updateInterest(int ops) {
		synchronized (tx_buf) {
			if (skey != null) {
				try {
					if (!isConnecting())
						skey.interestOps(ops);
					skey.selector().wakeup();
				}
				catch (CancelledKeyException e) {
					closeChannel(skey);
					skey = null;
				}
			}
		}
	}

	/** Test if the socket is connecting */
	private boolean isConnecting() {
		return (skey.interestOps() & SelectionKey.OP_CONNECT) != 0;
	}

	/** Check for data in receive buffer */
	public void checkReceive() {
		COMM.addJob(new Job() {
			@Override public String getName() {
				return "checkReceive";
			}
			@Override public void perform() {
				parseReceive();
			}
		});
	}

	/** Parse data in receive buffer */
	private void parseReceive() {
		// Always iterate through all operations
		// in case rx_buf contains multiple unrelated packets
		Iterator<Operation> it = r_queue.iterator();
		while (it.hasNext()) {
			Operation op = it.next();
			if (recvOperation(op)) {
				it.remove();
				tryAddQueue(op);
			}
		}
		clearRxBuf();
	}

	/** Parse received data */
	private boolean recvOperation(Operation op) {
		try {
			synchronized (rx_buf) {
				ByteBuffer rx = rx_buf.asReadOnlyBuffer();
				rx.flip();
				op.recv(rx);
			}
			return true;
		}
		catch (NotReceivedException e) {
			return false;
		}
		catch (ProtocolException e) {
			op.setFailed();
			op.putCtrlFaults("other", ex_msg(e));
			return false;
		}
		catch (ChecksumException e) {
			op.handleEvent(EventType.CHECKSUM_ERROR);
			return true;
		}
		catch (ParsingException e) {
			op.handleEvent(EventType.PARSING_ERROR);
			return true;
		}
		catch (ControllerException e) {
			op.handleEvent(EventType.CONTROLLER_ERROR);
			op.setFailed();
			op.putCtrlFaults("other", ex_msg(e));
			return true;
		}
		catch (IOException e) {
			op.handleEvent(EventType.COMM_ERROR);
			closeChannel();
			return true;
		}
	}

	/** Check if the poll queue is empty */
	private boolean isPollEmpty() {
		synchronized (op_set) {
			return p_queue.isEmpty();
		}
	}

	/** Start communication test */
	@Override
	public void startTesting(ControllerImpl c) {
		if (logger.isOpen())
			log("TESTING not implemented: " + c);
	}
}
