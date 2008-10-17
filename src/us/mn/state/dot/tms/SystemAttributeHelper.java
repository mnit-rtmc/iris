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
 * Static System Attribute convenience methods accessible from
 * the client and server. These methods are used to retrieve
 * system attributes and validate them.
 *
 * @author Michael Darter
 */
public class SystemAttributeHelper {

	/** disallow instantiation */
	private SystemAttributeHelper() {
		assert false;
	}

	/** Get the value of the named attribute as a string.
	 *  @param aname Name of an existing system attribute.
	 *  @throws IllegalArgumentException if the specified attribute 
	 *	    was not found.
	 *  @return The value of the named attribute;  
	 */
	static protected String getValue(final String aname)
		throws IllegalArgumentException 
	{
		if(aname == null)
			throw new IllegalArgumentException(
				"System attribute (null) was not found.");
		SystemAttribute a;
		if(IrisInfo.getClientSide())
			// client side code
			a = SonarState.singleton.lookupSystemAttribute(aname);
		else
			// server side code
			a = SystemAttributeImpl.lookupSystemAttribute(aname);
		if(a == null)
			throw new IllegalArgumentException(
				"System attribute ("+aname+") was not found.");
		return a.getValue();
	}

        /** Get the value of the named attribute as an integer.
         *  @param aname Name of an existing system attribute.
         *  @throws IllegalArgumentException if the specified attribute 
         *          was not found.
         *  @return The value of the named attribute;  
         */
        static public int getValueInt(final String aname) 
                throws IllegalArgumentException 
        {
                return SString.stringToInt(
                        SystemAttributeHelper.getValue(aname));
        }

	/** Get the value of the named attribute as an integer. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param aname Name of an existing system attribute.
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default;  
	 */
	static public int getValueIntDef(final String aname,int dvalue) {
		int ret = dvalue;
		try {
			ret = getValueInt(aname);
		} catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(aname,
				new Integer(dvalue).toString()));
		}
		return ret;
	}

	/** Get the value of the named attribute as a boolean.
	 *  @param aname Name of an existing system attribute.
	 *  @throws IllegalArgumentException if the specified attribute 
	 *	    was not found.
	 *  @return The value of the named attribute;  
	 */
	static public boolean getValueBoolean(final String aname) 
		throws IllegalArgumentException 
	{
		return SString.stringToBoolean(
			SystemAttributeHelper.getValue(aname));
	}

	/** Get the value of the named attribute as a boolean. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param aname Name of an existing system attribute.
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default;  
	 */
	static public boolean getValueBooleanDef(final String aname,boolean dvalue) {
		boolean ret = dvalue;
		try {
			ret = getValueBoolean(aname);
		} catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(aname,
				new Boolean(dvalue).toString()));
		}
		return ret;
	}

	/** return true if the specified attribute matches expected value */
	public static boolean isAttribute(String aname, String avalue) {
		if(aname == null || avalue == null)
			return false;
		String readvalue = "";
		try {
			readvalue = SystemAttributeHelper.getValue(aname);
		} catch(IllegalArgumentException ex) {
			return false;
		}
		return avalue.equals(readvalue);
	}

	/** return true if the specified attribute matches expected value */
	public static boolean isAttribute(String aname, boolean avalue) {
		if(aname == null)
			return false;
		boolean readvalue = false;
		try {
			String s = SystemAttributeHelper.getValue(aname);
			readvalue = new Boolean(s).booleanValue();
		} catch(IllegalArgumentException ex) {
			return false;
		}
		return avalue == readvalue;
	}

	/** return true if the agency id matches */
	public static boolean isAgencyCaltransD10() {
		return isAttribute(SystemAttribute.AGENCY_ID,
			SystemAttribute.AGENCY_ID_CALTRANS_D10);
	}

	/** return true if the agency id matches */
	public static boolean isAgencyMnDOT() {
		return isAttribute(SystemAttribute.AGENCY_ID,
			SystemAttribute.AGENCY_ID_MNDOT);
	}

	/** return the DMS poll time in seconds */
	public static int getDMSPollTimeSecs() {
		final int MINIMUM = 5;
		final int DEFAULT = 30;
		int secs = SystemAttributeHelper.getValueIntDef(
			SystemAttribute.DMS_POLL_FREQ_SECS, DEFAULT);
		return (secs < MINIMUM ? MINIMUM : secs);
	}

	/** return a 'missing system attribute' warning message */
	public static String getWarningMessage(String aname,int avalue) {
		return getWarningMessage(aname, Integer.toString(avalue));
	}

	/** return a 'missing system attribute' warning message */
	public static String getWarningMessage(String aname,boolean avalue) {
		return getWarningMessage(aname, Boolean.toString(avalue));
	}

	/** return a 'missing system attribute' warning message */
	public static String getWarningMessage(String aname,String avalue) {
		return "Warning: a system attribute ("+aname+
			") was not found, using a default value ("+avalue+").";
	}

	/** Validate the database version */
	static protected void validateDatabaseVersion() {
		shutdownValidate(SystemAttribute.DATABASE_VERSION,
			new String[] {"@@VERSION@@"});
	}

	/** Validate the agency id */
	static protected void validateAgencyId() {
		shutdownValidate(SystemAttribute.AGENCY_ID,
			new String[] {SystemAttribute.AGENCY_ID_MNDOT,
			SystemAttribute.AGENCY_ID_CALTRANS_D10});
	}

	/** Validate an attribute and force shutdown if invalid */
	static protected void shutdownValidate(String aname, String[] values) 
	{
		if(!validate(aname,values)) {
			String msg = "";
			msg += "Failure: a required system attribute ("+aname;
			msg += ") did not match an expected value (";
			for(int i=0; i<values.length; ++i) {
				msg += values[i];
				if(i < values.length - 1)
					msg += ",";
			}
			msg += "). Shutting down.";
			System.err.println(msg);
			System.exit(1);
		}
	}

	/** Validate a system attribute against multiple values */
	static protected boolean validate(String aname,String[] values) {
		if(aname == null || values == null || values.length <= 0)
			return false;
		String readValue;
		try {
			readValue = getValue(aname);
		} catch(IllegalArgumentException ex) {
			return false;
		}
		for(String i : values)
			if(readValue.equals(i))
				return true;
		return false;
	}

	/** Return true to use the DMSDispatcher get status button */
	public static boolean useGetStatusBtn() {
		return getValueBooleanDef(
			SystemAttribute.DMSDISPATCHER_GETSTATUS_BTN,false);
	}

	/** Return true to display onscreen PTZ controls in CameraViewer */
	public static boolean useOnScrnPTZ() {
		return getValueBooleanDef(
			SystemAttribute.CAMERAVIEWER_ONSCRN_PTZCTRLS,false);
	}

	/** Return number of CameraViewer PTZ preset buttons */
	public static int numPresetBtns() {
		return SystemAttributeHelper.getValueIntDef(
			SystemAttribute.CAMERAVIEWER_NUM_PRESET_BTNS, 3);
	}

	/** Return dmslite operation timeout in seconds */
	public static int dmsliteOpTimeoutSecs() {
		return SystemAttributeHelper.getValueIntDef(
			SystemAttribute.CALTRANS_D10_DMSLITE_OP_TIMEOUT_SECS, 60+5);
	}

	/** Return dmslite modem operation timeout in seconds */
	public static int dmsliteModemOpTimeoutSecs() {
		return SystemAttributeHelper.getValueIntDef(
			SystemAttribute.CALTRANS_D10_DMSLITE_MODEM_OP_TIMEOUT_SECS, 5*60+5);
	}

}

