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

import javax.swing.JTextField;

/** Modified text field class for MULTI tag parameters that provides methods
 *  for checking validity depending on the expected data type and whether or
 *  not it is required.
 */
@SuppressWarnings("serial")
class WTagParamField extends JTextField implements WTagParamComponent {
	/** Whether or not the parameter taken from this field is required. */
	protected boolean required = true;
	
	/** Create a WTagParamField expecting a string.
	 *  @param text - text to pre-fill field with
	 *  @param columns - number of columns for the field box
	 *  @param required - whether or not this parameter is required
	 */
	public WTagParamField(String text, int columns, boolean required) {
		super(text, columns);
		this.required = required;
	}
	
	@Override
	public boolean isRequired() {
		return required;
	}
	
	/** If this parameter is required, the field cannot be empty and the
	 *  contents must be valid (whatever that means).
	 */
	public boolean validateRequired() {
		return !getText().isEmpty() && validateString();
	}
		
	/** If this parameter is not required, the field must either be empty or
	 *  contain a valid string (whatever that means).
	 */
	@Override
	public boolean validateNotRequired() {
		return getText().isEmpty() || validateString();
	}
	
	/** Allow additional parameter-specific validation to be performed on the
	 *  string. Unless overridden this assumes all strings are fine.
	 */
	protected boolean validateString() {
		return true;
	}
}
