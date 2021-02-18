/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Comparator;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.AlertConfigHelper;
import us.mn.state.dot.tms.CapEvent;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for alert configurations.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertConfigModel extends ProxyTableModel<AlertConfig> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<AlertConfig> descriptor(Session s) {
		return new ProxyDescriptor<AlertConfig>(
			s.getSonarState().getAlertConfigs(),
			false,  /* has_properties */
			true,   /* has_create_delete */
			false   /* has_name */
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<AlertConfig>> createColumns() {
		ArrayList<ProxyColumn<AlertConfig>> cols =
			new ArrayList<ProxyColumn<AlertConfig>>(1);
		cols.add(new ProxyColumn<AlertConfig>("alert.event", 260) {
			public Object getValueAt(AlertConfig cfg) {
				return cfg.getEvent();
			}
			protected TableCellRenderer createCellRenderer() {
				return new EventRenderer();
			}
		});
		return cols;
	}

	/** Get a table row sorter */
	@Override
	public RowSorter<ProxyTableModel<AlertConfig>> createSorter() {
		TableRowSorter<ProxyTableModel<AlertConfig>> sorter =
			new TableRowSorter<ProxyTableModel<AlertConfig>>(this)
		{
			@Override public boolean isSortable(int c) {
				return true;
			}
		};
		sorter.setSortsOnUpdates(true);
		ArrayList<RowSorter.SortKey> keys =
			new ArrayList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(keys);
		return sorter;
	}

	/** Create a new alert config table model */
	public AlertConfigModel(Session s) {
		super(s, descriptor(s), 16);
	}

	/** Create an object */
	@Override
	public void createObject(String name) {
		// ignore the provided name
		String n = AlertConfigHelper.createUniqueName();
		descriptor.cache.createObject(n);
	}

	/** Renderer for CAP event in a table cell */
	static private class EventRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			if (value instanceof String) {
				CapEvent ev = CapEvent.fromCode((String) value);
				if (ev != null) {
					value = ev.description + " (" +
						ev.name() + ")";
				}
			}
			return super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		}
	}
}
