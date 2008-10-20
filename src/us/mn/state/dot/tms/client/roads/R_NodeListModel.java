/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import javax.swing.DefaultListModel;
import us.mn.state.dot.tms.R_Node;

/**
 * A list model for r_node renderers.
 *
 * @author Douglas Lau
 */
public class R_NodeListModel extends DefaultListModel {

	/** Update the list item for the specified proxy */
	public void updateItem(R_Node proxy) {
		for(int i = 0; i < getSize(); i++) {
			R_NodeRenderer r = (R_NodeRenderer)getElementAt(i);
			if(r.getProxy() == proxy) {
				fireContentsChanged(this, i, i);
				return;
			}
		}
	}
}
