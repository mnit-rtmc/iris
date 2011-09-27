/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for comm links
 *
 * @author Douglas Lau
 */
public class CommLinkModel extends ProxyTableModel<CommLink> {

	/** List of all possible protocol selections */
	static protected final LinkedList<String> PROTOCOLS =
		new LinkedList<String>();
	static {
		for(String cp: CommProtocol.getDescriptions())
			PROTOCOLS.add(cp);
	}

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<CommLink>("Comm Link", 90) {
			public Object getValueAt(CommLink cl) {
				return cl.getName();
			}
			public boolean isEditable(CommLink cl) {
				return (cl == null) && canAdd();
			}
			public void setValueAt(CommLink cl, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<CommLink>("Description", 220) {
			public Object getValueAt(CommLink cl) {
				return cl.getDescription();
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "description");
			}
			public void setValueAt(CommLink cl, Object value) {
				cl.setDescription(value.toString().trim());
			}
		},
		new ProxyColumn<CommLink>("URI", 280) {
			public Object getValueAt(CommLink cl) {
				return cl.getUri();
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "uri");
			}
			public void setValueAt(CommLink cl, Object value) {
				cl.setUri(value.toString().trim());
			}
		},
		new ProxyColumn<CommLink>("Status", 44) {
			public Object getValueAt(CommLink cl) {
				return cl.getStatus();
			}
			protected TableCellRenderer createCellRenderer() {
				return new StatusCellRenderer();
			}
		},
		new ProxyColumn<CommLink>("Protocol", 140) {
			public Object getValueAt(CommLink cl) {
				return PROTOCOLS.get(cl.getProtocol());
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "protocol");
			}
			public void setValueAt(CommLink cl, Object value) {
				cl.setProtocol(Short.valueOf(
					(short)PROTOCOLS.indexOf(value)));
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					PROTOCOLS.toArray());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<CommLink>("Timeout", 60) {
			public Object getValueAt(CommLink cl) {
				return cl.getTimeout();
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "timeout");
			}
			public void setValueAt(CommLink cl, Object value) {
				if(value instanceof Integer)
					cl.setTimeout((Integer)value);
			}
			protected TableCellEditor createCellEditor() {
				return new TimeoutCellEditor();
			}
		}
	    };
	}

	/** Create a new comm link table model */
	public CommLinkModel(Session s) {
		super(s, s.getSonarState().getConCache().getCommLinks());
	}

	/** Renderer for link status in a table cell */
	public class StatusCellRenderer extends DefaultTableCellRenderer {
		protected final Icon ok = new CommLinkIcon(Color.BLUE);
		protected final Icon fail = new CommLinkIcon(Color.GRAY);
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			JLabel label =
				(JLabel)super.getTableCellRendererComponent(
				table, "", isSelected, hasFocus, row,
				column);
			if(value == null)
				label.setIcon(null);
			else if("".equals(value))
				label.setIcon(ok);
			else
				label.setIcon(fail);
			return label;
		}
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return CommLink.SONAR_TYPE;
	}
}
