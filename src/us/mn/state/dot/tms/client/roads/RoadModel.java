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
package us.mn.state.dot.tms.client.roads;

import java.util.LinkedList;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for roads
 *
 * @author Douglas Lau
 */
public class RoadModel extends ProxyTableModel<Road> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 5;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Abbreviation column number */
	static protected final int COL_ABBREV = 1;

	/** Restricted column number */
	static protected final int COL_R_CLASS = 2;

	/** Direction column number */
	static protected final int COL_DIRECTION = 3;

	/** Alternate directino column number */
	static protected final int COL_ALT_DIR = 4;

	/** List of all possible road class values */
	static LinkedList<String> R_CLASS = new LinkedList<String>();
	static {
		for(String r: Road.R_CLASS)
			R_CLASS.add(r);
	}

	/** List of all possible direction values */
	static LinkedList<String> DIRECTION = new LinkedList<String>();
	static {
		for(String d: Direction.DIR_LONG)
			DIRECTION.add(d);
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR user */
	protected final User user;

	/** Create a new road table model */
	public RoadModel(TypeCache<Road> c, Namespace ns, User u) {
		super(c);
		namespace = ns;
		user = u;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Road r = getProxy(row);
		if(r == null)
			return null;
		switch(column) {
			case COL_NAME:
				return r.getName();
			case COL_ABBREV:
				return r.getAbbrev();
			case COL_R_CLASS:
				return R_CLASS.get(r.getRClass());
			case COL_DIRECTION:
				return DIRECTION.get(r.getDirection());
			case COL_ALT_DIR:
				return DIRECTION.get(r.getAltDir());
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(isLastRow(row))
			return column == COL_NAME;
		if(column == COL_NAME)
			return false;
		return true;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Road r = getProxy(row);
		switch(column) {
			case COL_NAME:
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
				break;
			case COL_ABBREV:
				r.setAbbrev(value.toString());
				break;
			case COL_R_CLASS:
				r.setRClass((short)R_CLASS.indexOf(value));
				break;
			case COL_DIRECTION:
				r.setDirection((short)DIRECTION.indexOf(value));
				break;
			case COL_ALT_DIR:
				r.setAltDir((short)DIRECTION.indexOf(value));
				break;
		}
	}

	/** Create the road class column */
	protected TableColumn createRClassColumn() {
		TableColumn c = new TableColumn(COL_R_CLASS, 120);
		c.setHeaderValue("Road Class");
		JComboBox combo = new JComboBox(Road.R_CLASS);
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the direction column */
	protected TableColumn createDirectionColumn() {
		TableColumn c = new TableColumn(COL_DIRECTION, 120);
		c.setHeaderValue("Direction");
		JComboBox combo = new JComboBox(DIRECTION.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the alternate direction column */
	protected TableColumn createAltDirColumn() {
		TableColumn c = new TableColumn(COL_ALT_DIR, 120);
		c.setHeaderValue("Alt Dir");
		JComboBox combo = new JComboBox(DIRECTION.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 200, "Road"));
		m.addColumn(createColumn(COL_ABBREV, 80, "Abbrev"));
		m.addColumn(createRClassColumn());
		m.addColumn(createDirectionColumn());
		m.addColumn(createAltDirColumn());
		return m;
	}

	/** Check if the user can remove a road */
	public boolean canRemove(Road r) {
		if(r == null)
			return false;
		return namespace.canRemove(user, new Name(Road.SONAR_TYPE,
			r.getName()));
	}
}
