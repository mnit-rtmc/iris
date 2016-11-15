/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import javax.swing.JPanel;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * PropBrightness is a GUI panel for displaying brightness data on a DMS
 * properties form.
 *
 * @author Douglas Lau
 */
public class PropBrightness extends IPanel {

	/** Unknown value string */
	static private final String UNKNOWN = "???";

	/** Photocell status table */
	private final ZTable photocell_tbl = new ZTable();

	/** Light output label */
	private final JLabel output_lbl = createValueLabel();

	/** Current brightness low feedback action */
	private final IAction bright_low = new IAction("dms.brightness.low") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(DeviceRequest.
				BRIGHTNESS_TOO_DIM.ordinal());
		}
	};

	/** Current brightness good feedback action */
	private final IAction bright_good = new IAction("dms.brightness.good") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(DeviceRequest.
				BRIGHTNESS_GOOD.ordinal());
		}
	};

	/** Current brightness high feedback action */
	private final IAction bright_high = new IAction("dms.brightness.high") {
		protected void doActionPerformed(ActionEvent e) {
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
		session = s;
		dms = sign;
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		photocell_tbl.setAutoCreateColumnsFromModel(false);
		photocell_tbl.setVisibleRowCount(6);
		add("dms.brightness.photocells");
		add(photocell_tbl, Stretch.FULL);
		add("dms.brightness.output");
		add(output_lbl, Stretch.LAST);
		add("dms.brightness.feedback");
		add(buildButtonBox(), Stretch.LEFT);
		updateAttribute(null);
	}

	/** Build the button box */
	private Box buildButtonBox() {
		Box box = Box.createHorizontalBox();
		box.add(new JButton(bright_low));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(bright_good));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(bright_high));
		return box;
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
			dms.getMsgCurrent());
		bright_low.setEnabled(enable);
		bright_good.setEnabled(enable);
		bright_high.setEnabled(enable);
	}

	/** Check if the user is permitted to update an attribute */
	private boolean isUpdatePermitted(String aname) {
		return session.isUpdatePermitted(dms, aname);
	}

	/** Check if the user can make device requests */
	private boolean canRequest() {
		return isUpdatePermitted("deviceRequest");
	}
}
