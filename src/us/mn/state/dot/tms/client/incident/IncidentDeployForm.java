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

import java.awt.Color;
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

	/** Impact codes */
	static private enum ImpactCode {
		FREE_FLOWING, PARTIALLY_BLOCKED, BLOCKED;
		static protected ImpactCode fromChar(char im) {
			switch(im) {
			case '?':
				return PARTIALLY_BLOCKED;
			case '!':
				return BLOCKED;
			default:
				return FREE_FLOWING;
			}
		}
	};

	/** Distance 1 upstream of incident to deploy devices */
	static protected final float DIST_UPSTREAM_1_MILES = 0.5f;

	/** Distance 2 upstream of incident to deploy devices */
	static protected final float DIST_UPSTREAM_2_MILES = 1.0f;

	/** Distance 3 upstream of incident to deploy devices */
	static protected final float DIST_UPSTREAM_3_MILES = 1.5f;

	/** Frame title */
	static private final String TITLE = "Incident: ";

	/** Currently logged in user */
	protected final User user;

	/** Incident manager */
	protected final IncidentManager manager;

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
		list.setVisibleRowCount(3);
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
				if(up > 0 && up < DIST_UPSTREAM_3_MILES)
					upstream.put(up, lcs_array);
			}
		}
		int shift = cb.getShift(proxy.getEasting(),proxy.getNorthing());
		for(Float up: upstream.keySet()) {
			LCSArray lcs_array = upstream.get(up);
			Integer[] ind = createIndications(lcs_array, up, shift);
			if(shouldDeploy(ind)) {
				model.addElement(lcs_array);
				indications.put(lcs_array.getName(), ind);
			}
		}
	}

	/** Create indications for an LCS array.
	 * @param lcs_array LCS array to deploy.
	 * @param up Distance upstream from incident (miles).
	 * @param shift Left lane shift at incident.
	 * @return Array of LaneUseIndication ordinal values. */
	protected Integer[] createIndications(LCSArray lcs_array, float up,
		int shift)
	{
		int n_lanes = lcs_array.getIndicationsCurrent().length;
		int l_shift = lcs_array.getShift() - shift;
		if(up < DIST_UPSTREAM_1_MILES)
			return createIndications1(n_lanes, l_shift);
		if(up < DIST_UPSTREAM_2_MILES)
			return createIndications2(n_lanes, l_shift);
		else
			return createIndications3(n_lanes, l_shift);
	}

	/** Create first indications for an LCS array.
	 * @param n_lanes Number of lanes on LCS array.
	 * @param l_shift Lane shift relative to incident.
	 * @return Array of LaneUseIndication ordinal values. */
	protected Integer[] createIndications1(int n_lanes, int l_shift) {
		Integer[] ind = new Integer[n_lanes];
		for(int i = 0; i < ind.length; i++)
			ind[i] = getIndication1(l_shift + n_lanes -i).ordinal();
		return ind;
	}

	/** Get the first indication for one lane.
	 * @param ln Lane shift relative to incident.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication1(int ln) {
		ImpactCode ic = getImpact(ln);
		if(ic == ImpactCode.BLOCKED)
			return LaneUseIndication.LANE_CLOSED;
		if(ic == ImpactCode.PARTIALLY_BLOCKED)
			return LaneUseIndication.USE_CAUTION;
		ImpactCode right = getImpact(ln + 1);
		if(right == ImpactCode.BLOCKED)
			return LaneUseIndication.USE_CAUTION;
		ImpactCode left = getImpact(ln - 1);
		if(left == ImpactCode.BLOCKED)
			return LaneUseIndication.USE_CAUTION;
		return LaneUseIndication.LANE_OPEN;
	}

	/** Get the impact at the specified lane */
	protected ImpactCode getImpact(int ln) {
		String impact = proxy.getImpact();
		if(ln < 0 || ln >= impact.length())
			return ImpactCode.FREE_FLOWING;
		else
			return ImpactCode.fromChar(impact.charAt(ln));
	}

	/** Create second indications for an LCS array.
	 * @param n_lanes Number of lanes on LCS array.
	 * @param l_shift Lane shift relative to incident.
	 * @return Array of LaneUseIndication ordinal values. */
	protected Integer[] createIndications2(int n_lanes, int l_shift) {
		Integer[] ind = new Integer[n_lanes];
		for(int i = 0; i < ind.length; i++)
			ind[i] = getIndication2(n_lanes, l_shift, i).ordinal();
		return ind;
	}

	/** Get the second indication for one lane.
	 * @param n_lanes Number of lanes in array.
	 * @param l_shift Lane shift relative to incident.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication2(int n_lanes, int l_shift,
		int i)
	{
		int ln = l_shift + n_lanes - i;
		ImpactCode ic = getImpact2(ln);
		if(ic != ImpactCode.BLOCKED)
			return LaneUseIndication.LANE_OPEN;
		ImpactCode right = getImpact2(ln + 1);
		if(i - 1 < 0)
			right = ImpactCode.BLOCKED;
		ImpactCode left = getImpact2(ln - 1);
		if(i + 1 >= n_lanes)
			left = ImpactCode.BLOCKED;
		if(left == ImpactCode.BLOCKED && right == ImpactCode.BLOCKED)
			return LaneUseIndication.LANE_CLOSED;
		if(left != ImpactCode.BLOCKED && right != ImpactCode.BLOCKED)
			return LaneUseIndication.MERGE_BOTH;
		if(left == ImpactCode.BLOCKED)
			return LaneUseIndication.MERGE_RIGHT;
		else
			return LaneUseIndication.MERGE_LEFT;
	}

	/** Get the impact at the specified lane */
	protected ImpactCode getImpact2(int ln) {
		String impact = proxy.getImpact();
		if(ln < 0 || ln >= impact.length())
			return ImpactCode.BLOCKED;
		else
			return ImpactCode.fromChar(impact.charAt(ln));
	}

	/** Create third indications for an LCS array.
	 * @param n_lanes Number of lanes on LCS array.
	 * @param l_shift Lane shift relative to incident.
	 * @return Array of LaneUseIndication ordinal values. */
	protected Integer[] createIndications3(int n_lanes, int l_shift) {
		Integer[] ind = new Integer[n_lanes];
		for(int i = 0; i < ind.length; i++)
			ind[i] = getIndication3(n_lanes, l_shift, i).ordinal();
		return ind;
	}

	/** Get the third indication for one lane.
	 * @param n_lanes Number of lanes in array.
	 * @param l_shift Lane shift relative to incident.
	 * @return LaneUseIndication value. */
	protected LaneUseIndication getIndication3(int n_lanes, int l_shift,
		int i)
	{
		int ln = l_shift + n_lanes - i;
		ImpactCode ic = getImpact2(ln);
		if(ic != ImpactCode.BLOCKED)
			return LaneUseIndication.LANE_OPEN;
		ImpactCode right = getImpact2(ln + 1);
		if(i - 1 < 0)
			right = ImpactCode.BLOCKED;
		ImpactCode left = getImpact2(ln - 1);
		if(i + 1 >= n_lanes)
			left = ImpactCode.BLOCKED;
		if(left == ImpactCode.BLOCKED && right == ImpactCode.BLOCKED) {
			// NOTE: adjacent lanes are also blocked, so find the
			//       impact 2 lanes to the left and right
			right = getImpact2(ln + 2);
			if(i - 2 < 0)
				right = ImpactCode.BLOCKED;
			left = getImpact2(ln - 2);
			if(i + 2 >= n_lanes)
				left = ImpactCode.BLOCKED;
			if(left != ImpactCode.BLOCKED &&
			   right != ImpactCode.BLOCKED)
				return LaneUseIndication.MERGE_BOTH;
			if(left == ImpactCode.BLOCKED)
				return LaneUseIndication.MERGE_RIGHT;
			else
				return LaneUseIndication.MERGE_LEFT;
		} else
			return LaneUseIndication.LANE_CLOSED_AHEAD;
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
