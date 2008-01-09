/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeSet;

/**
 * Scheduler for performing jobs in a dedicated thread.
 *
 * @author Douglas Lau
 */
public final class Scheduler extends Thread {

	/** Job identifier */
	static protected long jobId = 0;

	/** Job for the scheduler to perform */
	static public abstract class Job implements Comparable {

		/** Next time this job must be performed */
		protected final Date nextTime;

		/** Time interval to perform this job */
		protected final long interval;

		/** Time offset from whole interval boundary */
		protected final long offset;

		/** Unique job identifier */
		protected final long id = jobId++;

		/**
		 * Create a new scheduler job
		 * @param iField java.util.Calendar field for time interval
		 * @param i Time interval to schedule the job
		 * @param oField java.util.Calendar field for interval offset
		 * @param o Time interval offset to schedule the job
		 */
		public Job(int iField, int i, int oField, int o) {
			if(i < 0) throw new
				IllegalArgumentException("Bad interval");
			Date time = new Date(0);
			Calendar c = Calendar.getInstance();
			c.setTime(time);
			c.add(iField, i);
			interval = c.getTime().getTime();
			c.setTime(time);
			c.add(oField, o);
			offset = c.getTime().getTime();
			if(o < 0 || offset >= interval) throw new
				IllegalArgumentException("Bad offset");
			nextTime = new Date();
			computeNextTime();
		}

		/**
		 * Create a new scheduler job
		 * @param iField java.util.Calendar field for time interval
		 * @param i Time interval to schedule the job
		 */
		public Job(int iField, int i) {
			this(iField, i, Calendar.SECOND, 0);
		}

		/** Create a one-shot scheduler job */
		public Job(int milliseconds) {
			interval = 0;
			offset = 0;
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MILLISECOND, milliseconds);
			nextTime = c.getTime();
		}

		/** Compute the next time this job will be scheduled */
		protected void computeNextTime() {
			Calendar c = Calendar.getInstance();
			long off = offset - c.get(Calendar.ZONE_OFFSET) -
				c.get(Calendar.DST_OFFSET);
			long now = c.getTime().getTime();
			long last = (now - off) / interval * interval;
			if(last > nextTime.getTime() + off)
				System.err.println(nextTime +
				" MISSED EVENT, interval: " + interval);
			nextTime.setTime(last + interval + off);
		}

		/** Perform the task for this job */
		public void performTask() throws Exception {
			if(interval > 0)
				computeNextTime();
			try { perform(); }
			finally {
				complete();
			}
		}

		/** Actual "job" to be performed */
		abstract public void perform() throws Exception;

		/** Do this upon completion of the job */
		public void complete() {}

		/** Compare this job with another one */
		public int compareTo(Object o) {
			Job other = (Job)o;
			long c = nextTime.compareTo(other.nextTime);
			if(c == 0)
				c = interval - other.interval;
			if(c == 0)
				c = offset - other.offset;
			if(c == 0)
				c = id - other.id;
			if(c < 0)
				return -1;
			if(c > 0)
				return 1;
			return 0;
		}
	}

	/** Scheduler exception handler */
	static public interface ExceptionHandler {
		public void handleException(Exception e);
	}

	/** Set of scheduled jobs to do */
	protected final TreeSet<Job> todo = new TreeSet<Job>();

	/** Scheduler's exception handler */
	protected ExceptionHandler handler;

	/** Create a new job scheduler with the default exception handler */
	public Scheduler(String name) {
		this(name, new ExceptionHandler() {
			public void handleException(Exception e) {
				e.printStackTrace();
			}
		});
	}

	/** Create a new job scheduler with a custom exception handler */
	public Scheduler(ExceptionHandler h) {
		this("Job Scheduler", h);
	}

	/** Create a new job scheduler with a custom exception handler */
	public Scheduler(String name, ExceptionHandler h) {
		super(name);
		handler = h;
		setDaemon(true);
		start();
	}

	/** Set a new exception handler */
	public void setHandler(ExceptionHandler h) {
		handler = h;
	}

	/** Get the next job on the "todo" list */
	protected synchronized Job firstJob() {
		while(todo.isEmpty()) {
			try { wait(); }
			catch(InterruptedException e) {
				handler.handleException(e);
			}
		}
		return todo.first();
	}

	/** Get the next job for the scheduler to perform */
	protected synchronized Job nextJob(Job job) {
		if(job != null && job.interval > 0)
			todo.add(job);
		job = firstJob();
		long delay = job.nextTime.getTime() -
			System.currentTimeMillis();
		while(delay > 0) {
			try { wait(delay); }
			catch(InterruptedException e) {
				handler.handleException(e);
			}
			job = firstJob();
			delay = job.nextTime.getTime() -
				System.currentTimeMillis();
		}
		todo.remove(job);
		return job;
	}

	/** Process all scheduled jobs */
	public void run() {
		Job job = nextJob(null);
		while(!isInterrupted()) {
			try { job.performTask(); }
			catch(Exception e) {
				handler.handleException(e);
			}
			catch(VirtualMachineError e) {
				System.err.println("VIRTUAL MACHINE ERROR");
				e.printStackTrace();
				System.err.println("FATAL: RESTARTING");
				System.exit(1);
			}
			job = nextJob(job);
		}
	}

	/** Add a (runnable) job for this worker thread to perform */
	public synchronized void addJob(Job job) {
		todo.add(job);
		notify();
	}

	/** Remove a (runnable) job from this worker thread */
	public synchronized void removeJob(Job job) {
		try {
			todo.remove(job);
			notify();
		}
		catch(ClassCastException e) {
			System.err.println("NO SUCH JOB");
		}
	}
}
