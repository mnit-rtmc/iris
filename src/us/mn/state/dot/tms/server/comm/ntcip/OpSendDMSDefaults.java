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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibskyline.*;

/**
 * Operation to send default parameters to a DMS.
 *
 * @author Douglas Lau
 */
public class OpSendDMSDefaults extends OpDMS {

	/** Create a new operation to send DMS default parameters */
	public OpSendDMSDefaults(DMSImpl d) {
		super(DOWNLOAD, d);
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new SetCommPowerLoss();
	}

	/** Phase to set the comm and power loss times */
	protected class SetCommPowerLoss extends Phase {

		/** Set the comm loss action */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsShortPowerLossTime power_time =
				new DmsShortPowerLossTime();
			DmsTimeCommLoss comm_time = new DmsTimeCommLoss();
			DmsEndDurationMessage end_msg =
				new DmsEndDurationMessage();
			power_time.setInteger(0);
			comm_time.setInteger(10);
			end_msg.setMemoryType(DmsMessageMemoryType.Enum.blank);
			end_msg.setNumber(1);
			end_msg.setCrc(0);
			mess.add(power_time);
			mess.add(comm_time);
			mess.add(end_msg);
			DMS_LOG.log(dms.getName() + ":= " + power_time);
			DMS_LOG.log(dms.getName() + ":= " + comm_time);
			DMS_LOG.log(dms.getName() + ":= " + end_msg);
			mess.setRequest();
			return new PixelService();
		}
	}

	/** Phase to set the pixel service schedule */
	protected class PixelService extends Phase {

		/** Set the pixel service schedule */
		protected Phase poll(AddressedMessage mess) throws IOException {
			VmsPixelServiceDuration dur =
				new VmsPixelServiceDuration();
			VmsPixelServiceFrequency freq =
				new VmsPixelServiceFrequency();
			VmsPixelServiceTime time = new VmsPixelServiceTime();
			dur.setInteger(10);
			freq.setInteger(1440);
			time.setInteger(180);
			mess.add(dur);
			mess.add(freq);
			mess.add(time);
			DMS_LOG.log(dms.getName() + ":= " + dur);
			DMS_LOG.log(dms.getName() + ":= " + freq);
			DMS_LOG.log(dms.getName() + ":= " + time);
			mess.setRequest();
			return new MessageDefaults();
		}
	}

	/** Phase to set the message defaults */
	protected class MessageDefaults extends Phase {

		/** Set the message defaults */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DefaultJustificationLine line =
				new DefaultJustificationLine();
			DefaultJustificationPage page =
				new DefaultJustificationPage();
			DefaultPageOnTime on_time = new DefaultPageOnTime();
			DefaultPageOffTime off_time = new DefaultPageOffTime();
			line.setEnum(MultiString.JustificationLine.CENTER);
			page.setEnum(MultiString.JustificationPage.TOP);
			on_time.setInteger(Math.round(10 * SystemAttrEnum.
				DMS_PAGE_ON_DEFAULT_SECS.getFloat()));
			off_time.setInteger(Math.round(10 * SystemAttrEnum.
				DMS_PAGE_OFF_DEFAULT_SECS.getFloat()));
			mess.add(line);
			mess.add(page);
			mess.add(on_time);
			mess.add(off_time);
			DMS_LOG.log(dms.getName() + ":= " + line);
			DMS_LOG.log(dms.getName() + ":= " + page);
			DMS_LOG.log(dms.getName() + ":= " + on_time);
			DMS_LOG.log(dms.getName() + ":= " + off_time);
			mess.setRequest();
			return new LedstarDefaults();
		}
	}

	/** Phase to set Ledstar-specific object defaults */
	protected class LedstarDefaults extends Phase {

		/** Set Ledstar-specific object defaults */
		protected Phase poll(AddressedMessage mess) throws IOException {
			LedHighTempCutoff temp = new LedHighTempCutoff();
			LedSignErrorOverride override = 
				new LedSignErrorOverride();
			LedBadPixelLimit limit = new LedBadPixelLimit();
			temp.setInteger(
				SystemAttrEnum.DMS_HIGH_TEMP_CUTOFF.getInt());
			limit.setInteger(500);
			mess.add(temp);
			mess.add(override);
			mess.add(limit);
			try {
				mess.setRequest();
				DMS_LOG.log(dms.getName() + ":= " + temp);
				DMS_LOG.log(dms.getName() + ":= " + override);
				DMS_LOG.log(dms.getName() + ":= " + limit);
			}
			catch(SNMP.Message.NoSuchName e) {
				// Must not be a Ledstar sign
				return new SkylineDefaults();
			}
			return null;
		}
	}

	/** Phase to set Skyline-specific object defaults */
	protected class SkylineDefaults extends Phase {

		/** Set Skyline-specific object defaults */
		protected Phase poll(AddressedMessage mess) throws IOException {
			DmsTempCritical temp = new DmsTempCritical();
			DynBrightDayNight day_night = new DynBrightDayNight();
			DynBrightDayRate day_rate = new DynBrightDayRate();
			DynBrightNightRate night_rate =new DynBrightNightRate();
			DynBrightMaxNightManLvl max_lvl =
				new DynBrightMaxNightManLvl();
			temp.setInteger(
				SystemAttrEnum.DMS_HIGH_TEMP_CUTOFF.getInt());
			day_night.setInteger(32);
			day_rate.setInteger(1);
			night_rate.setInteger(15);
			max_lvl.setInteger(20);
			mess.add(temp);
			mess.add(day_night);
			mess.add(day_rate);
			mess.add(night_rate);
			mess.add(max_lvl);
			try {
				mess.setRequest();
				DMS_LOG.log(dms.getName() + ":= " + temp);
				DMS_LOG.log(dms.getName() + ":= " + day_night);
				DMS_LOG.log(dms.getName() + ":= " + day_rate);
				DMS_LOG.log(dms.getName() + ":= " + night_rate);
				DMS_LOG.log(dms.getName() + ":= " + max_lvl);
			}
			catch(SNMP.Message.NoSuchName e) {
				// Must not be a Skyline sign
				return new AddcoDefaults();
			}
			return null;
		}
	}

	/** Phase to set ADDCO-specific object defaults */
	protected class AddcoDefaults extends Phase {

		/** Set ADDCO-specific object defaults */
		protected Phase poll(AddressedMessage mess) throws IOException {
			// ADDCO brick signs have these dimensions
			String make = dms.getMake();
			// NOTE: setting these objects requires use of the
			//       "administrator" community name.  We need to
			//       check that the password is not null before
			//       attempting to set them.
			if(make != null &&
			   make.startsWith("ADDCO") &&
			   dms.getDmsType() == DMSType.VMS_CHAR.ordinal() &&
			   controller.getPassword() != null)
			{
				DmsHorizontalBorder h_border =
					new DmsHorizontalBorder();
				DmsVerticalBorder v_border =
					new DmsVerticalBorder();
				VmsHorizontalPitch h_pitch =
					new VmsHorizontalPitch();
				VmsVerticalPitch v_pitch =
					new VmsVerticalPitch();
				h_border.setInteger(50);
				v_border.setInteger(69);
				h_pitch.setInteger(69);
				v_pitch.setInteger(69);
				mess.add(h_border);
				mess.add(v_border);
				mess.add(h_pitch);
				mess.add(v_pitch);
				mess.setRequest();
				DMS_LOG.log(dms.getName() + ":= " + h_border);
				DMS_LOG.log(dms.getName() + ":= " + v_border);
				DMS_LOG.log(dms.getName() + ":= " + h_pitch);
				DMS_LOG.log(dms.getName() + ":= " + v_pitch);
			}
			return null;
		}
	}
}
