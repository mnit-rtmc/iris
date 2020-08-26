/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.tms.CommConfig;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.ProxyPanel;
import us.mn.state.dot.tms.units.Interval;

/**
 * A panel for viewing the properties of a comm config.
 *
 * @author Douglas Lau
 */
public class CommConfigPanel extends ProxyPanel<CommConfig> {

	/** Description text field */
	private final JTextField description_txt = new JTextField(20);

	/** Protocol combobox */
	private final JComboBox<CommProtocol> protocol_cbx =
		new JComboBox<CommProtocol>(CommProtocol.valuesSorted());

	/** Modem check box */
	private final JCheckBox modem_chk = new JCheckBox();

	/** Spinner for timeout */
	private final JSpinner timeout_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, CommConfig.MAX_TIMEOUT_MS, 50));

	/** Spinner for poll period */
	private final JSpinner period_spn = new JSpinner(
		new SpinnerListModel(CommConfig.VALID_PERIODS));

	/** Spinner for idle disconnect */
	private final JSpinner idle_disconnect_spn = new JSpinner(
		new SpinnerListModel(CommConfig.VALID_DISCONNECT));

	/** Spinner for no response disconnect */
	private final JSpinner no_response_disconnect_spn = new JSpinner(
		new SpinnerListModel(CommConfig.VALID_DISCONNECT));

	/** User session */
	private final Session session;

	/** Create the comm_config panel */
	public CommConfigPanel(Session s) {
		super(s, s.getSonarState().getConCache().getCommConfigs());
		session = s;
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		add("device.description");
		add(description_txt, Stretch.LAST);
		add("comm.config.protocol");
		add(protocol_cbx, Stretch.LAST);
		add("comm.config.modem");
		add(modem_chk);
		add("comm.config.timeout_ms");
		add(timeout_spn, Stretch.LAST);
		add("comm.config.poll_period_sec");
		add(period_spn, Stretch.LAST);
		add("comm.config.idle_disconnect_sec");
		add(idle_disconnect_spn, Stretch.LAST);
		add("comm.config.no_response_disconnect_sec");
		add(no_response_disconnect_spn, Stretch.LAST);
		// Disable all the widgets
		description_txt.setEnabled(false);
		protocol_cbx.setEnabled(false);
		modem_chk.setEnabled(false);
		timeout_spn.setEnabled(false);
		period_spn.setEnabled(false);
		idle_disconnect_spn.setEnabled(false);
		no_response_disconnect_spn.setEnabled(false);
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode(CommConfig cc) { }

	/** Update one attribute */
	@Override
	protected void updateAttrib(CommConfig cc, String a) {
		if (a == null || a.equals("description"))
			description_txt.setText(cc.getDescription());
		if (a == null || a.equals("protocol")) {
			protocol_cbx.setSelectedItem(CommProtocol.fromOrdinal(
				cc.getProtocol()));
		}
		if (a == null || a.equals("modem"))
			modem_chk.setSelected(cc.getModem());
		if (a == null || a.equals("timeoutMs"))
			timeout_spn.setValue(cc.getTimeoutMs());
		if (a == null || a.equals("pollPeriodSec")) {
			Interval p = new Interval(cc.getPollPeriodSec());
			period_spn.setValue(p);
		}
		if (a == null || a.equals("idleDisconnectSec")) {
			Interval p = new Interval(cc.getIdleDisconnectSec());
			idle_disconnect_spn.setValue(p);
		}
		if (a == null || a.equals("noResponseDisconnectSec")) {
			Interval p = new Interval(
				cc.getNoResponseDisconnectSec());
			no_response_disconnect_spn.setValue(p);
		}
	}

	/** Clear the view */
	@Override
	protected void clearView() {
		description_txt.setText("");
		protocol_cbx.setSelectedIndex(0);
		modem_chk.setSelected(false);
		timeout_spn.setValue(0);
		period_spn.setValue(new Interval(30));
		idle_disconnect_spn.setValue(new Interval(0));
		no_response_disconnect_spn.setValue(new Interval(0));
	}
}
