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
package us.mn.state.dot.tms.client.meter;

import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * Table model for ramp meters.
 *
 * @author Douglas Lau
 */
public class RampMeterModel extends ProxyTableModel<RampMeter> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<RampMeter>("Meter", 200) {
			public Object getValueAt(RampMeter rm) {
				return rm.getName();
			}
			public boolean isEditable(RampMeter rm) {
				return (rm == null) && canAdd();
			}
			public void setValueAt(RampMeter rm, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<RampMeter>("Location", 300) {
			public Object getValueAt(RampMeter rm) {
				return GeoLocHelper.getDescription(
					rm.getGeoLoc());
			}
		}
	    };
	}

	/** Create a new ramp meter table model */
	public RampMeterModel(Session s) {
		super(s, s.getSonarState().getRampMeters());
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<RampMeter> createPropertiesForm(
		RampMeter proxy)
	{
		return new RampMeterProperties(session, proxy);
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return RampMeter.SONAR_TYPE;
	}
}
