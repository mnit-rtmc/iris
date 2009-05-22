/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import java.io.PrintStream;

/**
 * Profile is used to profile the memory and thread usage
 *
 * @author Douglas Lau
 */
public class Profile {

	/** Constant value of one megabyte */
	static protected final float MEGABYTE = 1024.0f * 1024.0f;

	/** Runtime used to get memory information */
	static protected final Runtime JVM = Runtime.getRuntime();

	/** Print memory profiling information */
	static public void printMemory(PrintStream ps) {
		long free = JVM.freeMemory();
		long total = JVM.totalMemory();
		ps.println("Free memory: " + free / MEGABYTE + "M");
		ps.println("Total memory: " + total / MEGABYTE + "M");
	}

	/** Print thread profiling information for all threads */
	static public void printThreads(PrintStream ps) {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		while(group.getParent() != null)
			group = group.getParent();
		printThreads(ps, group, 0);
	}

	/** Print thread profiling information for the specified group */
	static protected void printThreads(PrintStream ps, ThreadGroup group,
		int deep)
	{
		Thread[] thread = new Thread[group.activeCount() + 1];
		int count = group.enumerate(thread, false);
		StringBuffer g = new StringBuffer();
		while(g.length() < deep) g.append(" ");
		g.append(group.getName());
		g.append(" thread group: ");
		g.append(count);
		while(g.length() < 66) g.append(" ");
		g.append(group.getMaxPriority());
		while(g.length() < 68) g.insert(66, " ");
		ps.println(g);
		for(int i = 0; i < count; i++) {
			StringBuffer t = new StringBuffer();
			while(t.length() < 4 + deep) t.insert(0, " ");
			t.append(thread[i].getName());
			if(!thread[i].isAlive()) t.append(" (dead)");
			if(thread[i].isDaemon()) t.append(" (daemon)");
			while(t.length() < 66) t.append(" ");
			t.append(thread[i].getPriority());
			while(t.length() < 68) t.insert(66, " ");
			ps.println(t);
		}
		ThreadGroup[] groups =
			new ThreadGroup[group.activeGroupCount() + 1];
		count = group.enumerate(groups, false);
		for(int i = 0; i < count; i++)
			printThreads(ps, groups[i], deep + 2);
	}
}
