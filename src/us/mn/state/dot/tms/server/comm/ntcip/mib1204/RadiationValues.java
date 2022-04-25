/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.utils.Json;

/**
 * Solar radiation sample values.
 *
 * @author Douglas Lau
 */
public class RadiationValues {

	/** Value of 1441 indicates error or missing value */
	static private final int MINUTES_ERROR_MISSING = 1441;

	/** Value of 2049 indicates error or missing value */
	static private final int RADIATION_ERROR_MISSING = 2049;

	/** Value of 65,535 indicates error or missing value */
	static private final int SOLAR_ERROR_MISSING = 65535;

	/** Total daily minutes of sun (0-1440 minutes) */
	public final ASN1Integer total_sun = essTotalSun.makeInt();

	/** Cloud situation */
	public final ASN1Enum<CloudSituation> cloud_situation =
		new ASN1Enum<CloudSituation>(CloudSituation.class,
		essCloudSituation.node);

	/** Instantaneous terrestrial radiation (watts / m^2) */
	public final RadiationObject instantaneous_terrestrial =
		new RadiationObject("instantaneous_terrestrial_radiation", 
			essInstantaneousTerrestrialRadiation.makeInt());

	/** Instantaneous solar radiation (watts / m^2) */
	public final RadiationObject instantaneous_solar =
		new RadiationObject("instantaneous_solar_radiation", 
			essInstantaneousSolarRadiation.makeInt());

	/** Total radiation during collection period (watts / m^2) */
	public final RadiationObject total_radiation =
		new RadiationObject("total_radiation", 
			essTotalRadiation.makeInt());

	/** Total radiation period (seconds) */
	public final ASN1Integer total_radiation_period =
		essTotalRadiationPeriod.makeInt();

	/** Solar radiation over 24 hours (Joules / m^2; deprecated in V2) */
	public final ASN1Integer solar_radiation = essSolarRadiation.makeInt();

	/** Create radiation values */
	public RadiationValues() {
		total_sun.setInteger(MINUTES_ERROR_MISSING);
		total_radiation_period.setInteger(0);
		solar_radiation.setInteger(SOLAR_ERROR_MISSING);
	}

	/** Get the total sun minutes (0-1440) */
	public Integer getTotalSun() {
		int s = total_sun.getInteger();
		return (s >= 0 && s < MINUTES_ERROR_MISSING)
		      ? Integer.valueOf(s)
		      : null;
	}

	/** Get the cloud situation */
	public CloudSituation getCloudSituation() {
		CloudSituation cs = cloud_situation.getEnum();
		return (cs != CloudSituation.undefined) ? cs : null;
	}

	/** Get the total radiation period (seconds) */
	public Integer getTotalRadiationPeriod() {
		int s = total_radiation_period.getInteger();
		return (s > 0) ? Integer.valueOf(s) : null;
	}

	/** Get the solar radiation (joules / m^2) */
	public Integer getSolarRadiation() {
		int s = solar_radiation.getInteger();
		return (s < SOLAR_ERROR_MISSING) ? Integer.valueOf(s) : null;
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append(Json.num("total_sun", getTotalSun()));
		sb.append(Json.str("cloud_situation", getCloudSituation()));
		Integer s = getSolarRadiation();
		if (s != null) {
			sb.append(Json.num("solar_radiation", s));
		} else {
			sb.append(instantaneous_terrestrial.toJson());
			sb.append(instantaneous_solar.toJson());
			sb.append(total_radiation.toJson());
			sb.append(Json.num("total_radiation_period",
				getTotalRadiationPeriod()));
		}
		return sb.toString();
	}
}
