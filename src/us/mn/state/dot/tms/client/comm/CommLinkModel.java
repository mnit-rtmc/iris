/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.CommConfig;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;

/**
 * Table model for comm links.
 *
 * @author Douglas Lau
 */
public class CommLinkModel extends ProxyTableModel<CommLink> {

	/** Inner class for rendering cells in the comm config list */
	static private class CommConfigListRenderer
		extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected,
			boolean hasFocus)
		{
			return super.getListCellRendererComponent(list,
				getCommConfigLabel(value), index, isSelected,
				hasFocus);
		}
	}

	/** Inner class for rendering cells in the comm config column */
	static private class CommConfigTableRenderer
		extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			return super.getTableCellRendererComponent(table,
				getCommConfigLabel(value), isSelected,
				hasFocus, row, column);
		}
	}

	/** Get a comm config label (description) */
	static private Object getCommConfigLabel(Object value) {
		if (value instanceof CommConfig) {
			CommConfig cc = (CommConfig) value;
			return cc.getDescription();
		} else
			return value;
	}

	/** Create a proxy descriptor */
	static public ProxyDescriptor<CommLink> descriptor(Session s) {
		return new ProxyDescriptor<CommLink>(
			s.getSonarState().getConCache().getCommLinks(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<CommLink>> createColumns() {
		ArrayList<ProxyColumn<CommLink>> cols =
			new ArrayList<ProxyColumn<CommLink>>(6);
		cols.add(new ProxyColumn<CommLink>("comm.link", 80) {
			public Object getValueAt(CommLink cl) {
				return cl.getName();
			}
		});
		cols.add(new ProxyColumn<CommLink>("device.description", 220) {
			public Object getValueAt(CommLink cl) {
				return cl.getDescription();
			}
			public boolean isEditable(CommLink cl) {
				return canWrite(cl, "description");
			}
			public void setValueAt(CommLink cl, Object value) {
				cl.setDescription(value.toString().trim());
			}
		});
		cols.add(new ProxyColumn<CommLink>("comm.link.uri", 280) {
			public Object getValueAt(CommLink cl) {
				return cl.getUri();
			}
			public boolean isEditable(CommLink cl) {
				return canWrite(cl, "uri");
			}
			public void setValueAt(CommLink cl, Object value) {
				cl.setUri(value.toString().trim());
			}
		});
		cols.add(new ProxyColumn<CommLink>("comm.link.poll_enabled", 56,
			Boolean.class)
		{
			public Object getValueAt(CommLink cl) {
				return cl.getPollEnabled();
			}
			public boolean isEditable(CommLink cl) {
				return canWrite(cl, "pollEnabled");
			}
			public void setValueAt(CommLink cl, Object value) {
				if (value instanceof Boolean)
					cl.setPollEnabled((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<CommLink>("device.status", 44) {
			public Object getValueAt(CommLink cl) {
				return cl.getStatus();
			}
			protected TableCellRenderer createCellRenderer() {
				return new StatusCellRenderer();
			}
		});
		cols.add(new ProxyColumn<CommLink>("comm.config", 180) {
			public Object getValueAt(CommLink cl) {
				return cl.getCommConfig();
			}
			public boolean isEditable(CommLink cl) {
				return canWrite(cl, "commConfig");
			}
			public void setValueAt(CommLink cl, Object value) {
				if (value instanceof CommConfig) {
					CommConfig cc = (CommConfig) value;
					cl.setCommConfig(cc);
				}
			}
			@Override
			protected TableCellEditor createCellEditor() {
				JComboBox<CommConfig> cbx =
					new JComboBox<CommConfig>();
				cbx.setModel(new IComboBoxModel<CommConfig>(
					comm_config_mdl));
				cbx.setRenderer(new CommConfigListRenderer());
				return new DefaultCellEditor(cbx);
			}
			@Override
			protected TableCellRenderer createCellRenderer() {
				return new CommConfigTableRenderer();
			}
		});
		return cols;
	}

	/** Comm config proxy list model */
	private final CommConfigListModel comm_config_mdl;

	/** Create a new comm link table model */
	public CommLinkModel(Session s) {
		super(s, descriptor(s), 8, 24);
		comm_config_mdl = new CommConfigListModel(s);
	}

	/** Initialize the comm link table model */
	@Override
	public void initialize() {
		super.initialize();
		comm_config_mdl.initialize();
	}

	/** Dispose of the comm link table model */
	@Override
	public void dispose() {
		comm_config_mdl.dispose();
		super.dispose();
	}

	/** Create a new comm link */
	@Override
	public void createObject(String name) {
		String n = name.trim();
		if (n.length() > 0) {
			HashMap<String, Object> attrs = createAttrs();
			if (attrs != null)
				descriptor.cache.createObject(n, attrs);
		}
	}

	/** Create a mapping of attributes */
	private HashMap<String, Object> createAttrs() {
		if (comm_config_mdl.getSize() > 0) {
			CommConfig cc = comm_config_mdl.getProxy(0);
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("comm_config", cc);
			return attrs;
		} else
			return null;
	}
}
