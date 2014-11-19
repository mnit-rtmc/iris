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

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.PlanPhaseHelper;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;

/**
 * Table model for DMS actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class DmsActionModel extends ProxyTableModel<DmsAction> {

	/** Allowed activation priorities */
	static private final DMSMessagePriority[] A_PRIORITIES = {
		DMSMessagePriority.PREFIX_PAGE,
		DMSMessagePriority.PSA,
		DMSMessagePriority.TRAVEL_TIME,
		DMSMessagePriority.SPEED_LIMIT,
		DMSMessagePriority.SCHEDULED,
		DMSMessagePriority.OTHER_SYSTEM,
		DMSMessagePriority.INCIDENT_LOW,
		DMSMessagePriority.INCIDENT_MED,
		DMSMessagePriority.INCIDENT_HIGH
	};

	/** Allowed run-time priorities */
	static private final DMSMessagePriority[] R_PRIORITIES = {
		DMSMessagePriority.PSA,
		DMSMessagePriority.TRAVEL_TIME,
		DMSMessagePriority.SPEED_LIMIT,
		DMSMessagePriority.SCHEDULED,
		DMSMessagePriority.OTHER_SYSTEM,
		DMSMessagePriority.INCIDENT_LOW,
		DMSMessagePriority.INCIDENT_MED,
		DMSMessagePriority.INCIDENT_HIGH
	};

	/** Create the columns in the model */
	protected ArrayList<ProxyColumn<DmsAction>> createColumns() {
		ArrayList<ProxyColumn<DmsAction>> cols =
			new ArrayList<ProxyColumn<DmsAction>>(6);
		cols.add(new ProxyColumn<DmsAction>("action.plan.dms.group",
			120)
		{
			public Object getValueAt(DmsAction da) {
				return da.getSignGroup();
			}
			public boolean isEditable(DmsAction da) {
				return da == null && canAdd();
			}
			public void setValueAt(DmsAction da, Object value) {
				String v = value.toString().trim();
				SignGroup sg = SignGroupHelper.lookup(v);
				if(sg != null && action_plan != null)
					create(sg);
			}
		});
		cols.add(new ProxyColumn<DmsAction>("action.plan.phase", 100) {
			public Object getValueAt(DmsAction da) {
				return da.getPhase();
			}
			public boolean isEditable(DmsAction da) {
				return canUpdate(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				if(value instanceof PlanPhase)
					da.setPhase((PlanPhase)value);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox();
				combo.setModel(new WrapperComboBoxModel(
					phase_model));
				return new DefaultCellEditor(combo);
			}
		});
		cols.add(new ProxyColumn<DmsAction>("quick.message", 160) {
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
		});
		cols.add(new ProxyColumn<DmsAction>("dms.beacon.enabled", 100,
			Boolean.class)
		{
			public Object getValueAt(DmsAction da) {
				return da.getBeaconEnabled();
			}
			public boolean isEditable(DmsAction da) {
				return canUpdate(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				if (value instanceof Boolean)
					da.setBeaconEnabled((Boolean)value);
			}
		});
		cols.add(new ProxyColumn<DmsAction>("dms.priority.activation",
			120)
		{
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
				JComboBox combo = new JComboBox(A_PRIORITIES);
				return new DefaultCellEditor(combo);
			}
		});
		cols.add(new ProxyColumn<DmsAction>("dms.priority.run.time",
			120)
		{
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
				JComboBox combo = new JComboBox(R_PRIORITIES);
				return new DefaultCellEditor(combo);
			}
		});
		return cols;
	}

	/** Currently selected action plan */
	protected final ActionPlan action_plan;

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return action_plan != null && super.canAdd();
	}

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_model;

	/** Create a new DMS action table model */
	public DmsActionModel(Session s, ActionPlan ap) {
		super(s, s.getSonarState().getDmsActions());
		action_plan = ap;
		phase_model = s.getSonarState().getPhaseModel();
	}

	/** Add a new proxy to the table model */
	@Override
	protected int doProxyAdded(DmsAction da) {
		if (da.getActionPlan() == action_plan)
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
			attrs.put("phase", action_plan.getDefaultPhase());
			attrs.put("a_priority",
				DMSMessagePriority.SCHEDULED.ordinal());
			attrs.put("r_priority",
				DMSMessagePriority.SCHEDULED.ordinal());
			cache.createObject(name, attrs);
		}
	}

	/** Lookup the appropriate plan phase for a DMS action */
	private PlanPhase lookupPlanPhase() {
		PlanPhase phase = PlanPhaseHelper.lookup("deployed");
		return (phase != null) ? phase : action_plan.getDefaultPhase();
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

	/** Get the SONAR type name */
	protected String getSonarType() {
		return DmsAction.SONAR_TYPE;
	}
}
