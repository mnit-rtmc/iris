/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconAction;
import us.mn.state.dot.tms.BeaconHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;

/**
 * Table model for beacon actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class BeaconActionModel extends ProxyTableModel<BeaconAction> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<BeaconAction>> createColumns() {
		ArrayList<ProxyColumn<BeaconAction>> cols =
			new ArrayList<ProxyColumn<BeaconAction>>(2);
		cols.add(new ProxyColumn<BeaconAction>("beacon", 160) {
			public Object getValueAt(BeaconAction la) {
				return la.getBeacon();
			}
			public boolean isEditable(BeaconAction la) {
				return la == null && canAdd();
			}
			public void setValueAt(BeaconAction la, Object value) {
				String v = value.toString().trim();
				Beacon b = BeaconHelper.lookup(v);
				if(b != null && action_plan != null)
					create(b);
			}
		});
		cols.add(new ProxyColumn<BeaconAction>("action.plan.phase", 100) {
			public Object getValueAt(BeaconAction la) {
				return la.getPhase();
			}
			public boolean isEditable(BeaconAction la) {
				return canUpdate(la);
			}
			public void setValueAt(BeaconAction la, Object value) {
				if(value instanceof PlanPhase)
					la.setPhase((PlanPhase)value);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox();
				combo.setModel(new WrapperComboBoxModel(
					phase_model));
				return new DefaultCellEditor(combo);
			}
		});
		return cols;
	}

	/** Currently selected action plan */
	private final ActionPlan action_plan;

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return action_plan != null && super.canAdd();
	}

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_model;

	/** Create a new beacon action table model */
	public BeaconActionModel(Session s, ActionPlan ap) {
		super(s, s.getSonarState().getBeaconActions());
		action_plan = ap;
		phase_model = s.getSonarState().getPhaseModel();
	}

	/** Add a new proxy to the table model */
	@Override
	protected int doProxyAdded(BeaconAction ba) {
		if (ba.getActionPlan() == action_plan)
			return super.doProxyAdded(ba);
		else
			return -1;
	}

	/** Create a new beacon action */
	private void create(Beacon b) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("beacon", b);
			attrs.put("phase", action_plan.getDefaultPhase());
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique beacon action name */
	private String createUniqueName() {
		for(int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if(cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}

	/** Get the SONAR type name */
	@Override
	protected String getSonarType() {
		return BeaconAction.SONAR_TYPE;
	}
}
