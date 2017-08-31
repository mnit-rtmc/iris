/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.LaneMarkingHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;

/**
 * Table model for lane actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class LaneActionModel extends ProxyTableModel<LaneAction> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<LaneAction> descriptor(Session s) {
		return new ProxyDescriptor<LaneAction>(
			s.getSonarState().getLaneActions(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<LaneAction>> createColumns() {
		ArrayList<ProxyColumn<LaneAction>> cols =
			new ArrayList<ProxyColumn<LaneAction>>(2);
		cols.add(new ProxyColumn<LaneAction>("lane_marking", 160) {
			public Object getValueAt(LaneAction la) {
				return la.getLaneMarking();
			}
		});
		cols.add(new ProxyColumn<LaneAction>("action.plan.phase", 100) {
			public Object getValueAt(LaneAction la) {
				return la.getPhase();
			}
			public boolean isEditable(LaneAction la) {
				return canWrite(la);
			}
			public void setValueAt(LaneAction la, Object value) {
				if (value instanceof PlanPhase)
					la.setPhase((PlanPhase)value);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<PlanPhase> cbx = new JComboBox
					<PlanPhase>();
				cbx.setModel(new IComboBoxModel<PlanPhase>(
					phase_mdl));
				return new DefaultCellEditor(cbx);
			}
		});
		return cols;
	}

	/** Currently selected action plan */
	private final ActionPlan action_plan;

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_mdl;

	/** Create a new lane action table model */
	public LaneActionModel(Session s, ActionPlan ap) {
		super(s, descriptor(s), 16);
		action_plan = ap;
		phase_mdl = s.getSonarState().getPhaseModel();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(LaneAction proxy) {
		return proxy.getActionPlan() == action_plan;
	}

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return action_plan != null && super.canAdd();
	}

	/** Create an object with the name */
	@Override
	public void createObject(String name) {
		LaneMarking lm = LaneMarkingHelper.lookup(name.trim());
		if (lm != null && action_plan != null)
			create(lm);
	}

	/** Create a new lane action */
	private void create(LaneMarking lm) {
		String name = createUniqueName();
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("lane_marking", lm);
			attrs.put("phase", action_plan.getDefaultPhase());
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Create a unique lane action name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}
}
