/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;

/**
 * CorridorList is a graphical roadway corridor list.
 *
 * @author Douglas Lau
 */
public class CorridorList extends JPanel implements ProxyListener<R_Node> {

	/** Offset angle for default North map markers */
	static protected final double NORTH_ANGLE = Math.PI / 2;

	/** Roadway node manager */
	protected final R_NodeManager manager;

	/** Roadway node creator */
	protected final R_NodeCreator creator;

	/** Roadway node type cache */
	protected final TypeCache<R_Node> r_nodes;

	/** Location type cache */
	protected final TypeCache<GeoLoc> geo_locs;

	/** Renderer for painting roadway nodes */
	protected final R_NodeCellRenderer renderer =
		new R_NodeCellRenderer();

	/** List component */
	protected final JList jlist = new JList();

	/** Roadway corridor */
	protected CorridorBase corridor;

	/** Roadway node renderer list */
	protected List<R_NodeRenderer> r_list =new LinkedList<R_NodeRenderer>();

	/** Roadway node renderer list model */
	protected R_NodeListModel nr_list = new R_NodeListModel();

	/** Button to add a new roadway node */
	protected JButton abutton = new JButton("Add");

	/** Button to edit the currently selected roadway node */
	protected JButton ebutton = new JButton("Edit");

	/** Button to remove the currently selected roadway node */
	protected JButton rbutton = new JButton("Remove");

	/** Create a corridor list */
	public CorridorList(R_NodeManager m, R_NodeCreator c) {
		super(new GridBagLayout());
		manager = m;
		creator = c;
		r_nodes = creator.getR_Nodes();
		geo_locs = creator.getGeoLocs();
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
		r_nodes.addProxyListener(this);
		geo_locs.addProxyListener(new ProxyListener<GeoLoc>() {
			public void proxyAdded(GeoLoc proxy) { }
			public void enumerationComplete() { }
			public void proxyRemoved(GeoLoc proxy) { }
			public void proxyChanged(GeoLoc proxy, String a) {
				geoLocChanged(proxy, a);
			}
		});
	}

	/** Enumeration complete flag */
	protected boolean complete;

	/** Called when a proxy has been added */
	public void proxyAdded(final R_Node proxy) {
		// Don't hog the SONAR TaskProcessor thread
		if(complete && manager.checkCorridor(proxy)) {
			new AbstractJob() {
				public void perform() {
					updateListModel();
				}
			}.addToScheduler();
		}
	}

	/** Called when proxy enumeration is complete */
	public void enumerationComplete() {
		complete = true;
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				updateListModel();
			}
		}.addToScheduler();
	}

	/** Called when a proxy has been removed */
	public void proxyRemoved(R_Node proxy) {
		if(manager.checkCorridor(proxy)) {
			// Don't hog the SONAR TaskProcessor thread
			new AbstractJob() {
				public void perform() {
					updateListModel();
				}
			}.addToScheduler();
		}
	}

	/** Called when a proxy attribute has changed */
	public void proxyChanged(R_Node proxy, String a) {
		if(manager.checkCorridor(proxy))
			nr_list.updateItem(proxy);
	}

	/** Called when a GeoLoc proxy attribute has changed */
	protected void geoLocChanged(final GeoLoc loc, String a) {
		// Don't hog the SONAR TaskProcessor thread
		new AbstractJob() {
			public void perform() {
				if(checkCorridor(loc))
					updateListModel();
			}
		}.addToScheduler();
	}

	/** Check the corridor for a geo location */
	protected boolean checkCorridor(GeoLoc loc) {
		// NOTE: The fast path assumes that GeoLoc name matches R_Node
		//       name.  If that is not the case, the GeoLoc should
		//       still be found by checkRenderers(GeoLoc).
		R_Node proxy = r_nodes.lookupObject(loc.getName());
		return (proxy != null && manager.checkCorridor(proxy)) ||
			checkRenderers(loc);
	}

	/** Check the renderer list for a geo location. This is needed in case
	 * the geo location has changed to a different corridor. */
	protected boolean checkRenderers(GeoLoc loc) {
		for(R_NodeRenderer r: r_list) {
			if(r.getProxy().getGeoLoc() == loc)
				return true;
		}
		return false;
	}

	/** Create a sorted list of roadway nodes for one corridor */
	static protected CorridorBase createCorridor(Set<R_Node> node_s) {
		GeoLoc loc = getCorridorLoc(node_s);
		if(loc != null) {
			CorridorBase c = new CorridorBase(loc);
			for(R_Node n: node_s)
				c.addNode(n);
			c.arrangeNodes();
			return c;
		} else
			return null;
	}

	/** Get a location for a corridor */
	static protected GeoLoc getCorridorLoc(Set<R_Node> node_s) {
		Iterator<R_Node> it = node_s.iterator();
		if(it.hasNext()) {
			R_Node n = it.next();
			return n.getGeoLoc();
		} else
			return null;
	}

	/** Update the corridor list model */
	public void updateListModel() {
		Set<R_Node> node_s = manager.createSet();
		r_list = createRendererList(node_s);
		nr_list = new R_NodeListModel();
		for(R_NodeRenderer r: r_list)
			nr_list.addElement(r);
		jlist.setModel(nr_list);
	}

	/** Create a list of roadway node renderers for one corridor */
	protected List<R_NodeRenderer> createRendererList(Set<R_Node> node_s) {
		LinkedList<R_NodeRenderer> ren_l =
			new LinkedList<R_NodeRenderer>();
		List<R_NodeRenderer> no_loc = createNullLocList(node_s);
		corridor = createCorridor(node_s);
		List<R_Node> node_t = getSortedList();
		R_NodeRenderer prev = null;
		for(R_Node proxy: node_t) {
			R_NodeRenderer r = new R_NodeRenderer(proxy);
			r.setUpstream(prev);
			ren_l.add(0, r);
			prev = r;
		}
		ren_l.addAll(0, no_loc);
		return ren_l;
	}

	/** Create a list of r_node renderers with null locations.  The r_nodes
	 * are then removed from the set passed in.
	 * @param node_s Set of nodes on the corridor.
	 * @return List of renderers with null location. */
	protected List<R_NodeRenderer> createNullLocList(Set<R_Node> node_s) {
		LinkedList<R_NodeRenderer> no_loc =
			new LinkedList<R_NodeRenderer>();
		Iterator<R_Node> it = node_s.iterator();
		while(it.hasNext()) {
			R_Node proxy = it.next();
			if(GeoLocHelper.isNull(proxy.getGeoLoc())) {
				no_loc.add(new R_NodeRenderer(proxy));
				it.remove();
			}
		}
		return no_loc;
	}

	/** Get a sorted list of roadway nodes for the selected corridor */
	protected List<R_Node> getSortedList() {
		if(corridor != null)
			return corridor.getNodes();
		else
			return new LinkedList<R_Node>();
	}

	/** Get the selected roadway node */
	protected R_Node getSelectedNode() {
		int index = jlist.getSelectedIndex();
		try {
			return r_list.get(index).getProxy();
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
		ebutton.setEnabled(proxy != null);
		rbutton.setEnabled(proxy != null);
	}

	/** Do the add button action */
	protected void doAddButton() {
		CorridorBase c = corridor;
		if(c != null)
			creator.create(c.getRoadway(), c.getRoadDir());
		else
			creator.create();
	}

	/** Do the remove button action */
	protected void doRemoveButton() {
		R_Node proxy = getSelectedNode();
		if(proxy != null) {
			GeoLoc loc = proxy.getGeoLoc();
			proxy.destroy();
			loc.destroy();
		}
	}
}
