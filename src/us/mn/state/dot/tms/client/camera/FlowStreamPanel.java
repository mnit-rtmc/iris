/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.FlowStream;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for displaying a table of flow streams.
 *
 * @author Douglas Lau
 */
public class FlowStreamPanel extends ProxyTablePanel<FlowStream> {

	/** Send settings action */
	private final IAction settings = new IAction("device.send.settings") {
		protected void doActionPerformed(ActionEvent e) {
			FlowStream fs = getSelectedProxy();
			if (fs != null) {
				fs.setDeviceRequest(DeviceRequest.
					SEND_SETTINGS.ordinal());
			}
		}
	};

	/** User session */
	private final Session session;

	/** Create a new flow stream panel */
	public FlowStreamPanel(Session s) {
		super(new FlowStreamModel(s));
		session = s;
	}

	/** Initialise the panel */
	@Override
	public void initialize() {
		super.initialize();
		settings.setEnabled(false);
	}

	/** Add create/delete widgets to the button panel */
	@Override
	protected void addCreateDeleteWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		JButton b = new JButton(settings);
		hg.addComponent(b);
		vg.addComponent(b);
		hg.addGap(UI.hgap);
		super.addCreateDeleteWidgets(hg, vg);
	}

	/** Update the button panel */
	@Override
	public void updateButtonPanel() {
		FlowStream fs = getSelectedProxy();
		settings.setEnabled(canRequest(fs));
		super.updateButtonPanel();
	}

	/** Check if the user can make device requests */
	private boolean canRequest(FlowStream fs) {
		return session.isWritePermitted(fs, "deviceRequest");
	}
}
