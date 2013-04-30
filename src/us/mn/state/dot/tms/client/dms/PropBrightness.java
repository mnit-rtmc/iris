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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PropBrightness is a GUI panel for displaying brightness data on a DMS
 * properties form.
 *
 * @author Douglas Lau
 */
public class PropBrightness extends FormPanel {

	/** Unknown value string */
	static private final String UNKNOWN = "???";

	/** Photocell status table */
	private final ZTable photocell_tbl = new ZTable();

	/** Light output label */
	private final JLabel output_lbl = createValueLabel();

	/** Current brightness low feedback action */
	private final IAction bright_low = new IAction("dms.brightness.low") {
		@Override protected void do_perform() {
			dms.setDeviceRequest(DeviceRequest.
				BRIGHTNESS_TOO_DIM.ordinal());
		}
	};

	/** Current brightness good feedback action */
	private final IAction bright_good = new IAction("dms.brightness.good") {
		@Override protected void do_perform() {
			dms.setDeviceRequest(DeviceRequest.
				BRIGHTNESS_GOOD.ordinal());
		}
	};

	/** Current brightness high feedback action */
	private final IAction bright_high = new IAction("dms.brightness.high") {
		@Override protected void do_perform() {
			dms.setDeviceRequest(DeviceRequest.
				BRIGHTNESS_TOO_BRIGHT.ordinal());
		}
	};

	/** User session */
	private final Session session;

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties brightness panel */
	public PropBrightness(Session s, DMS sign) {
		super(true);
		session = s;
		dms = sign;
	}

	/** Initialize the widgets on the panel */
	public void initialize() {
		photocell_tbl.setAutoCreateColumnsFromModel(false);
		photocell_tbl.setVisibleRowCount(6);
		JPanel f_pnl = new JPanel();
		f_pnl.add(new JButton(bright_low));
		f_pnl.add(new JButton(bright_good));
		f_pnl.add(new JButton(bright_high));
		addRow(I18N.get("dms.brightness.photocells"), photocell_tbl);
		addRow(I18N.get("dms.brightness.output"), output_lbl);
		addRow(I18N.get("dms.brightness.feedback"), f_pnl);
		updateAttribute(null);
	}

	/** Update one attribute on the panel */
	public void updateAttribute(String a) {
		if(a == null || a.equals("photocellStatus"))
			updatePhotocellStatus();
		if(a == null || a.equals("lightOutput") ||
		   a.equals("messageCurrent"))
		{
			Integer o = dms.getLightOutput();
			if(o != null)
				output_lbl.setText("" + o + "%");
			else
				output_lbl.setText(UNKNOWN);
			updateFeedback();
		}
	}

	/** Update the photocell status */
	private void updatePhotocellStatus() {
		String[] s = dms.getPhotocellStatus();
		if(s != null) {
			PhotocellTableModel m = new PhotocellTableModel(s);
			photocell_tbl.setColumnModel(m.createColumnModel());
			photocell_tbl.setModel(m);
		}
	}

	/** Update the feedback buttons */
	private void updateFeedback() {
		boolean enable = canRequest() && !SignMessageHelper.isBlank(
			dms.getMessageCurrent());
		bright_low.setEnabled(enable);
		bright_good.setEnabled(enable);
		bright_high.setEnabled(enable);
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
