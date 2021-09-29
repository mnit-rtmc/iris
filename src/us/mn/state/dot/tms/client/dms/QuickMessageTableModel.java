/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2021  Minnesota Department of Transportation
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

import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.MsgCombining;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import static us.mn.state.dot.tms.client.widget.IOptionPane.showHint;

/**
 * Table model for quick messages, which is for editing and creating
 * quick messages.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class QuickMessageTableModel extends ProxyTableModel<QuickMessage> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<QuickMessage> descriptor(Session s) {
		return new ProxyDescriptor<QuickMessage>(
			s.getSonarState().getDmsCache().getQuickMessages(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<QuickMessage>> createColumns() {
		ArrayList<ProxyColumn<QuickMessage>> cols =
			new ArrayList<ProxyColumn<QuickMessage>>(4);
		cols.add(new ProxyColumn<QuickMessage>("quick.message.name",
			180)
		{
			public Object getValueAt(QuickMessage qm) {
				return qm.getName();
			}
		});
		cols.add(new ProxyColumn<QuickMessage>("dms.group", 120) {
			public Object getValueAt(QuickMessage qm) {
				return qm.getSignGroup();
			}
			public boolean isEditable(QuickMessage qm) {
				return canWrite(qm);
			}
			public void setValueAt(QuickMessage qm, Object value) {
				qm.setSignGroup(lookupSignGroup(value));
			}
		});
		cols.add(new ProxyColumn<QuickMessage>("quick.message.config",
			120)
		{
			public Object getValueAt(QuickMessage qm) {
				return qm.getSignConfig();
			}
			public boolean isEditable(QuickMessage qm) {
				return canWrite(qm);
			}
			public void setValueAt(QuickMessage qm, Object value) {
				qm.setSignConfig((value instanceof SignConfig)
				                ? (SignConfig) value
				                : null);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<SignConfig> cbx =
					new JComboBox<SignConfig>();
				cbx.setModel(new IComboBoxModel<SignConfig>(
					config_mdl));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<QuickMessage>("dms.msg.combining", 100)
		{
			public Object getValueAt(QuickMessage qm) {
				int mc = qm.getMsgCombining();
				return MsgCombining.fromOrdinal(mc);
			}
			public boolean isEditable(QuickMessage qm) {
				return canWrite(qm);
			}
			public void setValueAt(QuickMessage qm, Object value) {
				if (value instanceof MsgCombining) {
					MsgCombining mc = (MsgCombining) value;
					qm.setMsgCombining(mc.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<MsgCombining> cbx =
					new JComboBox<MsgCombining>(
					MsgCombining.values());
				return new DefaultCellEditor(cbx);
			}
		});
		return cols;
	}

	/** Sign configuration proxy list model */
	private final ProxyListModel<SignConfig> config_mdl;

	/** Lookup a sign group */
	private SignGroup lookupSignGroup(Object value) {
		String v = value.toString().trim();
		if (v.length() > 0) {
			SignGroup sg = SignGroupHelper.lookup(v);
			if (null == sg)
				showHint("dms.group.unknown.hint");
			return sg;
		} else
			return null;
	}

	/** Create a new table model.
	 * @param s Session */
	public QuickMessageTableModel(Session s) {
		super(s, descriptor(s), 12, 20);
		config_mdl = new ProxyListModel<SignConfig>(
			s.getSonarState().getDmsCache().getSignConfigs());
	}

	/** Initialize the model */
	@Override
	public void initialize() {
		super.initialize();
		config_mdl.initialize();
	}

	/** Dispose of the model */
	@Override
	public void dispose() {
		config_mdl.dispose();
		super.dispose();
	}
}
