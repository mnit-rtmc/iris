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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PropManufacturer is a GUI panel for displaying manufacturer data on a DMS
 * properties form.
 *
 * @author Douglas Lau
 */
public class PropManufacturer extends IPanel {

	/** Unknown value string */
	static private final String UNKNOWN = "???";

	/** Format a string field */
	static private String formatString(String s) {
		if(s != null && s.length() > 0)
			return s;
		else
			return UNKNOWN;
	}

	/** Generic sign make */
	static private final String MAKE_GENERIC = "Generic";

	/** Ledstar sign make */
	static private final String MAKE_LEDSTAR = "Ledstar";

	/** Skyline sign make */
	static private final String MAKE_SKYLINE = "Skyline";

	/** Card layout for manufacturer panels */
	private final CardLayout cards = new CardLayout();

	/** Card panel for manufacturer panels */
	private final JPanel card_pnl = new JPanel(cards);

	/** Make label */
	private final JLabel make_lbl = createValueLabel();

	/** Model label */
	private final JLabel model_lbl = createValueLabel();

	/** Version label */
	private final JLabel version_lbl = createValueLabel();

	/** Spinner to adjuct LDC pot base */
	private final JSpinner pot_base_spn = new JSpinner(
		new SpinnerNumberModel(20, 20, 65, 5));

	/** Pixel current low threshold spinner */
	private final JSpinner current_low_spn = new JSpinner(
		new SpinnerNumberModel(5, 0, 100, 1));

	/** Pixel current high threshold spinner */
	private final JSpinner current_high_spn = new JSpinner(
		new SpinnerNumberModel(40, 0, 100, 1));

	/** Heat tape status label */
	private final JLabel heat_tape_lbl = createValueLabel();

	/** Config action */
	private final IAction config = new IAction("dms.config") {
		protected void doActionPerformed(ActionEvent e) {
			configPressed();
		}
	};

	/** Sign config button pressed */
	private void configPressed() {
		SignConfig sc = dms.getSignConfig();
		if (sc != null) {
			session.getDesktop().show(new SignConfigForm(session,
				sc));
		}
	}

	/** User session */
	private final Session session;

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties manufacturer panel */
	public PropManufacturer(Session s, DMS sign) {
		session = s;
		dms = sign;
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		add("dms.make");
		add(make_lbl, Stretch.LAST);
		add("dms.model");
		add(model_lbl, Stretch.LAST);
		add("dms.version");
		add(version_lbl, Stretch.LAST);
		add(card_pnl, Stretch.CENTER);
		card_pnl.add(createGenericPanel(), MAKE_GENERIC);
		card_pnl.add(createLedstarPanel(), MAKE_LEDSTAR);
		card_pnl.add(createSkylinePanel(), MAKE_SKYLINE);
		add(new JButton(config), Stretch.RIGHT);
		createUpdateJobs();
		updateAttribute(null);
	}

	/** Create the widget jobs */
	private void createUpdateJobs() {
		pot_base_spn.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Number n = (Number)pot_base_spn.getValue();
				dms.setLdcPotBase(n.intValue());
			}
		});
		current_low_spn.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Number n = (Number)current_low_spn.getValue();
				dms.setPixelCurrentLow(n.intValue());
			}
		});
		current_high_spn.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Number n = (Number)current_high_spn.getValue();
				dms.setPixelCurrentHigh(n.intValue());
			}
		});
	}

	/** Create generic manufacturer panel */
	private JPanel createGenericPanel() {
		IPanel p = new IPanel();
		p.setTitle(I18N.get("dms.manufacturer.unknown"));
		p.add(createValueLabel(UNKNOWN), Stretch.CENTER);
		return p;
	}

	/** Create Ledstar-specific panel */
	private JPanel createLedstarPanel() {
		IPanel p = new IPanel();
		p.setTitle(MAKE_LEDSTAR);
		p.add("dms.ledstar.pot.base");
		p.add(pot_base_spn, Stretch.LAST);
		p.add("dms.ledstar.current.low");
		p.add(current_low_spn, Stretch.LAST);
		p.add("dms.ledstar.current.high");
		p.add(current_high_spn, Stretch.LAST);
		return p;
	}

	/** Create Skyline-specific panel */
	private JPanel createSkylinePanel() {
		IPanel p = new IPanel();
		p.setTitle(MAKE_SKYLINE);
		p.add("dms.skyline.heat.tape");
		p.add(heat_tape_lbl, Stretch.LAST);
		return p;
	}

	/** Update one attribute on the panel */
	public void updateAttribute(String a) {
		if(a == null || a.equals("make")) {
			String m = formatString(dms.getMake());
			make_lbl.setText(m);
			updateMake(m.toUpperCase());
		}
		if(a == null || a.equals("model"))
			model_lbl.setText(formatString(dms.getModel()));
		if(a == null || a.equals("version"))
			version_lbl.setText(formatString(dms.getVersion()));
		if(a == null || a.equals("ldcPotBase")) {
			pot_base_spn.setEnabled(canUpdate("ldcPotBase"));
			Integer b = dms.getLdcPotBase();
			if(b != null)
				pot_base_spn.setValue(b);
		}
		if(a == null || a.equals("pixelCurrentLow")) {
			current_low_spn.setEnabled(canUpdate("pixelCurrentLow"));
			Integer c = dms.getPixelCurrentLow();
			if(c != null)
				current_low_spn.setValue(c);
		}
		if(a == null || a.equals("pixelCurrentHigh")) {
			current_high_spn.setEnabled(canUpdate(
				"pixelCurrentHigh"));
			Integer c = dms.getPixelCurrentHigh();
			if(c != null)
				current_high_spn.setValue(c);
		}
		if(a == null || a.equals("heatTapeStatus"))
			heat_tape_lbl.setText(dms.getHeatTapeStatus());
		if (null == a || a.equals("signConfig"))
			config.setEnabled(dms.getSignConfig() != null);
	}

	/** Select card on manufacturer panel for the given make */
	private void updateMake(String m) {
		if(m.contains(MAKE_LEDSTAR.toUpperCase()))
			cards.show(card_pnl, MAKE_LEDSTAR);
		else if(m.contains(MAKE_SKYLINE.toUpperCase()))
			cards.show(card_pnl, MAKE_SKYLINE);
		else
			cards.show(card_pnl, MAKE_GENERIC);
	}

	/** Check if the user can update an attribute */
	private boolean canUpdate(String aname) {
		return session.canUpdate(dms, aname);
	}
}
