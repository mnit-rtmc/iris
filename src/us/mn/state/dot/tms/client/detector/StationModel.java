/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.detector;

import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for stations
 *
 * @author Douglas Lau
 */
public class StationModel extends ProxyTableModel<Station> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Station>("Station", 60) {
			public Object getValueAt(Station s) {
				return s.getName();
			}
		},
		new ProxyColumn<Station>("Label", 150) {
			public Object getValueAt(Station s) {
				return StationHelper.getLabel(s);
			}
		}
	    };
	}

	/** Create a new station table model */
	public StationModel(Session s) {
		super(s, s.getSonarState().getDetCache().getStations());
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		synchronized(proxies) {
			return proxies.size();
		}
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Station.SONAR_TYPE;
	}

	/** Determine if delete button is available */
	public boolean hasDelete() {
		return false;
	}
}
