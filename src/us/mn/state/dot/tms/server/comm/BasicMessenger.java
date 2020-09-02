/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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
import java.io.InputStream;
import java.io.OutputStream;

import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * BasicMessenger is a class used for creating
 * Messenger child-classes which create low-level
 * input/output streams.  It handles wrapping
 * streams to monitor then, manages the no-response
 * timer, and adjusts the closing of the Messenger
 * to work with the no-response timer.
 *
 * @author John L. Stanley - SRF Consulting
 */
abstract class BasicMessenger extends Messenger {

	/** No-response disconnect (sec) */
	private final int no_resp_disconnect_sec;

	/** Create a new basic messenger */
	protected BasicMessenger(int nrd) {
		no_resp_disconnect_sec = nrd;
	}

	public void close()
			throws IOException {
		stopNoResponseTimer();
		close2();
	}

	/** Get wrapped input stream */
	@Override
	public InputStream getInputStream(String path)
			throws IOException {
		return new InputDetector(this, getRawInputStream(path));
	}

	/** Get wrapped input stream */
	@Override
	public InputStream getInputStream(String path, ControllerImpl c)
			throws IOException {
		return new InputDetector(this, getRawInputStream(path, c));
	}

	/** Get wrapped output stream for the specified controller */
	@Override
	public OutputStream getOutputStream(ControllerImpl c)
			throws IOException {
		OutputStream os = getRawOutputStream(c);
		if (os == null)
			return os;
		return new OutputDetector(this, os);
	}

	protected InputStream getRawInputStream(String path, ControllerImpl c)
			throws IOException {
		return getRawInputStream(path);
	}

	//----- Abstract child-class methods

	protected abstract InputStream getRawInputStream(String path)
			throws IOException;

	protected abstract OutputStream getRawOutputStream(ControllerImpl c)
			throws IOException;

	protected abstract void close2()
			throws IOException;

	//----- No-Response-Disconnect code --------------------

	/** Scheduler for no-response jobs */
	static private final Scheduler NORESPONSE = new Scheduler("noResponse");

	/** Set when a no-response disconnect is triggered */
	private boolean bNoResponseDisconnect = false;

	/** Current no-response job for this Messenger */
	private NoRespDisconnectJob noRespDisconnectJob = null;

	/** Start the no-response timer */
	public void startNoResponseTimer() {
		if (noRespDisconnectJob != null)
			return; // it's already running
		if (no_resp_disconnect_sec > 0) {
			synchronized (NORESPONSE) {
				noRespDisconnectJob = new NoRespDisconnectJob(
					no_resp_disconnect_sec);
				NORESPONSE.addJob(noRespDisconnectJob);
			}
		}
	}

	/** Stop the no-response timer */
	public void stopNoResponseTimer() {
		NoRespDisconnectJob nrdj = noRespDisconnectJob;
		if (nrdj != null) {
			synchronized (NORESPONSE) {
				NORESPONSE.removeJob(nrdj);
				noRespDisconnectJob = null;
			}
		}
	}

	/** called by OutputDetector when data sent to device */
	public void outputDetected() {
		startNoResponseTimer();
	}

	/** called by InputDetector when data received from device */
	public void inputDetected() {
		stopNoResponseTimer();
	}

	/** Job that closes an unresponsive connection */
	private class NoRespDisconnectJob extends Job {
		private NoRespDisconnectJob(int delaysec) {
			super(delaysec * 1000); // convert to milliseconds
		}

		@Override
		public void perform() {
			if (noRespDisconnectJob != this)
				return; // only process latest disconnect-job
			try {
				bNoResponseDisconnect = true;
				close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/** Return bNoResponseDisconnect flag */
	public boolean hitNoResponseDisconnect() {
		return bNoResponseDisconnect;
	}
}
