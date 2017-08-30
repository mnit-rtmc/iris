/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import static us.mn.state.dot.tms.client.widget.IOptionPane.showHint;
import us.mn.state.dot.tms.utils.MultiString;

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
			new ArrayList<ProxyColumn<QuickMessage>>(3);
		cols.add(new ProxyColumn<QuickMessage>("quick.message.name",
			100)
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
				return canUpdate(qm);
			}
			public void setValueAt(QuickMessage qm, Object value) {
				qm.setSignGroup(lookupSignGroup(value));
			}
		});
		cols.add(new ProxyColumn<QuickMessage>("quick.message.multi",
			680)
		{
			public Object getValueAt(QuickMessage qm) {
				return qm.getMulti();
			}
			public boolean isEditable(QuickMessage qm) {
				return canUpdate(qm);
			}
			public void setValueAt(QuickMessage qm, Object value) {
				qm.setMulti(new MultiString(value.toString())
					.normalize());
			}
		});
		return cols;
	}

	/** Lookup a sign group */
	private SignGroup lookupSignGroup(Object value) {
		String v = value.toString().trim();
		if (v.length() > 0) {
			SignGroup sg = SignGroupHelper.lookup(v);
			if (null == sg)
				showHint("quick.message.sign.group.hint");
			return sg;
		} else
			return null;
	}

	/** Create a new table model.
	 * @param s Session */
	public QuickMessageTableModel(Session s) {
		super(s, descriptor(s), 12, 20);
	}
}
