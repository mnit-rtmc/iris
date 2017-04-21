/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.MonitorStyle;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;

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
			new ArrayList<ProxyColumn<VideoMonitor>>(5);
		cols.add(new ProxyColumn<VideoMonitor>("video.monitor", 140) {
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
		cols.add(new ProxyColumn<VideoMonitor>("video.monitor.num",
			90, Integer.class)
		{
			public Object getValueAt(VideoMonitor vm) {
				return vm.getMonNum();
			}
			public boolean isEditable(VideoMonitor vm) {
				return canUpdate(vm);
			}
			public void setValueAt(VideoMonitor vm, Object value) {
				if (value instanceof Integer)
					vm.setMonNum((Integer) value);
			}
		});
		cols.add(new ProxyColumn<VideoMonitor>("video.restricted", 100,
			Boolean.class)
		{
			public Object getValueAt(VideoMonitor vm) {
				return vm.getRestricted();
			}
			public boolean isEditable(VideoMonitor vm) {
				return canUpdate(vm, "restricted");
			}
			public void setValueAt(VideoMonitor vm, Object value) {
				if (value instanceof Boolean)
					vm.setRestricted((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<VideoMonitor>("monitor.style", 160) {
			public Object getValueAt(VideoMonitor vm) {
				return vm.getMonitorStyle();
			}
			public boolean isEditable(VideoMonitor vm) {
				return canUpdate(vm, "monitorStyle");
			}
			public void setValueAt(VideoMonitor vm, Object value) {
				if (value instanceof MonitorStyle)
					vm.setMonitorStyle((MonitorStyle)value);
				else
					vm.setMonitorStyle(null);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<MonitorStyle> cbx =
					new JComboBox<MonitorStyle>();
				cbx.setModel(new IComboBoxModel<MonitorStyle>(
					style_mdl));
				return new DefaultCellEditor(cbx);
			}
		});
		return cols;
	}

	/** Monitor style proxy list model */
	private final ProxyListModel<MonitorStyle> style_mdl;

	/** Create a new video monitor table model */
	public VideoMonitorModel(Session s) {
		super(s, descriptor(s), 16);
		style_mdl = new ProxyListModel<MonitorStyle>(
			s.getSonarState().getCamCache().getMonitorStyles());
	}

	/** Initialize the model */
	@Override
	public void initialize() {
		super.initialize();
		style_mdl.initialize();
	}

	/** Dispose of the model */
	@Override
	public void dispose() {
		style_mdl.dispose();
		super.dispose();
	}
}
