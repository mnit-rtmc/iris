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
package us.mn.state.dot.tms.client.dms;

import java.util.ArrayList;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
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
			new ArrayList<ProxyColumn<DMS>>(9);
		cols.add(new ProxyColumn<DMS>("dms", 120) {
			public Object getValueAt(DMS d) {
				return d.getName();
			}
		});
		cols.add(new ProxyColumn<DMS>("location", 240) {
			public Object getValueAt(DMS d) {
				return GeoLocHelper.getDescription(
					d.getGeoLoc());
			}
		});
		cols.add(new ProxyColumn<DMS>("location.dir", 32) {
			public Object getValueAt(DMS d) {
				return DMSHelper.getRoadDir(d);
			}
		});
		if (SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			cols.add(new ProxyColumn<DMS>("dms.aws.allowed", 80,
				Boolean.class)
			{
				public Object getValueAt(DMS d) {
					return d.getAwsAllowed();
				}
			});
			cols.add(new ProxyColumn<DMS>(
				"item.style.aws.controlled", 80, Boolean.class)
			{
				public Object getValueAt(DMS d) {
					return d.getAwsControlled();
				}
			});
		}
		cols.add(new ProxyColumn<DMS>("device.status", 160) {
			public Object getValueAt(DMS d) {
				return DMSHelper.getAllStyles(d);
			}
		});
		cols.add(new ProxyColumn<DMS>("dms.model", 100) {
			public Object getValueAt(DMS d) {
				return d.getModel();
			}
		});
		cols.add(new ProxyColumn<DMS>("dms.access", 140) {
			public Object getValueAt(DMS d) {
				return d.getSignAccess();
			}
		});
		return cols;
	}

	/** Create a new DMS table model */
	public DMSModel(Session s) {
		super(s, DMSManager.descriptor(s), 16);
	}
}
