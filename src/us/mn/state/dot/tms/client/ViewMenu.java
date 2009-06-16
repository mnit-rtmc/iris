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
import us.mn.state.dot.tms.client.camera.CameraForm;
import us.mn.state.dot.tms.client.camera.VideoMonitorForm;
import us.mn.state.dot.tms.client.dms.DMSForm;
import us.mn.state.dot.tms.client.dms.FontForm;
import us.mn.state.dot.tms.client.lcs.GraphicForm;
import us.mn.state.dot.tms.client.lcs.LcsForm;
import us.mn.state.dot.tms.client.meter.RampMeterForm;
import us.mn.state.dot.tms.client.roads.RoadForm;
import us.mn.state.dot.tms.client.schedule.HolidayForm;
import us.mn.state.dot.tms.client.security.UserRoleForm;
import us.mn.state.dot.tms.client.system.SystemAttributeForm;
import us.mn.state.dot.tms.client.toast.AlarmForm;
import us.mn.state.dot.tms.client.toast.CabinetStyleForm;
import us.mn.state.dot.tms.client.toast.CommLinkForm;
import us.mn.state.dot.tms.client.toast.DetectorForm;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.warning.WarningSignForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * ViewMenu is a JMenu which contains items to view various TMS object types.
 *
 * @author Douglas Lau
 */
public class ViewMenu extends JMenu {

	/** Create a new view menu */
	public ViewMenu(final Session s) {
		super("View");
		final SmartDesktop desktop = s.getDesktop();
		final SonarState state = s.getSonarState();
		final User user = s.getUser();
		setMnemonic('V');
		JMenuItem item = new JMenuItem("Users/Roles");
		item.setMnemonic('U');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new UserRoleForm(state.getUsers(),
					state.getRoles(),
					state.getConnections()));
			}
		};
		add(item);
		item = new JMenuItem("System Attributes");
		item.setMnemonic('S');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new SystemAttributeForm(
					state.getSystemAttributes(), user));
			}
		};
		add(item);
		item = new JMenuItem("Cabinet Styles");
		item.setMnemonic('s');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CabinetStyleForm(
					state.getCabinetStyles()));
			}
		};
		add(item);
		item = new JMenuItem("Comm Links");
		item.setMnemonic('L');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CommLinkForm(s,
					state.getCommLinks(),
					state.getControllers()));
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
				desktop.show(new RoadForm(state.getRoads()));
			}
		};
		add(item);
		item = new JMenuItem("Detectors", Icons.getIcon("detector"));
		item.setMnemonic('t');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new DetectorForm(
					state.getDetectors()));
			}
		};
		add(item);
		item = new JMenuItem("Ramp Meters", Icons.getIcon(
			"meter-inactive"));
		item.setMnemonic('M');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new RampMeterForm(s,
					state.getRampMeters()));
			}
		};
		add(item);
		item = new JMenuItem("Holidays");
		item.setMnemonic('H');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new HolidayForm(
					state.getHolidays()));
			}
		};
		add(item);

		String dms_name = I18N.get("dms.abbreviation");
		item = new JMenuItem(dms_name, Icons.getIcon("drum-inactive"));
		if(dms_name.length() > 0)
			item.setMnemonic(dms_name.charAt(0));

		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new DMSForm(s));
			}
		};
		add(item);
		item = new JMenuItem("Fonts");
		item.setMnemonic('F');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new FontForm(state));
			}
		};
		add(item);
		item = new JMenuItem("Cameras");
		item.setMnemonic('C');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CameraForm(s,
					state.getCameras()));
			}
		};
		add(item);
		item = new JMenuItem("LCS", Icons.getIcon(
			"lanecontrol-inactive"));
		item.setMnemonic('L');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new LcsForm(s));
			}
		};
		add(item);
		item = new JMenuItem("Graphics");
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new GraphicForm(s,
					state.getGraphics()));
			}
		};
		add(item);
		item = new JMenuItem("Warning Signs");
		item.setMnemonic('W');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new WarningSignForm(s,
					state.getWarningSigns()));
			}
		};
		add(item);
		item = new JMenuItem("Monitors");
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new VideoMonitorForm(
					state.getVideoMonitors()));
			}
		};
		add(item);
	}
}
