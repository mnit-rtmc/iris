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
package us.mn.state.dot.tms.client.marking;

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * Table model for lane markings.
 *
 * @author Douglas Lau
 */
public class LaneMarkingModel extends ProxyTableModel<LaneMarking> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<LaneMarking>("Lane Marking", 120) {
			public Object getValueAt(LaneMarking lm) {
				return lm.getName();
			}
			public boolean isEditable(LaneMarking lm) {
				return (lm == null) && canAdd();
			}
			public void setValueAt(LaneMarking lm, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<LaneMarking>("Location", 300) {
			public Object getValueAt(LaneMarking lm) {
				return GeoLocHelper.getDescription(
					lm.getGeoLoc());
			}
		}
	    };
	}

	/** Create a new lane marking table model */
	public LaneMarkingModel(Session s) {
		super(s, s.getSonarState().getLaneMarkings());
	}

	/** Check if the user can add a lane marking */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(LaneMarking.SONAR_TYPE,
			"oname"));
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<LaneMarking> createPropertiesForm(
		LaneMarking proxy)
	{
		return new LaneMarkingProperties(session, proxy);
	}
}
