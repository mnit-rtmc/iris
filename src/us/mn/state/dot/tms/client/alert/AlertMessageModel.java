/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.alert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.AlertMessage;
import us.mn.state.dot.tms.AlertMessageHelper;
import us.mn.state.dot.tms.AlertPeriod;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import static us.mn.state.dot.tms.client.widget.IOptionPane.showHint;

/**
 * Table model for alert config messages.
 *
 * @author Douglas Lau
 */
public class AlertMessageModel extends ProxyTableModel<AlertMessage> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<AlertMessage> descriptor(Session s) {
		return new ProxyDescriptor<AlertMessage>(
			s.getSonarState().getAlertMessages(),
			false,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Lookup a quick message */
	static private QuickMessage lookupQuickMessage(Object value) {
		String v = value.toString().trim();
		if (v.length() > 0) {
			QuickMessage qm = QuickMessageHelper.lookup(v);
			if (null == qm)
				showHint("quick.message.unknown.hint");
			return qm;
		} else
			return null;
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<AlertMessage>> createColumns() {
		ArrayList<ProxyColumn<AlertMessage>> cols =
			new ArrayList<ProxyColumn<AlertMessage>>(2);
		cols.add(new ProxyColumn<AlertMessage>("alert.period", 100) {
			public Object getValueAt(AlertMessage am) {
				return AlertPeriod.fromOrdinal(
					am.getAlertPeriod());
			}
			public boolean isEditable(AlertMessage am) {
				return canWrite(am);
			}
			public void setValueAt(AlertMessage am, Object value) {
				if (value instanceof AlertPeriod) {
					AlertPeriod ap = (AlertPeriod) value;
					am.setAlertPeriod(ap.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				return new DefaultCellEditor(new JComboBox
					<AlertPeriod>(AlertPeriod.VALUES));
			}
		});
		cols.add(new ProxyColumn<AlertMessage>("alert.quick.message",
			120)
		{
			public Object getValueAt(AlertMessage am) {
				return am.getQuickMessage();
			}
			public boolean isEditable(AlertMessage am) {
				return canWrite(am);
			}
			public void setValueAt(AlertMessage am, Object value) {
				am.setQuickMessage(lookupQuickMessage(value));
			}
		});
		return cols;
	}

	/** Get a table row sorter */
	@Override
	public RowSorter<ProxyTableModel<AlertMessage>> createSorter() {
		TableRowSorter<ProxyTableModel<AlertMessage>> sorter =
			new TableRowSorter<ProxyTableModel<AlertMessage>>(this);
		sorter.setSortsOnUpdates(true);
		sorter.setComparator(0, new Comparator<AlertPeriod>() {
			public int compare(AlertPeriod a, AlertPeriod b) {
				return Integer.compare(a.ordinal(), b.ordinal());
			}
		});
		ArrayList<RowSorter.SortKey> keys =
			new ArrayList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(keys);
		return sorter;
	}

	/** Alert configuration */
	private final AlertConfig cfg;

	/** Create a new alert message model */
	public AlertMessageModel(Session s, AlertConfig ac) {
		super(s, descriptor(s), 5);
		cfg = ac;
	}

	/** Check if a proxy is included in the table */
	@Override
	protected boolean check(AlertMessage proxy) {
		return proxy.getAlertConfig() == cfg;
	}

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return (cfg != null) && super.canAdd();
	}

	/** Create an object with the given name */
	@Override
	public void createObject(String n) {
		String name = AlertMessageHelper.createUniqueName();
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("alert_config", cfg);
			attrs.put("alert_period", AlertPeriod.DURING.ordinal());
			descriptor.cache.createObject(name, attrs);
		}
	}
}
