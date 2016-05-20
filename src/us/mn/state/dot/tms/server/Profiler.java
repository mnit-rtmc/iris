/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.NumberFormat;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.BaseHelper;

/**
 * The server profiler is used to periodically write interesting server 
 * statistics to a persistent log.  This is used for long-term IRIS 
 * reliability tracking.  It also can log memory and thread usage.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class Profiler {

	/** Uptime log file name */
	static private final File UPTIME_LOG_FILE =
		new File("/var/www/html/irisuptimelog.csv");

	/** Debug line length */
	static private final int LINE_LEN = 58;

	/** Constant value of one megabyte */
	static private final double MIB = 1024.0 * 1024.0;

	/** Profile debug log */
	private final DebugLog PROFILE_LOG = new DebugLog("profile");

	/** Runtime used to get memory information */
	private final Runtime jvm = Runtime.getRuntime();

	/** OS bean */
	private final OperatingSystemMXBean osbean = 
		ManagementFactory.getOperatingSystemMXBean();

	/** Debug memory profiling information */
	public void debugMemory() {
		if (PROFILE_LOG.isOpen()) {
			long free = jvm.freeMemory();
			long total = jvm.totalMemory();
			PROFILE_LOG.log("Free memory: " + formatMem(free));
			PROFILE_LOG.log("Total memory: " + formatMem(total));
		}
	}

	/** Format a memory value */
	private String formatMem(long mem) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		return nf.format(mem / MIB) + " MiB";
	}

	/** Debug thread profiling information for all threads */
	public void debugThreads() {
		if (PROFILE_LOG.isOpen()) {
			ThreadGroup g = Thread.currentThread().getThreadGroup();
			while (g.getParent() != null)
				g = g.getParent();
			debugThreads(g, 0);
		}
	}

	/** Print thread profiling information for the specified group */
	private void debugThreads(ThreadGroup group, int deep) {
		debugThreadGroup(group, deep);
		ThreadGroup[] groups =
			new ThreadGroup[group.activeGroupCount() + 1];
		int count = group.enumerate(groups, false);
		for (int i = 0; i < count; i++)
			debugThreads(groups[i], deep + 2);
	}

	/** Debug one thread group */
	private void debugThreadGroup(ThreadGroup group, int deep) {
		Thread[] thread = new Thread[group.activeCount() + 1];
		int count = group.enumerate(thread, false);
		StringBuilder sb = new StringBuilder();
		while (sb.length() < deep)
			sb.append(" ");
		sb.append(group.getName());
		sb.append(" thread group: ");
		sb.append(count);
		while (sb.length() < LINE_LEN - 2)
			sb.append(" ");
		sb.append(group.getMaxPriority());
		while (sb.length() < LINE_LEN)
			sb.insert(LINE_LEN - 2, " ");
		PROFILE_LOG.log(sb.toString());
		for (int i = 0; i < count; i++)
			debugThread(thread[i], deep);
	}

	/** Add one thread to debug log */
	private void debugThread(Thread t, int deep) {
		StringBuilder sb = new StringBuilder();
		while (sb.length() < 4 + deep)
			sb.insert(0, " ");
		sb.append(t.getName());
		if (!t.isAlive())
			sb.append(" (dead)");
		if (t.isDaemon())
			sb.append(" (daemon)");
		while (sb.length() < LINE_LEN - 2)
			sb.append(" ");
		sb.append(t.getPriority());
		while (sb.length() < LINE_LEN)
			sb.insert(LINE_LEN - 2, " ");
		PROFILE_LOG.log(sb.toString());
	}

	/** Append to uptime log file */
	public void appendUptimeLog() throws IOException {
		FileWriter fw = new FileWriter(UPTIME_LOG_FILE, true);
		try {
			fw.write(createLogEntry());
		}
		finally {
			fw.close();
		}
	}

	/** Create a log entry */
	private String createLogEntry() {
		StringBuilder sb = new StringBuilder();
		sb.append(TimeSteward.currentDateTimeString(true));
		sb.append(',');
		sb.append(getLoadAvg());
		sb.append(',');
		sb.append(getHeapSize());
		sb.append(',');
		sb.append(getConnectionCount());
		sb.append('\n');
		return sb.toString();
	}

	/** Get the current load average */
	private int getLoadAvg() {
		return (int)Math.round(100 * osbean.getSystemLoadAverage());
	}

	/** Get the size of the heap */
	private long getHeapSize() {
		return jvm.totalMemory() - jvm.freeMemory();
	}

	/** Get the current connection count */
	private int getConnectionCount() {
		Namespace ns = BaseHelper.namespace;
		if(ns != null)
			return ns.getCount(Connection.SONAR_TYPE);
		else
			return -1;
	}
}
