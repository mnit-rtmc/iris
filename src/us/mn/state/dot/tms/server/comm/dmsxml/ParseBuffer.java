/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.dmsxml;

/**
 * A buffer for parsing. A caller typically adds to the end,
 * and extracts tokens from the beginning. The buffer grows
 * automatically to the maximum specified size. An assertion
 * is thrown if the buffer grows beyond the maximum size.
 *
 * @author Michael Darter
 */
final class ParseBuffer
{
	// fields
	private char[] m_buffer = new char[0];	// buffer
	private final int m_allocsize;		// allocated buffer size
	private int m_emptySpot;		// index of first empty spot in buffer
	private final int m_maxsize;		// maximum size allowed

	// types
	enum ExtractType {
		KDK,    // keep left most chunk, delete middle, keep right most chunk
		DDK,    // delete left most chunk, delete middle, keep right most chunk
		KKK,    // keep left, middle, right
	}

	/**
	 * Constructor.
	 *
	 * @param allocsize Size of the allocated buffer in bytes.
	 * @param maxsize The maximum allowed size the buffer grows to.
	 */
	ParseBuffer(int allocsize, int maxsize) {
		if((allocsize < 0) || (allocsize > maxsize) || (maxsize < 0)) {
			throw new IllegalArgumentException();
		}

		m_allocsize = allocsize;
		m_buffer = new char[allocsize];
		m_maxsize = maxsize;
		m_emptySpot = 0;

		// LOG.log("m_buffer="+this.toString()+",capacity="+this.capacity()+", length="+this.length()+",maxsize="+this.maxSize());
	}

	/** initialize the buffer */
	void init() {
		m_buffer = new char[m_allocsize];
		m_emptySpot = 0;
	}

	/**
	 *  Append the specified array to the end of the buffer.
	 *
	 *  @params a Byte array to append to the buffer.
	 */
	void append(byte[] ba) throws IllegalArgumentException {
		if(ba == null) {
			throw new IllegalArgumentException("arg is invalid");
		}

		int numbytes = ba.length;

		this.append(numbytes, ba);
	}

	/**
	 *  Append the specified array to the end of the buffer.
	 *
	 *  @params numbytes Number of bytes to append from array.
	 *  @params a Byte array to append to the buffer.
	 */
	void append(int numbytes, byte[] ba)
		throws IllegalArgumentException {
		if((ba == null) || (numbytes < 0)) {
			throw new IllegalArgumentException("arg is invalid");
		}

		// LOG.log("Will append "+numbytes+" bytes.");
		char[] ca = byteArrayToCharArray(numbytes, ba);
		if(ca != null)
			append(numbytes, ca);
	}

	/** Convert byte[] to char[] using assumed encoding.
	 *  @returns May return null. */
	static char[] byteArrayToCharArray(int len, byte[] ba) {
		char[] ca;
		try {
			ca = new String(ba, 0, len, "ISO-8859-1").
				toCharArray();
		} catch (Exception UnsupportedEncodingException) {
			ca = null;
		}
		return ca;
	}

	/**
	 *  Append the specified char[] to the end of the buffer.
	 *
	 *  @params a char array to append to the buffer.
	 */
	void append(char[] a) throws IllegalStateException {
		if(a == null)
			throw new IllegalArgumentException("arg is invalid");

		int numbytes = a.length;

		this.append(numbytes, a);
	}

	/**
	 *  Append the specified char[] to the end of the buffer.
	 *
	 *  @params numchars Number of chars to append from array.
	 *  @params a char array to append to the buffer.
	 */
	void append(int numchars, char[] a)
		throws IllegalStateException {
		if((a == null) || (numchars < 0) || (numchars > a.length)) {
			throw new IllegalArgumentException("arg is invalid");
		}

		// LOG.log("Will append "+numchars+" chars.");
		// expand?
		int newlen = this.length() + numchars;

		// LOG.log("newlen="+newlen);
		if(newlen > this.capacity()) {
			this.expand(newlen);
		}

		// append
		System.arraycopy(a, 0, m_buffer, this.length(), numchars);
		m_emptySpot += numchars;

		// LOG.log("m_buffer="+this.toString()+",capacity="+this.capacity()+", length="+this.length());
		assert(m_emptySpot >= 0) && (m_emptySpot <= this.capacity());
	}

	/**
	 *  test methods. //FIXME: move to junit
	 */
	static boolean test() {
		boolean ok = true;

		{

			// create
			//LOG.info("T1");

			ParseBuffer b = new ParseBuffer(5, 10);

			ok = ok && (b.length() == 0) && (b.capacity() == 5);

			if(!ok) {
				return (ok);
			}

			// append
			//LOG.info("T2");
			b.append(new char[] { 'a', 'b', 'c' });
			ok = ok && (b.length() == 3) && (b.capacity() == 5);
			ok = ok && (b.toString().compareTo("abc") == 0);

			if(!ok) {
				return (ok);
			}

			// 2nd append
			//LOG.info("T3");
			b.append(new char[] { 'd', 'e', 'f' });
			ok = ok && (b.length() == 6) && (b.capacity() == 6);
			ok = ok && (b.toString().compareTo("abcdef") == 0);

			if(!ok) {
				return (ok);
			}

			// exceed capacity
			//LOG.info("T4");

			boolean ok2 = false;

			try {
				b.append(new char[] { 'g', 'h', 'i', 'j',
						      'k' });
			} catch (IllegalStateException ex) {
				ok2 = true;
			}

			ok = ok && ok2;
			ok = ok && (b.length() == 6) && (b.capacity() == 6);
			ok = ok && (b.toString().compareTo("abcdef") == 0);

			if(!ok) {
				return (ok);
			}

			// append nothing
			//LOG.info("T5");
			b.append(new char[0]);
			ok = ok && (b.length() == 6) && (b.capacity() == 6);
			ok = ok && (b.toString().compareTo("abcdef") == 0);

			if(!ok) {
				return (ok);
			}
		}

		// get token
		{
			//LOG.info("T6");

			ParseBuffer zb = new ParseBuffer(5, 10);

			zb.append(new char[] {
				'a', 'b', 'c', 'd', 'e', 'f'
			});

			String t1 = zb.getToken(ExtractType.KKK, "a", "c");

			ok = ok && (zb.length() == 6) && (zb.capacity() == 6);
			ok = ok && (zb.toString().compareTo("abcdef") == 0);
			ok = ok && (t1.compareTo("abc") == 0);

			if(!ok) {
				return (ok);
			}
		}

		// extract token
		{
			//LOG.info("T7");

			ParseBuffer b = new ParseBuffer(5, 10);

			b.append(new char[] {
				'a', 'b', 'c', 'd', 'e', 'f'
			});

			String t2 = b.getToken(ExtractType.KDK, "a", "c");

			ok = ok && (b.length() == 3) && (b.capacity() == 6);
			ok = ok && (b.toString().compareTo("def") == 0);
			ok = ok && (t2.compareTo("abc") == 0);

			if(!ok) {
				return (ok);
			}
		}

		// extract token
		{
			//LOG.info("T8");

			ParseBuffer x1 = new ParseBuffer(2, 10);

			ok = ok && (x1.length() == 0) && (x1.capacity() == 2)
			     && (x1.maxSize() == 10);
			x1.append(new char[] {
				'a', 'b', 'c', 'd', 'e', 'f', 'g'
			});
			ok = ok && (x1.length() == 7) && (x1.capacity() == 7)
			     && (x1.maxSize() == 10);
			ok = ok && (x1.toString().compareTo("abcdefg") == 0);

			String s = x1.getToken(ExtractType.KDK, "bc", "de");

			ok = ok && (x1.length() == 3) && (x1.capacity() == 7)
			     && (x1.maxSize() == 10);
			ok = ok && (x1.toString().compareTo("afg") == 0);
			ok = ok && (s.compareTo("bcde") == 0);

			if(!ok) {
				return (ok);
			}
		}

		// extract token-overlap
		{
			//LOG.info("T9");

			ParseBuffer x1 = new ParseBuffer(2, 10);

			ok = ok && (x1.length() == 0) && (x1.capacity() == 2)
			     && (x1.maxSize() == 10);
			x1.append(new char[] {
				'a', 'b', 'c', 'd', 'e', 'f', 'g'
			});
			ok = ok && (x1.length() == 7) && (x1.capacity() == 7)
			     && (x1.maxSize() == 10);
			ok = ok && (x1.toString().compareTo("abcdefg") == 0);

			boolean bb = false;

			try {
				x1.getToken(ExtractType.KDK, "bc", "cd");
			} catch (IllegalArgumentException ex) {
				bb = true;
			}

			ok = ok && bb;

			if(!ok) {
				return (ok);
			}
		}

		// extract token-all
		{
			//LOG.info("T10");

			ParseBuffer x1 = new ParseBuffer(2, 10);

			ok = ok && (x1.length() == 0) && (x1.capacity() == 2)
			     && (x1.maxSize() == 10);
			x1.append(new char[] {
				'a', 'b', 'c', 'd', 'e', 'f', 'g'
			});
			ok = ok && (x1.length() == 7) && (x1.capacity() == 7)
			     && (x1.maxSize() == 10);
			ok = ok && (x1.toString().compareTo("abcdefg") == 0);

			String s = x1.getToken(ExtractType.KDK, "abcdef", "g");

			ok = ok && (x1.length() == 0) && (x1.capacity() == 7)
			     && (x1.maxSize() == 10);
			ok = ok && (x1.toString().compareTo("") == 0);
			ok = ok && (s.compareTo("abcdefg") == 0);

			if(!ok) {
				return (ok);
			}
		}

		// remove chunk
		{
			//LOG.info("T11");

			ParseBuffer x1;

			// KKK
			x1 = new ParseBuffer(2, 10);
			x1.append(new char[] {
				'a', 'b', 'c', 'd', 'e', 'f', 'g'
			});
			ok = ok && (x1.getToken(ExtractType.KKK, "cd",
						"ef").compareTo("cdef") == 0);
			ok = ok && (x1.toString().compareTo("abcdefg") == 0);

			// KDK
			x1 = new ParseBuffer(2, 10);
			x1.append(new char[] {
				'a', 'b', 'c', 'd', 'e', 'f', 'g'
			});
			ok = ok && (x1.getToken(ExtractType.KDK, "cd",
						"ef").compareTo("cdef") == 0);
			ok = ok && (x1.toString().compareTo("abg") == 0);

			// DDK
			x1 = new ParseBuffer(2, 10);
			x1.append(new char[] {
				'a', 'b', 'c', 'd', 'e', 'f', 'g'
			});
			ok = ok && (x1.getToken(ExtractType.DDK, "cd",
						"ef").compareTo("cdef") == 0);
			ok = ok && (x1.toString().compareTo("g") == 0);

			if(!ok) {
				return (ok);
			}
		}

		// append with specified length
		{
			//LOG.info("T12");

			ParseBuffer b = new ParseBuffer(10, 10);

			b.append(3, new byte[] {
				32, 32, 32, 32, 32, 32
			});
			ok = ok && (b.length() == 3);
			ok = ok && (b.capacity() == 10);
			ok = ok && (b.toString().compareTo("   ") == 0);

			if(!ok) {
				return (ok);
			}
		}

		// test init
		{
			//LOG.info("T13");

			ParseBuffer b = new ParseBuffer(10, 10);

			b.append(3, new byte[] {
				32, 32, 32, 32, 32, 32
			});
			b.init();
			b.append(3, new byte[] {
				32, 32, 32, 32, 32, 32
			});
			ok = ok && (b.length() == 3);
			ok = ok && (b.capacity() == 10);
			ok = ok && (b.toString().compareTo("   ") == 0);
			if(!ok) {
				return (ok);
			}
		}

		//LOG.info("ParseBuffer.test() done, return=" + ok);

		return (ok);
	}

	/**
	 * search buffer for a string. The index is returned,
	 * which is negative if not found.
	 *
	 *  @params s String to search for in the buffer.
	 *  @params fromIndex Index to start searching from
	 */
	int search(String s, int fromIndex) {
		if((s == null) || (s.length() <= 0) || (fromIndex < 0)) {
			throw new IllegalArgumentException();
		}

		String b = this.toString();

		return (b.indexOf(s, fromIndex));
	}

	/**
	 * Extract a portion of the buffer that starts and ends
	 * with the specified strings. null is returned if not found.
	 * The token is deleted from the buffer if remove is true.
	 *
	 *  @params et Enumerated type that indicates how buffer should be modified after token found.
	 *  @params start Start of token to extract.
	 *  @params end End of token to extract.
	 */
	String getToken(ParseBuffer.ExtractType et, String start,
			       String end) {
		if((start == null) || (end == null)) {
			throw new IllegalArgumentException();
		}

		if((start.length() <= 0) || (end.length() <= 0)) {
			throw new IllegalArgumentException();
		}

		String b = this.toString();

		// does the start exist?
		int i1 = b.indexOf(start, 0);

		// LOG.log("i1="+i1);
		if(i1 < 0) {
			return (null);
		}

		// does the end exist?
		int i2 = b.indexOf(end, i1);

		// LOG.log("i2="+i2);
		if(i2 < 0) {
			return (null);
		}

		// overlap of start and end token?
		if(i1 + start.length() > i2) {
			throw new IllegalArgumentException(
			    "getToken: start and end tokens overlap.");
		}

		// get substring
		String ret = b.substring(i1, i2 + end.length());

		// LOG.log("ret="+ret+", len="+ret.length());

		// remove chunk
		this.removeChunk(et, i1, i2 + end.length());

		return (ret);
	}

	/**
	 * Modify the buffer by deleting or keeping around or in the specified range.
	 *
	 * @params et Enumerated type that indicates how buffer should be modified.
	 * @params start Start of location (inclusive).
	 * @params end End of location (exclusive).
	 */
	void removeChunk(ParseBuffer.ExtractType et, int start,	int end) {

		// LOG.log("before remove: m_buffer="+this.toString()+", len="+this.length()+",cap="+this.capacity());
		if(et == ExtractType.KKK) {
			return;
		}

		int chunklen = end - start;
		int initcap = this.capacity();

		// sanity check
		if((start < 0) || (start >= this.length()) || (chunklen < 0)
			|| (end > this.length())) {
			throw new IllegalArgumentException();
		}

		// remove or keep left, middle, right
		char[] nb = new char[this.capacity()];
		int nb_len = 0;

		for(int i = 0, j = 0; i < this.length(); ++i) {
			boolean keep = false;

			// left
			if((i < start)
				&& ((et == ExtractType.KDK)
				    || (et == ExtractType.KKK))) {
				keep = true;

				// middle
			} else if((i >= start) && (i < end)
				&& (et == ExtractType.KKK)) {
				keep = true;

				// end
			} else if((i >= end)
				&& ((et == ExtractType.KDK)
				    || (et == ExtractType.DDK)
				    || (et == ExtractType.KKK))) {
				keep = true;
			}

			if(keep) {

				// LOG.log("ParseBuffer.removeRange(): keep: i="+i+", j="+j);
				nb[j] = m_buffer[i];
				++j;
				++nb_len;
			}
		}

		m_buffer = nb;
		this.setLength(nb_len);
		assert initcap == this.capacity();

		// LOG.log("after remove: m_buffer="+this.toString()+", len="+this.length()+",cap="+this.capacity());
	}

	/**
	 * specify the length of the buffer.
	 */
	private void setLength(int len) {
		m_emptySpot = len;
	}

	/** to string */
	public String toString() {

		// LOG.log("len="+this.length());
		// LOG.log("cap="+this.capacity());
		// LOG.log("b="+m_buffer.toString());
		StringBuilder s = new StringBuilder(this.length());

		for(int i = 0; i < this.length(); ++i) {
			s.append(m_buffer[i]);
		}

		return (s.toString());
	}

	/** return the length of buffer */
	int length() {
		assert(m_emptySpot >= 0) && (m_emptySpot <= m_buffer.length);
		return (m_emptySpot);
	}

	/** return the capacity of the buffer */
	int capacity() {
		return (m_buffer.length);
	}

	/** return the maximum size of the buffer */
	int maxSize() {
		return (m_maxsize);
	}

	/**
	 * return buffer as an array.
	 */
	char[] toArray() {
		char[] b = new char[this.length()];

		System.arraycopy(m_buffer, 0, b, 0, m_emptySpot);

		return (b);
	}

	/**
	 * Expand the array size. The existing array is copied
	 * into the new one.
	 *
	 * @params newcap New capacity of buffer.
	 */
	private void expand(int argnewcap) throws IllegalStateException {

		// calculate new size
		int newcap = argnewcap;

		if(newcap <= 0) {
			throw new IllegalArgumentException();
		}

		if(newcap < this.capacity()) {
			newcap = 2 * this.capacity();
		}

		if(newcap > this.m_maxsize) {
			throw new IllegalStateException(
			    "Max size exeeded in ParseBuffer.expand()");
		}

		// LOG.log("newcap="+newcap);
		char[] na = new char[newcap];

		System.arraycopy(m_buffer, 0, na, 0, this.length());

		// LOG.log("m_buffer1="+this.toString()+",length="+m_buffer.length);
		m_buffer = na;

		// LOG.log("m_buffer2="+this.toString()+",length="+m_buffer.length);
		assert(this.length() >= 0)
		      && (this.length() <= this.capacity());

		// LOG.log("new length="+this.length());
	}
}

