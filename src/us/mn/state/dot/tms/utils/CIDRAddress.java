/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2018  Minnesota Department of Transportation
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
import java.util.ArrayList;
import java.util.List;

/**
 * CIDR (Classless Inter-Domain Routing) Address.
 *
 * @author Douglas Lau
 */
public class CIDRAddress {

	/** Parse a delimited list of CIDR addresses */
	static public List<CIDRAddress> parseList(String p) {
		ArrayList<CIDRAddress> l = new ArrayList<CIDRAddress>();
		if (p != null) {
			for (String c: p.split("[ \t,]+")) {
				l.add(new CIDRAddress(c));
			}
		}
		return l;
	}

	/** Prefix address */
	private final String prefix_address;

	/** Prefix bits */
	private final Integer prefix_bits;

	/** Create a new CIDR address */
	public CIDRAddress(String a) throws IllegalArgumentException,
		NumberFormatException
	{
		String[] p = a.split("/");
		if (p.length > 2)
			throw new IllegalArgumentException("Invalid CIDR");
		prefix_address = p[0];
		prefix_bits = (p.length > 1) ? Integer.parseInt(p[1]) : null;
	}

	/** Test if an inet address matches */
	public boolean matches(InetAddress a) {
		try {
			return tryMatches(a);
		}
		catch (UnknownHostException e) {
			return false;
		}
	}

	/** Try to test if an inet address matches */
	private boolean tryMatches(InetAddress a) throws UnknownHostException {
		byte[] ad = a.getAddress();
		byte[] dom = getDomainAddress();
		if (ad.length != dom.length)
			return false;
		int n_bits = getPrefixBits(dom);
		int n_bytes = getFullBytes(n_bits);
		// Test full bytes in address
		for (int b = 0; b < n_bytes; b++) {
			if (ad[b] != dom[b])
				return false;
		}
		// Test trailing bits in address
		int mask = makeMask(n_bits);
		return n_bytes >= dom.length ||
		       ((ad[n_bytes] & mask) == (dom[n_bytes] & mask));
	}

	/** Get the network domain bytes */
	private byte[] getDomainAddress() throws UnknownHostException {
		return InetAddress.getByName(prefix_address).getAddress();
	}

	/** Get the number of bits in the prefix */
	private int getPrefixBits(byte[] dom) {
		return (prefix_bits != null) ? prefix_bits : dom.length * 8;
	}

	/** Get the number of full bytes to match */
	private int getFullBytes(int n_bits) {
		return n_bits / 8;
	}

	/** Make bit mask for final byte */
	private int makeMask(int n_bits) {
		int m = 0;
		for (int b = 0; b < n_bits % 8; b++) {
			m >>= 1;
			m |= 0x80;
		}
		return m;
	}
}
