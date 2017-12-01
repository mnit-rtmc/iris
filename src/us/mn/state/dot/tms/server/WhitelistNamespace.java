/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2017  Minnesota Department of Transportation
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Properties;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.utils.CIDRAddress;

/**
 * A namespace which checks client IP addresses against a whitelist.
 *
 * @author Douglas Lau
 */
public class WhitelistNamespace extends ServerNamespace {

	/** Check if type is checked */
	static private boolean isTypeChecked(String t) {
		return GateArmArray.SONAR_TYPE.equals(t);
	}

	/** Whitelist of CIDR addresses */
	private final LinkedList<CIDRAddress> whitelist =
		new LinkedList<CIDRAddress>();

	/** Create the whitelist namespace */
	public WhitelistNamespace(Properties props) throws UnknownHostException,
		NumberFormatException
	{
		String wl = props.getProperty("gate.arm.whitelist");
		if (wl != null) {
			for (String c: wl.split("[ \t,]+"))
				whitelist.add(new CIDRAddress(c));
		}
	}

	/** Check if address is in whitelist */
	private boolean checkList(InetAddress a) {
		for (CIDRAddress cidr: whitelist) {
			if (cidr.matches(a))
				return true;
		}
		return false;
	}

	/** Check name and whitelist */
	private boolean checkList(Name name, InetAddress a) {
		return (!isTypeChecked(name.getTypePart())) || checkList(a);
	}

	/** Check if a user has write privileges.
	 * @param name Name to check.
	 * @param u User to check.
	 * @param a Inet address of connection.
	 * @return true if update is allowed; false otherwise. */
	@Override
	public boolean canWrite(Name name, User u, InetAddress a) {
		return checkList(name, a) && canWrite(name, u);
	}
}
