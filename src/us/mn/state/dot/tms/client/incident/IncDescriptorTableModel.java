/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.IncDescriptor;
import us.mn.state.dot.tms.IncDetail;
import us.mn.state.dot.tms.LaneCode;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Table model for incident descriptors.
 *
 * @author Douglas Lau
 */
public class IncDescriptorTableModel extends ProxyTableModel<IncDescriptor> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<IncDescriptor> descriptor(Session s) {
		return new ProxyDescriptor<IncDescriptor>(
			s.getSonarState().getIncCache().getIncDescriptors(),
			false,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Create the incident type combo box */
	static private JComboBox<EventType> createIncTypeCombo() {
		return new JComboBox<EventType>(new EventType[] {
			EventType.INCIDENT_CRASH,
			EventType.INCIDENT_STALL,
			EventType.INCIDENT_ROADWORK,
			EventType.INCIDENT_HAZARD
		});
	}

	/** Renderer for event types names in a table cell */
	static private class EventTypeCellRenderer
		extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			return super.getTableCellRendererComponent(table,
				eventTypeDesc(value), isSelected, hasFocus, row,
				column);
		}
	}

	/** Get a string value of an event type */
	static private Object eventTypeDesc(Object value) {
		if (value instanceof EventType) {
			EventType et = (EventType) value;
			return EventTypeRenderer.eventTypeToString(et);
		} else
			return value;
	}

	/** Renderer for incident details in a table cell */
	static private class IncDetailCellRenderer
		extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			return super.getTableCellRendererComponent(table,
				detailDesc(value), isSelected, hasFocus, row,
				column);
		}
	}

	/** Get a string value of an incident detail */
	static private Object detailDesc(Object value) {
		if (value instanceof IncDetail) {
			IncDetail dtl = (IncDetail) value;
			return dtl.getDescription();
		} else
			return value;
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<IncDescriptor>> createColumns() {
		ArrayList<ProxyColumn<IncDescriptor>> cols =
			new ArrayList<ProxyColumn<IncDescriptor>>(4);
		cols.add(new ProxyColumn<IncDescriptor>("incident.type", 100) {
			public Object getValueAt(IncDescriptor dsc) {
				return EventType.fromId(dsc.getEventType());
			}
			public boolean isEditable(IncDescriptor dsc) {
				return canWrite(dsc);
			}
			public void setValueAt(IncDescriptor dsc, Object value){
				if (value instanceof EventType) {
					EventType et = (EventType) value;
					dsc.setEventType(et.id);
				}
			}
			protected TableCellRenderer createCellRenderer() {
				return new EventTypeCellRenderer();
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<EventType> cbx = createIncTypeCombo();
				cbx.setRenderer(new EventTypeRenderer());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<IncDescriptor>("incident.detail",
			128)
		{
			public Object getValueAt(IncDescriptor dsc) {
				return dsc.getDetail();
			}
			public boolean isEditable(IncDescriptor dsc) {
				return canWrite(dsc);
			}
			public void setValueAt(IncDescriptor dsc, Object value){
				if (value instanceof IncDetail)
					dsc.setDetail((IncDetail) value);
				else
					dsc.setDetail(null);
			}
			protected TableCellRenderer createCellRenderer() {
				return new IncDetailCellRenderer();
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<IncDetail> cbx =
					new JComboBox<IncDetail>();
				cbx.setRenderer(new IncDetailRenderer());
				cbx.setModel(new IComboBoxModel<IncDetail>(
					detail_mdl));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<IncDescriptor>("incident.lane_code",
			96)
		{
			public Object getValueAt(IncDescriptor dsc) {
				return LaneCode.fromCode(dsc.getLaneCode());
			}
			public boolean isEditable(IncDescriptor dsc) {
				return canWrite(dsc);
			}
			public void setValueAt(IncDescriptor dsc, Object value){
				if (value instanceof LaneCode) {
					LaneCode lc = (LaneCode) value;
					dsc.setLaneCode(lc.lcode);
				}
			}
			protected TableCellEditor createCellEditor() {
				return new DefaultCellEditor(IncidentCreator
					.createLaneCodeCombo());
			}
		});
		cols.add(new ProxyColumn<IncDescriptor>("dms.multi.string", 300)
		{
			public Object getValueAt(IncDescriptor dsc) {
				return dsc.getMulti();
			}
			public boolean isEditable(IncDescriptor dsc) {
				return canWrite(dsc);
			}
			public void setValueAt(IncDescriptor dsc, Object value){
				dsc.setMulti(new MultiString(value.toString())
					.normalizeLine().toString());
			}
		});
		return cols;
	}

	/** Incident detail proxy list model */
	private final ProxyListModel<IncDetail> detail_mdl;

	/** Create a new table model.
	 * @param s Session */
	public IncDescriptorTableModel(Session s) {
		super(s, descriptor(s), 12, 20);
		detail_mdl = new ProxyListModel<IncDetail>(
			s.getSonarState().getIncCache().getIncDetails());
	}

	/** Initialize the model */
	@Override
	public void initialize() {
		super.initialize();
		detail_mdl.initialize();
	}

	/** Dispose of the model */
	@Override
	public void dispose() {
		detail_mdl.dispose();
		super.dispose();
	}

	/** Get a proxy comparator */
	@Override
	protected Comparator<IncDescriptor> comparator() {
		return new Comparator<IncDescriptor>() {
			public int compare(IncDescriptor dsc0,
				IncDescriptor dsc1)
			{
				int et0 = dsc0.getEventType();
				int et1 = dsc1.getEventType();
				if (et0 != et1)
					return et0 - et1;
				IncDetail dt0 = dsc0.getDetail();
				IncDetail dt1 = dsc1.getDetail();
				if (dt0 != dt1) {
					if (dt0 == null)
						return -1;
					else if (dt1 == null)
						return 1;
					else {
						return dt0.getName().compareTo(
						       dt1.getName());
					}
				}
				String lc0 = dsc0.getLaneCode();
				String lc1 = dsc1.getLaneCode();
				if (!lc0.equals(lc1))
					return lc0.compareTo(lc1);
				return dsc0.getName().compareTo(dsc1.getName());
			}
		};
	}

	/** Create a new incident descriptor */
	@Override
	public void createObject(String name_ignore) {
		String name = createUniqueName();
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("event_desc_id", EventType.INCIDENT_CRASH.id);
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Create a unique incident descriptor name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 99999; uid++) {
			String n = String.format("idsc_%05d", uid);
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}
}
