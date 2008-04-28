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

//~--- JDK imports ------------------------------------------------------------

import java.lang.IllegalArgumentException;
import java.lang.StackTraceElement;
import java.lang.Throwable;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides contract verification operations
 *
 * @author      Stephen Donecker
 * @company University of California, Davis
 * @created     February 15, 2008
 */
public class Verify {

    /** The logger */
    private static final Logger log = Logger.getLogger(Verify.class.getName());

    /** Verify if a value is within a specified range */
    public static boolean isInRange(int aValue, int minValue, int maxValue) {
        if ((minValue <= aValue) && (aValue <= maxValue)) {
            return true;
        } else {
            StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

            System.err.println("Contract Violation: value (" + aValue + ") is not in the range [" + minValue + ","
                               + maxValue + "]");

            return false;
        }
    }

    /** Verify if a value is greater than another value */
    public static boolean isGreaterThan(int aValue, int anotherValue) {
        if (aValue > anotherValue) {
            return true;
        } else {
            StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

            System.err.println("Contract Violation: value (" + aValue + ") is not > (" + anotherValue + ")");

            return false;
        }
    }

    /** Verify if a value is less than another value */
    public static boolean isLessThan(int aValue, int anotherValue) {
        if (aValue < anotherValue) {
            return true;
        } else {
            StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

            System.err.println("Contract Violation: value (" + aValue + ") is not < (" + anotherValue + ")");

            return false;
        }
    }

    /** Verify if a value is greater than or equal to another value */
    public static boolean isGreaterThanOrEqual(int aValue, int anotherValue) {
        if (aValue >= anotherValue) {
            return true;
        } else {
            StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

            System.err.println("Contract Violation: value (" + aValue + ") is not >= (" + anotherValue + ")");

            return false;
        }
    }

    /** Verify if a value is less than or equal to another value */
    public static boolean isLessThanOrEqual(int aValue, int anotherValue) {
        if (aValue <= anotherValue) {
            return true;
        } else {
            StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

            System.err.println("Contract Violation: value (" + aValue + ") is not <= (" + anotherValue + ")");

            return false;
        }
    }

    /** Verify if a value is equal to another value */
    public static boolean isEqual(int aValue, int anotherValue) {
        if (aValue == anotherValue) {
            return true;
        } else {
            StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

            System.err.println("Contract Violation: value (" + aValue + ") is not = (" + anotherValue + ")");

            return false;
        }
    }

    /** Verify if a value is contained in a given set */
    public static boolean isInSet(byte aByte, byte[] aSet) {
        boolean inSet = false;

        for (int i = 0; i < aSet.length; i++) {
            if (aByte == aSet[i]) {
                inSet = true;
            }
        }

        if (!inSet) {
            StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

            System.err.println("Contract Violation: value (0x" + Convert.toHexString(aByte) + ") is not in the set {"
                               + Convert.toHexString(aSet, ',') + "} (hex)");
        }

        return inSet;
    }

    /** Verify if an array is null */
    public static boolean isNull(byte[] anArray) {
        if (anArray == null) {
            StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

            System.err.println("Contract Violation: array is NULL");

            return true;
        } else {
            return false;
        }
    }

    /** Verify if an object is null */
    public static boolean isNull(Object anObject) {
        if (anObject == null) {
            StackTraceElement stack = new Throwable().fillInStackTrace().getStackTrace()[1];

            System.err.println("Contract Violation: object is NULL");

            return true;
        } else {
            return false;
        }
    }

    /** validate, true returned on success else false. */
    public static boolean test() {
        boolean ok = true;

        // add tests here
        return (ok);
    }
}
