/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

/**
 * Table model for plan phases.
 *
 * @author Douglas Lau
 */
public class PlanPhaseModel extends ProxyTableModel<PlanPhase> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<PlanPhase>("Phase Name", 120) {
			public Object getValueAt(PlanPhase p) {
				return p.getName();
			}
			public boolean isEditable(PlanPhase p) {
				return p == null && canAdd();
			}
			public void setValueAt(PlanPhase p, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<PlanPhase>("Hold Time", 120, Integer.class) {
			public Object getValueAt(PlanPhase p) {
				return p.getHoldTime();
			}
			public boolean isEditable(PlanPhase p) {
				return canUpdate(p);
			}
			public void setValueAt(PlanPhase p, Object value) {
				if(value instanceof Integer)
					p.setHoldTime((Integer)value);
			}
		},
		new ProxyColumn<PlanPhase>("Next Phase", 120) {
			public Object getValueAt(PlanPhase p) {
				return p.getNextPhase();
			}
			public boolean isEditable(PlanPhase p) {
				return canUpdate(p);
			}
			public void setValueAt(PlanPhase p, Object value) {
				if(value instanceof PlanPhase)
					p.setNextPhase((PlanPhase)value);
				else
					p.setNextPhase(null);
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

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_model;

	/** Create a new plan phase table model */
	public PlanPhaseModel(Session s) {
		super(s, s.getSonarState().getPlanPhases());
		phase_model = s.getSonarState().getPhaseModel();
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return PlanPhase.SONAR_TYPE;
	}
}
