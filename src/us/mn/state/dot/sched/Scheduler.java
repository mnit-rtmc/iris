/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2020  Minnesota Department of Transportation
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

import java.util.Date;
import java.util.TreeSet;

/**
 * Scheduler for performing jobs in a dedicated thread.
 *
 * @author Douglas Lau
 */
public final class Scheduler {

	/** Scheduler thread group */
	static private final ThreadGroup GROUP = new ThreadGroup("SCHED");

	/** Default exception handler */
	static private ExceptionHandler HANDLER = new ExceptionHandler() {
		public boolean handle(Exception e) {
			System.err.println("Scheduler: " +
				Thread.currentThread().getName());
			e.printStackTrace();
			return true;
		}
	};

	/** Set the default exception handler */
	static public void setHandler(ExceptionHandler h) {
		HANDLER = h;
	}

	/** Exception handler */
	private final ExceptionHandler handler;

	/** Handle an exception */
	private void handleException(Exception e) {
		if (handler != null)
			handler.handle(e);
		else
			HANDLER.handle(e);
	}

	/** Thread for running jobs */
	private final Thread thread;

	/** Debug log for scheduled jobs */
	private final DebugLog slog;

	/** Set of scheduled jobs to do */
	private final TreeSet<Job> todo = new TreeSet<Job>();

	/** Set of jobs to remove from scheduler */
	private final TreeSet<Job> toremove = new TreeSet<Job>();

	/** Flag to indicate disposing */
	private boolean disposing = false;

	/** Create a new job scheduler */
	public Scheduler() {
		this("sched");
	}

	/** Create a new job scheduler */
	public Scheduler(String name) {
		this(name, null);
	}

	/** Create a new job scheduler */
	public Scheduler(String name, ExceptionHandler h) {
		slog = new DebugLog(name, h);
		handler = h;
		thread = new Thread(GROUP, name) {
			public void run() {
				try {
					performJobs();
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

	/** Perform jobs as they are scheduled */
	private void performJobs() throws InterruptedException {
		Job job = waitJob();
		while (!thread.isInterrupted()) {
			performJob(job);
			if (job.isRepeating())
				repeatJob(job);
			removeJobs();
			job = waitJob();
		}
	}

	/** Wait until the next job needs to be performed.
	 * @return Job to be performed. */
	private synchronized Job waitJob() throws InterruptedException {
		Job job = nextJob();
		long delay = job.delay();
		while (delay > 0) {
			TimeSteward.wait(this, delay);
			// We need to check the next job here in case the job
			// was removed or a new job was added while we were
			// waiting
			job = nextJob();
			delay = job.delay();
		}
		todo.remove(job);
		return job;
	}

	/** Get the next job on the "todo" list */
	private synchronized Job nextJob() throws InterruptedException {
		while (todo.isEmpty()) {
			wait();
		}
		return todo.first();
	}

	/** Perform a job */
	private void performJob(Job job) {
		boolean op = slog.isOpen();
		try {
			if (op)
				slog.log("Starting " + job.getName());
			job.performTask();
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
		finally {
			if (op)
				slog.log("Finished " + job.getName());
		}
	}

	/** Add a job to be repeated */
	private synchronized void repeatJob(Job job) {
		job.computeNextTime();
		todo.add(job);
	}

	/** Add a job for this scheduler to perform */
	public synchronized void addJob(Job job) {
		todo.add(job);
		if (slog.isOpen() && todo.size() > 5000)
			slog.log("OVERLOADED: " + todo.size());
		notify();
	}

	/** Remove a job from this scheduler */
	public synchronized void removeJob(Job job) {
		if (job != null) {
			toremove.add(job);
			notify();
		}
	}

	/** Remove jobs which need to be removed.  This needs to be done on the
	 * scheduler thread in case the job is being performed while removeJob
	 * is called. */
	private synchronized void removeJobs() {
		for (Job job: toremove)
			todo.remove(job);
		toremove.clear();
	}

	/** Test if the current thread is the scheduler thread */
	public boolean isCurrentThread() {
		return Thread.currentThread() == thread;
	}

	/** Dispose of the scheduler */
	public void dispose() {
		disposing = true;
		thread.interrupt();
	}
}
