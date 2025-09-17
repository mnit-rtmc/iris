/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2025  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;

/**
 * A panel for displaying a table of action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanPanel extends ProxyTablePanel<ActionPlan> {

	/** Create a new action plan panel */
	public ActionPlanPanel(Session s) {
		super(new ActionPlanModel(s));
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		ActionPlanModel mdl = getActionPlanModel();
		if (mdl != null) {
			String name = add_txt.getText().trim();
			if (name.length() > 0)
				mdl.create(name);
		}
		add_txt.setText("");
	}

	/** Get the action plan model */
	private ActionPlanModel getActionPlanModel() {
		ProxyTableModel<ActionPlan> mdl = model;
		return (mdl instanceof ActionPlanModel)
		     ? (ActionPlanModel) mdl
		     : null;
	}
}
