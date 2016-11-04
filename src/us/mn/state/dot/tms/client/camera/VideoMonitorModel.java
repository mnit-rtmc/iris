/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for video monitors
 *
 * @author Douglas Lau
 */
public class VideoMonitorModel extends ProxyTableModel<VideoMonitor> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<VideoMonitor> descriptor(Session s) {
		return new ProxyDescriptor<VideoMonitor>(
			s.getSonarState().getCamCache().getVideoMonitors(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<VideoMonitor>> createColumns() {
		ArrayList<ProxyColumn<VideoMonitor>> cols =
			new ArrayList<ProxyColumn<VideoMonitor>>(3);
		cols.add(new ProxyColumn<VideoMonitor>("video.monitor", 160) {
			public Object getValueAt(VideoMonitor vm) {
				return vm.getName();
			}
		});
		cols.add(new ProxyColumn<VideoMonitor>("device.notes", 300) {
			public Object getValueAt(VideoMonitor vm) {
				return vm.getNotes();
			}
			public boolean isEditable(VideoMonitor vm) {
				return canUpdate(vm);
			}
			public void setValueAt(VideoMonitor vm, Object value) {
				vm.setNotes(value.toString());
			}
		});
		cols.add(new ProxyColumn<VideoMonitor>("video.restricted", 120,
			Boolean.class)
		{
			public Object getValueAt(VideoMonitor vm) {
				return vm.getRestricted();
			}
			public boolean isEditable(VideoMonitor vm) {
				return canUpdate(vm);
			}
			public void setValueAt(VideoMonitor vm, Object value) {
				if (value instanceof Boolean)
					vm.setRestricted((Boolean)value);
			}
		});
		return cols;
	}

	/** Create a new video monitor table model */
	public VideoMonitorModel(Session s) {
		super(s, descriptor(s), 16);
	}
}
