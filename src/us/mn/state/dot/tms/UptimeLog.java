/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.Runtime;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.STime;

/**
 * The server uptime log, used to periodically write interesting server 
 * statistics to a persistent log. This is used for long-term IRIS 
 * reliability tracking.
 *
 * @author Michael Darter
 * @created 01/22/09
 * @see TMSImpl
 */
public class UptimeLog {

	/** log file name */
	protected String m_fname = "";

	/** server namespace */
	protected ServerNamespace m_namespace = null;

	/** runtime */
	protected final Runtime m_rt = Runtime.getRuntime();

	/** OS bean */
	protected final OperatingSystemMXBean m_osb = 
		ManagementFactory.getOperatingSystemMXBean();

	/** write to iris server uptime log */
	public static void writeServerLog(ServerNamespace namespace) {
		if(!SystemAttrEnum.UPTIME_LOG_ENABLE.getBoolean())
			return;
		if(namespace == null)
			return;
		String fname = SystemAttrEnum.UPTIME_LOG_FILENAME.getString();
		if(fname == null || fname.length() <= 0 ) {
			System.err.println("UptimeLog.writeServerLog(): " +
				"warning: bogus file name: "+fname);
			return;
		}
		UptimeLog log = new UptimeLog(fname, namespace);
		if(!log.write()) {
			System.err.println("Warning: failed to append to " +
				fname);
		}
	}

	/** Create a new uptime log.
	 *  @param fname Log file name. */
	public UptimeLog(String fname, ServerNamespace namespace) {
		m_fname = fname;
		m_namespace = namespace;
	}

	/** Append to uptime log.
	 *  @return true on success else false on error. */
	public boolean write() {
		if(m_fname == null || m_fname.length() <= 0) {
			System.err.println("UptimeLog.write(): warning: " + 
				" bogus name: " + m_fname);
			return false;
		}

		OutputStream os = null;
		boolean ok = false;
		try {
			File f = new File(m_fname);
			os = new FileOutputStream(f.getAbsolutePath(), true);
			ok = appendLog(os);
		}
		catch(IOException ex) {
			System.err.println("UptimeLog.write(): ex: " + ex);
			return false;
		}
		catch(Exception ex) {
			System.err.println("UptimeLog.write(): ex: " + ex);
			ex.printStackTrace();
			return false;
		}
		finally {
			if(!close(os))
				return false;
		}
		return ok;
	}

	/** append to log */
	protected boolean appendLog(OutputStream os) {
		if(os == null)
			return false;

		StringBuilder sb = new StringBuilder();

		// date and time in UTC
		sb.append(STime.getCurDateTimeString(false));
		sb.append(',');

		// cpu utilization in last 60 seconds as int, e.g. 13 is 13%
		sb.append((int)Math.round(100 * m_osb.getSystemLoadAverage()));
		sb.append(',');

		// heap size
		long heapsize = m_rt.totalMemory() - m_rt.freeMemory();
		sb.append(heapsize);
		sb.append(',');

		// number of user connections
		sb.append(m_namespace.getCount(Connection.SONAR_TYPE));
		sb.append('\n');

		// write
		try {
			os.write(sb.toString().getBytes());
		}
		catch(IOException ex) {
			System.err.println("Warning: UptimeLog.appendLog(): " +
				ex);
			return false;
		}
		catch(Exception ex) {
			System.err.println("Warning: UptimeLog.appendLog(): " +
				ex);
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/** close */
	protected boolean close(OutputStream os) {
		try {
			if(os != null)
				os.close();
			return true;
		}
		catch(Exception e) {
			System.err.println("Warning: UptimeLog.close(): " + e);
		}
		return false;
	}
}
