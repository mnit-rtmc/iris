/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for cabinet styles
 *
 * @author Douglas Lau
 */
public class CabinetStyleModel extends ProxyTableModel<CabinetStyle> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<CabinetStyle> descriptor(Session s) {
		return new ProxyDescriptor<CabinetStyle>(
			s.getSonarState().getConCache().getCabinetStyles(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<CabinetStyle>> createColumns() {
		ArrayList<ProxyColumn<CabinetStyle>> cols =
			new ArrayList<ProxyColumn<CabinetStyle>>(2);
		cols.add(new ProxyColumn<CabinetStyle>("cabinet.style", 90) {
			public Object getValueAt(CabinetStyle cs) {
				return cs.getName();
			}
		});
		cols.add(new ProxyColumn<CabinetStyle>("cabinet.style.dip", 60){
			public Object getValueAt(CabinetStyle cs) {
				return cs.getDip();
			}
			public boolean isEditable(CabinetStyle cs) {
				return canWrite(cs);
			}
			public void setValueAt(CabinetStyle cs, Object value) {
				if (value instanceof Integer)
					cs.setDip((Integer)value);
			}
			protected TableCellEditor createCellEditor() {
				return new DipEditor();
			}
		});
		return cols;
	}

	/** Create a new cabinet style table model */
	public CabinetStyleModel(Session s) {
		super(s, descriptor(s), 12);
	}

	/** Editor for dip values in a table cell */
	public class DipEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		private final SpinnerNumberModel model =
			new SpinnerNumberModel(0, 0, 256, 1);
		private final JSpinner spinner = new JSpinner(model);

		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			spinner.setValue((value != null) ? value : 0);
			return spinner;
		}
		public Object getCellEditorValue() {
			return spinner.getValue();
		}
	}
}
