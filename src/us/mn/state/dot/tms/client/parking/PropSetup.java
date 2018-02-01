/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.client.parking;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;

/**
 * Parking area properties setup panel.
 *
 * @author Douglas Lau
 */
public class PropSetup extends IPanel {

	/** Parse an integer */
	static private Integer parseInt(JTextField txt) {
		try {
			String t = txt.getText().trim();
			return Integer.parseInt(t);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Format an integer field */
	static private void formatInt(JTextField txt, Integer v) {
		txt.setText((v != null) ? v.toString() : "");
	}

	/** Parse a text field */
	static private String parseTxt(JTextField txt) {
		String t = txt.getText();
		if (t != null) {
			t = t.trim();
			return t.isEmpty() ? null : t;
		} else
			return null;
	}

	/** Format a text field */
	static private void formatTxt(JTextField txt, String v) {
		txt.setText((v != null) ? v : "");
	}

	/** Site ID text */
	private final JTextField site_id_txt = new JTextField("", 25);

	/** Relevant highway text */
	private final JTextField highway_txt = new JTextField("", 10);

	/** Reference post text */
	private final JTextField post_txt = new JTextField("", 10);

	/** Exit ID text */
	private final JTextField exit_txt = new JTextField("", 10);

	/** Facility name text */
	private final JTextField facility_txt = new JTextField("", 30);

	/** Street address text */
	private final JTextField street_adr_txt = new JTextField("", 30);

	/** City text */
	private final JTextField city_txt = new JTextField("", 30);

	/** State text */
	private final JTextField state_txt = new JTextField("", 2);

	/** Zip code text */
	private final JTextField zip_txt = new JTextField("", 10);

	/** Time zone text */
	private final JTextField tz_txt = new JTextField("", 10);

	/** Ownership text */
	private final JTextField ownership_txt = new JTextField("", 2);

	/** Amenities text */
	private final JTextField amenities_txt = new JTextField("", 30);

	/** Capacity text */
	private final JTextField capacity_txt = new JTextField("", 6);

	/** Low threshold text */
	private final JTextField low_txt = new JTextField("", 6);

	/** User session */
	private final Session session;

	/** Parking area */
	private final ParkingArea proxy;

	/** Create a new parking area properties setup panel */
	public PropSetup(Session s, ParkingArea c) {
		session = s;
		proxy = c;
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		add("parking_area.site.id");
		add(site_id_txt, Stretch.LAST);
		add("parking_area.highway");
		add(highway_txt, Stretch.LAST);
		add("parking_area.post");
		add(post_txt, Stretch.LAST);
		add("parking_area.exit");
		add(exit_txt, Stretch.LAST);
		add("parking_area.facility");
		add(facility_txt, Stretch.LAST);
		add("parking_area.street.adr");
		add(street_adr_txt, Stretch.LAST);
		add("parking_area.city");
		add(city_txt, Stretch.LAST);
		add("parking_area.state");
		add(state_txt, Stretch.LAST);
		add("parking_area.zip");
		add(zip_txt, Stretch.LAST);
		add("parking_area.time.zone");
		add(tz_txt, Stretch.LAST);
		add("parking_area.ownership");
		add(ownership_txt, Stretch.LAST);
		add("parking_area.amenities");
		add(amenities_txt, Stretch.LAST);
		add("parking_area.capacity");
		add(capacity_txt, Stretch.LAST);
		add("parking_area.low");
		add(low_txt, Stretch.LAST);
		createJobs();
	}

	/** Create jobs */
	private void createJobs() {
		site_id_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setSiteId(parseTxt(site_id_txt));
			}
		});
		highway_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setRelevantHighway(parseTxt(highway_txt));
			}
		});
		post_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setReferencePost(parseTxt(post_txt));
			}
		});
		exit_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setExitId(parseTxt(exit_txt));
			}
		});
		facility_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setFacilityName(parseTxt(facility_txt));
			}
		});
		street_adr_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setStreetAdr(parseTxt(street_adr_txt));
			}
		});
		city_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setCity(parseTxt(city_txt));
			}
		});
		state_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setState(parseTxt(state_txt));
			}
		});
		zip_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setZip(parseTxt(zip_txt));
			}
		});
		tz_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setTimeZone(parseTxt(tz_txt));
			}
		});
		ownership_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setOwnership(parseTxt(ownership_txt));
			}
		});
		amenities_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setAmenities(parseTxt(amenities_txt));
			}
		});
		capacity_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setCapacity(parseInt(capacity_txt));
			}
		});
		low_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    proxy.setLowThreshold(parseInt(low_txt));
			}
		});
	}

	/** Update the edit mode */
	public void updateEditMode() {
		site_id_txt.setEnabled(canWrite("siteId"));
		highway_txt.setEnabled(canWrite("relevantHighway"));
		post_txt.setEnabled(canWrite("referencePost"));
		exit_txt.setEnabled(canWrite("exitId"));
		facility_txt.setEnabled(canWrite("facilityName"));
		street_adr_txt.setEnabled(canWrite("streetAdr"));
		city_txt.setEnabled(canWrite("city"));
		state_txt.setEnabled(canWrite("state"));
		zip_txt.setEnabled(canWrite("zip"));
		tz_txt.setEnabled(canWrite("timeZone"));
		ownership_txt.setEnabled(canWrite("ownership"));
		amenities_txt.setEnabled(canWrite("amenities"));
		capacity_txt.setEnabled(canWrite("capacity"));
		low_txt.setEnabled(canWrite("lowThreshold"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (a == null || a.equals("siteId"))
			formatTxt(site_id_txt, proxy.getSiteId());
		if (a == null || a.equals("relevantHighway"))
			formatTxt(highway_txt, proxy.getRelevantHighway());
		if (a == null || a.equals("referencePost"))
			formatTxt(post_txt, proxy.getReferencePost());
		if (a == null || a.equals("exitId"))
			formatTxt(exit_txt, proxy.getExitId());
		if (a == null || a.equals("facilityName"))
			formatTxt(facility_txt, proxy.getFacilityName());
		if (a == null || a.equals("streetAdr"))
			formatTxt(street_adr_txt, proxy.getStreetAdr());
		if (a == null || a.equals("city"))
			formatTxt(city_txt, proxy.getCity());
		if (a == null || a.equals("state"))
			formatTxt(state_txt, proxy.getState());
		if (a == null || a.equals("zip"))
			formatTxt(zip_txt, proxy.getZip());
		if (a == null || a.equals("timeZone"))
			formatTxt(tz_txt, proxy.getTimeZone());
		if (a == null || a.equals("ownership"))
			formatTxt(ownership_txt, proxy.getOwnership());
		if (a == null || a.equals("amenities"))
			formatTxt(amenities_txt, proxy.getAmenities());
		if (a == null || a.equals("capacity"))
			formatInt(capacity_txt, proxy.getCapacity());
		if (a == null || a.equals("lowThreshold"))
			formatInt(low_txt, proxy.getLowThreshold());
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		return session.canWrite(proxy, aname);
	}
}
