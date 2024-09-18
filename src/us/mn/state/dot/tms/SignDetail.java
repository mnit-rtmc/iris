/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2024  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.SonarObject;

/**
 * Sign detail defines detailed parameters of a sign.
 *
 * @author Douglas Lau
 */
public interface SignDetail extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "sign_detail";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = DMS.SONAR_TYPE;

	/** Get DMS type */
	int getDmsType();

	/** Get portable flag */
	boolean getPortable();

	/** Get sign technology description */
	String getTechnology();

	/** Get sign access description */
	String getSignAccess();

	/** Get sign legend */
	String getLegend();

	/** Get beacon type description */
	String getBeaconType();

	/** Get the hardware make */
	String getHardwareMake();

	/** Get the hardware model */
	String getHardwareModel();

	/** Get the software make */
	String getSoftwareMake();

	/** Get the software model */
	String getSoftwareModel();

	/** Get the supported MULTI tags (bit flags of MultiTag) */
	int getSupportedTags();

	/** Get the maximum number of pages */
	int getMaxPages();

	/** Get the maximum MULTI string length */
	int getMaxMultiLen();

	/** Get beacon activation flag (3.6.6.5 in PRL) */
	boolean getBeaconActivationFlag();

	/** Get pixel service flag (3.6.6.6 in PRL) */
	boolean getPixelServiceFlag();
}
