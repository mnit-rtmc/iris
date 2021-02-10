/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.alert;

import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.AlertInfoHelper;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.CapCertainty;
import us.mn.state.dot.tms.CapEvent;
import us.mn.state.dot.tms.CapResponseType;
import us.mn.state.dot.tms.CapSeverity;
import us.mn.state.dot.tms.CapUrgency;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An alert dispatcher is a GUI panel for dispatching and reviewing automated
 * alerts for deployment on DMS.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertDispatcher extends IPanel {

	/** Date/time formatter */
	private final SimpleDateFormat dt_format =
		new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/** Client session */
	private final Session session;

	/** Alert manager */
	private final AlertManager manager;

	/** Proxy watcher */
	private final ProxyWatcher<AlertInfo> watcher;

	/** Alert selection model */
	private final ProxySelectionModel<AlertInfo> sel_mdl;

	/** Alert selection listener */
	private final ProxySelectionListener alertSelLstnr =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			selectAlert();
		}
	};

	/** Headline label */
	private final JLabel headline_lbl = createValueLabel();

	/** Response type label */
	private final JLabel resp_lbl = createValueLabel();

	/** Urgency label */
	private final JLabel urgency_lbl = createValueLabel();

	/** Severity label */
	private final JLabel severity_lbl = createValueLabel();

	/** Certainty label */
	private final JLabel certainty_lbl = createValueLabel();

	/** Start date label */
	private final JLabel start_date_lbl = createValueLabel();

	/** End date label */
	private final JLabel end_date_lbl = createValueLabel();

	/** Area description label */
	private final JLabel area_lbl = createValueLabel();

	/** Description text */
	private final JTextArea description_txt = new JTextArea(2, 24);

	/** Instruction text */
	private final JTextArea instruction_txt = new JTextArea(2, 24);

	/** Action to deploy alert */
	private final IAction deploy_act = new IAction("alert.deploy") {
		protected void doActionPerformed(ActionEvent e) {
			AlertInfo ai = sel_mdl.getSingleSelection();
			if (ai != null) {
				ai.setAlertStateReq(AlertState.ACTIVE_REQ
					.ordinal());
			}
		}
	};

	/** Action to clear an alert */
	private final IAction clear_act = new IAction("alert.clear") {
		protected void doActionPerformed(ActionEvent e) {
			AlertInfo ai = sel_mdl.getSingleSelection();
			if (ai != null) {
				ai.setAlertStateReq(clear_btn.isSelected()
					? AlertState.CLEARED_REQ.ordinal()
					: AlertState.ACTIVE_REQ.ordinal());
			}
		}
	};

	/** Button to clear an alert */
	private final JCheckBox clear_btn = new JCheckBox(clear_act);

	/** Proxy view */
	private final ProxyView<AlertInfo> view = new ProxyView<AlertInfo>() {
		private AlertInfo alert_info;
		@Override public void enumerationComplete() { }
		@Override public void update(AlertInfo ai, String a) {
			if (a == null) {
				alert_info = ai;
				setSelectedAlert(ai);
			}
			if (a == null || a.equals("alertState")) {
				AlertState st = AlertState.fromOrdinal(
					ai.getAlertState());
				deploy_act.setEnabled(st == AlertState.PENDING
					&& isWritePermitted());
				// temporarily disable action
				clear_act.setEnabled(false);
				clear_btn.setSelected(st == AlertState.CLEARED);
				clear_act.setEnabled(isWritePermitted());
			}
		}
		@Override public void clear() {
			alert_info = null;
			headline_lbl.setText("");
			resp_lbl.setText("");
			urgency_lbl.setText("");
			severity_lbl.setText("");
			certainty_lbl.setText("");
			start_date_lbl.setText("");
			end_date_lbl.setText("");
			area_lbl.setText("");
			description_txt.setText("");
			instruction_txt.setText("");
			deploy_act.setEnabled(false);
			clear_act.setEnabled(false);
		}
	};

	/** Alert DMS dispatcher for deploying/reviewing DMS used for this alert*/
	private final AlertDmsDispatcher dmsDispatcher;

	/** Create a new alert dispatcher. */
	public AlertDispatcher(Session s, AlertManager m) {
		super();
		session = s;
		manager = m;
		sel_mdl = manager.getSelectionModel();
		sel_mdl.setAllowMultiple(false);
		sel_mdl.addProxySelectionListener(alertSelLstnr);
		TypeCache<AlertInfo> cache = s.getSonarState().getAlertInfos();
		watcher = new ProxyWatcher<AlertInfo>(cache, view, false);
		dmsDispatcher = new AlertDmsDispatcher(s, m);
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		dmsDispatcher.initialize();
		setTitle(I18N.get("alert.selected"));
		add(headline_lbl, Stretch.CENTER);
		add("alert.response");
		add(resp_lbl);
		add("alert.urgency");
		add(urgency_lbl, Stretch.LAST);
		add("alert.severity");
		add(severity_lbl);
		add("alert.certainty");
		add(certainty_lbl, Stretch.LAST);
		add("alert.start.date");
		add(start_date_lbl);
		add("alert.end.date");
		add(end_date_lbl, Stretch.LAST);
		add("alert.area_desc");
		add(area_lbl, Stretch.LAST);
		add("alert.description");
		add(description_txt, Stretch.TEXT);
		add("alert.instruction");
		add(instruction_txt, Stretch.TEXT);
		add(new JLabel());
		add(new JButton(deploy_act));
		add(clear_btn, Stretch.LAST);
		add(dmsDispatcher, Stretch.FULL);
		description_txt.setEnabled(false);
		instruction_txt.setEnabled(false);
		view.clear();
		watcher.initialize();
	}

	/** Dispose of the widgets */
	@Override
	public void dispose() {
		view.clear();
		watcher.dispose();
		super.dispose();
	}

	/** Update the display to reflect the alert selected. */
	private void selectAlert() {
		AlertInfo ai = sel_mdl.getSingleSelection();
		if (ai != null)
			watcher.setProxy(ai);
		else {
			watcher.setProxy(null);
			dmsDispatcher.clearSelectedAlert();
		}
	}

	/** Set the selected alert */
	private void setSelectedAlert(AlertInfo ai) {
		headline_lbl.setText(ai.getHeadline());
		resp_lbl.setText(CapResponseType.fromOrdinal(
			ai.getResponseType()).toString());
		urgency_lbl.setText(CapUrgency.fromOrdinal(ai.getUrgency())
			.toString());
		severity_lbl.setText(CapSeverity.fromOrdinal(ai.getSeverity())
			.toString());
		certainty_lbl.setText(CapCertainty.fromOrdinal(
			ai.getCertainty()).toString());
		start_date_lbl.setText(dt_format.format(ai.getStartDate()));
		end_date_lbl.setText(dt_format.format(ai.getEndDate()));
		area_lbl.setText(ai.getAreaDesc());
		description_txt.setText(ai.getDescription());
		instruction_txt.setText(ai.getInstruction());
		deploy_act.setEnabled(isWritePermitted());
		clear_act.setEnabled(isWritePermitted());
		dmsDispatcher.setSelectedAlert(ai);
	}

	/** Check if the user is permitted to write to alert infos */
	private boolean isWritePermitted() {
		return session.isWritePermitted(AlertInfo.SONAR_TYPE);
	}

	/** Get the AlertDmsDispatcher */
	public AlertDmsDispatcher getDmsDispatcher() {
		return dmsDispatcher;
	}
}
