/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.CtrlCondition;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;

/**
 * ControllerForm is a Swing dialog for editing Controller records
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class ControllerForm extends SonarObjectForm<Controller> {

	/** Table row height */
	static private final int ROW_HEIGHT = 24;

	/** Comm link combo box model */
	private final IComboBoxModel<CommLink> comm_link_mdl;

	/** Comm link action */
	private final IAction comm_link_act = new IAction("comm.link") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setCommLink(comm_link_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			updateCommLink();
		}
	};

	/** Comm link combo box */
	private final JComboBox<CommLink> comm_link_cbx =
		new JComboBox<CommLink>();

	/** Comm link URI label */
	private final JLabel uri_lbl = new JLabel();

	/** Model for drop address spinner */
	private DropNumberModel drop_model;

	/** Drop address spinner */
	private final JSpinner drop_spn = new JSpinner();

	/** Controller notes text */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Access password */
	private final JPasswordField password = new JPasswordField(16);

	/** Action to clear the access password */
	private final IAction clear_pwd = new IAction(
		"controller.password.clear")
	{
		protected void doActionPerformed(ActionEvent e) {
			proxy.setPassword(null);
		}
	};

	/** Condition action */
	private final IAction condition_act = new IAction(
		"controller.condition")
	{
		protected void doActionPerformed(ActionEvent e) {
			proxy.setCondition(condition_cbx.getSelectedIndex());
		}
		@Override
		protected void doUpdateSelected() {
			condition_cbx.setSelectedIndex(proxy.getCondition());
		}
	};

	/** Condition combobox */
	private final JComboBox<CtrlCondition> condition_cbx =
		new JComboBox<CtrlCondition>(CtrlCondition.values());

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Cabinet style combo box model */
	private final IComboBoxModel<CabinetStyle> cab_style_mdl;

	/** Cabinet style action */
	private final IAction cab_style_act = new IAction("cabinet.style") {
		protected void doActionPerformed(ActionEvent e) {
			cabinet.setStyle(cab_style_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			cab_style_mdl.setSelectedItem((cabinet != null)
			                             ? cabinet.getStyle()
			                             : null);
		}
	};

	/** Cabinet style combo box */
	private final JComboBox<CabinetStyle> cab_style_cbx =
		new JComboBox<CabinetStyle>();

	/** Cabinet for controller */
	private final Cabinet cabinet;

	/** Cabinet cache */
	private final TypeCache<Cabinet> cabinets;

	/** Cabinet listener */
	private final CabinetListener cab_listener;

	/** Controller IO model */
	private ControllerIOModel io_model;

	/** Firmware version */
	private final JLabel version_lbl = IPanel.createValueLabel();

	/** Maint status */
	private final JLabel maint_lbl = IPanel.createValueLabel();

	/** Status */
	private final JLabel status_lbl = IPanel.createValueLabel();

	/** Fail time */
	private final JLabel fail_time_lbl = IPanel.createValueLabel();

	/** Timeout errors label */
	private final JLabel timeout_lbl = IPanel.createValueLabel();

	/** Checksum errors label */
	private final JLabel checksum_lbl = IPanel.createValueLabel();

	/** Parsing errors label */
	private final JLabel parsing_lbl = IPanel.createValueLabel();

	/** Controller errors label */
	private final JLabel controller_lbl = IPanel.createValueLabel();

	/** Successful operations label */
	private final JLabel success_lbl = IPanel.createValueLabel();

	/** Failed operations label */
	private final JLabel failed_lbl = IPanel.createValueLabel();

	/** Clear error status action */
	private final IAction clear_err = new IAction("controller.error.clear"){
		protected void doActionPerformed(ActionEvent e) {
			proxy.setCounters(true);
		}
	};

	/** Reset action */
	private final IAction reset = new IAction("controller.reset") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDownload(true);
		}
	};

	/** Create a new controller form */
	public ControllerForm(Session s, Controller c) {
		super(I18N.get("controller") + ": ", s, c);
		ConCache con_cache = state.getConCache();
		cabinets = con_cache.getCabinets();
		cabinet = proxy.getCabinet();
		cab_listener = new CabinetListener();
		loc_pnl = new LocationPanel(s);
		comm_link_mdl = new IComboBoxModel<CommLink>(
			con_cache.getCommLinkModel(), false);
		cab_style_mdl = new IComboBoxModel<CabinetStyle>(
			con_cache.getCabinetStyleModel());
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<Controller> getTypeCache() {
		return state.getConCache().getControllers();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		io_model = new ControllerIOModel(session, proxy);
		io_model.initialize();
		cabinets.addProxyListener(cab_listener);
		comm_link_cbx.setModel(comm_link_mdl);
		comm_link_cbx.setAction(comm_link_act);
		cab_style_cbx.setModel(cab_style_mdl);
		cab_style_cbx.setAction(cab_style_act);
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("device.setup"), createSetupPanel());
		tab.add(I18N.get("cabinet"), createCabinetPanel());
		tab.add(I18N.get("controller.io"), createIOPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
		createSetupJobs();
		if (!canRequest()) {
			clear_err.setEnabled(false);
			reset.setEnabled(false);
		}
		setBackground(Color.LIGHT_GRAY);
		super.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		io_model.dispose();
		cabinets.removeProxyListener(cab_listener);
		loc_pnl.dispose();
		super.dispose();
	}

	/** Create the controller setup panel */
	private JPanel createSetupPanel() {
		condition_cbx.setAction(condition_act);
		IPanel p = new IPanel();
		p.add("comm.link");
		p.add(comm_link_cbx);
		p.add(uri_lbl, Stretch.LAST);
		p.add("controller.drop");
		p.add(drop_spn, Stretch.LAST);
		p.add("controller.password");
		p.add(password);
		p.add(new JButton(clear_pwd), Stretch.RIGHT);
		p.add("device.notes");
		p.add(notes_txt, Stretch.FULL);
		p.add("controller.condition");
		p.add(condition_cbx, Stretch.LAST);
		return p;
	}

	/** Check if the user can write the cabinet */
	private boolean canWriteCabinet(String a) {
		return session.canWrite(cabinet, a);
	}

	/** Can a controller request be made */
	private boolean canRequest() {
		return isWritePermitted("counters") &&
		       isWritePermitted("download");
	}

	/** Create the jobs for the setup panel */
	private void createSetupJobs() {
		drop_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Number n = (Number)drop_spn.getValue();
				proxy.setDrop(n.shortValue());
			}
		});
		password.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String pwd = new String(
					password.getPassword()).trim();
				password.setText("");
				if (pwd.length() > 0)
					proxy.setPassword(pwd);
			}
		});
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setNotes(notes_txt.getText());
			}
		});
	}

	/** Create the cabinet panel */
	private JPanel createCabinetPanel() {
		loc_pnl.initialize();
		loc_pnl.add("cabinet.style");
		loc_pnl.add(cab_style_cbx, Stretch.LAST);
		loc_pnl.setGeoLoc((cabinet != null)
		                 ? cabinet.getGeoLoc()
		                 : null);
		return loc_pnl;
	}

	/** Listener for cabinet proxy changes */
	private class CabinetListener implements ProxyListener<Cabinet> {
		public void proxyAdded(Cabinet p) {}
		public void enumerationComplete() { }
		public void proxyRemoved(Cabinet p) {}
		public void proxyChanged(Cabinet p, final String a) {
			if (p == cabinet)
				doUpdateAttribute(a);
		}
	}

	/** Create the I/O panel */
	private JPanel createIOPanel() {
		ZTable table = new ZTable();
		table.setAutoCreateColumnsFromModel(false);
		table.setModel(io_model);
		table.setColumnModel(io_model.createColumnModel());
		table.setRowHeight(ROW_HEIGHT);
		table.setVisibleRowCount(8);
		IPanel p = new IPanel();
		p.add(table, Stretch.FULL);
		return p;
	}

	/** Create the status panel */
	private JPanel createStatusPanel() {
		JPanel buttonPnl = new JPanel();
		buttonPnl.add(new JButton(clear_err));
		buttonPnl.add(new JButton(reset));
		IPanel p = new IPanel();
		p.add("controller.version");
		p.add(version_lbl, Stretch.LAST);
		p.add("controller.maint");
		p.add(maint_lbl, Stretch.LAST);
		p.add("controller.status");
		p.add(status_lbl, Stretch.LAST);
		p.add("controller.fail");
		p.add(fail_time_lbl, Stretch.LAST);
		p.add("controller.err.timeout");
		p.add(timeout_lbl, Stretch.LAST);
		p.add("controller.err.checksum");
		p.add(checksum_lbl, Stretch.LAST);
		p.add("controller.err.parsing");
		p.add(parsing_lbl, Stretch.LAST);
		p.add("controller.err.ctrl");
		p.add(controller_lbl, Stretch.LAST);
		p.add("controller.ops.good");
		p.add(success_lbl, Stretch.LAST);
		p.add("controller.ops.bad");
		p.add(failed_lbl, Stretch.LAST);
		p.add(buttonPnl, Stretch.RIGHT);
		return p;
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		password.setEnabled(canWrite("password"));
		clear_pwd.setEnabled(canWrite("password"));
		comm_link_act.setEnabled(canWrite("commLink"));
		drop_spn.setEnabled(canWrite("drop"));
		notes_txt.setEnabled(canWrite("notes"));
		condition_act.setEnabled(canWrite("condition"));
		cab_style_act.setEnabled(canWriteCabinet("style"));
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("commLink")) {
			updateCommLink();
			drop_model = new DropNumberModel(
				proxy.getCommLink(), getTypeCache(),
				proxy.getDrop());
			drop_spn.setModel(drop_model);
		}
		if (a == null || a.equals("drop"))
			drop_spn.setValue(proxy.getDrop());
		if (a == null || a.equals("notes"))
			notes_txt.setText(proxy.getNotes());
		if (a == null || a.equals("condition"))
			condition_act.updateSelected();
		if (a == null || a.equals("version"))
			version_lbl.setText(proxy.getVersion());
		if (a == null || a.equals("maint"))
			maint_lbl.setText(proxy.getMaint());
		if (a == null || a.equals("status"))
			status_lbl.setText(proxy.getStatus());
		if (a == null || a.equals("failTime")) {
			Long ft = proxy.getFailTime();
			if (ft != null)
				fail_time_lbl.setText(new Date(ft).toString());
			else
				fail_time_lbl.setText("");
		}
		if (a == null || a.equals("timeoutErr")) {
			timeout_lbl.setText(String.valueOf(
				proxy.getTimeoutErr()));
		}
		if (a == null || a.equals("checksumErr")) {
			checksum_lbl.setText(String.valueOf(
				proxy.getChecksumErr()));
		}
		if (a == null || a.equals("parsingErr")) {
			parsing_lbl.setText(String.valueOf(
				proxy.getParsingErr()));
		}
		if (a == null || a.equals("controllerErr")) {
			controller_lbl.setText(String.valueOf(
				proxy.getControllerErr()));
		}
		if (a == null || a.equals("successOps")) {
			success_lbl.setText(String.valueOf(
				proxy.getSuccessOps()));
		}
		if (a == null || a.equals("failedOps")) {
			failed_lbl.setText(String.valueOf(
				proxy.getFailedOps()));
		}
		if (a == null || a.equals("style"))
			cab_style_act.updateSelected();
	}

	/** Update the comm link */
	private void updateCommLink() {
		CommLink cl = proxy.getCommLink();
		comm_link_mdl.setSelectedItem(cl);
		if (cl != null)
			uri_lbl.setText(cl.getUri());
		else
			uri_lbl.setText("");
	}
}
