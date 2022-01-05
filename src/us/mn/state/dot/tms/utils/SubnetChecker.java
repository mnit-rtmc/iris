/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020-2021  SRF Consulting Group
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttributeHelper;

/** SubnetChecker is a "static utility" class
 *  that identifies which subnet the Iris client
 *  is running on using a configurable series of
 *  TCP and/or ICMP pings.
 *  
 *  SubnetChecker's background ping-thread is
 *  started by a static initialization-block
 *  at the very end of the class.
 *  
 * @author John L. Stanley - SRF Consulting
 */
public final class SubnetChecker {

	/** A single thread for the checker */
	static private SubnetChecker2 THREAD;

	/** Default target array uses www.google.com
	 * to detect if we have generic "internet"
	 * access.  This is replaced by IRIS system-
	 * attribute table "subnet_target_#" entries,
	 * if any exist.
	 */
	private static String[] DEFAULT_TARGETS = {
		"www.google.com=internet",    // icmp ping
		"www.google.com:80=internet", // tcp ping
	};

	/** Array of ping targets and subnet names. */
	private static String[] TARGETS = DEFAULT_TARGETS;

	/** Maximum wait time for failed ping.
	 * Must be less than the default timeout
	 * for both a socket connection and a
	 * command-line ping command. {3 seconds}
	 */
	private final static int TIMEOUT_MS = 3 * 1000;

	/** Constant for unknown subnet name */
	public final static String UNKNOWN = "unknown";

	/** Current subnet */
	private static String subnetName = "";

	/** Flag to request a recheck */
	private static boolean updateReq = false;

	/** Time from end of previous update to start
	 *  of next automatic update. {1 minute} */
	private final static long AUTO_RETRY_MS = 60*1000;

	/** Operating system dependent ping retry-count command-line-prefix */
	private static String ping_retry_arg;

	/** Get current subnet name */
	public static String getSubnetName() {
		return subnetName.isEmpty() ? UNKNOWN : subnetName;
	}

	/** Request an update */
	public static void reqUpdate() {
		updateReq = true;
	}

	/** List of current addresses on this machine */
	static private String ipAddressList;

	/** Prevent instantiation of a "SubnetChecker" object */
	private SubnetChecker() {
	}

	/** Start the SubnetChecker.
	 * This method can be called immediately
	 * after the client-side system_properties
	 * cache is loaded to minimize the small
	 * delay between first referencing this class
	 * and the class reporting the correct subnet.
	 */
	static public void start() {
		// Getting to this point means the checker
		// thread has been started, so nothing
		// else needs to be done...
	}

	/** Load subnet target list.
	 * Searches system-attributes for ping target
	 * attributes named "subnet_target_#" where #
	 * is a positive int in the range 0..MAX_INT.
	 * Stops searching after 10 consecutive fails
	 * or if the attribute-name gets too long.
	 */
	static private void loadSubnetTargets() {
		int i = 0, gap = 0;
		String aName, target;
		List<String> list = new ArrayList<String>();
		while ((i >= 0) && (gap < 10)) {
			aName = "subnet_target_"+i;
			if (aName.length() > SystemAttribute.MAXLEN_ANAME)
				break; // VERY unlikely
			++i;
			target = getTarget(aName);
			if (target == null) {
				// missing-attribute or target-string-error
				++gap;
				continue;
			}
			// Found a valid target string
			list.add(target);
			gap = 0;
		}
		// Use default targets unless we found at
		// least one valid subnet_target_# entry.
		if (list.isEmpty() == false)
			TARGETS = list.stream().toArray(String[]::new);
		else
			TARGETS = DEFAULT_TARGETS;
	}

	/** Try to load a subnet-target attribute.
	 *  Tests target string for basic format rules.
	 *
	 * @return Target string if found.  Null if no
	 *  such attribute or target string is malformed.
	 */
	static private String getTarget(String aName) {
		SystemAttribute attrib = SystemAttributeHelper.get(aName);
		if (attrib == null)
			return null; // missing attribute
		String target = attrib.getValue();
		String [] strs = target.split("=");
		if (strs.length != 2) {
			System.out.println(
				"SubnetChecker: Target error 1: "+aName+": \""+target+"\"");
			return null;
		}
		strs = strs[0].trim().split(":");
		if ((strs.length < 1) || (strs.length > 2)) {
			System.out.println(
				"SubnetChecker: Target error 2: "+aName+": \""+target+"\"");
			return null;
		}
		return target;
	}

	/** SubnetChecker's ping-thread daemon */
	static private class SubnetChecker2 extends Thread {

		/** Initialize the thread. */
		private SubnetChecker2() {
			super("SubnetChecker");
		}

		/** Main checker loop.
		 *  Exits when program ends. */
		@Override
		public void run() {
			// the main checker loop
			while (true) {
				loadSubnetTargets();
				doPings();
				updateReq = false;
				doWait();
			}
		}

		/** Wait for one of the following:
		 *		Time for an automatic update
		 * 		Workstation IP-address change
		 * 		Workstation "sleep-mode" detection
		 * 		An update request
		 */
		private void doWait() {
			long now = System.currentTimeMillis();
			long end = now + AUTO_RETRY_MS;
			long prev = now;
			long delta;
			String tmp;
			while (true) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
				if (updateReq) {
					System.out.println("SubnetChecker: Update requested");
					break;
				}
				// time for automatic update?
				now = System.currentTimeMillis();
				if (now > end) {
					break; // yes
				}
				// snooze or time-change detected?
				delta = Math.abs(now - prev);
				if (delta > 10000) {
					System.out.println("SubnetChecker: Sleep-mode detected: "+delta);
					break;  // yes
				}
				// IP address change?
				tmp = getAllMyAddresses();
				if (!ipAddressList.equals(tmp)) {
					ipAddressList = tmp;
					System.out.println("SubnetChecker: Address change detected");
					break;  // yes
				}
				prev = now;
			}
		}

		/** Ping hosts and set SUBNET_NAME */
		private void doPings() {
			String target, host, port, subnet;
			String [] strs;
			int len = TARGETS.length;
			boolean gotPing;
			for (int i = 0; (i < len); ++i) {
				target = TARGETS[i];
				strs = target.split("=");
				if (strs.length != 2) {
					System.out.println(
						"Subnet target error: \""+target+"\"");
					continue;  // bad target string
				}
				host = strs[0].trim();
				subnet = strs[1].trim();
				strs = host.split(":");
				if (strs.length == 2) {
					// do a TCP ping
					host = strs[0].trim();
					port = strs[1].trim();
					gotPing = doTcpPing(host, Integer.parseInt(port));
				}
				else {
					// do an ICMP (standard) ping
					gotPing = doIcmpPing(host);
				}
				if (gotPing) {
					if (!subnetName.equals(subnet)) {
						subnetName = subnet;
						System.out.println("SubnetChecker.subnet = "+subnet);
					}
					return;
				}
			}
			// didn't get any responses...
			if (!subnetName.equals(UNKNOWN)) {
				subnetName = UNKNOWN;
				System.out.println("SubnetChecker.subnet = "+UNKNOWN);
			}
		}
	}
	
	/** Do an ICMP ping */
	public static boolean doIcmpPing(String host) {
		// Make sure we can resolve the host name to an IP address
		InetSocketAddress sockaddr = new InetSocketAddress(host, 0);
		if (sockaddr.isUnresolved())
			return false;

		// Run command line ping with a timeout of 5 seconds
		ProcessBuilder processBuilder = new ProcessBuilder("ping",
			ping_retry_arg, "1", "-w", "5000", host);
		try {
			Process proc = processBuilder.start();
			return proc.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		}
	}

	/** Do a TCP ping (TCP connect and instant disconnect) */
	public static boolean doTcpPing(String host, int port) {
		try {
			InetSocketAddress sockaddr = new InetSocketAddress(host, port);
			if (sockaddr.isUnresolved())
				return false;
			try (Socket soc = new Socket()) {
				soc.connect(sockaddr, TIMEOUT_MS);
			}
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	/** Get network interfaces.
	 * Requests network interfaces up to 5 times to
	 * deal with rare getNetworkInterfaces() error.
	 * (See Java Bug System: JDK-8165665)
	 * @return enumeration of interfaces or null */
	static private Enumeration<NetworkInterface> getNetworkInterfaces2() {
		for (int i = 1; (i < 6); ++i) {
			try {
				return NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e) {
				// try again
			} catch (java.lang.Error e2) {
				// try again
			}
		}
		return null;
	}
	
	/** Get string containing comma separated
	 *  list of all current IP addresses for
	 *  this workstation. */
	static public String getAllMyAddresses() {
		Enumeration<NetworkInterface> eni;
		eni = getNetworkInterfaces2();
		if (eni == null)
			return "";
		StringBuilder sb = new StringBuilder();
		NetworkInterface ni;
		Enumeration<InetAddress> eAddr;
		InetAddress addr;
		while (eni.hasMoreElements()) {
			ni = eni.nextElement();
			eAddr = ni.getInetAddresses();
			while (eAddr.hasMoreElements()) {
				addr = eAddr.nextElement();
				if (sb.length() > 0)
					sb.append(',');
				sb.append(addr.getHostAddress());
			}
		}
		return sb.toString();
	}

	/* To avoid static-initializer-order errors,
	 * this static block of code should be the
	 * VERY LAST definition in the class.
	 * 
	 * When SubnetChecker is first referenced,
	 * by a call to SubnetChecker.start() or
	 * SubnetChecker.getSubnetName(), the
	 * following code automatically starts
	 * the checker. */
	static {
		// Initialize work variables
		boolean bWindows = System.getProperty("os.name")
			.toLowerCase().contains("win");
		ping_retry_arg = bWindows ? "-n" : "-c";
		ipAddressList = getAllMyAddresses();

		// start the background ping-thread
		THREAD = new SubnetChecker2();
		THREAD.setDaemon(true);
		THREAD.start();
	}
}
