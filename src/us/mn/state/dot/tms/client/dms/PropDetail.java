/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import javax.swing.JLabel;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.utils.MultiTag;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IPanel;

/**
 * PropDetail is a GUI panel for displaying sign detail on a form.
 *
 * @author Douglas Lau
 */
public class PropDetail extends IPanel {

	/** Unknown value string */
	static private final String UNKNOWN = "???";

	/** Format a string field */
	static private String formatString(String s) {
		if (s != null && s.length() > 0)
			return s;
		else
			return UNKNOWN;
	}

	/** Format a boolean field */
	static private String formatBool(boolean v) {
		return v ? "Yes" : "No";
	}

	/** Sign type label */
	private final JLabel type_lbl = createValueLabel();

	/** Portable label */
	private final JLabel portable_lbl = createValueLabel();

	/** Sign technology label */
	private final JLabel tech_lbl = createValueLabel();

	/** Sign access label */
	private final JLabel access_lbl = createValueLabel();

	/** Sign legend label */
	private final JLabel legend_lbl = createValueLabel();

	/** Beacon label */
	private final JLabel beacon_lbl = createValueLabel();

	/** Hardware make label */
	private final JLabel hardware_make_lbl = createValueLabel();

	/** Hardware model label */
	private final JLabel hardware_model_lbl = createValueLabel();

	/** Software make label */
	private final JLabel software_make_lbl = createValueLabel();

	/** Software model label */
	private final JLabel software_model_lbl = createValueLabel();

	/** Supported tags label */
	private final JLabel supported_tags_lbl = createValueLabel();

	/** Maximum pages label */
	private final JLabel max_pages_lbl = createValueLabel();

	/** Maximum MULTI string length label */
	private final JLabel max_multi_len_lbl = createValueLabel();

	/** Beacon activation flag label */
	private final JLabel beacon_activation_flag_lbl = createValueLabel();

	/** Pixel service flag label */
	private final JLabel pixel_service_flag_lbl = createValueLabel();

	/** User session */
	private final Session session;

	/** Sing detail */
	private final SignDetail detail;

	/** Create a new sign detail panel */
	public PropDetail(Session s, SignDetail sd) {
		session = s;
		detail = sd;
	}

	/** Initialize the widgets on the form */
	@Override
	public void initialize() {
		super.initialize();
		add("dms.type");
		add(type_lbl, Stretch.LAST);
		add("dms.portable");
		add(portable_lbl, Stretch.LAST);
		add("dms.technology");
		add(tech_lbl, Stretch.LAST);
		add("dms.access");
		add(access_lbl, Stretch.LAST);
		add("dms.legend");
		add(legend_lbl, Stretch.LAST);
		add("dms.beacon");
		add(beacon_lbl, Stretch.LAST);
		add("dms.hardware.make");
		add(hardware_make_lbl, Stretch.LAST);
		add("dms.hardware.model");
		add(hardware_model_lbl, Stretch.LAST);
		add("dms.software.make");
		add(software_make_lbl, Stretch.LAST);
		add("dms.software.model");
		add(software_model_lbl, Stretch.LAST);
		add("dms.supported.tags");
		add(supported_tags_lbl, Stretch.LAST);
		add("dms.max.pages");
		add(max_pages_lbl, Stretch.LAST);
		add("dms.max.multi.len");
		add(max_multi_len_lbl, Stretch.LAST);
		add("dms.beacon.activation.flag");
		add(beacon_activation_flag_lbl, Stretch.LAST);
		add("dms.pixel.service.flag");
		add(pixel_service_flag_lbl, Stretch.LAST);
		updateAttribute(null);
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		SignDetail sd = detail;
		if (null == a) {
			DMSType t = DMSType.fromOrdinal(sd.getDmsType());
			type_lbl.setText(t.description);
			portable_lbl.setText(formatBool(sd.getPortable()));
			tech_lbl.setText(formatString(sd.getTechnology()));
			access_lbl.setText(formatString(sd.getSignAccess()));
			legend_lbl.setText(formatString(sd.getLegend()));
			beacon_lbl.setText(formatString(sd.getBeaconType()));
			hardware_make_lbl.setText(formatString(
				sd.getHardwareMake()));
			hardware_model_lbl.setText(formatString(
				sd.getHardwareModel()));
			software_make_lbl.setText(formatString(
				sd.getSoftwareMake()));
			software_model_lbl.setText(formatString(
				sd.getSoftwareModel()));
			supported_tags_lbl.setText(MultiTag.asString(
				sd.getSupportedTags()));
			max_pages_lbl.setText(Integer.toString(
				sd.getMaxPages()));
			max_multi_len_lbl.setText(Integer.toString(
				sd.getMaxMultiLen()));
			beacon_activation_flag_lbl.setText(formatBool(
				sd.getBeaconActivationFlag()));
			pixel_service_flag_lbl.setText(formatBool(
				sd.getPixelServiceFlag()));
		}
	}
}
