/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.util.ArrayList;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for DMS table form.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSModel extends ProxyTableModel<DMS> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<DMS>> createColumns() {
		ArrayList<ProxyColumn<DMS>> cols =
			new ArrayList<ProxyColumn<DMS>>(6);
		cols.add(new ProxyColumn<DMS>("dms", 120) {
			public Object getValueAt(DMS d) {
				return d.getName();
			}
		});
		cols.add(new ProxyColumn<DMS>("location", 240) {
			public Object getValueAt(DMS d) {
				return GeoLocHelper.getLocation(d.getGeoLoc());
			}
		});
		cols.add(new ProxyColumn<DMS>("location.dir", 32) {
			public Object getValueAt(DMS d) {
				return DMSHelper.getRoadDir(d);
			}
		});
		cols.add(new ProxyColumn<DMS>("device.status", 160) {
			public Object getValueAt(DMS d) {
				return DMSHelper.getAllStyles(d);
			}
		});
		cols.add(new ProxyColumn<DMS>("dms.model", 100) {
			public Object getValueAt(DMS d) {
				SignDetail sd = d.getSignDetail();
				return (sd != null) ? sd.getSoftwareModel() :"";
			}
		});
		cols.add(new ProxyColumn<DMS>("dms.access", 140) {
			public Object getValueAt(DMS d) {
				SignDetail sd = d.getSignDetail();
				return (sd != null) ? sd.getSignAccess() : "";
			}
		});
		return cols;
	}

	/** Create a new DMS table model */
	public DMSModel(Session s) {
		super(s, DMSManager.descriptor(s), 16);
	}
}
