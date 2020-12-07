/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.editor.tags;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/** Tag parameter input class for fields that reference a SONAR object (e.g. a
 *  CommLink or Station).
 */
@SuppressWarnings("serial")
class WTagParamObjectField<T> extends JComboBox<T>
		implements WTagParamComponent {
	/** Whether or not the parameter taken from this field is required. */
	public boolean required = true;
	
	/** Constructor for taking an array of values */
	public WTagParamObjectField(
			T[] values, T selected, boolean required) {
		super(values);
		setSelectedItem(selected);
		this.required = required;
	}
	
	/** Constructor for taking a model */
	public WTagParamObjectField(ComboBoxModel<T> model,
			T selected, boolean required) {
		super(model);
		setSelectedItem(selected);
		this.required = required;
	}
	
	@Override
	public boolean validateRequired() {
		return getSelectedItem() != null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T getSelectedItem() {
		return (T) super.getSelectedItem();
	}
}
