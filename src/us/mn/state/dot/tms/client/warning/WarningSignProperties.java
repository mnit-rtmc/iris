/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.ListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WarningSignProperties is a dialog for entering and editing warning signs 
 *
 * @author Douglas Lau
 */
public class WarningSignProperties extends SonarObjectForm<WarningSign> {

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void doActionPerformed(ActionEvent e) {
			controllerPressed();
		}
	};

	/** Camera action */
	private final IAction camera = new IAction("camera") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setCamera((Camera)camera_cbx.getSelectedItem());
		}
	};

	/** Camera combo box */
	private final JComboBox camera_cbx = new JComboBox();

	/** Sign message text area */
	private final JTextArea message_txt = new JTextArea(3, 24);

	/** Create a new warning sign form */
	public WarningSignProperties(Session s, WarningSign ws) {
		super(I18N.get("warning.sign") + ": ", s, ws);
		loc_pnl = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<WarningSign> getTypeCache() {
		return state.getWarningSigns();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("device.setup"), createSetupPanel());
		add(tab);
		if(canUpdate())
			createUpdateJobs();
		super.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		loc_pnl.dispose();
		super.dispose();
	}

	/** Create the location panel */
	private JPanel createLocationPanel() {
		loc_pnl.initialize();
		loc_pnl.add("device.notes");
		loc_pnl.add(notes_txt, Stretch.FULL);
		loc_pnl.add(new JButton(controller), Stretch.RIGHT);
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		return loc_pnl;
	}

	/** Create jobs for updating */
	private void createUpdateJobs() {
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setNotes(notes_txt.getText());
			}
		});
		message_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setMessage(message_txt.getText());
			}
		});
	}

	/** Controller lookup button pressed */
	private void controllerPressed() {
		Controller c = proxy.getController();
		if(c != null)
			showForm(new ControllerForm(session, c));
	}

	/** Create the setup panel */
	private JPanel createSetupPanel() {
		ListModel m = state.getCamCache().getCameraModel();
		camera_cbx.setModel(new WrapperComboBoxModel(m));
		IPanel p = new IPanel();
		p.add("camera");
		p.add(camera_cbx, Stretch.LAST);
		p.add("warning.sign.text");
		p.add(message_txt, Stretch.LAST);
		return p;
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if(a == null || a.equals("notes")) {
			notes_txt.setEnabled(canUpdate("notes"));
			notes_txt.setText(proxy.getNotes());
		}
		if(a == null || a.equals("camera")) {
			camera_cbx.setAction(null);
			camera_cbx.setSelectedItem(proxy.getCamera());
			camera.setEnabled(canUpdate("camera"));
			camera_cbx.setAction(camera);
		}
		if(a == null || a.equals("message")) {
			message_txt.setEnabled(canUpdate("message"));
			message_txt.setText(proxy.getMessage());
		}
	}
}
