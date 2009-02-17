/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * A MultipleSignTab is a GUI component for sending a message to multiple signs
 * at once within the DMS dispatcher.
 *
 * @author Douglas Lau
 */
public class MultipleSignTab extends FormPanel {

	/** Create a new multiple sign tab */
	public MultipleSignTab() {
		super(true);
	}
}
