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
package us.mn.state.dot.tms.client.system;

import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for incident details.
 *
 * @author Douglas Lau
 */
public class IncidentDetailModel extends ProxyTableModel<IncidentDetail> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<IncidentDetail>("Name", 90) {
			public Object getValueAt(IncidentDetail dtl) {
				return dtl.getName();
			}
			public boolean isEditable(IncidentDetail dtl) {
				return (dtl == null) && canAdd();
			}
			public void setValueAt(IncidentDetail dtl,
				Object value)
			{
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<IncidentDetail>("Description", 200) {
			public Object getValueAt(IncidentDetail dtl) {
				return dtl.getDescription();
			}
			public boolean isEditable(IncidentDetail dtl) {
				return canUpdate(dtl);
			}
			public void setValueAt(IncidentDetail dtl,
				Object value)
			{
				dtl.setDescription(value.toString());
			}
		}
	    };
	}

	/** Create a new incident detail table model */
	public IncidentDetailModel(Session s) {
		super(s, s.getSonarState().getIncidentDetails());
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return IncidentDetail.SONAR_TYPE;
	}
}
