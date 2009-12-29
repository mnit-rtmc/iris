/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for video monitors
 *
 * @author Douglas Lau
 */
public class VideoMonitorModel extends ProxyTableModel<VideoMonitor> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<VideoMonitor>("Video Monitor", 160) {
			public Object getValueAt(VideoMonitor vm) {
				return vm.getName();
			}
			public boolean isEditable(VideoMonitor vm) {
				return (vm == null) && canAdd();
			}
			public void setValueAt(VideoMonitor vm, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<VideoMonitor>("Description", 300) {
			public Object getValueAt(VideoMonitor vm) {
				return vm.getDescription();
			}
			public boolean isEditable(VideoMonitor vm) {
				return canUpdate(vm);
			}
			public void setValueAt(VideoMonitor vm, Object value) {
				vm.setDescription(value.toString());
			}
		},
		new ProxyColumn<VideoMonitor>("Restricted", 120, Boolean.class){
			public Object getValueAt(VideoMonitor vm) {
				return vm.getRestricted();
			}
			public boolean isEditable(VideoMonitor vm) {
				return canUpdate(vm);
			}
			public void setValueAt(VideoMonitor vm, Object value) {
				if(value instanceof Boolean)
					vm.setRestricted((Boolean)value);
			}
		}
	    };
	}

	/** Create a new video monitor table model */
	public VideoMonitorModel(Session s) {
		super(s, s.getSonarState().getCamCache().getVideoMonitors());
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return namespace.canAdd(user,
			new Name(VideoMonitor.SONAR_TYPE, "oname"));
	}
}
