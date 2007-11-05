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
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ErrorCounter;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * CommunicationLineTreeNode is a tree node for one communication line
 *
 * @author Douglas Lau
 */
public class CommunicationLineTreeNode extends TreeNode {

	/** Icons for painting lines */
	static protected final ImageIcon[] ICONS = new ImageIcon[4];
	static {
		ICONS[STATE_NORMAL] = Icons.getIcon("commline1");
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

	/** Remote line object */
	protected final CommunicationLine line;

	/** Remote communication line list */
	protected final IndexedList lines;

	/** Create a new line tree node */
	public CommunicationLineTreeNode(CommunicationLine l,
		IndexedList list)
	{
		super(l);
		line = l;
		lines = list;
	}

	/** Update the status of the lines */
	protected void doStatus() throws RemoteException {
		ErrorCounter ec = (ErrorCounter)line;
		cnt = ec.getCounters();
		str = "Line " + line.getIndex() + ">" + line.getDescription();
	}

	/** Add a child node */
	public void addChild(TmsConnection tc) throws RemoteException {
		SmartDesktop desktop = tc.getDesktop();
		desktop.show(new ControllerAddForm(tc, line, line.getIndex()));
	}

	/** Show the properties form for the node */
	public void showProperties(TmsConnection tc) throws RemoteException {
		SmartDesktop desktop = tc.getDesktop();
		desktop.show(new CommunicationLineForm(tc, line.getIndex()));
	}

	/** Remove the node */
	public void remove() throws TMSException, RemoteException {
		CommunicationLine last = (CommunicationLine)
			lines.getElement(lines.size());
		if(line.equals(last))
			lines.removeLast();
	}

	/** Do this when the node is selected */
	public void select() throws RemoteException {
		removeAllChildren();
		for(Controller c: line.getControllers())
			add(new ControllerTreeNode(c));
	}

	/** Get the status of the node */
	public String getStatus() throws RemoteException {
		int percent = Math.round(line.getLoad() * 100);
		String load = Integer.toString(percent) + "%";
		return "Status: " + line.getStatus() + "        Load: " + load;
	}
}
