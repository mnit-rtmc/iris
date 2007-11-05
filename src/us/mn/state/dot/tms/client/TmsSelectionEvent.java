/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client;

import java.util.EventObject;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * An event that indicates that a change in the current selection has occured.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class TmsSelectionEvent extends EventObject {

	/** The newly selected TMS object */
	protected final TmsMapProxy selected;

	/** Create a new TMS selection event */
	public TmsSelectionEvent(Object source, TmsMapProxy o) {
		super(source);
		selected = o;
	}

	/** Get the newly selected device */
	public TmsMapProxy getSelected() {
		return selected;
	}
}
