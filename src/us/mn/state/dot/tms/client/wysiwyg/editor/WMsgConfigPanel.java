/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.editor;

import static us.mn.state.dot.tms.units.Distance.Units.INCHES;
import static us.mn.state.dot.tms.units.Distance.Units.MILLIMETERS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiConfig;

/**
 * WYSIWYG DMS Message Editor Config tab with information about the sign
 * configuration corresponding to the current sign.
 *
 * @author Gordon Parikh  - SRF Consulting
 */
@SuppressWarnings("serial")


public class WMsgConfigPanel extends IPanel {
	/** Controller for updating renderings */
	private WController controller;

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
	
	/** Error and Warning Lists */
	private JList<String> errorList;
	private DefaultListModel<String> errorListModel =
			new DefaultListModel<String>();
	private JList<String> warningList;
	private DefaultListModel<String> warningListModel =
			new DefaultListModel<String>();
	
	/** Sign face width label */
	private JLabel f_width_lbl = createValueLabel();

	/** Sign face height label */
	private JLabel f_height_lbl = createValueLabel();

	/** Horizontal border label */
	private JLabel h_border_lbl = createValueLabel();

	/** Vertical border label */
	private JLabel v_border_lbl = createValueLabel();

	/** Horizontal pitch label */
	private JLabel h_pitch_lbl = createValueLabel();

	/** Vertical pitch label */
	private JLabel v_pitch_lbl = createValueLabel();

	/** Sign width (pixels) label */
	private JLabel p_width_lbl = createValueLabel();

	/** Sign height (pixels) label */
	private JLabel p_height_lbl = createValueLabel();

	/** Character width label */
	private JLabel c_width_lbl = createValueLabel();

	/** Character height label */
	private JLabel c_height_lbl = createValueLabel();

	/** Monochrome foreground label */
	private JLabel m_foreground_lbl = createValueLabel();

	/** Monochrome background label */
	private JLabel m_background_lbl = createValueLabel();

	/** Color scheme label */
	private JLabel c_scheme_lbl = createValueLabel();
	
	/** Default Font label */
	private JLabel font_lbl = createValueLabel();

	/** Font height label */
	private JLabel font_height_lbl = IPanel.createValueLabel();

	/** User session */
//	private Session session;
	
	/** Sign Group MultiConfig */
	private MultiConfig signGroupMultiConfig;
	
	/** Sign Group MultiConfig errors and warnings */
	private ArrayList<String> sgmcErrors = new ArrayList<String>();
	private ArrayList<String> sgmcWarnings = new ArrayList<String>();
	
	/** "Active" MultiConfig */
	private MultiConfig multiConfig;

	/** Active MultiConfig errors and warnings */
	private ArrayList<String> mcErrors = new ArrayList<String>();
	private ArrayList<String> mcWarnings = new ArrayList<String>();
	
	
	/** Create a new MULTI-mode panel */
	public WMsgConfigPanel(WController c) {
		controller = c;
		
		// get MultiConfig(s)
		signGroupMultiConfig = controller.getSignGroupMultiConfig();
		multiConfig = controller.getMultiConfig();
		
		// check for errors 
		updateErrorsWarnings();
		
		if (!sgmcErrors.isEmpty() || !mcErrors.isEmpty()) {
			// warning and error panel only
			initErrorsWarnings();
		} else if (!sgmcWarnings.isEmpty() || !mcWarnings.isEmpty()) {
			// warning and config panel only
			initWarningsConfig();
		} else {
			// config panel only
			initNormalConfig();
		}
		
	}
	
	public boolean hasErrors() {
		return !sgmcErrors.isEmpty() || !mcErrors.isEmpty();
	}

	public boolean hasWarnings() {
		return !sgmcWarnings.isEmpty() || !mcWarnings.isEmpty();
	}
	
	public void setActiveMultiConfig(MultiConfig mc) {
		multiConfig = mc;
		updateErrorsWarnings();
	}
	
	/** Initialize a panel that shows errors and warnings */
	public void initErrorsWarnings() {
		setLayout(new BorderLayout());
		initialize();
		GridBagLayout gbl = new GridBagLayout();
		JPanel p = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 10;
		gbc.ipady = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		// create lists in scroll panes for displaying them
		errorList = new JList<String>(errorListModel);
		JScrollPane errorPane = new JScrollPane(errorList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		warningList = new JList<String>(warningListModel);
		JScrollPane warningPane = new JScrollPane(warningList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		// add error list
		gbc.gridx = 0;
		gbc.gridy = 0;
		p.add(errorPane, gbc);
		
		// warning list beneath it
		gbc.gridy = 1;
		p.add(warningPane, gbc);
		add(p, BorderLayout.CENTER);
	}
	
	/** Initialize a panel that shows warnings and config information */
	public void initWarningsConfig() {
		setLayout(new BorderLayout());
		initialize();
		GridBagLayout gbl = new GridBagLayout();
		JPanel p = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 10;
		gbc.ipady = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		warningList = new JList<String>(warningListModel);
		JScrollPane warningPane = new JScrollPane(warningList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		p.add(warningPane, gbc);
		
		// and the config panel
		IPanel configPanel = getConfigPanel();
		
		// to the right
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		p.add(configPanel, gbc);
		add(p, BorderLayout.CENTER);
		updateForm();
	}
	
	private static ArrayList<String> getErrors(MultiConfig mc) {
		ArrayList<String> errors = new ArrayList<String>();
		if (mc != null) {
			for (String e: mc.getErrors())
				errors.add(e);
		}
		return errors;
	}

	private static ArrayList<String> getWarnings(MultiConfig mc) {
		ArrayList<String> warnings = new ArrayList<String>();
		if (mc != null) {
			for (String e: mc.getWarnings())
				warnings.add(e);
		}
		return warnings;
	}
	
	private void updateErrorsWarnings() {
		// check for errors 
		errorListModel.clear();
		warningListModel.clear();
		
		// add errors from the sign group config first
		if (signGroupMultiConfig != null) {
			sgmcErrors = getErrors(signGroupMultiConfig);
			sgmcWarnings = getWarnings(signGroupMultiConfig);
			
			if (!sgmcErrors.isEmpty()) {
				for (String e: sgmcErrors) {
					String s = String.format("%s: %s",
							signGroupMultiConfig.getName(), e);
					errorListModel.addElement(s);
				}
			}
			if (!sgmcWarnings.isEmpty()) {
				for (String w: sgmcWarnings) {
					String s = String.format("%s: %s",
							signGroupMultiConfig.getName(), w);
					warningListModel.addElement(s);
				}
			}
		}
		
		// then the "active" config
		if (multiConfig != null) {
			mcErrors = getErrors(multiConfig);
			mcWarnings = getWarnings(multiConfig);
			
			if (!mcErrors.isEmpty()) {
				for (String e: mcErrors) {
					String s = String.format("%s: %s",
							multiConfig.getName(), e);
					errorListModel.addElement(s);
				}
			}
			if (!mcWarnings.isEmpty()) {
				for (String w: mcWarnings) {
					String s = String.format("%s: %s",
							multiConfig.getName(), w);
					warningListModel.addElement(s);
				}
			}
		}
	}
	
	/** Initialize a panel that just shows config information */
	public void initNormalConfig() {
		setLayout(new BorderLayout());
		initialize();
		
		// and the config panel
		IPanel configPanel = getConfigPanel();
		add(configPanel, BorderLayout.CENTER);
		updateForm();
	}

	private IPanel getConfigPanel() {
		IPanel configPanel = new IPanel();
		configPanel.add("dms.face.width");
		configPanel.add(f_width_lbl, Stretch.LAST);
		configPanel.add("dms.face.height");
		configPanel.add(f_height_lbl, Stretch.LAST);
		configPanel.add("dms.border.horiz");
		configPanel.add(h_border_lbl, Stretch.LAST);
		configPanel.add("dms.border.vert");
		configPanel.add(v_border_lbl, Stretch.LAST);
		configPanel.add("dms.pitch.horiz");
		configPanel.add(h_pitch_lbl, Stretch.LAST);
		configPanel.add("dms.pitch.vert");
		configPanel.add(v_pitch_lbl, Stretch.LAST);
		configPanel.add("dms.pixel.width");
		configPanel.add(p_width_lbl, Stretch.LAST);
		configPanel.add("dms.pixel.height");
		configPanel.add(p_height_lbl, Stretch.LAST);
		configPanel.add("dms.char.width");
		configPanel.add(c_width_lbl, Stretch.LAST);
		configPanel.add("dms.char.height");
		configPanel.add(c_height_lbl, Stretch.LAST);
		configPanel.add("dms.monochrome.foreground");
		configPanel.add(m_foreground_lbl, Stretch.LAST);
		configPanel.add("dms.monochrome.background");
		configPanel.add(m_background_lbl, Stretch.LAST);
		configPanel.add("dms.color.scheme");
		configPanel.add(c_scheme_lbl, Stretch.LAST);
		configPanel.add("dms.font.default");
		configPanel.add(font_lbl, Stretch.LAST);
		configPanel.add("dms.font.height");
		configPanel.add(font_height_lbl, Stretch.LAST);
		return configPanel;
	}
	
	/** Update labels on the form tab */
	public void updateForm() {
		multiConfig = controller.getMultiConfig();
		if (multiConfig != null && multiConfig.isUseable()) {
			MultiConfig sc = multiConfig;
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
				sc.getDefaultFG().rgb(), 6));
			m_foreground_lbl.setIcon(new ColorIcon(
				sc.getDefaultFG().rgb()));
			m_background_lbl.setText(HexString.format(
				sc.getDefaultBG().rgb(), 6));
			m_background_lbl.setIcon(new ColorIcon(
				sc.getDefaultBG().rgb()));
			c_scheme_lbl.setText(sc.getColorScheme().description);
			font_lbl.setText(sc.getDefaultFont().getName());
			font_height_lbl.setText(calculateFontHeight());
		} else if (multiConfig != null && !multiConfig.isUseable()) {
			System.out.println("Bad config");
		}
	}

	/** Calculate the height of the default font on the sign */
	private String calculateFontHeight() {
		if (multiConfig != null) {
			MultiConfig sc = multiConfig;
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
		}
		return UNKNOWN;
	}

	/** Format the font height for display */
	private static String formatFontHeight(Distance fh) {
		Distance.Formatter df = new Distance.Formatter(1);
		return df.format(fh.convert(distUnitsTiny()));
	}
	
}