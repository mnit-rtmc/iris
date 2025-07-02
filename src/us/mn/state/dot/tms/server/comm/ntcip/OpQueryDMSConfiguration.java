/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
 * Copyright (C) 2016-2017  SRF Consulting Group
 * Copyright (C) 2017       Iteris Inc.
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
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DMSType;
import static us.mn.state.dot.tms.SignMessage.MAX_LINES;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.SignConfigImpl;
import us.mn.state.dot.tms.server.SignDetailImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1201.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201.*;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Flags;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;

/**
 * Operation to query the configuration of a DMS.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 * @author Michael Darter
 */
public class OpQueryDMSConfiguration extends OpDMS {

	/** Sign access */
	private final ASN1Flags<DmsSignAccess> access = new ASN1Flags<
		DmsSignAccess>(DmsSignAccess.class, dmsSignAccess.node);

	/** Sign type */
	private final DmsSignType type = new DmsSignType();

	/** Face width */
	private final ASN1Integer face_width = dmsSignWidth.makeInt();

	/** Face height */
	private final ASN1Integer face_height = dmsSignHeight.makeInt();

	/** Horizontal border */
	private final ASN1Integer h_border = dmsHorizontalBorder.makeInt();

	/** Vertical border */
	private final ASN1Integer v_border = dmsVerticalBorder.makeInt();

	/** Legend */
	private final ASN1Enum<DmsLegend> legend = new ASN1Enum<DmsLegend>(
		DmsLegend.class, dmsLegend.node);

	/** Beacon */
	private final ASN1Enum<DmsBeaconType> beaconType = new ASN1Enum<
		DmsBeaconType>(DmsBeaconType.class, dmsBeaconType.node);

	/** Sign technology */
	private final ASN1Flags<DmsSignTechnology> tech = new ASN1Flags<
		DmsSignTechnology>(DmsSignTechnology.class,
		dmsSignTechnology.node);

	/** Sign pixel height */
	private final ASN1Integer s_height = vmsSignHeightPixels.makeInt();

	/** Sign pixel width */
	private final ASN1Integer s_width = vmsSignWidthPixels.makeInt();

	/** Horizontal pitch */
	private final ASN1Integer h_pitch = vmsHorizontalPitch.makeInt();

	/** Vertical pitch */
	private final ASN1Integer v_pitch = vmsVerticalPitch.makeInt();

	/** Character height */
	private final ASN1Integer c_height = vmsCharacterHeightPixels.makeInt();

	/** Character width */
	private final ASN1Integer c_width = vmsCharacterWidthPixels.makeInt();

	/** Monochrome color */
	private final MonochromeColor mono_color = new MonochromeColor();

	/** Color scheme */
	private final ASN1Enum<ColorScheme> color_scheme = new ASN1Enum<
		ColorScheme>(ColorScheme.class, dmsColorScheme.node);

	/** Supported MULTI tags */
	private final DmsSupportedMultiTags supported_tags =
		new DmsSupportedMultiTags();

	/** Maximum number of pages */
	private final ASN1Integer max_pages = dmsMaxNumberPages.makeInt();

	/** Maximum multi string length */
	private final ASN1Integer max_multi_len =
		dmsMaxMultiStringLength.makeInt();

	/** Beacon activation flag (3.6.6.5 in PRL) */
	private boolean beacon_activation_flag;

	/** Pixel service flag (3.6.6.6 in PRL) */
	private boolean pixel_service_flag;

	/** Create a new DMS query configuration object */
	public OpQueryDMSConfiguration(DMSImpl d) {
		super(PriorityLevel.CONFIGURE, d);
		color_scheme.setEnum(ColorScheme.MONOCHROME_1_BIT);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryDmsInfo();
	}

	/** Phase to query the DMS information */
	protected class QueryDmsInfo extends Phase {

		/** Query the DMS information */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(access);
			mess.add(type);
			mess.add(face_height);
			mess.add(face_width);
			mess.add(h_border);
			mess.add(v_border);
			mess.add(legend);
			mess.add(beaconType);
			mess.add(tech);
			mess.queryProps();
			logQuery(access);
			logQuery(type);
			logQuery(face_height);
			logQuery(face_width);
			logQuery(h_border);
			logQuery(v_border);
			logQuery(legend);
			logQuery(beaconType);
			logQuery(tech);
			return new QueryVmsInfo();
		}
	}

	/** Phase to query the VMS information */
	protected class QueryVmsInfo extends Phase {

		/** Query the VMS information */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(s_height);
			mess.add(s_width);
			mess.add(h_pitch);
			mess.add(v_pitch);
			mess.add(c_height);
			mess.add(c_width);
			mess.queryProps();
			logQuery(s_height);
			logQuery(s_width);
			logQuery(h_pitch);
			logQuery(v_pitch);
			logQuery(c_height);
			logQuery(c_width);
			return new QuerySupportsBeacon();
		}
	}

	/** Phase to check beacon activation flag */
	private class QuerySupportsBeacon extends Phase {

		/** Query dmsMessageBeacon object */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			// Verify beacon activation flag support by reading
			// dmsMessageBeacon from the changeable.1 message slot
			ASN1Integer beacon = dmsMessageBeacon.makeInt(
				DmsMessageMemoryType.changeable, 1);
			mess.add(beacon);
			try {
				mess.queryProps();
				beacon_activation_flag = true;
			}
			catch (ControllerException e) {
				// NoSuchName / GenError
				beacon_activation_flag = false;
			}
			return new QuerySupportsPixelService();
		}
	}

	/** Phase to check pixel service flag */
	private class QuerySupportsPixelService extends Phase {

		/** Query dmsMessagePixelService object */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			// Verify pixel service flag by reading
			// dmsMessagePixelService from the
			// changeable.1 message slot.
			ASN1Integer srv = dmsMessagePixelService.makeInt(
				DmsMessageMemoryType.changeable, 1);
			mess.add(srv);
			try {
				mess.queryProps();
				pixel_service_flag = true;
			}
			catch (ControllerException e) {
				// NoSuchName / GenError
				pixel_service_flag = false;
			}
			return new QueryV2();
		}
	}

	/** Phase to query the 1203v2 objects */
	protected class QueryV2 extends Phase {

		/** Query the 1203v2 objects */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(mono_color);
			mess.add(color_scheme);
			mess.add(supported_tags);
			mess.add(max_pages);
			mess.add(max_multi_len);
			try {
				mess.queryProps();
				logQuery(mono_color);
				logQuery(color_scheme);
				logQuery(supported_tags);
				logQuery(max_pages);
				logQuery(max_multi_len);
			}
			catch (ControllerException e) {
				// NoSuchName / GenError
				// Sign supports 1203v1 only
				return null;
			}
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		dms.setConfigure(isSuccess());
		if (isSuccess()) {
			int dt = type.getValueEnum().ordinal();
			boolean p = type.isPortable();
			int mono_fg = mono_color.getForegroundInt();
			int mono_bg = mono_color.getBackgroundInt();
			SignDetailImpl sd = SignDetailImpl.findOrCreate(dt, p,
				tech.getValue(), access.getValue(),
				legend.getValue(), beaconType.getValue(),
				getHardwareMake(), getHardwareModel(),
				getSoftwareMake(), getSoftwareModel(),
				supported_tags.getInteger(),
				max_pages.getInteger(),
				max_multi_len.getInteger(),
				beacon_activation_flag, pixel_service_flag);
			if (sd != null)
				dms.setSignDetailNotify(sd);
			SignConfigImpl sc = SignConfigImpl.findOrCreate(
				face_width.getInteger(),
				face_height.getInteger(),
				h_border.getInteger(), v_border.getInteger(),
				h_pitch.getInteger(), v_pitch.getInteger(),
				s_width.getInteger(), s_height.getInteger(),
				c_width.getInteger(), getCharHeight(),
				mono_fg, mono_bg, color_scheme.getInteger());
			if (sc != null)
				dms.setSignConfigNotify(sc);
			else {
				putCtrlFaults("other", "Config: " +
					s_width.getInteger() + "x" +
					s_height.getInteger()
				);
			}
		} else
			putCtrlFaults("other", "Query Configuration");
		super.cleanup();
	}

	/** Get character height */
	private int getCharHeight() {
		// NOTE: some crazy vendors think line-matrix signs should have
		//       a variable character height, so we have to fix their
		//       mistake here ... uggh
		int h = c_height.getInteger();
		if (h == 0 && DMSType.isFixedHeight(type.getValueEnum()))
			return estimateLineHeight();
		else
			return h;
	}

	/** Estimate the line height (pixels) */
	private int estimateLineHeight() {
		int h = s_height.getInteger();
		for (int i = MAX_LINES; i > 0; i--) {
			if (0 == h % i)
				return h / i;
		}
		return 0;
	}
}
