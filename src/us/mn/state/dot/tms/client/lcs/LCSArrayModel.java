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
package us.mn.state.dot.tms.client.lcs;

import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * Table model for LCS arrays.
 *
 * @author Douglas Lau
 */
public class LCSArrayModel extends ProxyTableModel<LCSArray> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<LCSArray>("LCS Array", 200) {
			public Object getValueAt(LCSArray a) {
				return a.getName();
			}
			public boolean isEditable(LCSArray a) {
				return (a == null) && canAdd();
			}
			public void setValueAt(LCSArray a, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<LCSArray>("Location", 300) {
			public Object getValueAt(LCSArray a) {
				return LCSArrayHelper.lookupLocation(a);
			}
		}
	    };
	}

	/** Create a new LCS array table model */
	public LCSArrayModel(Session s) {
		super(s, s.getSonarState().getLcsCache().getLCSArrays());
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<LCSArray> createPropertiesForm(
		LCSArray proxy)
	{
		return new LCSArrayProperties(session, proxy);
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return LCSArray.SONAR_TYPE;
	}
}
