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

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.SystemAttrEnum;
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
	
	/** Sign Group MultiConfig */
	private MultiConfig signGroupMultiConfig;
	
	/** Panel to display when errors and warnings appear */
	private JPanel errorsWarningsPanel;
	
	/** Panel to display when warnings appear */
	private JPanel warningsConfigPanel;
	private IPanel wcp;
	
	/** Panel to display when no warnings or errors appear */
	private JPanel goodConfigPanel;
	private IPanel gcp;
	
	/** Sign Group MultiConfig errors and warnings */
	private ArrayList<String> sgmcErrors = new ArrayList<String>();
	private ArrayList<String> sgmcWarnings = new ArrayList<String>();
	
	/** "Active" MultiConfig */
	private MultiConfig multiConfig;

	/** Active MultiConfig errors and warnings */
	private ArrayList<String> mcErrors = new ArrayList<String>();
	private ArrayList<String> mcWarnings = new ArrayList<String>();
	
	private JLabel errorLabel;
	private JLabel eWarningLabel;
	private JLabel warningLabel;
	
	/** Create a new MULTI-mode panel */
	public WMsgConfigPanel(WController c) {
		controller = c;
		
		// get MultiConfig(s)
		signGroupMultiConfig = controller.getSignGroupMultiConfig();
		multiConfig = controller.getMultiConfig();
		
		// default error/warning labels
		errorLabel = new JLabel(I18N.get("wysiwyg.config.errors"));
		eWarningLabel = new JLabel(I18N.get("wysiwyg.config.warnings"));
		warningLabel = new JLabel(I18N.get("wysiwyg.config.warnings"));
		
		// check for errors
		updateErrorsWarnings();
		
		initialize();
		setLayout(new BorderLayout());
		JPanel p;
		initErrorsWarnings();
		initWarningsConfig();
		initNormalConfig();
		if (!sgmcErrors.isEmpty() || !mcErrors.isEmpty())
			// warning and error panel only
			p = errorsWarningsPanel;
		else if (!sgmcWarnings.isEmpty() || !mcWarnings.isEmpty())
			// warning and config panel only
			p = warningsConfigPanel;
		else
			// config panel only
			p = goodConfigPanel;
		add(p, BorderLayout.CENTER);
	}
	
	public boolean hasErrors() {
		return !sgmcErrors.isEmpty() || !mcErrors.isEmpty();
	}

	public boolean hasWarnings() {
		return !sgmcWarnings.isEmpty() || !mcWarnings.isEmpty();
	}
	
	public void setActiveMultiConfig(MultiConfig mc) {
		multiConfig = mc;
		removeAll();
		updateErrorsWarnings();
		initErrorsWarnings();
		initWarningsConfig();
		initNormalConfig();
		JPanel p;
		if (hasErrors())
			p = errorsWarningsPanel;
		else if (hasWarnings())
			p = warningsConfigPanel;
		else
			p = goodConfigPanel;
		add(p, BorderLayout.CENTER);
	}
	
	/** Initialize a panel that shows errors and warnings */
	public void initErrorsWarnings() {
		GridBagLayout gbl = new GridBagLayout();
		errorsWarningsPanel = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 0;
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
		errorsWarningsPanel.add(errorLabel, gbc);
		gbc.gridy = 1;
		errorsWarningsPanel.add(errorPane, gbc);
		
		// warning list beneath it
		gbc.gridy = 2;
		errorsWarningsPanel.add(eWarningLabel, gbc);
		gbc.gridy = 3;
		errorsWarningsPanel.add(warningPane, gbc);
	}
	
	/** Initialize a panel that shows warnings and config information */
	public void initWarningsConfig() {
		GridBagLayout gbl = new GridBagLayout();
		warningsConfigPanel = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		warningList = new JList<String>(warningListModel);
		JScrollPane warningPane = new JScrollPane(warningList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		warningsConfigPanel.add(warningLabel, gbc);
		gbc.gridy = 1;
		warningsConfigPanel.add(warningPane, gbc);
		
		// to the right
		gbc.gridx = 1;
		gbc.gridy = 0;
		warningsConfigPanel.add(new JLabel(I18N.get("wysiwyg.config")), gbc);
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		wcp = makeConfigPanel(multiConfig);
		if (wcp != null)
			warningsConfigPanel.add(wcp, gbc);
	}
	
	/** Initialize a panel that just shows config information */
	public void initNormalConfig() {
		GridBagLayout gbl = new GridBagLayout();
		goodConfigPanel = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		// and the config panel
		gbc.gridx = 0;
		gbc.gridy = 0;
		goodConfigPanel.add(new JLabel(I18N.get("wysiwyg.config")), gbc);
		gcp = makeConfigPanel(multiConfig);
		gbc.gridy = 1;
		if (gcp != null)
			goodConfigPanel.add(gcp, gbc);
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
		
		// show errors and/or warnings from "active" config
		String name = "MultiConfig";
		if (multiConfig != null) {
			if (multiConfig.getName() != null)
				name = multiConfig.getName();
			
			mcErrors = getErrors(multiConfig);
			mcWarnings = getWarnings(multiConfig);
			
			if (!mcErrors.isEmpty()) {
				for (String e: mcErrors) {
					errorListModel.addElement(e);
				}
			}
			if (!mcWarnings.isEmpty()) {
				for (String w: mcWarnings) {
					warningListModel.addElement(w);
				}
			}
		}
		
		// update the error labels as needed
		warningLabel.setText(name + " --- "
						+ I18N.get("wysiwyg.config.warnings"));
		eWarningLabel.setText(name + " --- "
						+ I18N.get("wysiwyg.config.warnings"));
		errorLabel.setText(name + " --- " 
						+ I18N.get("wysiwyg.config.errors"));
	}
	
	private IPanel makeConfigPanel(MultiConfig sc) {
		IPanel cp = null;
		if (sc != null && sc.isUseable()) {
			cp = new IPanel();
			JLabel f_width_lbl = createValueLabel(
					formatMM(sc.getFaceWidth()));
			JLabel f_height_lbl = createValueLabel(
					formatMM(sc.getFaceHeight()));
			JLabel h_border_lbl = createValueLabel(
					formatMM(sc.getBorderHoriz()));
			JLabel v_border_lbl = createValueLabel
					(formatMM(sc.getBorderVert()));
			JLabel h_pitch_lbl = createValueLabel(
					formatMM(sc.getPitchHoriz()));
			JLabel v_pitch_lbl = createValueLabel(
					formatMM(sc.getPitchVert()));
			JLabel p_width_lbl = createValueLabel(
					formatPixels(sc.getPixelWidth()));
			JLabel p_height_lbl = createValueLabel(
					formatPixels(sc.getPixelHeight()));
			JLabel c_width_lbl = createValueLabel(
					formatPixels(sc.getCharWidth()));
			JLabel c_height_lbl = createValueLabel(
					formatPixels(sc.getCharHeight()));
			JLabel m_foreground_lbl = createValueLabel(HexString.format(
					sc.getDefaultFG().rgb(), 6));
			m_foreground_lbl.setIcon(new ColorIcon(
					sc.getDefaultFG().rgb()));
			JLabel m_background_lbl = createValueLabel(HexString.format(
					sc.getDefaultBG().rgb(), 6));
			m_background_lbl.setIcon(new ColorIcon(
					sc.getDefaultBG().rgb()));
			JLabel c_scheme_lbl = createValueLabel(
					sc.getColorScheme().description);
			JLabel font_lbl = createValueLabel(
					sc.getDefaultFont().getName());
			JLabel font_height_lbl = createValueLabel(
					calculateFontHeight());
			
			cp.add("dms.face.width");
			cp.add(f_width_lbl, Stretch.LAST);
			cp.add("dms.face.height");
			cp.add(f_height_lbl, Stretch.LAST);
			cp.add("dms.border.horiz");
			cp.add(h_border_lbl, Stretch.LAST);
			cp.add("dms.border.vert");
			cp.add(v_border_lbl, Stretch.LAST);
			cp.add("dms.pitch.horiz");
			cp.add(h_pitch_lbl, Stretch.LAST);
			cp.add("dms.pitch.vert");
			cp.add(v_pitch_lbl, Stretch.LAST);
			cp.add("dms.pixel.width");
			cp.add(p_width_lbl, Stretch.LAST);
			cp.add("dms.pixel.height");
			cp.add(p_height_lbl, Stretch.LAST);
			cp.add("dms.char.width");
			cp.add(c_width_lbl, Stretch.LAST);
			cp.add("dms.char.height");
			cp.add(c_height_lbl, Stretch.LAST);
			cp.add("dms.monochrome.foreground");
			cp.add(m_foreground_lbl, Stretch.LAST);
			cp.add("dms.monochrome.background");
			cp.add(m_background_lbl, Stretch.LAST);
			cp.add("dms.color.scheme");
			cp.add(c_scheme_lbl, Stretch.LAST);
			cp.add("dms.font.default");
			cp.add(font_lbl, Stretch.LAST);
			cp.add("dms.font.height");
			cp.add(font_height_lbl, Stretch.LAST);
		}
		return cp;
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