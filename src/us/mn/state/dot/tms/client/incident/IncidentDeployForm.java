/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.lcs.LCSArrayCellRenderer;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * IncidentDeployForm is a dialog for deploying devices for an incident.
 *
 * @author Douglas Lau
 */
public class IncidentDeployForm extends SonarObjectForm<Incident> {

	/** Frame title */
	static private final String TITLE = "Incident: ";

	/** Currently logged in user */
	protected final User user;

	/** Incident manager */
	protected final IncidentManager manager;

	/** Incident deployment policy */
	protected final IncidentPolicy policy;

	/** Mapping of LCS array names to proposed indications */
	protected final HashMap<String, Integer []> indications =
		new HashMap<String, Integer []>();

	/** Model for deployment list */
	protected final DefaultListModel model = new DefaultListModel();

	/** List of deployments for the incident */
	protected final JList list = new JList(model);

	/** Button to send device messages */
	protected final JButton send_btn = new JButton("Send");

	/** Create a new incident deploy form */
	public IncidentDeployForm(Session s, Incident inc, IncidentManager man){
		super(TITLE, s, inc);
		user = session.getUser();
		manager = man;
		policy = new IncidentPolicy(inc);
	}

	/** Get the SONAR type cache */
	protected TypeCache<Incident> getTypeCache() {
		return state.getIncidents();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		list.setCellRenderer(new LCSArrayCellRenderer() {
			protected User getUser(LCSArray lcs_array) {
				return user;
			}
			protected Integer[] getIndications(LCSArray lcs_array) {
				return indications.get(lcs_array.getName());
			}
		});
		populateList();
		add(createPanel());
		createJobs();
	}

	/** Populate the list model with LCS array indications to display */
	protected void populateList() {
		IncidentLoc loc = new IncidentLoc(proxy);
		CorridorBase cb = manager.lookupCorridor(loc);
		if(cb != null) {
			Float mp = cb.calculateMilePoint(loc);
			if(mp != null)
				populateList(cb, mp);
		}
	}

	/** Populate the list model with LCS array indications to display */
	protected void populateList(CorridorBase cb, float mp) {
		TreeMap<Float, LCSArray> upstream =
			new TreeMap<Float, LCSArray>();
		for(LCSArray lcs_array: findLCS(cb)) {
			GeoLoc loc = LCSArrayHelper.lookupGeoLoc(lcs_array);
			Float lp = cb.calculateMilePoint(loc);
			if(lp != null) {
				float up = mp - lp;
				if(up > 0)
					upstream.put(up, lcs_array);
			}
		}
		int shift = cb.getShift(proxy.getEasting(),proxy.getNorthing());
		for(Float up: upstream.keySet()) {
			LCSArray lcs_array = upstream.get(up);
			Integer[] ind = policy.createIndications(lcs_array, up,
				shift);
			if(shouldDeploy(ind)) {
				model.addElement(lcs_array);
				indications.put(lcs_array.getName(), ind);
			}
		}
	}

	/** Find all LCS arrays on the given corridor */
	protected List<LCSArray> findLCS(CorridorBase cb) {
		final LinkedList<LCSArray> lcss = new LinkedList<LCSArray>();
		LCSArrayHelper.find(new Checker<LCSArray>() {
			public boolean check(LCSArray lcs_array) {
				lcss.add(lcs_array);
				return false;
			}
		});
		// Corridor filtering cannot be done within Checker because
		// of TypeCache deadlock problems.
		Iterator<LCSArray> it = lcss.iterator();
		while(it.hasNext()) {
			GeoLoc loc = LCSArrayHelper.lookupGeoLoc(it.next());
			if(loc.getFreeway() != cb.getFreeway() ||
			   loc.getFreeDir() != cb.getFreeDir())
				it.remove();
		}
		return lcss;
	}

	/** Check if a set of indications should be deployed */
	static protected boolean shouldDeploy(Integer[] ind) {
		for(int i: ind) {
			LaneUseIndication li = LaneUseIndication.fromOrdinal(i);
			switch(LaneUseIndication.fromOrdinal(i)) {
			case DARK:
			case LANE_OPEN:
				continue;
			default:
				return true;
			}
		}
		return false;
	}

	/** Create the panel for the form */
	protected JPanel createPanel() {
		JLabel lbl = new JLabel();
		lbl.setHorizontalTextPosition(SwingConstants.TRAILING);
		lbl.setText(manager.getDescription(proxy));
		Symbol sym = manager.getSymbol(proxy);
		if(sym != null)
			lbl.setIcon(sym.getLegend());
		FormPanel panel = new FormPanel(false);
		panel.addRow(lbl);
		panel.addRow("<html><p align=\"right\">Proposed" +
			"<br/>Indications</p></html>", list);
		panel.addRow(send_btn);
		send_btn.setEnabled(model.getSize() > 0);
		return panel;
	}

	/** Create jobs */
	protected void createJobs() {
		new ActionJob(send_btn) {
			public void perform() {
				sendIndications();
				closeForm();
			}
		};
	}

	/** Send new indications to LCS arrays for the incident */
	protected void sendIndications() {
		for(int i = 0; i < model.getSize(); i++) {
			Object e = model.getElementAt(i);
			if(e instanceof LCSArray)
				sendIndications((LCSArray)e);
		}
	}

	/** Send new indications to the specified LCS array */
	protected void sendIndications(LCSArray lcs_array) {
		Integer[] ind = indications.get(lcs_array.getName());
		if(ind != null) {
			lcs_array.setOwnerNext(user);
			lcs_array.setIndicationsNext(ind);
		}
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		if("cleared".equals(a) && proxy.getCleared())
			closeForm();
	}
}
