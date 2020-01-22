/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  Minnesota Department of Transportation
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
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import us.mn.state.dot.tms.EncoderStream;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.Encoding;
import us.mn.state.dot.tms.EncodingQuality;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for encoder streams.
 *
 * @author Douglas Lau
 */
public class EncoderStreamModel extends ProxyTableModel<EncoderStream> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<EncoderStream> descriptor(Session s) {
		return new ProxyDescriptor<EncoderStream>(
			s.getSonarState().getCamCache().getEncoderStreams(),
			false,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<EncoderStream>> createColumns() {
		ArrayList<ProxyColumn<EncoderStream>> cols =
			new ArrayList<ProxyColumn<EncoderStream>>(8);
		cols.add(new ProxyColumn<EncoderStream>(
			"encoder.stream.view.num", 80, Integer.class)
		{
			public Object getValueAt(EncoderStream es) {
				return es.getViewNum();
			}
			public boolean isEditable(EncoderStream es) {
				return canWrite(es, "viewNum");
			}
			public void setValueAt(EncoderStream es, Object value) {
				Integer view = (value instanceof Integer)
					? (Integer) value
					: null;
				es.setViewNum(view);
			}
		});
		cols.add(new ProxyColumn<EncoderStream>("encoder.stream.flow",
			90, Boolean.class)
		{
			public Object getValueAt(EncoderStream es) {
				return es.getFlowStream();
			}
			public boolean isEditable(EncoderStream es) {
				return canWrite(es, "flowStream");
			}
			public void setValueAt(EncoderStream es, Object value) {
				if (value instanceof Boolean)
					es.setFlowStream((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<EncoderStream>(
			"encoder.stream.encoding", 90)
		{
			public Object getValueAt(EncoderStream es) {
				return Encoding.fromOrdinal(es.getEncoding());
			}
			public boolean isEditable(EncoderStream es) {
				return canWrite(es, "encoding");
			}
			public void setValueAt(EncoderStream es, Object value) {
				if (value instanceof Encoding) {
					Encoding e = (Encoding) value;
					es.setEncoding(e.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				return new DefaultCellEditor(new JComboBox
					<Encoding>(Encoding.values()));
			}
		});
		cols.add(new ProxyColumn<EncoderStream>(
			"encoder.stream.quality", 80)
		{
			public Object getValueAt(EncoderStream es) {
				return EncodingQuality.fromOrdinal(
					es.getQuality());
			}
			public boolean isEditable(EncoderStream es) {
				return canWrite(es, "quality");
			}
			public void setValueAt(EncoderStream es, Object value) {
				if (value instanceof EncodingQuality) {
					EncodingQuality q =
						(EncodingQuality) value;
					es.setQuality(q.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				return new DefaultCellEditor(new JComboBox
					<EncodingQuality>(EncodingQuality
					.values()));
			}
		});
		cols.add(new ProxyColumn<EncoderStream>(
			"encoder.stream.uri.scheme", 100)
		{
			public Object getValueAt(EncoderStream es) {
				return es.getUriScheme();
			}
			public boolean isEditable(EncoderStream es) {
				return canWrite(es, "uriScheme");
			}
			public void setValueAt(EncoderStream es, Object value) {
				String s = value.toString().trim();
				es.setUriScheme((s.length() > 0) ? s : null);
			}
		});
		cols.add(new ProxyColumn<EncoderStream>(
			"encoder.stream.uri.path", 240)
		{
			public Object getValueAt(EncoderStream es) {
				return es.getUriPath();
			}
			public boolean isEditable(EncoderStream es) {
				return canWrite(es, "uriPath");
			}
			public void setValueAt(EncoderStream es, Object value) {
				String p = value.toString().trim();
				es.setUriPath((p.length() > 0) ? p : null);
			}
		});
		cols.add(new ProxyColumn<EncoderStream>(
			"encoder.stream.mcast.port", 100, Integer.class)
		{
			public Object getValueAt(EncoderStream es) {
				return es.getMcastPort();
			}
			public boolean isEditable(EncoderStream es) {
				return canWrite(es, "mcastPort");
			}
			public void setValueAt(EncoderStream es, Object value) {
				Integer port = (value instanceof Integer)
					? (Integer) value
					: null;
				es.setMcastPort(port);
			}
		});
		cols.add(new ProxyColumn<EncoderStream>("encoder.stream.latency",
			100, Integer.class)
		{
			public Object getValueAt(EncoderStream es) {
				return es.getLatency();
			}
			public boolean isEditable(EncoderStream es) {
				return canWrite(es, "latency");
			}
			public void setValueAt(EncoderStream es, Object value) {
				if (value instanceof Integer)
					es.setLatency((Integer) value);
			}
		});
		return cols;
	}

	/** Encoder type associated with streams */
	private final EncoderType encoder_type;

	/** Create a new encoder stream table model */
	public EncoderStreamModel(Session s, EncoderType et) {
		super(s, descriptor(s), 16);
		encoder_type = et;
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(EncoderStream proxy) {
		return proxy.getEncoderType() == encoder_type;
	}

	/** Get a table row sorter */
	@Override
	public RowSorter<ProxyTableModel<EncoderStream>> createSorter() {
		TableRowSorter<ProxyTableModel<EncoderStream>> sorter =
			new TableRowSorter<ProxyTableModel<EncoderStream>>(this)
		{
			@Override public boolean isSortable(int c) {
				return true;
			}
		};
		sorter.setSortsOnUpdates(true);
		sorter.setComparator(3, new Comparator<EncodingQuality>() {
			public int compare(EncodingQuality eq0,
				EncodingQuality eq1)
			{
				return eq0.ordinal() - eq1.ordinal();
			}
		});
		ArrayList<RowSorter.SortKey> keys =
			new ArrayList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		keys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
		keys.add(new RowSorter.SortKey(6, SortOrder.DESCENDING));
		keys.add(new RowSorter.SortKey(5, SortOrder.ASCENDING));
		sorter.setSortKeys(keys);
		return sorter;
	}

	/** Create an object with the given name.
	 * @param tn Type name. */
	@Override
	public void createObject(String tn) {
		String name = createUniqueName();
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("encoder_type", encoder_type);
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Create a unique stream name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 9999; uid++) {
			String n = "est_" + uid;
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		assert false;
		return null;
	}
}
