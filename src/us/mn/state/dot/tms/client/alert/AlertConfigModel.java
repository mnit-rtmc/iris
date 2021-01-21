/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
 * Copyright (C) 2021  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.IpawsConfig;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
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
public class AlertConfigModel extends ProxyTableModel<IpawsConfig> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<IpawsConfig> descriptor(Session s) {
		return new ProxyDescriptor<IpawsConfig>(
				s.getSonarState().getIpawsConfigCache(), false, true, true);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<IpawsConfig>> createColumns() {
		ArrayList<ProxyColumn<IpawsConfig>> cols =
				new ArrayList<ProxyColumn<IpawsConfig>>(3);
		cols.add(new ProxyColumn<IpawsConfig>("alert.config.event", 300) {
			public Object getValueAt(IpawsConfig iac) {
				return iac.getEvent();
			}
			public boolean isEditable(IpawsConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsConfig iac, Object value) {
				iac.setEvent(value.toString());
			}
		});
		cols.add(new ProxyColumn<IpawsConfig>("alert.config.sign_group",
			200)
		{
			public Object getValueAt(IpawsConfig iac) {
				return iac.getSignGroup();
			}
			public boolean isEditable(IpawsConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsConfig iac, Object value) {
				iac.setSignGroup((value instanceof SignGroup)
					? (SignGroup) value
					: null);
			}
			protected TableCellEditor createCellEditor() {
				ArrayList<SignGroup> sgl =
					new ArrayList<SignGroup>();
				Iterator<SignGroup> it = SignGroupHelper.iterator();
				while (it.hasNext())
					sgl.add(it.next());

				JComboBox<SignGroup> cbx = new JComboBox<SignGroup>(
					new DefaultComboBoxModel<SignGroup>(
						sgl.toArray(new SignGroup[0])));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<IpawsConfig>(
				"alert.config.quick_message", 200) {
			public Object getValueAt(IpawsConfig iac) {
				return iac.getQuickMessage();
			}
			public boolean isEditable(IpawsConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsConfig iac, Object value) {
				iac.setQuickMessage(
					(value instanceof QuickMessage)
					? (QuickMessage) value
					: null
				);
			}
			protected TableCellEditor createCellEditor() {
				ArrayList<QuickMessage> qml =
					new ArrayList<QuickMessage>();
				Iterator<QuickMessage> it = QuickMessageHelper
					.iterator();
				while (it.hasNext())
					qml.add(it.next());
				JComboBox<QuickMessage> cbx = new JComboBox
					<QuickMessage>(new DefaultComboBoxModel
<QuickMessage>(
						qml.toArray(new QuickMessage[0])));
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<IpawsConfig>(
				"alert.config.pre_alert_time", 100) {
			public Object getValueAt(IpawsConfig iac) {
				return iac.getPreAlertTime();
			}
			public boolean isEditable(IpawsConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsConfig iac, Object value) {
				try {
					iac.setPreAlertTime(Integer.valueOf(value.toString()));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});
		cols.add(new ProxyColumn<IpawsConfig>(
				"alert.config.post_alert_time", 100) {
			public Object getValueAt(IpawsConfig iac) {
				return iac.getPostAlertTime();
			}
			public boolean isEditable(IpawsConfig iac) {
				return canWrite(iac);
			}
			public void setValueAt(IpawsConfig iac, Object value) {
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
