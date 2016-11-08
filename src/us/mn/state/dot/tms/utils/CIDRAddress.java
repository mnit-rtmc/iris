/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * CIDR (Classless Inter-Domain Routing) Address.
 *
 * @author Douglas Lau
 */
public class CIDRAddress {

	/** Inet address */
	private final byte[] address;

	/** Prefix bits */
	private final int prefix;

	/** Number of full bytes to match in addresses */
	private final int n_bytes;

	/** Mask of bits in final byte */
	private final int mask;

	/** Create a new CIDR address */
	public CIDRAddress(String a) throws UnknownHostException,
		NumberFormatException
	{
		String[] p = a.split("/");
		if (p.length > 2)
			throw new IllegalArgumentException("Invalid CIDR");
		address = InetAddress.getByName(p[0]).getAddress();
		prefix = (p.length > 1) ? Integer.parseInt(p[1]) : bits();
		mask = makeMask();
		n_bytes = prefix / 8;
	}

	/** Get the number of bits in the address */
	private int bits() {
		return address.length * 8;
	}

	/** Make bit mask for final byte */
	private int makeMask() {
		int m = 0;
		for (int b = 0; b < prefix % 8; b++) {
			m >>= 1;
			m |= 0x80;
		}
		return m;
	}

	/** Test if an inet address matches */
	public boolean matches(InetAddress a) {
		byte[] ad = a.getAddress();
		if (ad.length != address.length)
			return false;
		// Test full bytes in address
		for (int b = 0; b < n_bytes; b++) {
			if (ad[b] != address[b])
				return false;
		}
		// Test trailing bits in address
		return n_bytes >= address.length ||
		       ((ad[n_bytes] & mask) == (address[n_bytes] & mask));
	}
}
