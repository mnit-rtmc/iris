/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2020  Minnesota Department of Transportation
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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.IncAdvice;
import us.mn.state.dot.tms.IncImpact;
import us.mn.state.dot.tms.IncRange;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Table model for incident advices.
 *
 * @author Douglas Lau
 */
public class IncAdviceTableModel extends ProxyTableModel<IncAdvice> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<IncAdvice> descriptor(Session s) {
		return new ProxyDescriptor<IncAdvice>(
			s.getSonarState().getIncCache().getIncAdvices(),
			false,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<IncAdvice>> createColumns() {
		ArrayList<ProxyColumn<IncAdvice>> cols =
			new ArrayList<ProxyColumn<IncAdvice>>(6);
		cols.add(new ProxyColumn<IncAdvice>("incident.impact", 160) {
			public Object getValueAt(IncAdvice adv) {
				return IncImpact.fromOrdinal(adv.getImpact());
			}
			public boolean isEditable(IncAdvice adv) {
				return canWrite(adv);
			}
			public void setValueAt(IncAdvice adv, Object value) {
				if (value instanceof IncImpact) {
					IncImpact imp = (IncImpact) value;
					adv.setImpact(imp.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<IncImpact> cbx = new JComboBox
					<IncImpact>(IncImpact.values());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<IncAdvice>("incident.lane_type", 80) {
			public Object getValueAt(IncAdvice adv) {
				return LaneType.fromOrdinal(adv.getLaneType());
			}
			public boolean isEditable(IncAdvice adv) {
				return canWrite(adv);
			}
			public void setValueAt(IncAdvice adv, Object value) {
				if (value instanceof LaneType) {
					LaneType lt = (LaneType) value;
					adv.setLaneType((short) lt.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				return new DefaultCellEditor(IncidentCreator
					.createLaneTypeCombo());
			}
		});
		cols.add(new ProxyColumn<IncAdvice>("incident.range", 64) {
			public Object getValueAt(IncAdvice adv) {
				return IncRange.fromOrdinal(adv.getRange());
			}
			public boolean isEditable(IncAdvice adv) {
				return canWrite(adv);
			}
			public void setValueAt(IncAdvice adv, Object value) {
				if (value instanceof IncRange) {
					IncRange r = (IncRange) value;
					adv.setRange(r.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<IncRange> cbx = new JComboBox
					<IncRange>(IncRange.values());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<IncAdvice>("incident.open.lanes", 80,
			Integer.class)
		{
			public Object getValueAt(IncAdvice adv) {
				return adv.getOpenLanes();
			}
			public boolean isEditable(IncAdvice adv) {
				return canWrite(adv);
			}
			public void setValueAt(IncAdvice adv, Object value) {
				adv.setOpenLanes((value instanceof Integer)
					? (Integer) value
					: null);
			}
		});
		cols.add(new ProxyColumn<IncAdvice>("incident.impacted.lanes",
			112, Integer.class)
		{
			public Object getValueAt(IncAdvice adv) {
				return adv.getImpactedLanes();
			}
			public boolean isEditable(IncAdvice adv) {
				return canWrite(adv);
			}
			public void setValueAt(IncAdvice adv, Object value) {
				adv.setImpactedLanes((value instanceof Integer)
					? (Integer) value
					: null);
			}
		});
		cols.add(new ProxyColumn<IncAdvice>("dms.multi.string", 300) {
			public Object getValueAt(IncAdvice adv) {
				return adv.getMulti();
			}
			public boolean isEditable(IncAdvice adv) {
				return canWrite(adv);
			}
			public void setValueAt(IncAdvice adv, Object value) {
				adv.setMulti(new MultiString(value.toString())
					.normalizeLine().toString());
			}
		});
		return cols;
	}

	/** Create a new table model.
	 * @param s Session */
	public IncAdviceTableModel(Session s) {
		super(s, descriptor(s), 12, 20);
	}

	/** Get a proxy comparator */
	@Override
	protected Comparator<IncAdvice> comparator() {
		return new Comparator<IncAdvice>() {
			public int compare(IncAdvice adv0, IncAdvice adv1) {
				int imp0 = adv0.getImpact();
				int imp1 = adv1.getImpact();
				if (imp0 != imp1)
					return imp0 - imp1;
				int lt0 = adv0.getLaneType();
				int lt1 = adv0.getLaneType();
				if (lt0 != lt1)
					return lt0 - lt1;
				int r0 = adv0.getRange();
				int r1 = adv1.getRange();
				if (r0 != r1)
					return r0 - r1;
				Integer ol0 = adv0.getOpenLanes();
				Integer ol1 = adv1.getOpenLanes();
				if (ol0 != ol1) {
					if (ol1 == null)
						return -1;
					else if (ol0 == null)
						return 1;
					else
						return ol0.compareTo(ol1);
				}
				Integer il0 = adv0.getImpactedLanes();
				Integer il1 = adv1.getImpactedLanes();
				if (il0 != il1) {
					if (il1 == null)
						return -1;
					else if (il0 == null)
						return 1;
					else
						return il0.compareTo(il1);
				}
				return adv0.getName().compareTo(adv1.getName());
			}
		};
	}

	/** Create a new incident advice */
	@Override
	public void createObject(String name_ignore) {
		String name = createUniqueName();
		if (name != null)
			descriptor.cache.createObject(name);
	}

	/** Create a unique incident advice name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 99999; uid++) {
			String n = String.format("iadv_%05d", uid);
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}
}
