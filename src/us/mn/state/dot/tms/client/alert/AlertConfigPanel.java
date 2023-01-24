/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021-2023  Minnesota Department of Transportation
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

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.AlertMessage;
import us.mn.state.dot.tms.CapEvent;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import static us.mn.state.dot.tms.client.widget.IOptionPane.showHint;

/**
 * A panel for editing the properties of an alert config.
 *
 * @author Douglas Lau
 */
public class AlertConfigPanel extends IPanel {

	/** Inner class for rendering cells in the event combo box*/
	static private class EventCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected,
			boolean hasFocus)
		{
			String v = "";
			if (value instanceof CapEvent) {
				CapEvent ev = (CapEvent) value;
				v = ev.description + " (" + ev.name() + ")";
			}
			return super.getListCellRendererComponent(list, v,
				index, isSelected, hasFocus);
		}
	}

	/** Check a hashtag */
	static private String checkHashtag(Object value) {
		return DMSHelper.normalizeHashtag(value.toString());
	}

	/** AlertConfig action */
	abstract private class CAction extends IAction {
		protected CAction(String text_id) {
			super(text_id);
		}
		protected final void doActionPerformed(ActionEvent e) {
			AlertConfig cfg = alert_cfg;
			if (cfg != null)
				do_perform(cfg);
		}
		abstract protected void do_perform(AlertConfig cfg);
	}

	/** Event action */
	private final CAction event_act = new CAction("alert.event") {
		protected void do_perform(AlertConfig cfg) {
			Object ev = event_cbx.getSelectedItem();
			cfg.setEvent((ev instanceof CapEvent)
				? ((CapEvent) ev).name()
				: null);
		}
		@Override protected void doUpdateSelected() {
			AlertConfig cfg = alert_cfg;
			if (cfg != null) {
				CapEvent ev = CapEvent.fromCode(cfg.getEvent());
				if (ev != null) {
					event_cbx.setSelectedItem(ev);
					return;
				}
			}
			event_cbx.setSelectedItem(null);
		}
	};

	/** Event combobox */
	private final JComboBox<CapEvent> event_cbx =
		new JComboBox<CapEvent>(CapEvent.values());

	/** Response shelter check box */
	private final JCheckBox resp_shelter_chk = new JCheckBox(
		new CAction("alert.response.shelter")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setResponseShelter(resp_shelter_chk.isSelected());
		}
	});

	/** Response evacuate check box */
	private final JCheckBox resp_evacuate_chk = new JCheckBox(
		new CAction("alert.response.evacuate")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setResponseEvacuate(resp_evacuate_chk.isSelected());
		}
	});

	/** Response prepare check box */
	private final JCheckBox resp_prepare_chk = new JCheckBox(
		new CAction("alert.response.prepare")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setResponsePrepare(resp_prepare_chk.isSelected());
		}
	});

	/** Response execute check box */
	private final JCheckBox resp_execute_chk = new JCheckBox(
		new CAction("alert.response.execute")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setResponseExecute(resp_execute_chk.isSelected());
		}
	});

	/** Response avoid check box */
	private final JCheckBox resp_avoid_chk = new JCheckBox(
		new CAction("alert.response.avoid")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setResponseAvoid(resp_avoid_chk.isSelected());
		}
	});

	/** Response monitor check box */
	private final JCheckBox resp_monitor_chk = new JCheckBox(
		new CAction("alert.response.monitor")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setResponseMonitor(resp_monitor_chk.isSelected());
		}
	});

	/** Response all clear check box */
	private final JCheckBox resp_all_clear_chk = new JCheckBox(
		new CAction("alert.response.all_clear")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setResponseAllClear(resp_all_clear_chk.isSelected());
		}
	});

	/** Response none check box */
	private final JCheckBox resp_none_chk = new JCheckBox(
		new CAction("alert.response.none")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setResponseNone(resp_none_chk.isSelected());
		}
	});

	/** Urgency unknown check box */
	private final JCheckBox urg_unknown_chk = new JCheckBox(
		new CAction("alert.urgency.unknown")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setUrgencyUnknown(urg_unknown_chk.isSelected());
		}
	});

	/** Urgency past check box */
	private final JCheckBox urg_past_chk = new JCheckBox(
		new CAction("alert.urgency.past")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setUrgencyPast(urg_past_chk.isSelected());
		}
	});

	/** Urgency future check box */
	private final JCheckBox urg_future_chk = new JCheckBox(
		new CAction("alert.urgency.future")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setUrgencyFuture(urg_future_chk.isSelected());
		}
	});

	/** Urgency expected check box */
	private final JCheckBox urg_expected_chk = new JCheckBox(
		new CAction("alert.urgency.expected")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setUrgencyExpected(urg_expected_chk.isSelected());
		}
	});

	/** Urgency immediate check box */
	private final JCheckBox urg_immediate_chk = new JCheckBox(
		new CAction("alert.urgency.immediate")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setUrgencyImmediate(urg_immediate_chk.isSelected());
		}
	});

	/** Severity unknown check box */
	private final JCheckBox sev_unknown_chk = new JCheckBox(
		new CAction("alert.severity.unknown")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setSeverityUnknown(sev_unknown_chk.isSelected());
		}
	});

	/** Severity minor check box */
	private final JCheckBox sev_minor_chk = new JCheckBox(
		new CAction("alert.severity.minor")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setSeverityMinor(sev_minor_chk.isSelected());
		}
	});

	/** Severity moderate check box */
	private final JCheckBox sev_moderate_chk = new JCheckBox(
		new CAction("alert.severity.moderate")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setSeverityModerate(sev_moderate_chk.isSelected());
		}
	});

	/** Severity severe check box */
	private final JCheckBox sev_severe_chk = new JCheckBox(
		new CAction("alert.severity.severe")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setSeveritySevere(sev_severe_chk.isSelected());
		}
	});

	/** Severity extreme check box */
	private final JCheckBox sev_extreme_chk = new JCheckBox(
		new CAction("alert.severity.extreme")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setSeverityExtreme(sev_extreme_chk.isSelected());
		}
	});

	/** Certainty unknown check box */
	private final JCheckBox cer_unknown_chk = new JCheckBox(
		new CAction("alert.certainty.unknown")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setCertaintyUnknown(cer_unknown_chk.isSelected());
		}
	});

	/** Certainty unlikely check box */
	private final JCheckBox cer_unlikely_chk = new JCheckBox(
		new CAction("alert.certainty.unlikely")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setCertaintyUnlikely(cer_unlikely_chk.isSelected());
		}
	});

	/** Certainty possible check box */
	private final JCheckBox cer_possible_chk = new JCheckBox(
		new CAction("alert.certainty.possible")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setCertaintyPossible(cer_possible_chk.isSelected());
		}
	});

	/** Certainty likely check box */
	private final JCheckBox cer_likely_chk = new JCheckBox(
		new CAction("alert.certainty.likely")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setCertaintyLikely(cer_likely_chk.isSelected());
		}
	});

	/** Certainty observed check box */
	private final JCheckBox cer_observed_chk = new JCheckBox(
		new CAction("alert.certainty.observed")
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setCertaintyObserved(cer_observed_chk.isSelected());
		}
	});

	/** Auto-deploy check box */
	private final JCheckBox auto_deploy_chk = new JCheckBox(
		new CAction(null)
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setAutoDeploy(auto_deploy_chk.isSelected());
		}
	});

	/** Spinner for before period hours */
	private final JSpinner bfr_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 24, 1));

	/** Spinner for after period hours */
	private final JSpinner aft_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 24, 1));

	/** DMS hashtag text field */
	private final JTextField hashtag_txt = new JTextField(16);

	/** Alert message table panel */
	private final ProxyTablePanel<AlertMessage> msg_panel;

	/** User session */
	private final Session session;

	/** Proxy view */
	private final ProxyView<AlertConfig> view = new ProxyView<AlertConfig>()
	{
		@Override public void enumerationComplete() { }
		@Override public void update(AlertConfig cfg, String a) {
			if (a == null) {
				alert_cfg = cfg;
				updateEditMode();
			}
			if (a == null || a.equals("event"))
				event_act.updateSelected();
			if (a == null || a.equals("responseShelter")) {
				resp_shelter_chk.setSelected(
					cfg.getResponseShelter());
			}
			if (a == null || a.equals("responseEvacuate")) {
				resp_evacuate_chk.setSelected(
					cfg.getResponseEvacuate());
			}
			if (a == null || a.equals("responsePrepare")) {
				resp_prepare_chk.setSelected(
					cfg.getResponsePrepare());
			}
			if (a == null || a.equals("responseExecute")) {
				resp_execute_chk.setSelected(
					cfg.getResponseExecute());
			}
			if (a == null || a.equals("responseAvoid")) {
				resp_avoid_chk.setSelected(
					cfg.getResponseAvoid());
			}
			if (a == null || a.equals("responseMonitor")) {
				resp_monitor_chk.setSelected(
					cfg.getResponseMonitor());
			}
			if (a == null || a.equals("responseAllClear")) {
				resp_all_clear_chk.setSelected(
					cfg.getResponseAllClear());
			}
			if (a == null || a.equals("responseNone")) {
				resp_none_chk.setSelected(
					cfg.getResponseNone());
			}
			if (a == null || a.equals("urgencyUnknown")) {
				urg_unknown_chk.setSelected(
					cfg.getUrgencyUnknown());
			}
			if (a == null || a.equals("urgencyPast")) {
				urg_past_chk.setSelected(
					cfg.getUrgencyPast());
			}
			if (a == null || a.equals("urgencyFuture")) {
				urg_future_chk.setSelected(
					cfg.getUrgencyFuture());
			}
			if (a == null || a.equals("urgencyExpected")) {
				urg_expected_chk.setSelected(
					cfg.getUrgencyExpected());
			}
			if (a == null || a.equals("urgencyImmediate")) {
				urg_immediate_chk.setSelected(
					cfg.getUrgencyImmediate());
			}
			if (a == null || a.equals("severityUnknown")) {
				sev_unknown_chk.setSelected(
					cfg.getSeverityUnknown());
			}
			if (a == null || a.equals("severityMinor")) {
				sev_minor_chk.setSelected(
					cfg.getSeverityMinor());
			}
			if (a == null || a.equals("severityModerate")) {
				sev_moderate_chk.setSelected(
					cfg.getSeverityModerate());
			}
			if (a == null || a.equals("severitySevere")) {
				sev_severe_chk.setSelected(
					cfg.getSeveritySevere());
			}
			if (a == null || a.equals("severityExtreme")) {
				sev_extreme_chk.setSelected(
					cfg.getSeverityExtreme());
			}
			if (a == null || a.equals("certaintyUnknown")) {
				cer_unknown_chk.setSelected(
					cfg.getCertaintyUnknown());
			}
			if (a == null || a.equals("certaintyUnlikely")) {
				cer_unlikely_chk.setSelected(
					cfg.getCertaintyUnlikely());
			}
			if (a == null || a.equals("certaintyPossible")) {
				cer_possible_chk.setSelected(
					cfg.getCertaintyPossible());
			}
			if (a == null || a.equals("certaintyLikely")) {
				cer_likely_chk.setSelected(
					cfg.getCertaintyLikely());
			}
			if (a == null || a.equals("certaintyObserved")) {
				cer_observed_chk.setSelected(
					cfg.getCertaintyObserved());
			}
			if (a == null || a.equals("autoDeploy"))
				auto_deploy_chk.setSelected(cfg.getAutoDeploy());
			if (a == null || a.equals("beforePeriodHours"))
				bfr_spn.setValue(cfg.getBeforePeriodHours());
			if (a == null || a.equals("afterPeriodHours"))
				aft_spn.setValue(cfg.getAfterPeriodHours());
			if (a == null || a.equals("dmsHashtag")) {
				String ht = cfg.getDmsHashtag();
				hashtag_txt.setText((ht != null) ? ht : "");
			}
		}
		@Override public void clear() {
			alert_cfg = null;
			event_act.setEnabled(false);
			event_cbx.setSelectedItem(null);
			event_cbx.setRenderer(new EventCellRenderer());
			resp_shelter_chk.setEnabled(false);
			resp_shelter_chk.setSelected(false);
			resp_evacuate_chk.setEnabled(false);
			resp_evacuate_chk.setSelected(false);
			resp_prepare_chk.setEnabled(false);
			resp_prepare_chk.setSelected(false);
			resp_execute_chk.setEnabled(false);
			resp_execute_chk.setSelected(false);
			resp_avoid_chk.setEnabled(false);
			resp_avoid_chk.setSelected(false);
			resp_monitor_chk.setEnabled(false);
			resp_monitor_chk.setSelected(false);
			resp_all_clear_chk.setEnabled(false);
			resp_all_clear_chk.setSelected(false);
			resp_none_chk.setEnabled(false);
			resp_none_chk.setSelected(false);
			urg_unknown_chk.setEnabled(false);
			urg_unknown_chk.setSelected(false);
			urg_past_chk.setEnabled(false);
			urg_past_chk.setSelected(false);
			urg_future_chk.setEnabled(false);
			urg_future_chk.setSelected(false);
			urg_expected_chk.setEnabled(false);
			urg_expected_chk.setSelected(false);
			urg_immediate_chk.setEnabled(false);
			urg_immediate_chk.setSelected(false);
			sev_unknown_chk.setEnabled(false);
			sev_unknown_chk.setSelected(false);
			sev_minor_chk.setEnabled(false);
			sev_minor_chk.setSelected(false);
			sev_moderate_chk.setEnabled(false);
			sev_moderate_chk.setSelected(false);
			sev_severe_chk.setEnabled(false);
			sev_severe_chk.setSelected(false);
			sev_extreme_chk.setEnabled(false);
			sev_extreme_chk.setSelected(false);
			cer_unknown_chk.setEnabled(false);
			cer_unknown_chk.setSelected(false);
			cer_unlikely_chk.setEnabled(false);
			cer_unlikely_chk.setSelected(false);
			cer_possible_chk.setEnabled(false);
			cer_possible_chk.setSelected(false);
			cer_likely_chk.setEnabled(false);
			cer_likely_chk.setSelected(false);
			cer_observed_chk.setEnabled(false);
			cer_observed_chk.setSelected(false);
			auto_deploy_chk.setEnabled(false);
			auto_deploy_chk.setSelected(false);
			bfr_spn.setEnabled(false);
			bfr_spn.setValue(0);
			aft_spn.setEnabled(false);
			aft_spn.setValue(0);
			hashtag_txt.setEnabled(false);
			hashtag_txt.setText("");
		}
	};

	/** Proxy watcher */
	private final ProxyWatcher<AlertConfig> watcher;

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

	/** AlertConfig being edited */
	private AlertConfig alert_cfg;

	/** Set the alert config */
	public void setAlertConfig(AlertConfig cfg) {
		watcher.setProxy(cfg);
		msg_panel.setModel(new AlertMessageModel(session, cfg));
	}

	/** Create the alert config panel */
	public AlertConfigPanel(Session s) {
		session = s;
		TypeCache<AlertConfig> cache =
			s.getSonarState().getAlertConfigs();
		msg_panel = new ProxyTablePanel<AlertMessage>(
			new AlertMessageModel(s, null));
		watcher = new ProxyWatcher<AlertConfig>(cache, view, false);
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		event_cbx.setAction(event_act);
		add("alert.event");
		add(event_cbx, Stretch.LAST);
		add("alert.response");
		JPanel p = new JPanel(new GridLayout(4, 2));
		p.setBorder(UI.buttonBorder());
		p.add(resp_shelter_chk);
		p.add(resp_evacuate_chk);
		p.add(resp_prepare_chk);
		p.add(resp_execute_chk);
		p.add(resp_avoid_chk);
		p.add(resp_monitor_chk);
		p.add(resp_all_clear_chk);
		p.add(resp_none_chk);
		add(p);
		add("alert.urgency");
		p = new JPanel(new GridLayout(3, 2));
		p.setBorder(UI.buttonBorder());
		p.add(urg_unknown_chk);
		p.add(urg_past_chk);
		p.add(urg_future_chk);
		p.add(urg_expected_chk);
		p.add(urg_immediate_chk);
		add(p, Stretch.LAST);
		add("alert.severity");
		p = new JPanel(new GridLayout(3, 2));
		p.setBorder(UI.buttonBorder());
		p.add(sev_unknown_chk);
		p.add(sev_minor_chk);
		p.add(sev_moderate_chk);
		p.add(sev_severe_chk);
		p.add(sev_extreme_chk);
		add(p);
		add("alert.certainty");
		p = new JPanel(new GridLayout(3, 2));
		p.setBorder(UI.buttonBorder());
		p.add(cer_unknown_chk);
		p.add(cer_unlikely_chk);
		p.add(cer_possible_chk);
		p.add(cer_likely_chk);
		p.add(cer_observed_chk);
		add(p, Stretch.LAST);
		add("alert.config.auto_deploy");
		add(auto_deploy_chk, Stretch.LAST);
		add("alert.before_period_hours");
		add(bfr_spn);
		add("alert.after_period_hours");
		add(aft_spn, Stretch.LAST);
		add("alert.hashtag");
		add(hashtag_txt, Stretch.LAST);
		add(msg_panel, Stretch.FULL);
		createJobs();
		watcher.initialize();
		view.clear();
		msg_panel.initialize();
		session.addEditModeListener(edit_lsnr);
	}

	/** Create the jobs */
	private void createJobs() {
		bfr_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Number h = (Number) bfr_spn.getValue();
				setBeforePeriodHours(h.intValue());
			}
		});
		aft_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Number h = (Number) aft_spn.getValue();
				setAfterPeriodHours(h.intValue());
			}
		});
		hashtag_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				setDmsHashtag(hashtag_txt.getText());
			}
		});
	}

	/** Set the before period hours */
	private void setBeforePeriodHours(int h) {
		AlertConfig cfg = alert_cfg;
		if (cfg != null)
			cfg.setBeforePeriodHours(h);
	}

	/** Set the after period hours */
	private void setAfterPeriodHours(int h) {
		AlertConfig cfg = alert_cfg;
		if (cfg != null)
			cfg.setAfterPeriodHours(h);
	}

	/** Set the DMS hashtag */
	private void setDmsHashtag(Object v) {
		AlertConfig cfg = alert_cfg;
		if (cfg != null)
			cfg.setDmsHashtag(checkHashtag(v));
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		view.clear();
		session.removeEditModeListener(edit_lsnr);
		watcher.dispose();
		super.dispose();
	}

	/** Update the edit mode */
	public void updateEditMode() {
		AlertConfig cfg = alert_cfg;
		boolean write = session.canWrite(cfg, "event");
		event_act.setEnabled(write);
		resp_shelter_chk.setEnabled(write);
		resp_evacuate_chk.setEnabled(write);
		resp_prepare_chk.setEnabled(write);
		resp_execute_chk.setEnabled(write);
		resp_avoid_chk.setEnabled(write);
		resp_monitor_chk.setEnabled(write);
		resp_all_clear_chk.setEnabled(write);
		resp_none_chk.setEnabled(write);
		urg_unknown_chk.setEnabled(write);
		urg_past_chk.setEnabled(write);
		urg_future_chk.setEnabled(write);
		urg_expected_chk.setEnabled(write);
		urg_immediate_chk.setEnabled(write);
		sev_unknown_chk.setEnabled(write);
		sev_minor_chk.setEnabled(write);
		sev_moderate_chk.setEnabled(write);
		sev_severe_chk.setEnabled(write);
		sev_extreme_chk.setEnabled(write);
		cer_unknown_chk.setEnabled(write);
		cer_unlikely_chk.setEnabled(write);
		cer_possible_chk.setEnabled(write);
		cer_likely_chk.setEnabled(write);
		cer_observed_chk.setEnabled(write);
		auto_deploy_chk.setEnabled(write);
		bfr_spn.setEnabled(write);
		aft_spn.setEnabled(write);
		hashtag_txt.setEnabled(write);
	}
}
