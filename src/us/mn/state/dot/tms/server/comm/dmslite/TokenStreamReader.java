/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dmslite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.GregorianCalendar;
import us.mn.state.dot.tms.utils.Log;

/**
 * A specialized stream reader for reading tokens.
 *
 * @author Michael Darter
 * @company AHMCT, UCD
 */
public class TokenStreamReader
{
	// fields
	final private BufferedReader m_inps;
	private ParseBuffer m_pb;
	final private int m_sleeptime;

	/**
	 * Constructor.
	 *
	 * @param inps input stream to wrap.
	 */
	public TokenStreamReader(InputStream inps, int initsize,
				 int maxcapacity, int sleeptime) {
		if((inps == null) || (initsize <= 0) || (maxcapacity <= 0) || (sleeptime <= 0))
			throw new IllegalArgumentException("Illegal argument in TokenStreamReader constructor.");

		m_inps = new BufferedReader(new InputStreamReader(inps));
		m_pb = createParseBuffer(initsize, maxcapacity);
		m_sleeptime = sleeptime;
	}

	/** Close the stream */
	public void close() throws IOException {
		m_inps.close();
	}

	/** return the buffer */
	public String getBuffer() {
		return new String(m_pb.toString());
	}

	/** reset buffer */
	public void resetBuffer() {
		m_pb.init();
	}

	/** create a new parse buffer */
	private static ParseBuffer createParseBuffer(int size,int maxsize) {
		return new ParseBuffer(size,maxsize);
	}

	/**
	 * Read a token with the specified starting and ending. Any characters read
	 * before the token are discarded. This method returns when a token is read.
	 *
	 * @param timeout Maximum time to wait for a response in MS. Zero means wait forever.
	 * @param tokenstart Start of token.
	 * @param tokenend End of token.
	 * @return Null if timed out, only if the specified arg timeout>0.
	 * @throws IllegalStateException if maximum capacity is exceeded.
	 * @throws IOException if endpoint disconnects.
	 */
	public String readToken(int timeout, String tokenstart, String tokenend)
		throws IllegalStateException, IOException {

		// check args
		if((timeout < 0) || (tokenstart == null)
			|| (tokenend == null)) {
			throw new IllegalArgumentException(
			    "Invalid argument in readToken()");
		}

		int numread = 0;
		char[] fragment = new char[512];
		String token = null;

		// timeout?
		long start = 0;
		if(timeout > 0) {
			start = TokenStreamReader.getCurTimeUTCinMillis();
		}

		// read bytes until token discovered
		while(true) {

			// is a complete token already in buffer? if yes, 
			// extract it and delete preceeding text.
			token = m_pb.getToken(ParseBuffer.ExtractType.DDK,
				tokenstart, tokenend);
			if(token != null)
				return token;

			// read
			//Log.finest("TokenStreamReader:Waiting for bytes to read, buf="+m_pb.toString());
			try {
				numread = m_inps.read(fragment, 0,fragment.length);
			} catch (SocketTimeoutException ex) {

				// Log.finest("TokenStreamReader:Ignored SocketTimeoutException:"+ex);
				// check if timed out
				if(this.timedOut(start, timeout)) {
					return (null);
				}

				try {
					Thread.sleep(m_sleeptime);
				} catch (InterruptedException ex2) {}

				continue;
			} catch (IOException ex) {
				//Log.finest("TokenStreamReader:Client disconnected.");
				throw ex;
			}

			//Log.finest("TokenStreamReader:Read " + numread + " bytes from client.");

			// bytes received
			if(numread > 0) {

				// add to existing parse buffer, throws IllegalStateException if cap exceeded
				// Log.finest("TokenStreamReader:parsebuffer: length="+m_pb.length()+", cap="+m_pb.capacity()+", maxcap="+m_pb.maxSize()+".");
				// Log.finest("TokenStreamReader:Adding "+numread+" chars to existing ParseBuffer.");
				m_pb.append(numread, fragment);

				// Log.finest("TokenStreamReader:parsebuffer: length="+m_pb.length()+", cap="+m_pb.capacity()+", maxcap="+m_pb.maxSize()+".");

				// is a complete token in buffer? if yes, extract it,
				// and delete text in buffer preceeding token, if any.
				token = m_pb.getToken(ParseBuffer.ExtractType.DDK,
					tokenstart, tokenend);

				// Log.finest("TokenStreamReader:Extracted token, length now "+m_pb.length()+".");

				// read again with no sleep
				if(token == null) {
					continue;

				// found token
				} else {

					// Log.finest("TokenStreamReader:Found complete token:"+token);
					return (token);
				}

			// disconnect
			} else if(numread < 0) {

				// Log.finest("TokenStreamReader:Client disconnected (numread<0).");
				throw new IOException("client disconnected");
			}

			// check if timed out
			if(this.timedOut(start, timeout)) {
				return (null);
			}

			// sleep and read again
			try {
				Thread.sleep(m_sleeptime);
			} catch (InterruptedException ex) {}
		}

		// unreachable
	}

	/**
	 *      Return true if a timeout occurred else false.
	 */
	private boolean timedOut(long start, int timeout) {

		// timeout used?
		if(timeout <= 0) {
			return (false);
		}

		if(TokenStreamReader.calcTimeDeltaMS(start) + m_sleeptime > timeout) {
			// Log.finest("TokenStreamReader:Timed out, waited "+timeout/1000+" secs.");
			return (true);
		}

		return (false);
	}

	/**
	 *  calc time difference between now (UTC since 1970)
	 *  and given start time in MS.
	 */
	private static long calcTimeDeltaMS(long startInUTC) {
		java.util.Date d = new GregorianCalendar().getTime();
		long t = d.getTime() - startInUTC;
		return t;
	}

	/**
	 *  get current time in MS (UTC) since Jan 1st 1970 00:00:00.
	 */
	private static long getCurTimeUTCinMillis() {
		java.util.Date d = new GregorianCalendar().getTime();
		return (d.getTime());
	}

	/**
	 *  test methods.
	 */
	static public boolean test() {
		boolean ok = true;
		System.err.println("TokenStreamReader:Test done, return=" + ok);
		return (ok);
	}
}
