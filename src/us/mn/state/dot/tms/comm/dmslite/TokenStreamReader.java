/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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



package us.mn.state.dot.tms.comm.dmslite;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.SocketTimeoutException;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A specialized stream reader for reading tokens.
 *
 * @author Michael Darter
 * @company AHMCT, UCD
 */
public class TokenStreamReader {

    // fields
    private BufferedReader m_inps;
    private ParseBuffer    m_pb;
    private int            m_sleeptime;

    /**
     * Constructor.
     *
     * @param inps input stream to wrap.
     * @param maxsize The maximum allowed size the buffer grows to.
     */
    public TokenStreamReader(InputStream inps, int initsize, int maxcapacity, int sleeptime) {
        if ((inps == null) || (initsize <= 0) || (maxcapacity <= 0) || (sleeptime <= 0)) {
            throw new IllegalArgumentException("Illegal argument in TokenStreamReader constructor.");
        }

        m_inps      = new BufferedReader(new InputStreamReader(inps));
        m_pb        = new ParseBuffer(initsize, maxcapacity);
        m_sleeptime = sleeptime;
    }

    /** Close the stream */
    public void close() throws IOException {
        m_inps.close();    // FIXME: also close InputStreamReader()?
    }

    /**
     * Read a token with the specified starting and ending. Any characters read
     * before the token are discarded. This method returns when a token is read.
     *
     * @param timeout Maximum time to wait for a response in MS. Zero means wait forever.
     * @param tokenstart Start of token.
     * @param tokenend End of token.
     *
     * @return Null if timed out.
     *
     * @throws IllegalStateException if maximum capacity is exceeded.
     * @throws IOException if endpoint disconnects.
     */
    public String readToken(int timeout, String tokenstart, String tokenend) throws IllegalStateException, IOException {

        // check args
        if ((timeout < 0) || (tokenstart == null) || (tokenend == null)) {
            throw new IllegalArgumentException("Invalid argument in readToken()");
        }

        int    numread  = 0;
        char[] fragment = new char[512];
        String token    = null;

        // timeout?
        long start = 0;

        if (timeout > 0) {
            start = TokenStreamReader.getCurTimeUTCinMillis();
        }

        // read bytes until token discovered
        while (true) {

            // read
            System.err.println("TokenStreamReader.readToken(): waiting for bytes to read.");

            try {
                numread = m_inps.read(fragment, 0, fragment.length);
            } catch (SocketTimeoutException ex) {

                // Log.finest("TokenStreamReader.readToken(): ignored SocketTimeoutException:"+ex);
                // check if timed out
                if (this.timedOut(start, timeout)) {
                    return (null);
                }

                try {
                    Thread.sleep(m_sleeptime);
                } catch (InterruptedException ex2) {}

                continue;
            } catch (IOException ex) {
                System.err.println("TokenStreamReader.readToken(): client disconnected.");

                throw new IOException("client disconnected");
            }

            System.err.println("TokenStreamReader.readToken(): read bytes from iris:" + numread);

            // bytes received
            if (numread > 0) {

                // add to existing parse buffer, throws except if cap exceeded
                m_pb.append(fragment);

                // is a complete token in buffer? if yes, extract it,
                // and delete text in buffer preceeding token, if any.
                token = m_pb.getToken(ParseBuffer.ExtractType.DDK, tokenstart, tokenend);

                // read again with no sleep
                if (token == null) {
                    continue;

                    // found token
                } else {
                    System.err.println("TokenStreamReader.readToken(): found complete token:" + token);

                    return (token);
                }

                // disconnect
            } else if (numread < 0) {
                System.err.println("TokenStreamReader.readToken(): client disconnected (numread<0).");

                throw new IOException("client disconnected");
            }

            // check if timed out
            if (this.timedOut(start, timeout)) {
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
     *      Return true if a timeout occured else false.
     */
    private boolean timedOut(long start, int timeout) {

        // timeout used?
        if (timeout <= 0) {
            return (false);
        }

        if (TokenStreamReader.calcTimeDeltaMS(start) + m_sleeptime > timeout) {
            System.err.println("TokenStreamReader.timeout(): timed out, waited " + timeout / 1000 + " secs.");

            return (true);
        }

        return (false);
    }

    /**
     *      calc time difference between now (UTC since 1970)
     *  and given start time in MS.
     */
    private static long calcTimeDeltaMS(long startInUTC) {
        java.util.Date d = new GregorianCalendar().getTime();
        long           t = d.getTime() - startInUTC;

        return (t);
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

        System.err.println("TokenStreamReader.test() done, return=" + ok);

        return (ok);
    }

    /** init buffer */
    public void initBuffer() {
        m_pb.init();
    }

}
