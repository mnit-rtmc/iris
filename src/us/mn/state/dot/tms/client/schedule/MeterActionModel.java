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

import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanState;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for meter actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class MeterActionModel extends ProxyTableModel<MeterAction> {

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
		new ProxyColumn<MeterAction>("Ramp Meter", 160) {
			public Object getValueAt(MeterAction ma) {
				return ma.getRampMeter();
			}
			public boolean isEditable(MeterAction ma) {
				return ma == null && canAdd();
			}
			public void setValueAt(MeterAction ma, Object value) {
				String v = value.toString().trim();
				RampMeter rm = RampMeterHelper.lookup(v);
				if(rm != null)
					create(rm);
			}
		},
		new ProxyColumn<MeterAction>("State", 80) {
			public Object getValueAt(MeterAction ma) {
				return ActionPlanState.fromOrdinal(
					ma.getState());
			}
			public boolean isEditable(MeterAction ma) {
				return canUpdate(ma);
			}
			public void setValueAt(MeterAction ma, Object value) {
				if(value instanceof ActionPlanState) {
					ActionPlanState st =
						(ActionPlanState)value;
					ma.setState(st.ordinal());
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

	/** Create a new meter action table model */
	public MeterActionModel(Session s, ActionPlan ap) {
		super(s, s.getSonarState().getMeterActions());
		action_plan = ap;
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(MeterAction ma) {
		if(ma.getActionPlan() == action_plan)
			return super.doProxyAdded(ma);
		else
			return -1;
	}

	/** Create a new meter action */
	protected void create(RampMeter rm) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("ramp_meter", rm);
			attrs.put("state", ActionPlanState.deployed.ordinal());
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique meter action name */
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
		return MeterAction.SONAR_TYPE;
	}
}
