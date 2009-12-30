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
package us.mn.state.dot.tms.client.schedule;

import java.awt.Color;
import javax.swing.JTabbedPane;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * A form for displaying and editing action plans and schedules.
 *
 * @author Douglas Lau
 */
public class ScheduleForm extends AbstractForm {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return ActionPlanPanel.isPermitted(s) &&
		       DayPlanPanel.isPermitted(s);
	}

	/** Frame title */
	static protected final String TITLE = "Plans and Schedules";

	/** Tabbed pane */
	protected final JTabbedPane tab = new JTabbedPane();

	/** Action plan panel */
	protected final ActionPlanPanel p_panel;

	/** Day plan panel */
	protected final DayPlanPanel d_panel;

	/** Create a new schedule form */
	public ScheduleForm(Session s) {
		super(TITLE);
		p_panel = new ActionPlanPanel(s);
		d_panel = new DayPlanPanel(s);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		p_panel.initialize();
		tab.add("Action Plans", p_panel);
		tab.add("Day Plans", d_panel);
		add(tab);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		p_panel.dispose();
		d_panel.dispose();
	}
}
