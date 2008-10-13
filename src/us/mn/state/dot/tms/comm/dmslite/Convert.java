/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.lang.IllegalArgumentException;
import java.lang.StackTraceElement;
import java.util.Arrays;

/**
 * The convert class includes type conversion and formatting operations
 *
 * @author      Stephen Donecker
 * @company University of California, Davis
 * @created     February 15, 2008
 *
 */
public class Convert {

    /** constructor */
    private Convert() {}

    /**
     *  test methods in this class.
     */
    static public boolean test() {
        boolean ok = true;

        // byteArrayToCharArray
        // log.fine("Testing byteArrayToCharArray");
        {
            byte[] ba = new byte[] { 32, 32, 32 };
            char[] ca = byteArrayToCharArray(ba);

            ok = ok && (ca.length == 3);

            for (int i = 0; i < ca.length; ++i) {
                ok = ok && (ca[i] == 32);
            }
        }

        // byteArrayToCharArray
        // log.fine("Testing byteArrayToCharArray");
        {
            byte[] ba = new byte[] { 32, 32, 32, 32, 32 };
            char[] ca = byteArrayToCharArray(2, ba);

            ok = ok && (ca.length == 2);

            for (int i = 0; i < ca.length; ++i) {
                ok = ok && (ca[i] == 32);
            }
        }

        // log.info("Common.Convert.test() ok? "+ok);
        return (ok);
    }

    /**
     * convert byte[] to char[] using assumed encoding.
     *
     * @returns May return null.
     */
    public static char[] byteArrayToCharArray(int len, byte[] ba) {
        char[] ca;

        try {
            ca = new String(ba, 0, len, "ISO-8859-1").toCharArray();
        } catch (Exception UnsupportedEncodingException) {
            ca = null;
        }

        return (ca);
    }

    /**
     * Resize an array to the specified number of elements.
     *
     * @returns resized array.
     */
    public static char[] byteArrayToCharArray(byte[] ba) {
        return (Convert.byteArrayToCharArray(ba.length, ba));
    }
}
