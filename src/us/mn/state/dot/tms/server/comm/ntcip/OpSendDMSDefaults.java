/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
 * Copyright (C) 2019-2021  Iteris Inc.
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
import java.util.Arrays;
import java.util.HashSet;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.PageTimeHelper;
import static us.mn.state.dot.tms.SystemAttrEnum.*;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibledstar.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mibledstar.MIB.*;
import us.mn.state.dot.tms.server.comm.ntcip.mibskyline.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mibskyline.MIB.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1OctetString;
import us.mn.state.dot.tms.server.comm.snmp.BadValue;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.Multi.JustificationPage;

/**
 * Operation to send default parameters to a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class OpSendDMSDefaults extends OpDMS {

	/** Minimum threshold for comm loss.  Some Ledstar firmware
	 * behaves strangely when dmsTimeCommLoss.0 is 1 minute. */
	static private final int COMM_LOSS_MINIMUM_MINS = 2;

	/** Number of missed polling periods for comm loss threshold */
	static private final int COMM_LOSS_PERIODS = 10;

	/** Temperature (C) at which DMS shuts off automatically */
	static private final int DMS_HIGH_TEMP_CUTOFF = 60;

	/** Create a new operation to send DMS default parameters */
	public OpSendDMSDefaults(DMSImpl d) {
		super(PriorityLevel.SETTINGS, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new SetCommPowerLoss();
	}

	/** Phase to set the comm and power loss times */
	protected class SetCommPowerLoss extends Phase {

		/** Set the comm loss action */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer power_time =dmsShortPowerLossTime.makeInt();
			ASN1Integer comm_time = dmsTimeCommLoss.makeInt();
			MessageIDCode end_msg = new MessageIDCode(
				dmsEndDurationMessage.node);
			power_time.setInteger(0);
			comm_time.setInteger(getCommLossMinutes());
			end_msg.setMemoryType(DmsMessageMemoryType.blank);
			end_msg.setNumber(1);
			end_msg.setCrc(0);
			mess.add(power_time);
			mess.add(comm_time);
			mess.add(end_msg);
			logStore(power_time);
			logStore(comm_time);
			logStore(end_msg);
			mess.storeProps();
			return new PixelService();
		}
	}

	/** Get the comm loss threshold */
	private int getCommLossMinutes() {
		return isCommLossEnabled()
		     ? Math.max(COMM_LOSS_MINIMUM_MINS, getLinkCommLossMins())
		     : 0;
	}

	/** Is DMS comm loss enabled? */
	private boolean isCommLossEnabled() {
		// check for (Ledstar) firmware version which locks up with
		// a CTO error if dmsTimeCommLoss is set to a non-zero value
		return DMS_COMM_LOSS_ENABLE.getBoolean() &&
		      (getVersion() != "VMS-MN2A-27x105 V2.6 Apr 20,2011");
	}

	/** Get the comm loss threshold for the comm link */
	private int getLinkCommLossMins() {
		return controller.getPollPeriodSec() * COMM_LOSS_PERIODS / 60;
	}

	/** Phase to set the pixel service schedule */
	protected class PixelService extends Phase {

		/** Set the pixel service schedule */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer dur = vmsPixelServiceDuration.makeInt();
			ASN1Integer freq = vmsPixelServiceFrequency.makeInt();
			ASN1Integer time = vmsPixelServiceTime.makeInt();
			dur.setInteger(10); // 10 seconds
			freq.setInteger(1440); // 1,440 minutes (24-hours)
			time.setInteger(180); // minute-of-day (3 AM)
			mess.add(dur);
			mess.add(freq);
			mess.add(time);
			logStore(dur);
			logStore(freq);
			logStore(time);
			try {
				mess.storeProps();
			}
			catch (ControllerException e) {
				logError("Error setting pixel service sched");
			}
			return new FlashDefaults();
		}
	}

	/** Phase to set the flash defaults */
	protected class FlashDefaults extends Phase {

		/** Set the flash defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer flash_on = defaultFlashOn.makeInt();
			ASN1Integer flash_off = defaultFlashOff.makeInt();
			flash_on.setInteger(0);
			flash_off.setInteger(0);
			mess.add(flash_on);
			mess.add(flash_off);
			logStore(flash_on);
			logStore(flash_off);
			try {
				mess.storeProps();
			}
			catch (ControllerException e) {
				logError("Error setting flash defaults");
			}
			return new MessageDefaults();
		}
	}

	/** Phase to set the message defaults */
	protected class MessageDefaults extends Phase {

		/** Set the message defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Enum<JustificationLine> line = new ASN1Enum<
				JustificationLine>(JustificationLine.class,
				defaultJustificationLine.node);
			ASN1Enum<JustificationPage> page = new ASN1Enum<
				JustificationPage>(JustificationPage.class,
				defaultJustificationPage.node);
			ASN1Integer on_time = defaultPageOnTime.makeInt();
			ASN1Integer off_time = defaultPageOffTime.makeInt();
			line.setInteger(
				JustificationLine.defaultValue().ordinal()
			);
			page.setInteger(
				JustificationPage.defaultValue().ordinal()
			);
			on_time.setInteger(
				PageTimeHelper.defaultPageOnTimeDs()
			);
			off_time.setInteger(
				PageTimeHelper.defaultPageOffTimeDs()
			);
			mess.add(line);
			mess.add(page);
			mess.add(on_time);
			mess.add(off_time);
			logStore(line);
			logStore(page);
			logStore(on_time);
			logStore(off_time);
			mess.storeProps();
			return new MessageDefaultsV2();
		}
	}

	/** Phase to set the V2 message defaults */
	protected class MessageDefaultsV2 extends Phase {

		/** Set the message defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1OctetString fg = getDefaultForeground();
			ASN1OctetString bg = getDefaultBackground();
			mess.add(bg);
			mess.add(fg);
			logStore(bg);
			logStore(fg);
			try {
				mess.storeProps();
			}
			catch (ControllerException e) {
				// NoSuchName: not a v2 sign
				// GenError: unsupported color
				// BadValue: who knows?
			}
			return vendorDefaults();
		}
	}

	/** Get the default background color */
	private ASN1OctetString getDefaultBackground() throws IOException {
		ASN1OctetString bg = new ASN1OctetString(
			defaultBackgroundRGB.node);
		bg.setOctetString(DMSHelper.getDefaultBackgroundBytes(dms));
		return bg;
	}

	/** Get the default foreground color */
	private ASN1OctetString getDefaultForeground() throws IOException {
		ASN1OctetString fg = new ASN1OctetString(
			defaultForegroundRGB.node);
		fg.setOctetString(DMSHelper.getDefaultForegroundBytes(dms));
		return fg;
	}

	/** Get phase to send vendor-specific defaults */
	private Phase vendorDefaults() {
		if (isLedstar())
			return new LedstarDefaults();
		else if (isAddco()) {
			// NOTE: setting these objects requires use of the
			//       "administrator" community name.  This means we
			//       need to check that the password is not null
			//       before attempting to set them.
			if (isCharMatrix() && controller.getPassword() != null)
				return new AddcoCharMatrixDefaults();
			else
				return null;
		} else
			return null;
	}

	/** Phase to set Ledstar-specific object defaults */
	protected class LedstarDefaults extends Phase {

		/** Set Ledstar-specific object defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer temp = ledHighTempCutoff.makeInt();
			ASN1Integer override = ledSignErrorOverride.makeInt();
			ASN1Integer limit = ledBadPixelLimit.makeInt();
			temp.setInteger(DMS_HIGH_TEMP_CUTOFF);
			limit.setInteger(getBadPixelLimit());
			mess.add(temp);
			mess.add(override);
			mess.add(limit);
			try {
				mess.storeProps();
				logStore(temp);
				logStore(override);
				logStore(limit);
			}
			catch (NoSuchName e) {
				// Must not be a Ledstar sign
				return new SkylineDefaults();
			}
			catch (BadValue e) {
				// Daktronics uses this instead of NoSuchName
				// Is there a better way to check for that?
			}
			return null;
		}
	}

	/** Get bad pixel limit (Ledstar-specific) */
	private int getBadPixelLimit() {
		int count = getPixelCount();
		return (count >= 4) ? (count / 4) : 500;
	}

	/** Phase to set Skyline-specific object defaults */
	protected class SkylineDefaults extends Phase {

		/** Set Skyline-specific object defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer temp = dmsTempCritical.makeInt();
			ASN1Integer day_night = dynBrightDayNight.makeInt();
			ASN1Integer day_rate = dynBrightDayRate.makeInt();
			ASN1Integer night_rate = dynBrightNightRate.makeInt();
			ASN1Integer max_lvl = dynBrightMaxNightManLvl.makeInt();
			temp.setInteger(DMS_HIGH_TEMP_CUTOFF);
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
				mess.storeProps();
				logStore(temp);
				logStore(day_night);
				logStore(day_rate);
				logStore(night_rate);
				logStore(max_lvl);
			}
			catch (NoSuchName e) {
				// Must not be a Skyline sign
			}
			return null;
		}
	}

	/** Phase to set ADDCO-specific character matrix object defaults */
	protected class AddcoCharMatrixDefaults extends Phase {

		/** Set ADDCO-specific object defaults */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer h_border = dmsHorizontalBorder.makeInt();
			ASN1Integer v_border = dmsVerticalBorder.makeInt();
			ASN1Integer h_pitch = vmsHorizontalPitch.makeInt();
			ASN1Integer v_pitch = vmsVerticalPitch.makeInt();
			// ADDCO brick signs have these dimensions
			h_border.setInteger(50);
			v_border.setInteger(69);
			h_pitch.setInteger(69);
			v_pitch.setInteger(69);
			mess.add(h_border);
			mess.add(v_border);
			mess.add(h_pitch);
			mess.add(v_pitch);
			try {
				mess.storeProps();
			}
			catch (NoSuchName ex) {
				// ADDCO VSL ver: 3.4.x throws this (21r7)
				log("ADDCO: ignored ex=" + ex);
			}
			logStore(h_border);
			logStore(v_border);
			logStore(h_pitch);
			logStore(v_pitch);
			return null;
		}
	}
}
