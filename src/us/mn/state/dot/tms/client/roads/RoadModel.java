/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import java.util.LinkedList;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for roads
 *
 * @author Douglas Lau
 */
public class RoadModel extends ProxyTableModel<Road> {

	/** List of all possible road class values */
	static LinkedList<String> R_CLASS = new LinkedList<String>();
	static {
		for(String r: Road.R_CLASS)
			R_CLASS.add(r);
	}

	/** List of all possible direction values */
	static LinkedList<String> DIRECTION = new LinkedList<String>();
	static {
		for(String d: Direction.DIR_LONG)
			DIRECTION.add(d);
	}

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Road>("Road", 200) {
			public Object getValueAt(Road r) {
				return r.getName();
			}
			public boolean isEditable(Road r) {
				return (r == null) && canAdd();
			}
			public void setValueAt(Road r, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<Road>("Abbrev", 80) {
			public Object getValueAt(Road r) {
				return r.getAbbrev();
			}
			public boolean isEditable(Road r) {
				return canUpdate(r);
			}
			public void setValueAt(Road r, Object value) {
				r.setAbbrev(value.toString());
			}
		},
		new ProxyColumn<Road>("Road Class", 120) {
			public Object getValueAt(Road r) {
				return R_CLASS.get(r.getRClass());
			}
			public boolean isEditable(Road r) {
				return canUpdate(r);
			}
			public void setValueAt(Road r, Object value) {
				r.setRClass((short)R_CLASS.indexOf(value));
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(Road.R_CLASS);
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<Road>("Direction", 120) {
			public Object getValueAt(Road r) {
				return DIRECTION.get(r.getDirection());
			}
			public boolean isEditable(Road r) {
				return canUpdate(r);
			}
			public void setValueAt(Road r, Object value) {
				r.setDirection((short)DIRECTION.indexOf(value));
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					DIRECTION.toArray());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<Road>("Alt Dir", 120) {
			public Object getValueAt(Road r) {
				return DIRECTION.get(r.getAltDir());
			}
			public boolean isEditable(Road r) {
				return canUpdate(r);
			}
			public void setValueAt(Road r, Object value) {
				r.setAltDir((short)DIRECTION.indexOf(value));
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					DIRECTION.toArray());
				return new DefaultCellEditor(combo);
			}
		}
	    };
	}

	/** Create a new road table model */
	public RoadModel(Session s) {
		super(s, s.getSonarState().getRoads());
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(Road.SONAR_TYPE,
			"oname"));
	}
}
