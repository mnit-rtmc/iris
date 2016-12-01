/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.INCHES;
import static us.mn.state.dot.tms.units.Distance.Units.MILLIMETERS;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PropConfiguration is a GUI panel for displaying sign configuration on a
 * form.
 *
 * @author Douglas Lau
 */
public class PropConfiguration extends IPanel {

	/** Get tiny distance units to use for display */
	static private Distance.Units distUnitsTiny() {
		return SystemAttrEnum.CLIENT_UNITS_SI.getBoolean()
		     ? Distance.Units.CENTIMETERS : INCHES;
	}

	/** Unknown value string */
	static private final String UNKNOWN = "???";

	/** Format a string field */
	static private String formatString(String s) {
		if (s != null && s.length() > 0)
			return s;
		else
			return UNKNOWN;
	}

	/** Format millimeter units for display */
	static private String formatMM(Integer i) {
		if (i != null && i > 0)
			return i + " " + I18N.get("units.mm");
		else
			return UNKNOWN;
	}

	/** Format pixel units for display */
	static private String formatPixels(Integer i) {
		if (i != null) {
			if (i > 0)
				return i + " " + I18N.get("units.pixels");
			else if (0 == i)
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

	/** Default font combo box */
	private final JComboBox<Font> font_cbx = new JComboBox<Font>();

	/** Font height label */
	private final JLabel font_height_lbl = IPanel.createValueLabel();

	/** User session */
	private final Session session;

	/** Sing configuration */
	private final SignConfig config;

	/** Create a new sign configuration panel */
	public PropConfiguration(Session s, SignConfig sc) {
		session = s;
		config = sc;
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
		add("dms.font.default");
		add(font_cbx, Stretch.LAST);
		add("dms.font.height");
		add(font_height_lbl, Stretch.LAST);
		font_cbx.setAction(new IAction("font") {
			protected void doActionPerformed(ActionEvent e) {
				config.setDefaultFont(
					(Font) font_cbx.getSelectedItem());
			}
		});
		font_cbx.setModel(new IComboBoxModel<Font>(
			session.getSonarState().getDmsCache().getFontModel()));
		updateAttribute(null);
	}

	/** Update the edit mode */
	public void updateEditMode() {
		font_cbx.setEnabled(canUpdate("defaultFont"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		SignConfig sc = config;
		if (null == a) {
			DMSType t = DMSType.fromOrdinal(sc.getDmsType());
			type_lbl.setText(t.description);
			tech_lbl.setText(formatString(sc.getTechnology()));
			access_lbl.setText(formatString(sc.getSignAccess()));
			legend_lbl.setText(formatString(sc.getLegend()));
			beacon_lbl.setText(formatString(sc.getBeaconType()));
			f_width_lbl.setText(formatMM(sc.getFaceWidth()));
			f_height_lbl.setText(formatMM(sc.getFaceHeight()));
			h_border_lbl.setText(formatMM(sc.getBorderHoriz()));
			v_border_lbl.setText(formatMM(sc.getBorderVert()));
			h_pitch_lbl.setText(formatMM(sc.getPitchHoriz()));
			v_pitch_lbl.setText(formatMM(sc.getPitchVert()));
			p_width_lbl.setText(formatPixels(sc.getPixelWidth()));
			p_height_lbl.setText(formatPixels(sc.getPixelHeight()));
			c_width_lbl.setText(formatPixels(sc.getCharWidth()));
			c_height_lbl.setText(formatPixels(sc.getCharHeight()));
		}
		if (null == a || a.equals("defaultFont")) {
			font_cbx.setSelectedItem(sc.getDefaultFont());
			font_height_lbl.setText(calculateFontHeight());
		}
	}

	/** Calculate the height of the default font on the sign */
	private String calculateFontHeight() {
		SignConfig sc = config;
		Font f = sc.getDefaultFont();
		if (f != null) {
			int pv = sc.getPitchVert();
			int h = f.getHeight();
			if (h > 0 && pv > 0) {
				float mm = (h - 0.5f) * pv;
				Distance fh = new Distance(mm, MILLIMETERS);
				return formatFontHeight(fh);
			}
		}
		return UNKNOWN;
	}

	/** Format the font height for display */
	private String formatFontHeight(Distance fh) {
		Distance.Formatter df = new Distance.Formatter(1);
		return df.format(fh.convert(distUnitsTiny()));
	}

	/** Check if the user can update an attribute */
	private boolean canUpdate(String aname) {
		return session.canUpdate(config, aname);
	}
}
