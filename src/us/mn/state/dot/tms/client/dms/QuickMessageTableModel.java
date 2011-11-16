/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.MultiParser;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for quick messages, which is for editing and creating
 * quick messages.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class QuickMessageTableModel extends ProxyTableModel<QuickMessage> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<QuickMessage>("Name", 100) {
			public Object getValueAt(QuickMessage qm) {
				return qm.getName();
			}
			public boolean isEditable(QuickMessage qm) {
				return (qm == null) && canAdd();
			}
			public void setValueAt(QuickMessage qm, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<QuickMessage>("Sign Group", 120) {
			public Object getValueAt(QuickMessage qm) {
				return qm.getSignGroup();
			}
			public boolean isEditable(QuickMessage qm) {
				return canUpdate(qm);
			}
			public void setValueAt(QuickMessage qm, Object value) {
				String v = value.toString().trim();
				qm.setSignGroup(SignGroupHelper.lookup(v));
			}
		},
		new ProxyColumn<QuickMessage>("MULTI String", 680) {
			public Object getValueAt(QuickMessage qm) {
				return qm.getMulti();
			}
			public boolean isEditable(QuickMessage qm) {
				return canUpdate(qm);
			}
			public void setValueAt(QuickMessage qm, Object value) {
				qm.setMulti(MultiParser.normalize(
					value.toString()));
			}
		}
	    };
	}

	/** Create a new table model.
	 * @param s Session */
	public QuickMessageTableModel(Session s) {
		super(s, s.getSonarState().getDmsCache().getQuickMessages());
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return QuickMessage.SONAR_TYPE;
	}
}
