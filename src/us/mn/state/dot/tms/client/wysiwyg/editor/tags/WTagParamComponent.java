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

import java.awt.Color;

interface WTagParamComponent {
	/** Warning color for fields with invalid contents. */
	public static final Color WARNING_COLOR =
			new Color(255, 204, 203, 255);
	
	public static final Color OK_COLOR = Color.WHITE;
	
	/** Check if contents of the field are valid using the validateContents()
	 *  method. If contents are invalid, the field is highlighted with the
	 *  warning color.
	 */
	public default boolean contentsValid() {
		boolean valid = validateContents();
		if (!valid)
			setBackground(WARNING_COLOR);
		else
			setBackground(OK_COLOR);
		return valid;
	}
	
	/** Return whether or not this parameter is required. */
	public default boolean isRequired() {
		return true;
	}
	
	/** Validate the data in the field. If the parameter is required, this will
	 *  use the validateRequired method, otherwise it will use the
	 *  validateNotRequired method.
	 */
	public default boolean validateContents() {
		return (isRequired() && validateRequired())
				|| (!isRequired() && validateNotRequired());
	}
	
	/** Check if contents are valid in cases where this parameter is
	 *  required.
	 */
	public boolean validateRequired();

	/** Check if contents are valid in cases where this parameter is not
	 *  required. By default this returns true.
	 */
	public default boolean validateNotRequired() {
		return true;
	}
	
	public void setBackground(Color c);
}
