/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing action plans and schedules.
 *
 * @author Douglas Lau
 */
public class ScheduleForm extends AbstractForm {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return ActionPlanTab.isPermitted(s) &&
		       DayPlanPanel.isPermitted(s);
	}

	/** Tabbed pane */
	private final JTabbedPane tab = new JTabbedPane();

	/** Action plan tab */
	private final ActionPlanTab p_panel;

	/** Day plan panel */
	private final DayPlanPanel d_panel;

	/** Plan phase panel */
	private final ProxyTablePanel<PlanPhase> pp_panel;

	/** Create a new schedule form */
	public ScheduleForm(Session s) {
		super(I18N.get("action.plan.schedule.title"));
		p_panel = new ActionPlanTab(s);
		d_panel = new DayPlanPanel(s);
		pp_panel =new ProxyTablePanel<PlanPhase>(new PlanPhaseModel(s));
	}

	/** Initializze the widgets in the form */
	@Override
	protected void initialize() {
		super.initialize();
		p_panel.initialize();
		d_panel.initialize();
		pp_panel.initialize();
		tab.add(I18N.get("action_plan.title"), p_panel);
		tab.add(I18N.get("action.plan.day.plural"), d_panel);
		tab.add(I18N.get("action.plan.phase.plural"), pp_panel);
		add(tab);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		p_panel.dispose();
		d_panel.dispose();
		pp_panel.dispose();
		super.dispose();
	}
}
