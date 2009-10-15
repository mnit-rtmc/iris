/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanState;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for DMS actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class DmsActionModel extends ProxyTableModel<DmsAction> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 5;

	/** Sign group column number */
	static protected final int COL_GROUP = 0;

	/** Plan state column number */
	static protected final int COL_STATE = 1;

	/** Quick message column number */
	static protected final int COL_Q_MSG = 2;

	/** Activation priority column number */
	static protected final int COL_A_PRIORITY = 3;

	/** Run-time priority column number */
	static protected final int COL_R_PRIORITY = 4;

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_GROUP, 120, "Sign Group"));
		m.addColumn(createStateColumn());
		m.addColumn(createColumn(COL_Q_MSG, 160, "Quick Message"));
		m.addColumn(createPriorityColumn(COL_A_PRIORITY,
			"Activation Priority"));
		m.addColumn(createPriorityColumn(COL_R_PRIORITY,
			"Run-Time Priority"));
		return m;
	}

	/** Create the state column */
	static protected TableColumn createStateColumn() {
		TableColumn c = createColumn(COL_STATE, 80, "State");
		JComboBox combo = new JComboBox(STATES);
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Allowed states */
	static protected final ActionPlanState[] STATES = {
		ActionPlanState.undeployed,
		ActionPlanState.deploying,
		ActionPlanState.deployed,
		ActionPlanState.undeploying
	};

	/** Create a priority column */
	static protected TableColumn createPriorityColumn(int n,
		String header)
	{
		TableColumn c = createColumn(n, 120, header);
		JComboBox combo = new JComboBox(PRIORITIES);
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Allowed priorities */
	static protected final DMSMessagePriority[] PRIORITIES = {
		DMSMessagePriority.PSA,
		DMSMessagePriority.TRAVEL_TIME,
		DMSMessagePriority.SCHEDULED,
		DMSMessagePriority.INCIDENT_LOW,
		DMSMessagePriority.INCIDENT_MED,
		DMSMessagePriority.INCIDENT_HIGH
	};

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Logged-in user */
	protected final User user;

	/** Currently selected action plan */
	protected final ActionPlan action_plan;

	/** Create a new DMS action table model */
	public DmsActionModel(TypeCache<DmsAction> c, ActionPlan ap,
		Namespace ns, User u)
	{
		super(c);
		action_plan = ap;
		namespace = ns;
		user = u;
		initialize();
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(DmsAction da) {
		if(da.getActionPlan() == action_plan)
			return super.doProxyAdded(da);
		else
			return -1;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		synchronized(proxies) {
			return proxies.size() + 1;
		}
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		DmsAction da = getProxy(row);
		if(da == null)
			return null;
		switch(column) {
		case COL_GROUP:
			return da.getSignGroup();
		case COL_STATE:
			return ActionPlanState.fromOrdinal(da.getState());
		case COL_Q_MSG:
			return da.getQuickMessage();
		case COL_A_PRIORITY:
			return DMSMessagePriority.fromOrdinal(
			       da.getActivationPriority());
		case COL_R_PRIORITY:
			return DMSMessagePriority.fromOrdinal(
			       da.getRunTimePriority());
		default:
			return null;
		}
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		return String.class;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		DmsAction da = getProxy(row);
		if(da != null)
			return column != COL_GROUP && canUpdate();
		else
			return column == COL_GROUP && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		DmsAction da = getProxy(row);
		if(da == null) {
			if(column == COL_GROUP) {
				String v = value.toString().trim();
				SignGroup sg = SignGroupHelper.lookup(v);
				if(sg != null)
					create(sg);
			}
			return;
		}
		switch(column) {
		case COL_STATE:
			if(value instanceof ActionPlanState) {
				ActionPlanState st = (ActionPlanState)value;
				da.setState(st.ordinal());
			}
			break;
		case COL_Q_MSG:
			String v = value.toString().trim();
			da.setQuickMessage(QuickMessageHelper.lookup(v));
			break;
		case COL_A_PRIORITY:
			if(value instanceof DMSMessagePriority) {
				DMSMessagePriority p =(DMSMessagePriority)value;
				da.setActivationPriority(p.ordinal());
			}
			break;
		case COL_R_PRIORITY:
			if(value instanceof DMSMessagePriority) {
				DMSMessagePriority p =(DMSMessagePriority)value;
				da.setRunTimePriority(p.ordinal());
			}
			break;
		}
	}

	/** Create a new DMS action */
	protected void create(SignGroup sg) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("sign_group", sg);
			attrs.put("a_priority",
				DMSMessagePriority.SCHEDULED.ordinal());
			attrs.put("r_priority",
				DMSMessagePriority.SCHEDULED.ordinal());
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique DMS action name */
	protected String createUniqueName() {
		for(int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if(cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}

	/** Check if the user can add */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(DmsAction.SONAR_TYPE,
			"oname"));
	}

	/** Check if the user can update */
	public boolean canUpdate() {
		return namespace.canUpdate(user, new Name(DmsAction.SONAR_TYPE,
			"oname", "aname"));
	}

	/** Check if the user can remove the action at the specified row */
	public boolean canRemove(int row) {
		DmsAction da = getProxy(row);
		if(da != null)
			return canRemove(da);
		else
			return false;
	}

	/** Check if the user can remove a DMS action */
	public boolean canRemove(DmsAction da) {
		return namespace.canRemove(user, new Name(DmsAction.SONAR_TYPE,
			da.getName()));
	}
}
