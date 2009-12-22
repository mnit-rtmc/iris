/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.system;

import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * The system attribute allows administrators to change system-wide policy
 * attributes.
 *
 * @author Douglas Lau
 */
public class SystemAttributeForm extends ProxyTableForm<SystemAttribute> {

	/** Create a new system attribute form */
	public SystemAttributeForm(Session s) {
		super("System Attributes", new SystemAttributeTableModel(s));
		setHelpPageName("Help.SystemAttributeForm");
	}

	/** Create the table */
	protected ZTable createTable() {
		return new ZTable() {
			public String getToolTipText(int row, int column) {
				Object value = model.getValueAt(row,
					SystemAttributeTableModel.COL_NAME);
				if(value instanceof String) {
					return SystemAttrEnum.getDesc(
						(String)value);
				} else
					return null;
			}
		};
	}

	/** Get the row height */
	protected int getRowHeight() {
		return 20;
	}

	/** Get the visible row count */
	protected int getVisibleRowCount() {
		return 12;
	}
}
