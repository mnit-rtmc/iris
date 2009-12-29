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
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanState;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.LaneMarkingHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for lane actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class LaneActionModel extends ProxyTableModel<LaneAction> {

	/** Allowed states */
	static protected final ActionPlanState[] STATES = {
		ActionPlanState.undeployed,
		ActionPlanState.deploying,
		ActionPlanState.deployed,
		ActionPlanState.undeploying
	};

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<LaneAction>("Lane Marking", 160) {
			public Object getValueAt(LaneAction la) {
				return la.getLaneMarking();
			}
			public boolean isEditable(LaneAction la) {
				return la == null && canAdd();
			}
			public void setValueAt(LaneAction la, Object value) {
				String v = value.toString().trim();
				LaneMarking lm = LaneMarkingHelper.lookup(v);
				if(lm != null)
					create(lm);
			}
		},
		new ProxyColumn<LaneAction>("State", 80) {
			public Object getValueAt(LaneAction la) {
				return ActionPlanState.fromOrdinal(
					la.getState());
			}
			public boolean isEditable(LaneAction la) {
				return canUpdate(la);
			}
			public void setValueAt(LaneAction la, Object value) {
				if(value instanceof ActionPlanState) {
					ActionPlanState st =
						(ActionPlanState)value;
					la.setState(st.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(STATES);
				return new DefaultCellEditor(combo);
			}
		},
	    };
	}

	/** Currently selected action plan */
	protected final ActionPlan action_plan;

	/** Create a new lane action table model */
	public LaneActionModel(Session s, ActionPlan ap) {
		super(s, s.getSonarState().getLaneActions());
		action_plan = ap;
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(LaneAction la) {
		if(la.getActionPlan() == action_plan)
			return super.doProxyAdded(la);
		else
			return -1;
	}

	/** Create a new lane action */
	protected void create(LaneMarking lm) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("lane_marking", lm);
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique lane action name */
	protected String createUniqueName() {
		for(int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if(cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return LaneAction.SONAR_TYPE;
	}
}
