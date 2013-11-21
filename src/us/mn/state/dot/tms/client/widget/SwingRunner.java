/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.TimeSteward;

/**
 * SwingRunner is a simple utility class for running code on the Swing thread.
 * A warning message is printed if the time elapsed is too long.
 *
 * @author Douglas Lau
 */
public final class SwingRunner {

	/** Don't allow instantiation */
	private SwingRunner() { }

	/** Run some runnable code */
	static private void runNow(Runnable r) {
		long start = TimeSteward.currentTimeMillis();
		r.run();
		long e = TimeSteward.currentTimeMillis() - start;
		if(e > 50) {
			System.err.println("SwingRunner took " + e + " ms");
			System.err.println("  from: " + r.getClass());
		}
	}

	/** Invoke a Runnable later on the swing thread */
	static private void invokeLater(final Runnable r) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				runNow(r);
			}
		});
	}

	/** Invoke a Runnable on the swing thread */
	static public void runSwing(final Runnable r) {
		if(SwingUtilities.isEventDispatchThread())
			runNow(r);
		else
			invokeLater(r);
	}
}
