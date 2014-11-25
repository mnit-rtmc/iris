/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PropConfiguration is a GUI panel for displaying configuration data on a DMS
 * properties form.
 *
 * @author Douglas Lau
 */
public class PropConfiguration extends IPanel {

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
		protected void doActionPerformed(ActionEvent e) {
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
		session = s;
		dms = sign;
	}

	/** Initialize the widgets on the form */
	@Override
	public void initialize() {
		super.initialize();
		add("dms.type");
		add(type_lbl, Stretch.LAST);
		add("dms.technology");
		add(tech_lbl, Stretch.LAST);
		add("dms.access");
		add(access_lbl, Stretch.LAST);
		add("dms.legend");
		add(legend_lbl, Stretch.LAST);
		add("dms.beacon");
		add(beacon_lbl, Stretch.LAST);
		add("dms.face.width");
		add(f_width_lbl, Stretch.LAST);
		add("dms.face.height");
		add(f_height_lbl, Stretch.LAST);
		add("dms.border.horiz");
		add(h_border_lbl, Stretch.LAST);
		add("dms.border.vert");
		add(v_border_lbl, Stretch.LAST);
		add("dms.pitch.horiz");
		add(h_pitch_lbl, Stretch.LAST);
		add("dms.pitch.vert");
		add(v_pitch_lbl, Stretch.LAST);
		add("dms.pixel.width");
		add(p_width_lbl, Stretch.LAST);
		add("dms.pixel.height");
		add(p_height_lbl, Stretch.LAST);
		add("dms.char.width");
		add(c_width_lbl, Stretch.LAST);
		add("dms.char.height");
		add(c_height_lbl, Stretch.LAST);
		add(new JButton(config), Stretch.RIGHT);
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

	/** Check if the user is permitted to update an attribute */
	private boolean isUpdatePermitted(String aname) {
		return session.isUpdatePermitted(dms, aname);
	}

	/** Check if the user can make device requests */
	private boolean canRequest() {
		return isUpdatePermitted("deviceRequest");
	}
}
