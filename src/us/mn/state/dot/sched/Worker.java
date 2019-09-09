/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
package us.mn.state.dot.sched;

import java.util.PriorityQueue;

/**
 * Work for performing work on a dedicated thread.
 *
 * @author Douglas Lau
 */
public final class Worker {

	/** Worker thread group */
	static private final ThreadGroup GROUP = new ThreadGroup("WORKER");

	/** Exception handler */
	private final ExceptionHandler handler;

	/** Handle an exception */
	private void handleException(Exception e) {
		handler.handle(e);
	}

	/** Thread for running work */
	private final Thread thread;

	/** Set of work to do */
	private final PriorityQueue<Work> todo = new PriorityQueue<Work>();

	/** Flag to indicate disposing */
	private boolean disposing = false;

	/** Create a Worker */
	public Worker(String name, ExceptionHandler h) {
		handler = h;
		thread = new Thread(GROUP, name) {
			public void run() {
				try {
					performWork();
				}
				catch (InterruptedException e) {
					if (!disposing)
						handleException(e);
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	/** Perform work*/
	private void performWork() throws InterruptedException {
		Work w = waitWork();
		while (!thread.isInterrupted()) {
			performWork(w);
			w = waitWork();
		}
	}

	/** Wait until work needs to be performed.
	 * @return Work to be performed. */
	private synchronized Work waitWork() throws InterruptedException {
		while (todo.isEmpty())
			wait();
		return todo.poll();
	}

	/** Perform Work */
	private void performWork(Work w) {
		try {
			w.performWork();
		}
		catch (Exception e) {
			handleException(e);
		}
		catch (VirtualMachineError e) {
			System.err.println("VIRTUAL MACHINE ERROR");
			e.printStackTrace();
			System.err.println("FATAL: RESTARTING");
			System.exit(1);
		}
	}

	/** Add work to perform */
	public synchronized void addWork(Work w) {
		todo.add(w);
		notify();
	}

	/** Test if the current thread is the worker thread */
	public boolean isCurrentThread() {
		return Thread.currentThread() == thread;
	}

	/** Dispose of the worker */
	public void dispose() {
		disposing = true;
		thread.interrupt();
	}

	/** Get the count of work in the queue */
	public synchronized int size() {
		return todo.size();
	}
}
