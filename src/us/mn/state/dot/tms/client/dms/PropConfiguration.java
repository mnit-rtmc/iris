/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2022  Minnesota Department of Transportation
 * Copyright (C) 2021  Iteris Inc.
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
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.HexString;
import static us.mn.state.dot.tms.units.Distance.Units.INCHES;
import static us.mn.state.dot.tms.units.Distance.Units.MILLIMETERS;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.SString;

/**
 * PropConfiguration is a GUI panel for displaying sign configuration on a
 * form.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class PropConfiguration extends IPanel {

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
	static private String formatMM(int i) {
		return (i > 0) ? i + " " + I18N.get("units.mm") : UNKNOWN;
	}

	/** Format pixel units for display */
	static private String formatPixels(int i) {
		if (i > 0)
			return i + " " + I18N.get("units.pixels");
		else if (0 == i)
			return I18N.get("units.pixels.variable");
		else
			return UNKNOWN;
	}

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

	/** Monochrome foreground label */
	private final JLabel m_foreground_lbl = createValueLabel();

	/** Monochrome background label */
	private final JLabel m_background_lbl = createValueLabel();

	/** Color scheme label */
	private final JLabel c_scheme_lbl = createValueLabel();

	/** Default font combo box */
	private final JComboBox<Font> font_cbx = new JComboBox<Font>();

	/** Font height label */
	private final JLabel font_height_lbl = IPanel.createValueLabel();

	/** Module width edit field */
	private final JTextField module_width_txt = new JTextField(4);

	/** Module height edit field */
	private final JTextField module_height_txt = new JTextField(4);

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
		add("dms.monochrome.foreground");
		add(m_foreground_lbl, Stretch.LAST);
		add("dms.monochrome.background");
		add(m_background_lbl, Stretch.LAST);
		add("dms.color.scheme");
		add(c_scheme_lbl, Stretch.LAST);
		add("dms.font.default");
		add(font_cbx, Stretch.LAST);
		add("dms.font.height");
		add(font_height_lbl, Stretch.LAST);
		add("dms.module.width");
		add(module_width_txt, Stretch.LAST);
		add("dms.module.height");
		add(module_height_txt, Stretch.LAST);
		font_cbx.setAction(new IAction("font") {
			protected void doActionPerformed(ActionEvent e) {
				config.setDefaultFont(
					(Font) font_cbx.getSelectedItem());
			}
		});
		font_cbx.setModel(new IComboBoxModel<Font>(
			session.getSonarState().getDmsCache().getFontModel()));
		module_width_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				config.setModuleWidth(SString.stringToInt(
					module_width_txt.getText()));
			}
		});
		module_height_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				config.setModuleHeight(SString.stringToInt(
					module_height_txt.getText()));
			}
		});
		updateAttribute(null);
	}

	/** Update the edit mode */
	public void updateEditMode() {
		font_cbx.setEnabled(canWrite("defaultFont"));
		module_width_txt.setEnabled(canWrite("moduleWidth"));
		module_height_txt.setEnabled(canWrite("moduleHeight"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		SignConfig sc = config;
		if (null == a) {
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
			m_foreground_lbl.setText(HexString.format(
				sc.getMonochromeForeground(), 6));
			m_foreground_lbl.setIcon(new ColorIcon(
				sc.getMonochromeForeground()));
			m_background_lbl.setText(HexString.format(
				sc.getMonochromeBackground(), 6));
			m_background_lbl.setIcon(new ColorIcon(
				sc.getMonochromeBackground()));
			ColorScheme cs = ColorScheme.fromOrdinal(
				sc.getColorScheme());
			c_scheme_lbl.setText(cs.description);
		}
		if (null == a || a.equals("defaultFont")) {
			font_cbx.setSelectedItem(sc.getDefaultFont());
			font_height_lbl.setText(calculateFontHeight());
		}
		if (null == a || a.equals("moduleHeight")) {
			module_height_txt.setText(
				SString.intToString(sc.getModuleHeight()));
		}
		if (null == a || a.equals("moduleWidth")) {
			module_width_txt.setText(
				SString.intToString(sc.getModuleWidth()));
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

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		return session.canWrite(config, aname);
	}
}
