/*
 * IRIS -- Intelligent Roadway Information System
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.CapEvent;
import us.mn.state.dot.tms.CapResponseType;
import us.mn.state.dot.tms.CapUrgency;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.IOptionPane.showHint;
import us.mn.state.dot.tms.client.widget.IPanel;

/**
 * A panel for editing the properties of an alert config.
 *
 * @author Douglas Lau
 */
public class AlertConfigPanel extends IPanel {

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
	private final CAction event_act = new CAction("alert.cap.event") {
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
					event_lbl.setText(ev.description);
					return;
				}
			}
			event_cbx.setSelectedItem(null);
			event_lbl.setText("");
		}
	};

	/** Event combobox */
	private final JComboBox<CapEvent> event_cbx =
		new JComboBox<CapEvent>(CapEvent.values());

	/** Event description label */
	private final JLabel event_lbl = createValueLabel();

	/** Response type action */
	private final CAction resp_act = new CAction("alert.cap.response") {
		protected void do_perform(AlertConfig cfg) {
			Object rt = resp_cbx.getSelectedItem();
			cfg.setResponseType((rt instanceof CapResponseType)
				? ((CapResponseType) rt).ordinal()
				: null);
		}
		@Override protected void doUpdateSelected() {
			AlertConfig cfg = alert_cfg;
			CapResponseType rt = (cfg != null)
			    ? CapResponseType.fromOrdinal(cfg.getResponseType())
			    : null;
			resp_cbx.setSelectedItem(rt);
		}
	};

	/** Response type combobox */
	private final JComboBox<CapResponseType> resp_cbx =
		new JComboBox<CapResponseType>(CapResponseType.values());

	/** Urgency action */
	private final CAction urg_act = new CAction("alert.cap.urgency") {
		protected void do_perform(AlertConfig cfg) {
			Object urg = urg_cbx.getSelectedItem();
			cfg.setUrgency((urg instanceof CapUrgency)
				? ((CapUrgency) urg).ordinal()
				: null);
		}
		@Override protected void doUpdateSelected() {
			AlertConfig cfg = alert_cfg;
			CapUrgency urg = (cfg != null)
			          ? CapUrgency.fromOrdinal(cfg.getUrgency())
			          : null;
			urg_cbx.setSelectedItem(urg);
		}
	};

	/** Urgency combobox */
	private final JComboBox<CapUrgency> urg_cbx =
		new JComboBox<CapUrgency>(CapUrgency.values());

	/** Sign group text field */
	private final JTextField group_txt = new JTextField(20);

	/** Lookup a sign group */
	private SignGroup lookupSignGroup() {
		String v = group_txt.getText().trim();
		if (v.length() > 0) {
			SignGroup sg = SignGroupHelper.lookup(v);
			if (null == sg)
				showHint("dms.group.unknown.hint");
			return sg;
		} else
			return null;
	}

	/** Quick message text field */
	private final JTextField qm_txt = new JTextField(20);

	/** Lookup a quick message */
	private QuickMessage lookupQuickMessage() {
		String v = qm_txt.getText().trim();
		if (v.length() > 0) {
			QuickMessage qm = QuickMessageHelper.lookup(v);
			if (null == qm)
				showHint("quick.message.unknown.hint");
			return qm;
		} else
			return null;
	}

	/** Spinner for pre-alert hours */
	private final JSpinner pre_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 24, 1));

	/** Spinner for post-alert hours */
	private final JSpinner post_spn = new JSpinner(
		new SpinnerNumberModel(0, 0, 24, 1));

	/** Auto-deploy check box */
	private final JCheckBox auto_deploy_chk = new JCheckBox(
		new CAction(null)
	{
		protected void do_perform(AlertConfig cfg) {
			cfg.setAutoDeploy(auto_deploy_chk.isSelected());
		}
	});

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
			if (a == null || a.equals("responseType"))
				resp_act.updateSelected();
			if (a == null || a.equals("urgency"))
				urg_act.updateSelected();
			if (a == null || a.equals("signGroup")) {
				SignGroup sg = cfg.getSignGroup();
				group_txt.setText((sg != null)
					? sg.getName()
					: "");
			}
			if (a == null || a.equals("quickMessage")) {
				QuickMessage qm = cfg.getQuickMessage();
				qm_txt.setText((qm != null)
					? qm.getName()
					: "");
			}
			if (a == null || a.equals("preAlertHours"))
				pre_spn.setValue(cfg.getPreAlertHours());
			if (a == null || a.equals("postAlertHours"))
				post_spn.setValue(cfg.getPostAlertHours());
			if (a == null || a.equals("autoDeploy"))
				auto_deploy_chk.setSelected(cfg.getAutoDeploy());
		}
		@Override public void clear() {
			alert_cfg = null;
			event_act.setEnabled(false);
			event_cbx.setSelectedItem(null);
			event_lbl.setText("");
			resp_act.setEnabled(false);
			resp_cbx.setSelectedItem(null);
			urg_act.setEnabled(false);
			urg_cbx.setSelectedItem(null);
			group_txt.setEnabled(false);
			group_txt.setText("");
			qm_txt.setEnabled(false);
			qm_txt.setText("");
			pre_spn.setEnabled(false);
			pre_spn.setValue(0);
			post_spn.setEnabled(false);
			post_spn.setValue(0);
			auto_deploy_chk.setEnabled(false);
			auto_deploy_chk.setSelected(false);
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
	}

	/** Create the alert config panel */
	public AlertConfigPanel(Session s) {
		session = s;
		TypeCache<AlertConfig> cache =
			s.getSonarState().getAlertConfigs();
		watcher = new ProxyWatcher<AlertConfig>(cache, view, false);
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		event_cbx.setAction(event_act);
		resp_cbx.setAction(resp_act);
		urg_cbx.setAction(urg_act);
		add("alert.cap.event");
		add(event_cbx);
		add(event_lbl, Stretch.LAST);
		add("alert.cap.response");
		add(resp_cbx, Stretch.LAST);
		add("alert.cap.urgency");
		add(urg_cbx, Stretch.LAST);
		add("dms.group");
		add(group_txt, Stretch.END);
		add("dms.quick.message");
		add(qm_txt, Stretch.END);
		add("alert.pre_alert_hours");
		add(pre_spn, Stretch.LAST);
		add("alert.post_alert_hours");
		add(post_spn, Stretch.LAST);
		add("alert.config.auto_deploy");
		add(auto_deploy_chk);
		createJobs();
		watcher.initialize();
		view.clear();
		session.addEditModeListener(edit_lsnr);
	}

	/** Create the jobs */
	private void createJobs() {
		group_txt.addFocusListener(new FocusAdapter() {
			@Override public void focusLost(FocusEvent e) {
				setSignGroup();
			}
		});
		qm_txt.addFocusListener(new FocusAdapter() {
			@Override public void focusLost(FocusEvent e) {
				setQuickMessage();
			}
		});
		pre_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Number h = (Number) pre_spn.getValue();
				setPreAlertHours(h.intValue());
			}
		});
		post_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Number h = (Number) post_spn.getValue();
				setPostAlertHours(h.intValue());
			}
		});
	}

	/** Set the sign group */
	private void setSignGroup() {
		AlertConfig cfg = alert_cfg;
		if (cfg != null)
			cfg.setSignGroup(lookupSignGroup());
	}

	/** Set the quick message */
	private void setQuickMessage() {
		AlertConfig cfg = alert_cfg;
		if (cfg != null)
			cfg.setQuickMessage(lookupQuickMessage());
	}

	/** Set the pre-alert hours */
	private void setPreAlertHours(int h) {
		AlertConfig cfg = alert_cfg;
		if (cfg != null)
			cfg.setPreAlertHours(h);
	}

	/** Set the post-alert hours */
	private void setPostAlertHours(int h) {
		AlertConfig cfg = alert_cfg;
		if (cfg != null)
			cfg.setPostAlertHours(h);
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
		event_act.setEnabled(session.canWrite(cfg, "event"));
		resp_act.setEnabled(session.canWrite(cfg, "responseType"));
		urg_act.setEnabled(session.canWrite(cfg, "urgency"));
		group_txt.setEnabled(session.canWrite(cfg, "signGroup"));
		qm_txt.setEnabled(session.canWrite(cfg, "quickMessage"));
		pre_spn.setEnabled(session.canWrite(cfg, "preAlertHours"));
		post_spn.setEnabled(session.canWrite(cfg, "postAlertHours"));
		auto_deploy_chk.setEnabled(session.canWrite(cfg, "autoDeploy"));
	}
}
