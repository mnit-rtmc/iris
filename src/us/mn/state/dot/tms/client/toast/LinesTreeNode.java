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
import us.mn.state.dot.tms.CommunicationLine;
import us.mn.state.dot.tms.ErrorCounter;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * LinesTreeNode is a tree node which contains all communication lines
 *
 * @author Douglas Lau
 */
public class LinesTreeNode extends TreeNode {

	/** Icons for painting lines */
	static protected final ImageIcon[] ICONS = new ImageIcon[4];
	static {
		ICONS[STATE_NORMAL] = Icons.getIcon("commline");
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

	/** Remote line list object */
	protected final IndexedList lines;

	/** Create a new lines tree node */
	public LinesTreeNode(IndexedList l) {
		super(l);
		lines = l;
		str = "Communication Lines";
	}

	/** Update the status of the lines */
	protected void doStatus() throws RemoteException {
		int[][] sum = new int[2][4];
		for(int i = 1; i <= lines.size(); i++) {
			ErrorCounter ec = (ErrorCounter)lines.getElement(i);
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
		CommunicationLine line = (CommunicationLine)lines.append();
		add(new CommunicationLineTreeNode(line, lines));
	}

	/** Do this when the node is selected */
	public void select() throws RemoteException {
		removeAllChildren();
		int s = lines.size();
		for(int i = 1; i <= s; i++) {
			CommunicationLine line =
				(CommunicationLine)lines.getElement(i);
			add(new CommunicationLineTreeNode(line, lines));
		}
	}
}
