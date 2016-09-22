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

/**
 * The selector thread performs non-blocking I/O on a set of channels.
 *
 * @author Douglas Lau
 */
public final class SelectorThread {

	/** Singleton comm selector */
	static public CommSelector TASK;

	/** Thread group for selector thread */
	static private final ThreadGroup GROUP = new ThreadGroup("Selector");

	/** Thread to run select loop */
	private final Thread thread;

	/** Create a new selector thread */
	public SelectorThread() {
 		thread = new Thread(GROUP, "selector") {
			@Override public void run() {
				doRun();
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	/** Run the thread */
	private void doRun() {
		try (CommSelector task = new CommSelector()) {
			TASK = task;
			task.selectLoop();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
