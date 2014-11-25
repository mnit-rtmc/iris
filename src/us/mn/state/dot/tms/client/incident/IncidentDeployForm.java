/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LaneConfiguration;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.lcs.LCSArrayCellRenderer;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.I18N;

/**
 * IncidentDeployForm is a dialog for deploying devices for an incident.
 *
 * @author Douglas Lau
 */
public class IncidentDeployForm extends SonarObjectForm<Incident> {

	/** Incident manager */
	private final IncidentManager manager;

	/** Incident deployment policy */
	private final IncidentPolicy policy;

	/** Mapping of LCS array names to proposed indications */
	private final HashMap<String, Integer []> indications =
		new HashMap<String, Integer []>();

	/** Model for deployment list */
	private final DefaultListModel model = new DefaultListModel();

	/** List of deployments for the incident */
	private final JList list = new JList(model);

	/** Action to send device messages */
	private final IAction send = new IAction("incident.send") {
		protected void doActionPerformed(ActionEvent e) {
			sendIndications();
			close(session.getDesktop());
		}
	};

	/** Create a new incident deploy form */
	public IncidentDeployForm(Session s, Incident inc, IncidentManager man){
		super(I18N.get("incident") + ": ", s, inc);
		manager = man;
		policy = new IncidentPolicy(inc);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<Incident> getTypeCache() {
		return state.getIncidents();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		list.setCellRenderer(new LCSArrayCellRenderer(
			session.getLCSArrayManager())
		{
			@Override protected User getUser(LCSArray lcs_array) {
				return session.getUser();
			}
			@Override protected Integer[] getIndications(
				LCSArray lcs_array)
			{
				return indications.get(lcs_array.getName());
			}
		});
		populateList();
		add(createPanel());
		super.initialize();
	}

	/** Populate the list model with LCS array indications to display */
	private void populateList() {
		IncidentLoc loc = new IncidentLoc(proxy);
		CorridorBase cb = manager.lookupCorridor(loc);
		if(cb != null) {
			Float mp = cb.calculateMilePoint(loc);
			if(mp != null)
				populateList(cb, mp);
		}
	}

	/** Populate the list model with LCS array indications to display */
	private void populateList(CorridorBase cb, float mp) {
		TreeMap<Distance, LCSArray> upstream = findUpstream(cb, mp);
		LaneConfiguration config = cb.laneConfiguration(
			getWgs84Position());
		int shift = config.leftShift;
		for(Distance up: upstream.keySet()) {
			LCSArray lcs_array = upstream.get(up);
			int l_shift = lcs_array.getShift() - shift;
			Integer[] ind = policy.createIndications(up, lcs_array,
				l_shift, config.getLanes());
			if(shouldDeploy(ind)) {
				model.addElement(lcs_array);
				indications.put(lcs_array.getName(), ind);
			}
		}
	}

	/** Get Position in WGS84 */
	private Position getWgs84Position() {
		return new Position(proxy.getLat(), proxy.getLon());
	}

	/** Find all LCS arrays upstream of a given point on a corridor */
	private TreeMap<Distance, LCSArray> findUpstream(CorridorBase cb,
		float mp)
	{
		TreeMap<Distance, LCSArray> upstream =
			new TreeMap<Distance, LCSArray>();
		Iterator<LCSArray> lit = LCSArrayHelper.iterator();
		while(lit.hasNext()) {
			LCSArray lcs_array = lit.next();
			GeoLoc loc = LCSArrayHelper.lookupGeoLoc(lcs_array);
			if(loc != null && loc.getRoadway() == cb.getRoadway() &&
			   loc.getRoadDir() == cb.getRoadDir())
			{
				Float lp = cb.calculateMilePoint(loc);
				if(lp != null && mp > lp) {
					Distance up = new Distance(mp - lp,
						Distance.Units.MILES);
					upstream.put(up, lcs_array);
				}
			}
		}
		return upstream;
	}

	/** Check if a set of indications should be deployed */
	static private boolean shouldDeploy(Integer[] ind) {
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
	private JPanel createPanel() {
		JLabel lbl = new JLabel();
		lbl.setHorizontalTextPosition(SwingConstants.TRAILING);
		lbl.setText(manager.getDescription(proxy));
		lbl.setIcon(manager.getIcon(proxy));
		JButton btn = new JButton(send);
		send.setEnabled(model.getSize() > 0);
		btn.setEnabled(model.getSize() > 0);
		IPanel p = new IPanel();
		p.add(lbl, Stretch.CENTER);
		p.add("incident.deploy.proposed");
		p.add(list, Stretch.FULL);
		p.add(btn, Stretch.RIGHT);
		return p;
	}

	/** Send new indications to LCS arrays for the incident */
	private void sendIndications() {
		for(int i = 0; i < model.getSize(); i++) {
			Object e = model.getElementAt(i);
			if(e instanceof LCSArray)
				sendIndications((LCSArray)e);
		}
	}

	/** Send new indications to the specified LCS array */
	private void sendIndications(LCSArray lcs_array) {
		Integer[] ind = indications.get(lcs_array.getName());
		if(ind != null) {
			lcs_array.setOwnerNext(session.getUser());
			lcs_array.setIndicationsNext(ind);
		}
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		if("cleared".equals(a) && proxy.getCleared())
			close(session.getDesktop());
	}
}
