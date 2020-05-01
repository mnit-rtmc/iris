/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.EncodingQuality;
import us.mn.state.dot.tms.FlowStream;
import us.mn.state.dot.tms.FlowStreamStatus;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;

/**
 * Table model for flow streams
 *
 * @author Douglas Lau
 */
public class FlowStreamModel extends ProxyTableModel<FlowStream> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<FlowStream> descriptor(Session s) {
		return new ProxyDescriptor<FlowStream>(
			s.getSonarState().getCamCache().getFlowStreams(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<FlowStream>> createColumns() {
		ArrayList<ProxyColumn<FlowStream>> cols =
			new ArrayList<ProxyColumn<FlowStream>>(9);
		cols.add(new ProxyColumn<FlowStream>("flow.stream", 140) {
			public Object getValueAt(FlowStream fs) {
				return fs.getName();
			}
		});
		cols.add(new ProxyColumn<FlowStream>("video.restricted", 100,
			Boolean.class)
		{
			public Object getValueAt(FlowStream fs) {
				return fs.getRestricted();
			}
			public boolean isEditable(FlowStream fs) {
				return canWrite(fs, "restricted");
			}
			public void setValueAt(FlowStream fs, Object value) {
				if (value instanceof Boolean)
					fs.setRestricted((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<FlowStream>("flow.stream.loc.overlay",
			120, Boolean.class)
		{
			public Object getValueAt(FlowStream fs) {
				return fs.getLocOverlay();
			}
			public boolean isEditable(FlowStream fs) {
				return canWrite(fs, "locOverlay");
			}
			public void setValueAt(FlowStream fs, Object value) {
				if (value instanceof Boolean)
					fs.setLocOverlay((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<FlowStream>("flow.stream.quality", 80){
			public Object getValueAt(FlowStream fs) {
				return EncodingQuality.fromOrdinal(
					fs.getQuality());
			}
			public boolean isEditable(FlowStream fs) {
				return canWrite(fs, "quality");
			}
			public void setValueAt(FlowStream fs, Object value) {
				if (value instanceof EncodingQuality) {
					EncodingQuality q =
						(EncodingQuality) value;
					fs.setQuality(q.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				return new DefaultCellEditor(new JComboBox
					<EncodingQuality>(EncodingQuality
					.values()));
			}
		});
		cols.add(new ProxyColumn<FlowStream>("camera", 160) {
			public Object getValueAt(FlowStream fs) {
				return fs.getCamera();
			}
			public boolean isEditable(FlowStream fs) {
				return canWrite(fs, "camera");
			}
			public void setValueAt(FlowStream fs, Object value) {
				fs.setCamera((value instanceof Camera) ?
					(Camera) value : null);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<Camera> cbx = new JComboBox<Camera>();
				cbx.setModel(new IComboBoxModel<Camera>(
					camera_mdl));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<FlowStream>("video.monitor.num", 90,
			Integer.class)
		{
			public Object getValueAt(FlowStream fs) {
				return fs.getMonNum();
			}
			public boolean isEditable(FlowStream fs) {
				return canWrite(fs, "monNum");
			}
			public void setValueAt(FlowStream fs, Object value) {
				fs.setMonNum((value instanceof Integer)
					? (Integer) value
					: null);
			}
		});
		cols.add(new ProxyColumn<FlowStream>("flow.stream.address", 110)
		{
			public Object getValueAt(FlowStream fs) {
				return fs.getAddress();
			}
			public boolean isEditable(FlowStream fs) {
				return canWrite(fs, "address");
			}
			public void setValueAt(FlowStream fs, Object value) {
				String v = value.toString().trim();
				fs.setAddress(v.length() > 0 ? v : null);
			}
		});
		cols.add(new ProxyColumn<FlowStream>("flow.stream.port", 90,
			Integer.class)
		{
			public Object getValueAt(FlowStream fs) {
				return fs.getPort();
			}
			public boolean isEditable(FlowStream fs) {
				return canWrite(fs, "port");
			}
			public void setValueAt(FlowStream fs, Object value) {
				fs.setPort((value instanceof Integer) ?
					(Integer) value : null);
			}
		});
		cols.add(new ProxyColumn<FlowStream>("flow.stream.status", 120){
			public Object getValueAt(FlowStream fs) {
				return FlowStreamStatus.fromOrdinal(
					fs.getStatus());
			}
		});
		return cols;
	}

	/** Camera proxy list model */
	private final ProxyListModel<Camera> camera_mdl;

	/** Create a new flow stream table model */
	public FlowStreamModel(Session s) {
		super(s, descriptor(s), 16);
		camera_mdl = s.getSonarState().getCamCache().getCameraModel();
	}
}
