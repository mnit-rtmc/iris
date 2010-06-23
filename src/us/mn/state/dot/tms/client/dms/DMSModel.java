/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Table model for Dynamic Message Signs.
 *
 * @author Douglas Lau
 */
public class DMSModel extends ProxyTableModel<DMS> {

	/** DMS abbreviation */
	private static final String dms_abr = I18N.get("dms.abbreviation");

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<DMS>(dms_abr, 200) {
			public Object getValueAt(DMS d) {
				return d.getName();
			}
			public boolean isEditable(DMS d) {
				return (d == null) && canAdd();
			}
			public void setValueAt(DMS d, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<DMS>("Location", 300) {
			public Object getValueAt(DMS d) {
				return GeoLocHelper.getDescription(
					d.getGeoLoc());
			}
		}
	    };
	}

	/** Create a new DMS table model */
	public DMSModel(Session s) {
		super(s, s.getSonarState().getDmsCache().getDMSs());
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<DMS> createPropertiesForm(DMS proxy) {
		return new DMSProperties(session, proxy);
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return DMS.SONAR_TYPE;
	}
}
