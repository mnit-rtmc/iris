/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gps;

import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JLabel;
import us.mn.state.dot.sonar.client.TypeCache;
import static us.mn.state.dot.tms.DeviceRequest.QUERY_GPS_LOCATION;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.GpsHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * IPanel for polling a GPS status
 * (to be included in a device's "Location" properties-tab).
 *
 * @author Michael Janson - SRF Consulting
 * @author Douglas Lau
 */
public class DeviceGpsPanel extends IPanel {

	/** Device request to query GPS location */
	static private final int QUERY_LOC = QUERY_GPS_LOCATION.ordinal();

	/** Get a double to use for a text field */
	static private String asText(Double d) {
		return (d != null) ? d.toString() : "";
	}

	/** Button to query GPS */
	private final JButton query_btn = new JButton();

	/** Operation label */
	private final JLabel op_lbl = new JLabel();

	/** Label for latest poll */
	private final JLabel poll_lbl = new JLabel();

	/** Label for latest sample */
	private final JLabel sample_lbl = new JLabel();

	/** Latitude label */
	private final JLabel lat_lbl = new JLabel();

	/** Longitude label */
	private final JLabel lon_lbl = new JLabel();

	/** Date/time formatter */
	private final SimpleDateFormat dt_format =
		new SimpleDateFormat("yyyy-MM-dd HH:mm");

	/** User session */
	private final Session session;

	/** Cache of GPS objects */
	private final TypeCache<Gps> cache;

	/** Associated GPS object */
	private final Gps gps;

	/** Proxy watcher */
	private final ProxyWatcher<Gps> watcher;

	/** Proxy view */
	private final ProxyView<Gps> view = new ProxyView<Gps>() {
		public void enumerationComplete() { }
		public void update(Gps g, String a) {
			updateGps(a);
		}
		public void clear() {
			clearGps();
		}
	};

	/** Create the panel */
	public DeviceGpsPanel(Session s, GeoLoc loc) {
		session = s;
		gps = GpsHelper.findLoc(loc);
		cache = s.getSonarState().getGpses();
		watcher = new ProxyWatcher<Gps>(cache, view, false);
		add(query_btn, Stretch.END);
		add("device.operation");
		add(op_lbl, Stretch.LAST);
		add("gps.latest.poll");
		add(poll_lbl, Stretch.LAST);
		add("gps.latest.sample");
		add(sample_lbl, Stretch.LAST);
		add("location.latitude");
		add(lat_lbl, Stretch.LAST);
		add("location.longitude");
		add(lon_lbl, Stretch.LAST);
		query_btn.setAction(new IAction("gps.query") {
			protected void doActionPerformed(ActionEvent e) {
				queryGps();
			}
		});
	}

	/** Initialize the stuff */
	@Override
	public void initialize() {
		setBorder(UI.buttonBorder());
		watcher.initialize();
		watcher.setProxy(gps);
		query_btn.setEnabled(canRequestGps());
	}

	/** Force GPS to be queried */
	private void queryGps() {
		if (gps != null)
			gps.setDeviceRequest(QUERY_LOC);
	}

	/** Format a date/time stamp */
	private String formatStamp(Long ts) {
		return (ts != null) ? dt_format.format(new Date(ts)) : "";
	}

	/** Update the GPS */
	private void updateGps(String a) {
		query_btn.setEnabled(canRequestGps());
		if (a == null || a.equals("operation"))
			op_lbl.setText(gps.getOperation());
		if (a == null || a.equals("latestPoll"))
			poll_lbl.setText(formatStamp(gps.getLatestPoll()));
		if (a == null || a.equals("latestSample"))
			sample_lbl.setText(formatStamp(gps.getLatestSample()));
		if (a == null || a.equals("lat"))
			lat_lbl.setText(asText(gps.getLat()));
		if (a == null || a.equals("lon"))
			lon_lbl.setText(asText(gps.getLon()));
	}

	/** Check if GPS can be requested */
	private boolean canRequestGps() {
		return (gps != null) && session.isWritePermitted(gps);
	}

	/** Clear the selected GPS */
	private void clearGps() {
		query_btn.setEnabled(false);
		op_lbl.setText("");
		poll_lbl.setText("");
		sample_lbl.setText("");
		lat_lbl.setText("");
		lon_lbl.setText("");
	}

	/** Dispose of the GPS panel */
	@Override
	public void dispose() {
		watcher.dispose();
		super.dispose();
	}
}
