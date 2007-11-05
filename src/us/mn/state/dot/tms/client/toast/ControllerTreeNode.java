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
import us.mn.state.dot.tms.Location;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * ControllerTreeNode is a tree node which represents a controller
 *
 * @author Douglas Lau
 */
public class ControllerTreeNode extends TreeNode {

	/** Icons for painting controllers */
	static protected final ImageIcon[] ICONS = new ImageIcon[4];
	static {
		ICONS[STATE_NORMAL] = Icons.getIcon("controller");
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

	/** Remote controller object */
	protected final Controller controller;

	/** Create a new controller tree node */
	public ControllerTreeNode(Controller c) {
		super(c);
		controller = c;
	}

	/** Update the status of the controller */
	protected void doStatus() throws RemoteException {
		ErrorCounter ec = (ErrorCounter)controller;
		cnt = ec.getCounters();
		Circuit cir = controller.getCircuit();
		Location loc = controller.getLocation();
		str = "Drop " + controller.getDrop() + ":" +
			loc.getDescription() + "  (" + cir.getId() + ")";
	}

	/** Show the properties form for the node */
	public void showProperties(TmsConnection tc) throws RemoteException {
		SmartDesktop desktop = tc.getDesktop();
		desktop.show(ControllerForm.create(tc, controller,
			controller.getOID().toString()));
	}

	/** Remove the node */
	public void remove() throws TMSException, RemoteException {
		CommunicationLine line = controller.getLine();
		Circuit circuit = controller.getCircuit();
		circuit.removeController(controller.getDrop());
		circuit.notifyUpdate();
		line.notifyUpdate();
	}

	/** Get the status of the node */
	public String getStatus() throws RemoteException {
		if(controller.isFailed())
			return "Failed on " + controller.getFailTime();
		else
			return "";
	}
}
