/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.alert;

import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;

import us.mn.state.dot.tms.CapUrgency;
import us.mn.state.dot.tms.CapUrgencyEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for CAP urgency substitution values.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class CapUrgencyModel extends ProxyTableModel<CapUrgency> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<CapUrgency> descriptor(Session s) {
		return new ProxyDescriptor<CapUrgency>(
				s.getSonarState().getCapUrgencyCache(), true);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<CapUrgency>> createColumns() {
		ArrayList<ProxyColumn<CapUrgency>> cols =
				new ArrayList<ProxyColumn<CapUrgency>>(3);
		cols.add(new ProxyColumn<CapUrgency>("alert.cap.event", 300) {
			public Object getValueAt(CapUrgency cu) {
				return cu.getEvent();
			}
			public boolean isEditable(CapUrgency cu) {
				return canWrite(cu);
			}
			public void setValueAt(CapUrgency cu, Object value) {
				String ev = value.toString();
				if (ev == null || ev.isEmpty())
					cu.setEvent(CapUrgency.DEFAULT_EVENT);
				else
					cu.setEvent(ev);
			}
		});
		cols.add(new ProxyColumn<CapUrgency>(
				"alert.cap.urgency", 300) {
			public Object getValueAt(CapUrgency crt) {
				return crt.getUrgency();
			}
			public boolean isEditable(CapUrgency crt) {
				return canWrite(crt);
			}
			public void setValueAt(CapUrgency crt, Object value) {
				crt.setUrgency(value.toString());
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<String> cbx = new JComboBox<String>(
						CapUrgencyEnum.stringValues());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<CapUrgency>("alert.cap.multi", 300) {
			public Object getValueAt(CapUrgency crt) {
				return crt.getMulti();
			}
			public boolean isEditable(CapUrgency crt) {
				return canWrite(crt);
			}
			public void setValueAt(CapUrgency crt, Object value) {
				// TODO validate MULTI
				crt.setMulti(value.toString());
			}
		});
		return cols;
	}

	public CapUrgencyModel(Session s) {
		super(s, descriptor(s), 12);
	}
	
}
