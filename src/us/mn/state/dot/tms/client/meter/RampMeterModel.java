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
package us.mn.state.dot.tms.client.meter;

import java.util.ArrayList;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for ramp meters.
 *
 * @author Douglas Lau
 */
public class RampMeterModel extends ProxyTableModel<RampMeter> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<RampMeter>> createColumns() {
		ArrayList<ProxyColumn<RampMeter>> cols =
			new ArrayList<ProxyColumn<RampMeter>>(2);
		cols.add(new ProxyColumn<RampMeter>("ramp_meter", 200) {
			public Object getValueAt(RampMeter rm) {
				return rm.getName();
			}
		});
		cols.add(new ProxyColumn<RampMeter>("location", 300) {
			public Object getValueAt(RampMeter rm) {
				return GeoLocHelper.getOnRampDescription(
					rm.getGeoLoc());
			}
		});
		return cols;
	}

	/** Create a new ramp meter table model */
	public RampMeterModel(Session s) {
		super(s, MeterManager.descriptor(s), 16);
	}
}
