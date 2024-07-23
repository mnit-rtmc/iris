/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2024  Minnesota Department of Transportation
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
import java.util.List;
import java.util.Properties;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.utils.CidrBlock;

/**
 * A namespace which checks client IP addresses against an allow list.
 *
 * @author Douglas Lau
 */
public class AllowListNamespace extends ServerNamespace {

	/** Check if type is checked */
	static private boolean isTypeChecked(String t) {
		return GateArmArray.SONAR_TYPE.equals(t);
	}

	/** Allow list of CIDR blocks */
	private final List<CidrBlock> allow_list;

	/** Create the allow list namespace */
	public AllowListNamespace(Properties props)
		throws IllegalArgumentException
	{
		allow_list = CidrBlock.parseList(props.getProperty(
			"gate.arm.whitelist"));
	}

	/** Check if address is in allow list */
	private boolean checkList(InetAddress a) {
		for (CidrBlock block: allow_list) {
			if (block.matches(a))
				return true;
		}
		return false;
	}

	/** Check name and allow list */
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
