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

	/** AWS abbreviation */
	protected final String m_awsAbbr = I18N.get("dms.aws.abbreviation");

	/** A single column */
	abstract private class Column {
		String label;
		Class cclass;
		int width;

		/** Constructor */
		Column(String l, Class c, int w) {
			label = l;
			cclass = c;
			width = w;
		}

		/** add a column to the model */
		void addColumn(TableColumnModel m, int index) {
			m.addColumn(createColumn(index, width, label));
		}

		/** Get the value of the column */
		abstract Object getValueAt(DMS d);
	}

	/** Name column number, assumed to be 0th */
	static protected final int COL_NAME = 0;

	/** Create columns */
	Column[] m_columns = new Column[] {
		new Column(I18N.get("dms.abbreviation"), String.class, 40) {
			Object getValueAt(DMS d) {
				return d.getName();
			}
		},
		new Column("Location", String.class, 200) {
			Object getValueAt(DMS d) {
				return GeoLocHelper.getDescription(
					d.getGeoLoc());
			}
		},
		new Column("Dir.", String.class, 30) {
			Object getValueAt(DMS d) {
				return DMSHelper.getFreeDir(d);
			}
		},
		new Column(m_awsAbbr +" Allowed", Boolean.class, 80) {
			Object getValueAt(DMS d) {
				return d.getAwsAllowed();
			}
		},
		new Column(m_awsAbbr + " Controlled", Boolean.class, 80) {
			Object getValueAt(DMS d) {
				return d.getAwsControlled();
			}
		},
		new Column("Author", String.class, 60) {
			Object getValueAt(DMS d) {
				User u = d.getOwnerCurrent();
				String name = (u == null ? "" : u.getName());
				return (name == null ? "" : name);
			}
		},
		new Column("Status", String.class, 100) {
			Object getValueAt(DMS d) {
				return DMSHelper.getAllStyles(d);
			}
		},
		new Column("Model", String.class, 40) {
			Object getValueAt(DMS d) {
				return d.getModel();
			}
		},
		new Column("Com Type", String.class, 140) {
			Object getValueAt(DMS d) {
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
		return m_columns.length;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		DMS s = getProxy(row);
		if(s == null)
			return null;
		return m_columns[column].getValueAt(s);
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		return m_columns[column].cclass;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int col) {
		DMS dms = getProxy(row);
		return (dms == null) && (col == COL_NAME) && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		if(column == COL_NAME) {
			String v = value.toString().trim();
			if(v.length() > 0)
				cache.createObject(v);
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		for(int i = 0; i < m_columns.length; ++i)
			m_columns[i].addColumn(m, i);
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
