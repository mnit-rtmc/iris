/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Table model for DMS table form 2.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @see DMSForm, DMSForm2, DMSTable
 */
public class DMSModel2 extends ProxyTableModel<DMS> {

	/** DMS abbreviation */
	protected final String dms_abr = I18N.get("dms.abbreviation");

	/** AWS abbreviation */
	protected final String aws_abbr = I18N.get("dms.aws.abbreviation");

	/** Create columns */
	protected final ProxyColumn[] columns = new ProxyColumn[] {
		new ProxyColumn<DMS>(dms_abr, String.class, 40) {
			public Object getValueAt(DMS d) {
				return d.getName();
			}
			public boolean isEditable(DMS d) {
				return (d == null) && canAdd();
			}
			public void setValueAt(DMS d, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<DMS>("Location", String.class, 200) {
			public Object getValueAt(DMS d) {
				return GeoLocHelper.getDescription(
					d.getGeoLoc());
			}
		},
		new ProxyColumn<DMS>("Dir.", String.class, 30) {
			public Object getValueAt(DMS d) {
				return DMSHelper.getFreeDir(d);
			}
		},
		new ProxyColumn<DMS>(aws_abbr +" Allowed", Boolean.class, 80) {
			public Object getValueAt(DMS d) {
				return d.getAwsAllowed();
			}
		},
		new ProxyColumn<DMS>(aws_abbr + " Controlled", Boolean.class,
			80)
		{
			public Object getValueAt(DMS d) {
				return d.getAwsControlled();
			}
		},
		new ProxyColumn<DMS>("Author", String.class, 60) {
			public Object getValueAt(DMS d) {
				User u = d.getOwnerCurrent();
				String name = (u == null ? "" : u.getName());
				return (name == null ? "" : name);
			}
		},
		new ProxyColumn<DMS>("Status", String.class, 100) {
			public Object getValueAt(DMS d) {
				return DMSHelper.getAllStyles(d);
			}
		},
		new ProxyColumn<DMS>("Model", String.class, 40) {
			public Object getValueAt(DMS d) {
				return d.getModel();
			}
		},
		new ProxyColumn<DMS>("Com Type", String.class, 140) {
			public Object getValueAt(DMS d) {
				return d.getSignAccess();
			}
		}
	};

	/** Create a new DMS table model */
	public DMSModel2(Session s) {
		super(s, s.getSonarState().getDmsCache().getDMSs());
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return columns.length;
	}

	/** Get the proxy column at the given column index */
	public ProxyColumn getProxyColumn(int col) {
		if(col >= 0 && col < columns.length)
			return columns[col];
		else
			return null;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int col) {
		DMS s = getProxy(row);
		if(s == null)
			return null;
		return columns[col].getValueAt(s);
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int col) {
		return columns[col].getColumnClass();
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		ProxyColumn pc = getProxyColumn(col);
		return pc != null && pc.isEditable(getProxy(row));
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int col) {
		ProxyColumn pc = getProxyColumn(col);
		if(pc != null)
			pc.setValueAt(getProxy(row), value);
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		for(int i = 0; i < columns.length; ++i)
			columns[i].addColumn(m, i);
		return m;
	}

	/** Determine if a properties form is available */
	public boolean hasProperties() {
		return true;
	}

	/** Create a properties form for one proxy */
	protected SonarObjectForm<DMS> createPropertiesForm(DMS proxy) {
		return new DMSProperties(session, proxy);
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(DMS.SONAR_TYPE));
	}
}
