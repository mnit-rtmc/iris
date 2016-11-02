/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
 * Copyright (C) 2015  SRF Consulting Group
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
package us.mn.state.dot.tms.client.comm;

import java.util.ArrayList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.Modem;
import us.mn.state.dot.tms.ModemState;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for modems
 *
 * @author Douglas Lau
 */
public class ModemModel extends ProxyTableModel<Modem> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Modem> descriptor(Session s) {
		return new ProxyDescriptor<Modem>(
			s.getSonarState().getConCache().getModems(),
			false
		);
	}

	/** Maximum modem timeout (ms) */
	static private final int MAX_MODEM_TIMEOUT = 90000;

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Modem>> createColumns() {
		ArrayList<ProxyColumn<Modem>> cols =
			new ArrayList<ProxyColumn<Modem>>(4);
		cols.add(new ProxyColumn<Modem>("modem", 80) {
			public Object getValueAt(Modem m) {
				return m.getName();
			}
		});
		cols.add(new ProxyColumn<Modem>("comm.link.uri", 280) {
			public Object getValueAt(Modem m) {
				return m.getUri();
			}
			public boolean isEditable(Modem m) {
				return canUpdate(m, "uri");
			}
			public void setValueAt(Modem m, Object value) {
				m.setUri(value.toString().trim());
			}
		});
		cols.add(new ProxyColumn<Modem>("modem.config", 200) {
			public Object getValueAt(Modem m) {
				return m.getConfig();
			}
			public boolean isEditable(Modem m) {
				return canUpdate(m, "config");
			}
			public void setValueAt(Modem m, Object value) {
				m.setConfig(value.toString().trim());
			}
		});
		cols.add(new ProxyColumn<Modem>("modem.timeout", 80) {
			public Object getValueAt(Modem m) {
				return m.getTimeout();
			}
			public boolean isEditable(Modem m) {
				return canUpdate(m, "timeout");
			}
			public void setValueAt(Modem m, Object value) {
				if (value instanceof Integer)
					m.setTimeout((Integer) value);
			}
			protected TableCellEditor createCellEditor() {
				return new TimeoutCellEditor(MAX_MODEM_TIMEOUT);
			}
		});
		cols.add(new ProxyColumn<Modem>("modem.status", 80) {
			public Object getValueAt(Modem m) {
				return ModemState.fromOrdinal(m.getState());
			}
			public boolean isEditable(Modem m) {
				return false;
			}
			protected TableCellRenderer createCellRenderer(){
				return new ModemStatusCellRenderer();
			}
		});
		cols.add(new ProxyColumn<Modem>("modem.enable", 75,
			Boolean.class)
		{
			public Object getValueAt(Modem m) {
				return m.getEnabled();
			}
			public boolean isEditable(Modem m) {
				return canUpdate(m, "enable");
			}
			public void setValueAt(Modem m, Object value) {
				if (value instanceof Boolean)
					m.setEnabled((Boolean) value);
			}
		});
		return cols;
	}

	/** Create a new modem table model */
	public ModemModel(Session s) {
		super(s, descriptor(s),
		      true,	/* has_create_delete */
		      true);	/* has_name */
	}
}
