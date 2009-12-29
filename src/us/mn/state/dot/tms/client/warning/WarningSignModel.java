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
package us.mn.state.dot.tms.client.warning;

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * Table model for warning signs.
 *
 * @author Douglas Lau
 */
public class WarningSignModel extends ProxyTableModel<WarningSign> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<WarningSign>("Warning Sign", 200) {
			public Object getValueAt(WarningSign ws) {
				return ws.getName();
			}
			public boolean isEditable(WarningSign ws) {
				return (ws == null) && canAdd();
			}
			public void setValueAt(WarningSign ws, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<WarningSign>("Location", 300) {
			public Object getValueAt(WarningSign ws) {
				return GeoLocHelper.getDescription(
					ws.getGeoLoc());
			}
		}
	    };
	}

	/** Create a new warning sign table model */
	public WarningSignModel(Session s) {
		super(s, s.getSonarState().getWarningSigns());
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<WarningSign> createPropertiesForm(
		WarningSign proxy)
	{
		return new WarningSignProperties(session, proxy);
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(WarningSign.SONAR_TYPE,
			"oname"));
	}
}
