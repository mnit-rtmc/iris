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
import us.mn.state.dot.tms.Circuit;
import us.mn.state.dot.tms.CommunicationLine;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Node;
import us.mn.state.dot.tms.TMSException;

/**
 * CircuitTreeNode is a tree node which represents a fiber circuit
 *
 * @author Douglas Lau
 */
public class CircuitTreeNode extends TreeNode {

	/** Icons for painting circuits */
	static protected final ImageIcon[] ICONS = new ImageIcon[4];
	static {
		ICONS[STATE_NORMAL] = Icons.getIcon("circuit");
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

	/** Remote circuit object */
	protected final Circuit circuit;

	/** Create a new circuit tree node */
	public CircuitTreeNode(Circuit c) {
		super(c);
		circuit = c;
	}

	/** Update the status of the circuit */
	protected void doStatus() throws RemoteException {
		ErrorCounter ec = (ErrorCounter)circuit;
		cnt = ec.getCounters();
		CommunicationLine l = circuit.getLine();
		str = "Circuit " + circuit.getId() + "   L" + l.getIndex();
	}

	/** Remove the node */
	public void remove() throws TMSException, RemoteException {
		TreeNode n = (TreeNode)getParent();
		Node node = (Node)n.getUserObject();
		CommunicationLine line = circuit.getLine();
		node.deleteCircuit(circuit);
		node.notifyUpdate();
		line.notifyUpdate();
	}

	/** Do this when the node is selected */
	public void select() throws RemoteException {
		removeAllChildren();
		CommunicationLine line = (CommunicationLine)circuit.getLine();
		for(Controller c: line.getControllers()) {
			if(circuit.equals(c.getCircuit()))
				add(new ControllerTreeNode(c));
		}
	}
}
