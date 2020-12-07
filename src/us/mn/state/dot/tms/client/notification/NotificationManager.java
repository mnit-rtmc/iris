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

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Notification;
import us.mn.state.dot.tms.NotificationHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
import us.mn.state.dot.tms.client.toolbar.NotificationPanel;
import us.mn.state.dot.tms.client.widget.IWorker;

/**
 * A container for SONAR Notification objects used for alerting users of
 * things that need their attention.
 *
 * @author Gordon Parikh
 */
public class NotificationManager extends ProxyManager<Notification> {

	/** Current session */
	private Session session;

	/** Notification cache */
	private final TypeCache<Notification> cache;

	/** Handle to NotificationPanel */
	private NotificationPanel notifPnl;

	/** Proxy listener for SONAR updates. */
	private final SwingProxyAdapter<Notification> listener =
			new SwingProxyAdapter<Notification>(true) {

		/** Track when enumeration is completed so we don't bombard users with
		 *  notification indications.
		 */
		private boolean enumComplete = false;
		@Override protected void enumerationCompleteSwing(
				Collection<Notification> pns) { enumComplete = true; }

		/** Triggered when a new Notification is received from the server.
		 *  If the user can read or write the type of object referenced in
		 *  this notification (depending on whether needs_write is true), this
		 *  will start blinking the Notification button in the tool panel
		 *  until it is clicked or someone else addresses the notification.
		 */
		@Override
		protected void proxyAddedSwing(Notification pn) {
			if (pn != null && notifPnl != null) {
				if (NotificationHelper.check(session, pn, false))
					notifPnl.startButtonBlink();
			}
		}

		/** Triggered when an attribute of a Notification is changed. If
		 *  the addressed_time attribute is changed, this checks notifications
		 *  that the user can see to determine if the button should stop
		 *  blinking
		 */
		@Override
		protected void proxyChangedSwing(Notification pn, String attr) {
			checkStopBlinkBG();
		}
	};

	/** Check if the notification button should stop blinking, stopping the
	 *  blinking if it should.
	 */
	private void checkStopBlink() {
		// check all notifications the user can see
		Iterator<Notification> it = NotificationHelper.iterator();
		boolean stopBlink = true;
		while (it.hasNext()) {
			Notification n = it.next();

			// FIXME not sure why these need to be called twice for
			//       this code to work
			NotificationHelper.checkPrivileges(session, n);
			NotificationHelper.checkAddressed(n, false);
			if (NotificationHelper.checkPrivileges(session, n) &&
				NotificationHelper.checkAddressed(n, false))
			{
				stopBlink = false;
			}
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
	static private ProxyDescriptor<Notification> descriptor(Session s) {
		return new ProxyDescriptor<Notification>(
				s.getSonarState().getNotificationCache(), false);
	}

	/** Create the NotificationManager */
	public NotificationManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 0);
		session = s;
		cache = s.getSonarState().getNotificationCache();
		cache.addProxyListener(listener);
	}

	/** Give the notification manager the handle to the NotificationPanel
	 *  to allow it to manipulate the panel.
	 */
	public void setToolPanel(NotificationPanel pnp) {
		notifPnl = pnp;
	}

	/** Notifications have no theme. */
	@Override
	protected ProxyTheme<Notification> createTheme() { return null; }

	/** Notifications have no GeoLoc. */
	@Override
	protected GeoLoc getGeoLoc(Notification proxy) { return null; }
}
