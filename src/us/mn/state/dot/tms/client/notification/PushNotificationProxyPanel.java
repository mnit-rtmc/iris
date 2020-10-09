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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.PushNotification;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.ScreenPane;
import us.mn.state.dot.tms.client.map.LayerState;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyLayerState;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;

/**
 * Panel for displaying push notifications indicating something requiring user
 * interaction. Note that none of these fields are editable.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class PushNotificationProxyPanel
				extends ProxyTablePanel<PushNotification> {
	
	/** Handle to the form */
	private PushNotificationForm form;
	
	/** Handle to MapBean */
	private final MapBean map;

	/** Handle to screen pane */
	private final ScreenPane pane;
	
	/** MouseAdapter for triggering events when clicking notifications */
	private final MouseAdapter mouser = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2)
				goToRefObject();
		}
	};
	
	public PushNotificationProxyPanel(ProxyTableModel<PushNotification> m,
			MapBean map, ScreenPane p) {
		super(m);
		this.map = map;
		pane = p;
	}
	
	public void setForm(PushNotificationForm f) {
		form = f;
	}
	
	/** Create GUI jobs. */
	@Override
	protected void createJobs() {
		super.createJobs();
		
		// attach the MouseAdapter to the panel
		table.addMouseListener(mouser);
	}
	
	/** Action triggered by double clicking (shortcut to reference object,
	 *  wherever that is).
	 */
	@SuppressWarnings("unchecked")
	private void goToRefObject() {
		// get the selected notification to get the reference type/object
		PushNotification pn = getSelectedProxy();
		
		// don't do anything if already addressed (to avoid resetting that)
		if (pn == null || pn.getAddressedBy() != null)
			return;
		
		Session session = Session.getCurrent();
		
		// record that the notification has been addressed
		pn.setAddressedTime(new Date());
		pn.setAddressedBy(session.getUser().getName());
		
		// run a background job to check if the button should stop blinking
		session.getPushNotificationManager().checkStopBlinkBG();
		
		String refType = pn.getRefObjectType();
		String refName = pn.getRefObjectName();
		if (refType == null)
			return;
		System.out.println("Trying to select " + refType + "/" + refName);
		
		// try to get a tab for this type
		MapTab<SonarObject> typeTab = null;
		for (MapTab<SonarObject> tab: session.getTabs()) {
			if (refType.equals(tab.getManager().getSonarType())) {
				// if we get one, select it
				typeTab = tab;
				pane.setSelectedTab(tab);
				break;
			}
		}
		
		if (refName == null)
			return;
		
		// try to get the layer for this type
		LayerState ls = map.getTypeLayer(refType);
		if (ls instanceof ProxyLayerState) {
			// if we get one, get the cache and lookup the object
			ProxyLayerState<SonarObject> pls =
					(ProxyLayerState<SonarObject>) ls;
			
			ProxyManager<SonarObject> man = pls.getManager();
			TypeCache<SonarObject> cache = man.getCache();
			SonarObject obj = cache.lookupObject(refName);
			if (obj != null) {
				// if we get an object, use the GeoLoc to select it
				MapGeoLoc loc = man.findGeoLoc(obj);
				if (loc != null) {
					MapGeoLoc[] s = {loc};
					pls.setSelections(s);
				}
				
				// also try to select the object in the tab (if we got one)
				// TODO I think this should work in a different way, but this
				// works
				if (typeTab != null)
					typeTab.setSelectedProxy(obj);
			}
		}
		
		// close the notification panel
		if (form != null)
			session.getDesktop().closeForm(form);
	}
	
}


















