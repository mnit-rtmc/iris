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

package us.mn.state.dot.tms.client.notification;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import us.mn.state.dot.tms.PushNotification;
import us.mn.state.dot.tms.PushNotificationHelper;
import us.mn.state.dot.tms.client.ScreenPane;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Form for displaying push notifications indicating something requiring user
 * interaction. Note that none of these fields are editable.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class PushNotificationForm extends ProxyTableForm<PushNotification> {
	
	/** Button to address all notifications */
	private JButton addressAllBtn;
	
	/** Action to address all notifications */
	private final IAction addressAll =
			new IAction("notification.address_all") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			// show a dialog asking for confirmation
			int ret = JOptionPane.showConfirmDialog(
					Session.getCurrent().getDesktop(),
					I18N.get("notification.address_all_msg"),
					I18N.get("notification.address_all_title"),
					JOptionPane.YES_NO_OPTION);
			System.out.println("Got: " + ret);
			if (ret == 0)
				PushNotificationHelper.addressAll(Session.getCurrent());
		}
		
	};
	
	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(PushNotification.SONAR_TYPE);
	}
	
	/** Create a new Push Notification form */
	public PushNotificationForm(Session s, MapBean map, ScreenPane p) {
		super(I18N.get("notification"), new PushNotificationProxyPanel(
				new PushNotificationModel(s), map, p));
		((PushNotificationProxyPanel) panel).setForm(this);
		addressAllBtn = new JButton(addressAll);
	}
	
	/** Initialize the form */
	@Override
	public void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		super.initialize();
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.add(addressAllBtn);
		add(p);
	}
}
