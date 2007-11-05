/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incidents;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import us.mn.state.dot.tdxml.Incident;

/**
 * A TableModel for displaying incident information.
 *
 * @author Erik Engstrom
 */
public class IncidentTableModel extends AbstractTableModel {

	private List<Incident> list = new ArrayList<Incident>();
	
	/** Creates new IncidentTableModel */
	public IncidentTableModel() {
	}

	public int getRowCount() {
		return list.size();
	}
	
	public int getColumnCount() {
		return 5;
	}
	
	public Object getValueAt( int row, int column ){
		Incident incident = list.get(row);
		Object result = null;
		switch ( column ) {
			case 0:
				//result = incident.getIcon();
			break;
			case 1:
				result = incident.toString();
			break;
			case 2:
			break;
			case 3:
			break;
			case 4:
			break;
			default:
				result = null;
			break;
		}
		return result;
	}
}
