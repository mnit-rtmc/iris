/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2026  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.ActCondition;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.PhaseAction;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;

/**
 * Table model for phase actions assigned to action plans.
 *
 * @author Douglas Lau
 */
public class PhaseActionModel extends ProxyTableModel<PhaseAction> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<PhaseAction> descriptor(Session s) {
		return new ProxyDescriptor<PhaseAction>(
			s.getSonarState().getPhaseActions(),
			false, /* has_properties */
			true,  /* has_create_delete */
			false  /* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<PhaseAction>> createColumns() {
		ArrayList<ProxyColumn<PhaseAction>> cols =
			new ArrayList<ProxyColumn<PhaseAction>>(5);
		cols.add(new ProxyColumn<PhaseAction>("action.plan.day", 100) {
			public Object getValueAt(PhaseAction pa) {
				return pa.getDayPlan();
			}
			public boolean isEditable(PhaseAction pa) {
				return canWrite(pa);
			}
			public void setValueAt(PhaseAction pa, Object value) {
				if (value instanceof DayPlan)
					pa.setDayPlan((DayPlan) value);
				else
					pa.setDayPlan(null);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<DayPlan> cbx = new JComboBox<DayPlan>();
				cbx.setModel(new IComboBoxModel<DayPlan>(
					day_mdl));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<PhaseAction>("phase.action.from", 100) {
			public Object getValueAt(PhaseAction pa) {
				return pa.getFromPhase();
			}
			public boolean isEditable(PhaseAction pa) {
				return canWrite(pa);
			}
			public void setValueAt(PhaseAction pa, Object value) {
				if (value instanceof PlanPhase)
					pa.setFromPhase((PlanPhase) value);
				else
					pa.setFromPhase(null);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<PlanPhase> cbx = new JComboBox
					<PlanPhase>();
				cbx.setModel(new IComboBoxModel<PlanPhase>(
					phase_mdl));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<PhaseAction>("phase.action.to", 100) {
			public Object getValueAt(PhaseAction pa) {
				return pa.getToPhase();
			}
			public boolean isEditable(PhaseAction pa) {
				return canWrite(pa);
			}
			public void setValueAt(PhaseAction pa, Object value) {
				if (value instanceof PlanPhase)
					pa.setToPhase((PlanPhase) value);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<PlanPhase> cbx = new JComboBox
					<PlanPhase>();
				cbx.setModel(new IComboBoxModel<PlanPhase>(
					phase_mdl, false));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<PhaseAction>("phase.action.condition",
			120)
		{
			public Object getValueAt(PhaseAction pa) {
				int c = pa.getCondition();
				return ActCondition.fromOrdinal(c);
			}
			public boolean isEditable(PhaseAction pa) {
				return canWrite(pa);
			}
			public void setValueAt(PhaseAction pa, Object value) {
				if (value instanceof ActCondition) {
					ActCondition ac = (ActCondition) value;
					pa.setCondition(ac.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<ActCondition> cbx = new JComboBox
					<ActCondition>(ActCondition.values());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<PhaseAction>("phase.action.params",
			140)
		{
			public Object getValueAt(PhaseAction pa) {
				return pa.getParams();
			}
			public boolean isEditable(PhaseAction pa) {
				return canWrite(pa);
			}
			public void setValueAt(PhaseAction pa, Object value) {
				String v = value.toString().trim();
				pa.setParams((v.length() > 0) ? v : null);
			}
		});
		return cols;
	}

	/** Currently selected action plan */
	private final ActionPlan action_plan;

	/** Day plaa model */
	private final ProxyListModel<DayPlan> day_mdl;

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_mdl;

	/** Create a new phase action table model */
	public PhaseActionModel(Session s, ActionPlan ap) {
		super(s, descriptor(s), 12);
		action_plan = ap;
		day_mdl = s.getSonarState().getDayModel();
		phase_mdl = s.getSonarState().getPhaseModel();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(PhaseAction proxy) {
		return proxy.getActionPlan() == action_plan;
	}

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return action_plan != null && super.canAdd();
	}

	/** Create a new phase action */
	public void createObject() {
		if (action_plan != null) {
			String name = createUniqueName();
			if (name != null)
				create(name);
		}
	}

	/** Create a unique phase action name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}

	/** Create a new phase action */
	private void create(String name) {
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("action_plan", action_plan);
		attrs.put("to_phase", action_plan.getDefaultPhase());
		descriptor.cache.createObject(name, attrs);
	}
}
