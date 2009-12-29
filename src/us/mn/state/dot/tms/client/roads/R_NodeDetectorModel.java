/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.detector.DetectorManager;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

/**
 * Table model for r_node detectors
 *
 * @author Douglas Lau
 */
public class R_NodeDetectorModel extends ProxyTableModel<Detector> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Detector>("Detector", 80) {
			public Object getValueAt(Detector d) {
				return d.getName();
			}
			public boolean isEditable(Detector d) {
				return canAssign();
			}
			public void setValueAt(Detector d, Object value) {
				if(value == null || value instanceof Detector)
					setDetector(d, (Detector)value);
			}
			protected TableCellEditor createCellEditor() {
				return new NameCellEditor();
			}
		},
		new ProxyColumn<Detector>("Label", 140) {
			public Object getValueAt(Detector d) {
				return DetectorHelper.getLabel(d);
			}
		}
	    };
	}

	/** R_Node in question */
	protected final R_Node r_node;

	/** No r_node detector model */
	protected final WrapperComboBoxModel det_model;

	/** Create a new r_node detector table model */
	public R_NodeDetectorModel(Session s, R_Node n) {
		super(s, s.getSonarState().getDetCache().getDetectors());
		r_node = n;
		det_model = new WrapperComboBoxModel(
			s.getDetectorManager().getStyleModel(
			DetectorManager.STYLE_NO_R_NODE), true);
	}

	/** Add a new proxy to the list model */
	protected int doProxyAdded(Detector proxy) {
		if(proxy.getR_Node() == r_node)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Set the detector */
	protected void setDetector(Detector od, Detector nd) {
		if(od != nd) {
			if(od != null)
				od.setR_Node(null);
			if(nd != null)
				nd.setR_Node(r_node);
		}
	}

	/** Inner class for editing cells in the name column */
	protected class NameCellEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		protected final JComboBox combo = new JComboBox(det_model);
		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			det_model.setSelectedItem(value);
			return combo;
		}
		public Object getCellEditorValue() {
			return combo.getSelectedItem();
		}
	}

	/** Check if the user can assign a proxy */
	public boolean canAssign() {
		return namespace.canUpdate(user, new Name(Detector.SONAR_TYPE,
			"oname", "r_Node"));
	}
}
