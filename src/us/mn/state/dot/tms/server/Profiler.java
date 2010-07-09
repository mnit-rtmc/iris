/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.STime;

/**
 * The server profiler is used to periodically write interesting server 
 * statistics to a persistent log.  This is used for long-term IRIS 
 * reliability tracking.  It also can log memory and thread usage.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class Profiler {

	/** Constant value of one megabyte */
	static protected final float MIB = 1024.0f * 1024.0f;

	/** Profile debug log */
	protected final IDebugLog PROFILE_LOG = new IDebugLog("profile");

	/** Runtime used to get memory information */
	protected final Runtime jvm = Runtime.getRuntime();

	/** OS bean */
	protected final OperatingSystemMXBean osbean = 
		ManagementFactory.getOperatingSystemMXBean();

	/** Debug memory profiling information */
	public void debugMemory() {
		if(PROFILE_LOG.isOpen()) {
			long free = jvm.freeMemory();
			long total = jvm.totalMemory();
			PROFILE_LOG.log("Free memory: " + free / MIB + "MiB");
			PROFILE_LOG.log("Total memory: " + total / MIB + "MiB");
		}
	}

	/** Debug thread profiling information for all threads */
	public void debugThreads() {
		if(PROFILE_LOG.isOpen()) {
			ThreadGroup g = Thread.currentThread().getThreadGroup();
			while(g.getParent() != null)
				g = g.getParent();
			debugThreads(g, 0);
		}
	}

	/** Print thread profiling information for the specified group */
	protected void debugThreads(ThreadGroup group, int deep) {
		Thread[] thread = new Thread[group.activeCount() + 1];
		int count = group.enumerate(thread, false);
		StringBuilder g = new StringBuilder();
		while(g.length() < deep)
			g.append(" ");
		g.append(group.getName());
		g.append(" thread group: ");
		g.append(count);
		while(g.length() < 66)
			g.append(" ");
		g.append(group.getMaxPriority());
		while(g.length() < 68)
			g.insert(66, " ");
		PROFILE_LOG.log(g.toString());
		for(int i = 0; i < count; i++) {
			StringBuilder t = new StringBuilder();
			while(t.length() < 4 + deep)
				t.insert(0, " ");
			t.append(thread[i].getName());
			if(!thread[i].isAlive())
				t.append(" (dead)");
			if(thread[i].isDaemon())
				t.append(" (daemon)");
			while(t.length() < 66)
				t.append(" ");
			t.append(thread[i].getPriority());
			while(t.length() < 68)
				t.insert(66, " ");
			PROFILE_LOG.log(t.toString());
		}
		ThreadGroup[] groups =
			new ThreadGroup[group.activeGroupCount() + 1];
		count = group.enumerate(groups, false);
		for(int i = 0; i < count; i++)
			debugThreads(groups[i], deep + 2);
	}

	/** Append to uptime log */
	public void appendUptimeLog() throws IOException {
		String fn = SystemAttrEnum.UPTIME_LOG_FILENAME.getString();
		if(fn == null || fn.length() <= 0 ) {
			PROFILE_LOG.log("warning: bogus file name: " + fn);
			return;
		}
		File f = new File(fn);
		FileOutputStream fos = new FileOutputStream(f, true);
		try {
			PrintWriter pw = new PrintWriter(fos);
			pw.println(createLogEntry());
		}
		finally {
			fos.close();
		}
	}

	/** Create a log entry */
	protected String createLogEntry() {
		StringBuilder sb = new StringBuilder();

		// date and time in UTC
		sb.append(STime.getCurDateTimeString(false));
		sb.append(',');

		// cpu utilization in last 60 seconds as int, e.g. 13 is 13%
		sb.append((int)Math.round(100 * osbean.getSystemLoadAverage()));
		sb.append(',');

		// heap size
		long heapsize = jvm.totalMemory() - jvm.freeMemory();
		sb.append(heapsize);
		sb.append(',');

		// number of user connections
		sb.append(getConnectionCount());

		return sb.toString();
	}

	/** Get the current connection count */
	protected int getConnectionCount() {
		Namespace ns = BaseHelper.namespace;
		if(ns != null)
			return ns.getCount(Connection.SONAR_TYPE);
		else
			return -1;
	}
}
