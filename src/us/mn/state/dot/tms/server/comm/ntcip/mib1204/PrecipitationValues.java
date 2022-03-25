/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2022  Minnesota Department of Transportation
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

import java.text.NumberFormat;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.utils.Json;

/**
 * Precipitation sensor sample values.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class PrecipitationValues {

	/** Humidity of 101 indicates error or missing value */
	static private final int HUMIDITY_ERROR_MISSING = 101;

	/** Convert humidity to an integer.
	 * @param rhu Relative humidity in percent. A value of 101 indicates an
	 *            error or missing value.
	 * @return Humidity as a percent or null if missing. */
	static private Integer convertHumidity(ASN1Integer rhu) {
		if (rhu != null) {
			int irhu = rhu.getInteger();
			if (irhu >= 0 && irhu < HUMIDITY_ERROR_MISSING)
				return Integer.valueOf(irhu);
		}
		return null;
	}

	/** Precipitation of 65535 indicates error or missing value */
	static private final int PRECIP_ERROR_MISSING = 65535;

	/** Convert precipitation rate to mm/hr.
	 * @param pr precipitation rate in tenths of gram per square meter per
	 *           second.
	 * @return Precipiration rate in mm/hr or null if missing */
	static private Float convertPrecipRate(ASN1Integer pr) {
		if (pr != null) {
			int tg = pr.getInteger();
			if (tg != PRECIP_ERROR_MISSING) {
				// starting from tenths of g/m^2/s
				// divide by 10,000 => kg/m^2/s
				// equivalent to L/m^2/s (for water)
				// cancel out 0.001 m^3/m^2 => mm/s
				// multiply by 3600 => mm/hr
				// 3600 / 10,000 = 0.36
				return tg * 0.36f;
			}
		}
		return null;
	}

	/** Format precipitation rate to tenths of mm/hr */
	static private String formatPrecipRate(ASN1Integer pr) {
		Float pr_mm_hr = convertPrecipRate(pr);
		if (pr_mm_hr != null) {
			// Format mm/hr to 1 decimal place
			NumberFormat f = NumberFormat.getInstance();
			f.setMaximumFractionDigits(1);
			f.setMinimumFractionDigits(1);
			return f.format(pr_mm_hr);
		} else
			return null;
	}

	/** Convert precipitation total.
	 * @param pr Precipitation total in tenths of kg/m^2.
	 * @return Precipitation total in mm or null */
	static private Float convertPrecipTotal(ASN1Integer pt) {
		if (pt != null) {
			// 1kg of water is 1L volume,
			// equivalent to 1mm depth in a square meter
			int pti = pt.getInteger();
			if (pti != PRECIP_ERROR_MISSING)
				return pti * 0.1f;
		}
		return null;
	}

	/** Format precipitation total to mm units with 1 decimal place */
	static private String formatPrecipTotal(ASN1Integer pt) {
		Float pt_mm = convertPrecipTotal(pt);
		if (pt_mm != null) {
			NumberFormat f = NumberFormat.getInstance();
			f.setMaximumFractionDigits(1);
			f.setMinimumFractionDigits(1);
			return f.format(pt_mm);
		} else
			return null;
	}

	/** Relative humidity */
	public final ASN1Integer relative_humidity = essRelativeHumidity
		.makeInt();

	/** Precipitation rate (tenths of grams/m^2/s) */
	public final ASN1Integer precip_rate = essPrecipRate.makeInt();

	/** One hour precipitation total (tenths of kg/m^2) */
	public final ASN1Integer precip_1_hour =
		essPrecipitationOneHour.makeInt();

	/** Three hour precipitation total (tenths of kg/m^2) */
	public final ASN1Integer precip_3_hours =
		essPrecipitationThreeHours.makeInt();

	/** Six hour precipitation total (tenths of kg/m^2) */
	public final ASN1Integer precip_6_hours =
		essPrecipitationSixHours.makeInt();

	/** Twelve hour precipitation total (tenths of kg/m^2) */
	public final ASN1Integer precip_12_hours =
		essPrecipitationTwelveHours.makeInt();

	/** Twenty-four hour precipitation total (tenths of kg/m^2) */
	public final ASN1Integer precip_24_hours =
		essPrecipitation24Hours.makeInt();

	/** Precipitation situation */
	public final ASN1Enum<EssPrecipSituation> precip_situation =
		new ASN1Enum<EssPrecipSituation>(EssPrecipSituation.class,
		essPrecipSituation.node);

	/** Create precipitation values */
	public PrecipitationValues() {
		relative_humidity.setInteger(HUMIDITY_ERROR_MISSING);
		precip_rate.setInteger(PRECIP_ERROR_MISSING);
		precip_1_hour.setInteger(PRECIP_ERROR_MISSING);
		precip_3_hours.setInteger(PRECIP_ERROR_MISSING);
		precip_6_hours.setInteger(PRECIP_ERROR_MISSING);
		precip_12_hours.setInteger(PRECIP_ERROR_MISSING);
		precip_24_hours.setInteger(PRECIP_ERROR_MISSING);
	}

	/** Get the relative humidity (%) */
	public Integer getRelativeHumidity() {
		return convertHumidity(relative_humidity);
	}

	/** Get the precipitation rate in mm/hr */
	public Integer getPrecipRate() {
		Float pr = convertPrecipRate(precip_rate);
		return (pr != null) ? Math.round(pr) : null;
	}

	/** Get the one hour precipitation total in mm */
	public Integer getPrecip1Hour() {
		Float pt = convertPrecipTotal(precip_1_hour);
		return (pt != null) ? Math.round(pt) : null;
	}

	/** Get the precipitation situation */
	public EssPrecipSituation getPrecipSituation() {
		EssPrecipSituation eps = precip_situation.getEnum();
		return (eps != EssPrecipSituation.undefined) ? eps : null;
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append(Json.num("relative_humidity", getRelativeHumidity()));
		sb.append(Json.num("precip_rate", formatPrecipRate(
			precip_rate)));
		sb.append(Json.num("precip_1_hour", formatPrecipTotal(
			precip_1_hour)));
		sb.append(Json.num("precip_3_hours", formatPrecipTotal(
			precip_3_hours)));
		sb.append(Json.num("precip_6_hours", formatPrecipTotal(
			precip_6_hours)));
		sb.append(Json.num("precip_12_hours", formatPrecipTotal(
			precip_12_hours)));
		sb.append(Json.num("precip_24_hours", formatPrecipTotal(
			precip_24_hours)));
		sb.append(Json.str("precip_situation", getPrecipSituation()));
		return sb.toString();
	}
}
