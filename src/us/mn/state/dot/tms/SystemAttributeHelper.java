/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
 * Static System Attribute convenience methods accessible from
 * the client and server. These methods are used to retrieve
 * system attributes and validate them. Agency specific methods
 * should be placed in a subclass.
 * @author Michael Darter
 */
public class SystemAttributeHelper {

	/** SONAR namespace */
	static public Namespace namespace;

	/** Disallow instantiation */
	protected SystemAttributeHelper() {
		assert false;
	}

	/** Get the SystemAttribute with the specified name.
	 *  @param aname Name of an existing attribute.
	 *  @return SystemAttribute or null if not found.
	 */
	static public SystemAttribute get(String aname) {
		if(aname == null)
			return null;
		if(aname.length() > SystemAttribute.MAXLEN_ANAME)
			return null;
		return lookup(aname);
	}

	/** Lookup a SystemAttribute in the SONAR namespace. 
	 *  @return The specified system attribute, or null if the it does not
	 *  exist in the namespace.
	 */
	static protected SystemAttribute lookup(String att) {
		assert namespace != null;
		assert att != null && att.length() > 0;
		return (SystemAttribute)namespace.lookupObject(
			SystemAttribute.SONAR_TYPE, att);
	}

	/** Get the value of the named attribute as a string.
	 *  @param aname Name of an existing system attribute.
	 *  @throws IllegalArgumentException if the specified attribute 
	 *	    was not found.
	 *  @return The value of the named attribute
	 */
	static public String getValue(final String aname)
		throws IllegalArgumentException 
	{
		SystemAttribute a = get(aname);
		if(a == null) {
			throw new IllegalArgumentException("System attribute ("
				+ aname + ") was not found.");
		}
		return a.getValue();
	}

	/** Get the value of the named attribute as a string. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param aname Name of an existing system attribute.
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default
	 */
	static public String getValue(final String aname, String dvalue) {
		try {
			return getValue(aname);
		}
		catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(aname, dvalue));
			return dvalue;
		}
	}

        /** Get the value of the named attribute as an integer.
         *  @param aname Name of an existing system attribute.
         *  @throws IllegalArgumentException if the specified attribute 
         *          was not found.
         *  @return The value of the named attribute
         */
        static public int getValueInt(final String aname) 
                throws IllegalArgumentException 
        {
                return SString.stringToInt(getValue(aname));
        }

	/** Get the value of the named attribute as an integer. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param aname Name of an existing system attribute.
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default
	 */
	static public int getValueInt(final String aname, int dvalue) {
		try {
			return getValueInt(aname);
		}
		catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(aname,
				new Integer(dvalue).toString()));
			return dvalue;
		}
	}

        /** Get the value of the named attribute as a float.
         *  @param aname Name of an existing system attribute.
         *  @throws IllegalArgumentException if the specified attribute 
         *          was not found or could not be parsed.
         *  @return The value of the named attribute
         */
	static public float getValueFloat(String aname)
		throws IllegalArgumentException
	{
		try {
			return Float.parseFloat(getValue(aname));
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException("System Attribute " +
				aname + " could not be parsed");
		}
	}

	/** Get the value of the named attribute as a float. If the
	 *  attribute is not valid, the default is silently returned.
	 *  @param aname Name of an existing system attribute.
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default
	 */
	static public float getValueFloat(String aname, float dvalue) {
		try {
			return getValueFloat(aname);
		}
		catch(IllegalArgumentException e) {
			System.err.println(getWarningMessage(aname,
				new Float(dvalue).toString()));
			return dvalue;
		}
	}

	/** Get the value of the named attribute as a boolean.
	 *  @param aname Name of an existing system attribute.
	 *  @throws IllegalArgumentException if the specified attribute 
	 *	    was not found.
	 *  @return The value of the named attribute
	 */
	static public boolean getValueBoolean(final String aname) 
		throws IllegalArgumentException 
	{
		return SString.stringToBoolean(getValue(aname));
	}

	/** Get the value of the named attribute as a boolean. If the
	 *  attribute is not found, the default is silently returned.
	 *  @param aname Name of an existing system attribute.
	 *  @param dvalue Default value.
	 *  @return The value of the named attribute or the default
	 */
	static public boolean getValueBoolean(final String aname,
		boolean dvalue)
	{
		try {
			return getValueBoolean(aname);
		}
		catch(IllegalArgumentException ex) { 
			System.err.println(getWarningMessage(aname,
				new Boolean(dvalue).toString()));
			return dvalue;
		}
	}

	/** return true if the specified attribute matches expected value */
	public static boolean isAttribute(String aname, String avalue) {
		if(aname == null || avalue == null)
			return false;
		try {
			return avalue.equals(getValue(aname));
		}
		catch(IllegalArgumentException ex) {
			return false;
		}
	}

	/** return true if the specified attribute matches expected value */
	public static boolean isAttribute(String aname, boolean avalue) {
		if(aname == null)
			return false;
		try {
			String s = getValue(aname);
			boolean readvalue = new Boolean(s).booleanValue();
			return avalue == readvalue;
		}
		catch(IllegalArgumentException ex) {
			return false;
		}
	}

	/** Get the agency ID */
	static public String agencyId() {
		return getValue(SystemAttribute.AGENCY_ID, "");
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

	/** Get the DMS polling frequency (seconds) */
	static public int getDmsPollFreqSecs() {
		final int MINIMUM = 5;
		final int DEFAULT = 30;
		int secs = getValueInt(SystemAttribute.DMS_POLL_FREQ_SECS,
			DEFAULT);
		return (secs < MINIMUM ? MINIMUM : secs);
	}

	/** Get the DMS maximum number of text lines */
	static public int getDmsMaxLines() {
		final int MINIMUM = 1;
		final int DEFAULT = 3;
		final int MAXIMUM = 12;
		int v = getValueInt(SystemAttribute.DMS_MAX_LINES, DEFAULT);
		return getBoundedInt(v, MINIMUM, MAXIMUM);
	}

	/** Get an integer value bounded by minimum and maximum values */
	static protected int getBoundedInt(int i, int min, int max) {
		return Math.max(min, Math.min(i, max));
	}

	/** Get the DMS pixel test timeout (seconds) */
	static public int getDmsPixelTestTimeout() {
		return getValueInt(SystemAttribute.DMS_PIXEL_TEST_TIMEOUT, 30);
	}

	/** Get the DMS lamp test timeout (seconds) */
	static public int getDmsLampTestTimeout() {
		return getValueInt(SystemAttribute.DMS_LAMP_TEST_TIMEOUT, 30);
	}

	/** Get the DMS pixel off limit (in a message) */
	static public int getDmsPixelOffLimit() {
		return getValueInt(SystemAttribute.DMS_PIXEL_OFF_LIMIT, 2);
	}

	/** Get the DMS pixel on limit (near a message) */
	static public int getDmsPixelOnLimit() {
		return getValueInt(SystemAttribute.DMS_PIXEL_ON_LIMIT, 1);
	}

	/** Get the DMS high temperature cutoff (degress Celsius) */
	static public int getDmsHighTempCutoff() {
		return getValueInt(SystemAttribute.DMS_HIGH_TEMP_CUTOFF, 60);
	}

	/** Get the DMS page on time (seconds) */
	static public float getDmsPageOnSecs() {
		return getValueFloat(SystemAttribute.DMS_PAGE_ON_SECS, 2.0f);
	}

	/** Get the DMS page off time (seconds) */
	static public float getDmsPageOffSecs() {
		return getValueFloat(SystemAttribute.DMS_PAGE_OFF_SECS, 0.0f);
	}

	/** Get the DMS default line justification */
	static public int getDmsDefaultJustificationLine() {
		return getValueInt(
		       SystemAttribute.DMS_DEFAULT_JUSTIFICATION_LINE,
		       MultiString.JustificationLine.CENTER.ordinal());
	}

	/** Get the DMS default page justification */
	static public int getDmsDefaultJustificationPage() {
		return getValueInt(
		       SystemAttribute.DMS_DEFAULT_JUSTIFICATION_PAGE,
		       MultiString.JustificationPage.TOP.ordinal());
	}

	/** Get the meter green time (seconds) */
	static public float getMeterGreenSecs() {
		return getValueFloat(SystemAttribute.METER_GREEN_SECS, 1.3f);
	}

	/** Get the meter yellow time (seconds) */
	static public float getMeterYellowSecs() {
		return getValueFloat(SystemAttribute.METER_YELLOW_SECS, 0.7f);
	}

	/** Get the meter minimum red time (seconds) */
	static public float getMeterMinRedSecs() {
		return getValueFloat(SystemAttribute.METER_MIN_RED_SECS, 0.1f);
	}

	/** Get the meter maximum release rate (vehicles per hour) */
	static public int getMeterMaxRelease() {
		float cycle = getMeterGreenSecs() + getMeterYellowSecs() +
			getMeterMinRedSecs();
		return (int)(Interval.HOUR / cycle);
	}

	/** Get the meter maximum red time (seconds) */
	static public float getMeterMaxRedSecs() {
		return getValueFloat(SystemAttribute.METER_MAX_RED_SECS, 13f);
	}

	/** Get the meter minimum release rate (vehicles per hour) */
	static public int getMeterMinRelease() {
		float cycle = getMeterGreenSecs() + getMeterYellowSecs() +
			getMeterMaxRedSecs();
		return (int)(Interval.HOUR / cycle);
	}

	/** Get the incident ring 1 miles */
	static public int getIncidentRing1Miles() {
		return getValueInt(SystemAttribute.INCIDENT_RING_1_MILES, 0);
	}

	/** Get the incident ring 2 miles */
	static public int getIncidentRing2Miles() {
		return getValueInt(SystemAttribute.INCIDENT_RING_2_MILES, 0);
	}

	/** Get the incident ring 3 miles */
	static public int getIncidentRing3Miles() {
		return getValueInt(SystemAttribute.INCIDENT_RING_3_MILES, 0);
	}

	/** Get the incident ring 4 miles */
	static public int getIncidentRing4Miles() {
		return getValueInt(SystemAttribute.INCIDENT_RING_4_MILES, 0);
	}

	/** Get the minimum overall speed for travel times */
	static public int getTravelTimeMinMPH() {
		return getValueInt(SystemAttribute.TRAVEL_TIME_MIN_MPH, 15);
	}

	/** Get the maximum number of legs for travel time routes */
	static public int getTravelTimeMaxLegs() {
		return getValueInt(SystemAttribute.TRAVEL_TIME_MAX_LEGS, 8);
	}

	/** Get the maximum distance of a travel time route */
	static public int getTravelTimeMaxMiles() {
		return getValueInt(SystemAttribute.TRAVEL_TIME_MAX_MILES, 16);
	}

	/** Get the TESLA host name (and port) */
	static public String getTeslaHost() {
		return getValue(SystemAttribute.TESLA_HOST, null);
	}

	/** return a 'missing system attribute' warning message */
	public static String getWarningMessage(String aname, int avalue) {
		return getWarningMessage(aname, Integer.toString(avalue));
	}

	/** return a 'missing system attribute' warning message */
	public static String getWarningMessage(String aname, boolean avalue) {
		return getWarningMessage(aname, Boolean.toString(avalue));
	}

	/** return a 'missing system attribute' warning message */
	public static String getWarningMessage(String aname, String avalue) {
		return "Warning: a system attribute (" + aname +
			") was not found, using a default value (" + avalue +
			").";
	}

	/** Validate the database version */
	static protected void validateDatabaseVersion() {
		String c_version = "@@VERSION@@";
		String db_version = getValue(SystemAttribute.DATABASE_VERSION);
		if(!validateVersions(c_version, db_version)) {
			StringBuilder b = new StringBuilder();
			b.append("Failure: database_version (");
			b.append(db_version);
			b.append(") does not match the required value (");
			b.append(c_version);
			b.append(").  Shutting down.");
			System.err.println(b.toString());
			System.exit(1);
		}
	}

	/** Validate the database version */
	static protected boolean validateVersions(String v0, String v1) {
		String[] va0 = v0.split("\\.");
		String[] va1 = v1.split("\\.");
		// Versions must be "major.minor.micro"
		if(va0.length != 3 || va1.length != 3)
			return false;
		// Check that major versions match
		if(!va0[0].equals(va1[0]))
			return false;
		// Check that minor versions match
		if(!va0[1].equals(va1[1]))
			return false;
		// It's OK if micro versions don't match
		return true;
	}

	/** Validate the agency id */
	static protected void validateAgencyId() {
		shutdownValidate(SystemAttribute.AGENCY_ID,
			new String[] {SystemAttribute.AGENCY_ID_MNDOT,
			SystemAttribute.AGENCY_ID_CALTRANS_D10});
	}

	/** Validate an attribute and force shutdown if invalid */
	static protected void shutdownValidate(String aname, String[] values) {
		if(!validate(aname, values)) {
			String msg = "";
			msg += "Failure: a required system attribute (" + aname;
			msg += ") did not match an expected value (";
			for(int i = 0; i < values.length; ++i) {
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
		try {
			String readValue = getValue(aname);
			for(String i: values) {
				if(readValue.equals(i))
					return true;
			}
			return false;
		}
		catch(IllegalArgumentException ex) {
			return false;
		}
	}

	/** Return number of CameraViewer PTZ preset buttons */
	public static int numPresetBtns() {
		final int MIN = 0;
		final int MAX = 20;
		final int DEFAULT = 3;
		int np = getValueInt(
			SystemAttribute.CAMERAVIEWER_NUM_PRESET_BTNS, DEFAULT);
		np = (np < MIN ? MIN : np);
		np = (np > MAX ? MAX : np);
		return np;
	}

	/** Get the minimum available pages for DMS messages */
	static public int getDmsMessageMinPages() {
		return getValueInt(SystemAttribute.DMS_MESSAGE_MIN_PAGES, 1);
	}

	/** Return number of video frames before stream is stopped */
	public static int numVideoFramesBeforeStop() {
		final int MIN = 0;
		final int DEFAULT = 900;
		int nf = getValueInt(
			SystemAttribute.CAMERAVIEWER_NUM_VIDEO_FRAMES, DEFAULT);
		nf = (nf < MIN ? MIN : nf);
		return nf;
	}

	/** Is Automated Warning System enabled? */
	static public boolean isAwsEnabled() {
		return getValueBoolean(SystemAttribute.DMS_AWS_ENABLE, false);
	}

	/** Is DMS status stuff enabled? */
	static public boolean isDmsStatusEnabled() {
		return getValueBoolean(SystemAttribute.DMS_STATUS_ENABLE,
			false);
	}

	/** Is DMS duration selection enabled? */
	static public boolean isDmsDurationEnabled() {
		return getValueBoolean(SystemAttribute.DMS_DURATION_ENABLE,
			true);
	}

	/** Is DMS font selection enabled? */
	static public boolean isDmsFontSelectionEnabled() {
		return getValueBoolean(
			SystemAttribute.DMS_FONT_SELECTION_ENABLE, false);
	}

	/** Is DMS message blank lines enabled? */
	static public boolean isDmsMessageBlankLineEnabled() {
		return getValueBoolean(
			SystemAttribute.DMS_MESSAGE_BLANK_LINE_ENABLE, true);
	}

	/** Is DMS pixel testing enabled? */
	static public boolean isDmsPixelStatusEnabled() {
		return getValueBoolean(
			SystemAttribute.DMS_PIXEL_STATUS_ENABLE, true);
	}

	/** Is DMS brightness tab enabled? */
	static public boolean isDmsBrightnessEnabled() {
		return getValueBoolean(
			SystemAttribute.DMS_BRIGHTNESS_ENABLE, true);
	}

	/** Is DMS manufacturer-specific tab enabled? */
	static public boolean isDmsManufacturerEnabled() {
		return getValueBoolean(
			SystemAttribute.DMS_MANUFACTURER_ENABLE, true);
	}

	/** Is DMS reset enabled? */
	static public boolean isDmsResetEnabled() {
		return getValueBoolean(SystemAttribute.DMS_RESET_ENABLE, false);
	}

	/** Is Camera PTZ panel enabled? */
	static public boolean isCameraPTZPanelEnabled() {
		return getValueBoolean(SystemAttribute.CAMERA_PTZ_PANEL_ENABLE,
			false);
	}

	/** Is Fahrenheit temperature enabled? */
	static public boolean isTempFahrenheitEnabled() {
		return getValueBoolean(SystemAttribute.TEMP_FAHRENHEIT_ENABLE,
			false);
	}

	/** SMTP host name */
	static public String getEmailSmtpHost() {
		return getValue(SystemAttribute.EMAIL_SMTP_HOST);
	}

	/** Name of sender for server emails */
	static public String getEmailSenderServer() {
		return getValue(SystemAttribute.EMAIL_SENDER_SERVER);
	}

	/** Name of sender for client emails */
	static public String getEmailSenderClient() {
		return getValue(SystemAttribute.EMAIL_SENDER_CLIENT);
	}

	/** Address of recipient of AWS emails */
	static public String getEmailRecipientAws() {
		return getValue(SystemAttribute.EMAIL_RECIPIENT_AWS);
	}

	/** Address of recipient of BUG emails */
	static public String getEmailRecipientBugs() {
		return getValue(SystemAttribute.EMAIL_RECIPIENT_BUGS);
	}
}
