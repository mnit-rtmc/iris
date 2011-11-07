/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanModel extends ProxyTableModel<ActionPlan> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<ActionPlan>("Plan Name", 120) {
			public Object getValueAt(ActionPlan ap) {
				return ap.getName();
			}
			public boolean isEditable(ActionPlan ap) {
				return ap == null && canAdd();
			}
			public void setValueAt(ActionPlan ap, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<ActionPlan>("Description", 380) {
			public Object getValueAt(ActionPlan ap) {
				return ap.getDescription();
			}
			public boolean isEditable(ActionPlan ap) {
				return canUpdate(ap);
			}
			public void setValueAt(ActionPlan ap, Object value) {
				String v = value.toString().trim();
				ap.setDescription(v);
			}
		},
		new ProxyColumn<ActionPlan>("Sync Actions", 80, Boolean.class) {
			public Object getValueAt(ActionPlan ap) {
				return ap.getSyncActions();
			}
			public boolean isEditable(ActionPlan ap) {
				return canUpdate(ap);
			}
			public void setValueAt(ActionPlan ap, Object value) {
				if(value instanceof Boolean)
					ap.setSyncActions((Boolean)value);
			}
		},
		new ProxyColumn<ActionPlan>("Active", 80, Boolean.class) {
			public Object getValueAt(ActionPlan ap) {
				return ap.getActive();
			}
			public boolean isEditable(ActionPlan ap) {
				return canUpdate(ap);
			}
			public void setValueAt(ActionPlan ap, Object value) {
				if(value instanceof Boolean)
					ap.setActive((Boolean)value);
			}
		}
	    };
	}

	/** Create a new action plan table model */
	public ActionPlanModel(Session s) {
		super(s, s.getSonarState().getActionPlans());
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return ActionPlan.SONAR_TYPE;
	}
}
