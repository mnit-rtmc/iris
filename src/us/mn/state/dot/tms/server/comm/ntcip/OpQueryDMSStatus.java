/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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
import java.util.LinkedList;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibskyline.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.SNMP;

/**
 * This operation queries the status of a DMS. This includes temperature and
 * failure information.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSStatus extends OpDMS {

	/** Get the pixel maintenance threshold */
	static private int pixelMaintThreshold() {
		return SystemAttrEnum.DMS_PIXEL_MAINT_THRESHOLD.getInt();
	}

	/** Get the light output as percent */
	static private int getPercent(ASN1Integer light) {
		return Math.round(light.getInteger() / 655.35f);
	}

	/** Photocell level */
	private final ASN1Integer p_level =
		dmsIllumPhotocellLevelStatus.makeInt();

	/** List of light sensor status */
	protected final LinkedList<String> light_sensors =
		new LinkedList<String>();

	/** Short Error status */
	protected final ShortErrorStatus shortError = new ShortErrorStatus();

	/** Pixel failure table row count */
	private final PixelFailureTableNumRows pix_rows =
		new PixelFailureTableNumRows();

	/** Number of rows in pixel failure table found by pixel testing */
	private final DmsPixelFailureTestRows test_rows =
		new DmsPixelFailureTestRows();

	/** Number of rows in pixel failure table found by message display */
	private final DmsPixelFailureMessageRows message_rows =
		new DmsPixelFailureMessageRows();

	/** Get the pixel error count */
	public int getPixelErrorCount() {
		int n_test = test_rows.getInteger();
		int n_msg = message_rows.getInteger();
		return Math.max(n_test, n_msg);
	}

	/** Create a new DMS query status object */
	public OpQueryDMSStatus(DMSImpl d) {
		super(PriorityLevel.DEVICE_DATA, d);
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new QueryBrightness();
	}

	/** Phase to query the brightness status */
	protected class QueryBrightness extends Phase {

		/** Query the DMS brightness status */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer b_level =
				dmsIllumBrightLevelStatus.makeInt();
			ASN1Integer light =
				dmsIllumLightOutputStatus.makeInt();
			DmsIllumControl control = new DmsIllumControl();
			mess.add(p_level);
			mess.add(b_level);
			mess.add(light);
			mess.add(control);
			mess.queryProps();
			logQuery(p_level);
			logQuery(b_level);
			logQuery(light);
			logQuery(control);
			dms.setLightOutput(getPercent(light));
			return new QueryMessageTable();
		}
	}

	/** Phase to query the DMS message table status */
	protected class QueryMessageTable extends Phase {

		/** Query the DMS message table status */
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
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer min_cab = tempMinCtrlCabinet.makeInt();
			ASN1Integer max_cab = tempMaxCtrlCabinet.makeInt();
			mess.add(min_cab);
			mess.add(max_cab);
			mess.queryProps();
			logQuery(min_cab);
			logQuery(max_cab);
			int mn = min_cab.getInteger();
			int mx = max_cab.getInteger();
			if (mn <= mx) {
				dms.setMinCabinetTemp(mn);
				dms.setMaxCabinetTemp(mx);
			} else {
				dms.setMinCabinetTemp(null);
				dms.setMaxCabinetTemp(null);
			}
			return new AmbientTemperature();
		}
	}

	/** Phase to query the DMS ambient temperature status */
	protected class AmbientTemperature extends Phase {

		/** Query the DMS ambient temperature */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer min_amb = tempMinAmbient.makeInt();
			ASN1Integer max_amb = tempMaxAmbient.makeInt();
			mess.add(min_amb);
			mess.add(max_amb);
			try {
				mess.queryProps();
				logQuery(min_amb);
				logQuery(max_amb);
				int mn = min_amb.getInteger();
				int mx = max_amb.getInteger();
				if (mn <= mx) {
					dms.setMinAmbientTemp(mn);
					dms.setMaxAmbientTemp(mx);
				} else {
					dms.setMinAmbientTemp(null);
					dms.setMaxAmbientTemp(null);
				}
			}
			catch (SNMP.Message.NoSuchName e) {
				// Ledstar has no ambient temp objects
				dms.setMinAmbientTemp(null);
				dms.setMaxAmbientTemp(null);
			}
			return new HousingTemperature();
		}
	}

	/** Phase to query the DMS housing temperature status */
	protected class HousingTemperature extends Phase {

		/** Query the DMS housing temperature */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer min_hou = tempMinSignHousing.makeInt();
			ASN1Integer max_hou = tempMaxSignHousing.makeInt();
			mess.add(min_hou);
			mess.add(max_hou);
			mess.queryProps();
			logQuery(min_hou);
			logQuery(max_hou);
			int mn = min_hou.getInteger();
			int mx = max_hou.getInteger();
			if (mn <= mx) {
				dms.setMinHousingTemp(mn);
				dms.setMaxHousingTemp(mx);
			} else {
				dms.setMinHousingTemp(null);
				dms.setMaxHousingTemp(null);
			}
			return new Failures();
		}
	}

	/** Phase to query the DMS failure status */
	protected class Failures extends Phase {

		/** Query the DMS failure status */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(shortError);
			mess.queryProps();
			logQuery(shortError);
			return new MoreFailures();
		}
	}

	/** Phase to query more DMS failure status */
	protected class MoreFailures extends Phase {

		/** Query more DMS failure status */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsActivateMsgError msg_err = new DmsActivateMsgError();
			ControllerErrorStatus con = new ControllerErrorStatus();
			if (shortError.checkError(ShortErrorStatus.MESSAGE))
				mess.add(msg_err);
			if (shortError.checkError(ShortErrorStatus.CONTROLLER))
				mess.add(con);
			if (shortError.checkError(ShortErrorStatus.PIXEL))
				mess.add(pix_rows);
			mess.queryProps();
			if (shortError.checkError(ShortErrorStatus.MESSAGE))
				logQuery(msg_err);
			if (shortError.checkError(ShortErrorStatus.CONTROLLER))
				logQuery(con);
			if (shortError.checkError(ShortErrorStatus.PIXEL))
				logQuery(pix_rows);
			return new QueryTestAndMessageRows();
		}
	}

	/** Phase to query (v2) test/message rows in pixel failure table */
	protected class QueryTestAndMessageRows extends Phase {

		/** Query test/message rows in pixel failure table */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(test_rows);
			mess.add(message_rows);
			try {
				mess.queryProps();
				logQuery(test_rows);
				logQuery(message_rows);
			}
			catch (SNMP.Message.NoSuchName e) {
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
		protected Phase poll(CommMessage mess) throws IOException {
			DmsPowerNumRows n_pwr = new DmsPowerNumRows();
			mess.add(n_pwr);
			try {
				mess.queryProps();
			}
			catch (SNMP.Message.NoSuchName e) {
				// 1203v2 not supported ...
				dms.setPowerStatus(new String[0]);
				return new LedstarStatus();
			}
			logQuery(n_pwr);
			if (n_pwr.getInteger() > 0)
				return new QueryPowerStatus(n_pwr.getInteger());
			else {
				dms.setPowerStatus(new String[0]);
				return new LightSensorCount();
			}
		}
	}

	/** Phase to query power supply status */
	protected class QueryPowerStatus extends Phase {
		protected final String[] supplies;
		protected int row = 1;		// row in DmsPowerStatusTable
		protected int n_failed = 0;
		protected QueryPowerStatus(int n_pwr) {
			supplies = new String[n_pwr];
		}

		/** Query status of one power supply */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsPowerDescription desc = new DmsPowerDescription(row);
			DmsPowerType p_type = new DmsPowerType(row);
			DmsPowerStatus status = new DmsPowerStatus(row);
			DmsPowerMfrStatus mfr_status = new DmsPowerMfrStatus(
				row);
			DmsPowerVoltage voltage = new DmsPowerVoltage(row);
			mess.add(desc);
			mess.add(p_type);
			mess.add(status);
			mess.add(mfr_status);
			mess.add(voltage);
			try {
				mess.queryProps();
			}
			catch (SNMP.Message.NoSuchName e) {
				// Come on, man!  If we got here, 1203v2
				// objects should really be supported ...
				dms.setPowerStatus(new String[0]);
				return new LedstarStatus();
			}
			logQuery(desc);
			logQuery(p_type);
			logQuery(status);
			logQuery(mfr_status);
			logQuery(voltage);
			supplies[row - 1] = join(desc.getValue(),
				p_type.getValue(), status.getValue(),
				mfr_status.getValue() + ' ' +
				formatVoltage(voltage.getInteger()));
			if (status.getEnum() == DmsPowerStatus.Enum.powerFail)
				n_failed++;
			row++;
			if (row <= supplies.length)
				return this;
			else {
				if (2 * n_failed > supplies.length)
					setErrorStatus("POWER");
				dms.setPowerStatus(supplies);
				return new LightSensorCount();
			}
		}
	}

	/** Format power supply voltage */
	static protected String formatVoltage(int volts) {
		if (volts >= 0 && volts < 65535)
			return "" + (volts / 100f) + " volts";
		else
			return "";
	}

	/** Trim and join four strings */
	static protected String join(String s0, String s1, String s2, String s3)
	{
		return s0.trim() + ',' + s1.trim() + ',' + s2.trim() + ',' +
		       s3.trim();
	}

	/** Phase to query 1203v2 light sensors */
	protected class LightSensorCount extends Phase {

		/** Query number of light sensors */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsLightSensorNumRows n_snsr =
				new DmsLightSensorNumRows();
			mess.add(n_snsr);
			try {
				mess.queryProps();
			}
			catch (SNMP.Message.NoSuchName e) {
				// 1203v2 not supported ...
				return new LedstarStatus();
			}
			logQuery(n_snsr);
			if (n_snsr.getInteger() > 0) {
				return new QueryLightSensorStatus(
					n_snsr.getInteger());
			} else
				return new LedstarStatus();
		}
	}

	/** Phase to query light sensor status */
	protected class QueryLightSensorStatus extends Phase {
		protected final int n_sensors;
		protected int row = 1;	// row in DmsLightSensorStatusTable
		protected QueryLightSensorStatus(int n_snsr) {
			n_sensors = n_snsr;
		}

		/** Query status of one light sensor */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsLightSensorDescription desc =
				new DmsLightSensorDescription(row);
			DmsLightSensorStatus status =
				new DmsLightSensorStatus(row);
			DmsLightSensorCurrentReading reading =
				new DmsLightSensorCurrentReading(row);
			mess.add(desc);
			mess.add(status);
			mess.add(reading);
			mess.queryProps();
			logQuery(desc);
			logQuery(status);
			logQuery(reading);
			light_sensors.add(desc.getValue() + "," +
				status.getValue() + "," + reading.getInteger());
			row++;
			if (row <= n_sensors)
				return this;
			else
				return new LedstarStatus();
		}
	}

	/** Phase to query Ledstar-specific status */
	protected class LedstarStatus extends Phase {

		protected final LedLdcPotBase potBase = new LedLdcPotBase();
		protected final LedPixelLow low = new LedPixelLow();
		protected final LedPixelHigh high = new LedPixelHigh();
		protected final LedBadPixelLimit bad = new LedBadPixelLimit();

		/** Query Ledstar-specific status */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(potBase);
			mess.add(low);
			mess.add(high);
			mess.add(bad);
			try {
				mess.queryProps();
			}
			catch (SNMP.Message.NoSuchName e) {
				dms.setLdcPotBase(null);
				dms.setPixelCurrentLow(null);
				dms.setPixelCurrentHigh(null);
				return new SkylineStatus();
			}
			logQuery(potBase);
			logQuery(low);
			logQuery(high);
			logQuery(bad);
			dms.setLdcPotBase(potBase.getInteger());
			dms.setPixelCurrentLow(low.getInteger());
			dms.setPixelCurrentHigh(high.getInteger());
			return null;
		}
	}

	/** Phase to query Skyline-specific status */
	protected class SkylineStatus extends Phase {

		/** Query Skyline-specific status */
		protected Phase poll(CommMessage mess) throws IOException {
			SignFaceHeatStatus heat = new SignFaceHeatStatus();
			IllumPowerStatus power = new IllumPowerStatus();
			SensorFailures sensor = new SensorFailures();
			mess.add(heat);
			mess.add(power);
			mess.add(sensor);
			try {
				mess.queryProps();
				logQuery(heat);
				logQuery(power);
				logQuery(sensor);
				dms.setHeatTapeStatus(heat.getValue());
				dms.setPowerStatus(power.getPowerStatus());
				if (power.isCritical())
					setErrorStatus("POWER");
			}
			catch (SNMP.Message.NoSuchName e) {
				// Ignore; only Skyline has these objects
			}
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if (isSuccess()) {
			dms.setPhotocellStatus(formatPhotocellStatus());
			setMaintStatus(formatMaintStatus());
			setErrorStatus(formatErrorStatus());
		}
		super.cleanup();
	}

	/** Format the photocell status table */
	protected String[] formatPhotocellStatus() {
		light_sensors.add("composite," + photocellStatus() + "," +
			p_level.getInteger());
		return light_sensors.toArray(new String[0]);
	}

	/** Get the composite photocell status */
	protected String photocellStatus() {
		if (shortError.checkError(ShortErrorStatus.PHOTOCELL))
			return "fail";
		else
			return "noError";
	}

	/** Format the new maintenance status */
	protected String formatMaintStatus() {
		if (shortError.isMaintenance())
			return shortError.getValue();
		if (shortError.checkError(ShortErrorStatus.PIXEL) &&
		    getPixelErrorCount() > pixelMaintThreshold())
			return "Too many pixel errors";
		else
			return "";
	}

	/** Format the new error status */
	protected String formatErrorStatus() {
		// If no error status bits should be reported,
		// clear the controller error status by setting "".
		if (shortError.isCritical())
			return shortError.getValue();
		else
			return "";
	}
}
