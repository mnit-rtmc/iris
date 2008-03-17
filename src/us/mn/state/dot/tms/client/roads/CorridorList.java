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
import java.rmi.RemoteException;
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
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Roadway;
import us.mn.state.dot.tms.client.proxy.LocationProxy;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;
import us.mn.state.dot.tms.client.proxy.Vector;

/**
 * CorridorList is a graphical freeway corridor list.
 *
 * @author Douglas Lau
 */
public class CorridorList extends JPanel {

	/** Offset angle for default North map markers */
	static protected final double NORTH_ANGLE = Math.PI / 2;

	/** Roadway node handler */
	protected final R_NodeHandler handler;

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
	public CorridorList(R_NodeHandler h) {
		super(new GridBagLayout());
		handler = h;
		setBorder(BorderFactory.createTitledBorder(
			"Corridor Node List"));
		jlist.setCellRenderer(renderer);
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, jlist) {
			public void perform() throws RemoteException {
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
			public void perform() throws RemoteException {
				showPropertiesForm();
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
	static protected R_NodeProxy findNearest(Set<R_NodeProxy> node_s,
		R_NodeProxy end)
	{
		R_NodeProxy nearest = null;
		double distance = 0;
		for(R_NodeProxy proxy: node_s) {
			double d = proxy.distanceTo(end);
			if(nearest == null || d < distance) {
				nearest = proxy;
				distance = d;
			}
		}
		return nearest;
	}

	/** Link the nearest node */
	static protected void linkNearestNode(Set<R_NodeProxy> node_s,
		LinkedList<R_NodeProxy> node_l)
	{
		R_NodeProxy first = node_l.getFirst();
		R_NodeProxy last = node_l.getLast();
		R_NodeProxy fnear = findNearest(node_s, first);
		R_NodeProxy lnear = findNearest(node_s, last);
		if(first.distanceTo(fnear) < last.distanceTo(lnear)) {
			node_l.addFirst(fnear);
			node_s.remove(fnear);
		} else {
			node_l.addLast(lnear);
			node_s.remove(lnear);
		}
	}

	/** Check if a list of roadway nodes is reversed */
	static protected boolean isListReversed(R_NodeProxy first,
		R_NodeProxy last)
	{
		short dir = first.getLocation().getFreeDir();
		switch(dir) {
			case Roadway.NORTH:
				return first.loc.getTrueNorthing() <
					last.loc.getTrueNorthing();
			case Roadway.SOUTH:
				return first.loc.getTrueNorthing() >
					last.loc.getTrueNorthing();
			case Roadway.EAST:
				return first.loc.getTrueEasting() <
					last.loc.getTrueEasting();
			case Roadway.WEST:
				return first.loc.getTrueEasting() >
					last.loc.getTrueEasting();
		}
		return false;
	}

	/** Create a reversed list of roadway nodes */
	static protected List<R_NodeProxy> createReversed(
		LinkedList<R_NodeProxy> node_l)
	{
		LinkedList<R_NodeProxy> node_r = new LinkedList<R_NodeProxy>();
		for(R_NodeProxy proxy: node_l)
			node_r.addFirst(proxy);
		return node_r;
	}

	/** Reverse a list of roadway nodes if necessary */
	static protected List<R_NodeProxy> reversedListIfNecessary(
		LinkedList<R_NodeProxy> node_l)
	{
		if(node_l.size() > 1) {
			R_NodeProxy first = node_l.getFirst();
			R_NodeProxy last = node_l.getLast();
			if(isListReversed(first, last))
				return createReversed(node_l);
		}
		return node_l;
	}

	/** Create a sorted list of roadway nodes for one corridor */
	static protected List<R_NodeProxy> createSortedList(
		Set<R_NodeProxy> node_s)
	{
		LinkedList<R_NodeProxy> node_l = new LinkedList<R_NodeProxy>();
		Iterator<R_NodeProxy> it = node_s.iterator();
		if(it.hasNext()) {
			node_l.add(it.next());
			it.remove();
		}
		while(!node_s.isEmpty())
			linkNearestNode(node_s, node_l);
		return reversedListIfNecessary(node_l);
	}

	/** Set the tangent angles for all the roadway nodes in a list */
	static protected void setTangentAngles(List<R_NodeProxy> node_t) {
		LocationProxy loc, loc_a, loc_b;
		for(int i = 0; i < node_t.size(); i++) {
			if(i == 0)
				loc_a = node_t.get(0).getLocation();
			else
				loc_a = node_t.get(i - 1).getLocation();
			loc = node_t.get(i).getLocation();
			if(i == node_t.size() - 1)
				loc_b = node_t.get(i).getLocation();
			else
				loc_b = node_t.get(i + 1).getLocation();
			if(loc_a != loc_b) {
				Vector va = loc_a.getVector();
				Vector vb = loc_b.getVector();
				Vector a = va.subtract(vb);
				loc.setTangent(a.getAngle() - NORTH_ANGLE);
			}
		}
	}

	/** Create a list of roadway node renderers for one corridor */
	static protected List<R_NodeRenderer> createRendererList(
		Set<R_NodeProxy> node_s)
	{
		LinkedList<R_NodeRenderer> ren_l =
			new LinkedList<R_NodeRenderer>();
		Iterator<R_NodeProxy> it = node_s.iterator();
		while(it.hasNext()) {
			R_NodeProxy proxy = it.next();
			if(!proxy.hasLocation()) {
				ren_l.add(new R_NodeRenderer(proxy));
				it.remove();
			}
		}
		List<R_NodeProxy> node_t = createSortedList(node_s);
		setTangentAngles(node_t);
		R_NodeRenderer prev = null;
		for(R_NodeProxy proxy: node_t) {
			R_NodeRenderer r = new R_NodeRenderer(proxy);
			ren_l.add(r);
			if(prev != null)
				prev.setUpstream(r);
			prev = r;
		}
		return ren_l;
	}

	/** Set the corridor to display */
	public void setCorridor(Set<R_NodeProxy> node_s)
		throws RemoteException
	{
		r_nodes = createRendererList(node_s);
		nr_list = new DefaultListModel();
		for(R_NodeRenderer r: r_nodes)
			nr_list.addElement(r);
		jlist.setModel(nr_list);
		jlist.setSelectedIndex(0);
	}

	/** Get the selected roadway node */
	protected R_NodeProxy getSelectedNode() {
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
		R_NodeProxy proxy = getSelectedNode();
		if(proxy != null)
			handler.getSelectionModel().setSelected(proxy);
	}

	/** Show the properties form */
	protected void showPropertiesForm() throws RemoteException {
		R_NodeProxy proxy = getSelectedNode();
		if(proxy != null)
			proxy.showPropertiesForm(handler.getConnection());
	}

	/** Do the add button action */
	protected void doAddButton() throws Exception {
		R_Node r_node = handler.addNode();
System.out.println("R_Node OID: " + r_node.getOID());
	}

	/** Do the remove button action */
	protected void doRemoveButton() throws Exception {
		R_NodeProxy proxy = getSelectedNode();
		if(proxy != null)
			handler.removeNode(proxy);
	}
}
