/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
 * Copyright (C) 2017-2020  SRF Consulting Group
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
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.CommState;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.utils.SString;

/**
 * CommThread represents a communication channel with priority-queued polling.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class CommThread<T extends ControllerProperty> {

	/** Maximum message length */
	static private final int MAX_MSG_LEN = 64;

	/** Filter a message */
	static private String filterMsg(String m) {
		return SString.truncate(m, MAX_MSG_LEN);
	}

	/** Get a message describing an exception */
	static private String exceptionMessage(Exception e) {
		String m = e.getMessage();
		if (m != null && m.length() > 0)
			return m;
		else
			return e.getClass().getSimpleName();
	}

	/** Thread group for all comm threads */
	static private final ThreadGroup GROUP = new ThreadGroup("Comm");

	/** Check if messenger needs reconnect after read timeout failure.
	 * For a modem link, read timeout should be handled by reconnecting
	 * the modem.  For a datagram messenger (UDP), a reconnect may be
	 * required if a network error caused the information cached during
	 * the connect call to become stale.  This condition was observed when
	 * a router malfunctioned, causing all connected UDP sockets to always
	 * throw SocketTimeoutException until the socket was torn down and
	 * re-established.  Crazy.  2017-04-25. */
	static private boolean needsReconnect(Messenger m) {
		return (m instanceof ModemMessenger) ||
		       (m instanceof DatagramMessenger);
	}

	/** Write a message to the comm log */
	private void clog(String msg) {
		if (logger.isOpen())
			logger.log(thread.getName() + " " + msg);
	}

	/** Log an exception message */
	protected String logException(Exception e) {
		String msg = exceptionMessage(e);
		clog("Exception -- " + msg);
		return filterMsg(msg);
	}

	/** Threaded poller */
	private final ThreadedPoller<T> poller;

	/** Thread to poll operations */
	private final Thread thread;

	/** Operation queue */
	protected final OpQueue<T> queue;

	/** Default URI scheme */
	private final URI scheme;

	/** Remote URI */
	private final String uri;

	/** Receive timeout (ms) */
	private final int timeout;

	/** No response disconnect (sec) */
	private final int no_resp_disconnect_sec;

	/** Idle disconnect (ms) */
	private final long idle_disconnect_ms;

	/** Debug log */
	private final DebugLog logger;

	/** Done state */
	private boolean done = false;

	/** Is the thread done? */
	public boolean isDone() {
		return done;
	}

	/** Connected state */
	private boolean connected = false;

	/** Get the connected state */
	public boolean isConnected() {
		return connected && !done;
	}

	/** Create a new comm thread.
	 * @param dp The device poller.
	 * @param q The operation queue.
	 * @param s Default URI scheme.
	 * @param u The URI.
	 * @param rt Receive timeout (ms).
	 * @param nrd No-response disconnect (sec). */
	public CommThread(ThreadedPoller<T> dp, OpQueue<T> q, URI s, String u,
		int rt, int nrd, DebugLog log)
	{
		poller = dp;
 		thread = new Thread(GROUP, "Comm: " + poller.name) {
			@Override
			public void run() {
				doRun();
			}
		};
		thread.setDaemon(true);
		queue = q;
		scheme = s;
		uri = u;
		timeout = rt;
		no_resp_disconnect_sec = nrd;
		idle_disconnect_ms = poller.getIdleDisconnectSec() * 1000L;
		logger = log;
	}

	/** Start the thread */
	public void start() {
		thread.start();
	}

	/** Destroy the comm thread */
	public void destroy() {
		done = true;
		thread.interrupt();
	}

	/** Check if the comm thread should continue */
	private boolean shouldContinue() {
		return queue.isOpen() && !done;
	}

	/** Run comm thread operations */
	private void doRun() {
		clog("STARTING");
		try {
			performOperations();
		}
		catch (MessengerException e) {
			logException(e);
		}
		catch (RuntimeException e) {
			queue.drain();
			e.printStackTrace();
		}
		finally {
			done = true;
			clog("STOPPING");
			poller.disconnect();
		}
	}

	/** Create messenger and perform operations from the poll queue.
	 * @throws MessengerException if the messenger could not be created. */
	private void performOperations() throws MessengerException {
		while (shouldContinue()) {
			try (Messenger m = createMessenger(scheme, uri,
				timeout, no_resp_disconnect_sec))
			{
				connected = true;
				pollQueue(m);
			}
			catch (DisconnectException e) {
				logException(e);
				break;
			}
			catch (ReconnectException e) {
				connected = false;
				continue;
			}
			catch (NoModemException e) {
				// Keep looping until modem is available
				connected = false;
				logException(e);
			}
			catch (ConnectException e) {
				String msg = logException(e);
				if (poller.handleCommState(CommState.CONNECTION_ERROR, msg))
					break;
			}
			catch (NoResponseException e) {
				logException(e);
				if (poller.noMoreOps())
					break;
			}
			catch (IOException e) {
				String msg = logException(e);
				if (poller.handleCommState(CommState.ERROR, msg))
					break;
			}
			// Rest a second before trying again
			TimeSteward.sleep_well(1000);
		}
	}

	/** Create a messenger.
	 * @param s Default URI scheme.
	 * @param u The URI.
	 * @param rt Receive timeout (ms).
	 * @param nrd No-response disconnect (sec).
	 * @return The new messenger.
	 * @throws MessengerException if the messenger could not be created. */
	protected Messenger createMessenger(URI s, String u, int rt, int nrd)
		throws MessengerException, IOException
	{
		return Messenger.create(s, u, rt, nrd);
	}

	/** Poll the operation queue and perform operations.
	 * @throws DisconnectException if the link should be disconnected.
	 * @throws IOException if an unrecoverable IO error happens. */
	private void pollQueue(Messenger m) throws DisconnectException,
		IOException
	{
		while (shouldContinue()) {
			OpController<T> op = queue.next(idle_disconnect_ms);
			doPoll(m, op);
		}
	}

	/** Perform one poll for an operation.
	 * @param m Messenger to communicate with controller.
	 * @param o Operation to poll.
	 * @throws IOException if an unrecoverable IO error happens. */
	private void doPoll(Messenger m, final OpController<T> o)
		throws IOException
	{
		try {
			o.poll(createCommMessage(m, o));
		}
		catch (DeviceContentionException e) {
			handleContention(o, e);
		}
		catch (DownloadRequestException e) {
			sendSettings(o.getController(), o.getPriority());
		}
		catch (ProtocolException e) {
			String msg = logException(e);
			o.putCtrlFaults("other", msg);
			o.setFailed();
		}
		catch (ChecksumException e) {
			logException(e);
			o.handleCommState(CommState.CHECKSUM_ERROR);
			m.drain();
		}
		catch (ParsingException e) {
			logException(e);
			o.handleCommState(CommState.PARSING_ERROR);
			m.drain();
		}
		catch (ControllerException e) {
			String msg = logException(e);
			o.putCtrlFaults("other", msg);
			o.handleCommState(CommState.CONTROLLER_ERROR);
			o.setFailed();
		}
		catch (SocketTimeoutException e) {
			logException(e);
			o.handleCommState(CommState.TIMEOUT_ERROR);
			// Not sure if this is needed in addition
			// to no_response_disconnect feature
			if ((!o.isSuccess()) && needsReconnect(m))
				throw new ReconnectException();
		}
		catch (SocketException e) {
			logException(e);
			if (m instanceof BasicMessenger) {
				BasicMessenger bm = (BasicMessenger) m;
				if (bm.hitNoResponseDisconnect()) {
					o.handleCommState(CommState
						.TIMEOUT_ERROR);
					o.setFailed();
					throw new NoResponseException();
				}
			}
			o.handleCommState(CommState.ERROR);
			throw new ReconnectException();
		}
		finally {
			if (o.isDone() || !requeueOperation(o))
				o.cleanup();
		}
	}

	/** Handle device contention.  Another operation has the device lock.
	 * Ensure that we don't have a priority inversion problem. */
	@SuppressWarnings("unchecked")
	private void handleContention(OpController<T> op,
		DeviceContentionException e)
	{
		handleContention(op, e.operation);
	}

	/** Handle device contention */
	private void handleContention(OpController<T> op, OpController<T> oc) {
		if (oc.getPriority().ordinal() > op.getPriority().ordinal()) {
			poller.log("BUMPING " + oc + " from " + oc.getPriority()
				+ " to " + op.getPriority());
			oc.setPriority(op.getPriority());
			// If, for some crazy reason, the operation is
			// not on our queue, it will not be requeued.
			if (!requeueOperation(oc)) {
				oc.setFailed();
				oc.cleanup();
			}
		}
	}

	/** Requeue an in-progress operation */
	private boolean requeueOperation(OpController<T> op) {
		if (queue.requeue(op))
			return true;
		else {
			poller.log("DROPPING " + op);
			return false;
		}
	}

	/** Create a message for the specified operation.
	 * @param m The messenger.
	 * @param o The operation.
	 * @return New comm message. */
	protected CommMessage<T> createCommMessage(Messenger m,
		OpController<T> o) throws IOException
	{
		return new CommMessageImpl<T>(m, o, logger);
	}

	/** Respond to a settings request from a controller */
	private void sendSettings(ControllerImpl c, PriorityLevel p) {
		if (c.isActive())
			poller.sendSettings(c, p);
	}
}
