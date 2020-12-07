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
import java.util.Iterator;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;

import us.mn.state.dot.tms.IpawsAlertConfig;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for (IPAWS) alert configurations.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class AlertConfigModel extends ProxyTableModel<IpawsAlertConfig> {
	
	/** Create a proxy descriptor */
	static public ProxyDescriptor<IpawsAlertConfig> descriptor(Session s) {
		return new ProxyDescriptor<IpawsAlertConfig>(
				s.getSonarState().getIpawsConfigCache(), false, true, true);
	}
	
	/** List of sign groups */
	ArrayList<String> sgl;
	
	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<IpawsAlertConfig>> createColumns() {
		ArrayList<ProxyColumn<IpawsAlertConfig>> cols =
				new ArrayList<ProxyColumn<IpawsAlertConfig>>(3);
		cols.add(new ProxyColumn<IpawsAlertConfig>("alert.config.event", 300) {
			public Object getValueAt(IpawsAlertConfig iac) {
				return iac.getEvent();
			}
			public boolean isEditable(IpawsAlertConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsAlertConfig iac, Object value) {
				iac.setEvent(value.toString());
			}
		});
		cols.add(new ProxyColumn<IpawsAlertConfig>(
				"alert.config.sign_group", 200) {
			public Object getValueAt(IpawsAlertConfig iac) {
				return iac.getSignGroup();
			}
			public boolean isEditable(IpawsAlertConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsAlertConfig iac, Object value) {
				iac.setSignGroup((String) value);
			}
			protected TableCellEditor createCellEditor() {
				Iterator<SignGroup> it = session.getSonarState()
						.getDmsCache().getSignGroups().iterator();
				sgl = new ArrayList<String>();
				while (it.hasNext())
					sgl.add(it.next().getName());
				sgl.sort(String::compareToIgnoreCase);
				JComboBox<String> cbx = new JComboBox<String>(
						new DefaultComboBoxModel<String>(
								sgl.toArray(new String[0])));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<IpawsAlertConfig>(
				"alert.config.quick_message", 200) {
			public Object getValueAt(IpawsAlertConfig iac) {
				return iac.getQuickMessage();
			}
			public boolean isEditable(IpawsAlertConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsAlertConfig iac, Object value) {
				iac.setQuickMessage((String) value);
			}
			protected TableCellEditor createCellEditor() {
				Iterator<QuickMessage> it = session.getSonarState()
						.getDmsCache().getQuickMessages().iterator();
				ArrayList<String> qml = new ArrayList<String>();
				while (it.hasNext())
					qml.add(it.next().getName());
				qml.sort(String::compareToIgnoreCase);
				JComboBox<String> cbx = new JComboBox<String>(
						new DefaultComboBoxModel<String>(
								qml.toArray(new String[0])));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<IpawsAlertConfig>(
				"alert.config.pre_alert_time", 100) {
			public Object getValueAt(IpawsAlertConfig iac) {
				return iac.getPreAlertTime();
			}
			public boolean isEditable(IpawsAlertConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsAlertConfig iac, Object value) {
				try {
					iac.setPreAlertTime(Integer.valueOf(value.toString()));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});
		cols.add(new ProxyColumn<IpawsAlertConfig>(
				"alert.config.post_alert_time", 100) {
			public Object getValueAt(IpawsAlertConfig iac) {
				return iac.getPostAlertTime();
			}
			public boolean isEditable(IpawsAlertConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsAlertConfig iac, Object value) {
				try {
					iac.setPostAlertTime(Integer.valueOf(value.toString()));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});
		return cols;
	}
	
	/** Create a new alert config table model */
	public AlertConfigModel(Session s) {
		super(s, descriptor(s), 12);
	}
}
