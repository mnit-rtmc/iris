/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JTextArea;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.DialogHandler;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.units.Distance.Units;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PropRwis is a DMS properties panel for displaying
 * and editing DMS-specific RWIS configuration info
 * (mainly weather_sensor_override) on a DMS properties
 * form.
 *
 * @author John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")
public class PropRwis extends IPanel implements ProxyView<GeoLoc> {

	/** RWIS enabled label */
	private final JLabel rwisSystem_lbl = new JLabel();

	/** RWIS hashtags label */
	private final JLabel hashtagsFound_lbl = new JLabel();

	/** WeatherSensors associated with DMS label */
	private final JLabel wsFound_lbl = new JLabel();

	/** Closest WeatherSensor label */
	private final JLabel closest_lbl = new JLabel();
	
	/** Distance label */
	private final JLabel distance_lbl = new JLabel();

	/** WeatherSensorOverride text area */
	private final JTextArea weatherSensorOverride_txt = new JTextArea(3, 25);
//	private final JTextArea weatherSensorOverride_txt = new JTextArea(2, 37);

	/** Warning */
	private final JLabel warn_lbl = new JLabel(" ");

	/** User session */
	private final Session session;

	/** DMS to display */
	private final DMS dms;

	/** Proxy watcher */
	private final ProxyWatcher<GeoLoc> watcher;

	/** Create a new DMS properties setup panel */
	public PropRwis(Session s, DMS sign) {
		dms = sign;
		session = s;
		SonarState state = session.getSonarState();
		TypeCache<GeoLoc> cache = state.getGeoLocs();
		watcher = new ProxyWatcher<GeoLoc>(cache, this, false);
	}

	/** Initialize the widgets on the form */
	@Override
	public void initialize() {
		super.initialize();
		weatherSensorOverride_txt.addFocusListener(new FocusAdapter() {
			@Override public void focusLost(FocusEvent e) {
				String wso = weatherSensorOverride_txt.getText();
				DialogHandler handler = new DialogHandler();
				ArrayList<WeatherSensor> wsList;
				try {
					wsList = DMSHelper.parseWeatherSensorList(wso);
				} catch (TMSException e2) {
					handler.handle(e2);
					return;
				}
				if (wsList.isEmpty())
					wso = "";
				dms.setWeatherSensorOverride(wso);
				updateGui();
			}
		});
		add("dms.rwis.system");
		add(rwisSystem_lbl, Stretch.LAST);
		add("dms.rwis.hashtags");
		add(hashtagsFound_lbl, Stretch.LAST);
		add("dms.rwis.sensors");
		add(wsFound_lbl, Stretch.LAST);
		add(new JLabel(" "), Stretch.CENTER);
//		add(new JLabel(" "), Stretch.CENTER);
		add("dms.rwis.closest");
		add(closest_lbl, Stretch.LAST);
		add("dms.rwis.distance");
		add(distance_lbl, Stretch.LAST);
		add(new JLabel(" "), Stretch.CENTER);

//		add(new JLabel(" "));
		add("dms.rwis.override");
//		add(new JLabel("<html><small><super>(Semicolon separated list of WeatherSensor names.)"), Stretch.LAST);
//		add("dms.rwis.override.tip", Stretch.LAST);

//		add("dms.rwis.override");
		add(weatherSensorOverride_txt, Stretch.LAST);
//		add(weatherSensorOverride_txt, Stretch.CENTER);

		add(new JLabel(" "), Stretch.CENTER);
		add(new JLabel(" "), Stretch.CENTER);
		add(warn_lbl, Stretch.CENTER);
		
		// keep an eye on the sign's location
		watcher.setProxy(dms.getGeoLoc());
	}

	/** Set label count */
	private void setLabelCount(JLabel lbl, Integer count) {
		if (count == 0)
			lbl.setText("none");
		else
			lbl.setText(""+count);
	}
	
	/** Update all text and labels */
	public void updateGui() {
		boolean bRwisEnabled;
		bRwisEnabled = (SystemAttrEnum.RWIS_CYCLE_SEC.getInt() > 0); 
		rwisSystem_lbl.setText(bRwisEnabled ? "ENABLED" : "disabled");

		ArrayList<String> hashtagList = getRwisHashtags();
		int hashtagCnt = hashtagList.size();
		setLabelCount(hashtagsFound_lbl, hashtagCnt);

		ArrayList<WeatherSensor> sensorList;
		sensorList = DMSHelper.getAssociatedWeatherSensors(dms);
		int wsCnt = sensorList.size();
		setLabelCount(wsFound_lbl, wsCnt);
		
		Object o[] = DMSHelper.findClosestWeatherSensor(dms, true);
		WeatherSensor ws = (WeatherSensor)o[0];
		Integer       d  = (Integer)      o[1];
		if (ws != null) {
			Distance dist = Distance.create(d, Units.METERS);
			closest_lbl.setText(ws.getName());
			boolean useSI = SystemAttrEnum.CLIENT_UNITS_SI.getBoolean();
			Units u = useSI ? Units.KILOMETERS : Units.MILES;
			String dStr = dist.convert(u).toString();
			if (d > SystemAttrEnum.RWIS_AUTO_MAX_M.getInt())
				dStr += "  " + I18N.get("dms.rwis.out.of.range");
			distance_lbl.setText(dStr);
		}
		else {
			closest_lbl.setText(I18N.get("dms.rwis.none.defined"));
			distance_lbl.setText("");
		}

		boolean bHashtags = !hashtagList.isEmpty(); // found hashtags?
		boolean bSensors  = !sensorList.isEmpty();  // found sensors? (distance OR override)
		boolean bReady    = bHashtags && bSensors;  // is DMS ready to run RWIS?
		hashtagsFound_lbl.setForeground(Color.black);
		wsFound_lbl.setForeground(Color.black);
		warn_lbl.setForeground(Color.black);
		String warn = " ";
		if (bHashtags) {
			if (bSensors) {
				// found hashtags and sensors
				if (bRwisEnabled)
					warn = I18N.get("dms.rwis.enabled");
				else
					warn = I18N.get("dms.rwis.ready");
			}
			else {
				// found hashtags but no sensors
				warn = I18N.get("dms.rwis.no.sensors");
				wsFound_lbl.setForeground(Color.red);
				warn_lbl.setForeground(Color.red);
			}
		}
		else {
			if (bSensors) {
				// found sensors, but no hashtags
				hashtagsFound_lbl.setForeground(Color.red);
				warn = I18N.get("dms.rwis.no.hashtags");
				warn_lbl.setForeground(Color.red);
			}
			else {
				// didn't find hashtags or sensors
				warn = I18N.get("dms.rwis.disabled");
			}
		}
		warn_lbl.setText(warn);
		repaint();
	}

	/** Update the edit mode */
	public void updateEditMode() {
		weatherSensorOverride_txt.setEnabled(canWrite("weatherSensorOverride"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (null == a || a.equals("weatherSensorOverride")) {
			String eo = dms.getWeatherSensorOverride();
			if (eo == null)
				eo = "";
			weatherSensorOverride_txt.setText(eo);
			updateGui();
		}
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		Session session = Session.getCurrent();
		if (session == null)
			return false;
		return session.canWrite(dms, aname);
	}

	/** Called when all proxies have been enumerated (from ProxyView). */
	@Override
	public void enumerationComplete() { }

	/** Update one attribute (from ProxyView). */
	@Override
	public void update(GeoLoc l, String a) {
		if (a == null
		 || a.equals("lat")
		 || a.equals("lon"))
			updateGui();
	}

	/** Clear all attributes (from ProxyView). */
	@Override
	public void clear() {}

	/** Get array of RWIS hashtag strings 
	 * (without leading '#').
	 *  Returns empty array if no RWIS hashtags
	 *  are associated with the sign. */
	public ArrayList<String> getRwisHashtags() {
		ArrayList<String> tagList = new ArrayList<String>();
		if (dms != null)
			for (String tag: dms.getHashtags())
				if (tag.startsWith("#RWIS"))
					tagList.add(tag.substring(1));
		return tagList;
	}
}
