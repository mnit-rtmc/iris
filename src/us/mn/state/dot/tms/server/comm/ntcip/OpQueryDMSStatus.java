/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibskyline.*;

/**
 * This operation queries the status of a DMS. This includes temperature and
 * failure information.
 *
 * @author Douglas Lau
 */
public class OpQueryDMSStatus extends OpDMS {

	/** Photocell level */
	protected final DmsIllumPhotocellLevelStatus p_level =
		new DmsIllumPhotocellLevelStatus();

	/** List of light sensor status */
	protected final LinkedList<String> light_sensors =
		new LinkedList<String>();

	/** Short Error status */
	protected final ShortErrorStatus shortError = new ShortErrorStatus();

	/** Create a new DMS query status object */
	public OpQueryDMSStatus(DMSImpl d) {
		super(PriorityLevel.DEVICE_DATA, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryBrightness();
	}

	/** Phase to query the brightness status */
	protected class QueryBrightness extends Phase {

		/** Query the DMS brightness status */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsIllumBrightLevelStatus b_level =
				new DmsIllumBrightLevelStatus();
			DmsIllumLightOutputStatus light =
				new DmsIllumLightOutputStatus();
			DmsIllumControl control = new DmsIllumControl();
			mess.add(p_level);
			mess.add(b_level);
			mess.add(light);
			mess.add(control);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + p_level);
			DMS_LOG.log(dms.getName() + ": " + b_level);
			DMS_LOG.log(dms.getName() + ": " + light);
			DMS_LOG.log(dms.getName() + ": " + control);
			dms.setLightOutput(light.getPercent());
			return new QueryMessageTable();
		}
	}

	/** Phase to query the DMS message table status */
	protected class QueryMessageTable extends Phase {

		/** Query the DMS message table status */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsNumPermanentMsg perm_num = new DmsNumPermanentMsg();
			DmsNumChangeableMsg chg_num = new DmsNumChangeableMsg();
			DmsMaxChangeableMsg chg_max = new DmsMaxChangeableMsg();
			DmsFreeChangeableMemory chg_mem =
				new DmsFreeChangeableMemory();
			DmsNumVolatileMsg vol_num = new DmsNumVolatileMsg();
			DmsMaxVolatileMsg vol_max = new DmsMaxVolatileMsg();
			DmsFreeVolatileMemory vol_mem =
				new DmsFreeVolatileMemory();
			mess.add(perm_num);
			mess.add(chg_num);
			mess.add(chg_max);
			mess.add(chg_mem);
			mess.add(vol_num);
			mess.add(vol_max);
			mess.add(vol_mem);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + perm_num);
			DMS_LOG.log(dms.getName() + ": " + chg_num);
			DMS_LOG.log(dms.getName() + ": " + chg_max);
			DMS_LOG.log(dms.getName() + ": " + chg_mem);
			DMS_LOG.log(dms.getName() + ": " + vol_num);
			DMS_LOG.log(dms.getName() + ": " + vol_max);
			DMS_LOG.log(dms.getName() + ": " + vol_mem);
			return new ControllerTemperature();
		}
	}

	/** Phase to query the DMS controller temperature status */
	protected class ControllerTemperature extends Phase {

		/** Query the DMS controller temperature */
		protected Phase poll(CommMessage mess) throws IOException {
			TempMinCtrlCabinet min_cab = new TempMinCtrlCabinet();
			TempMaxCtrlCabinet max_cab = new TempMaxCtrlCabinet();
			mess.add(min_cab);
			mess.add(max_cab);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + min_cab);
			DMS_LOG.log(dms.getName() + ": " + max_cab);
			int mn = min_cab.getInteger();
			int mx = max_cab.getInteger();
			if(mn <= mx) {
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
			TempMinAmbient min_amb = new TempMinAmbient();
			TempMaxAmbient max_amb = new TempMaxAmbient();
			mess.add(min_amb);
			mess.add(max_amb);
			try {
				mess.queryProps();
				DMS_LOG.log(dms.getName() + ": " + min_amb);
				DMS_LOG.log(dms.getName() + ": " + max_amb);
				int mn = min_amb.getInteger();
				int mx = max_amb.getInteger();
				if(mn <= mx) {
					dms.setMinAmbientTemp(mn);
					dms.setMaxAmbientTemp(mx);
				} else {
					dms.setMinAmbientTemp(null);
					dms.setMaxAmbientTemp(null);
				}
			}
			catch(SNMP.Message.NoSuchName e) {
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
			TempMinSignHousing min_hou = new TempMinSignHousing();
			TempMaxSignHousing max_hou = new TempMaxSignHousing();
			mess.add(min_hou);
			mess.add(max_hou);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + min_hou);
			DMS_LOG.log(dms.getName() + ": " + max_hou);
			int mn = min_hou.getInteger();
			int mx = max_hou.getInteger();
			if(mn <= mx) {
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
			DMS_LOG.log(dms.getName() + ": " + shortError);
			if(shortError.isMaintenance())
				setMaintStatus(shortError.getValue());
			else
				setMaintStatus("");
			// If no error status bits should be reported,
			// clear the controller error status by setting "".
			if(shortError.isCritical())
				setErrorStatus(shortError.getValue());
			else
				setErrorStatus("");
			return new MoreFailures();
		}
	}

	/** Phase to query more DMS failure status */
	protected class MoreFailures extends Phase {

		/** Query more DMS failure status */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsActivateMsgError msg_err = new DmsActivateMsgError();
			ControllerErrorStatus con = new ControllerErrorStatus();
			if(shortError.checkError(ShortErrorStatus.MESSAGE))
				mess.add(msg_err);
			if(shortError.checkError(ShortErrorStatus.CONTROLLER))
				mess.add(con);
			if(shortError.checkError(ShortErrorStatus.MESSAGE |
			                         ShortErrorStatus.CONTROLLER))
			{
				mess.queryProps();
			}
			if(shortError.checkError(ShortErrorStatus.MESSAGE))
				DMS_LOG.log(dms.getName() + ": " + msg_err);
			if(shortError.checkError(ShortErrorStatus.CONTROLLER))
				DMS_LOG.log(dms.getName() + ": " + con);
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
			catch(SNMP.Message.NoSuchName e) {
				// 1203v2 not supported ...
				dms.setPowerStatus(new String[0]);
				return new LedstarStatus();
			}
			DMS_LOG.log(dms.getName() + ": " + n_pwr);
			if(n_pwr.getInteger() > 0)
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
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + desc);
			DMS_LOG.log(dms.getName() + ": " + p_type);
			DMS_LOG.log(dms.getName() + ": " + status);
			DMS_LOG.log(dms.getName() + ": " + mfr_status);
			DMS_LOG.log(dms.getName() + ": " + voltage);
			supplies[row - 1] = join(desc.getValue(),
				p_type.getValue(), status.getValue(),
				mfr_status.getValue() + ' ' +
				formatVoltage(voltage.getInteger()));
			row++;
			if(row < supplies.length)
				return this;
			else {
				dms.setPowerStatus(supplies);
				return new LightSensorCount();
			}
		}
	}

	/** Format power supply voltage */
	static protected String formatVoltage(int volts) {
		if(volts >= 0 && volts < 65535)
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
			catch(SNMP.Message.NoSuchName e) {
				// 1203v2 not supported ...
				return null;
			}
			DMS_LOG.log(dms.getName() + ": " + n_snsr);
			if(n_snsr.getInteger() > 0) {
				return new QueryLightSensorStatus(
					n_snsr.getInteger());
			} else
				return null;
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
			DMS_LOG.log(dms.getName() + ": " + desc);
			DMS_LOG.log(dms.getName() + ": " + status);
			DMS_LOG.log(dms.getName() + ": " + reading);
			light_sensors.add(desc.getValue() + "," +
				status.getValue() + "," + reading.getInteger());
			row++;
			if(row <= n_sensors)
				return this;
			else
				return null;
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
			catch(SNMP.Message.NoSuchName e) {
				dms.setLdcPotBase(null);
				dms.setPixelCurrentLow(null);
				dms.setPixelCurrentHigh(null);
				return new SkylineStatus();
			}
			DMS_LOG.log(dms.getName() + ": " + potBase);
			DMS_LOG.log(dms.getName() + ": " + low);
			DMS_LOG.log(dms.getName() + ": " + high);
			DMS_LOG.log(dms.getName() + ": " + bad);
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
				DMS_LOG.log(dms.getName() + ": " + heat);
				DMS_LOG.log(dms.getName() + ": " + power);
				DMS_LOG.log(dms.getName() + ": " + sensor);
				dms.setHeatTapeStatus(heat.getValue());
				dms.setPowerStatus(power.getPowerStatus());
			}
			catch(SNMP.Message.NoSuchName e) {
				// Ignore; only Skyline has these objects
			}
			return null;
		}
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(success)
			dms.setPhotocellStatus(formatPhotocellStatus());
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
		if(shortError.checkError(ShortErrorStatus.PHOTOCELL))
			return "fail";
		else
			return "noError";
	}
}
