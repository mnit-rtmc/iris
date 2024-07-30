/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.weather;

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
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WeatherSensorProperties is a dialog for entering and editing weather sensors
 *
 * @author Douglas Lau
 */
public class WeatherSensorProperties extends SonarObjectForm<WeatherSensor> {

	/** Location panel */
	private final LocationPanel loc_pnl;

	/** Site id text area */
	private final JTextArea site_id_txt = new JTextArea(1, 24);

	/** Alt id text area */
	private final JTextArea alt_id_txt = new JTextArea(1, 24);

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(8, 32);

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void doActionPerformed(ActionEvent e) {
			controllerPressed();
		}
	};

	/** Test RWIS level 1 action */
	private final IAction test_rwis_1_act = new IAction(
		"weather_sensor.test.rwis.1")
	{
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(DeviceRequest.
				TEST_RWIS_1.ordinal());
		}
	};

	/** Test RWIS level 2 action */
	private final IAction test_rwis_2_act = new IAction(
		"weather_sensor.test.rwis.2")
	{
		protected void doActionPerformed(ActionEvent e) {
			proxy.setDeviceRequest(DeviceRequest.
				TEST_RWIS_2.ordinal());
		}
	};

	/** Create a new weather sensor properties form */
	public WeatherSensorProperties(Session s, WeatherSensor ws) {
		super(I18N.get("weather_sensor") + ": ", s, ws);
		loc_pnl = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<WeatherSensor> getTypeCache() {
		return state.getWeatherSensors();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
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
		loc_pnl.add("weather_sensor.siteid");
		loc_pnl.add(site_id_txt, Stretch.FULL);
		loc_pnl.add("weather_sensor.altid");
		loc_pnl.add(alt_id_txt, Stretch.FULL);
		loc_pnl.add("device.notes");
		loc_pnl.add(notes_txt, Stretch.FULL);
		loc_pnl.add("weather_sensor.test.rwis");
		loc_pnl.add(new JButton(test_rwis_1_act));
		loc_pnl.add(new JButton(test_rwis_2_act), Stretch.FULL);
		loc_pnl.add(new JButton(controller), Stretch.RIGHT);
		loc_pnl.setGeoLoc(proxy.getGeoLoc());
		return loc_pnl;
	}

	/** Create the widget jobs */
	private void createUpdateJobs() {
		site_id_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setSiteId(site_id_txt.getText());
			}
		});
		alt_id_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setAltId(alt_id_txt.getText());
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

	/** Controller lookup button pressed */
	private void controllerPressed() {
		Controller c = proxy.getController();
		if (c != null)
			showForm(new ControllerForm(session, c));
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		loc_pnl.updateEditMode();
		site_id_txt.setEnabled(canWrite("site_id"));
		alt_id_txt.setEnabled(canWrite("alt_id"));
		notes_txt.setEnabled(canWrite("notes"));
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if (a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if (a == null || a.equals("site_id"))
			site_id_txt.setText(proxy.getSiteId());
		if (a == null || a.equals("alt_id"))
			alt_id_txt.setText(proxy.getAltId());
		if (a == null || a.equals("notes")) {
			String n = proxy.getNotes();
			notes_txt.setText((n != null) ? n : "");
		}
	}
}
