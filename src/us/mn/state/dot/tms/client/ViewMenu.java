/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.client.camera.VideoMenu;
import us.mn.state.dot.tms.client.comm.MaintenanceMenu;
import us.mn.state.dot.tms.client.detector.DetectorForm;
import us.mn.state.dot.tms.client.detector.StationForm;
import us.mn.state.dot.tms.client.dms.SignMenu;
import us.mn.state.dot.tms.client.gate.GateArmArrayForm;
import us.mn.state.dot.tms.client.incident.IncidentMenu;
import us.mn.state.dot.tms.client.lcs.LaneUseMenu;
import us.mn.state.dot.tms.client.meter.RampMeterForm;
import us.mn.state.dot.tms.client.schedule.ScheduleForm;
import us.mn.state.dot.tms.client.system.SystemMenu;
import us.mn.state.dot.tms.client.weather.WeatherSensorForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * ViewMenu is a JMenu which contains items to view various TMS object types.
 *
 * @author Douglas Lau
 */
public class ViewMenu extends IMenu {

	/** User Session */
	private final Session session;

	/** Smart desktop */
	private final SmartDesktop desktop;

	/** Create a new view menu */
	public ViewMenu(Session s) {
		super("view");
		session = s;
		desktop = session.getDesktop();
		addMenu(new SystemMenu(session));
		addMenu(new MaintenanceMenu(session));
		addMenu(new VideoMenu(session));
		addMenu(new SignMenu(session));
		addMenu(new IncidentMenu(session));
		addMenu(new LaneUseMenu(session));
		addItem(createDetectorItem());
		addItem(createStationItem());
		addItem(createMeterItem());
		addItem(createScheduleItem());
		addItem(createWeatherItem());
		addItem(createGateArmItem());
	}

	/** Create a detector menu item action */
	private IAction createDetectorItem() {
		return DetectorForm.isPermitted(session) ?
		    new IAction("detector.plural") {
			protected void doActionPerformed(ActionEvent e){
				desktop.show(new DetectorForm(session));
			}
		    } : null;
	}

	/** Create a station menu item action */
	private IAction createStationItem() {
		return StationForm.isPermitted(session) ?
		    new IAction("detector.station.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new StationForm(session));
			}
		    } : null;
	}

	/** Create a ramp meter menu item action */
	private IAction createMeterItem() {
		return RampMeterForm.isPermitted(session) ?
		    new IAction("ramp_meter.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new RampMeterForm(session));
			}
		    } : null;
	}

	/** Create a schedule menu item action */
	private IAction createScheduleItem() {
		return ScheduleForm.isPermitted(session) ?
		    new IAction("action.plan.schedule.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new ScheduleForm(session));
			}
		    } : null;
	}

	/** Create a weather sensor menu item action */
	private IAction createWeatherItem() {
		return WeatherSensorForm.isPermitted(session) ?
		    new IAction("weather_sensor.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new WeatherSensorForm(session));
			}
		    } : null;
	}

	/** Create a gate arm menu item action */
	private IAction createGateArmItem() {
		return GateArmArrayForm.isPermitted(session) ?
		    new IAction("gate_arm_array.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new GateArmArrayForm(session));
			}
		    } : null;
	}
}
