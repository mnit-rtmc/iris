/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
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

package us.mn.state.dot.tms.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.lang.InterruptedException;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.lang.Thread;
import java.util.List;


/**
 * Misc. OS utilities
 *
 * @author Travis Swanston
 */
public class OSUtils {

	/**
	 * Spawn an OS subprocess.  Follows the Oracle-recommended method of
	 * using ProcessBuilder rather than Runtime.exec(), and includes some
	 * safeguards to prevent blocked and stale processes.  A thread is
	 * created that will perform the spawn, consume its output and error
	 * streams (to prevent blocking due to full buffers), then clean up.
	 *
	 * @param cmd The cmd/arg list for execution
	 */
	static public void spawnProcess(final List<String> cmd) {
		if (cmd == null)
			return;
		Thread t = new Thread() {
			public void run() {
				Process proc = null;
				proc = startProcess(cmd, true);
				if (proc != null) {
					consumeProcessOutput(proc);
					try {
						proc.waitFor();
					}
					catch (InterruptedException e) {
						// ignore (we terminate anyway)
					}
					destroyProcess(proc);
				}
				Thread.currentThread().interrupt();
			}
		};
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Start a new process.
	 *
	 * @param cmd The cmd/arg list for execution
	 * @param merge True to merge the process's error stream into its
	 *              output stream
	 * @return The new Process, or null upon failure
	 */
	static private Process startProcess(List<String> cmd, boolean merge) {
		if (cmd == null)
			return null;
		ProcessBuilder pb = new ProcessBuilder(cmd);
		if (merge)
			pb = pb.redirectErrorStream(true);
		Process proc = null;
		try {
			proc = pb.start();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return proc;
	}

	/**
	 * Consume a process's output stream until EOS is reached.
	 * If this method is being used to prevent process blocking due to
	 * full output buffers, then it is recommended that the process be
	 * created with its error stream merged into its output stream,
	 * otherwise blocking can still occur due to a full error stream
	 * buffer.
	 *
	 * @param p The Process whose output stream to consume
	 */
	static private void consumeProcessOutput(Process p) {
		if (p == null)
			return;
		InputStream output = p.getInputStream();
		try {
			while (output.read() != -1) {
				// NOP
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Destroy a Process, first attempting to close its I/O streams.
	 *
	 * @param p The Process to destroy
	 */
	static private void destroyProcess(Process p) {
		if (p == null)
			return;
		InputStream stdout = p.getInputStream();
		InputStream stderr = p.getErrorStream();
		OutputStream stdin = p.getOutputStream();
		try {
			if (stdout != null)
				stdout.close();
			if (stderr != null)
				stderr.close();
			if (stdin != null)
				stdin.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		p.destroy();
	}

}

