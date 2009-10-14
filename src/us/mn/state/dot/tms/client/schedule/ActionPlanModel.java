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
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanModel extends ProxyTableModel<ActionPlan> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 7;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Description column number */
	static protected final int COL_DESCRIPTION = 1;

	/** Sync actions column number */
	static protected final int COL_SYNC_ACTIONS = 2;

	/** Deploying secs column number */
	static protected final int COL_DEP_SECS = 3;

	/** Undeploying secs column number */
	static protected final int COL_UNDEP_SECS = 4;

	/** Active column number */
	static protected final int COL_ACTIVE = 5;

	/** State column number */
	static protected final int COL_STATE = 6;

	/** Create the table column model */
	static public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, 100, "Plan Name"));
		m.addColumn(createColumn(COL_DESCRIPTION, 380, "Description"));
		m.addColumn(createColumn(COL_SYNC_ACTIONS, 80, "Sync Actions"));
		m.addColumn(createColumn(COL_DEP_SECS, 80, "Dep.Secs"));
		m.addColumn(createColumn(COL_UNDEP_SECS, 80, "Undep.Secs"));
		m.addColumn(createColumn(COL_ACTIVE, 80, "Active"));
		m.addColumn(createStateColumn());
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

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR user */
	protected final User user;

	/** Create a new action plan table model */
	public ActionPlanModel(TypeCache<ActionPlan> c, Namespace ns, User u) {
		super(c);
		namespace = ns;
		user = u;
		initialize();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the class of the specified column */
	public Class getColumnClass(int column) {
		switch(column) {
		case COL_SYNC_ACTIONS:
		case COL_ACTIVE:
			return Boolean.class;
		case COL_DEP_SECS:
		case COL_UNDEP_SECS:
			return Integer.class;
		default:
			return String.class;
		}
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		ActionPlan plan = getProxy(row);
		if(plan == null)
			return null;
		switch(column) {
		case COL_NAME:
			return plan.getName();
		case COL_DESCRIPTION:
			return plan.getDescription();
		case COL_SYNC_ACTIONS:
			return plan.getSyncActions();
		case COL_DEP_SECS:
			return plan.getDeployingSecs();
		case COL_UNDEP_SECS:
			return plan.getUndeployingSecs();
		case COL_ACTIVE:
			return plan.getActive();
		case COL_STATE:
			return ActionPlanState.fromOrdinal(plan.getState());
		}
		return null;
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		ActionPlan plan = getProxy(row);
		if(plan != null)
			return column != COL_NAME && canUpdate(plan);
		else
			return column == COL_NAME && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		ActionPlan plan = getProxy(row);
		String v = value.toString().trim();
		switch(column) {
		case COL_NAME:
			if(v.length() > 0)
				cache.createObject(v);
			break;
		case COL_DESCRIPTION:
			plan.setDescription(v);
			break;
		case COL_SYNC_ACTIONS:
			if(value instanceof Boolean)
				plan.setSyncActions((Boolean)value);
			break;
		case COL_DEP_SECS:
			if(value instanceof Integer)
				plan.setDeployingSecs((Integer)value);
			break;
		case COL_UNDEP_SECS:
			if(value instanceof Integer)
				plan.setUndeployingSecs((Integer)value);
			break;
		case COL_ACTIVE:
			if(value instanceof Boolean)
				plan.setActive((Boolean)value);
			break;
		case COL_STATE:
			if(value instanceof ActionPlanState) {
				ActionPlanState st = (ActionPlanState)value;
				plan.setDeployed(
					ActionPlanState.isDeployed(st));
			}
			break;
		}
	}

	/** Check if the user can add a plan */
	public boolean canAdd() {
		return namespace.canAdd(user, new Name(ActionPlan.SONAR_TYPE,
			"oname"));
	}

	/** Check if the user can update */
	public boolean canUpdate(ActionPlan plan) {
		return namespace.canUpdate(user, new Name(ActionPlan.SONAR_TYPE,
			plan.getName()));
	}

	/** Check if the user can remove the plan at the specified row */
	public boolean canRemove(int row) {
		ActionPlan plan = getProxy(row);
		if(plan != null)
			return canRemove(plan);
		else
			return false;
	}

	/** Check if the user can remove a plan */
	public boolean canRemove(ActionPlan plan) {
		return namespace.canRemove(user, new Name(ActionPlan.SONAR_TYPE,
			plan.getName()));
	}
}
