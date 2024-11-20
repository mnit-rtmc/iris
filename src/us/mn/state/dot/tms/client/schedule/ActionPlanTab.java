/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A widget for displaying and editing action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanTab extends JPanel {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(ActionPlan.SONAR_TYPE) &&
		       s.canRead(DeviceAction.SONAR_TYPE);
	}

	/** User session */
	private final Session session;

	/** Action plan panel */
	private final ActionPlanPanel plan_pnl;

	/** Time action table panel */
	private final ProxyTablePanel<TimeAction> t_panel;

	/** Device action table panel */
	private final ProxyTablePanel<DeviceAction> d_panel;

	/** Create a new action plan panel */
	public ActionPlanTab(Session s) {
		session = s;
		plan_pnl = new ActionPlanPanel(s) {
			protected void selectProxy() {
				super.selectProxy();
				selectActionPlan();
			}
		};
		t_panel = new TimeActionPanel(s);
		d_panel = new ProxyTablePanel<DeviceAction>(
			new DeviceActionModel(s, null));
	}

	/** Initializze the widgets on the panel */
	public void initialize() {
		plan_pnl.initialize();
		t_panel.initialize();
		d_panel.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("action.plan.schedule"), t_panel);
		tab.add(I18N.get("action.plan.device"), d_panel);
		GroupLayout gl = new GroupLayout(this);
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		hg.addComponent(plan_pnl);
		hg.addComponent(tab);
		vg.addComponent(plan_pnl);
		vg.addGap(UI.vgap);
		vg.addComponent(tab);
		gl.setHorizontalGroup(hg);
		gl.setVerticalGroup(vg);
		setLayout(gl);
	}

	/** Dispose of the form */
	public void dispose() {
		plan_pnl.dispose();
		t_panel.dispose();
		d_panel.dispose();
	}

	/** Change the selected action plan */
	private void selectActionPlan() {
		ActionPlan ap = plan_pnl.getSelectedProxy();
		t_panel.setModel(new TimeActionModel(session, ap));
		d_panel.setModel(new DeviceActionModel(session, ap));
	}
}
