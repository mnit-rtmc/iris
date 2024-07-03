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
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
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
 * PropRwis is a DMS properties panel for displaying and editing DMS-specific
 * RWIS configuration info (mainly weather sensors) on a DMS properties form.
 *
 * @author John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")
public class PropRwis extends IPanel implements ProxyView<GeoLoc> {

	/** Get distance in meters between a WeatherSensor and a DMS.
	 *  Returns null if distance is unknown. */
	static private Integer calcDistanceMeters(WeatherSensor ws, DMS dms) {
		if ((ws == null) || (dms == null))
			return null;
		GeoLoc g1 = ws.getGeoLoc();
		GeoLoc g2 = dms.getGeoLoc();
		Distance d = GeoLocHelper.distanceTo(g1, g2);
		return (d == null) ? null : d.round(Distance.Units.METERS);
	}

	/** Parse whitespace-separated list of WeatherSensor names */
	static private WeatherSensor[] parseWeatherSensors(String names)
		throws TMSException
	{
		TreeSet<WeatherSensor> ws_set = new TreeSet<WeatherSensor>();
		if (names != null) {
			for (String n : names.trim().split(" ")) {
				if (n.trim().isEmpty())
					continue;
				WeatherSensor ws = WeatherSensorHelper.lookup(n);
				if (ws != null)
					ws_set.add(ws);
				else {
					throw new ChangeVetoException(
						"Unknown WeatherSensor: " + n);
				}
			}
		}
		return ws_set.toArray(new WeatherSensor[0]);
	}

	/** Find the WeatherSensor that's closest to the DMS.
	 * @return A two-Object-array containing the WeatherSensor
	 *         and the distance to that WeatherSensor in meters.
	 *         If no WeatherSensor qualifies, return a two Object array
	 *         containing nulls. */
	static private Object[] findClosestWeatherSensor(DMS dms) {
		Object[] retVals = new Object[2];
		retVals[0] = null;
		retVals[1] = null;
		int cd = Integer.MAX_VALUE;
		WeatherSensor closestWs = null;
		WeatherSensor ws;
		Integer closestDist = cd;
		Integer dist;
		Iterator<WeatherSensor> it = WeatherSensorHelper.iterator();
		while (it.hasNext()) {
			ws = it.next();
			dist = calcDistanceMeters(ws, dms);
			if ((dist == null) || (dist > closestDist))
				continue;
			if (dist < closestDist) {
				closestDist = dist;
				closestWs   = ws;
			}
		}
		if (closestWs != null) {
			retVals[0] = closestWs;
			retVals[1] = closestDist;
		}
		return retVals;
	}

	/** RWIS enabled label */
	private final JLabel rwisSystem_lbl = new JLabel();

	/** RWIS hashtags label */
	private final JLabel hashtagsFound_lbl = new JLabel();

	/** Closest WeatherSensor label */
	private final JLabel closest_lbl = new JLabel();

	/** Distance label */
	private final JLabel distance_lbl = new JLabel();

	/** RWIS weather sensors text area */
	private final JTextArea weather_sensor_txt = new JTextArea(3, 32);

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
		weather_sensor_txt.addFocusListener(new FocusAdapter() {
			@Override public void focusLost(FocusEvent e) {
				String ws = weather_sensor_txt.getText();
				try {
					WeatherSensor[] sensors =
						parseWeatherSensors(ws);
					dms.setWeatherSensors(sensors);
					updateGui();
				}
				catch (TMSException e2) {
					DialogHandler handler = new DialogHandler();
					handler.handle(e2);
				}
			}
		});
		add("dms.rwis.system");
		add(rwisSystem_lbl, Stretch.LAST);
		add("dms.rwis.hashtags");
		add(hashtagsFound_lbl, Stretch.LAST);
		add("dms.rwis.closest");
		add(closest_lbl, Stretch.LAST);
		add("dms.rwis.distance");
		add(distance_lbl, Stretch.LAST);
		add(new JLabel(" "), Stretch.CENTER);
		add("dms.rwis.sensors");
		add(weather_sensor_txt, Stretch.LAST);
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
			lbl.setText("" + count);
	}

	/** Update all text and labels */
	public void updateGui() {
		boolean bRwisEnabled;
		bRwisEnabled = (SystemAttrEnum.RWIS_CYCLE_SEC.getInt() > 0); 
		rwisSystem_lbl.setText(bRwisEnabled ? "ENABLED" : "disabled");

		ArrayList<String> hashtagList = getRwisHashtags();
		int hashtagCnt = hashtagList.size();
		setLabelCount(hashtagsFound_lbl, hashtagCnt);

		Object[] o = findClosestWeatherSensor(dms);
		WeatherSensor ws = (WeatherSensor) o[0];
		Integer       d  = (Integer)       o[1];
		if (ws != null) {
			Distance dist = Distance.create(d, Units.METERS);
			closest_lbl.setText(ws.getName());
			boolean useSI =
				SystemAttrEnum.CLIENT_UNITS_SI.getBoolean();
			Units u = useSI ? Units.KILOMETERS : Units.MILES;
			String dStr = dist.convert(u).toString();
			if (d > SystemAttrEnum.RWIS_AUTO_MAX_M.getInt())
				dStr += "  " + I18N.get("dms.rwis.out.of.range");
			distance_lbl.setText(dStr);
		} else {
			closest_lbl.setText(I18N.get("dms.rwis.none.defined"));
			distance_lbl.setText("");
		}

		boolean bHashtags = !hashtagList.isEmpty(); // found hashtags?
		boolean bReady    = bHashtags;  // is DMS ready to run RWIS?
		hashtagsFound_lbl.setForeground(Color.black);
		warn_lbl.setForeground(Color.black);
		String warn = " ";
		if (bHashtags) {
			// found hashtags and sensors
			if (bRwisEnabled)
				warn = I18N.get("dms.rwis.enabled");
			else
				warn = I18N.get("dms.rwis.ready");
		} else {
			// found sensors, but no hashtags
			hashtagsFound_lbl.setForeground(Color.red);
			warn = I18N.get("dms.rwis.no.hashtags");
			warn_lbl.setForeground(Color.red);
		}
		warn_lbl.setText(warn);
		repaint();
	}

	/** Update the edit mode */
	public void updateEditMode() {
		weather_sensor_txt.setEnabled(canWrite("weatherSensors"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (null == a || a.equals("weatherSensors")) {
			StringBuilder names = new StringBuilder();
			for (WeatherSensor ws: dms.getWeatherSensors()) {
				if (!names.isEmpty())
					names.append(' ');
				names.append(ws.getName());
			}
			weather_sensor_txt.setText(names.toString());
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
