/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.PlanPhaseHelper;
import us.mn.state.dot.tms.SignMsgPriority;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import static us.mn.state.dot.tms.client.widget.IOptionPane.showHint;

/**
 * Table model for device actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class DeviceActionModel extends ProxyTableModel<DeviceAction> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<DeviceAction> descriptor(Session s) {
		return new ProxyDescriptor<DeviceAction>(
			s.getSonarState().getDeviceActions(), false
		);
	}

	/** Allowed message priorities */
	static private final SignMsgPriority[] PRIORITIES = {
		SignMsgPriority.low_1,
		SignMsgPriority.low_2,
		SignMsgPriority.low_3,
		SignMsgPriority.low_4,
		SignMsgPriority.medium_1,
		SignMsgPriority.medium_2,
		SignMsgPriority.medium_3,
		SignMsgPriority.medium_4,
		SignMsgPriority.high_1,
		SignMsgPriority.high_2,
		SignMsgPriority.high_3,
		SignMsgPriority.high_4
	};

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<DeviceAction>> createColumns() {
		ArrayList<ProxyColumn<DeviceAction>> cols =
			new ArrayList<ProxyColumn<DeviceAction>>(4);
		cols.add(new ProxyColumn<DeviceAction>("hashtag", 120) {
			public Object getValueAt(DeviceAction da) {
				return da.getHashtag();
			}
			public boolean isEditable(DeviceAction da) {
				return canWrite(da);
			}
			public void setValueAt(DeviceAction da, Object value) {
				String ht = Hashtags.normalize(value.toString());
				if (ht != null)
					da.setHashtag(ht);
				else
					showHint("hashtag.invalid.hint");
			}
		});
		cols.add(new ProxyColumn<DeviceAction>("action.plan.phase", 100) {
			public Object getValueAt(DeviceAction da) {
				return da.getPhase();
			}
			public boolean isEditable(DeviceAction da) {
				return canWrite(da);
			}
			public void setValueAt(DeviceAction da, Object value) {
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
		cols.add(new ProxyColumn<DeviceAction>("msg.pattern", 160) {
			public Object getValueAt(DeviceAction da) {
				return da.getMsgPattern();
			}
			public boolean isEditable(DeviceAction da) {
				return canWrite(da);
			}
			public void setValueAt(DeviceAction da, Object value) {
				String v = value.toString().trim();
				da.setMsgPattern(
					MsgPatternHelper.lookup(v));
			}
		});
		cols.add(new ProxyColumn<DeviceAction>("dms.msg.priority",
			120)
		{
			public Object getValueAt(DeviceAction da) {
				return SignMsgPriority.fromOrdinal(
				       da.getMsgPriority());
			}
			public boolean isEditable(DeviceAction da) {
				return canWrite(da);
			}
			public void setValueAt(DeviceAction da, Object value) {
				if (value instanceof SignMsgPriority) {
					SignMsgPriority mp =
						(SignMsgPriority) value;
					da.setMsgPriority(mp.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<SignMsgPriority> cbx = new JComboBox
					<SignMsgPriority>(PRIORITIES);
				return new DefaultCellEditor(cbx);
			}
		});
		return cols;
	}

	/** Currently selected action plan */
	private final ActionPlan action_plan;

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_mdl;

	/** Create a new device action table model */
	public DeviceActionModel(Session s, ActionPlan ap) {
		super(s, descriptor(s), 12);
		action_plan = ap;
		phase_mdl = s.getSonarState().getPhaseModel();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(DeviceAction proxy) {
		return proxy.getActionPlan() == action_plan;
	}

	/** Get a table row sorter */
	@Override
	public RowSorter<ProxyTableModel<DeviceAction>> createSorter() {
		TableRowSorter<ProxyTableModel<DeviceAction>> sorter =
			new TableRowSorter<ProxyTableModel<DeviceAction>>(this)
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
		String ht = Hashtags.normalize(hashtag);
		if (ht != null && ht.equals(hashtag))
			create(hashtag);
		else
			showHint("hashtag.invalid.hint");
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
				SignMsgPriority.medium_1.ordinal());
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
