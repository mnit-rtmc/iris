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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;

/**
 * DMS -- Dynamic Message Sign
 *
 * @author Douglas Lau
 */
public interface DMS extends TrafficDevice {

	/** Bad pixel limit (threshold for activating messages) */
	public int BAD_PIXEL_LIMIT = 35;

	/** High temp cutoff (threshold for shutting off sign) */
	public int HIGH_TEMP_CUTOFF = 60;

	/** Travel time sign status code */
	public int STATUS_TRAVEL_TIME = 5;

	/** Set verification camera */
	public void setCamera(String id) throws TMSException, RemoteException;

	/** Get verification camera */
	public String getCamera() throws RemoteException;

	/** Get the miles downstream of reference point */
	public Float getMile() throws RemoteException;

	/** Set the miles downstream of reference point */
	public void setMile(Float m) throws TMSException, RemoteException;

	/** Get the travel time message template */
	public String getTravel() throws RemoteException;

	/** Set the travel time message template */
	public void setTravel(String t) throws TMSException, RemoteException;

	/** Add a new timing plan to the sign */
	public void addTimingPlan(int period) throws TMSException,
		RemoteException;

	/** Associate (or dissociate) a timing plan with this sign */
	public void setTimingPlan(TimingPlan plan, boolean a)
		throws TMSException, RemoteException;

	/** Check if a timing plan is associated with this sign */
	public boolean hasTimingPlan(TimingPlan plan) throws RemoteException;

	/** Set the message displayed on the sign */
	public void setMessage(String owner, String text, int duration)
		throws InvalidMessageException, RemoteException;

	/** Clear the message displayed on the sign */
	public void clearMessage(String owner) throws RemoteException;

	/** Get the sign message */
	public SignMessage getMessage() throws RemoteException;

	/** Get the make */
	public String getMake() throws RemoteException;

	/** Get the model */
	public String getModel() throws RemoteException;

	/** Get the version */
	public String getVersion() throws RemoteException;

	/** Get sign access description */
	public String getSignAccess() throws RemoteException;

	/** Get sign type description */
	public String getSignType() throws RemoteException;

	/** Get sign height (mm) */
	public int getSignHeight() throws RemoteException;

	/** Get sign width (mm) */
	public int getSignWidth() throws RemoteException;

	/** Get horizontal border (mm) */
	public int getHorizontalBorder() throws RemoteException;

	/** Get vertical border (mm) */
	public int getVerticalBorder() throws RemoteException;

	/** Get sign legend */
	public String getSignLegend() throws RemoteException;

	/** Get beacon type description */
	public String getBeaconType() throws RemoteException;

	/** Get sign technology description */
	public String getSignTechnology() throws RemoteException;

	/** Get character height (pixels) */
	public int getCharacterHeightPixels() throws RemoteException;

	/** Get character width (pixels) */
	public int getCharacterWidthPixels() throws RemoteException;

	/** Get sign height (pixels) */
	public int getSignHeightPixels() throws RemoteException;

	/** Get sign width (pixels) */
	public int getSignWidthPixels() throws RemoteException;

	/** Get horizontal pitch (mm) */
	public int getHorizontalPitch() throws RemoteException;

	/** Get vertical pitch (mm) */
	public int getVerticalPitch() throws RemoteException;

	/** Get the pixel failure count */
	public int getPixelFailureCount() throws RemoteException;

	/** Constant definition for unknown temperature */
	public int UNKNOWN_TEMP = Integer.MIN_VALUE;

	/** Get the minimum cabinet temperature */
	public int getMinCabinetTemp() throws RemoteException;

	/** Get the maximum cabinet temperature */
	public int getMaxCabinetTemp() throws RemoteException;

	/** Get the minimum ambient temperature */
	public int getMinAmbientTemp() throws RemoteException;

	/** Get the maximum ambient temperature */
	public int getMaxAmbientTemp() throws RemoteException;

	/** Get the minimum housing temperature */
	public int getMinHousingTemp() throws RemoteException;

	/** Get the maximum housing temperature */
	public int getMaxHousingTemp() throws RemoteException;

	/** Get the number of supported brightness levels */
	public int getBrightnessLevels() throws RemoteException;

	/** Get the current brightness level */
	public int getBrightnessLevel() throws RemoteException;

	/** Get the maximum photocell level */
	public int getMaxPhotocellLevel() throws RemoteException;

	/** Get the current photocell level */
	public int getPhotocellLevel() throws RemoteException;

	/** Get the light output of the sign */
	public int getLightOutput() throws RemoteException;

	/** Set the brightness table */
	public void setBrightnessTable(int[] t) throws TMSException,
		RemoteException;

	/** Get the brightness table */
	public int[] getBrightnessTable() throws RemoteException;

	/** Get manual brightness control (on or off) */
	public boolean isManualBrightness() throws RemoteException;

	/** Activate/deactivate manual brightness */
	public void activateManualBrightness(boolean m) throws RemoteException;

	/** Set manual brightness level */
	public void setManualBrightness(int l) throws RemoteException;

	/** Activate a pixel test */
	public void testPixels() throws RemoteException;

	/** Activate a lamp test */
	public void testLamps() throws RemoteException;

	/** Get the lamp status */
	public String getLampStatus() throws RemoteException;

	/** Activate a fan test */
	public void testFans() throws RemoteException;

	/** Get the fan status */
	public String getFanStatus() throws RemoteException;

	/** Get the power supply status table */
	public StatusTable getPowerSupplyTable() throws RemoteException;

	/** Get sign face heat tape status */
	public String getHeatTapeStatus() throws RemoteException;

	/** Set the time (in minutes) to heat the sign housing */
	public void setHousingHeatTime(int minutes) throws RemoteException;

	/** Get the remaining housing heat time (in minutes) */
	public int getHousingHeatTime() throws RemoteException;

	/** Set the LDC pot base */
	public void setLdcPotBase(int base) throws RemoteException;

	/** Get the LDC pot base */
	public int getLdcPotBase() throws RemoteException;

	/** Set the pixel low current threshold */
	public void setPixelCurrentLow(int low) throws RemoteException;

	/** Get the pixel low current threshold */
	public int getPixelCurrentLow() throws RemoteException;

	/** Set the pixel high curent threshold */
	public void setPixelCurrentHigh(int high) throws RemoteException;

	/** Get the pixel high current threshold */
	public int getPixelCurrentHigh() throws RemoteException;

	/** Set the bad pixel limit */
	public void setBadPixelLimit(int bad) throws RemoteException;

	/** Get the bad pixel limit */
	public int getBadPixelLimit() throws RemoteException;

	/** Get the number of text lines */
	public int getTextLines() throws RemoteException;

	/** Get the optimal line height (pixels) */
	public int getLineHeightPixels() throws RemoteException;
}
