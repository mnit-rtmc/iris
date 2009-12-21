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
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.LaneMarkingHelper;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for lane actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class LaneActionModel extends ProxyTableModel<LaneAction> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 2;

	/** Lane marking column number */
	static protected final int COL_MARKING = 0;

	/** On-deploy column number */
	static protected final int COL_STATE = 1;

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_MARKING, 160, "Lane Marking"));
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

	/** Logged-in user */
	protected final User user;

	/** Currently selected action plan */
	protected final ActionPlan action_plan;

	/** Create a new lane action table model */
	public LaneActionModel(TypeCache<LaneAction> c, ActionPlan ap,
		Namespace ns, User u)
	{
		super(c);
		action_plan = ap;
		namespace = ns;
		user = u;
		initialize();
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(LaneAction la) {
		if(la.getActionPlan() == action_plan)
			return super.doProxyAdded(la);
		else
			return -1;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		LaneAction la = getProxy(row);
		if(la == null)
			return null;
		switch(column) {
		case COL_MARKING:
			return la.getLaneMarking();
		case COL_STATE:
			return ActionPlanState.fromOrdinal(la.getState());
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
		LaneAction la = getProxy(row);
		if(la != null)
			return column != COL_MARKING && canUpdate();
		else
			return column == COL_MARKING && canAdd();
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		LaneAction la = getProxy(row);
		if(la == null) {
			if(column == COL_MARKING) {
				String v = value.toString().trim();
				LaneMarking lm = LaneMarkingHelper.lookup(v);
				if(lm != null)
					create(lm);
			}
			return;
		}
		if(column == COL_STATE) {
			if(value instanceof ActionPlanState) {
				ActionPlanState st = (ActionPlanState)value;
				la.setState(st.ordinal());
			}
		}
	}

	/** Create a new lane action */
	protected void create(LaneMarking lm) {
		String name = createUniqueName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("lane_marking", lm);
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique lane action name */
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
		return namespace.canAdd(user, new Name(LaneAction.SONAR_TYPE,
			"oname"));
	}

	/** Check if the user can update */
	public boolean canUpdate() {
		return namespace.canUpdate(user, new Name(LaneAction.SONAR_TYPE,
			"oname", "aname"));
	}

	/** Check if the user can remove the action at the specified row */
	public boolean canRemove(int row) {
		LaneAction la = getProxy(row);
		if(la != null)
			return canRemove(la);
		else
			return false;
	}

	/** Check if the user can remove a lane action */
	public boolean canRemove(LaneAction la) {
		return namespace.canRemove(user, new Name(LaneAction.SONAR_TYPE,
			la.getName()));
	}
}
