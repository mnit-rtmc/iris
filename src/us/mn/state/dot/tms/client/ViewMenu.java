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
import us.mn.state.dot.tms.client.camera.VideoMenu;
import us.mn.state.dot.tms.client.detector.DetectorForm;
import us.mn.state.dot.tms.client.marking.LaneMarkingForm;
import us.mn.state.dot.tms.client.meter.RampMeterForm;
import us.mn.state.dot.tms.client.roads.RoadForm;
import us.mn.state.dot.tms.client.schedule.ScheduleForm;
import us.mn.state.dot.tms.client.system.SystemMenu;
import us.mn.state.dot.tms.client.toast.AlarmForm;
import us.mn.state.dot.tms.client.toast.CabinetStyleForm;
import us.mn.state.dot.tms.client.toast.CommLinkForm;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.warning.WarningSignForm;

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
		super("View");
		session = s;
		desktop = session.getDesktop();
		SystemMenu s_menu = new SystemMenu(session);
		if(s_menu.getItemCount() > 0)
			add(s_menu);
		VideoMenu vid_menu = new VideoMenu(session);
		if(vid_menu.getItemCount() > 0)
			add(vid_menu);
		setMnemonic('V');
		JMenuItem item = new JMenuItem("Cabinet Styles");
		item.setMnemonic('s');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CabinetStyleForm(session));
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
				desktop.show(new AlarmForm(session));
			}
		};
		add(item);
		item = new JMenuItem("Roadways");
		item.setMnemonic('R');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new RoadForm(session));
			}
		};
		add(item);
		item = new JMenuItem("Detectors");
		item.setMnemonic('t');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new DetectorForm(session));
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
				desktop.show(new LaneMarkingForm(session));
			}
		};
		add(item);
		item = new JMenuItem("Warning Signs");
		item.setMnemonic('W');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new WarningSignForm(session));
			}
		};
		add(item);
	}

	/** Add the ramp meter menu item */
	public void addMeterItem() {
		JMenuItem item = new JMenuItem("Ramp Meters");
		item.setMnemonic('M');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new RampMeterForm(session));
			}
		};
		add(item);
	}
}
