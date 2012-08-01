/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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

import javax.swing.CellEditor;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

/**
 * A combo box model wrapper for cell editors.
 *
 * @author Douglas Lau
 */
public class CellEditorComboBoxModel implements ComboBoxModel {

	protected final CellEditor editor;

	protected final ComboBoxModel model;

	public CellEditorComboBoxModel(CellEditor ce, ComboBoxModel m) {
		editor = ce;
		model = m;
	}
	public int getSize() {
		return model.getSize();
	}
	public Object getElementAt(int index) {
		return model.getElementAt(index);
	}
	public void addListDataListener(ListDataListener l) {
		model.addListDataListener(l);
	}
	public void removeListDataListener(ListDataListener l) {
		model.removeListDataListener(l);
	}
	public Object getSelectedItem() {
		return model.getSelectedItem();
	}
	public void setSelectedItem(Object item) {
		model.setSelectedItem(item);
		editor.stopCellEditing();
	}
}
