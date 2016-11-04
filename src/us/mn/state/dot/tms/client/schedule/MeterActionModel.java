/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;

/**
 * Table model for meter actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class MeterActionModel extends ProxyTableModel<MeterAction> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<MeterAction> descriptor(Session s) {
		return new ProxyDescriptor<MeterAction>(
			s.getSonarState().getMeterActions(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<MeterAction>> createColumns() {
		ArrayList<ProxyColumn<MeterAction>> cols =
			new ArrayList<ProxyColumn<MeterAction>>(2);
		cols.add(new ProxyColumn<MeterAction>("ramp_meter", 160) {
			public Object getValueAt(MeterAction ma) {
				return ma.getRampMeter();
			}
		});
		cols.add(new ProxyColumn<MeterAction>("action.plan.phase", 100){
			public Object getValueAt(MeterAction ma) {
				return ma.getPhase();
			}
			public boolean isEditable(MeterAction ma) {
				return canUpdate(ma);
			}
			public void setValueAt(MeterAction ma, Object value) {
				if (value instanceof PlanPhase)
					ma.setPhase((PlanPhase) value);
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

	/** Create a new meter action table model */
	public MeterActionModel(Session s, ActionPlan ap) {
		super(s, descriptor(s), 16);
		action_plan = ap;
		phase_mdl = s.getSonarState().getPhaseModel();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(MeterAction proxy) {
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
		RampMeter rm = RampMeterHelper.lookup(name.trim());
		if (rm != null && action_plan != null)
			create(rm);
	}

	/** Create a new meter action */
	private void create(RampMeter rm) {
		String name = createUniqueName();
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("ramp_meter", rm);
			attrs.put("phase", action_plan.getDefaultPhase());
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Create a unique meter action name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}
}
