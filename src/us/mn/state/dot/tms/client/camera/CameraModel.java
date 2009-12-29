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
package us.mn.state.dot.tms.client.camera;

import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * Table model for cameras
 *
 * @author Douglas Lau
 */
public class CameraModel extends ProxyTableModel<Camera> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Camera>("Camera", 200) {
			public Object getValueAt(Camera c) {
				return c.getName();
			}
			public boolean isEditable(Camera c) {
				return (c == null) && canAdd();
			}
			public void setValueAt(Camera c, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<Camera>("Location", 300) {
			public Object getValueAt(Camera c) {
				return GeoLocHelper.getDescription(
					c.getGeoLoc());
			}
		},
		new ProxyColumn<Camera>("Publish", 120, Boolean.class) {
			public Object getValueAt(Camera c) {
				return c.getPublish();
			}
			public boolean isEditable(Camera c) {
				return canUpdate(c);
			}
			public void setValueAt(Camera c, Object value) {
				if(value instanceof Boolean)
					c.setPublish((Boolean)value);
			}
		}
	    };
	}

	/** Create a new camera table model */
	public CameraModel(Session s) {
		super(s, s.getSonarState().getCamCache().getCameras());
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<Camera> createPropertiesForm(Camera proxy) {
		return new CameraProperties(session, proxy);
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Camera.SONAR_TYPE;
	}
}
