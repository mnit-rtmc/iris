/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * LaneUseMenu is a menu for LCS-related items.
 *
 * @author Douglas Lau
 */
public class LaneUseMenu extends JMenu {

	/** Create a new lane use menu */
	public LaneUseMenu(final Session s) {
		super("Lane Use");
		final SmartDesktop desktop = s.getDesktop();

		JMenuItem item = new JMenuItem("LCS");
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
				desktop.show(new GraphicForm(s));
			}
		};
		add(item);
		item = new JMenuItem("Lane-Use MULTI");
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new LaneUseMultiForm(s));
			}
		};
		add(item);
	}
}
