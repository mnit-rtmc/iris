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

import javax.swing.JComboBox;

@SuppressWarnings("serial")
class WTagParamEnumField<E> extends JComboBox<E> implements WTagParamComponent {
	/** Whether or not the parameter taken from this field is required. */
	public boolean required = true;
	
	public WTagParamEnumField(E[] values, E selected, boolean required) {
		super(values);
		setSelectedItem(selected);
		this.required = required;
	}

	@Override
	public boolean isRequired() {
		return required;
	}
	
	@Override
	public boolean validateRequired() {
		return getSelectedItem() != null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public E getSelectedItem() {
		return (E) super.getSelectedItem();
	}
}
