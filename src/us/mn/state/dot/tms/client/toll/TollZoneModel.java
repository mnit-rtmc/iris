/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toll;

import java.util.ArrayList;
import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for toll zones.
 *
 * @author Douglas Lau
 */
public class TollZoneModel extends ProxyTableModel<TollZone> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<TollZone> descriptor(Session s) {
		return new ProxyDescriptor<TollZone>(
			s.getSonarState().getTollZones(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<TollZone>> createColumns() {
		ArrayList<ProxyColumn<TollZone>> cols =
			new ArrayList<ProxyColumn<TollZone>>(3);
		cols.add(new ProxyColumn<TollZone>("toll_zone", 120) {
			public Object getValueAt(TollZone tz) {
				return tz.getName();
			}
		});
		cols.add(new ProxyColumn<TollZone>("toll_zone.start_id", 120) {
			public Object getValueAt(TollZone tz) {
				return tz.getStartID();
			}
			public boolean isEditable(TollZone tz) {
				return canUpdate(tz);
			}
			public void setValueAt(TollZone tz, Object value) {
				String sid = value.toString().trim();
				tz.setStartID((sid.length() > 0) ? sid : null);
			}
		});
		cols.add(new ProxyColumn<TollZone>("toll_zone.end_id", 120) {
			public Object getValueAt(TollZone tz) {
				return tz.getEndID();
			}
			public boolean isEditable(TollZone tz) {
				return canUpdate(tz);
			}
			public void setValueAt(TollZone tz, Object value) {
				String eid = value.toString().trim();
				tz.setEndID((eid.length() > 0) ? eid : null);
			}
		});
		cols.add(new ProxyColumn<TollZone>("toll_zone.tollway", 180) {
			public Object getValueAt(TollZone tz) {
				return tz.getTollway();
			}
			public boolean isEditable(TollZone tz) {
				return canUpdate(tz);
			}
			public void setValueAt(TollZone tz, Object value) {
				String tw = value.toString().trim();
				tz.setTollway((tw.length() > 0) ? tw : null);
			}
		});
		return cols;
	}

	/** Create a new toll zone table model */
	public TollZoneModel(Session s) {
		super(s, descriptor(s), 12);
	}
}
