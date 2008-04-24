//
// /***************************
// This module was developed by the Advanced Highway Maintenance &
// Construction Technology (AHMCT) Research Center at the University of
// California - Davis (UCD), in partnership with the California Department
// of Transportation (Caltrans) by Michael Darter, 03/13/08 and
// is provided as open-source software.
// ***************************/
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 



package us.mn.state.dot.tms.comm.dmslite;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

/**
 * A buffer for parsing. A caller typically adds to the end,
 * and extracts tokens from the beginning. The buffer grows
 * automatically to the maximum specified size. An assertion
 * is thrown if the buffer grows beyond the maximum size.
 *
 * @author Michael Darter
 * @company AHMCT, UCD
 */
public class ParseBuffer {

    // fields
    private char[]    m_buffer = new char[0];    // buffer
    private int       m_emptySpot;               // index of first empty spot in buffer
    private final int m_maxsize;                 // maximum size allowed

    // types
    public enum ExtractType {
        KDK,                                     // keep left most chunk, delete middle, keep right most chunk
        DDK,                                     // delete left most chunk, delete middle, keep right most chunk
        KKK,                                     // keep left, middle, right
    }

    /**
     * Constructor.
     *
     * @param allocsize Size of the allocated buffer in bytes.
     * @param maxsize The maximum allowed size the buffer grows to.
     */
    public ParseBuffer(int allocsize, int maxsize) {
        if ((allocsize < 0) || (allocsize > maxsize) || (maxsize < 0)) {
            throw new IllegalArgumentException();
        }

        m_buffer    = new char[allocsize];
        m_maxsize   = maxsize;
        m_emptySpot = 0;

        // Log.info("ParseBuffer.ParseBuffer(): m_buffer="+this.toString()+",capacity="+this.capacity()+", length="+this.length()+",maxsize="+this.maxSize());
    }

    /**
     *  Append the specified char[] to the end of the buffer.
     *
     *  @params a char array to append to the buffer.
     */
    public void append(char[] a) throws IllegalStateException {
        if (a == null) {
            return;
        }

        // expand?
        int newlen = this.length() + a.length;

        // Log.info("ParseBuffer.append(): newlen="+newlen);
        if (newlen > this.capacity()) {
            this.expand(newlen);
        }

        // append
        System.arraycopy(a, 0, m_buffer, this.length(), a.length);
        m_emptySpot += a.length;

        // Log.info("ParseBuffer.append(): m_buffer="+this.toString()+",capacity="+this.capacity()+", length="+this.length());
        assert (m_emptySpot >= 0) && (m_emptySpot <= this.capacity());
    }

    /**
     *  test methods.
     */
    static public boolean test() {
        boolean ok = true;

        {

            // create
            System.out.println("T1");

            ParseBuffer b = new ParseBuffer(5, 10);

            ok = ok && (b.length() == 0) && (b.capacity() == 5);

            if (!ok) {
                return (ok);
            }

            // append
            System.out.println("T2");
            b.append(new char[] { 'a', 'b', 'c' });
            ok = ok && (b.length() == 3) && (b.capacity() == 5);
            ok = ok && (b.toString().compareTo("abc") == 0);

            if (!ok) {
                return (ok);
            }

            // 2nd append
            System.out.println("T3");
            b.append(new char[] { 'd', 'e', 'f' });
            ok = ok && (b.length() == 6) && (b.capacity() == 6);
            ok = ok && (b.toString().compareTo("abcdef") == 0);

            if (!ok) {
                return (ok);
            }

            // exceed capacity
            System.out.println("T4");

            boolean ok2 = false;

            try {
                b.append(new char[] { 'g', 'h', 'i', 'j', 'k' });
            } catch (IllegalStateException ex) {
                ok2 = true;
            }

            ok = ok && ok2;
            ok = ok && (b.length() == 6) && (b.capacity() == 6);
            ok = ok && (b.toString().compareTo("abcdef") == 0);

            if (!ok) {
                return (ok);
            }

            // append nothing
            System.out.println("T5");
            b.append(new char[0]);
            ok = ok && (b.length() == 6) && (b.capacity() == 6);
            ok = ok && (b.toString().compareTo("abcdef") == 0);

            if (!ok) {
                return (ok);
            }
        }

        // get token
        {
            System.out.println("T6");

            ParseBuffer zb = new ParseBuffer(5, 10);

            zb.append(new char[] {
                'a', 'b', 'c', 'd', 'e', 'f'
            });

            String t1 = zb.getToken(ExtractType.KKK, "a", "c");

            ok = ok && (zb.length() == 6) && (zb.capacity() == 6);
            ok = ok && (zb.toString().compareTo("abcdef") == 0);
            ok = ok && (t1.compareTo("abc") == 0);

            if (!ok) {
                return (ok);
            }
        }

        // extract token
        {
            System.out.println("T7");

            ParseBuffer b = new ParseBuffer(5, 10);

            b.append(new char[] {
                'a', 'b', 'c', 'd', 'e', 'f'
            });

            String t2 = b.getToken(ExtractType.KDK, "a", "c");

            ok = ok && (b.length() == 3) && (b.capacity() == 6);
            ok = ok && (b.toString().compareTo("def") == 0);
            ok = ok && (t2.compareTo("abc") == 0);

            if (!ok) {
                return (ok);
            }
        }

        // extract token
        {
            System.out.println("T8");

            ParseBuffer x1 = new ParseBuffer(2, 10);

            ok = ok && (x1.length() == 0) && (x1.capacity() == 2) && (x1.maxSize() == 10);
            x1.append(new char[] {
                'a', 'b', 'c', 'd', 'e', 'f', 'g'
            });
            ok = ok && (x1.length() == 7) && (x1.capacity() == 7) && (x1.maxSize() == 10);
            ok = ok && (x1.toString().compareTo("abcdefg") == 0);

            String s = x1.getToken(ExtractType.KDK, "bc", "de");

            ok = ok && (x1.length() == 3) && (x1.capacity() == 7) && (x1.maxSize() == 10);
            ok = ok && (x1.toString().compareTo("afg") == 0);
            ok = ok && (s.compareTo("bcde") == 0);

            if (!ok) {
                return (ok);
            }
        }

        // extract token-overlap
        {
            System.out.println("T9");

            ParseBuffer x1 = new ParseBuffer(2, 10);

            ok = ok && (x1.length() == 0) && (x1.capacity() == 2) && (x1.maxSize() == 10);
            x1.append(new char[] {
                'a', 'b', 'c', 'd', 'e', 'f', 'g'
            });
            ok = ok && (x1.length() == 7) && (x1.capacity() == 7) && (x1.maxSize() == 10);
            ok = ok && (x1.toString().compareTo("abcdefg") == 0);

            boolean bb = false;

            try {
                String s = x1.getToken(ExtractType.KDK, "bc", "cd");
            } catch (IllegalArgumentException ex) {
                bb = true;
            }

            ok = ok && bb;

            if (!ok) {
                return (ok);
            }
        }

        // extract token-all
        {
            System.out.println("T10");

            ParseBuffer x1 = new ParseBuffer(2, 10);

            ok = ok && (x1.length() == 0) && (x1.capacity() == 2) && (x1.maxSize() == 10);
            x1.append(new char[] {
                'a', 'b', 'c', 'd', 'e', 'f', 'g'
            });
            ok = ok && (x1.length() == 7) && (x1.capacity() == 7) && (x1.maxSize() == 10);
            ok = ok && (x1.toString().compareTo("abcdefg") == 0);

            String s = x1.getToken(ExtractType.KDK, "abcdef", "g");

            ok = ok && (x1.length() == 0) && (x1.capacity() == 7) && (x1.maxSize() == 10);
            ok = ok && (x1.toString().compareTo("") == 0);
            ok = ok && (s.compareTo("abcdefg") == 0);

            if (!ok) {
                return (ok);
            }
        }

        // remove chunk
        {
            System.out.println("T11");

            ParseBuffer x1;

            // KKK
            x1 = new ParseBuffer(2, 10);
            x1.append(new char[] {
                'a', 'b', 'c', 'd', 'e', 'f', 'g'
            });
            ok = ok && (x1.getToken(ExtractType.KKK, "cd", "ef").compareTo("cdef") == 0);
            ok = ok && (x1.toString().compareTo("abcdefg") == 0);

            // KDK
            x1 = new ParseBuffer(2, 10);
            x1.append(new char[] {
                'a', 'b', 'c', 'd', 'e', 'f', 'g'
            });
            ok = ok && (x1.getToken(ExtractType.KDK, "cd", "ef").compareTo("cdef") == 0);
            ok = ok && (x1.toString().compareTo("abg") == 0);

            // DDK
            x1 = new ParseBuffer(2, 10);
            x1.append(new char[] {
                'a', 'b', 'c', 'd', 'e', 'f', 'g'
            });
            ok = ok && (x1.getToken(ExtractType.DDK, "cd", "ef").compareTo("cdef") == 0);
            ok = ok && (x1.toString().compareTo("g") == 0);

            if (!ok) {
                return (ok);
            }
        }

        // Log.info("ParseBuffer.test() done, return="+ok);
        return (ok);
    }

    /**
     * search buffer for a string. The index is returned,
     * which is negative if not found.
     *
     *  @params s String to search for in the buffer.
     *  @params fromIndex Index to start searching from
     */
    public int search(String s, int fromIndex) {
        if ((s == null) || (s.length() <= 0) || (fromIndex < 0)) {
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
    public String getToken(ParseBuffer.ExtractType et, String start, String end) {
        if ((start == null) || (end == null)) {
            throw new IllegalArgumentException();
        }

        if ((start.length() <= 0) || (end.length() <= 0)) {
            throw new IllegalArgumentException();
        }

        String b = this.toString();

        // does the start exist?
        int i1 = b.indexOf(start, 0);

        // Log.info("ParseBuffer.getToken(): i1="+i1);
        if (i1 < 0) {
            return (null);
        }

        // does the end exist?
        int i2 = b.indexOf(end, i1);

        // Log.info("ParseBuffer.getToken(): i2="+i2);
        if (i2 < 0) {
            return (null);
        }

        // overlap of start and end token?
        if (i1 + start.length() > i2) {
            throw new IllegalArgumentException("getToken: start and end tokens overlap.");
        }

        // get substring
        String ret = b.substring(i1, i2 + end.length());

        // Log.info("ParseBuffer.getToken(): ret="+ret+", len="+ret.length());

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
    public void removeChunk(ParseBuffer.ExtractType et, int start, int end) {

        // Log.info("ParseBuffer.removeRange()1: m_buffer="+this.toString()+", len="+this.length()+",cap="+this.capacity());
        if (et == ExtractType.KKK) {
            return;
        }

        int chunklen = end - start;
        int initcap  = this.capacity();

        // sanity check
        if ((start < 0) || (start >= this.length()) || (chunklen < 0) || (end > this.length())) {
            throw new IllegalArgumentException();
        }

        // remove or keep left, middle, right
        char[] nb     = new char[this.capacity()];
        int    nb_len = 0;

        for (int i = 0, j = 0; i < this.length(); ++i) {
            boolean keep = false;

            // left
            if ((i < start) && ((et == ExtractType.KDK) || (et == ExtractType.KKK))) {
                keep = true;

                // middle
            } else if ((i >= start) && (i < end) && (et == ExtractType.KKK)) {
                keep = true;

                // end
            } else if ((i >= end) && ((et == ExtractType.KDK) || (et == ExtractType.DDK) || (et == ExtractType.KKK))) {
                keep = true;
            }

            if (keep) {

                // Log.info("ParseBuffer.removeRange(): keep: i="+i+", j="+j);
                nb[j] = m_buffer[i];
                ++j;
                ++nb_len;
            }
        }

        m_buffer = nb;
        this.setLength(nb_len);
        assert initcap == this.capacity();

        // Log.info("ParseBuffer.removeRange()2: m_buffer="+this.toString()+", len="+this.length()+",cap="+this.capacity());
    }

    /**
     * modify the buffer by deleting the specified range.
     * The start arg is inclusive and end is non-inclusive.
     */
    private void setLength(int len) {
        m_emptySpot = len;
    }

    /**
     * Extract a portion of the buffer that starts and ends
     * with the specified strings. null is returned if not found.
     *
     */
    public String toString() {

        // Log.info("ParseBuffer.toString(): len="+this.length());
        // Log.info("ParseBuffer.toString(): cap="+this.capacity());
        // Log.info("ParseBuffer.toString(): b="+m_buffer.toString());
        StringBuilder s = new StringBuilder(this.length());

        for (int i = 0; i < this.length(); ++i) {
            s.append(m_buffer[i]);
        }

        return (s.toString());
    }

    /**
     * return length of buffer.
     */
    public int length() {
        assert (m_emptySpot >= 0) && (m_emptySpot <= m_buffer.length);

        return (m_emptySpot);
    }

    /**
     * return capacity of buffer.
     */
    public int capacity() {
        return (m_buffer.length);
    }

    /**
     * return the maximum size of the buffer.
     */
    public int maxSize() {
        return (m_maxsize);
    }

    /**
     * return buffer as an array.
     */
    public char[] toArray() {
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
    private void expand(int newcap) throws IllegalStateException {

        // calculate new size
        if (newcap <= 0) {
            throw new IllegalArgumentException();
        }

        if (newcap < this.capacity()) {
            newcap = 2 * this.capacity();
        }

        if (newcap > this.m_maxsize) {
            throw new IllegalStateException("Max size exeeded in ParseBuffer.expand()");
        }

        // Log.info("ParseBuffer.expand(): newcap="+newcap);
        char[] na = new char[newcap];

        System.arraycopy(m_buffer, 0, na, 0, this.length());

        // Log.info("ParseBuffer.expand(): m_buffer1="+this.toString()+",length="+m_buffer.length);
        m_buffer = na;

        // Log.info("ParseBuffer.expand(): m_buffer2="+this.toString()+",length="+m_buffer.length);
        assert (this.length() >= 0) && (this.length() <= this.capacity());

        // Log.info("ParseBuffer.expand(): new length="+this.length());
    }
}
