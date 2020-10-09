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

import us.mn.state.dot.tms.CapResponseType;
import us.mn.state.dot.tms.CapResponseTypeEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Table model for CAP response type substitution values.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class CapResponseTypeModel extends ProxyTableModel<CapResponseType> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<CapResponseType> descriptor(Session s) {
		return new ProxyDescriptor<CapResponseType>(
				s.getSonarState().getCapResponseTypeCache(), true);
	}
	
	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<CapResponseType>> createColumns() {
		ArrayList<ProxyColumn<CapResponseType>> cols =
				new ArrayList<ProxyColumn<CapResponseType>>(3);
		cols.add(new ProxyColumn<CapResponseType>("alert.cap.event", 300) {
			public Object getValueAt(CapResponseType crt) {
				return crt.getEvent();
			}
			public boolean isEditable(CapResponseType crt) {
				return canWrite(crt);
			}
			public void setValueAt(CapResponseType crt, Object value) {
				String ev = value.toString();
				if (ev == null || ev.isEmpty())
					crt.setEvent(CapResponseType.DEFAULT_EVENT);
				else
					crt.setEvent(ev);
			}
		});
		cols.add(new ProxyColumn<CapResponseType>(
				"alert.cap.response_type", 300) {
			public Object getValueAt(CapResponseType crt) {
				return crt.getResponseType();
			}
			public boolean isEditable(CapResponseType crt) {
				return canWrite(crt);
			}
			public void setValueAt(CapResponseType crt, Object value) {
				crt.setResponseType(value.toString());
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<String> cbx = new JComboBox<String>(
						CapResponseTypeEnum.stringValues());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<CapResponseType>("alert.cap.multi", 300) {
			public Object getValueAt(CapResponseType crt) {
				return crt.getMulti();
			}
			public boolean isEditable(CapResponseType crt) {
				return canWrite(crt);
			}
			public void setValueAt(CapResponseType crt, Object value) {
				// TODO validate MULTI
				crt.setMulti(value.toString());
			}
		});
		return cols;
	}

	public CapResponseTypeModel(Session s) {
		super(s, descriptor(s), 12);
	}
	
}
