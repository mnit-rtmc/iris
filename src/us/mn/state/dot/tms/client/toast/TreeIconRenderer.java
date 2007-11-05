/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2007  Minnesota Department of Transportation
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

import java.awt.Component;
import java.rmi.RemoteException;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * TreeIconRenderer provides an Icon for each node based on its status.
 *
 * @author Sandy Dinh
 * @author Douglas Lau
 */
public class TreeIconRenderer extends DefaultTreeCellRenderer {

	/** Render an image icon to the tree node based on its object type */
	public Component getTreeCellRendererComponent( JTree tree,
		Object value, boolean sel, boolean expanded,
		boolean leaf, int row, boolean hasFocus )
	{
		super.getTreeCellRendererComponent( tree, value, sel,
			expanded, leaf, row, hasFocus);
		if(value instanceof TreeNode) {
			try {
				TreeNode node = (TreeNode)value;
				node.doStatus();
				setIcon(node.getIcon());
				setText(node.toString());
			} catch(RemoteException e) {
				// FIXME: should really do something here
			}
		}
		return this;
	}
}
