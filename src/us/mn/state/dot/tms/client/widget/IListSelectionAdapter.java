/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Simple list selection adapter.
 *
 * @author Douglas Lau
 */
abstract public class IListSelectionAdapter implements ListSelectionListener {

	/** Respond to list selection event */
	@Override
	public final void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting())
			valueChanged();
	}

	/** Respond to list selection event (not adjusting) */
	abstract public void valueChanged();
}
