/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2026  Minnesota Department of Transportation
 * Copyright (C) 2021  Iteris Inc.
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PropPixels is a GUI panel for displaying pixel data on a DMS properties
 * form.
 *
 * @author Douglas Lau
 * @author Deb Behera
 */
public class PropPixels extends IPanel {

	/** Unknown value string */
	static private final String UNKNOWN = "???";

	/** Bad pixel count label */
	private final JLabel bad_pixels_lbl = createValueLabel();

	/** Stuck off pixel panel */
	private final SignPixelPanel stuck_off_pnl = new SignPixelPanel(400,
		100);

	/** Stuck on pixel panel */
	private final SignPixelPanel stuck_on_pnl = new SignPixelPanel(400,
		100);

	/** Action to query pixel failures */
	private final IAction query_pixels = new IAction("dms.query.pixels") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(DeviceRequest.
				QUERY_PIXEL_FAILURES.ordinal());
			updatePixelStatus();
		}
	};

	/** Action to test pixel failures */
	private final IAction test_pixels = new IAction("dms.test.pixels") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(
				DeviceRequest.TEST_PIXELS.ordinal());
			updatePixelStatus();
		}
	};

	/** Action to reset pixel errors */
	private final IAction reset_status = new IAction("dms.reset.pixels") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setDeviceRequest(
				DeviceRequest.RESET_STATUS.ordinal());
			updatePixelStatus();
		}
	};

	/** User session */
	private final Session session;

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties pixels panel */
	public PropPixels(Session s, DMS sign) {
		session = s;
		dms = sign;
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		add("dms.pixel.errors");
		add(bad_pixels_lbl, Stretch.LAST);
		add(createTitledPanel("dms.pixel.errors.off", stuck_off_pnl),
			Stretch.FULL);
		add(createTitledPanel("dms.pixel.errors.on", stuck_on_pnl),
			Stretch.FULL);
		add(buildButtonBox(), Stretch.RIGHT);
		updateAttribute(null);
	}

	/** Create a panel with a titled border */
	private JPanel createTitledPanel(String text_id, JPanel p) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(I18N.get(
			text_id)));
		panel.add(p, BorderLayout.CENTER);
		return panel;
	}

	/** Build the button box */
	private Box buildButtonBox() {
		Box box = Box.createHorizontalBox();
		box.add(new JButton(query_pixels));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(test_pixels));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(reset_status));
		return box;
	}

	/** Update one attribute on the panel */
	public void updateAttribute(String a) {
		// NOTE: msgCurrent attribute changes after all sign
		//       dimension attributes are updated.
		if (a == null || a.equals("pixelFailures") ||
		    a.equals("msgCurrent"))
			updatePixelStatus();
		if (a == null) {
			boolean r = canRequest();
			query_pixels.setEnabled(r);
			test_pixels.setEnabled(r);
			reset_status.setEnabled(r);
		}
	}

	/** Update stuck pixel status */
	private void updatePixelStatus() {
		updatePixelPanel(stuck_off_pnl);
		updatePixelPanel(stuck_on_pnl);
		stuck_off_pnl.setDrawModules(true);
		stuck_on_pnl.setDrawModules(true);
		try {
			BitmapGraphic stuck_off = DMSHelper.createStuckBitmap(
				dms, DMSHelper.STUCK_OFF
			);
			stuck_off_pnl.setGraphic(stuck_off);
			BitmapGraphic stuck_on = DMSHelper.createStuckBitmap(
				dms, DMSHelper.STUCK_ON
			);
			stuck_on_pnl.setGraphic(stuck_on);
			int n_bad = 0;
			if (stuck_off != null)
				n_bad += stuck_off.getLitCount();
			if (stuck_on != null)
				n_bad += stuck_on.getLitCount();
			if (stuck_off != null || stuck_on != null) {
				bad_pixels_lbl.setText(String.valueOf(n_bad));
				return;
			}
		}
		catch (InvalidMsgException e) {
			// fall thru
		}
		stuck_off_pnl.setGraphic(null);
		stuck_on_pnl.setGraphic(null);
		bad_pixels_lbl.setText(UNKNOWN);
	}

	/** Update the dimensions of a sign pixel panel */
	private void updatePixelPanel(SignPixelPanel p) {
		p.setDimensions(dms.getSignConfig());
		p.repaint();
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
