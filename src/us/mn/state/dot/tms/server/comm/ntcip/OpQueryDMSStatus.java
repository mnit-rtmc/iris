/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
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

	/** Short Error status */
	protected final ShortErrorStatus shortError = new ShortErrorStatus();

	/** Create a new DMS query status object */
	public OpQueryDMSStatus(DMSImpl d) {
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
			DmsIllumBrightLevelStatus b_level =
				new DmsIllumBrightLevelStatus();
			DmsIllumLightOutputStatus light =
				new DmsIllumLightOutputStatus();
			DmsIllumControl control = new DmsIllumControl();
			mess.add(p_level);
			mess.add(b_level);
			mess.add(light);
			mess.add(control);
			mess.getRequest();
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
		protected Phase poll(AddressedMessage mess) throws IOException {
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
			mess.getRequest();
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
		protected Phase poll(AddressedMessage mess) throws IOException {
			TempMinCtrlCabinet min_cab = new TempMinCtrlCabinet();
			TempMaxCtrlCabinet max_cab = new TempMaxCtrlCabinet();
			mess.add(min_cab);
			mess.add(max_cab);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + min_cab);
			DMS_LOG.log(dms.getName() + ": " + max_cab);
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
			TempMaxAmbient max_amb = new TempMaxAmbient();
			mess.add(min_amb);
			mess.add(max_amb);
			try {
				mess.getRequest();
				DMS_LOG.log(dms.getName() + ": " + min_amb);
				DMS_LOG.log(dms.getName() + ": " + max_amb);
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
			TempMaxSignHousing max_hou = new TempMaxSignHousing();
			mess.add(min_hou);
			mess.add(max_hou);
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + min_hou);
			DMS_LOG.log(dms.getName() + ": " + max_hou);
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
			mess.getRequest();
			DMS_LOG.log(dms.getName() + ": " + shortError);
			if(shortError.shouldReport())
				errorStatus = shortError.getValue();
			return new MoreFailures();
		}
	}

	/** Phase to query more DMS failure status */
	protected class MoreFailures extends Phase {

		/** Query more DMS failure status */
		protected Phase poll(AddressedMessage mess) throws IOException {
			LampFailureStuckOff l_off = new LampFailureStuckOff();
			LampFailureStuckOn l_on = new LampFailureStuckOn();
			DmsActivateMsgError msg_err = new DmsActivateMsgError();
			ControllerErrorStatus con = new ControllerErrorStatus();
			if(shortError.checkError(ShortErrorStatus.LAMP)) {
				mess.add(l_off);
				mess.add(l_on);
			}
			if(shortError.checkError(ShortErrorStatus.MESSAGE))
				mess.add(msg_err);
			if(shortError.checkError(ShortErrorStatus.CONTROLLER))
				mess.add(con);
			if(shortError.checkError(ShortErrorStatus.LAMP |
			                         ShortErrorStatus.MESSAGE |
			                         ShortErrorStatus.CONTROLLER))
			{
				mess.getRequest();
			}
			if(shortError.checkError(ShortErrorStatus.LAMP)) {
				DMS_LOG.log(dms.getName() + ": " + l_off);
				DMS_LOG.log(dms.getName() + ": " + l_on);
		 		String[] lamp = new String[2];
				lamp[DMS.STUCK_OFF_BITMAP] =
					Base64.encode(l_off.getOctetString());
				lamp[DMS.STUCK_ON_BITMAP] =
					Base64.encode(l_on.getOctetString());
				dms.setLampStatus(lamp);
			}
			if(shortError.checkError(ShortErrorStatus.MESSAGE))
				DMS_LOG.log(dms.getName() + ": " + msg_err);
			if(shortError.checkError(ShortErrorStatus.CONTROLLER))
				DMS_LOG.log(dms.getName() + ": " + con);
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
			try {
				mess.getRequest();
			}
			catch(SNMP.Message.NoSuchName e) {
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
		protected Phase poll(AddressedMessage mess) throws IOException {
			SignFaceHeatStatus heat = new SignFaceHeatStatus();
			IllumPowerStatus power = new IllumPowerStatus();
			SensorFailures sensor = new SensorFailures();
			mess.add(heat);
			mess.add(power);
			mess.add(sensor);
			try {
				mess.getRequest();
				DMS_LOG.log(dms.getName() + ": " + heat);
				DMS_LOG.log(dms.getName() + ": " + power);
				DMS_LOG.log(dms.getName() + ": " + sensor);
				dms.setHeatTapeStatus(heat.getValue());
				dms.setPowerStatus(power.getBitmaps());
			}
			catch(SNMP.Message.NoSuchName e) {
				// Ignore; only Skyline has these objects
			}
			return null;
		}
	}
}
