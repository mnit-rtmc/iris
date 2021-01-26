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
import us.mn.state.dot.tms.CapUrgencyField;
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
public class CapUrgencyFieldModel extends ProxyTableModel<CapUrgencyField> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<CapUrgencyField> descriptor(Session s) {
		return new ProxyDescriptor<CapUrgencyField>(
			s.getSonarState().getCapUrgencyFieldCache(), true);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<CapUrgencyField>> createColumns() {
		ArrayList<ProxyColumn<CapUrgencyField>> cols =
			new ArrayList<ProxyColumn<CapUrgencyField>>(3);
		cols.add(new ProxyColumn<CapUrgencyField>("alert.cap.event",
			300)
		{
			public Object getValueAt(CapUrgencyField cu) {
				return cu.getEvent();
			}
			public boolean isEditable(CapUrgencyField cu) {
				return canWrite(cu);
			}
			public void setValueAt(CapUrgencyField cu, Object value) {
				String ev = value.toString();
				if (ev == null || ev.isEmpty())
					cu.setEvent(CapUrgencyField.DEFAULT_EVENT);
				else
					cu.setEvent(ev);
			}
		});
		cols.add(new ProxyColumn<CapUrgencyField>("alert.cap.urgency",
			300)
		{
			public Object getValueAt(CapUrgencyField crt) {
				return CapUrgency.fromOrdinal(
					crt.getUrgency());
			}
			public boolean isEditable(CapUrgencyField crt) {
				return canWrite(crt);
			}
			public void setValueAt(CapUrgencyField crt,
				Object value)
			{
				int u = (value instanceof CapUrgency)
				      ?	((CapUrgency) value).ordinal()
				      : CapUrgency.UNKNOWN.ordinal();
				crt.setUrgency(u);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<CapUrgency> cbx =
					new JComboBox<CapUrgency>(
					CapUrgency.values());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<CapUrgencyField>("alert.cap.multi",
			300)
		{
			public Object getValueAt(CapUrgencyField crt) {
				return crt.getMulti();
			}
			public boolean isEditable(CapUrgencyField crt) {
				return canWrite(crt);
			}
			public void setValueAt(CapUrgencyField crt, Object value) {
				// TODO validate MULTI
				crt.setMulti(value.toString());
			}
		});
		return cols;
	}

	public CapUrgencyFieldModel(Session s) {
		super(s, descriptor(s), 12);
	}
}
