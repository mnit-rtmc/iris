/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
import javax.swing.JComboBox;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.Encoding;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for encoder types.
 *
 * @author Douglas Lau
 */
public class EncoderTypeModel extends ProxyTableModel<EncoderType> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<EncoderType> descriptor(Session s) {
		return new ProxyDescriptor<EncoderType>(
			s.getSonarState().getCamCache().getEncoderTypes(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<EncoderType>> createColumns() {
		ArrayList<ProxyColumn<EncoderType>> cols =
			new ArrayList<ProxyColumn<EncoderType>>(5);
		cols.add(new ProxyColumn<EncoderType>("camera.encoder.type",
			120)
		{
			public Object getValueAt(EncoderType et) {
				return et.getName();
			}
		});
		cols.add(new ProxyColumn<EncoderType>("camera.encoder.encoding",
			96)
		{
			public Object getValueAt(EncoderType et) {
				return Encoding.fromOrdinal(et.getEncoding());
			}
			public boolean isEditable(EncoderType et) {
				return canUpdate(et, "encoding");
			}
			public void setValueAt(EncoderType et, Object value) {
				if (value instanceof Encoding) {
					Encoding e = (Encoding) value;
					et.setEncoding(e.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				return new DefaultCellEditor(new JComboBox
					<Encoding>(Encoding.values()));
			}
		});
		cols.add(new ProxyColumn<EncoderType>(
			"camera.encoder.uri.scheme", 100)
		{
			public Object getValueAt(EncoderType et) {
				return et.getUriScheme();
			}
			public boolean isEditable(EncoderType et) {
				return canUpdate(et, "uriScheme");
			}
			public void setValueAt(EncoderType et, Object value) {
				et.setUriScheme(value.toString());
			}
		});
		cols.add(new ProxyColumn<EncoderType>(
			"camera.encoder.uri.path", 200)
		{
			public Object getValueAt(EncoderType et) {
				return et.getUriPath();
			}
			public boolean isEditable(EncoderType et) {
				return canUpdate(et, "uriPath");
			}
			public void setValueAt(EncoderType et, Object value) {
				et.setUriPath(value.toString());
			}
		});
		cols.add(new ProxyColumn<EncoderType>("camera.encoder.latency",
			90, Integer.class)
		{
			public Object getValueAt(EncoderType et) {
				return et.getLatency();
			}
			public boolean isEditable(EncoderType et) {
				return canUpdate(et, "latency");
			}
			public void setValueAt(EncoderType et, Object value) {
				if (value instanceof Integer)
					et.setLatency((Integer) value);
			}
		});
		return cols;
	}

	/** Create a new encoder type table model */
	public EncoderTypeModel(Session s) {
		super(s, descriptor(s), 12);
	}
}
