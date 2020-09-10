/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

/** SubnetChecker is a self-launching daemon thread
 *  that identifies which subnet the Iris client is
 *  running on using a configurable series of TCP
 *  and/or ICMP pings.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class SubnetChecker extends Thread {

	/** A single thread for the checker */
	static SubnetChecker THREAD;

	/** When SubnetChecker is first referenced,
	 *  this creates an instance and runs it. */
	static {
		THREAD = new SubnetChecker();
		THREAD.setDaemon(true);
		THREAD.start();
	}

	/** List of ping targets and subnet names.
	 * Default list uses google.com to detect
	 * if we have generic internet access.
	 * This is replaced by system-attribute
	 * subnet_target_# entries, if any exist.
	 */
	private static String[] TARGETS = {
		"www.google.com=internet",    // icmp ping
		"www.google.com:80=internet", // tcp ping
	};

	/** Maximum wait time for failed ping.
	 * Must be less than the default timeout
	 * for both a socket connection and a
	 * command-line ping command. {3 seconds}
	 */
	private final static int TIMEOUT_MS = 3 * 1000;

	/** Constant for unknown subnet name */
	public final static String UNKNOWN = "unknown";

	/** Current subnet */
	private static String subnetName = UNKNOWN;

	/** Flag to request a recheck */
	private static boolean updateReq = false;

	/** Time from end of previous update to start
	 *  of next automatic update. {1 minute} */
	private final static long AUTO_RETRY_MS = 60*1000;

	/** Is this machine running Windows? */
	private static boolean IS_WINDOWS = System.getProperty("os.name")
		.toLowerCase().contains("win");

	/** Operating system dependent ping retry-count command-line-prefix */
	private static String PING_RETRY_ARG = IS_WINDOWS ? "-n" : "-c";

	/** Get current subnet name */
	public static String getSubnetName() {
		return subnetName;
	}

	/** Request an update */
	public static void reqUpdate() {
		updateReq = true;
	}

	/** List of current addresses on this machine */
	private String ipAddressList;

	/** Construct/initialize the subnet checker */
	private SubnetChecker() {
		super("SubnetChecker");
		loadSubnetTargets();
		ipAddressList = getAllMyAddresses();
	}

	/** Load subnet target list.
	 *
	 * Searches system-attributes for ping target
	 * attributes named "subnet_target_#" where #
	 * is a positive int in the range 0..MAX_INT.
	 * Stops searching after 10 consecutive fails
	 * or if the attribute-name gets too long.
	 */
	private void loadSubnetTargets() {
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
				// missing attribute
				++gap;
				continue;
			}
			else if (target.isEmpty()) {
				// target string error
				System.err.println(
					"System attribute error: "+aName);
				++gap;
				continue;
			}
			// Found a valid target string
			list.add(target);
			gap = 0;
		}
		// Only override default targets if we found
		// at least one valid subnet_target_# entry.
		if (list.isEmpty() == false)
			TARGETS = list.stream().toArray(String[]::new);
	}

	/** Try to load subnet-target attribute.
	 *  Tests target string for basic format rules.
	 *
	 * @return Target string if found.  Null if no
	 *  such attribute.  Zero-length string if
	 *  target string is malformed.
	 */
	private String getTarget(String aName) {
		SystemAttribute attrib = SystemAttributeHelper.get(aName);
		if (attrib == null)
			return null; // missing attribute
		String target = attrib.getValue();
		String [] strs = target.split("=");
		if (strs.length != 2)
			return ""; // bad target string
		strs = strs[0].trim().split(":");
		if ((strs.length < 1) || (strs.length > 2))
			return ""; // bad target string
		return target;
	}

	/** Main checker loop.
	 *  Exits when program ends. */
	@Override
	public void run() {
		// the main checker loop
		while (true) {
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
				System.err.println(
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
				if (!subnetName.equals(subnet))
					System.out.println("SubnetChecker.subnet = "+subnet);
				subnetName = subnet;
				return;
			}
		}
		// didn't get any responses...
		subnetName = UNKNOWN;
	}

	/** Do an ICMP ping */
	public static boolean doIcmpPing(String host) {
		// Make sure we can resolve the host name to an IP address
		InetSocketAddress sockaddr = new InetSocketAddress(host, 0);
		if (sockaddr.isUnresolved())
			return false;

		// Run command line ping with a timeout of 5 seconds
		ProcessBuilder processBuilder = new ProcessBuilder("ping",
			PING_RETRY_ARG, "1", "-w", "5000", host);
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

	/** Get string containing comma separated
	 *  list of all current IP addresses for
	 *  this workstation. */
	static public String getAllMyAddresses() {
		StringBuilder sb = new StringBuilder();
		Enumeration<NetworkInterface> e;
		try {
			e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = e.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = ee.nextElement();
					if (sb.length() > 0)
						sb.append(',');
					sb.append(i.getHostAddress());
				}
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		return sb.toString();
	}
}
