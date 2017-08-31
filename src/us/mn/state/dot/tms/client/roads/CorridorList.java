/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import static us.mn.state.dot.tms.R_Node.MID_SHIFT;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.map.PointSelector;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.client.widget.IWorker;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The corridor list is a widget which allows selecting a corridor from a
 * combo box and displaying all r_nodes of the selected corridor in a list.
 *
 * @author Douglas Lau
 */
public class CorridorList extends IPanel {

	/** Create a sorted list of roadway nodes for one corridor */
	static private CorridorBase<R_Node> createCorridor(Set<R_Node> node_s) {
		GeoLoc loc = getCorridorLoc(node_s);
		if (loc != null) {
			CorridorBase<R_Node> c = new CorridorBase<R_Node>(loc);
			for (R_Node n: node_s)
				c.addNode(n);
			c.arrangeNodes();
			return c;
		} else
			return null;
	}

	/** Get a location for a corridor */
	static private GeoLoc getCorridorLoc(Set<R_Node> node_s) {
		Iterator<R_Node> it = node_s.iterator();
		if (it.hasNext()) {
			R_Node n = it.next();
			return n.getGeoLoc();
		} else
			return null;
	}

	/** User session */
	private final Session session;

	/** Roadway node manager */
	private final R_NodeManager manager;

	/** Selected r_node panel */
	private final R_NodePanel panel;

	/** Roadway node creator */
	private final R_NodeCreator creator;

	/** Client frame */
	private final IrisClient client;

	/** Roadway node type cache */
	private final TypeCache<R_Node> r_nodes;

	/** Location type cache */
	private final TypeCache<GeoLoc> geo_locs;

	/** Selected roadway corridor */
	private CorridorBase<R_Node> corridor;

	/** Corridor action */
	private final IAction corr_act = new IAction("r_node.corridor") {
		@SuppressWarnings("unchecked")
		protected void doActionPerformed(ActionEvent e) {
			Object s = corridor_cbx.getSelectedItem();
			if (s instanceof CorridorBase)
				setCorridor((CorridorBase<R_Node>) s);
			else
				setCorridor(null);
		}
	};

	/** Combo box to select a roadway corridor */
	private final JComboBox<CorridorBase<R_Node>> corridor_cbx =
		new JComboBox<CorridorBase<R_Node>>();

	/** Action to add a new roadway node */
	private final IAction add_node = new IAction("r_node.add") {
		protected void doActionPerformed(ActionEvent e) {
			doAddNode();
		}
	};

	/** Action to delete the currently selected roadway node */
	private final IAction delete_node = new IAction("r_node.delete") {
		protected void doActionPerformed(ActionEvent e) {
			doDeleteNode();
		}
	};

	/** R_Node selection model */
	private final ProxySelectionModel<R_Node> sel_mdl;

	/** List component for nodes */
	private final JList<R_NodeModel> n_list = new JList<R_NodeModel>();

	/** Roadway node list model */
	private R_NodeListModel node_mdl = new R_NodeListModel();

	/** R_Node list selection model */
	private R_NodeListSelectionModel list_sel_mdl;

	/** R_Node selection listener */
	private final ProxySelectionListener sel_lsnr =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			updateNodeSelection(sel_mdl.getSingleSelection());
		}
	};

	/** Listener for r_node changes */
	private final ProxyListener<R_Node> node_lsnr =
		new ProxyListener<R_Node>()
	{
		private boolean enumerated = false;
		public void proxyAdded(R_Node n) {
			if (enumerated)
				nodeAdded(n);
		}
		public void enumerationComplete() {
			enumerated = true;
			updateListModel();
		}
		public void proxyRemoved(R_Node n) {
			nodeRemoved(n);
		}
		public void proxyChanged(R_Node n, String a) {
			nodeChanged(n, a);
		}
	};

	/** Listener for geo_loc changes */
	private final ProxyListener<GeoLoc> loc_lsnr =
		new ProxyListener<GeoLoc>()
	{
		public void proxyAdded(GeoLoc n) { }
		public void enumerationComplete() { }
		public void proxyRemoved(GeoLoc n) { }
		public void proxyChanged(GeoLoc n, String a) {
			geoLocChanged(n, a);
		}
	};

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			panel.updateEditMode();
			updateButtonPanel();
		}
	};

	/** Create a new corridor list */
	public CorridorList(Session s, R_NodeManager m, R_NodePanel p) {
		session = s;
		manager = m;
		panel = p;
		creator = new R_NodeCreator(s);
		client = s.getDesktop().client;
		r_nodes = manager.getCache();
		geo_locs = creator.getGeoLocs();
		corridor_cbx.setAction(corr_act);
		corridor_cbx.setModel(manager.getCorridorModel());
		sel_mdl = manager.getSelectionModel();
		n_list.setCellRenderer(new R_NodeCellRenderer());
		n_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setBorder(BorderFactory.createTitledBorder(
			I18N.get("r_node.corridor.selected")));
	}

	/** Initialize the corridor list */
	public void initialize() {
		add("r_node.corridor");
		add(corridor_cbx, Stretch.SOME);
		add("r_node");
		add(new JButton(add_node), Stretch.NONE);
		add(new JButton(delete_node), Stretch.LAST);
		add(n_list, Stretch.FULL);
		r_nodes.addProxyListener(node_lsnr);
		geo_locs.addProxyListener(loc_lsnr);
		sel_mdl.addProxySelectionListener(sel_lsnr);
		createJobs();
		updateNodeSelection(null);
		updateButtonPanel();
		session.addEditModeListener(edit_lsnr);
	}

	/** Create the jobs */
	private void createJobs() {
		// no jobs
	}

	/** Set a new selected corridor */
	private void setCorridor(CorridorBase<R_Node> c) {
		client.setPointSelector(null);
		corridor = c;
		updateListModel();
		manager.updateExtent();
	}

	/** Dispose of the corridor chooser */
	public void dispose() {
		session.removeEditModeListener(edit_lsnr);
		sel_mdl.removeProxySelectionListener(sel_lsnr);
		geo_locs.removeProxyListener(loc_lsnr);
		r_nodes.removeProxyListener(node_lsnr);
		removeAll();
		if (list_sel_mdl != null) {
			list_sel_mdl.dispose();
			list_sel_mdl = null;
		}
	}

	/** Called when an r_node has been added */
	private void nodeAdded(R_Node n) {
		if (checkCorridor(n))
			updateListModel();
	}

	/** Called when an r_node has been removed */
	private void nodeRemoved(R_Node n) {
		if (checkCorridor(n))
			updateListModel();
	}

	/** Called when an r_node attribute has changed */
	private void nodeChanged(final R_Node n, String a) {
		if (checkCorridor(n)) {
			if (a.equals("abandoned"))
				updateListModel();
			else {
				runSwing(new Runnable() {
					public void run() {
						node_mdl.updateItem(n);
					}
				});
			}
		}
	}

	/** Called when a GeoLoc proxy attribute has changed */
	private void geoLocChanged(final GeoLoc loc, String a) {
		IWorker<Boolean> worker = new IWorker<Boolean>() {
			@Override
			public Boolean doInBackground() {
				return checkCorridor(loc);
			}
			@Override
			public void done() {
				Boolean c = getResult();
				if (c != null && c)
					updateListModel();
			}
		};
		worker.execute();
	}

	/** Check the corridor for a geo location */
	private boolean checkCorridor(GeoLoc loc) {
		// NOTE: The fast path assumes that GeoLoc name matches R_Node
		//       name.  If that is not the case, the GeoLoc should
		//       still be found by checkNodeList(GeoLoc).
		return checkCorridor(loc.getName()) || checkNodeList(loc);
	}

	/** Check the corridor for an r_node with the given name */
	private boolean checkCorridor(String name) {
		R_Node n = r_nodes.lookupObject(name);
		return n != null && checkCorridor(n);
	}

	/** Check the corridor of an r_node */
	private boolean checkCorridor(R_Node n) {
		return checkCorridor(corridor, n.getGeoLoc());
	}

	/** Check if an r_node is on the specified corridor */
	private boolean checkCorridor(CorridorBase<R_Node> cb, GeoLoc loc) {
		if (cb == null)
			return loc != null && loc.getRoadway() == null;
		else {
			return loc != null &&
			       cb.getRoadway() == loc.getRoadway() &&
			       cb.getRoadDir() == loc.getRoadDir();
		}
	}

	/** Check the node list for a geo location. This is needed in case
	 * the geo location has changed to a different corridor. */
	private boolean checkNodeList(GeoLoc loc) {
		R_NodeListModel mdl = node_mdl;
		for (int i = 0; i < mdl.getSize(); i++) {
			Object obj = mdl.get(i);
			if (obj instanceof R_NodeModel) {
				R_NodeModel m = (R_NodeModel)obj;
				if (m.r_node.getGeoLoc() == loc)
					return true;
			}
		}
		return false;
	}

	/** Update the corridor list model */
	private void updateListModel() {
		IWorker<List<R_NodeModel>> worker =
			new IWorker<List<R_NodeModel>>()
		{
			@Override
			public List<R_NodeModel> doInBackground() {
				return createNodeList();
			}
			@Override
			public void done() {
				List<R_NodeModel> nodes = getResult();
				if (nodes != null)
					setListModel(nodes);
			}
		};
		worker.execute();
	}

	/** Create a list model of roadway node models for one corridor */
	private List<R_NodeModel> createNodeList() {
		Set<R_Node> node_s = createSet();
		List<R_NodeModel> no_loc = createNullLocList(node_s);
		LinkedList<R_NodeModel> nodes = new LinkedList<R_NodeModel>();
		CorridorBase<R_Node> c = createCorridor(node_s);
		if (c != null) {
			R_NodeModel prev = null;
			for (R_Node n: c) {
				R_NodeModel m = new R_NodeModel(n, prev);
				nodes.add(0, m);
				prev = m;
			}
		}
		nodes.addAll(no_loc);
		return nodes;
	}

	/** Create a set of roadway nodes for the current corridor */
	private Set<R_Node> createSet() {
		HashSet<R_Node> nodes = new HashSet<R_Node>();
		for (R_Node n: manager.getCache()) {
			if (checkCorridor(n))
				nodes.add(n);
		}
		return nodes;
	}

	/** Create a list of r_node models with null locations.  The r_nodes
	 * are then removed from the set passed in.
	 * @param node_s Set of nodes on the corridor.
	 * @return List of r_node models with null location. */
	private List<R_NodeModel> createNullLocList(Set<R_Node> node_s) {
		LinkedList<R_NodeModel> no_loc =
			new LinkedList<R_NodeModel>();
		Iterator<R_Node> it = node_s.iterator();
		while (it.hasNext()) {
			R_Node n = it.next();
			if (isNullOrAbandoned(n)) {
				no_loc.add(new R_NodeModel(n, null));
				it.remove();
			}
		}
		return no_loc;
	}

	/** Check if location is null or r_node is abandoned */
	private boolean isNullOrAbandoned(R_Node n) {
		return n.getAbandoned() || GeoLocHelper.isNull(n.getGeoLoc());
	}

	/** Set the corridor list model.
	 * This must be called on the EDT. */
	private void setListModel(List<R_NodeModel> nodes) {
		if (list_sel_mdl != null)
			list_sel_mdl.dispose();
		node_mdl = new R_NodeListModel(nodes);
		list_sel_mdl = new R_NodeListSelectionModel(node_mdl, sel_mdl);
		n_list.setModel(node_mdl);
		n_list.setSelectionModel(list_sel_mdl);
		n_list.ensureIndexIsVisible(n_list.getLeadSelectionIndex());
	}

	/** Update the roadway node selection */
	private void updateNodeSelection(R_Node n) {
		if (isCorridorChanged(n)) {
			CorridorBase<R_Node> cb = manager.getCorridor(n);
			corridor_cbx.setSelectedItem(cb);
		}
		client.setPointSelector(null);
		panel.setR_Node(n);
		updateButtonPanel();
	}

	/** Check if the corridor is changed */
	private boolean isCorridorChanged(R_Node n) {
		return (n != null) && !checkCorridor(n);
	}

	/** Update the enabled state of add and delete buttons */
	private void updateButtonPanel() {
		R_Node n = getSelectedNode();
		add_node.setEnabled(canAdd());
		delete_node.setEnabled(canRemove(n));
	}

	/** Do the add node action */
	private void doAddNode() {
		client.setPointSelector(new PointSelector() {
			public boolean selectPoint(Point2D p) {
				createNode(corridor, p);
				return true;
			}
			public void finish() { }
		});
	}

	/** Create a new node at a specified point */
	private void createNode(CorridorBase<R_Node> c, Point2D p) {
		Position pos = getWgs84Position(p);
		if (c != null) {
			int lanes = 2;
			int shift = MID_SHIFT + 1;
			R_NodeModel m = findModel(c, pos);
			if (m != null) {
				shift = m.getDownstreamLane(false);
				lanes = shift - m.getDownstreamLane(true);
			}
			creator.create(c.getRoadway(), c.getRoadDir(), pos,
				lanes, shift);
		} else
			creator.create(pos);
	}

	/** Get a position */
	private Position getWgs84Position(Point2D p) {
		SphericalMercatorPosition smp = new SphericalMercatorPosition(
			p.getX(), p.getY());
		return smp.getPosition();
	}

	/** Find an r_node model near a point */
	private R_NodeModel findModel(CorridorBase<R_Node> c, Position pos) {
		R_Node found = c.findLastBefore(pos);
		R_NodeListModel mdl = node_mdl;
		for (int i = 0; i < mdl.getSize(); i++) {
			Object elem = mdl.get(i);
			if (elem instanceof R_NodeModel) {
				R_NodeModel m = (R_NodeModel)elem;
				if (m.r_node == found)
					return m;
			}
		}
		return null;
	}

	/** Do the delete node action */
	private void doDeleteNode() {
		R_Node n = getSelectedNode();
		if (n != null) {
			GeoLoc loc = n.getGeoLoc();
			n.destroy();
			loc.destroy();
		}
	}

	/** Get the selected roadway node */
	private R_Node getSelectedNode() {
		return sel_mdl.getSingleSelection();
	}

	/** Test if a new r_node can be added */
	private boolean canAdd() {
		return session.canAdd(R_Node.SONAR_TYPE);
	}

	/** Test if an r_node can be removed */
	private boolean canRemove(R_Node n) {
		return n != null && session.canWrite(n);
	}
}
