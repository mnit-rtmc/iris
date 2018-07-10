/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
 * Copyright (C) 2018  Minnesota Department of Transportation
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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import us.mn.state.dot.sonar.client.TypeCache;
import static us.mn.state.dot.tms.DeviceRequest.QUERY_GPS_LOCATION;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * IPanel for configuring/polling a GPS (to be
 * included in a device's "Location" properties-tab).
 *
 * @author Michael Janson - SRF Consulting
 * @author Douglas Lau
 */
public class GpsPanel extends IPanel implements ProxyView<Gps> {

	/** Suffix for GPS names */
	static private final String NAME_SUFFIX = "_gps";

	/** Maximum length of GPS names */
	static private final int MAX_NAME_LEN = 20 - NAME_SUFFIX.length();

	/** Device request to query GPS location */
	static private final int QUERY_LOC = QUERY_GPS_LOCATION.ordinal();

	/** Checkbox to enable GPS for a device */
	private final JCheckBox enable_cbx = new JCheckBox();

	/** Button to query GPS */
	private final JButton query_btn = new JButton();

	/** Operation label */
	private final JLabel op_lbl = new JLabel();

	/** Label for latest poll */
	private final JLabel poll_lbl = new JLabel();

	/** Label for latest sample */
	private final JLabel sample_lbl = new JLabel();

	/** Date/time formatter */
	private final SimpleDateFormat dt_format =
			new SimpleDateFormat("yyyy/MM/dd HH:mm");

	/** User session */
	private final Session session;

	/** Cache of GPS objects */
	private final TypeCache<Gps> cache;

	/** DMS being tracked by the GPS */
	private final DMS dms;

	/** Proxy watcher */
	private final ProxyWatcher<Gps> watcher;

	/** Associated GPS object */
	private Gps gps;

	/** Create the panel */
	public GpsPanel(Session s, DMS d) {
		setBorder(UI.buttonBorder());
		session = s;
		dms = d;
		cache = s.getSonarState().getGpses();
		watcher = new ProxyWatcher<Gps>(cache, this, false);

		add(enable_cbx);
		add(query_btn, Stretch.END);
		add("device.operation");
		add(op_lbl, Stretch.LAST);
		add("gps.latest.poll");
		add(poll_lbl, Stretch.LAST);
		add("gps.latest.sample");
		add(sample_lbl, Stretch.LAST);

		enable_cbx.setAction(new IAction("gps.enable") {
			protected void doActionPerformed(ActionEvent e) {
				selectGpsEnabled();
			}
		});
		query_btn.setAction(new IAction("gps.query") {
			protected void doActionPerformed(ActionEvent e) {
				Gps g = gps;
				if (g != null)
					g.setDeviceRequest(QUERY_LOC);
			}
		});

		watcher.initialize();
		setGps(dms.getGps());
		updateGpsPanel();
	}

	/** GPS enabled checkbox action */
	private void selectGpsEnabled() {
		if (enable_cbx.isSelected()) {
			if (null == gps)
				createGps();
		} else if (gps != null)
			destroyGps();
		updateEditMode();
	}

	/** Create a GPS device */
	private void createGps() {
		String name = getGpsName();
		cache.createObject(name);
		Gps g = cache.lookupObjectWait(name);
		dms.setGps(g);
		setGps(g);
	}

	/** Get the GPS name */
	private String getGpsName() {
		return getDmsNameTruncated() + NAME_SUFFIX;
	}

	/** Get the DMS name truncated */
	private String getDmsNameTruncated() {
		String n = dms.getName();
		return (n.length() <= MAX_NAME_LEN)
		      ? n
		      : n.substring(0, MAX_NAME_LEN);
	}

	/** Destroy a GPS device */
	private void destroyGps() {
		Gps g = gps;
		dms.setGps(null);
		g.setController(null);
		g.destroy();
	}

	/** Update the edit mode */
	public void updateEditMode() {
		boolean allowed = canUpdateGps();
		boolean e = (gps != null);
		enable_cbx.setEnabled(allowed);
		query_btn.setEnabled(allowed && e);
	}

	/** Check if GPS can be updated */
	private boolean canUpdateGps() {
		return session.canWrite("gps")
		    && session.canWrite(dms, "gps");
	}

	/** Update the panel */
	public void updateGpsPanel() {
		Gps g = gps;
		boolean e = (g != null);
		enable_cbx.setSelected(e);
		if (g != null) {
			op_lbl.setText(g.getOperation());
			poll_lbl.setText(formatStamp(g.getLatestPoll()));
			sample_lbl.setText(formatStamp(g.getLatestSample()));
		} else {
			op_lbl.setText("");
			poll_lbl.setText("");
			sample_lbl.setText("");
		}
	}

	/** Format a date/time stamp */
	private String formatStamp(Long ts) {
		return (ts != null) ? dt_format.format(new Date(ts)) : "";
	}

	/** Called when all proxies have been enumerated (from ProxyView). */
	@Override
	public void enumerationComplete() { }

	@Override
	public void update(Gps g, String a) {
		gps = g;
		updateGpsPanel();
	}

	/** Set the GPS object */
	public void setGps(Gps g) {
		watcher.setProxy(g);
	}

	@Override
	public void clear() {
		gps = null;
		enable_cbx.setSelected(false);
		enable_cbx.setEnabled(false);
		query_btn.setEnabled(false);
		op_lbl.setText("");
		poll_lbl.setText("");
		sample_lbl.setText("");
	}

	/** Dispose of the GPS panel */
	@Override
	public void dispose() {
		watcher.dispose();
		super.dispose();
	}
}
