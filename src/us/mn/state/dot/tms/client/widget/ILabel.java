/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import javax.swing.JLabel;
import us.mn.state.dot.tms.utils.I18N;

/** 
 * An internationalized label widget.
 *
 * @author Douglas Lau
 */
public class ILabel extends JLabel {

	/** Create an I18N'd label.
	 * @param text_id I18N id for label text. */
	public ILabel(String text_id) {
		super(I18N.get(text_id));
	}
}
