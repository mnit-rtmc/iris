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
package us.mn.state.dot.tms.client.roads;

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for map extents.
 *
 * @author Douglas Lau
 */
public class MapExtentModel extends ProxyTableModel<MapExtent> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<MapExtent>("Map Extent", 160) {
			public Object getValueAt(MapExtent me) {
				return me.getName();
			}
			public boolean isEditable(MapExtent me) {
				return (me == null) && canAdd();
			}
			public void setValueAt(MapExtent me, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<MapExtent>("Easting", 80, Integer.class) {
			public Object getValueAt(MapExtent me) {
				return me.getEasting();
			}
			public boolean isEditable(MapExtent me) {
				return canUpdate(me);
			}
			public void setValueAt(MapExtent me, Object value) {
				if(value instanceof Integer)
					me.setEasting((Integer)value);
			}
		},
		new ProxyColumn<MapExtent>("East Span", 80, Integer.class) {
			public Object getValueAt(MapExtent me) {
				return me.getEastSpan();
			}
			public boolean isEditable(MapExtent me) {
				return canUpdate(me);
			}
			public void setValueAt(MapExtent me, Object value) {
				if(value instanceof Integer)
					me.setEastSpan((Integer)value);
			}
		},
		new ProxyColumn<MapExtent>("Northing", 80, Integer.class) {
			public Object getValueAt(MapExtent me) {
				return me.getNorthing();
			}
			public boolean isEditable(MapExtent me) {
				return canUpdate(me);
			}
			public void setValueAt(MapExtent me, Object value) {
				if(value instanceof Integer)
					me.setNorthing((Integer)value);
			}
		},
		new ProxyColumn<MapExtent>("North Span", 80, Integer.class) {
			public Object getValueAt(MapExtent me) {
				return me.getNorthSpan();
			}
			public boolean isEditable(MapExtent me) {
				return canUpdate(me);
			}
			public void setValueAt(MapExtent me, Object value) {
				if(value instanceof Integer)
					me.setNorthSpan((Integer)value);
			}
		}
	    };
	}

	/** Create a new map extent table model */
	public MapExtentModel(Session s) {
		super(s, s.getSonarState().getMapExtents());
	}

	/** Check if the user can add a map extent */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(MapExtent.SONAR_TYPE,
			"oname"));
	}
}
