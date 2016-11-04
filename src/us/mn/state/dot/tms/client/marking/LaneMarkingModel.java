/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.marking;

import java.util.ArrayList;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for lane markings.
 *
 * @author Douglas Lau
 */
public class LaneMarkingModel extends ProxyTableModel<LaneMarking> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<LaneMarking>> createColumns() {
		ArrayList<ProxyColumn<LaneMarking>> cols =
			new ArrayList<ProxyColumn<LaneMarking>>(2);
		cols.add(new ProxyColumn<LaneMarking>("lane_marking", 120) {
			public Object getValueAt(LaneMarking lm) {
				return lm.getName();
			}
		});
		cols.add(new ProxyColumn<LaneMarking>("location", 300) {
			public Object getValueAt(LaneMarking lm) {
				return GeoLocHelper.getDescription(
					lm.getGeoLoc());
			}
		});
		return cols;
	}

	/** Create a new lane marking table model */
	public LaneMarkingModel(Session s) {
		super(s, LaneMarkingManager.descriptor(s), 12);
	}
}
