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
package us.mn.state.dot.tms.client.toast;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for cabinet styles
 *
 * @author Douglas Lau
 */
public class CabinetStyleModel extends ProxyTableModel<CabinetStyle> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<CabinetStyle>("Style", 90) {
			public Object getValueAt(CabinetStyle cs) {
				return cs.getName();
			}
			public boolean isEditable(CabinetStyle cs) {
				return (cs == null) && canAdd();
			}
			public void setValueAt(CabinetStyle cs, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<CabinetStyle>("Dip", 60) {
			public Object getValueAt(CabinetStyle cs) {
				return cs.getDip();
			}
			public boolean isEditable(CabinetStyle cs) {
				return canUpdate(cs);
			}
			public void setValueAt(CabinetStyle cs, Object value) {
				if(value instanceof Integer)
					cs.setDip((Integer)value);
			}
			protected TableCellEditor createCellEditor() {
				return new DipEditor();
			}
		}
	    };
	}

	/** Create a new cabinet style table model */
	public CabinetStyleModel(Session s) {
		super(s, s.getSonarState().getConCache().getCabinetStyles());
	}

	/** Editor for dip values in a table cell */
	public class DipEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		protected final SpinnerNumberModel model =
			new SpinnerNumberModel(0, 0, 256, 1);
		protected final JSpinner spinner = new JSpinner(model);

		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			if(value != null)
				spinner.setValue(value);
			else
				spinner.setValue(0);
			return spinner;
		}
		public Object getCellEditorValue() {
			return spinner.getValue();
		}
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return CabinetStyle.SONAR_TYPE;
	}
}
