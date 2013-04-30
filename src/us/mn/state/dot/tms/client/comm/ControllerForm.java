/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusLostJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;

/**
 * ControllerForm is a Swing dialog for editing Controller records
 *
 * @author Douglas Lau
 */
public class ControllerForm extends SonarObjectForm<Controller> {

	/** Table row height */
	static protected final int ROW_HEIGHT = 24;

	/** Comm link action */
	private final IAction comm_link = new IAction("comm.link") {
		@Override protected void do_perform() {
			proxy.setCommLink(
				(CommLink)comm_link_cbx.getSelectedItem());
		}
	};

	/** Comm link combo box */
	private final JComboBox comm_link_cbx = new JComboBox();

	/** Model for drop address spinner */
	protected DropNumberModel drop_model;

	/** Drop address spinner */
	protected final JSpinner drop_id = new JSpinner();

	/** Controller notes text */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Access password */
	protected final JPasswordField password = new JPasswordField(16);

	/** Action to clear the access password */
	private final IAction clear_pwd = new IAction(
		"controller.password.clear")
	{
		@Override protected void do_perform() {
			proxy.setPassword(null);
		}
	};

	/** Active checkbox */
	private final JCheckBox active_chk = new JCheckBox(new IAction(null) {
		@Override protected void do_perform() {
			proxy.setActive(active_chk.isSelected());
		}
	});

	/** Location panel */
	private final LocationPanel location;

	/** Mile point text field */
	protected final JTextField mile = new JTextField(10);

	/** Cabinet style action */
	private final IAction cab_style = new IAction("cabinet.style") {
		@Override protected void do_perform() {
			cabinet.setStyle((CabinetStyle)
				cab_style_cbx.getSelectedItem());
		}
	};

	/** Cabinet style combo box */
	private final JComboBox cab_style_cbx = new JComboBox();

	/** Cabinet for controller */
	protected final Cabinet cabinet;

	/** Cabinet cache */
	protected final TypeCache<Cabinet> cabinets;

	/** Cabinet listener */
	protected CabinetListener cab_listener;

	/** Controller IO model */
	protected ControllerIOModel io_model;

	/** Firmware version */
	protected final JLabel version = new JLabel();

	/** Maint status */
	protected final JLabel maint = new JLabel();

	/** Status */
	protected final JLabel status = new JLabel();

	/** Fail time */
	protected final JLabel fail_time_lbl = new JLabel();

	/** Timeout errors label */
	protected final JLabel timeout_lbl = new JLabel();

	/** Checksum errors label */
	protected final JLabel checksum_lbl = new JLabel();

	/** Parsing errors label */
	protected final JLabel parsing_lbl = new JLabel();

	/** Controller errors label */
	protected final JLabel controller_lbl = new JLabel();

	/** Successful operations label */
	protected final JLabel success_lbl = new JLabel();

	/** Failed operations label */
	protected final JLabel failed_lbl = new JLabel();

	/** Clear error status action */
	private final IAction clear_err = new IAction("controller.error.clear"){
		@Override protected void do_perform() {
			proxy.setCounters(true);
		}
	};

	/** Reset action */
	private final IAction reset = new IAction("controller.reset") {
		@Override protected void do_perform() {
			proxy.setDownload(true);
		}
	};

	/** Comm Link list model */
	protected final ProxyListModel<CommLink> link_model;

	/** Cabinet style list model */
	protected final ProxyListModel<CabinetStyle> sty_model;

	/** Create a new controller form */
	public ControllerForm(Session s, Controller c) {
		super(I18N.get("controller") + ": ", s, c);
		ConCache con_cache = state.getConCache();
		link_model = con_cache.getCommLinkModel();
		cabinets = con_cache.getCabinets();
		cabinet = proxy.getCabinet();
		cab_listener = new CabinetListener();
		sty_model = con_cache.getCabinetStyleModel();
		location = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	protected TypeCache<Controller> getTypeCache() {
		return state.getConCache().getControllers();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		io_model = new ControllerIOModel(session, proxy);
		io_model.initialize();
		cabinets.addProxyListener(cab_listener);
		comm_link_cbx.setAction(comm_link);
		comm_link_cbx.setModel(new WrapperComboBoxModel(link_model,
			false));
		cab_style_cbx.setAction(cab_style);
		cab_style_cbx.setModel(new WrapperComboBoxModel(sty_model,
			true));
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("device.setup"), createSetupPanel());
		tab.add(I18N.get("cabinet"), createCabinetPanel());
		tab.add(I18N.get("controller.io"), createIOPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
		updateAttribute(null);
		if(canUpdate())
			createSetupJobs();
		if(canUpdateCabinet())
			createCabinetJobs();
		if(!canRequest()) {
			clear_err.setEnabled(false);
			reset.setEnabled(false);
		}
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		io_model.dispose();
		cabinets.removeProxyListener(cab_listener);
		location.dispose();
		super.dispose();
	}

	/** Create the controller setup panel */
	protected JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(canUpdate());
		panel.add(I18N.get("comm.link"), comm_link_cbx);
		panel.finishRow();
		panel.add(I18N.get("controller.drop"), drop_id);
		panel.finishRow();
		panel.add(I18N.get("controller.password"), password);
		panel.setEast();
		panel.addRow(new JButton(clear_pwd));
		panel.addRow(I18N.get("device.notes"), notes);
		panel.add(I18N.get("controller.active"), active_chk);
		// Add a third column to the grid bag so the drop spinner
		// does not extend across the whole form
		panel.addRow(new javax.swing.JLabel());
		return panel;
	}

	/** Check if the user can activate a controller */
	protected boolean canActivate() {
		return canUpdate("active");
	}

	/** Check if the user can update the cabinet */
	protected boolean canUpdateCabinet() {
		return session.canUpdate(cabinet);
	}

	/** Can a controller request be made */
	protected boolean canRequest() {
		return canUpdate("counters") && canUpdate("download");
	}

	/** Create the jobs for the setup panel */
	protected void createSetupJobs() {
		drop_id.addChangeListener(new ChangeJob(WORKER) {
			@Override public void perform() {
				Number n = (Number)drop_id.getValue();
				proxy.setDrop(n.shortValue());
			}
		});
		password.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				String pwd = new String(
					password.getPassword()).trim();
				password.setText("");
				if(pwd.length() > 0)
					proxy.setPassword(pwd);
			}
		});
		notes.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setNotes(notes.getText());
			}
		});
	}

	/** Create the cabinet panel */
	protected JPanel createCabinetPanel() {
		location.initialize();
		location.setGeoLoc(cabinet.getGeoLoc());
		location.add(I18N.get("cabinet.milepoint"), mile);
		location.finishRow();
		location.add(I18N.get("cabinet.style"), cab_style_cbx);
		location.finishRow();
		return location;
	}

	/** Create the jobs for the cabinet panel */
	protected void createCabinetJobs() {
		mile.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				cabinet.setMile(parseMile());
			}
		});
	}

	/** Parse the mile point number */
	private Float parseMile() {
		try {
			return Float.valueOf(mile.getText());
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** Listener for cabinet proxy changes */
	protected class CabinetListener implements ProxyListener<Cabinet> {
		public void proxyAdded(Cabinet p) {}
		public void enumerationComplete() { }
		public void proxyRemoved(Cabinet p) {}
		public void proxyChanged(Cabinet p, final String a) {
			if(p == cabinet)
				updateAttribute(a);
		}
	}

	/** Create the I/O panel */
	protected JPanel createIOPanel() {
		ZTable table = new ZTable();
		table.setAutoCreateColumnsFromModel(false);
		table.setModel(io_model);
		table.setColumnModel(io_model.createColumnModel());
		table.setRowHeight(ROW_HEIGHT);
		table.setVisibleRowCount(8);
		FormPanel panel = new FormPanel();
		panel.addRow(table);
		return panel;
	}

	/** Create the status panel */
	protected JPanel createStatusPanel() {
		JPanel buttonPnl = new JPanel();
		buttonPnl.add(new JButton(clear_err));
		buttonPnl.add(new JButton(reset));
		FormPanel panel = new FormPanel(canUpdate());
		panel.addRow(I18N.get("controller.version"), version);
		panel.addRow(I18N.get("controller.maint"), maint);
		panel.addRow(I18N.get("controller.status"), status);
		panel.addRow(I18N.get("controller.fail"), fail_time_lbl);
		panel.addRow(I18N.get("controller.err.timeout"), timeout_lbl);
		panel.addRow(I18N.get("controller.err.checksum"), checksum_lbl);
		panel.addRow(I18N.get("controller.err.parsing"), parsing_lbl);
		panel.addRow(I18N.get("controller.err.ctrl"), controller_lbl);
		panel.addRow(I18N.get("controller.ops.good"), success_lbl);
		panel.addRow(I18N.get("controller.ops.bad"), failed_lbl);
		panel.addRow(buttonPnl);
		return panel;
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("commLink")) {
			comm_link_cbx.setEnabled(canUpdate());
			comm_link_cbx.setSelectedItem(proxy.getCommLink());
			drop_model = new DropNumberModel(
				proxy.getCommLink(), getTypeCache(),
				proxy.getDrop());
			drop_id.setModel(drop_model);
		}
		if(a == null || a.equals("drop"))
			drop_id.setValue(proxy.getDrop());
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("active")) {
			active_chk.setEnabled(canActivate());
			active_chk.setSelected(proxy.getActive());
		}
		if(a == null || a.equals("version"))
			version.setText(proxy.getVersion());
		if(a == null || a.equals("maint"))
			maint.setText(proxy.getMaint());
		if(a == null || a.equals("status"))
			status.setText(proxy.getStatus());
		if(a == null || a.equals("failTime")) {
			Long ft = proxy.getFailTime();
			if(ft != null)
				fail_time_lbl.setText(new Date(ft).toString());
			else
				fail_time_lbl.setText("");
		}
		if(a == null || a.equals("timeoutErr")) {
			timeout_lbl.setText(String.valueOf(
				proxy.getTimeoutErr()));
		}
		if(a == null || a.equals("checksumErr")) {
			checksum_lbl.setText(String.valueOf(
				proxy.getChecksumErr()));
		}
		if(a == null || a.equals("parsingErr")) {
			parsing_lbl.setText(String.valueOf(
				proxy.getParsingErr()));
		}
		if(a == null || a.equals("controllerErr")) {
			controller_lbl.setText(String.valueOf(
				proxy.getControllerErr()));
		}
		if(a == null || a.equals("successOps")) {
			success_lbl.setText(String.valueOf(
				proxy.getSuccessOps()));
		}
		if(a == null || a.equals("failedOps")) {
			failed_lbl.setText(String.valueOf(
				proxy.getFailedOps()));
		}
		if(a == null || a.equals("mile")) {
			mile.setEnabled(canUpdateCabinet());
			Float m = cabinet.getMile();
			if(m == null)
				mile.setText("");
			else
				mile.setText(m.toString());
		}
		if(a == null || a.equals("style")) {
			cab_style_cbx.setEnabled(canUpdateCabinet());
			cab_style_cbx.setSelectedItem(cabinet.getStyle());
		}
	}
}
