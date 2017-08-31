/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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

import java.util.ArrayList;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for cameras
 *
 * @author Douglas Lau
 */
public class CameraModel extends ProxyTableModel<Camera> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Camera>> createColumns() {
		ArrayList<ProxyColumn<Camera>> cols =
			new ArrayList<ProxyColumn<Camera>>(3);
		cols.add(new ProxyColumn<Camera>("camera", 200) {
			public Object getValueAt(Camera c) {
				return c.getName();
			}
		});
		cols.add(new ProxyColumn<Camera>("location", 300) {
			public Object getValueAt(Camera c) {
				return GeoLocHelper.getDescription(
					c.getGeoLoc());
			}
		});
		cols.add(new ProxyColumn<Camera>("camera.num", 90,
			Integer.class)
		{
			public Object getValueAt(Camera c) {
				return c.getCamNum();
			}
			public boolean isEditable(Camera c) {
				return canWrite(c);
			}
			public void setValueAt(Camera c, Object value) {
				if (value instanceof Integer)
					c.setCamNum((Integer) value);
				else
					c.setCamNum(null);
			}
		});
		cols.add(new ProxyColumn<Camera>("camera.publish", 120,
			Boolean.class)
		{
			public Object getValueAt(Camera c) {
				return c.getPublish();
			}
			public boolean isEditable(Camera c) {
				return canWrite(c);
			}
			public void setValueAt(Camera c, Object value) {
				if (value instanceof Boolean)
					c.setPublish((Boolean)value);
			}
		});
		return cols;
	}

	/** Create a new camera table model */
	public CameraModel(Session s) {
		super(s, CameraManager.descriptor(s), 12);
	}
}
