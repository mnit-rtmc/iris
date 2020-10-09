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
package us.mn.state.dot.tms.client.alert;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import us.mn.state.dot.tms.IpawsAlertDeployer;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.MapTab;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * The AlertTab class provides the GUI for working with automated alert
 * objects, e.g. weather (and other) alerts from IPAWS.
 * 
 * NOTE this would need changing to let the alert tab handle other types of
 * alerts (we would need a new Alert parent SONAR object - not sure what that
 * would look like yet). 
 *
 * @author Gordon Parikh
 */

@SuppressWarnings("serial")
public class AlertTab extends MapTab<IpawsAlertDeployer> {
	
	/** Summary of alerts */
	private final StyleSummary<IpawsAlertDeployer> summary;
	
	/** Alert dispatcher for dispatching and reviewing alerts */
	private final AlertDispatcher dispatcher;
	
	/** JScrollPane that contains all the elements of the tab (since it can
	 *  get big).
	 */
	private JScrollPane sp;
	
	/** Component adapter for handling resize events. */
	private ComponentAdapter resizeHandler = new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent e) {
			Dimension d = sp.getPreferredSize();
			d.height = side_pnl.getSize().height - 80;
			sp.setPreferredSize(d);
		}
	};
	
	protected AlertTab(Session session, AlertManager man) {
		super(man);
		summary = man.createStyleSummary(false, 2);
		dispatcher = new AlertDispatcher(session, man);
		addComponentListener(resizeHandler);
	}
	
	/** Initialize the alert tab. */
	@Override
	public void initialize() {
		summary.initialize();
		dispatcher.initialize();
		JPanel p = new JPanel(new BorderLayout());
		p.add(summary, BorderLayout.NORTH);
		p.add(dispatcher, BorderLayout.CENTER);
		sp = new JScrollPane(p, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(sp, BorderLayout.NORTH);
	}
	
	@Override
	public void postInit() {
		Dimension d = sp.getPreferredSize();
		d.height = side_pnl.getSize().height - 80;
		sp.setPreferredSize(d);
	}
	
	/** Dispose of the alert tab. */
	@Override
	public void dispose() {
		super.dispose();
		summary.dispose();
	}
	
	/** Get the alert tab ID. Overridden to generalize name. */
	@Override
	public String getTabId() {
		return getAlertTabId();
	}
	
	public static String getAlertTabId() {
		return "alert";
	}
	
	/** Select an alert in the tab */
	@Override
	public void setSelectedProxy(IpawsAlertDeployer proxy) {
		// check the style of the alert and select the appropriate one
		ItemStyle style = manager.getItemStyle(proxy);
		if (style != null)
			summary.setStyle(style);
		
		dispatcher.selectAlert(proxy);
		summary.ensureSelectedProxyVisible();
	}
	
	/** Update the counts in the style summary */
	public void updateStyleCounts() {
		System.out.println("Updating style counts");
		
		// use a timer to fire this in just a bit
		Timer t = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				summary.updateCounts();
			}
		});
		t.start();
	}
	
	/** Get the AlertDispatcher */
	public AlertDispatcher getAlertDispatcher() {
		return dispatcher;
	}

	/** Get the AlertDmsDispatcher */
	public AlertDmsDispatcher getDmsDispatcher() {
		return dispatcher.getDmsDispatcher();
	}
}
