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
package us.mn.state.dot.tms.comm.ntcip;

import java.io.IOException;
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.comm.AddressedMessage;

/**
 * This operation queries the status of a DMS. This includes temperature and
 * failure information.
 *
 * @author Douglas Lau
 */
public class DMSQueryStatus extends DMSOperation {

	/** Skyline power supply status table columns */
	static protected final String[] SKYLINE_POWER_COLUMNS = {
		"Power Supply", "Status"
	};

	/** Short Error status */
	protected final ShortErrorStatus shortError = new ShortErrorStatus();

	/** Create a new DMS query status object */
	public DMSQueryStatus(DMSImpl d) {
		super(DEVICE_DATA, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryBrightness();
	}

	/** Phase to query the brightness status */
	protected class QueryBrightness extends Phase {

		/** Query the DMS brightness status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsIllumPhotocellLevelStatus p_level =
				new DmsIllumPhotocellLevelStatus();
			mess.add(p_level);
			DmsIllumBrightLevelStatus b_level =
				new DmsIllumBrightLevelStatus();
			mess.add(b_level);
			DmsIllumLightOutputStatus light =
				new DmsIllumLightOutputStatus();
			mess.add(light);
			DmsIllumControl control = new DmsIllumControl();
			mess.add(control);
			mess.getRequest();
			dms.setPhotocellLevel(p_level.getInteger());
			dms.setBrightnessLevel(b_level.getInteger());
			dms.setLightOutput(light.getInteger());
			if(control.isManual())
				dms.setManualBrightness(true);
			else {
				dms.setManualBrightness(false);
				if(!control.isPhotocell())
					DMS_LOG.log(dms.getId() + ": "+control);
			}
			return new ControllerTemperature();
		}
	}

	/** Phase to query the DMS controller temperature status */
	protected class ControllerTemperature extends Phase {

		/** Query the DMS controller temperature */
		protected Phase poll(AddressedMessage mess) throws IOException {
			TempMinCtrlCabinet min_cab = new TempMinCtrlCabinet();
			mess.add(min_cab);
			TempMaxCtrlCabinet max_cab = new TempMaxCtrlCabinet();
			mess.add(max_cab);
			mess.getRequest();
			dms.setMinCabinetTemp(min_cab.getInteger());
			dms.setMaxCabinetTemp(max_cab.getInteger());
			return new AmbientTemperature();
		}
	}

	/** Phase to query the DMS ambient temperature status */
	protected class AmbientTemperature extends Phase {

		/** Query the DMS ambient temperature */
		protected Phase poll(AddressedMessage mess) throws IOException {
			TempMinAmbient min_amb = new TempMinAmbient();
			mess.add(min_amb);
			TempMaxAmbient max_amb = new TempMaxAmbient();
			mess.add(max_amb);
			try {
				mess.getRequest();
				dms.setMinAmbientTemp(min_amb.getInteger());
				dms.setMaxAmbientTemp(max_amb.getInteger());
			}
			catch(SNMP.Message.NoSuchName e) {
				// Ledstar has no ambient temp objects
			}
			return new HousingTemperature();
		}
	}

	/** Phase to query the DMS housing temperature status */
	protected class HousingTemperature extends Phase {

		/** Query the DMS housing temperature */
		protected Phase poll(AddressedMessage mess) throws IOException {
			TempMinSignHousing min_hou = new TempMinSignHousing();
			mess.add(min_hou);
			TempMaxSignHousing max_hou = new TempMaxSignHousing();
			mess.add(max_hou);
			mess.getRequest();
			dms.setMinHousingTemp(min_hou.getInteger());
			dms.setMaxHousingTemp(max_hou.getInteger());
			return new Failures();
		}
	}

	/** Phase to query the DMS failure status */
	protected class Failures extends Phase {

		/** Query the DMS failure status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(shortError);
//			DmsStatDoorOpen door = new DmsStatDoorOpen();
//			mess.add(door);
			PixelFailureTableNumRows rows =
				new PixelFailureTableNumRows();
			mess.add(rows);
			mess.getRequest();
			dms.setErrorStatus(shortError);
//			DMS_LOG.log(dms.getId() + ": " + door);
			dms.setPixelFailureCount(rows.getInteger());
			return new MoreFailures();
		}
	}

	/** Phase to query more DMS failure status */
	protected class MoreFailures extends Phase {

		/** Query more DMS failure status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			LampFailureStuckOff l_off = new LampFailureStuckOff();
			LampFailureStuckOn l_on = new LampFailureStuckOn();
			if(shortError.checkError(ShortErrorStatus.LAMP)) {
				mess.add(l_off);
				mess.add(l_on);
			}
			FanFailures fan = new FanFailures();
			if(shortError.checkError(ShortErrorStatus.FAN))
				mess.add(fan);
			ControllerErrorStatus con = new ControllerErrorStatus();
			if(shortError.checkError(ShortErrorStatus.CONTROLLER))
				mess.add(con);
			if(shortError.checkError(ShortErrorStatus.LAMP |
				ShortErrorStatus.FAN |
				ShortErrorStatus.CONTROLLER))
			{
				mess.getRequest();
			}
			String lamp = l_off.getValue();
			if(lamp.equals("OK"))
				lamp = l_on.getValue();
			else if(!l_on.getValue().equals("OK"))
				lamp += ", " + l_on.getValue();
			dms.setLampStatus(lamp);
			dms.setFanStatus(fan.getValue());
			DMS_LOG.log(dms.getId() + ": " + con);
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
		protected Phase poll(AddressedMessage mess) throws IOException {
			mess.add(potBase);
			mess.add(low);
			mess.add(high);
			mess.add(bad);
			try { mess.getRequest(); }
			catch(SNMP.Message.NoSuchName e) {
				return new SkylineStatus();
			}
			dms.setLdcPotBase(potBase.getInteger(), false);
			dms.setPixelCurrentLow(low.getInteger(), false);
			dms.setPixelCurrentHigh(high.getInteger(), false);
			dms.setBadPixelLimit(bad.getInteger(), false);
			return null;
		}
	}

	/** Phase to query Skyline-specific status */
	protected class SkylineStatus extends Phase {

		/** Query Skyline-specific status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			IllumPowerStatus power = new IllumPowerStatus();
			mess.add(power);
			SignFaceHeatStatus heat = new SignFaceHeatStatus();
			mess.add(heat);
			SensorFailures sensor = new SensorFailures();
			mess.add(sensor);
			try {
				mess.getRequest();
				dms.setPowerStatus(power.getStatus());
				dms.setHeatTapeStatus(heat.getValue());
				DMS_LOG.log(dms.getId() + ": " + sensor);
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
			dms.notifyUpdate();
		super.cleanup();
	}
}
