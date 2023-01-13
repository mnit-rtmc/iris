/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MsgPattern;
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
 * Table model for editing and creating message patterns.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgPatternTableModel extends ProxyTableModel<MsgPattern> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<MsgPattern> descriptor(Session s) {
		return new ProxyDescriptor<MsgPattern>(
			s.getSonarState().getDmsCache().getMsgPatterns(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<MsgPattern>> createColumns() {
		ArrayList<ProxyColumn<MsgPattern>> cols =
			new ArrayList<ProxyColumn<MsgPattern>>(3);
		cols.add(new ProxyColumn<MsgPattern>("msg.pattern.name",
			180)
		{
			public Object getValueAt(MsgPattern pat) {
				return pat.getName();
			}
		});
		cols.add(new ProxyColumn<MsgPattern>("dms.group", 120) {
			public Object getValueAt(MsgPattern pat) {
				return pat.getSignGroup();
			}
			public boolean isEditable(MsgPattern pat) {
				return canWrite(pat);
			}
			public void setValueAt(MsgPattern pat, Object value) {
				pat.setSignGroup(lookupSignGroup(value));
			}
		});
		cols.add(new ProxyColumn<MsgPattern>("msg.pattern.config",
			120)
		{
			public Object getValueAt(MsgPattern pat) {
				return pat.getSignConfig();
			}
			public boolean isEditable(MsgPattern pat) {
				return canWrite(pat);
			}
			public void setValueAt(MsgPattern pat, Object value) {
				pat.setSignConfig((value instanceof SignConfig)
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
	public MsgPatternTableModel(Session s) {
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
