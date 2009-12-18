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
package us.mn.state.dot.tms.client.detector;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for detectors
 *
 * @author Douglas Lau
 */
public class DetectorModel extends ProxyTableModel<Detector> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 9;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Label column number */
	static protected final int COL_LABEL = 1;

	/** Lane type column number */
	static protected final int COL_LANE_TYPE = 2;

	/** Lane number column number */
	static protected final int COL_LANE_NUMBER = 3;

	/** Abandoned column number */
	static protected final int COL_ABANDONED = 4;

	/** Force fail column number */
	static protected final int COL_FORCE_FAIL = 5;

	/** Field length column number */
	static protected final int COL_FIELD_LENGTH = 6;

	/** Fake expression column number */
	static protected final int COL_FAKE = 7;

	/** Notes column number */
	static protected final int COL_NOTES = 8;

	/** List of all lane types */
	static protected final LinkedList<String> LANE_TYPES =
		new LinkedList<String>();
	static {
		for(String lt: LaneType.getDescriptions())
			LANE_TYPES.add(lt);
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR user */
	protected final User user;

	/** Create a new detector table model */
	public DetectorModel(TypeCache<Detector> c, Namespace ns, User u) {
		super(c);
		namespace = ns;
		user = u;
	}

	/** Create an empty set of proxies */
	protected TreeSet<Detector> createProxySet() {
		return new TreeSet<Detector>(
			new Comparator<Detector>() {
				public int compare(Detector a, Detector b) {
					return DetectorHelper.compare(a, b);
				}
			}
		);
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		if(column == COL_ABANDONED || column == COL_FORCE_FAIL)
			return Boolean.class;
		else if(column == COL_FIELD_LENGTH)
			return Float.class;
		else if(column == COL_LANE_NUMBER)
			return Short.class;
		else
			return String.class;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Detector d = getProxy(row);
		if(d == null)
			return null;
		switch(column) {
		case COL_NAME:
			return d.getName();
		case COL_LABEL:
			return DetectorHelper.getLabel(d);
		case COL_LANE_TYPE:
			return LANE_TYPES.get(d.getLaneType());
		case COL_LANE_NUMBER:
			return d.getLaneNumber();
		case COL_ABANDONED:
			return d.getAbandoned();
		case COL_FORCE_FAIL:
			return d.getForceFail();
		case COL_FIELD_LENGTH:
			return d.getFieldLength();
		case COL_FAKE:
			return d.getFake();
		case COL_NOTES:
			return d.getNotes();
		default:
			return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		if(isLastRow(row))
			return column == COL_NAME;
		else
			return column != COL_NAME && column != COL_LABEL;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		String v = value.toString().trim();
		Detector d = getProxy(row);
		switch(column) {
		case COL_NAME:
			if(v.length() > 0)
				cache.createObject(v);
			break;
		case COL_LANE_TYPE:
			d.setLaneType((short)LANE_TYPES.indexOf(value));
			break;
		case COL_LANE_NUMBER:
			d.setLaneNumber(((Number)value).shortValue());
			break;
		case COL_ABANDONED:
			d.setAbandoned((Boolean)value);
			break;
		case COL_FORCE_FAIL:
			d.setForceFail((Boolean)value);
			break;
		case COL_FIELD_LENGTH:
			d.setFieldLength(((Number)value).floatValue());
			break;
		case COL_FAKE:
			if(v.length() > 0)
				d.setFake(v);
			else
				d.setFake(null);
			break;
		case COL_NOTES:
			d.setNotes(v);
			break;
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 60, "Detector"));
		m.addColumn(createColumn(COL_LABEL, 140, "Label"));
		m.addColumn(createLaneTypeColumn());
		m.addColumn(createColumn(COL_LANE_NUMBER, 60, "Lane #"));
		m.addColumn(createColumn(COL_ABANDONED, 60, "Abandoned"));
		m.addColumn(createColumn(COL_FORCE_FAIL, 60, "Force Fail"));
		m.addColumn(createColumn(COL_FIELD_LENGTH, 60, "Field Len"));
		m.addColumn(createColumn(COL_FAKE, 180, "Fake"));
		m.addColumn(createColumn(COL_NOTES, 180, "Notes"));
		return m;
	}

	/** Create the lane type column */
	protected TableColumn createLaneTypeColumn() {
		TableColumn c = new TableColumn(COL_LANE_TYPE, 80);
		c.setHeaderValue("Lane Type");
		JComboBox combo = new JComboBox(LaneType.getDescriptions());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Check if the user can remove a proxy */
	public boolean canRemove(Detector d) {
		return d != null && namespace.canRemove(user, new Name(d));
	}
}
