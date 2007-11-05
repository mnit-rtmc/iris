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
import us.mn.state.dot.tms.Circuit;
import us.mn.state.dot.tms.ErrorCounter;
import us.mn.state.dot.tms.Location;
import us.mn.state.dot.tms.Node;
import us.mn.state.dot.tms.NodeGroup;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * NodeTreeNode is a tree node which represents a fiber node
 *
 * @author Douglas Lau
 */
public class NodeTreeNode extends TreeNode {

	/** Icons for painting nodes */
	static protected final ImageIcon[] ICONS = new ImageIcon[4];
	static {
		ICONS[STATE_NORMAL] = Icons.getIcon("node");
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

	/** Remote node object */
	protected final Node node;

	/** Create a new node tree node */
	public NodeTreeNode(Node n) {
		super(n);
		node = n;
	}

	/** Update the status of the node */
	protected void doStatus() throws RemoteException {
		ErrorCounter ec = (ErrorCounter)node;
		cnt = ec.getCounters();
		Location loc = node.getLocation();
		str = "Node " + node.getId() + ": " + loc.getDescription();
	}

	/** Add a child node */
	public void addChild(TmsConnection tc) throws RemoteException {
		SmartDesktop desktop = tc.getDesktop();
		desktop.show(new CircuitAddForm(tc, node, node.getId()));
	}

	/** Show the properties form for the node */
	public void showProperties(TmsConnection tc) throws RemoteException {
		SmartDesktop desktop = tc.getDesktop();
		desktop.show(new NodeForm(tc, node.getId(), node));
	}

	/** Remove the node */
	public void remove() throws TMSException, RemoteException {
		NodeGroup group = node.getGroup();
		group.deleteNode(node);
		group.notifyUpdate();
	}

	/** Do this when the node is selected */
	public void select() throws RemoteException {
		removeAllChildren();
		for(Circuit c: node.getCircuits())
			add(new CircuitTreeNode(c));
	}
}
