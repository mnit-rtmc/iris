/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toll;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * TagReaderProperties is a dialog for entering and editing tag readers.
 *
 * @author Douglas Lau
 */
public class TagReaderProperties extends SonarObjectForm<TagReader> {

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void doActionPerformed(ActionEvent e) {
			Controller c = proxy.getController();
			if (c != null)
				showForm(new ControllerForm(session, c));
		}
	};

	/** Status panel */
	private final IPanel status_pnl;

	/** Send settings action */
	private final IAction settings = new IAction("device.send.settings") {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** Create a new tag reader properties form */
	public TagReaderProperties(Session s, TagReader tr) {
		super(I18N.get("tag_reader") + ": ", s, tr);
		loc_pnl = new LocationPanel(s);
		status_pnl = new IPanel();
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<TagReader> getTypeCache() {
		return state.getTagReaders();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		add(tab);
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

	/** Create the status panel */
	private IPanel createStatusPanel() {
		status_pnl.initialize();
		status_pnl.add(new JButton(settings), Stretch.RIGHT);
		return status_pnl;
	}

	/** Create the widget jobs */
	private void createUpdateJobs() {
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setNotes(notes_txt.getText());
			}
		});
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		notes_txt.setEnabled(canUpdate("notes"));
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if (a == null || a.equals("notes"))
			notes_txt.setText(proxy.getNotes());
		if (a == null) {
			boolean r = canRequest();
			settings.setEnabled(r);
		}
	}

	/** Check if the user can make device requests */
	private boolean canRequest() {
		return isUpdatePermitted("deviceRequest");
	}
}
