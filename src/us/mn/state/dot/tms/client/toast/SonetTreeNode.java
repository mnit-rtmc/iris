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
import us.mn.state.dot.tms.NodeGroup;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * SonetTreeNode is a tree node which contains all node groups
 *
 * @author Douglas Lau
 */
public class SonetTreeNode extends TreeNode {

	/** Icons for painting sonet */
	static protected final ImageIcon[] ICONS = new ImageIcon[4];
	static {
		ICONS[STATE_NORMAL] = Icons.getIcon("sonet");
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

	/** Remote node group list object */
	protected final IndexedList groups;

	/** Create a new node group list tree node */
	public SonetTreeNode(IndexedList s) {
		super(s);
		groups = s;
		str = "Sonet System";
	}

	/** Update the status of the node group list */
	protected void doStatus() throws RemoteException {
		int[][] sum = new int[2][4];
		for(int i = 1; i <= groups.size(); i++) {
			ErrorCounter ec = (ErrorCounter)groups.getElement(i);
			int[][] cc = ec.getCounters();
			for(int r = 0; r < 4; r++)
				for(int c = 0; c < 2; c++)
					sum[c][r] += cc[c][r];
		}
		cnt = sum;
	}

	/** Add a child node */
	public void addChild(TmsConnection tc) throws TMSException,
		RemoteException
	{
		NodeGroup ring = (NodeGroup)groups.append();
		add(new NodeGroupTreeNode(ring, groups));
	}

	/** Do this when the node is selected */
	public void select() throws RemoteException {
		removeAllChildren();
		for(int i = 1; i <= groups.size(); i++) {
			NodeGroup ring = (NodeGroup)groups.getElement(i);
			add(new NodeGroupTreeNode(ring, groups));
		}
	}
}
