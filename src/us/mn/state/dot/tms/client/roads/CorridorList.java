/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.client.sonar.MapGeoLoc;

/**
 * CorridorList is a graphical freeway corridor list.
 *
 * @author Douglas Lau
 */
public class CorridorList extends JPanel {

	/** Offset angle for default North map markers */
	static protected final double NORTH_ANGLE = Math.PI / 2;

	/** Roadway node manager */
	protected final R_NodeManager manager;

	/** Renderer for painting roadway nodes */
	protected final R_NodeCellRenderer renderer =
		new R_NodeCellRenderer();

	/** List component */
	protected final JList jlist = new JList();

	/** Roadway node renderer list */
	protected List<R_NodeRenderer> r_nodes;

	/** Roadway node renderer list model */
	protected DefaultListModel nr_list;

	/** Button to add a new roadway node */
	protected JButton abutton = new JButton("Add");

	/** Button to edit the currently selected roadway node */
	protected JButton ebutton = new JButton("Edit");

	/** Button to remove the currently selected roadway node */
	protected JButton rbutton = new JButton("Remove");

	/** Create a corridor list */
	public CorridorList(R_NodeManager m) {
		super(new GridBagLayout());
		manager = m;
		setBorder(BorderFactory.createTitledBorder(
			"Corridor Node List"));
		jlist.setCellRenderer(renderer);
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, jlist) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					updateNodeSelection();
			}
		};
		jlist.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if(ev.getClickCount() == 2)
					ebutton.doClick();
			}
		});
		new ActionJob(this, abutton) {
			public void perform() throws Exception {
				doAddButton();
			}
		};
		new ActionJob(this, ebutton) {
			public void perform() {
				manager.showPropertiesForm();
			}
		};
		new ActionJob(this, rbutton) {
			public void perform() throws Exception {
				doRemoveButton();
			}
		};
		JScrollPane scroll = new JScrollPane(jlist);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridy = 0;
		bag.gridwidth = 3;
		bag.insets = new Insets(2, 4, 2, 4);
		bag.fill = GridBagConstraints.BOTH;
		bag.weightx = 1;
		bag.weighty = 1;
		add(scroll, bag);
		bag.gridy = 1;
		bag.gridwidth = 1;
		bag.fill = GridBagConstraints.NONE;
		bag.weightx = 0.1f;
		bag.weighty = 0;
		add(abutton, bag);
		bag.gridx = 1;
		add(ebutton, bag);
		bag.gridx = 2;
		add(rbutton, bag);
	}

	/** Find the nearest node to the given node */
	static protected R_Node findNearest(Set<R_Node> node_s, R_Node end) {
		R_Node nearest = null;
		double distance = 0;
		for(R_Node proxy: node_s) {
			double d = GeoLocHelper.metersTo(proxy.getGeoLoc(),
				end.getGeoLoc());
			if(nearest == null || d < distance) {
				nearest = proxy;
				distance = d;
			}
		}
		return nearest;
	}

	/** Link the nearest node */
	static protected void linkNearestNode(Set<R_Node> node_s,
		LinkedList<R_Node> node_l)
	{
		R_Node first = node_l.getFirst();
		R_Node last = node_l.getLast();
		R_Node fnear = findNearest(node_s, first);
		R_Node lnear = findNearest(node_s, last);
		double df = GeoLocHelper.metersTo(first.getGeoLoc(),
			fnear.getGeoLoc());
		double dl = GeoLocHelper.metersTo(last.getGeoLoc(),
			lnear.getGeoLoc());
		if(df < dl) {
			node_l.addFirst(fnear);
			node_s.remove(fnear);
		} else {
			node_l.addLast(lnear);
			node_s.remove(lnear);
		}
	}

	/** Compare the Northing of two R_Nodes */
	static protected boolean compareNorthing(R_Node n0, R_Node n1) {
		return GeoLocHelper.getTrueNorthing(n0.getGeoLoc()) <
			GeoLocHelper.getTrueNorthing(n1.getGeoLoc());
	}

	/** Compare the Easting of two R_Nodes */
	static protected boolean compareEasting(R_Node n0, R_Node n1) {
		return GeoLocHelper.getTrueEasting(n0.getGeoLoc()) <
			GeoLocHelper.getTrueEasting(n1.getGeoLoc());
	}

	/** Check if a list of roadway nodes is reversed */
	static protected boolean isListReversed(R_Node first, R_Node last) {
		short dir = first.getGeoLoc().getFreeDir();
		switch(dir) {
			case Road.NORTH:
				return compareNorthing(first, last);
			case Road.SOUTH:
				return compareNorthing(last, first);
			case Road.EAST:
				return compareEasting(first, last);
			case Road.WEST:
				return compareEasting(last, first);
		}
		return false;
	}

	/** Create a reversed list of roadway nodes */
	static protected List<R_Node> createReversed(LinkedList<R_Node> node_l)
	{
		LinkedList<R_Node> node_r = new LinkedList<R_Node>();
		for(R_Node proxy: node_l)
			node_r.addFirst(proxy);
		return node_r;
	}

	/** Reverse a list of roadway nodes if necessary */
	static protected List<R_Node> reversedListIfNecessary(
		LinkedList<R_Node> node_l)
	{
		if(node_l.size() > 1) {
			R_Node first = node_l.getFirst();
			R_Node last = node_l.getLast();
			if(isListReversed(first, last))
				return createReversed(node_l);
		}
		return node_l;
	}

	/** Create a sorted list of roadway nodes for one corridor */
	static protected List<R_Node> createSortedList(Set<R_Node> node_s) {
		LinkedList<R_Node> node_l = new LinkedList<R_Node>();
		Iterator<R_Node> it = node_s.iterator();
		if(it.hasNext()) {
			node_l.add(it.next());
			it.remove();
		}
		while(!node_s.isEmpty())
			linkNearestNode(node_s, node_l);
		return reversedListIfNecessary(node_l);
	}

	/** Set the corridor to display */
	public void setCorridor(Set<R_Node> node_s) {
		r_nodes = createRendererList(node_s);
		nr_list = new DefaultListModel();
		for(R_NodeRenderer r: r_nodes)
			nr_list.addElement(r);
		jlist.setModel(nr_list);
		jlist.setSelectedIndex(0);
	}

	/** Create a list of roadway node renderers for one corridor */
	protected List<R_NodeRenderer> createRendererList(Set<R_Node> node_s) {
		LinkedList<R_NodeRenderer> ren_l =
			new LinkedList<R_NodeRenderer>();
		Iterator<R_Node> it = node_s.iterator();
		while(it.hasNext()) {
			R_Node proxy = it.next();
			if(GeoLocHelper.isNull(proxy.getGeoLoc())) {
				ren_l.add(new R_NodeRenderer(proxy));
				it.remove();
			}
		}
		List<R_Node> node_t = createSortedList(node_s);
		setTangentAngles(node_t);
		R_NodeRenderer prev = null;
		for(R_Node proxy: node_t) {
			R_NodeRenderer r = new R_NodeRenderer(proxy);
			ren_l.add(r);
			if(prev != null)
				prev.setUpstream(r);
			prev = r;
		}
		return ren_l;
	}

	/** Set the tangent angles for all the roadway nodes in a list */
	protected void setTangentAngles(List<R_Node> node_t) {
		MapGeoLoc loc, loc_a, loc_b;
		for(int i = 0; i < node_t.size(); i++) {
			if(i == 0)
				loc_a = manager.findGeoLoc(node_t.get(0));
			else
				loc_a = manager.findGeoLoc(node_t.get(i - 1));
			loc = manager.findGeoLoc(node_t.get(i));
			if(i == node_t.size() - 1)
				loc_b = manager.findGeoLoc(node_t.get(i));
			else
				loc_b = manager.findGeoLoc(node_t.get(i + 1));
			if(loc_a != loc_b) {
				Vector va = Vector.create(loc_a.getGeoLoc());
				Vector vb = Vector.create(loc_b.getGeoLoc());
				Vector a = va.subtract(vb);
				loc.setTangent(a.getAngle() - NORTH_ANGLE);
			}
		}
	}

	/** Get the selected roadway node */
	protected R_Node getSelectedNode() {
		int index = jlist.getSelectedIndex();
		try {
			return r_nodes.get(index).getProxy();
		}
		catch(IndexOutOfBoundsException e) {
			return null;
		}
	}

	/** Update the roadway node selection */
	protected void updateNodeSelection() {
		R_Node proxy = getSelectedNode();
		if(proxy != null)
			manager.getSelectionModel().setSelected(proxy);
	}

	/** Do the add button action */
	protected void doAddButton() {
		// FIXME
	}

	/** Do the remove button action */
	protected void doRemoveButton() {
		// FIXME
	}
}
