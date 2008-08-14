/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.rmi.RemoteException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.utils.TMSProxy;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * ControllerForm is a Swing dialog for editing Controller records
 *
 * @author Douglas Lau
 */
public class ControllerForm extends SonarObjectForm<Controller> {

	/** Table row height */
	static protected final int ROW_HEIGHT = 24;

	/** Frame title */
	static protected final String TITLE = "Controller: ";

	/** Comm link combo box */
	protected final JComboBox comm_link = new JComboBox();

	/** Model for drop address spinner */
	protected DropNumberModel drop_model;

	/** Drop address spinner */
	protected final JSpinner drop_id = new JSpinner();

	/** Controller notes text */
	protected final JTextArea notes = new JTextArea();

	/** Active checkbox */
	protected final JCheckBox active = new JCheckBox();

	/** Location panel */
	protected LocationPanel location;

	/** Mile point text field */
	protected final JTextField mile = new JTextField(10);

	/** Cabinet style combo box */
	protected final JComboBox cab_style = new JComboBox();

	/** Cabinet for controller */
	protected final Cabinet cabinet;

	/** Cabinet cache */
	protected final TypeCache<Cabinet> cabinets;

	/** Cabinet listener */
	protected CabinetListener cab_listener;

	/** Controller IO model */
	protected ControllerIOModel io_model;

	/** Status */
	protected final JLabel status = new JLabel();

	/** Error detail */
	protected final JLabel error = new JLabel();

	/** Firmware version */
	protected final JLabel version = new JLabel();

	/** Download button */
	protected final JButton download = new JButton("Download");

	/** Reset button */
	protected final JButton reset = new JButton("Reset");

	/** Comm Link list model */
	protected final ProxyListModel<CommLink> link_model;

	/** Cabinet style list model */
	protected final ProxyListModel<CabinetStyle> sty_model;

	/** Create a new controller form */
	public ControllerForm(TmsConnection tc, Controller c) {
		super(TITLE, tc, c);
		SonarState state = tc.getSonarState();
		TypeCache<CommLink> links = state.getCommLinks();
		link_model = new ProxyListModel<CommLink>(links);
		cabinets = state.getCabinets();
		cabinet = proxy.getCabinet();
		cab_listener = new CabinetListener();
		TypeCache<CabinetStyle> styles = state.getCabinetStyles();
		sty_model = new ProxyListModel<CabinetStyle>(styles);
	}

	/** Get the SONAR type cache */
	protected TypeCache<Controller> getTypeCache(SonarState st) {
		return st.getControllers();
	}

	/** Get the ControllerIO array */
	protected ControllerIO[] getControllerIO() {
		Integer[] cio = proxy.getCio();
		ControllerIO[] io = new ControllerIO[cio.length];
		TMSProxy tms = connection.getProxy();
		for(int i = 0; i < cio.length; i++) {
			if(cio[i] != null)
				io[i] = (ControllerIO)tms.getTMSObject(cio[i]);
		}
		return io;
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		super.initialize();
		io_model = new ControllerIOModel(proxy, connection.getProxy());
		cabinets.addProxyListener(cab_listener);
		link_model.initialize();
		sty_model.initialize();
		comm_link.setModel(new WrapperComboBoxModel(link_model, false));
		cab_style.setModel(new WrapperComboBoxModel(sty_model, true));
		JTabbedPane tab = new JTabbedPane();
		tab.add("Setup", createSetupPanel());
		tab.add("Cabinet", createCabinetPanel());
		tab.add("I/O", createIOPanel());
		tab.add("Status", createStatusPanel());
		add(tab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		cabinets.removeProxyListener(cab_listener);
		link_model.dispose();
		sty_model.dispose();
		location.dispose();
		super.dispose();
	}

	/** Create the controller setup panel */
	protected JPanel createSetupPanel() {
		FormPanel panel = new FormPanel(admin);
		panel.addRow("Comm Link", comm_link);
		new ActionJob(this, comm_link) {
			public void perform() {
				proxy.setCommLink(
					(CommLink)comm_link.getSelectedItem());
			}
		};
		panel.addRow("Drop", drop_id);
		new ChangeJob(this, drop_id) {
			public void perform() {
				Number n = (Number)drop_id.getValue();
				proxy.setDrop(n.shortValue());
			}
		};
		panel.addRow("Notes", notes);
		new FocusJob(notes) {
			public void perform() {
				if(wasLost())
					proxy.setNotes(notes.getText());
			}
		};
		panel.add("Active", active);
		// Add a third column to the grid bag so the drop spinner
		// does not extend across the whole form
		panel.addRow(new javax.swing.JLabel());
		active.setEnabled(connection.isAdmin() ||
			connection.isActivate());
		new ActionJob(this, active) {
			public void perform() {
				proxy.setActive(active.isSelected());
			}
		};
		return panel;
	}

	/** Create the cabinet panel */
	protected JPanel createCabinetPanel() {
		location = new LocationPanel(admin, cabinet.getGeoLoc(),
			connection.getSonarState());
		location.initialize();
		location.addRow("Milepoint", mile);
		new FocusJob(mile) {
			public void perform() {
				if(wasLost()) {
					String ms = mile.getText();
					try {
						Float m = Float.valueOf(ms);
						cabinet.setMile(m);
					}
					catch(NumberFormatException e) {
						cabinet.setMile(null);
					}
				}
			}
		};
		location.addRow("Style", cab_style);
		new ActionJob(this, cab_style) {
			public void perform() {
				cabinet.setStyle((CabinetStyle)
					cab_style.getSelectedItem());
			}
		};
		return location;
	}

	/** Listener for cabinet proxy changes */
	protected class CabinetListener implements ProxyListener<Cabinet> {
		public void proxyAdded(Cabinet p) {}
		public void proxyRemoved(Cabinet p) {}
		public void proxyChanged(Cabinet p, final String a) {
			if(p == cabinet)
				updateAttribute(a);
		}
	}

	/** Create the I/O panel */
	protected JPanel createIOPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BORDER);
		JTable table = new JTable();
		table.setAutoCreateColumnsFromModel(false);
		table.setModel(io_model);
		table.setColumnModel(io_model.createColumnModel());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(ROW_HEIGHT);
		table.setPreferredScrollableViewportSize(new Dimension(
			table.getPreferredSize().width, ROW_HEIGHT * 8));
		JScrollPane pane = new JScrollPane(table);
		panel.add(pane);
		return panel;
	}

	/** Create the status panel */
	protected JPanel createStatusPanel() {
		FormPanel panel = new FormPanel(admin);
		panel.addRow("Status:", status);
		panel.addRow("Error Detail:", error);
		panel.addRow("Version:", version);
		panel.add(download);
		new ActionJob(this, download) {
			public void perform() {
				proxy.setDownload(false);
			}
		};
		panel.setCenter();
		panel.add(reset);
		new ActionJob(this, reset) {
			public void perform() {
				proxy.setDownload(true);
			}
		};
		return panel;
	}

	/** Update one attribute on the form */
	protected void updateAttribute(String a) {
		if(a == null || a.equals("commLink")) {
			comm_link.setSelectedItem(proxy.getCommLink());
			drop_model = new DropNumberModel(
				proxy.getCommLink(), getTypeCache(
				connection.getSonarState()), proxy.getDrop());
			drop_id.setModel(drop_model);
		}
		if(a == null || a.equals("drop"))
			drop_id.setValue(proxy.getDrop());
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("active"))
			active.setSelected(proxy.getActive());
		if(a == null || a.equals("status"))
			status.setText(proxy.getStatus());
		if(a == null || a.equals("error"))
			error.setText(proxy.getError());
		if(a == null || a.equals("version"))
			version.setText(proxy.getVersion());
		if(a == null || a.equals("mile")) {
			Float m = cabinet.getMile();
			if(m == null)
				mile.setText("");
			else
				mile.setText(m.toString());
		}
		if(a == null || a.equals("style"))
			cab_style.setSelectedItem(cabinet.getStyle());
		if(a == null || a.equals("cio"))
			io_model.setCio(getControllerIO());
	}
}
