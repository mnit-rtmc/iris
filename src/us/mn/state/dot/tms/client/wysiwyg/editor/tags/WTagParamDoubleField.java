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

/** Tag parameter input class for double (or float) fields. */
@SuppressWarnings("serial")
class WTagParamDoubleField extends WTagParamField {

	public WTagParamDoubleField(Double val, int columns, boolean required) {
		super("", columns, required);
		if (val != null) {
			setText(val.toString());
		}
	}
	
	/** Get the double-precision floating point value of the data in the
	 *  field. If the data cannot be converted to a double, null is returned.
	 */
	public Double getValue() {
		try {
			return Double.parseDouble(getText());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** If this parameter is required, the field cannot be empty and must
	 *  contain a valid double.
	 */
	@Override
	public boolean validateRequired() {
		return !getText().isEmpty() && getValue() != null;
	}
		
	/** If this parameter is not required, the field must either be empty or
	 *  contain a valid double.
	 */
	@Override
	public boolean validateNotRequired() {
		return getText().isEmpty() || getValue() != null;
	}
}