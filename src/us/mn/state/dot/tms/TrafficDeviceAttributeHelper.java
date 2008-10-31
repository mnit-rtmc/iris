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

import us.mn.state.dot.sonar.Namespace;
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

	/** SONAR namespace */
	static public Namespace namespace;

	/** disallow instantiation */
	protected TrafficDeviceAttributeHelper() {
		assert false;
	}

	/** Return the TrafficDeviceAttribute with the specified name.
	 *  @param name Object name, e.g. "V1_AWS_controlled"
	 *  @return TrafficDeviceAttribute or null if not found.
	 */
	static public TrafficDeviceAttribute get(final String name) {
		if(name == null)
			return null;
		if(name.length() > TrafficDeviceAttribute.MAXLEN_NAME)
			return null;
		return lookup(name);
	}

	/** Lookup a TrafficDeviceAttribute in the SONAR namespace. 
	 *  @return Null if the specified attribute does not exist else the 
	 *  attribute value.
	 */
	static protected TrafficDeviceAttribute lookup(String att) {
		assert namespace != null;
		assert att != null && att.length() > 0;
		return (TrafficDeviceAttribute)namespace.lookupObject(
			TrafficDeviceAttribute.SONAR_TYPE, att);
	}

	/** Return the TrafficDeviceAttribute with the specified id and name.
	 *  @param id Traffic device ID, e.g. "V1".
	 *  @param aname Name of an existing attribute.
	 *  @return TrafficDeviceAttribute or null if not found.
	 */
	static public TrafficDeviceAttribute get(final String id, 
		final String aname)
	{
		return get(createName(id,aname));
	}

	/** Get the value of the named attribute as a string.
	 *  @param id Traffic device ID, e.g. "V1".
	 *  @param name Object name, e.g. "V1_AWS_controlled"
	 *  @throws IllegalArgumentException if the specified attribute 
	 *	    was not found.
	 *  @return The value of the named attribute;  
	 */
	static public String getValue(final String name)
		throws IllegalArgumentException 
	{
		TrafficDeviceAttribute a = get(name);
		if(a == null)
			throw new IllegalArgumentException(
				"Device attribute (" + name + 
				") was not found.");
		return a.getAValue();
	}

	/** Create a new name using an id and aname */
	public static String createName(String id, String aname) {
		if(id==null || aname==null)
			return null;
		if(id.length() <= 0 || aname.length() <= 0)
			return null;
		return id + "_" + aname;
	}

	/** return the device id given the attribute name.
	 *  @param name Name of attribute, e.g. "V1_numofpixels"
	 *  @return Device id, e.g. "V1" or null on error.
	 */
	public static String extractDeviceId(String name) {
		if(name == null)
			return null;
		int i = name.indexOf('_');
		if(i <= 0)
			return null;
		String id = null;
		try { 
			id = name.substring(0, i);
		}
		catch(Exception ex) {
			return null;
		}
		return id;		
	}

	/** Get the value of the named attribute as a string. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param name Object name, e.g. "V1_AWS_controlled"
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default;  
	 */
	static public String getValueDef(final String name, String dvalue) 
	{
		String ret = dvalue;
		try {
			ret = getValue(name);
		} catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(name, dvalue));
		}
		return ret;
	}

        /** Get the value of the named attribute as an integer.
	 *  @param name Object name, e.g. "V1_AWS_controlled"
         *  @throws IllegalArgumentException if the specified attribute 
         *          was not found.
         *  @return The value of the named attribute;  
         */
        static public int getValueInt(final String name) 
                throws IllegalArgumentException 
        {
                return SString.stringToInt(getValue(name));
        }

	/** Get the value of the named attribute as an integer. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param name Object name, e.g. "V1_AWS_controlled"
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default;  
	 */
	static public int getValueIntDef(final String name, int dvalue) 
	{
		int ret = dvalue;
		try {
			ret = getValueInt(name);
		} catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(name,
				new Integer(dvalue).toString()));
		}
		return ret;
	}

	/** Get the value of the named attribute as a boolean.
	 *  @param name Object name, e.g. "V1_AWS_controlled"
	 *  @throws IllegalArgumentException if the specified attribute 
	 *	    was not found.
	 *  @return The value of the named attribute;  
	 */
	static public boolean getValueBoolean(final String name) 
		throws IllegalArgumentException 
	{
		return SString.stringToBoolean(getValue(name));
	}

	/** Get the value of the named attribute as a boolean. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param name Object name, e.g. "V1_AWS_controlled"
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default;  
	 */
	static public boolean getValueBooleanDef(final String name, 
		boolean dvalue) 
	{
		boolean ret = dvalue;
		try {
			ret = getValueBoolean(name);
		} catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(name,
				new Boolean(dvalue).toString()));
		}
		return ret;
	}

	/** return true if the specified attribute matches expected value */
	public static boolean isAttribute(final String name, 
		final String avalue) 
	{
		if(name == null || avalue == null)
			return false;
		String readvalue = "";
		try {
			readvalue = getValue(name);
		} catch(IllegalArgumentException ex) {
			return false;
		}
		return avalue.equals(readvalue);
	}

	/** return true if the specified attribute matches expected value */
	public static boolean isAttribute(final String name, boolean avalue) 
	{
		if(name == null)
			return false;
		boolean readvalue = false;
		try {
			String s = getValue(name);
			readvalue = new Boolean(s).booleanValue();
		} catch(IllegalArgumentException ex) {
			return false;
		}
		return avalue == readvalue;
	}

	/** return a 'missing device attribute' warning message */
	public static String getWarningMessage(final String name, int avalue) 
	{
		return getWarningMessage(name, Integer.toString(avalue));
	}

	/** return a 'missing device attribute' warning message */
	public static String getWarningMessage(final String name, 
		boolean avalue) 
	{
		return getWarningMessage(name, Boolean.toString(avalue));
	}

	/** return a 'missing device attribute' warning message */
	public static String getWarningMessage(final String name, 
		String avalue) 
	{
		return "Warning: a device attribute ("+name+
			") was not found, using a default value ("+avalue+").";
	}

	/** get attribute that flags if the device controlled by an AWS */
	public static boolean awsControlled(String devid) {
		return getValueBooleanDef(createName(devid,
			TrafficDeviceAttribute.AWS_CONTROLLED),false);
	}
}

