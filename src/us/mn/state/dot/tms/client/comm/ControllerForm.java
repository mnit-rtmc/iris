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
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
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
	static private final int ROW_HEIGHT = 24;

	/** Comm link action */
	private final IAction comm_link = new IAction("comm.link") {
		protected void do_perform() {
			proxy.setCommLink(
				(CommLink)comm_link_cbx.getSelectedItem());
		}
	};

	/** Comm link combo box */
	private final JComboBox comm_link_cbx = new JComboBox();

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
		protected void do_perform() {
			proxy.setPassword(null);
		}
	};

	/** Active checkbox */
	private final JCheckBox active_chk = new JCheckBox(new IAction(null) {
		protected void do_perform() {
			proxy.setActive(active_chk.isSelected());
		}
	});

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Mile point text field */
	private final JTextField mile_txt = new JTextField(10);

	/** Cabinet style action */
	private final IAction cab_style = new IAction("cabinet.style") {
		protected void do_perform() {
			cabinet.setStyle((CabinetStyle)
				cab_style_cbx.getSelectedItem());
		}
	};

	/** Cabinet style combo box */
	private final JComboBox cab_style_cbx = new JComboBox();

	/** Cabinet for controller */
	private final Cabinet cabinet;

	/** Cabinet cache */
	private final TypeCache<Cabinet> cabinets;

	/** Cabinet listener */
	private CabinetListener cab_listener;

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
		protected void do_perform() {
			proxy.setCounters(true);
		}
	};

	/** Reset action */
	private final IAction reset = new IAction("controller.reset") {
		protected void do_perform() {
			proxy.setDownload(true);
		}
	};

	/** Comm Link list model */
	private final ProxyListModel<CommLink> link_model;

	/** Cabinet style list model */
	private final ProxyListModel<CabinetStyle> sty_model;

	/** Create a new controller form */
	public ControllerForm(Session s, Controller c) {
		super(I18N.get("controller") + ": ", s, c);
		ConCache con_cache = state.getConCache();
		link_model = con_cache.getCommLinkModel();
		cabinets = con_cache.getCabinets();
		cabinet = proxy.getCabinet();
		cab_listener = new CabinetListener();
		sty_model = con_cache.getCabinetStyleModel();
		loc_pnl = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	@Override protected TypeCache<Controller> getTypeCache() {
		return state.getConCache().getControllers();
	}

	/** Initialize the widgets on the form */
	@Override protected void initialize() {
		super.initialize();
		io_model = new ControllerIOModel(session, proxy);
		io_model.initialize();
		cabinets.addProxyListener(cab_listener);
		comm_link_cbx.setModel(new WrapperComboBoxModel(link_model,
			false));
		cab_style_cbx.setModel(new WrapperComboBoxModel(sty_model,
			true));
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("device.setup"), createSetupPanel());
		tab.add(I18N.get("cabinet"), createCabinetPanel());
		tab.add(I18N.get("controller.io"), createIOPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
		updateAttribute(null);
		createSetupJobs();
		if(canUpdateCabinet("mile"))
			createCabinetJobs();
		if(!canRequest()) {
			clear_err.setEnabled(false);
			reset.setEnabled(false);
		}
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	@Override protected void dispose() {
		io_model.dispose();
		cabinets.removeProxyListener(cab_listener);
		loc_pnl.dispose();
		super.dispose();
	}

	/** Create the controller setup panel */
	private JPanel createSetupPanel() {
		IPanel p = new IPanel();
		p.add("comm.link");
		p.add(comm_link_cbx, Stretch.LAST);
		p.add("controller.drop");
		p.add(drop_spn, Stretch.LAST);
		p.add("controller.password");
		p.add(password);
		p.add(new JButton(clear_pwd), Stretch.RIGHT);
		p.add("device.notes");
		p.add(notes_txt, Stretch.FULL);
		p.add("controller.active");
		p.add(active_chk, Stretch.LAST);
		return p;
	}

	/** Check if the user can update the cabinet */
	private boolean canUpdateCabinet(String a) {
		return session.canUpdate(cabinet, a);
	}

	/** Can a controller request be made */
	private boolean canRequest() {
		return canUpdate("counters", true) &&
		       canUpdate("download", true);
	}

	/** Create the jobs for the setup panel */
	private void createSetupJobs() {
		if(canUpdate("drop")) {
			drop_spn.addChangeListener(new ChangeJob(WORKER) {
				public void perform() {
					Number n = (Number)drop_spn.getValue();
					proxy.setDrop(n.shortValue());
				}
			});
		}
		if(canUpdate("password")) {
			password.addFocusListener(new FocusLostJob(WORKER) {
				public void perform() {
					String pwd = new String(
						password.getPassword()).trim();
					password.setText("");
					if(pwd.length() > 0)
						proxy.setPassword(pwd);
				}
			});
		} else {
			password.setEnabled(false);
			clear_pwd.setEnabled(false);
		}
		if(canUpdate("notes")) {
			notes_txt.addFocusListener(new FocusLostJob(WORKER) {
				public void perform() {
					proxy.setNotes(notes_txt.getText());
				}
			});
		}
	}

	/** Create the cabinet panel */
	private JPanel createCabinetPanel() {
		loc_pnl.initialize();
		loc_pnl.setGeoLoc(cabinet.getGeoLoc());
		loc_pnl.add("cabinet.milepoint");
		loc_pnl.add(mile_txt, Stretch.LAST);
		loc_pnl.add("cabinet.style");
		loc_pnl.add(cab_style_cbx, Stretch.LAST);
		return loc_pnl;
	}

	/** Create the jobs for the cabinet panel */
	private void createCabinetJobs() {
		mile_txt.addFocusListener(new FocusLostJob(WORKER) {
			public void perform() {
				cabinet.setMile(parseMile());
			}
		});
	}

	/** Parse the mile point number */
	private Float parseMile() {
		try {
			return Float.valueOf(mile_txt.getText());
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** Listener for cabinet proxy changes */
	private class CabinetListener implements ProxyListener<Cabinet> {
		public void proxyAdded(Cabinet p) {}
		public void enumerationComplete() { }
		public void proxyRemoved(Cabinet p) {}
		public void proxyChanged(Cabinet p, final String a) {
			if(p == cabinet)
				updateAttribute(a);
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

	/** Update one attribute on the form */
	@Override protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("commLink")) {
			comm_link_cbx.setAction(null);
			comm_link_cbx.setSelectedItem(proxy.getCommLink());
			comm_link.setEnabled(canUpdate("commLink"));
			comm_link_cbx.setAction(comm_link);
			drop_model = new DropNumberModel(
				proxy.getCommLink(), getTypeCache(),
				proxy.getDrop());
			drop_spn.setModel(drop_model);
		}
		if(a == null || a.equals("drop")) {
			drop_spn.setEnabled(canUpdate("drop"));
			drop_spn.setValue(proxy.getDrop());
		}
		if(a == null || a.equals("notes")) {
			notes_txt.setEnabled(canUpdate("notes"));
			notes_txt.setText(proxy.getNotes());
		}
		if(a == null || a.equals("active")) {
			active_chk.setEnabled(canUpdate("active"));
			active_chk.setSelected(proxy.getActive());
		}
		if(a == null || a.equals("version"))
			version_lbl.setText(proxy.getVersion());
		if(a == null || a.equals("maint"))
			maint_lbl.setText(proxy.getMaint());
		if(a == null || a.equals("status"))
			status_lbl.setText(proxy.getStatus());
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
			mile_txt.setEnabled(canUpdateCabinet("mile"));
			Float m = cabinet.getMile();
			if(m == null)
				mile_txt.setText("");
			else
				mile_txt.setText(m.toString());
		}
		if(a == null || a.equals("style")) {
			cab_style_cbx.setAction(null);
			cab_style_cbx.setSelectedItem(cabinet.getStyle());
			cab_style.setEnabled(canUpdateCabinet("style"));
			cab_style_cbx.setAction(cab_style);
		}
	}
}
