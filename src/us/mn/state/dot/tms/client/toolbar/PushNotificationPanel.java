/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
 
package us.mn.state.dot.tms.client.toolbar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.Timer;

import us.mn.state.dot.tms.client.ScreenPane;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.notification.PushNotificationForm;
import us.mn.state.dot.tms.client.notification.PushNotificationManager;
import us.mn.state.dot.tms.client.widget.IAction;

/**
 * A tool panel that opens a push notification manager.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class PushNotificationPanel extends ToolPanel {
	
	/** Is this panel IRIS enabled? */
	static public boolean getIEnabled() {
		return true;
	}

	/** User session */
	private final Session session;
	
	/** Handle to map */
	private final MapBean map;
	
	/** Handle to screen pane */
	private final ScreenPane pane;
	
	/** Push notification manager */
	private final PushNotificationManager manager;
	
	/** Button to open notification GUI */
	private final JButton notificationBtn;
	
	/** Action to open notification GUI */
	private final IAction openNotifGui = new IAction("notification") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			session.getDesktop().show(
					new PushNotificationForm(session, map, pane));
		}
	};
	
	public PushNotificationPanel(Session s, MapBean m, ScreenPane p) {
		session = s;
		map = m;
		pane = p;
		notificationBtn = new JButton(openNotifGui);
		add(notificationBtn);
		manager = session.getPushNotificationManager();
		manager.setToolPanel(this);
	}
	
	/** Blinking state (blinking or not) */
	private boolean blinking;
	
	/** Current button blink state (on or off) */
	private boolean bbOn;
	
	/** Color to show blink */
	private final static Color blinkColor = Color.YELLOW;
	
	/** Timer for blinking the notification button when a new notification is
	 *  received.
	 */
	private Timer blinkTimer = new Timer(2000, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			// check if we should be blinking the button
			if (!blinking) {
				// if no, set blink color off and stop timer
				notificationBtn.setBackground(null);
				((Timer) e.getSource()).stop();
			} else
				// if yes, alternate blink color on and off
				blinkButton();
		}
		
	});
	
	/** Blink the button by toggling the color */
	private void blinkButton() {
		notificationBtn.setBackground(bbOn ? blinkColor : null);
		bbOn = !bbOn;
	}
	
	/** Start blinking the notification button (to indicate there is a button
	 *  that requires the user's attention).
	 */
	public void startButtonBlink() {
		// fire immediately, then start the timer to toggle every 2 seconds
		blinking = true;
		bbOn = true;
		blinkButton();
		blinkTimer.restart();
	}
	
	/** Stop blinking the notification button (either when clicked, or when
	 *  another client addresses the alert).
	 */
	public void stopButtonBlink() {
		// disable blinking, stop the timer and, clear the button color
		blinking = false;
		blinkTimer.stop();
		notificationBtn.setBackground(null);
	}

}