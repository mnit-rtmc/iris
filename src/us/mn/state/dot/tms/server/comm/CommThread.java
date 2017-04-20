/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.EventType.COMM_ERROR;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * CommThread represents a communication channel with priority-queued polling.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public class CommThread<T extends ControllerProperty> {

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

	/** Write a message to the comm log */
	private void clog(String msg) {
		if (logger.isOpen())
			logger.log(thread.getName() + " " + msg);
	}

	/** Get an exception message */
	protected String getMessage(Exception e) {
		String msg = exceptionMessage(e);
		clog("Exception -- " + msg);
		return msg;
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

	/** Debug log */
	private final DebugLog logger;

	/** Connected status */
	private boolean connected;

	/** Thread status */
	private String status = "";

	/** Set the thread status */
	protected void setStatus(String s) {
		status = s;
	}

	/** Get the thread status */
	public String getStatus() {
		return status;
	}

	/** Create a new comm thread.
	 * @param dp The device poller.
	 * @param q The operation queue.
	 * @param s Default URI scheme.
	 * @param u The URI.
	 * @param rt Receive timeout (ms) */
	public CommThread(ThreadedPoller<T> dp, OpQueue<T> q, URI s, String u,
		int rt, DebugLog log)
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
		logger = log;
		connected = false;
	}

	/** Start the thread */
	public void start() {
		thread.start();
	}

	/** Check if the thread is connected */
	public boolean isConnected() {
		return connected;
	}

	/** Destroy the comm thread */
	public void destroy() {
		thread.interrupt();
	}

	/** Check if the comm thread should continue */
	private boolean shouldContinue() {
		return queue.isOpen() && !thread.isInterrupted();
	}

	/** Run comm thread operations */
	private void doRun() {
		clog("STARTING");
		connected = true;
		try {
			performOperations();
		}
		catch (InterruptedException | MessengerException e) {
			setStatus(getMessage(e));
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}
		finally {
			connected = false;
			clog("STOPPING");
		}
	}

	/** Create messenger and perform operations from the poll queue.
	 * @throws MessengerException if the messenger could not be created.
	 * @throws InterruptedException when thread is destroyed. */
	private void performOperations() throws InterruptedException,
		MessengerException
	{
		while (shouldContinue()) {
			try (Messenger m = createMessenger(scheme, uri,timeout))
			{
				pollQueue(m);
			}
			catch (NoModemException e) {
				// Keep looping until modem is available
				setStatus(getMessage(e));
			}
			catch (IOException e) {
				String msg = getMessage(e);
				setStatus(msg);
				if (poller.handleError(COMM_ERROR, msg))
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
	 * @return The new messenger.
	 * @throws MessengerException if the messenger could not be created. */
	protected Messenger createMessenger(URI s, String u, int rt)
		throws MessengerException, IOException
	{
		return Messenger.create(s, u, rt);
	}

	/** Poll the operation queue and perform operations.
	 * @throws IOException if an unrecoverable IO error happens.
	 * @throws InterruptedException when thread is destroyed. */
	private void pollQueue(Messenger m) throws InterruptedException,
		IOException
	{
		setStatus("");
		while (shouldContinue()) {
			OpController<T> op = queue.tryNext();
			if (op == null) {
				startIdleDisconnectTimer();
				op = queue.next();
				stopIdleDisconnectTimer();
			}
			doPoll(m, op);
			setStatus("");
		}
	}

	/** Perform one poll for an operation.
	 * @param m Messenger to communicate with controller.
	 * @param o Operation to poll.
	 * @throws IOException if an unrecoverable IO error happens. */
	private void doPoll(Messenger m, final OpController<T> o)
		throws IOException
	{
		final String oname = o.toString();
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
			String msg = getMessage(e);
			o.setFailed();
			o.setMaintStatus(msg);
		}
		catch (ChecksumException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.CHECKSUM_ERROR, msg);
			m.drain();
		}
		catch (ParsingException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.PARSING_ERROR, msg);
			m.drain();
		}
		catch (ControllerException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.CONTROLLER_ERROR, msg);
			o.setFailed();
			o.setMaintStatus(msg);
		}
		catch (SocketTimeoutException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.POLL_TIMEOUT_ERROR, msg);
			if (m instanceof ModemMessenger && !o.isSuccess()) {
				// Force modem to reconnect
				throw e;
			}
		}
		catch (SocketException e) {
			String msg = getMessage(e);
			o.handleCommError(EventType.COMM_ERROR, msg);
			throw e;
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

	//----- IdleDisconnect code --------------------

	/** Scheduler for idle-disconnect jobs */
	static private final Scheduler IDLEDISCONNECT = new Scheduler("idledisconnect");

	/** Current idle-disconnect job for this CommThread */
	private transient IdleDisconnectJob idle_disconnect_job = null;

	/** Stop the idle-disconnect timer */
	private void stopIdleDisconnectTimer() {
		IdleDisconnectJob hj = idle_disconnect_job;
		if (hj != null) {
			IDLEDISCONNECT.removeJob(hj);
			idle_disconnect_job = null;
		}
	}

	/** Start the idle-disconnect timer
	 * (if appropriate for this connection) */
	private void startIdleDisconnectTimer() {
		stopIdleDisconnectTimer();
		int delaysec = getCommIdleDisconnectSec();
		if (delaysec != -1) {
			// Set minimum (non-infinite) idle delay to 1 sec
			// to avoid a race condition between the disconnect
			// timer and the op processing thread...
			if (delaysec == 0)
				delaysec = 1;
			idle_disconnect_job = new IdleDisconnectJob(delaysec);
			IDLEDISCONNECT.addJob(idle_disconnect_job);
		}
	}

	/** Get max seconds an idle connection
	 *  should be left open (-1 == infinite) */
	int getCommIdleDisconnectSec() {
		if (isModemLink())
			return SystemAttrEnum.COMM_IDLE_DISCONNECT_MODEM_SEC.getInt();
		DevicePoller p = poller;
		return (p == null)
		      ? -1 // == Infinite
		      : p.getPollerIdleDisconnectSec();
	}
	
	/** Check if a modem is required for the link */
	public boolean isModemLink() {
		return uri.startsWith("modem:");
	}

	/** Job that disconnects an idle connection */
	private class IdleDisconnectJob extends Job {
		private IdleDisconnectJob(int delaysec) {
			super(delaysec * 1000); // seconds -> milliseconds
		}

		@Override 
		public void perform() {
			if (idle_disconnect_job != this)
				return; // only process current disconnect-job
			DevicePoller dp = poller;
			if (dp != null)
				dp.disconnectIfIdle();
		}
	}
}
