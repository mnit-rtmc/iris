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

import us.mn.state.dot.sonar.SonarObject;

/** Tag parameter input class for fields that reference a SONAR object (e.g. a
 *  CommLink or Station).
 */
@SuppressWarnings("serial")
class WTagParamSonarObjectField<T extends SonarObject> extends JComboBox<SonarObject>
		implements WTagParamComponent {
	/** Whether or not the parameter taken from this field is required. */
	public boolean required = true;
	
	/** Constructor for taking an array of values */
	public WTagParamSonarObjectField(
			SonarObject[] values, SonarObject selected, boolean required) {
		super(values);
		setSelectedItem(selected);
		this.required = required;
	}
	
	/** Constructor for taking a model */
	@SuppressWarnings("unchecked")
	public WTagParamSonarObjectField(ComboBoxModel<T> model,
			SonarObject selected, boolean required) {
		super((ComboBoxModel<SonarObject>) model);
		setSelectedItem(selected);
		this.required = required;
	}
	
	@SuppressWarnings("unchecked")
	public void setComboBoxModel(ComboBoxModel<T> model) {
		setModel((ComboBoxModel<SonarObject>) model);
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
	
//	/** Set the selected object based on the name */
//	public void setSelectedObject(SonarObject so) {
//		// try to find the object in the list
//		ComboBoxModel<SonarObject> model = getModel();
//		for (int i = 0; i < model.getSize(); ++i) {
//			SonarObject o = model.getElementAt(i);
//			if (o.getName() == so.getName())
//				
//		}
//	}
}
