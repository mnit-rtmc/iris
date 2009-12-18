/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.client.detector.DetectorForm;
import us.mn.state.dot.tms.client.dms.SignMenu;
import us.mn.state.dot.tms.client.marking.LaneMarkingForm;
import us.mn.state.dot.tms.client.meter.RampMeterForm;
import us.mn.state.dot.tms.client.roads.MapExtentForm;
import us.mn.state.dot.tms.client.roads.RoadForm;
import us.mn.state.dot.tms.client.schedule.ScheduleForm;
import us.mn.state.dot.tms.client.security.UserRoleForm;
import us.mn.state.dot.tms.client.system.SystemAttributeForm;
import us.mn.state.dot.tms.client.toast.AlarmForm;
import us.mn.state.dot.tms.client.toast.CabinetStyleForm;
import us.mn.state.dot.tms.client.toast.CommLinkForm;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.warning.WarningSignForm;

/**
 * ViewMenu is a JMenu which contains items to view various TMS object types.
 *
 * @author Douglas Lau
 */
public class ViewMenu extends JMenu {

	/** Session */
	protected final Session session;

	/** Smart desktop */
	protected final SmartDesktop desktop;

	/** SONAR state */
	protected final SonarState state;

	/** SONAR user */
	protected final User user;

	/** Create a new view menu */
	public ViewMenu(Session s) {
		super("View");
		session = s;
		desktop = session.getDesktop();
		state = session.getSonarState();
		user = session.getUser();
		setMnemonic('V');
		JMenuItem item = new JMenuItem("Users/Roles");
		item.setMnemonic('U');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new UserRoleForm(state, user));
			}
		};
		add(item);
		item = new JMenuItem("System Attributes");
		item.setMnemonic('S');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new SystemAttributeForm(state,
					user));
			}
		};
		add(item);
		item = new JMenuItem("Cabinet Styles");
		item.setMnemonic('s');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CabinetStyleForm(state));
			}
		};
		add(item);
		item = new JMenuItem("Comm Links");
		item.setMnemonic('L');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CommLinkForm(session));
			}
		};
		add(item);
		item = new JMenuItem("Alarms");
		item.setMnemonic('a');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new AlarmForm(state.getAlarms()));
			}
		};
		add(item);
		item = new JMenuItem("Roadways", Icons.getIcon("roadway"));
		item.setMnemonic('R');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new RoadForm(state.getRoads(),
					state.getNamespace(), user));
			}
		};
		add(item);
		item = new JMenuItem("Map extents");
		item.setMnemonic('e');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new MapExtentForm(
					state.getMapExtents(),
					state.getNamespace(), user));
			}
		};
		add(item);
		item = new JMenuItem("Detectors", Icons.getIcon("detector"));
		item.setMnemonic('t');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new DetectorForm(
					state.getDetCache().getDetectors()));
			}
		};
		add(item);
		item = new JMenuItem("Plans and Schedules");
		item.setMnemonic('P');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new ScheduleForm(session));
			}
		};
		add(item);
		item = new JMenuItem("Lane Markings");
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new LaneMarkingForm(session,
					state.getLaneMarkings()));
			}
		};
		add(item);
		item = new JMenuItem("Warning Signs");
		item.setMnemonic('W');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new WarningSignForm(session,
					state.getWarningSigns()));
			}
		};
		add(item);
	}

	/** Add the ramp meter menu item */
	public void addMeterItem() {
		JMenuItem item = new JMenuItem("Ramp Meters", Icons.getIcon(
			"meter-inactive"));
		item.setMnemonic('M');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new RampMeterForm(session,
					state.getRampMeters()));
			}
		};
		add(item);
	}
}
