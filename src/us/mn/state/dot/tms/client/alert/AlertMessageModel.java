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

import java.awt.Color;
import java.awt.Font;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.AlertMessage;
import us.mn.state.dot.tms.AlertMessageHelper;
import us.mn.state.dot.tms.AlertPeriod;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import static us.mn.state.dot.tms.client.widget.IOptionPane.showHint;
import us.mn.state.dot.tms.utils.I18N;

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

	/** Lookup a sign group */
	static private SignGroup lookupSignGroup(Object value) {
		String v = value.toString().trim();
		if (v.length() > 0) {
			SignGroup sg = SignGroupHelper.lookup(v);
			if (null == sg)
				showHint("dms.group.unknown.hint");
			return sg;
		} else
			return null;
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
			new ArrayList<ProxyColumn<AlertMessage>>(4);
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
		cols.add(new ProxyColumn<AlertMessage>("alert.group", 120) {
			public Object getValueAt(AlertMessage am) {
				return am.getSignGroup();
			}
			public boolean isEditable(AlertMessage am) {
				return canWrite(am);
			}
			public void setValueAt(AlertMessage am, Object value) {
				am.setSignGroup(lookupSignGroup(value));
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
		cols.add(new ProxyColumn<AlertMessage>("alert.msg.check", 40) {
			public Object getValueAt(AlertMessage am) {
				return checkMessage(am);
			}
			protected TableCellRenderer createCellRenderer() {
				return new CheckRenderer();
			}
		});
		return cols;
	}

	/** Check if an alert message is OK */
	private boolean checkMessage(AlertMessage am) {
		SignGroup sg = am.getSignGroup();
		QuickMessage qm = am.getQuickMessage();
		if (sg != null && qm != null) {
			SignConfig sc = qm.getSignConfig();
			if (sc != null) {
				for (DMS dms: SignGroupHelper.getAllSigns(sg)) {
					if (dms.getSignConfig() != sc)
						return false;
				}
				return true;
			}
		}
		return false;
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

	/** Renderer for check value in a table cell */
	protected class CheckRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label = (JLabel)
				super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);
			boolean v = (Boolean) value;
			if (v) {
				label.setForeground(null);
				label.setText(I18N.get("alert.msg.check.ok"));
			} else {
				Font f = label.getFont();
				label.setFont(f.deriveFont(
					f.getStyle() | Font.BOLD));
				label.setForeground(Color.RED);
				label.setText(I18N.get("alert.msg.check.err"));
			}
			return label;
		}
	}
}
