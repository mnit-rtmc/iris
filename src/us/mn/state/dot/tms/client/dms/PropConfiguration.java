/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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

import javax.swing.JButton;
import javax.swing.JLabel;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PropConfiguration is a GUI panel for displaying configuration data on a DMS
 * properties form.
 *
 * @author Douglas Lau
 */
public class PropConfiguration extends FormPanel {

	/** Unknown value string */
	static private final String UNKNOWN = "???";

	/** Format a string field */
	static private String formatString(String s) {
		if(s != null && s.length() > 0)
			return s;
		else
			return UNKNOWN;
	}

	/** Format millimeter units for display */
	static private String formatMM(Integer i) {
		if(i != null && i > 0)
			return i + " " + I18N.get("units.mm");
		else
			return UNKNOWN;
	}

	/** Format pixel units for display */
	static private String formatPixels(Integer i) {
		if(i != null) {
			if(i > 0)
				return i + " " + I18N.get("units.pixels");
			else if(i == 0)
				return I18N.get("units.pixels.variable");
		}
		return UNKNOWN;
	}

	/** Sign type label */
	private final JLabel type_lbl = createValueLabel();

	/** Sign technology label */
	private final JLabel tech_lbl = createValueLabel();

	/** Sign access label */
	private final JLabel access_lbl = createValueLabel();

	/** Sign legend label */
	private final JLabel legend_lbl = createValueLabel();

	/** Beacon label */
	private final JLabel beacon_lbl = createValueLabel();

	/** Sign face width label */
	private final JLabel f_width_lbl = createValueLabel();

	/** Sign face height label */
	private final JLabel f_height_lbl = createValueLabel();

	/** Horizontal border label */
	private final JLabel h_border_lbl = createValueLabel();

	/** Vertical border label */
	private final JLabel v_border_lbl = createValueLabel();

	/** Horizontal pitch label */
	private final JLabel h_pitch_lbl = createValueLabel();

	/** Vertical pitch label */
	private final JLabel v_pitch_lbl = createValueLabel();

	/** Sign width (pixels) label */
	private final JLabel p_width_lbl = createValueLabel();

	/** Sign height (pixels) label */
	private final JLabel p_height_lbl = createValueLabel();

	/** Character width label */
	private final JLabel c_width_lbl = createValueLabel();

	/** Character height label */
	private final JLabel c_height_lbl = createValueLabel();

	/** Button to query configuration */
	private final IAction config = new IAction("dms.query.config") {
		@Override protected void do_perform() {
			dms.setDeviceRequest(DeviceRequest.
				QUERY_CONFIGURATION.ordinal());
		}
	};

	/** User session */
	private final Session session;

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties configuration panel */
	public PropConfiguration(Session s, DMS sign) {
		super(true);
		session = s;
		dms = sign;
	}

	/** Initialize the widgets on the form */
	public void initialize() {
		addRow(I18N.get("dms.type"), type_lbl);
		addRow(I18N.get("dms.technology"), tech_lbl);
		addRow(I18N.get("dms.access"), access_lbl);
		addRow(I18N.get("dms.legend"), legend_lbl);
		addRow(I18N.get("dms.beacon"), beacon_lbl);
		addRow(I18N.get("dms.face.width"), f_width_lbl);
		addRow(I18N.get("dms.face.height"), f_height_lbl);
		addRow(I18N.get("dms.border.horiz"), h_border_lbl);
		addRow(I18N.get("dms.border.vert"), v_border_lbl);
		addRow(I18N.get("dms.pitch.horiz"), h_pitch_lbl);
		addRow(I18N.get("dms.pitch.vert"), v_pitch_lbl);
		addRow(I18N.get("dms.pixel.width"), p_width_lbl);
		addRow(I18N.get("dms.pixel.height"), p_height_lbl);
		addRow(I18N.get("dms.char.width"), c_width_lbl);
		addRow(I18N.get("dms.char.height"), c_height_lbl);
		addRow(new JButton(config));
		updateAttribute(null);
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if(a == null || a.equals("dmsType")) {
			DMSType t = DMSType.fromOrdinal(dms.getDmsType());
			type_lbl.setText(t.description);
		}
		if(a == null || a.equals("technology"))
			tech_lbl.setText(formatString(dms.getTechnology()));
		if(a == null || a.equals("signAccess"))
			access_lbl.setText(formatString(dms.getSignAccess()));
		if(a == null || a.equals("legend"))
			legend_lbl.setText(formatString(dms.getLegend()));
		if(a == null || a.equals("beaconType"))
			beacon_lbl.setText(formatString(dms.getBeaconType()));
		if(a == null || a.equals("faceWidth"))
			f_width_lbl.setText(formatMM(dms.getFaceWidth()));
		if(a == null || a.equals("faceHeight"))
			f_height_lbl.setText(formatMM(dms.getFaceHeight()));
		if(a == null || a.equals("horizontalBorder")) {
			h_border_lbl.setText(formatMM(
				dms.getHorizontalBorder()));
		}
		if(a == null || a.equals("verticalBorder"))
			v_border_lbl.setText(formatMM(dms.getVerticalBorder()));
		if(a == null || a.equals("horizontalPitch"))
			h_pitch_lbl.setText(formatMM(dms.getHorizontalPitch()));
		if(a == null || a.equals("verticalPitch"))
			v_pitch_lbl.setText(formatMM(dms.getVerticalPitch()));
		if(a == null || a.equals("widthPixels"))
			p_width_lbl.setText(formatPixels(dms.getWidthPixels()));
		if(a == null || a.equals("heightPixels")) {
			p_height_lbl.setText(formatPixels(
				dms.getHeightPixels()));
		}
		if(a == null || a.equals("charWidthPixels")) {
			c_width_lbl.setText(formatPixels(
				dms.getCharWidthPixels()));
		}
		if(a == null || a.equals("charHeightPixels")) {
			c_height_lbl.setText(formatPixels(
				dms.getCharHeightPixels()));
		}
		if(a == null)
			config.setEnabled(canRequest());
	}

	/** Check if the user can update an attribute */
	private boolean canUpdate(String aname) {
		return session.canUpdate(dms, aname);
	}

	/** Check if the user can make device requests */
	private boolean canRequest() {
		return canUpdate("deviceRequest");
	}
}
