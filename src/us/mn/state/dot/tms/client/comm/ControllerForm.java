/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2025  Minnesota Department of Transportation
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.CtrlCondition;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.client.Session;
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
	private final JPasswordField password = new JPasswordField();

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
			proxy.setCabinetStyle(cab_style_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			cab_style_mdl.setSelectedItem(proxy.getCabinetStyle());
		}
	};

	/** Cabinet style combo box */
	private final JComboBox<CabinetStyle> cab_style_cbx =
		new JComboBox<CabinetStyle>();

	/** Controller IO model */
	private ControllerIOModel io_model;

	/** Firmware version */
	private final JLabel version_lbl = IPanel.createValueLabel();

	/** Fault label */
	private final JLabel fault_lbl = IPanel.createValueLabel();

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
			proxy.setDeviceRequest(
				DeviceRequest.RESET_STATUS.ordinal());
		}
	};

	/** Send settings action */
	private final IAction settings = new IAction("device.send.settings") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(
				DeviceRequest.SEND_SETTINGS.ordinal());
		}
	};

	/** Reset action */
	private final IAction reset = new IAction("controller.reset") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(
				DeviceRequest.RESET_DEVICE.ordinal());
		}
	};

	/** Create a new controller form */
	public ControllerForm(Session s, Controller c) {
		super(I18N.get("controller") + ": ", s, c);
		ConCache con_cache = state.getConCache();
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
		comm_link_cbx.setModel(comm_link_mdl);
		comm_link_cbx.setAction(comm_link_act);
		cab_style_cbx.setModel(cab_style_mdl);
		cab_style_cbx.setAction(cab_style_act);
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("device.setup"), createSetupPanel());
		tab.add(I18N.get("location"), createLocPanel());
		tab.add(I18N.get("controller.io"), createIOPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
		createSetupJobs();
		if (!canRequest()) {
			clear_err.setEnabled(false);
			settings.setEnabled(false);
			reset.setEnabled(false);
		}
		setBackground(Color.LIGHT_GRAY);
		super.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		io_model.dispose();
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
		p.add(password, Stretch.WIDE);
		p.add(new JButton(clear_pwd), Stretch.RIGHT);
		p.add("device.notes");
		p.add(notes_txt, Stretch.FULL);
		p.add("controller.condition");
		p.add(condition_cbx, Stretch.LAST);
		return p;
	}

	/** Can a controller request be made */
	private boolean canRequest() {
		return isWritePermitted("deviceRequest");
	}

	/** Create the jobs for the setup panel */
	private void createSetupJobs() {
		drop_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Number n = (Number) drop_spn.getValue();
				proxy.setDrop(n.intValue());
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
				String n = notes_txt.getText().trim();
				proxy.setNotes((n.length() > 0) ? n : null);
			}
		});
	}

	/** Create the location panel */
	private JPanel createLocPanel() {
		loc_pnl.initialize();
		loc_pnl.add("cabinet.style");
		loc_pnl.add(cab_style_cbx, Stretch.LAST);
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		return loc_pnl;
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
		buttonPnl.add(new JButton(settings));
		buttonPnl.add(new JButton(reset));
		IPanel p = new IPanel();
		p.add("controller.version");
		p.add(version_lbl, Stretch.LAST);
		p.add("controller.fault");
		p.add(fault_lbl, Stretch.LAST);
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
		comm_link_act.setEnabled(canWrite("commLink"));
		drop_spn.setEnabled(canWrite("drop"));
		cab_style_act.setEnabled(canWrite("cabinetStyle"));
		notes_txt.setEnabled(canWrite("notes"));
		condition_act.setEnabled(canWrite("condition"));
		password.setEnabled(canWrite("password"));
		clear_pwd.setEnabled(canWrite("password"));
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
		if (a == null || a.equals("cabinetStyle"))
			cab_style_act.updateSelected();
		if (a == null || a.equals("notes")) {
			String n = proxy.getNotes();
			notes_txt.setText((n != null) ? n : "");
		}
		if (a == null || a.equals("condition"))
			condition_act.updateSelected();
		if (a == null || a.equals("setup")) {
			String v = ControllerHelper.getSetup(proxy, "version");
			version_lbl.setText(v);
		}
		if (a == null || a.equals("status")) {
			String f = ControllerHelper.optFaults(proxy);
			fault_lbl.setText((f != null) ? f : "");
		}
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
