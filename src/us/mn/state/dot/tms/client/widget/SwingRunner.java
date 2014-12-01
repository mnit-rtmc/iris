/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import javax.swing.SwingUtilities;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.client.MainClient;

/**
 * SwingRunner is a simple utility class for running code on the Swing thread.
 * A warning message is printed if the time elapsed is too long.
 *
 * @author Douglas Lau
 */
public final class SwingRunner {

	/** Maximum elapsed time before logging */
	static public final long MAX_ELAPSED = 50;

	/** Get the exception handler */
	static private ExceptionHandler getHandler() {
		return MainClient.getHandler();
	}

	/** Log a message */
	static private void log(String msg, long e) {
		System.err.println("SwingRunner took " + e + " ms");
		System.err.println("  from: " + msg);
	}

	/** Run some runnable code */
	static private void runNow(Runnable r) {
		long st = TimeSteward.currentTimeMillis();
		try {
			try {
				r.run();
			}
			finally {
				long e = TimeSteward.currentTimeMillis() - st;
				if (e > MAX_ELAPSED)
					log(r.getClass().toString(), e);
			}
		}
		catch (Exception e) {
			getHandler().handle(e);
		}
	}

	/** Invoke a Runnable on the swing thread */
	static public void runSwing(final Runnable r) {
		// NOTE: use invokeLater for 2 reasions:
		//    1. Serialize on the EDT no matter which thread we're on.
		//    2. Drop any held locks before invoking the Runnable.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				runNow(r);
			}
		});
	}

	/** Invoke some invokable code */
	static private void runNow(Invokable r) {
		long st = TimeSteward.currentTimeMillis();
		try {
			try {
				r.invoke();
			}
			finally {
				long e = TimeSteward.currentTimeMillis() - st;
				if (e > MAX_ELAPSED)
					log(r.getClass().toString(), e);
			}
		}
		catch (Exception e) {
			getHandler().handle(e);
		}
	}

	/** Run an invokable on the swing thread */
	static public void runSwing(final Invokable r) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				runNow(r);
			}
		});
	}

	/** Don't allow instantiation */
	private SwingRunner() { }
}
