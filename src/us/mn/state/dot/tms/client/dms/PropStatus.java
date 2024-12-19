/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2024  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.json.JSONArray;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.units.Temperature;

/**
 * PropStatus is a GUI panel for displaying status data on a DMS properties
 * form.
 *
 * @author Douglas Lau
 */
public class PropStatus extends IPanel {

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
	static private String formatTemp(Object temp) {
		if (temp instanceof Integer) {
			Integer t = (Integer) temp;
			Temperature.Formatter tf = new Temperature.Formatter(0);
			return tf.format(new Temperature(t).convert(
				tempUnits()));
		} else
			return UNKNOWN;
	}

	/** Format a temperature range.
	 * @param temps Array of temps (Celsius).
	 * @return Formatted temperature range. */
	static private String formatTemps(Object temps) {
		Integer mn = null;
		Integer mx = null;
		if (temps instanceof JSONArray) {
			JSONArray arr = (JSONArray) temps;
			for (int i = 0; i < arr.length(); i++) {
				Object val = arr.opt(i);
				if (val instanceof Integer) {
					Integer temp = (Integer) val;
					if (mn == null || temp < mn)
						mn = temp;
					if (mx == null || temp > mx)
						mx = temp;
				}
			}
		}
		if (mn == null || mn.equals(mx))
			return formatTemp(mx);
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

	/** Query message action */
	private final IAction query_msg_act = new IAction("dms.query.msg") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(DeviceRequest.
				QUERY_MESSAGE.ordinal());
		}
	};

	/** Button to query configuration */
	private final IAction query_config = new IAction("dms.query.config") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(DeviceRequest.
				QUERY_CONFIGURATION.ordinal());
		}
	};

	/** Query status action */
	private final IAction query_status = new IAction("dms.query.status") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(DeviceRequest.
				QUERY_STATUS.ordinal());
		}
	};

	/** Query settings action */
	private final IAction query_settings = new IAction(
		"device.query.settings")
	{
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(DeviceRequest.
				QUERY_SETTINGS.ordinal());
		}
	};

	/** Send settings action */
	private final IAction send_settings = new IAction(
		"device.send.settings")
	{
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** DMS reset action */
	private final IAction reset = new IAction("dms.reset") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(DeviceRequest.
				RESET_DEVICE.ordinal());
		}
	};

	/** User session */
	private final Session session;

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties status panel */
	public PropStatus(Session s, DMS sign) {
		session = s;
		dms = sign;
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		power_tbl.setAutoCreateColumnsFromModel(false);
		power_tbl.setVisibleRowCount(6);
		add("dms.temp.cabinet");
		add(temp_cabinet_lbl, Stretch.LAST);
		add("dms.temp.ambient");
		add(temp_ambient_lbl, Stretch.LAST);
		add("dms.temp.housing");
		add(temp_housing_lbl, Stretch.LAST);
		add("dms.power.supplies");
		add(power_tbl, Stretch.FULL);
		add(buildButtonBox(), Stretch.RIGHT);
		updateAttribute(null);
	}

	/** Build the button box */
	private Box buildButtonBox() {
		Box box = Box.createHorizontalBox();
		box.add(new JButton(query_msg_act));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(query_status));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(query_config));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(query_settings));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(send_settings));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(reset));
		return box;
	}

	/** Update one attribute on the panel */
	public void updateAttribute(String a) {
		if (a == null || a.equals("status")) {
			temp_cabinet_lbl.setText(formatTemps(
				DMSHelper.optStatus(dms, DMS.CABINET_TEMPS)
			));
			temp_ambient_lbl.setText(formatTemps(
				DMSHelper.optStatus(dms, DMS.AMBIENT_TEMPS)
			));
			temp_housing_lbl.setText(formatTemps(
				DMSHelper.optStatus(dms, DMS.HOUSING_TEMPS)
			));
			updatePowerStatus();
		}
		if (null == a) {
			boolean r = canRequest();
			query_msg_act.setEnabled(r);
			query_config.setEnabled(r);
			query_status.setEnabled(r);
			query_settings.setEnabled(r);
			send_settings.setEnabled(r);
			reset.setEnabled(r);
		}
	}

	/** Update the power status */
	private void updatePowerStatus() {
		Object ps = DMSHelper.optStatus(dms, DMS.POWER_SUPPLIES);
		if (ps instanceof JSONArray) {
			PowerTableModel m = new PowerTableModel((JSONArray) ps);
			power_tbl.setColumnModel(m.createColumnModel());
			power_tbl.setModel(m);
		}
	}

	/** Check if the user is permitted to write an attribute */
	private boolean isWritePermitted(String aname) {
		return session.isWritePermitted(dms, aname);
	}

	/** Check if the user can make device requests */
	private boolean canRequest() {
		return isWritePermitted("deviceRequest");
	}
}
