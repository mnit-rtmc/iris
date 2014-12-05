/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for Dynamic Message Signs.
 *
 * @author Douglas Lau
 */
public class DMSModel extends ProxyTableModel<DMS> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<DMS>> createColumns() {
		ArrayList<ProxyColumn<DMS>> cols =
			new ArrayList<ProxyColumn<DMS>>(2);
		cols.add(new ProxyColumn<DMS>("dms", 200) {
			public Object getValueAt(DMS d) {
				return d.getName();
			}
		});
		cols.add(new ProxyColumn<DMS>("location", 300) {
			public Object getValueAt(DMS d) {
				return GeoLocHelper.getDescription(
					d.getGeoLoc());
			}
		});
		return cols;
	}

	/** Create a new DMS table model */
	public DMSModel(Session s) {
		super(s, s.getSonarState().getDmsCache().getDMSs(),
		      true,	/* has_properties */
		      true,	/* has_create_delete */
		      true);	/* has_name */
	}

	/** Get the SONAR type name */
	@Override
	protected String getSonarType() {
		return DMS.SONAR_TYPE;
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 12;
	}

	/** Create a properties form for one proxy */
	@Override
	protected DMSProperties createPropertiesForm(DMS proxy) {
		return new DMSProperties(session, proxy);
	}
}
