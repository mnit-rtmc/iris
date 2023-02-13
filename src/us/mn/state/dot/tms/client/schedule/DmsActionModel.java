/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.PlanPhaseHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import static us.mn.state.dot.tms.client.widget.IOptionPane.showHint;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * Table model for DMS actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class DmsActionModel extends ProxyTableModel<DmsAction> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<DmsAction> descriptor(Session s) {
		return new ProxyDescriptor<DmsAction>(
			s.getSonarState().getDmsActions(), false
		);
	}

	/** Allowed message priorities */
	static private final DmsMsgPriority[] PRIORITIES = {
		DmsMsgPriority.PSA,
		DmsMsgPriority.TRAVEL_TIME,
		DmsMsgPriority.ALERT_LOW,
		DmsMsgPriority.SCHED_A,
		DmsMsgPriority.SCHED_B,
		DmsMsgPriority.SCHED_C,
		DmsMsgPriority.SCHED_D,
		DmsMsgPriority.ALERT_MED,
		DmsMsgPriority.GATE_ARM,
		DmsMsgPriority.SCHED_HIGH
	};

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<DmsAction>> createColumns() {
		ArrayList<ProxyColumn<DmsAction>> cols =
			new ArrayList<ProxyColumn<DmsAction>>(4);
		cols.add(new ProxyColumn<DmsAction>("action.plan.hashtag",
			120)
		{
			public Object getValueAt(DmsAction da) {
				return da.getDmsHashtag();
			}
			public boolean isEditable(DmsAction da) {
				return canWrite(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				String ht = DMSHelper.normalizeHashtag(
					value.toString());
				if (ht != null)
					da.setDmsHashtag(ht);
				else
					showHint("dms.hashtag.invalid.hint");
			}
		});
		cols.add(new ProxyColumn<DmsAction>("action.plan.phase", 100) {
			public Object getValueAt(DmsAction da) {
				return da.getPhase();
			}
			public boolean isEditable(DmsAction da) {
				return canWrite(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				if (value instanceof PlanPhase)
					da.setPhase((PlanPhase) value);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<PlanPhase> cbx = new JComboBox
					<PlanPhase>();
				cbx.setModel(new IComboBoxModel<PlanPhase>(
					phase_mdl));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<DmsAction>("msg.pattern", 160) {
			public Object getValueAt(DmsAction da) {
				return da.getMsgPattern();
			}
			public boolean isEditable(DmsAction da) {
				return canWrite(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				String v = value.toString().trim();
				da.setMsgPattern(
					MsgPatternHelper.lookup(v));
			}
		});
		cols.add(new ProxyColumn<DmsAction>("dms.msg.priority",
			120)
		{
			public Object getValueAt(DmsAction da) {
				return DmsMsgPriority.fromOrdinal(
				       da.getMsgPriority());
			}
			public boolean isEditable(DmsAction da) {
				return canWrite(da);
			}
			public void setValueAt(DmsAction da, Object value) {
				if (value instanceof DmsMsgPriority) {
					DmsMsgPriority p =
						(DmsMsgPriority) value;
					da.setMsgPriority(p.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<DmsMsgPriority> cbx = new JComboBox
					<DmsMsgPriority>(PRIORITIES);
				return new DefaultCellEditor(cbx);
			}
		});
		return cols;
	}

	/** Currently selected action plan */
	private final ActionPlan action_plan;

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_mdl;

	/** Create a new DMS action table model */
	public DmsActionModel(Session s, ActionPlan ap) {
		super(s, descriptor(s), 12);
		action_plan = ap;
		phase_mdl = s.getSonarState().getPhaseModel();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(DmsAction proxy) {
		return proxy.getActionPlan() == action_plan;
	}

	/** Get a table row sorter */
	@Override
	public RowSorter<ProxyTableModel<DmsAction>> createSorter() {
		TableRowSorter<ProxyTableModel<DmsAction>> sorter =
			new TableRowSorter<ProxyTableModel<DmsAction>>(this)
		{
			@Override public boolean isSortable(int c) {
				return c == 0;
			}
		};
		sorter.setSortsOnUpdates(true);
		LinkedList<RowSorter.SortKey> keys =
			new LinkedList<RowSorter.SortKey>();
		keys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(keys);
		return sorter;
	}

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return action_plan != null && super.canAdd();
	}

	/** Create an object with a hashtag */
	@Override
	public void createObject(String hashtag) {
		String ht = DMSHelper.normalizeHashtag(hashtag);
		if (ht != null && ht.equals(hashtag))
			create(hashtag);
		else
			showHint("dms.hashtag.invalid.hint");
	}

	/** Create a new DMS action */
	private void create(String hashtag) {
		String name = createUniqueName();
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("dms_hashtag", hashtag);
			attrs.put("phase", lookupPlanPhase());
			attrs.put("msg_priority",
				DmsMsgPriority.SCHED_A.ordinal());
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Create a unique DMS action name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}

	/** Lookup the appropriate plan phase for a DMS action */
	private PlanPhase lookupPlanPhase() {
		PlanPhase phase = PlanPhaseHelper.lookup("deployed");
		return (phase != null) ? phase : action_plan.getDefaultPhase();
	}
}
