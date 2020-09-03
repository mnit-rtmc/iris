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

import javax.swing.JLabel;
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

	/** Format a period nicely */
	static private String prettyPeriod(int sec) {
		Interval p = new Interval(sec);
		for (Interval per: CommConfig.VALID_PERIODS) {
			if (p.equals(per))
				return per.toString();
		}
		return p.toString();
	}

	/** Filler label */
	private final JLabel filler_lbl = createValueLabel(
		"____________________");

	/** Description label */
	private final JLabel description_lbl = createValueLabel();

	/** Protocol label */
	private final JLabel protocol_lbl = createValueLabel();

	/** Modem label */
	private final JLabel modem_lbl = createValueLabel();

	/** Timeout label */
	private final JLabel timeout_lbl = createValueLabel();

	/** Poll period label */
	private final JLabel period_lbl = createValueLabel();

	/** Long poll period label */
	private final JLabel long_period_lbl = createValueLabel();

	/** Idle disconnect label */
	private final JLabel idle_disconnect_lbl = createValueLabel();

	/** No response disconnect label */
	private final JLabel no_response_disconnect_lbl = createValueLabel();

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
		filler_lbl.setForeground(filler_lbl.getBackground());
		add("comm.config", Stretch.CENTER);
		add(new JLabel());
		add(filler_lbl, Stretch.LAST);
		add("device.description");
		add(description_lbl, Stretch.LAST);
		add("comm.config.protocol");
		add(protocol_lbl, Stretch.LAST);
		add("comm.config.modem");
		add(modem_lbl, Stretch.LAST);
		add("comm.config.timeout_ms");
		add(timeout_lbl, Stretch.LAST);
		add("comm.config.poll_period_sec");
		add(period_lbl, Stretch.LAST);
		add("comm.config.long_poll_period_sec");
		add(long_period_lbl, Stretch.LAST);
		add("comm.config.idle_disconnect_sec");
		add(idle_disconnect_lbl, Stretch.LAST);
		add("comm.config.no_response_disconnect_sec");
		add(no_response_disconnect_lbl, Stretch.LAST);
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode(CommConfig cc) { }

	/** Update one attribute */
	@Override
	protected void updateAttrib(CommConfig cc, String a) {
		if (a == null || a.equals("description"))
			description_lbl.setText(cc.getDescription());
		if (a == null || a.equals("protocol")) {
			protocol_lbl.setText(CommProtocol.fromOrdinal(
				cc.getProtocol()).toString());
		}
		if (a == null || a.equals("modem"))
			modem_lbl.setText(cc.getModem() ? "Yes" : "No");
		if (a == null || a.equals("timeoutMs"))
			timeout_lbl.setText("" + cc.getTimeoutMs() + " ms");
		if (a == null || a.equals("pollPeriodSec"))
			period_lbl.setText(prettyPeriod(cc.getPollPeriodSec()));
		if (a == null || a.equals("longPollPeriodSec")) {
			long_period_lbl.setText(prettyPeriod(
				cc.getLongPollPeriodSec()));
		}
		if (a == null || a.equals("idleDisconnectSec")) {
			idle_disconnect_lbl.setText(prettyPeriod(
				cc.getIdleDisconnectSec()));
		}
		if (a == null || a.equals("noResponseDisconnectSec")) {
			no_response_disconnect_lbl.setText(prettyPeriod(
				cc.getNoResponseDisconnectSec()));
		}
	}

	/** Clear the view */
	@Override
	protected void clearView() {
		description_lbl.setText("");
		protocol_lbl.setText("");
		modem_lbl.setText("");
		timeout_lbl.setText("");
		period_lbl.setText("");
		long_period_lbl.setText("");
		idle_disconnect_lbl.setText("");
		no_response_disconnect_lbl.setText("");
	}
}
