/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.camera.VideoMenu;
import us.mn.state.dot.tms.client.comm.MaintenanceMenu;
import us.mn.state.dot.tms.client.detector.DetectorForm;
import us.mn.state.dot.tms.client.detector.StationForm;
import us.mn.state.dot.tms.client.dms.SignMenu;
import us.mn.state.dot.tms.client.gate.GateArmForm;
import us.mn.state.dot.tms.client.lcs.LaneUseMenu;
import us.mn.state.dot.tms.client.meter.RampMeterForm;
import us.mn.state.dot.tms.client.schedule.ScheduleForm;
import us.mn.state.dot.tms.client.system.SystemMenu;
import us.mn.state.dot.tms.client.weather.WeatherSensorForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * ViewMenu is a JMenu which contains items to view various TMS object types.
 *
 * @author Douglas Lau
 */
public class ViewMenu extends JMenu {

	/** User Session */
	protected final Session session;

	/** Smart desktop */
	protected final SmartDesktop desktop;

	/** Create a new view menu */
	public ViewMenu(Session s) {
		super(I18N.get("view"));
		session = s;
		desktop = session.getDesktop();
		SystemMenu s_menu = new SystemMenu(session);
		if(s_menu.getItemCount() > 0)
			add(s_menu);
		MaintenanceMenu m_menu = new MaintenanceMenu(session);
		if(m_menu.getItemCount() > 0)
			add(m_menu);
		VideoMenu vid_menu = new VideoMenu(session);
		if(vid_menu.getItemCount() > 0)
			add(vid_menu);
		SignMenu sgn_menu = new SignMenu(session);
		if(sgn_menu.getItemCount() > 0)
			add(sgn_menu);
		LaneUseMenu lu_menu = new LaneUseMenu(session);
		if(lu_menu.getItemCount() > 0)
			add(lu_menu);
		JMenuItem item = createDetectorItem();
		if(item != null)
			add(item);
		item = createStationItem();
		if(item != null)
			add(item);
		item = createMeterItem();
		if(item != null)
			add(item);
		item = createScheduleItem();
		if(item != null)
			add(item);
		item = createWeatherItem();
		if(item != null)
			add(item);
		item = createGateArmItem();
		if(item != null)
			add(item);
	}

	/** Create the detector menu item */
	protected JMenuItem createDetectorItem() {
		if(DetectorForm.isPermitted(session)) {
			return new JMenuItem(new IAction("detector.plural") {
				protected void do_perform() {
					desktop.show(new DetectorForm(session));
				}
			});
		} else
			return null;
	}

	/** Create the station menu item */
	protected JMenuItem createStationItem() {
		if(!StationForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("detector.station.plural") {
			protected void do_perform() {
				desktop.show(new StationForm(session));
			}
		});
	}

	/** Create the ramp meter menu item */
	protected JMenuItem createMeterItem() {
		if(!RampMeterForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("ramp.meter.long.plural") {
			protected void do_perform() {
				desktop.show(new RampMeterForm(session));
			}
		});
	}

	/** Create the schedule menu item */
	protected JMenuItem createScheduleItem() {
		if(!ScheduleForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("action.plan.schedule.title") {
			protected void do_perform() {
				desktop.show(new ScheduleForm(session));
			}
		});
	}

	/** Create the weather sensor menu item */
	protected JMenuItem createWeatherItem() {
		if(!WeatherSensorForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("weather.sensors") {
			protected void do_perform() {
				desktop.show(new WeatherSensorForm(session));
			}
		});
	}

	/** Create the gate arm menu item */
	private JMenuItem createGateArmItem() {
		if(!GateArmForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("gate.arms") {
			protected void do_perform() {
				desktop.show(new GateArmForm(session));
			}
		});
	}
}
