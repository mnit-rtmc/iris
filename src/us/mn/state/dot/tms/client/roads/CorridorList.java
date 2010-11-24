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
import java.awt.geom.Point2D;
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
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.map.PointSelector;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.IrisClient;

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

	/** Client frame */
	protected final IrisClient client;

	/** Roadway node type cache */
	protected final TypeCache<R_Node> r_nodes;

	/** Location type cache */
	protected final TypeCache<GeoLoc> geo_locs;

	/** Roadway corridor */
	protected CorridorBase corridor;

	/** List component for nodes */
	protected final JList n_list = new JList();

	/** Roadway node list model */
	protected R_NodeListModel n_model = new R_NodeListModel();

	/** Button to add a new roadway node */
	protected JButton add_btn = new JButton("Add");

	/** Button to edit the currently selected roadway node */
	protected JButton edit_btn = new JButton("Edit");

	/** Button the shift the selected node left */
	protected JButton lf_btn = new JButton("<-");

	/** Button the shift the selected node right */
	protected JButton rt_btn = new JButton("->");

	/** Button to remove the currently selected roadway node */
	protected JButton remove_btn = new JButton("Remove");

	/** Create a corridor list */
	public CorridorList(R_NodeManager m, R_NodeCreator c, IrisClient ic) {
		super(new GridBagLayout());
		manager = m;
		creator = c;
		client = ic;
		r_nodes = creator.getR_Nodes();
		geo_locs = creator.getGeoLocs();
		setBorder(BorderFactory.createTitledBorder(
			"Corridor Node List"));
		n_list.setCellRenderer(new R_NodeCellRenderer());
		n_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane(n_list);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridy = 0;
		bag.gridwidth = 5;
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
		add(add_btn, bag);
		bag.gridx = GridBagConstraints.RELATIVE;
		add(edit_btn, bag);
		add(lf_btn, bag);
		add(rt_btn, bag);
		add(remove_btn, bag);
		r_nodes.addProxyListener(this);
		geo_locs.addProxyListener(new ProxyListener<GeoLoc>() {
			public void proxyAdded(GeoLoc proxy) { }
			public void enumerationComplete() { }
			public void proxyRemoved(GeoLoc proxy) { }
			public void proxyChanged(GeoLoc proxy, String a) {
				geoLocChanged(proxy, a);
			}
		});
		createJobs();
	}

	/** Create the jobs */
	protected void createJobs() {
		new ListSelectionJob(this, n_list) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					updateNodeSelection();
			}
		};
		n_list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				if(ev.getClickCount() == 2)
					edit_btn.doClick();
			}
		});
		new ActionJob(this, add_btn) {
			public void perform() {
				doAddButton();
			}
		};
		new ActionJob(this, edit_btn) {
			public void perform() {
				manager.showPropertiesForm();
			}
		};
		new ActionJob(this, lf_btn) {
			public void perform() {
				doShiftLeft();
			}
		};
		new ActionJob(this, rt_btn) {
			public void perform() {
				doShiftRight();
			}
		};
		new ActionJob(this, remove_btn) {
			public void perform() {
				doRemoveButton();
			}
		};
	}

	/** Shift the selected r_node to the left */
	protected void doShiftLeft() {
		R_Node proxy = getSelectedNode();
		int shift = proxy.getShift();
		if(shift > 0)
			proxy.setShift(shift - 1);
	}

	/** Shift the selected r_node to the right */
	protected void doShiftRight() {
		R_Node proxy = getSelectedNode();
		int shift = proxy.getShift();
		if(shift < 12)
			proxy.setShift(shift + 1);
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
			n_model.updateItem(proxy);
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
		//       still be found by checkNodeList(GeoLoc).
		R_Node proxy = r_nodes.lookupObject(loc.getName());
		return (proxy != null && manager.checkCorridor(proxy)) ||
			checkNodeList(loc);
	}

	/** Check the node list for a geo location. This is needed in case
	 * the geo location has changed to a different corridor. */
	protected boolean checkNodeList(GeoLoc loc) {
		ListModel lm = n_list.getModel();
		for(int i = 0; i < lm.getSize(); i++) {
			Object obj = lm.getElementAt(i);
			if(obj instanceof R_NodeModel) {
				R_NodeModel m = (R_NodeModel)obj;
				if(m.r_node.getGeoLoc() == loc)
					return true;
			}
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
		n_model = createNodeList(node_s);
		n_list.setModel(n_model);
	}

	/** Create a list model of roadway node models for one corridor */
	protected R_NodeListModel createNodeList(Set<R_Node> node_s) {
		LinkedList<R_NodeModel> nodes = new LinkedList<R_NodeModel>();
		List<R_NodeModel> no_loc = createNullLocList(node_s);
		corridor = createCorridor(node_s);
		if(corridor != null) {
			R_NodeModel prev = null;
			for(R_Node proxy: corridor) {
				R_NodeModel mdl = new R_NodeModel(proxy, prev);
				nodes.add(0, mdl);
				prev = mdl;
			}
		}
		nodes.addAll(0, no_loc);
		return new R_NodeListModel(nodes);
	}

	/** Create a list of r_node models with null locations.  The r_nodes
	 * are then removed from the set passed in.
	 * @param node_s Set of nodes on the corridor.
	 * @return List of r_node models with null location. */
	protected List<R_NodeModel> createNullLocList(Set<R_Node> node_s) {
		LinkedList<R_NodeModel> no_loc =
			new LinkedList<R_NodeModel>();
		Iterator<R_Node> it = node_s.iterator();
		while(it.hasNext()) {
			R_Node proxy = it.next();
			if(GeoLocHelper.isNull(proxy.getGeoLoc())) {
				no_loc.add(new R_NodeModel(proxy, null));
				it.remove();
			}
		}
		return no_loc;
	}

	/** Get the selected roadway node */
	protected R_Node getSelectedNode() {
		Object sel = n_list.getSelectedValue();
		if(sel instanceof R_NodeModel)
			return ((R_NodeModel)sel).r_node;
		else
			return null;
	}

	/** Update the roadway node selection */
	protected void updateNodeSelection() {
		R_Node proxy = getSelectedNode();
		if(proxy != null)
			manager.getSelectionModel().setSelected(proxy);
		edit_btn.setEnabled(proxy != null);
		lf_btn.setEnabled(proxy != null);
		rt_btn.setEnabled(proxy != null);
		remove_btn.setEnabled(proxy != null);
	}

	/** Do the add button action */
	protected void doAddButton() {
		client.setPointSelector(new PointSelector() {
			public void selectPoint(Point2D p) {
				createNode(corridor, p);
			}
		});
	}

	/** Create a new node at a specified point */
	protected void createNode(CorridorBase c, Point2D p) {
		int e = (int)p.getX();
		int n = (int)p.getY();
		if(c != null) {
			int lanes = 2;
			int shift = 4;
			R_NodeModel mdl = findModel(c, e, n);
			if(mdl != null) {
				shift = mdl.getDownstreamLane(false);
				lanes = shift - mdl.getDownstreamLane(true);
			}
			creator.create(c.getRoadway(), c.getRoadDir(), e, n,
				lanes, shift);
		} else
			creator.create(e, n);
		client.setPointSelector(null);
		add_btn.setEnabled(true);
	}

	/** Find an r_node model near a point */
	protected R_NodeModel findModel(CorridorBase c, int e, int n) {
		R_Node found = c.findLastBefore(e, n);
		for(int i = 0; i < n_model.getSize(); i++) {
			Object elem = n_model.get(i);
			if(elem instanceof R_NodeModel) {
				R_NodeModel mdl = (R_NodeModel)elem;
				if(mdl.r_node == found)
					return mdl;
			}
		}
		return null;
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
