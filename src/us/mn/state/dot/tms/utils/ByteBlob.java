/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2012  Iteris Inc.
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

import java.util.ArrayList;

/**
 * A byte buffer.
 * @see us.mn.state.dot.tms.utils.ByteBlobTest
 * @author Michael Darter
 */
public class ByteBlob
{
	/** Byte buffer, never null */
	private ArrayList<Byte> m_buffer = new ArrayList<Byte>();

	/** Constructor */
	public ByteBlob() {}

	/** Constructor with initial capacity.
	 * @param ic Initial capacity in bytes */
	public ByteBlob(int ic) {
		m_buffer = new ArrayList<Byte>(ic);
	}

	/** Constructor that takes an array.
	 * @param array byte array, may be null */
	public ByteBlob(byte[] array) {
		array = (array == null ? new byte[0] : array);
		m_buffer = new ArrayList<Byte>(array.length);
		add(array);
	}

	/** Constructor with an array.
	 * @param array Int array, may be null */
	public ByteBlob(int[] array) {
		for(int i : array)
			add(i);
	}

	/** Constructor with blob */
	public ByteBlob(ByteBlob bb) {
		for(int i : bb.toArray())
			add(i);
	}

	/** Constructor, which adds the specified number of elements from the
	 * array, which may be less than or greater than the array length.
	 * @param array byte array, may be null */
	public ByteBlob(int len, byte[] array) {
		len = (len < 0 ? 0 : len);
		array = (array == null ? new byte[0] : array);
		m_buffer = new ArrayList<Byte>(len);
		add(len, array);
	}

	/** Constructor with string */
	public ByteBlob(String s) {
		if(s == null)
			s = "";
		add(s.getBytes());
	}

	/** Clone */
	public ByteBlob clone() {
		return new ByteBlob(toArray());
	}

	/** Is empty */
	public boolean isEmpty() {
		return size() == 0;
	}

	/** Equals */
	public boolean equals(byte[] a) {
		if(a == null || a.length != size())
			return false;
		for(int i = 0; i < size(); ++i)
			if(getByte(i) != a[i])
				return false;
		return true;
	}

	/** Remove all bytes from the container */
	public void clear() {
		m_buffer.clear();
	}

	/** Remove all bytes from the container and add new bytes. */
	public void clear(ByteBlob bb) {
		clear();
		add(bb);
	}

	/** Append a byte blob */
	public ByteBlob add(ByteBlob bb) {
		if(bb != null)
			for(int i = 0; i < bb.size(); ++i)
				add(bb.getByte(i));
		return this;
	}

	/** add a char to the end of the buffer as a byte */
	public ByteBlob add(char c) {
		m_buffer.add((byte)c);
		return this;
	}

	/** add a byte to the end of the buffer */
	public ByteBlob add(byte b) {
		m_buffer.add(b);
		return this;
	}

	/** add an int to the end of the buffer as a single byte value */
	public ByteBlob add(int i) {
		m_buffer.add((byte)i);
		return this;
	}

	/** add an array to the end of the buffer */
	public ByteBlob add(byte[] b) {
		if(b == null)
			b = new byte[0];
		add(b.length, b);
		return this;
	}

	/** Append the specified number of array elements to the buffer.
	 * @param num Number of elements from array to add.
	 * @param b Array elements to add. */
	public void add(int num, byte[] b) {
		assert b != null;
		if(b == null)
			return;
		for(int i = 0; i < num; ++i)
			if(i < b.length)
				add(b[i]);
			else
				add(0);
	}

	/** Get the number of bytes in the buffer */
	public int size() {
		return m_buffer.size();
	}

	/** Get the bytes remaining, from the specified index.
	 * @param i Start index, inclusive. */
	public int size(int i) {
		if(i >= size())
			return 0;
		i = (i < 0 ? 0 : i);
		return size() - i;
	}

	/** Set the size, truncating or expanding as necessary.
	 * @param ns New size. Values less than 0 are assumed to be 0. Values
	 *	     greater than the current size expand the buffer. */
	public ByteBlob setSize(int ns) {
		ns = (ns < 0 ? 0 : ns);
		if(ns == size())
			return this;
		else if(ns < size()) {
			ArrayList<Byte> nb = new ArrayList<Byte>(ns);
			for(int i = 0; i < ns; ++i)
				nb.add(m_buffer.get(i));
			m_buffer = nb;
		} else {
			final int more = ns - size();
			for(int i = 0; i < more; ++i)
				m_buffer.add((byte)0);
		}
		return this;
	}

	/** Validate index. Returns 0 if the size is 0. */
	private int idx(int i) {
		i = (i >= size() ? size() - 1: i);
		i = (i < 0 ? 0 : i);
		return i;
	}

	/** Get the value of the specified number of bits (low order) at 
	 * the byte at the specified location. The returned value is unsigned.
	 * @param i Index of byte to return.
	 * @param nb Number of bits to return, 1-8.
	 * @return Unsigned value of specified byte anded w/ nb bits. */
	public int getBits(int i, int nb) {
		if(nb < 1)
			return 0;
		if(nb >= 8)
			return getInt(i);
		int m = 1;
		for(int j = 0; j < nb; ++j)
			m *= 2;
		--m;
		int r = getInt(i) & m;
		return r;
	}

	/** Get the byte at the specified position as a signed byte */
	public byte getByte(int i) {
		return m_buffer.get(idx(i));
	}

	/** Get byte at specified position as quasi unsigned byte (integer). */
	public int getInt(int i) {
		return unsigned(getByte(i));
	}

	/** given a signed byte return a quasi unsigned byte as an integer. */
	static public int unsigned(byte b) {
		return 0xff & b;
	}

	/** Extract and return a 2 byte composite value from the message. */
	public int getTwoByteValue(int idx) {
		assert idx + 1 < size() : "idx is " + idx;
		if(idx + 1 >= size())
			return 0;
		int b1 = getInt(idx + 0);
		int b2 = getInt(idx + 1);
		int ret = (b1 << 8) | b2;
		assert b1 * 256 + b2 == ret;
		return ret;
	}

	/** Extract and return an unsigned 4 byte value.
	 * @param idx Index of MSB */
	public long getFourByteValue(int idx) {
		if(idx + 4 >= size())
			return 0;
		int b1 = getInt(idx + 0);
		int b2 = getInt(idx + 1);
		int b3 = getInt(idx + 2);
		int b4 = getInt(idx + 3);
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}

	/** validate a single byte against a single value. */
	public boolean validateByte(int idx, byte evalue) {
		return evalue == getByte(idx);
	}

	/** validate a single byte against multiple values. */
	public boolean validateByte(int idx, byte[] evalue) {
		for(int i=0; i < evalue.length; ++i)
			if(evalue[i] == getByte(idx))
				return true;
		return false;
	}

	/** return a byte[] containing the buffer */
	public byte[] toArray() {
		return toArray(m_buffer);
	}

	/** return an array given an array list */
	static private byte[] toArray(ArrayList<Byte> buffer) {
		if(buffer == null)
			return new byte[0];
		if(buffer.size() <= 0)
			return new byte[0];
		byte[] ret = new byte[buffer.size()];
		for(int i=0; i<buffer.size(); ++i) {
			Byte b = buffer.get(i);
			ret[i] = b.byteValue();
		}
		return ret;
	}

	/** to string */
	public String toString() {
		StringBuilder ret = new StringBuilder(size()*8);
		ret.append("(ByteBlob: size=").append(size()).append(", ");
		for(int i=0; i < size(); ++i) {
			ret.append("[").append(i).append("]=");
			ret.append(getInt(i));
			if(i < size() - 1)
				ret.append(", ");
		}
		ret.append(")");
		return ret.toString();
	}

	/** Calculate a checksum as an integer */
	public int calcIntChecksum() {
		return calcIntChecksum(0, size() - 1);
	}

	/** Calculate a checksum as an integer */
	public int calcIntChecksum(int fi, int ti) {
		int cs = 0;
		for(int i = fi; i <= ti; ++i) {
			cs += getInt(i);
		}
		return cs;
	}

	/** Calculate a checksum as a single byte (low order). */
	public int calcOneByteChecksum() {
		return calcOneByteChecksum(0, size() - 1);
	}

	/** Calculate a checksum as a single byte (low order), which 
	 * is returned numerically as an unsigned byte in int form. */
	public int calcOneByteChecksum(int fi, int ti) {
		return unsigned(calcByteChecksum(fi, ti)[1]);
	}

	/** Calculate a checksum as two bytes.
	 * @return Byte[2] with the high order byte as [0]. */
	public byte[] calcByteChecksum() {
		return calcByteChecksum(0, size() - 1);
	}

	/** Calculate a checksum as two bytes. 
	 * @return Byte[2] with the high order byte as [0]. */
	public byte[] calcByteChecksum(int fi, int ti) {
		return toByteArray(calcIntChecksum(fi, ti), 2);
	}

	/** Convert integer to byte array of specified length. The high 
	 * order byte is returned as [0]. */
	static private byte[] toByteArray(int value, int length) {
		if(length > 4 || length < 1)
			throw new IllegalArgumentException();
		byte[] ba = new byte[length];
		for(int i = 0; i < ba.length; i++)
			ba[i] = (byte) 
				((value >> ((ba.length - 1 - i) * 8)) & 0xFF);
		return ba;
	}

	/** Return a sub array 
	 * @param si Start index, inclusive.
	 * @param ei End index, exclusive. Use negative value for length.
	 * @return Byte array containing byte range specified. */
	public byte[] getByteArray(int si, int ei) {
		si = idx(si);
		ei = (ei < 0 ? size(): ei);
		ei = (ei > size() ? size() : ei);
		final int len = ei - si;
		if(len <= 0)
			return new byte[0];
		byte[] r = new byte[len];
		for(int t = 0, s = si; t < len; ++t, ++s)
			r[t] = getByte(s);
		return r;
	}

	/** Return a new byte blob using the specified range.
	 * @param si Start index, inclusive.
	 * @param ei End index, exclusive. Use negative value for length.
	 * @return Byte array containing byte range specified. */
	public ByteBlob getByteBlob(int si, int ei) {
		return new ByteBlob(getByteArray(si, ei));
	}

	/** 
	 * Search the entire buffer for the specified leader.
	 * @param leader Leader byte array containing leader.
	 * @return The index of the found leader else -1 if not found.
	 */
	public int search(byte[] leader) {
		if(leader == null || leader.length <= 0)
			return -1;
		for(int i = 0; i<size(); ++i)
			if(search(leader, i))
				return i;
		return -1;
	}

	/** 
	 * Determine if the buffer contains the specified leader
	 * starting at the specified position.
	 * @param leader Leader byte array containing leader.
	 * @param idx index to start searching for leader.
	 * @return True if buffer contains leader else false.
	 */
	public boolean search(byte[] leader, int idx) {
		if(leader == null || leader.length <= 0)
			return true;
		for(int i = 0; i<leader.length; ++i) {
			if(idx + i >= size())
				return false;
			if(getByte(idx + i) != leader[i])
				return false;
		}
		return true;
	}

	/** return true if the buffer contains the specified string. */
	public boolean contains(String s) {
		if(s == null)
			s = "";
		return search(s.getBytes()) >= 0;
	}

	/** return true if the buffer starts with the specified string. */
	public boolean startsWith(String s) {
		if(s == null)
			s = "";
		return search(s.getBytes(), 0);
	}

	/** Return true if the buffer starts with the specified string,
	 *  starting at the specified position. */
	public boolean startsWith(String s, int i) {
		if(s == null)
			s = "";
		return search(s.getBytes(), i);
	}

	/** return true if the buffer starts with the specified byte array,
 	 * where the byte array is converted to a string. */
	public boolean startsWith(byte[] ba) {
		return startsWith(byteArrayToString(ba));
	}

	/** Convert byte[] to char[] using specific encoding.
	 * @returns An empty string on error. */
	static private String byteArrayToString(byte[] b) {
		int len=(b==null ? 0 : b.length);
		return byteArrayToString(b,len);
	}

	/** Convert byte[] to char[] using specific encoding.
	 * @returns An empty string on error. */
	static private String byteArrayToString(byte[] b, int len) {
		if(b == null || b.length <= 0 || len <= 0)
			return "";
		if(b.length < len)
			len = b.length;
		String s = "";
		try {
			s = new String(b, 0, len, "ISO-8859-1");
		} catch(Exception ex) {
			s = "";
		}
		return s;
	}

	/** Return true if the buffer starts with the specified string, 
	 *  ignoring leading \n or \r characters in the buffer. */
	public boolean startsWithIgnoreLeadCrLf(String s) {
		if(s == null)
			s = "";
		for(int i = 0; i < size(); ++i) {
			if(startsWith(s, i))
				return true;
			if(getByte(i) == '\n' || getByte(i) == '\r')
				continue;
			else
				return false;

		}
		return false;
	}

	/** return true if the buffer ends with the specified string. */
	public boolean endsWith(String s) {
		if(s == null)
			s = "";
		int len = s.length();
		return search(s.getBytes(), size() - len);
	}

	/** Return the bytes encoded as characters. */
	public String toStringChars() {
		return new String(toArray());
	}
}
