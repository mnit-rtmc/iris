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
package us.mn.state.dot.tms.client.notification;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.PushNotification;
import us.mn.state.dot.tms.PushNotificationHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
import us.mn.state.dot.tms.client.toolbar.PushNotificationPanel;
import us.mn.state.dot.tms.client.widget.IWorker;

/**
 * A container for SONAR PushNotification objects used for alerting users of
 * things that need their attention.
 * 
 * @author Gordon Parikh
 */
public class PushNotificationManager extends ProxyManager<PushNotification> {

	/** Current session */
	private Session session;
	
	/** PushNotification cache */
	private final TypeCache<PushNotification> cache;
	
	/** Handle to PushNotificationPanel */
	private PushNotificationPanel notifPnl;
	
	/** Proxy listener for SONAR updates. */
	private final SwingProxyAdapter<PushNotification> listener = 
			new SwingProxyAdapter<PushNotification>(true) {

		/** Track when enumeration is completed so we don't bombard users with
		 *  notification indications.
		 */
		private boolean enumComplete = false;
		@Override protected void enumerationCompleteSwing(
				Collection<PushNotification> pns) { enumComplete = true; }
		
		/** Triggered when a new PushNotification is received from the server.
		 *  If the user can read or write the type of object referenced in
		 *  this notification (depending on whether needs_write is true), this
		 *  will start blinking the Notification button in the tool panel
		 *  until it is clicked or someone else addresses the notification.
		 */
		@Override
		protected void proxyAddedSwing(PushNotification pn) {
			if (pn != null && notifPnl != null) {
				if (PushNotificationHelper.check(session, pn, false))
					notifPnl.startButtonBlink();
			}
		}
		
		/** Triggered when an attribute of a PushNotification is changed. If
		 *  the addressed_time attribute is changed, this checks notifications
		 *  that the user can see to determine if the button should stop
		 *  blinking
		 */
		@Override
		protected void proxyChangedSwing(PushNotification pn, String attr) {
			checkStopBlinkBG();
		}
	};
	
	/** Check if the notification button should stop blinking, stopping the
	 *  blinking if it should.
	 */
	private void checkStopBlink() {
		// check all notifications the user can see
		Iterator<PushNotification> it =
				PushNotificationHelper.iterator();
		boolean stopBlink = true;
		while (it.hasNext()) {
			PushNotification n = it.next();
			
			// FIXME not sure why these need to be called twice for this code
			// to work
			PushNotificationHelper.checkPrivileges(session, n);
			PushNotificationHelper.checkAddressed(n, false);
			if (PushNotificationHelper.checkPrivileges(session, n) &&
					PushNotificationHelper.checkAddressed(n, false))
				stopBlink = false;
		}
		if (stopBlink)
			notifPnl.stopButtonBlink();
	}
	
	/** Run a background job to check if the notification button should stop
	 *  blinking.
	 */
	public void checkStopBlinkBG() {
		IWorker<Void> blinkWorker = new IWorker<Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				// wait for a second before checking
				Thread.sleep(1000);
				checkStopBlink();
				return null;
			}
		};
		blinkWorker.execute();
	}
	
	/** Create a proxy descriptor */
	static private ProxyDescriptor<PushNotification> descriptor(Session s) {
		return new ProxyDescriptor<PushNotification>(
				s.getSonarState().getPushNotificationCache(), false);
	}
	
	/** Create the PushNotificationManager */
	public PushNotificationManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 0);
		session = s;
		cache = s.getSonarState().getPushNotificationCache();
		cache.addProxyListener(listener);
	}
	
	/** Give the notification manager the handle to the PushNotificationPanel
	 *  to allow it to manipulate the panel.
	 */
	public void setToolPanel(PushNotificationPanel pnp) {
		notifPnl = pnp;
	}
	
	/** PushNotifications have no theme. */
	@Override
	protected ProxyTheme<PushNotification> createTheme() { return null; }

	/** PushNotifications have no GeoLoc. */
	@Override
	protected GeoLoc getGeoLoc(PushNotification proxy) { return null; }
	
}
