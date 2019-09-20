/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
 * Copyright (C) 2017  Iteris Inc.
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
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.DECIMETERS;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;

/**
 * Atmospheric / visibility sample values.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class AtmosphericValues {

	/** Pressure of 65535 indicates error or missing value */
	static private final int PRESSURE_INVALID_MISSING = 65535;

	/** Convert atmospheric pressure to pascals.
	 * @param apr Atmospheric pressure in 1/10ths of millibars, with
	 *            65535 indicating an error or missing value.
	 * @return Pressure in pascals */
	static private Integer convertAtmosphericPressure(ASN1Integer apr) {
		if (apr != null) {
			int tmb = apr.getInteger();
			if (tmb != PRESSURE_INVALID_MISSING) {
				double mb = (double) tmb * 0.1;
				double pa = mb * 100;
				return new Integer((int) Math.round(pa));
			}
		}
		return null;
	}

	/** Visibility of 1000001 indicates error or missing value */
	static private final int VISIBILITY_ERROR_MISSING = 1000001;

	/** Convert visibility to Distance.
	 * @param vis Visibility in decimeters with 1000001 indicating an error
	 *            or missing value.
	 * @return Visibility distance or null for missing */
	static private Distance convertVisibility(ASN1Integer vis) {
		if (vis != null) {
			int iv = vis.getInteger();
			if (iv != VISIBILITY_ERROR_MISSING)
				return new Distance(iv, DECIMETERS);
		}
		return null;
	}

	/** Atmospheric pressure in tenths of millibars */
	public final ASN1Integer atmospheric_pressure = essAtmosphericPressure
		.makeInt();

	/** Visibility in decimeters */
	public final ASN1Integer visibility = essVisibility.makeInt();

	/** Visibility situation */
	public final ASN1Enum<EssVisibilitySituation> visibility_situation =
		new ASN1Enum<EssVisibilitySituation>(EssVisibilitySituation.class,
		essVisibilitySituation.node);

	/** Get atmospheric pressure in pascals */
	public Integer getAtmosphericPressure() {
		return convertAtmosphericPressure(atmospheric_pressure);
	}

	/** Get visibility in meters */
	public Float getVisibility() {
		Distance vis = convertVisibility(visibility);
		return (vis != null) ? vis.asFloat(METERS) : null;
	}

	/** Get the visibility situation */
	public EssVisibilitySituation getVisibilitySituation() {
		return visibility_situation.getEnum();
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append(Json.num("atmospheric_pressure",
			getAtmosphericPressure()));
		sb.append(Json.num("visibility", getVisibility()));
		sb.append(Json.str("visibility_situation",
			getVisibilitySituation()));
		return sb.toString();
	}
}
