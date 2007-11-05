/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.rmi.RemoteException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import us.mn.state.dot.tms.ErrorCounter;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.Node;
import us.mn.state.dot.tms.NodeGroup;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * NodeGroupTreeNode is a tree node which represents a fiber node group
 *
 * @author Douglas Lau
 */
public class NodeGroupTreeNode extends TreeNode {

	/** Icons for painting node groups */
	static protected final ImageIcon[] ICONS = new ImageIcon[4];
	static {
		ICONS[STATE_NORMAL] = Icons.getIcon("nodegrps");
		ICONS[STATE_WARNING] = Icons.filter(ICONS[STATE_NORMAL],
			new Icons.YellowFilter());
		ICONS[STATE_FAILED] = Icons.filter(ICONS[STATE_NORMAL],
			new Icons.RedFilter());
		ICONS[STATE_INACTIVE] = Icons.filter(ICONS[STATE_NORMAL],
			new Icons.DesaturateFilter());
	}

	/** Get an icon to paint the node */
	public Icon getIcon() {
		return ICONS[getState()];
	}

	/** Remote node group object */
	protected final NodeGroup node_group;

	/** Remote node group list */
	protected final IndexedList groups;

	/** Create a new node-group tree node */
	public NodeGroupTreeNode(NodeGroup ng, IndexedList glist) {
		super(ng);
		node_group = ng;
		groups = glist;
	}

	/** Update the status of the node group */
	protected void doStatus() throws RemoteException {
		ErrorCounter ec = (ErrorCounter)node_group;
		cnt = ec.getCounters();
		str = node_group.getDescription();
	}

	/** Add a child node */
	public void addChild(TmsConnection tc) throws RemoteException {
		SmartDesktop desktop = tc.getDesktop();
		desktop.show(new NodeAddForm(node_group,
			node_group.getIndex()));
	}

	/** Show the properties form for the node */
	public void showProperties(TmsConnection tc) throws RemoteException {
		SmartDesktop desktop = tc.getDesktop();
		desktop.show(new NodeGroupForm(tc, node_group.getIndex()));
	}

	/** Remove the node */
	public void remove() throws TMSException, RemoteException {
		NodeGroup last = (NodeGroup)groups.getElement(groups.size());
		if(node_group.equals(last))
			groups.removeLast();
	}

	/** Do this when the node is selected */
	public void select() throws RemoteException {
		removeAllChildren();
		for(Node n: node_group.getNodes())
			add(new NodeTreeNode(n));
	}
}
