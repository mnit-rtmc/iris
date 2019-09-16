/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.RoadAffix;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for road affixes.
 *
 * @author Douglas Lau
 */
public class RoadAffixModel extends ProxyTableModel<RoadAffix> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<RoadAffix> descriptor(Session s) {
		return new ProxyDescriptor<RoadAffix>(
			s.getSonarState().getRoadAffixes(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<RoadAffix>> createColumns() {
		ArrayList<ProxyColumn<RoadAffix>> cols =
			new ArrayList<ProxyColumn<RoadAffix>>(4);
		cols.add(new ProxyColumn<RoadAffix>("location.road.affix", 200){
			public Object getValueAt(RoadAffix ra) {
				return ra.getName();
			}
		});
		cols.add(new ProxyColumn<RoadAffix>("location.road.prefix", 80,
			Boolean.class)
		{
			public Object getValueAt(RoadAffix ra) {
				return ra.getPrefix();
			}
			public boolean isEditable(RoadAffix ra) {
				return canWrite(ra);
			}
			public void setValueAt(RoadAffix ra, Object value) {
				if (value instanceof Boolean)
					ra.setPrefix((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<RoadAffix>("location.road.affix.fixup",
			200)
		{
			public Object getValueAt(RoadAffix ra) {
				return ra.getFixup();
			}
			public boolean isEditable(RoadAffix ra) {
				return canWrite(ra);
			}
			public void setValueAt(RoadAffix ra, Object value) {
				String v = value.toString().trim();
				ra.setFixup((v.length() > 0) ? v : null);
			}
		});
		cols.add(new ProxyColumn<RoadAffix>(
			"location.road.affix.allow.retain", 120, Boolean.class)
		{
			public Object getValueAt(RoadAffix ra) {
				return ra.getAllowRetain();
			}
			public boolean isEditable(RoadAffix ra) {
				return canWrite(ra);
			}
			public void setValueAt(RoadAffix ra, Object value) {
				if (value instanceof Boolean)
					ra.setAllowRetain((Boolean) value);
			}
		});
		return cols;
	}

	/** Create a new road affix table model */
	public RoadAffixModel(Session s) {
		super(s, descriptor(s), 16);
	}
}
