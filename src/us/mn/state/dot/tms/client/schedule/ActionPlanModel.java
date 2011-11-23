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
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

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
				if(ap != null)
					return ap.getName();
				else
					return null;
			}
			public boolean isEditable(ActionPlan ap) {
				return ap == null && default_phase != null &&
				       canAdd();
			}
			public void setValueAt(ActionPlan ap, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					create(v);
			}
		},
		new ProxyColumn<ActionPlan>("Description", 380) {
			public Object getValueAt(ActionPlan ap) {
				if(ap != null)
					return ap.getDescription();
				else
					return null;
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
				if(ap != null)
					return ap.getSyncActions();
				else
					return null;
			}
			public boolean isEditable(ActionPlan ap) {
				return canUpdate(ap);
			}
			public void setValueAt(ActionPlan ap, Object value) {
				if(value instanceof Boolean)
					ap.setSyncActions((Boolean)value);
			}
		},
		new ProxyColumn<ActionPlan>("Sticky", 80, Boolean.class) {
			public Object getValueAt(ActionPlan ap) {
				if(ap != null)
					return ap.getSticky();
				else
					return null;
			}
			public boolean isEditable(ActionPlan ap) {
				return canUpdate(ap);
			}
			public void setValueAt(ActionPlan ap, Object value) {
				if(value instanceof Boolean)
					ap.setSticky((Boolean)value);
			}
		},
		new ProxyColumn<ActionPlan>("Active", 80, Boolean.class) {
			public Object getValueAt(ActionPlan ap) {
				if(ap != null)
					return ap.getActive();
				else
					return null;
			}
			public boolean isEditable(ActionPlan ap) {
				return canUpdate(ap);
			}
			public void setValueAt(ActionPlan ap, Object value) {
				if(value instanceof Boolean)
					ap.setActive((Boolean)value);
			}
		},
		new ProxyColumn<ActionPlan>("Default Phase", 100) {
			public Object getValueAt(ActionPlan ap) {
				if(ap != null)
					return ap.getDefaultPhase();
				else
					return default_phase;
			}
			public boolean isEditable(ActionPlan ap) {
				return canUpdate(ap) || canAdd();
			}
			public void setValueAt(ActionPlan ap, Object value) {
				PlanPhase p = null;
				if(value instanceof PlanPhase)
					p = (PlanPhase)value;
				if(ap != null)
					ap.setDefaultPhase(p);
				else
					default_phase = p;
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox();
				combo.setModel(new WrapperComboBoxModel(
					phase_model));
				return new DefaultCellEditor(combo);
			}
		},
	    };
	}

	/** Get the value at the specified cell.  Note: this overrides the
	 * method from ProxyTableModel to allow null proxies to be passed to
	 * ProxyColumn.getValueAt. */
	public Object getValueAt(int row, int col) {
		ActionPlan ap = getProxy(row);
		ProxyColumn pc = getProxyColumn(col);
		if(pc != null)
			return pc.getValueAt(ap);
		else
			return null;
	}

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_model;

	/** Default phase for new action plan */
	private PlanPhase default_phase;

	/** Create a new action plan table model */
	public ActionPlanModel(Session s) {
		super(s, s.getSonarState().getActionPlans());
		phase_model = s.getSonarState().getPhaseModel();
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return ActionPlan.SONAR_TYPE;
	}

	/** Create a new action plan */
	private void create(String name) {
		PlanPhase p = default_phase;
		if(p != null)
			create(name, p);
	}

	/** Create a new action plan */
	private void create(String name, PlanPhase p) {
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("default_phase", p);
		attrs.put("phase", p);
		cache.createObject(name, attrs);
	}
}
