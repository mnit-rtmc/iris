/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.RoadClass;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for roads
 *
 * @author Douglas Lau
 */
public class RoadModel extends ProxyTableModel<Road> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Road> descriptor(Session s) {
		return new ProxyDescriptor<Road>(
			s.getSonarState().getRoads(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Road>> createColumns() {
		ArrayList<ProxyColumn<Road>> cols =
			new ArrayList<ProxyColumn<Road>>(5);
		cols.add(new ProxyColumn<Road>("location.road", 200) {
			public Object getValueAt(Road r) {
				return r.getName();
			}
		});
		cols.add(new ProxyColumn<Road>("location.road.abbrev", 80) {
			public Object getValueAt(Road r) {
				return r.getAbbrev();
			}
			public boolean isEditable(Road r) {
				return canUpdate(r);
			}
			public void setValueAt(Road r, Object value) {
				r.setAbbrev(value.toString());
			}
		});
		cols.add(new ProxyColumn<Road>("location.road.class", 120) {
			public Object getValueAt(Road r) {
				return RoadClass.fromOrdinal(r.getRClass());
			}
			public boolean isEditable(Road r) {
				return canUpdate(r);
			}
			public void setValueAt(Road r, Object value) {
				if (value instanceof RoadClass) {
					RoadClass c = (RoadClass)value;
					r.setRClass((short)c.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<RoadClass> cbx = new JComboBox
					<RoadClass>(RoadClass.values());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<Road>("location.direction", 120) {
			public Object getValueAt(Road r) {
				return Direction.fromOrdinal(r.getDirection());
			}
			public boolean isEditable(Road r) {
				return canUpdate(r);
			}
			public void setValueAt(Road r, Object value) {
				if (value instanceof Direction) {
					Direction d = (Direction)value;
					r.setDirection((short)d.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<Direction> cbx = new JComboBox
					<Direction>(Direction.values());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<Road>("location.alt_dir", 120) {
			public Object getValueAt(Road r) {
				return Direction.fromOrdinal(r.getAltDir());
			}
			public boolean isEditable(Road r) {
				return canUpdate(r);
			}
			public void setValueAt(Road r, Object value) {
				if (value instanceof Direction) {
					Direction d = (Direction)value;
					r.setAltDir((short)d.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<Direction> cbx = new JComboBox
					<Direction>(Direction.values());
				return new DefaultCellEditor(cbx);
			}
		});
		return cols;
	}

	/** Create a new road table model */
	public RoadModel(Session s) {
		super(s, descriptor(s),
		      true,	/* has_create_delete */
		      true);	/* has_name */
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 20;
	}
}
