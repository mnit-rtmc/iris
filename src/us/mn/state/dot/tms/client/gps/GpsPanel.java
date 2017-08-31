/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
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

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.GpsHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.swing.AbstractAction;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * IPanel for configuring/polling a GPS (to be
 * included in a device's "Location" properties-tab).
 *
 * @author Michael Janson - SRF Consulting
 */

@SuppressWarnings("serial")
public class GpsPanel extends IPanel
		implements ProxyView<Gps> {
	private JCheckBox chckbxEnableGps =
			new JCheckBox(I18N.get("gps.use.modem"));
	private JTextField tolerance = new JTextField();
	private JTextField blank = new JTextField();
	private JButton btnQueryGps = new JButton(
			new QueryGpsAction(I18N.get("gps.query")));
	private JLabel lblLatestAttempt =
			new JLabel(I18N.get("gps.lastattempt"));
	private JLabel txtLatestAttempt =new JLabel();
	private JLabel lblGpsStatus = new JLabel();
	private JLabel txtGpsStatus = new JLabel();
	private JLabel lblLatestSuccess = 
			new JLabel(I18N.get("gps.lastsuccess"));
	private JLabel txtLatestSuccess = new JLabel();
	private SimpleDateFormat dtFormatter = 
			new SimpleDateFormat("yyyy/MM/dd HH:mm");

	/** User session */
	protected final Session session;

	/** Sonar state object */
	protected final SonarState state;
	
	/** Device being tracked by the GPS */
	protected final Device parentDevice;
	
	/** Associated GPS object */
	protected Gps gps;
	
	/** Proxy watcher */
	private final ProxyWatcher<Gps> watcher;

	/**
	 * Create the panel.
	 */
	public GpsPanel(Session s, Device pd) {
		session = s;
		parentDevice = pd;
		state = s.getSonarState();
		TypeCache<Gps> cache = state.getGpses();
		watcher = new ProxyWatcher<Gps>(cache, this, false);
		final String gpsName = parentDevice.getName() + "_gps";

		// Lookup GPS based on parentDevice name
		gps = GpsHelper.lookup(gpsName);

		//--- Setup panel
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		Border border = new LineBorder(Color.GRAY, 2);
		Border margin = new EmptyBorder(10,10,10,10);
		setBorder(new CompoundBorder(border, margin));

		//--- tweak various controls

		// lock width of tolerance editbox and add listener
		tolerance.setColumns(4);
		tolerance.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				gps.setJitterToleranceMeters(
					Integer.parseInt(tolerance.getText()));
			}
		});

		// configure invisible editbox for spacing
		// and panel-width control
		Color panelColor = new Color(238,238,238);//getBackground();
		blank.setColumns(20);
		blank.setBorder(new LineBorder(panelColor));
		blank.setBackground(panelColor);
		blank.setEnabled(false);

		// add action to [Query GPS] button
		chckbxEnableGps.setAction(new IAction("gps.use.modem") {
			protected void doActionPerformed(ActionEvent e){
				if (gps == null && chckbxEnableGps.isSelected()) {				
					session.getSonarState().getGpses().createObject(
							gpsName);
					gps = GpsHelper.lookup(gpsName);
				}
				if (gps != null)
					gps.setGpsEnable(chckbxEnableGps.isSelected());
				updateEditMode();
			}
		});

		//--- add controls to panel
		add(chckbxEnableGps, Stretch.LEFT);
		add("gps.tolerance");
		add(tolerance, Stretch.WIDE);
		add("gps.units", Stretch.LEFT);
		add(blank, Stretch.END);
		add(btnQueryGps, Stretch.LEFT);
		add(lblLatestAttempt, Stretch.NONE);
		add(txtLatestAttempt, Stretch.LEFT);
		add(lblGpsStatus, Stretch.NONE);
		add(txtGpsStatus, Stretch.LEFT);
		add(lblLatestSuccess, Stretch.NONE);
		add(txtLatestSuccess, Stretch.LEFT);

		watcher.initialize();
		setGps(gps);
		updateGpsPanel();
	}

	/** Query GPS action */
	class QueryGpsAction extends AbstractAction {
		public QueryGpsAction(String name) {
			super(name);
		}

		@SuppressWarnings("incomplete-switch")
		public void actionPerformed(ActionEvent e) {
			switch (gpsPanelMode) {
				case GPS_ENABLED:
				case GPS_DISABLED: // <- allow manual query
					gps.setDeviceRequest(DeviceRequest
							.QUERY_GPS_LOCATION_FORCE.ordinal());
			}
		}
	}

	public void updateEditMode() {
		boolean bCanUpdateGps = canWrite("gps");
		boolean bGpsDeviceEnabled = (gps != null)
	                       && chckbxEnableGps.isSelected();
		chckbxEnableGps.setEnabled(bCanUpdateGps);
		if (!bGpsDeviceEnabled) {
			tolerance.setEnabled(false);
		} else {
			tolerance.setEnabled(bCanUpdateGps);
		}
		btnQueryGps.setEnabled(
				gpsPanelMode != GpsPanelMode.DISABLED);
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		return session.canWrite(parentDevice, aname);
	}

	static enum GpsPanelMode {
		DISABLED,
		GPS_DISABLED,
		GPS_ENABLED;
	}

	GpsPanelMode gpsPanelMode = GpsPanelMode.DISABLED;

	private void updatePanelMode() {
		if (gps != null) {
			if (gps.getGpsEnable())
				gpsPanelMode = GpsPanelMode.GPS_ENABLED;
			else
				gpsPanelMode = GpsPanelMode.GPS_DISABLED;
		}
		else
			gpsPanelMode = GpsPanelMode.DISABLED;
	}	
	
	@SuppressWarnings("incomplete-switch")
	public void updateGpsPanel() {
		String str;
		String sJitter = "";
		
		updatePanelMode();

		switch (gpsPanelMode) {
			case GPS_DISABLED:
			case GPS_ENABLED:
				sJitter = ""+gps.getJitterToleranceMeters();
				break;
		}
		tolerance.setText(sJitter);

		chckbxEnableGps.setSelected(
				gpsPanelMode == GpsPanelMode.GPS_ENABLED);
		switch (gpsPanelMode) {
			case DISABLED:
				txtLatestAttempt.setText("");
				lblGpsStatus.setText(I18N.get("gps.status"));
				txtGpsStatus.setText("");
				txtLatestSuccess.setText("");
				break;
			case GPS_DISABLED:
			case GPS_ENABLED:
				Long lPoll = gps.getPollDatetime();
				if ((lPoll == null) || (lPoll == 0))
					txtLatestAttempt.setText("");
				else {
					Timestamp tsPoll = new Timestamp(lPoll);
					txtLatestAttempt.setText(dtFormatter.format(tsPoll));
				}

				// Status field shows gps-comm-status if there's no gps-error
				str = gps.getErrorStatus();
				if ((str != null) && !str.isEmpty()) {
					lblGpsStatus.setText(I18N.get("gps.error"));
					txtGpsStatus.setText(str);
				} else {
					lblGpsStatus.setText(I18N.get("gps.status"));
					str = gps.getCommStatus();
					if ((str != null) && !str.isEmpty())
						txtGpsStatus.setText(str);
					else
						txtGpsStatus.setText("");
				}

				Long lSample = gps.getSampleDatetime();
				if ((lSample == null) || (lSample == 0)) {
					txtLatestSuccess.setText("");
				} else {
					Timestamp tsSample = new Timestamp(lSample);
					txtLatestSuccess.setText(dtFormatter.format(tsSample));
				}
		}
	}

	public void update(Gps gps, String a) {
		updateGpsPanel();
	}

	/** Set the GPS object */
	public void setGps(Gps g) {
		watcher.setProxy(g);

		// Hide GPS polling-status fields
		// if there's no GPS object.
		boolean visible = (g != null);
		lblLatestAttempt.setVisible(visible);
		txtLatestAttempt.setVisible(visible);
		lblGpsStatus.setVisible(visible);
		txtGpsStatus.setVisible(visible);
		lblLatestSuccess.setVisible(visible);
		txtLatestSuccess.setVisible(visible);
	}

	@Override
	public void clear() {
		chckbxEnableGps.setSelected(false);
		tolerance.setText("");
		txtLatestAttempt.setText("");
		lblGpsStatus.setText(I18N.get("gps.status"));
		txtGpsStatus.setText("");
		txtLatestSuccess.setText("");		
	}
	
	/** Dispose of the GPS panel */
	@Override
	public void dispose() {
		watcher.dispose();
		super.dispose();
	}
}
