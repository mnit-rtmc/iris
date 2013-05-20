/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.units.Temperature;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PropStatus is a GUI panel for displaying status data on a DMS properties
 * form.
 *
 * @author Douglas Lau
 */
public class PropStatus extends FormPanel {

	/** Get temperature units to use for display */
	static private Temperature.Units tempUnits() {
		return SystemAttrEnum.CLIENT_UNITS_SI.getBoolean()
		     ? Temperature.Units.CELSIUS
		     : Temperature.Units.FAHRENHEIT;
	}

	/** Unknown value string */
	static private final String UNKNOWN = "???";

	/** Format a string field */
	static private String formatString(String s) {
		if(s != null && s.length() > 0)
			return s;
		else
			return UNKNOWN;
	}

	/** Format a temperature.
	 * @param temp Temperature in degrees Celsius. */
	static private String formatTemp(Integer temp) {
		if(temp != null) {
			Temperature.Formatter tf = new Temperature.Formatter(0);
			return tf.format(new Temperature(temp).convert(
				tempUnits()));
		} else
			return UNKNOWN;
	}

	/** Format a temperature range.
	 * @param mn Minimum temp (Celsius).
	 * @param mx Maximum temp (Celsius).
	 * @return Formatted temperature range. */
	static private String formatTemp(Integer mn, Integer mx) {
		if(mn == null || mn == mx)
			return formatTemp(mx);
		else if(mx == null)
			return formatTemp(mn);
		else
			return formatTemp(mn) + "..." + formatTemp(mx);
	}

	/** Cabinet temperature label */
	private final JLabel temp_cabinet_lbl = createValueLabel();

	/** Ambient temperature label */
	private final JLabel temp_ambient_lbl = createValueLabel();

	/** Housing temperature label */
	private final JLabel temp_housing_lbl = createValueLabel();

	/** Power supply status table */
	private final ZTable power_tbl = new ZTable();

	/** Operation description label */
	private final JLabel operation_lbl = createValueLabel();

	/** Query message action */
	private final IAction query_msg = new IAction("dms.query.msg",
		SystemAttrEnum.DMS_QUERYMSG_ENABLE)
	{
		protected void do_perform() {
			dms.setDeviceRequest(DeviceRequest.
				QUERY_MESSAGE.ordinal());
		}
	};

	/** Reset DMS action */
	private final IAction reset = new IAction("dms.reset",
		SystemAttrEnum.DMS_RESET_ENABLE)
	{
		protected void do_perform() {
			dms.setDeviceRequest(DeviceRequest.
				RESET_DEVICE.ordinal());
		}
	};

	/** Query status action */
	private final IAction query_status = new IAction("dms.query.status") {
		protected void do_perform() {
			dms.setDeviceRequest(DeviceRequest.
				QUERY_STATUS.ordinal());
		}
	};

	/** Send settings action */
	private final IAction settings = new IAction("device.send.settings") {
		protected void do_perform() {
			dms.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** User session */
	private final Session session;

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties status panel */
	public PropStatus(Session s, DMS sign) {
		super(true);
		session = s;
		dms = sign;
	}

	/** Initialize the widgets on the panel */
	public void initialize() {
		power_tbl.setAutoCreateColumnsFromModel(false);
		power_tbl.setVisibleRowCount(6);
		addRow(I18N.get("dms.temp.cabinet"), temp_cabinet_lbl);
		addRow(I18N.get("dms.temp.ambient"), temp_ambient_lbl);
		addRow(I18N.get("dms.temp.housing"), temp_housing_lbl);
		addRow(I18N.get("dms.power.supplies"), power_tbl);
		add(I18N.get("device.operation"), operation_lbl);
		if(query_msg.getIEnabled())
			add(new JButton(query_msg));
		finishRow();
		addRow(createButtonPanel());
		updateAttribute(null);
	}

	/** Create the button panel */
	private JPanel createButtonPanel() {
		JPanel p = new JPanel(new FlowLayout());
		p.add(new JButton(query_status));
		p.add(new JButton(settings));
		if(reset.getIEnabled())
			p.add(new JButton(reset));
		return p;
	}

	/** Update one attribute on the panel */
	public void updateAttribute(String a) {
		if(a == null || a.equals("minCabinetTemp") ||
		   a.equals("maxCabinetTemp"))
		{
			temp_cabinet_lbl.setText(formatTemp(
				dms.getMinCabinetTemp(),
				dms.getMaxCabinetTemp()));
		}
		if(a == null || a.equals("minAmbientTemp") ||
		   a.equals("maxAmbientTemp"))
		{
			temp_ambient_lbl.setText(formatTemp(
				dms.getMinAmbientTemp(),
				dms.getMaxAmbientTemp()));
		}
		if(a == null || a.equals("minHousingTemp") ||
		   a.equals("maxHousingTemp"))
		{
			temp_housing_lbl.setText(formatTemp(
				dms.getMinHousingTemp(),
				dms.getMaxHousingTemp()));
		}
		if(a == null || a.equals("powerStatus"))
			updatePowerStatus();
		if(a == null || a.equals("operation"))
			operation_lbl.setText(dms.getOperation());
		if(a == null) {
			boolean r = canRequest();
			query_msg.setEnabled(r);
			reset.setEnabled(r);
			query_status.setEnabled(r);
			settings.setEnabled(r);
		}
	}

	/** Update the power status */
	private void updatePowerStatus() {
		String[] s = dms.getPowerStatus();
		if(s != null) {
			PowerTableModel m = new PowerTableModel(s);
			power_tbl.setColumnModel(m.createColumnModel());
			power_tbl.setModel(m);
		}
	}

	/** Check if the user can update an attribute */
	private boolean canUpdate(String aname) {
		return session.canUpdate(dms, aname);
	}

	/** Check if the user can make device requests */
	private boolean canRequest() {
		return canUpdate("deviceRequest");
	}
}
