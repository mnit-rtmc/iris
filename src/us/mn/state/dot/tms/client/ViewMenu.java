/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.camera.CameraListForm;
import us.mn.state.dot.tms.client.camera.VideoMonitorForm;
import us.mn.state.dot.tms.client.dms.DMSListForm;
import us.mn.state.dot.tms.client.dms.FontForm;
import us.mn.state.dot.tms.client.lcs.LcsListForm;
import us.mn.state.dot.tms.client.meter.RampMeterListForm;
import us.mn.state.dot.tms.client.roads.RoadForm;
import us.mn.state.dot.tms.client.roads.StationListForm;
import us.mn.state.dot.tms.client.security.UserRoleForm;
import us.mn.state.dot.tms.client.toast.CabinetStyleForm;
import us.mn.state.dot.tms.client.toast.CommLinkForm;
import us.mn.state.dot.tms.client.toast.DetectorListForm;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.HolidayForm;
import us.mn.state.dot.tms.client.toast.PolicyForm;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.warning.WarningSignListForm;
import us.mn.state.dot.tms.utils.I18NMessages;


/**
 * ViewMenu is a JMenu which contains items to view various TMS object types.
 *
 * @author Douglas Lau
 */
public class ViewMenu extends JMenu {

	/** Create a new view menu */
	public ViewMenu(final TmsConnection tc, final SonarState st) {
		super("View");
		final SmartDesktop desktop = tc.getDesktop();
		setMnemonic('V');
		JMenuItem item = new JMenuItem("Users/Roles");
		item.setMnemonic('U');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new UserRoleForm(st.getUsers(),
					st.getRoles(), st.getConnections()));
			}
		};
		add(item);
		item = new JMenuItem("Policy");
		item.setMnemonic('P');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new PolicyForm(
					st.getSystemPolicy()));
			}
		};
		add(item);
		item = new JMenuItem("Cabinet Styles");
		item.setMnemonic('s');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CabinetStyleForm(
					st.getCabinetStyles()));
			}
		};
		add(item);
		item = new JMenuItem("Comm Links");
		item.setMnemonic('L');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CommLinkForm(
					st.getCommLinks()));
			}
		};
		add(item);
		item = new JMenuItem("Roadways", Icons.getIcon("roadway"));
		item.setMnemonic('R');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new RoadForm(st.getRoads()));
			}
		};
		add(item);
		item = new JMenuItem("Detectors", Icons.getIcon("detector"));
		item.setMnemonic('t');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new DetectorListForm(tc));
			}
		};
		add(item);
		item = new JMenuItem("Stations");
		item.setMnemonic('S');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new StationListForm(tc));
			}
		};
		add(item);
		item = new JMenuItem("Ramp Meters", Icons.getIcon(
			"meter-inactive"));
		item.setMnemonic('M');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new RampMeterListForm(tc));
			}
		};
		add(item);
		item = new JMenuItem("Holidays");
		item.setMnemonic('H');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new HolidayForm(st.getHolidays()));
			}
		};
		add(item);

		// get DMS menu item name
		String dmsmenuitem=I18NMessages.get("MesgSignLabel");
		item = new JMenuItem(dmsmenuitem, Icons.getIcon("drum-inactive"));
		// use 1st char as mnemonic
		if (dmsmenuitem.length()>0)
			item.setMnemonic(dmsmenuitem.charAt(0));

		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new DMSListForm(tc));
			}
		};
		add(item);
		item = new JMenuItem("Fonts");
		item.setMnemonic('F');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new FontForm(st.getFonts(),
					st.getGlyphs(), st.getGraphics()));
			}
		};
		add(item);
		item = new JMenuItem("Cameras");
		item.setMnemonic('C');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CameraListForm(tc));
			}
		};
		add(item);
		item = new JMenuItem("LCS", Icons.getIcon(
			"lanecontrol-inactive"));
		item.setMnemonic( 'L' );
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new LcsListForm(tc));
			}
		};
		add(item);
		item = new JMenuItem("Warning Signs");
		item.setMnemonic('W');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new WarningSignListForm(tc));
			}
		};
		add(item);
		item = new JMenuItem("Monitors");
//		item.setMnemonic('C');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new VideoMonitorForm(
					st.getVideoMonitors()));
			}
		};
		add(item);
	}
}
