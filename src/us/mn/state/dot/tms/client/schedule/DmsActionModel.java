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

import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanState;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for DMS actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class DmsActionModel extends ProxyTableModel<DmsAction> {

	/** Allowed states */
	static protected final ActionPlanState[] STATES = {
		ActionPlanState.undeployed,
		ActionPlanState.deploying,
		ActionPlanState.deployed,
		ActionPlanState.undeploying
	};

	/** Allowed priorities */
	static protected final DMSMessagePriority[] PRIORITIES = {
		DMSMessagePriority.PSA,
		DMSMessagePriority.TRAVEL_TIME,
		DMSMessagePriority.SCHEDULED,
		DMSMessagePriority.INCIDENT_LOW,
		DMSMessagePriority.INCIDENT_MED,
		DMSMessagePriority.INCIDENT_HIGH
	};

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<DmsAction>("Sign Group", 120) {
			public Object getValueAt(DmsAction da) {
				return da.getSignGroup();
			}
			public boolean isEditable(DmsAction da) {
				return da == null && canAdd();
			}
			public void setValueAt(DmsAction da, Object value) {
				String v = value.toString().trim();
				SignGroup sg = SignGroupHelper.lookup(v);
				if(sg != null)
					create(sg);
			}
		},
		new ProxyColumn<DmsAction>("State", 80) {
			public Object getValueAt(DmsAction da) {
				return ActionPlanState.fromOrdinal(
					da.getState());
			}
			public boolean isEditable(DmsAction da) {
				return canUpdate(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				if(value instanceof ActionPlanState) {
					ActionPlanState st =
						(ActionPlanState)value;
					da.setState(st.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(STATES);
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<DmsAction>("Quick Message", 160) {
			public Object getValueAt(DmsAction da) {
				return da.getQuickMessage();
			}
			public boolean isEditable(DmsAction da) {
				return canUpdate(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				String v = value.toString().trim();
				da.setQuickMessage(
					QuickMessageHelper.lookup(v));
			}
		},
		new ProxyColumn<DmsAction>("Activation Priority", 120) {
			public Object getValueAt(DmsAction da) {
				return DMSMessagePriority.fromOrdinal(
				       da.getActivationPriority());
			}
			public boolean isEditable(DmsAction da) {
				return canUpdate(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				if(value instanceof DMSMessagePriority) {
					DMSMessagePriority p =
						(DMSMessagePriority)value;
					da.setActivationPriority(p.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(PRIORITIES);
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<DmsAction>("Run-Time Priority", 120) {
			public Object getValueAt(DmsAction da) {
				return DMSMessagePriority.fromOrdinal(
				       da.getRunTimePriority());
			}
			public boolean isEditable(DmsAction da) {
				return canUpdate(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				if(value instanceof DMSMessagePriority) {
					DMSMessagePriority p =
						(DMSMessagePriority)value;
					da.setRunTimePriority(p.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(PRIORITIES);
				return new DefaultCellEditor(combo);
			}
		}
	    };
	}

	/** Currently selected action plan */
	protected final ActionPlan action_plan;

	/** Create a new DMS action table model */
	public DmsActionModel(Session s, ActionPlan ap) {
		super(s, s.getSonarState().getDmsActions());
		action_plan = ap;
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(DmsAction da) {
		if(da.getActionPlan() == action_plan)
			return super.doProxyAdded(da);
		else
			return -1;
	}

	/** Create a new DMS action */
	protected void create(SignGroup sg) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("sign_group", sg);
			attrs.put("a_priority",
				DMSMessagePriority.SCHEDULED.ordinal());
			attrs.put("r_priority",
				DMSMessagePriority.SCHEDULED.ordinal());
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique DMS action name */
	protected String createUniqueName() {
		for(int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if(cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}

	/** Check if the user can add */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(DmsAction.SONAR_TYPE,
			"oname"));
	}
}
