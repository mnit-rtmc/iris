/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * The IncidentDispatcher is a GUI component for creating incidents.
 *
 * @author Douglas Lau
 */
public class IncidentDispatcher extends JPanel
	implements ProxyListener<Incident>, ProxySelectionListener<Incident>
{
	/** Font for "L" and "R" labels */
	static protected final Font FONT = new Font(null, Font.BOLD, 24);

	/** Formatter for incident names */
	static protected final SimpleDateFormat NAME_FMT =
		new SimpleDateFormat("yyyyMMddHHmmssSSS");

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Cache of incident proxy objects */
	protected final TypeCache<Incident> cache;

	/** Selection model */
	protected final ProxySelectionModel<Incident> selectionModel;

	/** Currently logged in user */
	protected final User user;

	/** Type label */
	protected final JLabel type_lbl = new JLabel();

	/** Location of incident */
	protected final JTextField location_txt = FormPanel.createTextField();

	/** Verify camera name textfield */
	protected final JTextField camera_txt = FormPanel.createTextField();

	/** Impact panel */
	protected final ImpactPanel impact_pnl = new ImpactPanel();

	/** Button to log an incident change */
	protected final JButton log_btn = new JButton("Log");

	/** Button to deploy devices */
	protected final JButton deploy_btn = new JButton("Deploy");

	/** Button to clear an incident */
	protected final JToggleButton clear_btn = new JToggleButton("Clear");

	/** Incident manager */
	protected final IncidentManager manager;

	/** Currently watching incident */
	protected Incident watching;

	/** Create a new incident dispatcher */
	public IncidentDispatcher(Session session, IncidentManager man) {
		super(new BorderLayout());
		SonarState st = session.getSonarState();
		namespace = st.getNamespace();
		cache = st.getIncidents();
		user = session.getUser();
		manager = man;
		selectionModel = manager.getSelectionModel();
		cache.addProxyListener(this);
		selectionModel.addProxySelectionListener(this);
		add(createMainPanel());
		clearSelected();
		createButtonJobs();
	}

	/** Create the main panel */
	protected JPanel createMainPanel() {
		type_lbl.setHorizontalTextPosition(SwingConstants.TRAILING);
		FormPanel panel = new FormPanel(true);
		panel.setBorder(BorderFactory.createTitledBorder(
			"Selected Incident"));
		panel.addRow("Incident Type", type_lbl);
		panel.addRow("Location", location_txt);
		panel.addRow("Camera", camera_txt);
		panel.addRow(buildImpactBox());
		JPanel btns = new JPanel(new FlowLayout());
		btns.add(log_btn);
		btns.add(deploy_btn);
		btns.add(clear_btn);
		panel.addRow(btns);
		return panel;
	}

	/** Build the impact box */
	protected Box buildImpactBox() {
		impact_pnl.setBorder(BorderFactory.createTitledBorder(
			"Impact"));
		Box box = Box.createHorizontalBox();
		box.add(createLabel("L"));
		box.add(Box.createHorizontalStrut(4));
		box.add(impact_pnl);
		box.add(Box.createHorizontalStrut(4));
		box.add(createLabel("R"));
		return box;
	}

	/** Create an "L" or "R" label */
	protected JLabel createLabel(String t) {
		JLabel label = new JLabel(t);
		label.setFont(FONT);
		return label;
	}

	/** Create jobs for button press events */
	protected void createButtonJobs() {
		new ActionJob(log_btn) {
			public void perform() {
				Incident inc = getSingleSelection();
				if(inc instanceof ClientIncident)
					create((ClientIncident)inc);
				else if(inc != null)
					logUpdate(inc);
			}
		};
		new ActionJob(clear_btn) {
			public void perform() {
				Incident inc = getSingleSelection();
				if(inc != null &&
				   !(inc instanceof ClientIncident))
					inc.setCleared(clear_btn.isSelected());
			}
		};
	}

	/** Create a new incident */
	protected void create(ClientIncident inc) {
		String name = createUniqueIncidentName();
		if(canAdd(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("event_desc_id", inc.getEventType());
			attrs.put("road", inc.getRoad());
			attrs.put("dir", inc.getDir());
			attrs.put("easting", inc.getEasting());
			attrs.put("northing", inc.getNorthing());
			attrs.put("impact", impact_pnl.getImpact());
			attrs.put("cleared", false);
			cache.createObject(name, attrs);
			// FIXME: wait for object creation and select it
			selectionModel.clearSelection();
		}
	}

	/** Update an incident */
	protected void logUpdate(Incident inc) {
		inc.setImpact(impact_pnl.getImpact());
	}

	/** Create a unique incident name */
	protected String createUniqueIncidentName() {
		String name = NAME_FMT.format(System.currentTimeMillis());
		return name.substring(0, 16);
	}

	/** A new proxy has been added */
	public void proxyAdded(Incident proxy) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	public void proxyRemoved(Incident proxy) {
		// Note: the IncidentManager will remove the proxy from the
		//       ProxySelectionModel, so we can ignore this.
	}

	/** A proxy has been changed */
	public void proxyChanged(final Incident proxy, final String a) {
		if(proxy == getSingleSelection()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateAttribute(proxy, a);
				}
			});
		}
	}

	/** Get the selected incident (if only one is selected) */
	protected Incident getSingleSelection() {
		if(selectionModel.getSelectedCount() == 1) {
			for(Incident inc: selectionModel.getSelected())
				return inc;
		}
		return null;
	}

	/** Dispose of the dispatcher */
	public void dispose() {
		selectionModel.removeProxySelectionListener(this);
		cache.removeProxyListener(this);
		if(watching != null) {
			cache.ignoreObject(watching);
			watching = null;
		}
		clearSelected();
		removeAll();
	}

	/** Called whenever an object is added to the selection */
	public void selectionAdded(Incident s) {
		updateSelected();
	}

	/** Called whenever an object is removed from the selection */
	public void selectionRemoved(Incident s) {
		updateSelected();
	}

	/** Update the selected object(s) */
	protected void updateSelected() {
		List<Incident> selected = selectionModel.getSelected();
		if(selected.size() == 1) {
			for(Incident inc: selected)
				setSelected(inc);
		} else
			clearSelected();
	}

	/** Clear the selection */
	protected void clearSelected() {
		disableWidgets();
	}

	/** Disable the dispatcher widgets */
	protected void disableWidgets() {
		clearEventType();
		location_txt.setText("");
		camera_txt.setText("");
		log_btn.setEnabled(false);
		deploy_btn.setEnabled(false);
		clear_btn.setEnabled(false);
		clear_btn.setSelected(false);
		impact_pnl.setImpact("");
	}

	/** Set a single selected incident */
	protected void setSelected(Incident inc) {
		if(watching != null)
			cache.ignoreObject(watching);
		if(inc instanceof ClientIncident)
			watching = null;
		else {
			watching = inc;
			cache.watchObject(watching);
		}
		updateAttribute(inc, null);
		enableWidgets(inc);
	}

	/** Enable the dispatcher widgets */
	protected void enableWidgets(Incident inc) {
		log_btn.setEnabled(true);
		if(inc instanceof ClientIncident) {
			deploy_btn.setEnabled(false);
			clear_btn.setEnabled(false);
		} else {
			deploy_btn.setEnabled(true);
			clear_btn.setEnabled(true);
		}
	}

	/** Update one attribute on the form */
	protected void updateAttribute(Incident inc, String a) {
		if(a == null) {
			EventType et = EventType.fromId(inc.getEventType());
			if(et != null)
				updateEventType(et);
			else
				clearEventType();
			Road road = inc.getRoad();
			short dir = inc.getDir();
			String loc = GeoLocHelper.getCorridorName(road, dir);
			location_txt.setText(loc);
			Camera cam = findNearestCamera(inc);
			if(cam != null)
				camera_txt.setText(cam.getName());
			else
				camera_txt.setText("");
		}
		if(a == null || a.equals("impact")) {
			impact_pnl.setImpact(inc.getImpact());
		}
		if(a == null || a.equals("cleared")) {
			clear_btn.setSelected(inc.getCleared());
		}
	}

	/** Update the event type */
	protected void updateEventType(EventType et) {
		String sty = manager.getStyle(et);
		if(sty != null) {
			type_lbl.setText(sty);
			Symbol sym = manager.getTheme().getSymbol(sty);
			if(sym != null)
				type_lbl.setIcon(sym.getLegend());
			else
				type_lbl.setIcon(null);
		} else
			clearEventType();
	}

	/** Clear the event type */
	protected void clearEventType() {
		type_lbl.setText("");
		type_lbl.setIcon(null);
	}

	/** Find the nearest camera to an incident */
	protected Camera findNearestCamera(Incident inc) {
		CameraFinder finder = new CameraFinder(inc);
		CameraHelper.find(finder);
		return finder.camera;
	}

	/** Inner class to find the camera nearest to an incident */
	static protected class CameraFinder implements Checker<Camera> {
		protected final int easting;
		protected final int northing;
		protected double dist = Double.POSITIVE_INFINITY;
		protected Camera camera = null;
		protected CameraFinder(Incident inc) {
			easting = inc.getEasting();
			northing = inc.getNorthing();
		}
		public boolean check(Camera cam) {
			GeoLoc loc = cam.getGeoLoc();
			double d = GeoLocHelper.metersTo(loc, easting,northing);
			if(d < dist) {
				dist = d;
				camera = cam;
			}
			return false;
		}
	}

	/** Check if the user can add the named incident */
	public boolean canAdd(String oname) {
		return oname != null &&
		       namespace.canAdd(user, new Name(Incident.SONAR_TYPE,
			oname));
	}
}
