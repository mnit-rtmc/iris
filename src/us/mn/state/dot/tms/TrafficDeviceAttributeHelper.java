/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.utils.IrisInfo;
import us.mn.state.dot.tms.utils.SString;

/**
 * Static traffic device attribute convenience methods accessible from
 * the client and server. These methods are used to retrieve traffic device
 * attributes and validate them. Agency specific methods should be placed in 
 * a subclass. 
 * 
 * Note: this class shares much code and is conceptually very similar to 
 *       SystemAttribute. It would be good at some point to perhaps eliminate
 *	 any code duplication.
 * 
 * @author Michael Darter
 */
public class TrafficDeviceAttributeHelper {

	/** disallow instantiation */
	protected TrafficDeviceAttributeHelper() {
		assert false;
	}

	/** Get the value of the named attribute as a string.
	 *  @param id Traffic device ID, e.g. "V1".
	 *  @param aname Name of an existing attribute.
	 *  @throws IllegalArgumentException if the specified attribute 
	 *	    was not found.
	 *  @return The value of the named attribute;  
	 */
	static protected String getValue(final String id, final String aname)
		throws IllegalArgumentException 
	{
		if(id == null)
			throw new IllegalArgumentException(
				"Traffic device id was null");
		if(aname == null)
			throw new IllegalArgumentException(
				"Device attribute (null) was not found.");
		if(aname.length() > TrafficDeviceAttribute.MAXLEN_ANAME)
			throw new IllegalArgumentException(
				"Device attribute name is too long (>"+
				TrafficDeviceAttribute.MAXLEN_ANAME+").");
		TrafficDeviceAttribute a;
		final String fullaname = id + "_" + aname;
		if(IrisInfo.getClientSide())
			// client side code
			a = SonarState.singleton.
				lookupTrafficDeviceAttribute(fullaname);
		else
			// server side code
			a = TrafficDeviceAttributeImpl.lookup(fullaname);
		if(a == null)
			throw new IllegalArgumentException(
				"Device attribute (" + fullaname + 
				") was not found.");
		return a.getAttributeValue();
	}

	/** Get the value of the named attribute as a string. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param aname Name of an existing attribute.
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default;  
	 */
	static public String getValueDef(final String id, final String aname, 
		String dvalue) 
	{
		String ret = dvalue;
		try {
			ret = getValue(id, aname);
		} catch(IllegalArgumentException ex) { 
			System.err.println(
				getWarningMessage(id, aname, dvalue));
		}
		return ret;
	}

        /** Get the value of the named attribute as an integer.
         *  @param aname Name of an existing attribute.
         *  @throws IllegalArgumentException if the specified attribute 
         *          was not found.
         *  @return The value of the named attribute;  
         */
        static public int getValueInt(final String id, final String aname) 
                throws IllegalArgumentException 
        {
                return SString.stringToInt(
                        TrafficDeviceAttributeHelper.getValue(id, aname));
        }

	/** Get the value of the named attribute as an integer. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param aname Name of an existing attribute.
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default;  
	 */
	static public int getValueIntDef(final String id, final String aname, 
		int dvalue) 
	{
		int ret = dvalue;
		try {
			ret = getValueInt(id, aname);
		} catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(id, aname,
				new Integer(dvalue).toString()));
		}
		return ret;
	}

	/** Get the value of the named attribute as a boolean.
	 *  @param aname Name of an existing attribute.
	 *  @throws IllegalArgumentException if the specified attribute 
	 *	    was not found.
	 *  @return The value of the named attribute;  
	 */
	static public boolean getValueBoolean(final String id, 
		final String aname) throws IllegalArgumentException 
	{
		return SString.stringToBoolean(
			TrafficDeviceAttributeHelper.getValue(id, aname));
	}

	/** Get the value of the named attribute as a boolean. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param aname Name of an existing attribute.
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default;  
	 */
	static public boolean getValueBooleanDef(final String id, 
		final String aname, boolean dvalue) 
	{
		boolean ret = dvalue;
		try {
			ret = getValueBoolean(id, aname);
		} catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(id, aname,
				new Boolean(dvalue).toString()));
		}
		return ret;
	}

	/** return true if the specified attribute matches expected value */
	public static boolean isAttribute(final String id, String aname, 
		String avalue) 
	{
		if(aname == null || avalue == null)
			return false;
		String readvalue = "";
		try {
			readvalue = TrafficDeviceAttributeHelper.getValue(
				id, aname);
		} catch(IllegalArgumentException ex) {
			return false;
		}
		return avalue.equals(readvalue);
	}

	/** return true if the specified attribute matches expected value */
	public static boolean isAttribute(final String id, String aname, 
		boolean avalue) 
	{
		if(aname == null)
			return false;
		boolean readvalue = false;
		try {
			String s = TrafficDeviceAttributeHelper.getValue(
				id, aname);
			readvalue = new Boolean(s).booleanValue();
		} catch(IllegalArgumentException ex) {
			return false;
		}
		return avalue == readvalue;
	}

	/** return a 'missing device attribute' warning message */
	public static String getWarningMessage(String id, String aname, 
		int avalue) 
	{
		return getWarningMessage(id, aname, Integer.toString(avalue));
	}

	/** return a 'missing device attribute' warning message */
	public static String getWarningMessage(String id, String aname, 
		boolean avalue) 
	{
		return getWarningMessage(id, aname, Boolean.toString(avalue));
	}

	/** return a 'missing device attribute' warning message */
	public static String getWarningMessage(String id, String aname,
		String avalue) 
	{
		return "Warning: a device ("+id+") attribute ("+aname+
			") was not found, using a default value ("+avalue+").";
	}

	/** get attribute that flags if the device controlled by an AWS */
	public static boolean awsControlled(String id) {
		return getValueBooleanDef(id, 
			TrafficDeviceAttribute.AWS_CONTROLLED, false);
	}
}

