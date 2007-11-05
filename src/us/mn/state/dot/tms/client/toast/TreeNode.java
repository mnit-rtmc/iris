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
import javax.swing.tree.DefaultMutableTreeNode;
import us.mn.state.dot.tms.ErrorCounter;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * TreeNode class is an implementation of DefaultMutableTreeNode
 * FIXME: name clashes with javax.swing.tree.TreeNode
 *
 * @author Douglas Lau
 */
abstract public class TreeNode extends DefaultMutableTreeNode {

	/** Normal icon state */
	static protected final int STATE_NORMAL = 0;

	/** Warning icon state */
	static protected final int STATE_WARNING = 1;

	/** Failed icon state */
	static protected final int STATE_FAILED = 2;

	/** Inactive icon state */
	static protected final int STATE_INACTIVE = 3;

	/** Counters for this tree node */
	protected int[][] cnt =
		new int[ErrorCounter.TYPES.length][ErrorCounter.PERIODS.length];

	/** Node description */
	protected String str = "[uninitialized node]";

	/** Get the string description of the node */
	public String toString() {
		return str;
	}

	/** Get the icon state */
	protected int getState() {
		int g = cnt[ErrorCounter.TYPE_GOOD][ErrorCounter.PERIOD_NOW];
		int f = cnt[ErrorCounter.TYPE_FAIL][ErrorCounter.PERIOD_NOW];
		if(f > 0) {
			if(g > 0)
				return STATE_WARNING;
			else
				return STATE_FAILED;
		} else {
			if(g > 0)
				return STATE_NORMAL;
			else
				return STATE_INACTIVE;
		}
	}

	/** Create a new tree node */
	protected TreeNode(Object o) {
		super(o);
	}

	/** Get the icon to draw for the node */
	abstract public Icon getIcon();

	/** Update the status of the node */
	abstract protected void doStatus() throws RemoteException;

	/** Get the error counter model */
	public CounterModel getCounterModel() throws RemoteException {
		doStatus();
		return new CounterModel(cnt);
	}

	/** Add a child node */
	public void addChild(TmsConnection tc) throws TMSException,
		RemoteException
	{
		// Do nothing
	}

	/** Show the properties form for the node */
	public void showProperties(TmsConnection tc) throws RemoteException {
		// Do nothing
	}

	/** Remove the node */
	public void remove() throws TMSException, RemoteException {
		// Do nothing
	}

	/** Do this when the node is selected */
	public void select() throws RemoteException {
		// Do nothing
	}

	/** Get the status of the node */
	public String getStatus() throws RemoteException {
		return "";
	}
}
