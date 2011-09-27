/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.Modem;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for modems
 *
 * @author Douglas Lau
 */
public class ModemModel extends ProxyTableModel<Modem> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Modem>("Modem", 80) {
			public Object getValueAt(Modem m) {
				return m.getName();
			}
			public boolean isEditable(Modem m) {
				return (m == null) && canAdd();
			}
			public void setValueAt(Modem m, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<Modem>("Config String", 200) {
			public Object getValueAt(Modem m) {
				return m.getConfig();
			}
			public boolean isEditable(Modem m) {
				return canUpdate(m, "config");
			}
			public void setValueAt(Modem m, Object value) {
				m.setConfig(value.toString().trim());
			}
		},
		new ProxyColumn<Modem>("URI", 280) {
			public Object getValueAt(Modem m) {
				return m.getUri();
			}
			public boolean isEditable(Modem m) {
				return canUpdate(m, "uri");
			}
			public void setValueAt(Modem m, Object value) {
				m.setUri(value.toString().trim());
			}
		},
		new ProxyColumn<Modem>("Timeout", 60) {
			public Object getValueAt(Modem m) {
				return m.getTimeout();
			}
			public boolean isEditable(Modem m) {
				return canUpdate(m, "timeout");
			}
			public void setValueAt(Modem m, Object value) {
				if(value instanceof Integer)
					m.setTimeout((Integer)value);
			}
			protected TableCellEditor createCellEditor() {
				return new TimeoutCellEditor();
			}
		}
	    };
	}

	/** Create a new modem table model */
	public ModemModel(Session s) {
		super(s, s.getSonarState().getConCache().getModems());
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Modem.SONAR_TYPE;
	}
}
