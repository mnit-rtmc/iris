/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2019  Minnesota Department of Transportation
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

import java.awt.Component;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.utils.HexString;

/**
 * PropDetail is a GUI panel for displaying sign detail on a form.
 *
 * @author Douglas Lau
 */
public class PropDetail extends IPanel {

	/** Icon size */
	static private final int ICON_SIZE = 24;

	/** Icon for colors */
	static private class ColorIcon implements Icon {
		private final Color color;
		private ColorIcon(int rgb) {
			color = new Color(rgb);
		}
		public int getIconHeight() {
			return ICON_SIZE;
		}
		public int getIconWidth() {
			return ICON_SIZE;
		}
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color);
			g.fillRect(x, y, ICON_SIZE, ICON_SIZE);
		}
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

	/** Monochrome foreground label */
	private final JLabel m_foreground_lbl = createValueLabel();

	/** Monochrome background label */
	private final JLabel m_background_lbl = createValueLabel();

	/** Software make label */
	private final JLabel software_make_lbl = createValueLabel();

	/** Software model label */
	private final JLabel software_model_lbl = createValueLabel();

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
		add("dms.technology");
		add(tech_lbl, Stretch.LAST);
		add("dms.access");
		add(access_lbl, Stretch.LAST);
		add("dms.legend");
		add(legend_lbl, Stretch.LAST);
		add("dms.beacon");
		add(beacon_lbl, Stretch.LAST);
		add("dms.monochrome.foreground");
		add(m_foreground_lbl, Stretch.LAST);
		add("dms.monochrome.background");
		add(m_background_lbl, Stretch.LAST);
		add("dms.software.make");
		add(software_make_lbl, Stretch.LAST);
		add("dms.software.model");
		add(software_model_lbl, Stretch.LAST);
		updateAttribute(null);
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		SignDetail sd = detail;
		if (null == a) {
			DMSType t = DMSType.fromOrdinal(sd.getDmsType());
			type_lbl.setText(t.description);
			tech_lbl.setText(formatString(sd.getTechnology()));
			access_lbl.setText(formatString(sd.getSignAccess()));
			legend_lbl.setText(formatString(sd.getLegend()));
			beacon_lbl.setText(formatString(sd.getBeaconType()));
			m_foreground_lbl.setText(HexString.format(
				sd.getMonochromeForeground(), 6));
			m_foreground_lbl.setIcon(new ColorIcon(
				sd.getMonochromeForeground()));
			m_background_lbl.setText(HexString.format(
				sd.getMonochromeBackground(), 6));
			m_background_lbl.setIcon(new ColorIcon(
				sd.getMonochromeBackground()));
			software_make_lbl.setText(formatString(
				sd.getSoftwareMake()));
			software_model_lbl.setText(formatString(
				sd.getSoftwareModel()));
		}
	}
}
