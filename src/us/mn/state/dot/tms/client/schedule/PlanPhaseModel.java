/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;

/**
 * Table model for plan phases.
 *
 * @author Douglas Lau
 */
public class PlanPhaseModel extends ProxyTableModel<PlanPhase> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<PlanPhase> descriptor(Session s) {
		return new ProxyDescriptor<PlanPhase>(
			s.getSonarState().getPlanPhases(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<PlanPhase>> createColumns() {
		ArrayList<ProxyColumn<PlanPhase>> cols =
			new ArrayList<ProxyColumn<PlanPhase>>(3);
		cols.add(new ProxyColumn<PlanPhase>("action.plan.phase.name",
			120)
		{
			public Object getValueAt(PlanPhase p) {
				return p.getName();
			}
		});
		cols.add(new ProxyColumn<PlanPhase>("action.plan.phase.hold",
			120, Integer.class)
		{
			public Object getValueAt(PlanPhase p) {
				return p.getHoldTime();
			}
			public boolean isEditable(PlanPhase p) {
				return canUpdate(p);
			}
			public void setValueAt(PlanPhase p, Object value) {
				if (value instanceof Integer)
					p.setHoldTime((Integer) value);
			}
		});
		cols.add(new ProxyColumn<PlanPhase>("action.plan.phase.next",
			120)
		{
			public Object getValueAt(PlanPhase p) {
				return p.getNextPhase();
			}
			public boolean isEditable(PlanPhase p) {
				return canUpdate(p);
			}
			public void setValueAt(PlanPhase p, Object value) {
				if (value instanceof PlanPhase)
					p.setNextPhase((PlanPhase) value);
				else
					p.setNextPhase(null);
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

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_mdl;

	/** Create a new plan phase table model */
	public PlanPhaseModel(Session s) {
		super(s, descriptor(s), 16);
		phase_mdl = s.getSonarState().getPhaseModel();
	}
}
