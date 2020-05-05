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

/** Tag parameter input class for integer fields. */
@SuppressWarnings("serial")
class WTagParamIntField extends WTagParamField {
	public WTagParamIntField(Integer val, int columns, boolean required) {
		super("", columns, required);
		if (val != null)
			setText(val.toString());
	}
	
	/** Get the integer value of the data in the field. If the data cannot be
	 *  converted to an integer, null is returned.
	 */
	public Integer getValue() {
		try {
			return Integer.parseInt(getText());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** If this parameter is required, the field cannot be empty and must
	 *  contain a valid integer.
	 */
	@Override
	public boolean validateRequired() {
		return !getText().isEmpty() && getValue() != null;
	}
		
	/** If this parameter is not required, the field must either be empty or
	 *  contain a valid integer.
	 */
	@Override
	public boolean validateNotRequired() {
		return getText().isEmpty() || getValue() != null;
	}
}
