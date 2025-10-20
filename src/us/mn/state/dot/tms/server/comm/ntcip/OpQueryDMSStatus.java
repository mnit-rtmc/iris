/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
 * Copyright (C) 2023       SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mibledstar.MIB.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibskyline.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mibskyline.MIB.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Flags;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.DisplayString;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;

/**
 * This operation queries the status of a DMS.  This includes temperature and
 * failure information.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class OpQueryDMSStatus extends OpDMS {

	/** Valid temperature range (non-inclusive) */
	static private final int TEMP_MIN = -128;
	static private final int TEMP_MAX = 127;

	/** Get integer value from 0 to 65,535 as percent */
	static private Integer getPercent(ASN1Integer value) {
		int val = value.getInteger();
		return (val >= 0 && val <= 65535)
		      ? Math.round(val / 655.35f)
		      : null;
	}

	/** Get integer value from 0 to max as percent */
	static private Integer getPercent(ASN1Integer value, ASN1Integer max) {
		int val = value.getInteger();
		int mx = max.getInteger();
		return (val >= 0 && val <= mx)
		      ? Math.round((val * 100.0f) / mx)
		      : null;
	}

	/** Get photocell reading */
	static private String getReading(ASN1Integer reading) {
		Integer r = getPercent(reading);
		return (r != null) ? r.toString() : "invalid";
	}

	/** Get power supply voltage */
	static private String getVoltage(ASN1Integer voltage) {
		Integer v = voltage.getInteger();
		return (v >= 0 && v <= 65535)
		      ? Float.toString(v / 100f)
		      : "invalid";
	}

	/** Photocell level (virtual) */
	private final ASN1Integer p_level =
		dmsIllumPhotocellLevelStatus.makeInt();

	/** Maximum photocell level */
	private final ASN1Integer max_level =
		dmsIllumMaxPhotocellLevel.makeInt();

	/** Short Error status */
	private final ASN1Flags<ShortErrorStatus> shortError = new ASN1Flags<
		ShortErrorStatus>(ShortErrorStatus.class,shortErrorStatus.node);

	/** Pixel failure table row count */
	private final ASN1Integer pix_rows = pixelFailureTableNumRows.makeInt();

	/** Number of rows in pixel failure table found by pixel testing */
	private final ASN1Integer test_rows = dmsPixelFailureTestRows.makeInt();

	/** Number of rows in pixel failure table found by message display */
	private final ASN1Integer message_rows =
		dmsPixelFailureMessageRows.makeInt();

	/** Get the pixel error count */
	public int getPixelErrorCount() {
		int n_test = test_rows.getInteger();
		int n_msg = message_rows.getInteger();
		return Math.max(n_test, n_msg);
	}

	/** Create a new DMS query status object */
	public OpQueryDMSStatus(DMSImpl d) {
		super(PriorityLevel.POLL_LOW, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryBrightness();
	}

	/** Phase to query the brightness status */
	protected class QueryBrightness extends Phase {

		/** Query the DMS brightness status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer b_level =
				dmsIllumBrightLevelStatus.makeInt();
			ASN1Integer light =
				dmsIllumLightOutputStatus.makeInt();
			ASN1Enum<DmsIllumControl> control = new ASN1Enum<
				DmsIllumControl>(DmsIllumControl.class,
				dmsIllumControl.node);
			mess.add(p_level);
			mess.add(max_level);
			mess.add(b_level);
			mess.add(light);
			mess.add(control);
			mess.queryProps();
			logQuery(p_level);
			logQuery(max_level);
			logQuery(b_level);
			logQuery(light);
			logQuery(control);
			putStatus(DMS.LIGHT_OUTPUT, getPercent(light));
			return new QueryMessageTable();
		}
	}

	/** Phase to query the DMS message table status */
	protected class QueryMessageTable extends Phase {

		/** Query the DMS message table status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer perm_num = dmsNumPermanentMsg.makeInt();
			ASN1Integer chg_num = dmsNumChangeableMsg.makeInt();
			ASN1Integer chg_max = dmsMaxChangeableMsg.makeInt();
			ASN1Integer chg_mem = dmsFreeChangeableMemory.makeInt();
			ASN1Integer vol_num = dmsNumVolatileMsg.makeInt();
			ASN1Integer vol_max = dmsMaxVolatileMsg.makeInt();
			ASN1Integer vol_mem = dmsFreeVolatileMemory.makeInt();
			mess.add(perm_num);
			mess.add(chg_num);
			mess.add(chg_max);
			mess.add(chg_mem);
			mess.add(vol_num);
			mess.add(vol_max);
			mess.add(vol_mem);
			mess.queryProps();
			logQuery(perm_num);
			logQuery(chg_num);
			logQuery(chg_max);
			logQuery(chg_mem);
			logQuery(vol_num);
			logQuery(vol_max);
			logQuery(vol_mem);
			return new ControllerTemperature();
		}
	}

	/** Phase to query the DMS controller temperature status */
	protected class ControllerTemperature extends Phase {

		/** Query the DMS controller temperature */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			pollTemp(mess, DMS.CABINET_TEMPS, tempMinCtrlCabinet,
				tempMaxCtrlCabinet);
			return new AmbientTemperature();
		}
	}

	/** Phase to query the DMS ambient temperature status */
	protected class AmbientTemperature extends Phase {

		/** Query the DMS ambient temperature */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			pollTemp(mess, DMS.AMBIENT_TEMPS, tempMinAmbient,
				tempMaxAmbient);
			return new HousingTemperature();
		}
	}

	/** Phase to query the DMS housing temperature status */
	protected class HousingTemperature extends Phase {

		/** Query the DMS housing temperature */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			pollTemp(mess, DMS.HOUSING_TEMPS, tempMinSignHousing,
				tempMaxSignHousing);
			return new Failures();
		}
	}

	/** Phase to query the DMS failure status */
	protected class Failures extends Phase {

		/** Query the DMS failure status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(shortError);
			mess.queryProps();
			logQuery(shortError);
			String faults = shortError.getValue(";");
			if (faults.length() > 0)
				putFaults(faults.toLowerCase());
			return new MoreFailures();
		}
	}

	/** Phase to query more DMS failure status */
	protected class MoreFailures extends Phase {

		/** Query more DMS failure status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<DmsActivateMsgError> msg_err = new ASN1Enum<
				DmsActivateMsgError>(DmsActivateMsgError.class,
				dmsActivateMsgError.node);
			ASN1Flags<ControllerErrorStatus> con = new ASN1Flags<
				ControllerErrorStatus>(ControllerErrorStatus
				.class, controllerErrorStatus.node);
			int se = shortError.getInteger();
			if (ShortErrorStatus.MESSAGE.isSet(se))
				mess.add(msg_err);
			if (ShortErrorStatus.CONTROLLER.isSet(se))
				mess.add(con);
			if (ShortErrorStatus.PIXEL.isSet(se))
				mess.add(pix_rows);
			mess.queryProps();
			if (ShortErrorStatus.MESSAGE.isSet(se))
				logQuery(msg_err);
			String faults = "";
			if (ShortErrorStatus.CONTROLLER.isSet(se)) {
				logQuery(con);
				faults = con.getValue(";");
			}
			if (faults.length() > 0)
				putCtrlFaults(faults.toLowerCase(), null);
			else
				putCtrlFaults(null, null);
			if (ShortErrorStatus.PIXEL.isSet(se))
				logQuery(pix_rows);
			return new QueryTestAndMessageRows();
		}
	}

	/** Phase to query (v2) test/message rows in pixel failure table */
	protected class QueryTestAndMessageRows extends Phase {

		/** Query test/message rows in pixel failure table */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(test_rows);
			mess.add(message_rows);
			try {
				mess.queryProps();
				logQuery(test_rows);
				logQuery(message_rows);
			}
			catch (NoSuchName e) {
				// Must be 1203v1 only
				int n_rows = pix_rows.getInteger();
				test_rows.setInteger(n_rows);
				message_rows.setInteger(n_rows);
			}
			return new PowerSupplyCount();
		}
	}

	/** Phase to query 1203v2 power supplies */
	protected class PowerSupplyCount extends Phase {

		/** Query number of power supplies */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer n_pwr = dmsPowerNumRows.makeInt();
			mess.add(n_pwr);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// 1203v2 not supported ...
				return vendorStatus();
			}
			logQuery(n_pwr);
			return (n_pwr.getInteger() > 0)
			      ? new QueryPowerStatus(n_pwr.getInteger())
			      : new LightSensorCount();
		}
	}

	/** Phase to query power supply status */
	protected class QueryPowerStatus extends Phase {
		private final JSONArray supplies;
		private final int n_supplies;
		private int row = 1; // row in DmsPowerStatusTable
		private int n_failed = 0;
		protected QueryPowerStatus(int n) {
			n_supplies = n;
			supplies = new JSONArray();
		}

		/** Query status of one power supply */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			DisplayString desc = new DisplayString(
				dmsPowerDescription.node, row);
			ASN1Enum<DmsPowerType> p_type = new ASN1Enum<
				DmsPowerType>(DmsPowerType.class,
				dmsPowerType.node, row);
			ASN1Enum<DmsPowerStatus> p_stat = new ASN1Enum<
				DmsPowerStatus>(DmsPowerStatus.class,
				dmsPowerStatus.node, row);
			DisplayString mfr_status = new DisplayString(
				dmsPowerMfrStatus.node, row);
			ASN1Integer voltage = dmsPowerVoltage.makeInt(row);
			mess.add(desc);
			mess.add(p_type);
			mess.add(p_stat);
			mess.add(mfr_status);
			mess.add(voltage);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Come on, man!  If we got here, 1203v2
				// objects should really be supported ...
				return vendorStatus();
			}
			logQuery(desc);
			logQuery(p_type);
			logQuery(p_stat);
			logQuery(mfr_status);
			logQuery(voltage);
			JSONObject supply = new JSONObject();
			supply.put("description", desc.getValue());
			supply.put("supply_type", p_type.getValue());
			supply.put("voltage", (p_stat.getEnum().isError())
				? p_stat.getEnum().getDetail(mfr_status)
				: getVoltage(voltage));
			supplies.put(supply);
			if (p_stat.getEnum() == DmsPowerStatus.powerFail)
				n_failed++;
			row++;
			if (row <= n_supplies)
				return this;
			else {
				putStatus(DMS.POWER_SUPPLIES, supplies);
				return new LightSensorCount();
			}
		}
	}

	/** Phase to query 1203v2 light sensor count */
	protected class LightSensorCount extends Phase {

		/** Query number of light sensors */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer n_snsr = dmsLightSensorNumRows.makeInt();
			mess.add(n_snsr);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// 1203v2 not supported ...
				return vendorStatus();
			}
			logQuery(n_snsr);
			if (n_snsr.getInteger() > 0) {
				return new QueryLightSensorStatus(
					n_snsr.getInteger());
			} else
				return vendorStatus();
		}
	}

	/** Phase to query light sensor status */
	protected class QueryLightSensorStatus extends Phase {
		private final JSONArray photocells;
		private final int n_sensors;
		private int row = 1;	// row in DmsLightSensorStatusTable
		protected QueryLightSensorStatus(int n) {
			n_sensors = n;
			photocells = new JSONArray();
		}

		/** Query status of one light sensor */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			DisplayString desc = new DisplayString(
				dmsLightSensorDescription.node, row);
			ASN1Enum<DmsLightSensorStatus> s_stat = new ASN1Enum<
				DmsLightSensorStatus>(DmsLightSensorStatus.class,
				dmsLightSensorStatus.node, row);
			ASN1Integer reading = dmsLightSensorCurrentReading
				.makeInt(row);
			mess.add(desc);
			mess.add(s_stat);
			mess.add(reading);
			mess.queryProps();
			logQuery(desc);
			logQuery(s_stat);
			logQuery(reading);
			JSONObject photocell = new JSONObject();
			photocell.put("description", desc.getValue());
			photocell.put("reading", (s_stat.getEnum().isError())
				? s_stat.getValue()
				: getReading(reading)
			);
			photocells.put(photocell);
			row++;
			if (row <= n_sensors)
				return this;
			else {
				photocell = virtualPhotocell();
				if (photocell != null)
					photocells.put(photocell);
				putStatus(DMS.PHOTOCELLS, photocells);
				return vendorStatus();
			}
		}
	}

	/** Get the virtual photocell value */
	private JSONObject virtualPhotocell() {
		String r = virtualPhotocellReading();
		if (r != null) {
			JSONObject photocell = new JSONObject();
			photocell.put("description", "virtual");
			photocell.put("reading", r);
			return photocell;
		} else
			return null;
	}

	/** Get the virtual photocell reading */
	private String virtualPhotocellReading() {
		Integer r = getPercent(p_level, max_level);
		if (r != null)
			return r.toString();
		else {
			int err = shortError.getInteger();
			if (ShortErrorStatus.PHOTOCELL.isSet(err))
				return "fail";
			else
				return null;
		}
	}

	/** Get phase to query vendor-specific status objects */
	private Phase vendorStatus() {
		// American Signal signs timeout if you ask for unknown objects
		if (isLedstar())
			return new LedstarStatus();
		else if (isSkyline())
			return new SkylineStatus();
		else
			return null;
	}

	/** Phase to query Ledstar-specific status */
	protected class LedstarStatus extends Phase {

		private final ASN1Integer potBase = ledLdcPotBase.makeInt();
		private final ASN1Integer low = ledPixelLow.makeInt();
		private final ASN1Integer high = ledPixelHigh.makeInt();
		private final ASN1Integer limit = ledBadPixelLimit.makeInt();

		/** Query Ledstar-specific status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(potBase);
			mess.add(low);
			mess.add(high);
			mess.add(limit);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Ignore; only LEDSTAR has these objects
				return null;
			}
			logQuery(potBase);
			logQuery(low);
			logQuery(high);
			logQuery(limit);
			putStatus(DMS.LDC_POT_BASE, potBase.getInteger());
			putStatus(DMS.PIXEL_CURRENT_LOW, low.getInteger());
			putStatus(DMS.PIXEL_CURRENT_HIGH, high.getInteger());
			return null;
		}
	}

	/** Phase to query Skyline-specific status */
	protected class SkylineStatus extends Phase {

		/** Query Skyline-specific status */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			IllumPowerStatus power = new IllumPowerStatus();
			SensorFailures sensor = new SensorFailures();
			mess.add(power);
			mess.add(sensor);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Ignore; only Skyline has these objects
				return null;
			}
			logQuery(power);
			logQuery(sensor);
			putStatus(DMS.POWER_SUPPLIES, power.getPowerStatus());
			return null;
		}
	}

	/** Consolidated method to query temperature status */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void pollTemp(CommMessage mess, String key, MIB1203 min_obj,
		MIB1203 max_obj) throws IOException
	{
		ASN1Integer min_temp = min_obj.makeInt();
		ASN1Integer max_temp = max_obj.makeInt();
		mess.add(min_temp);
		mess.add(max_temp);
		try {
			mess.queryProps();
			logQuery(min_temp);
			logQuery(max_temp);
			int mn = min_temp.getInteger();
			int mx = max_temp.getInteger();
			boolean mn_valid = (mn > TEMP_MIN && mn < TEMP_MAX);
			boolean mx_valid = (mx > TEMP_MIN && mx < TEMP_MAX);
			if (mn_valid && mx_valid && mn > mx) {
				// swap min/max temps
				int v = mn;
				mn = mx;
				mx = v;
			}
			JSONArray temps = new JSONArray();
			if (mn_valid)
				temps.put(mn);
			if (mx_valid && (mx > mn || !mn_valid))
				temps.put(mx);
			if (mn_valid || mx_valid)
				putStatus(key, temps);
		}
		catch (NoSuchName e) {
			// Some signs don't have all temperature objects.
		}
	}
}
