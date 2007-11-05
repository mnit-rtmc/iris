/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.proxy.LocationProxy;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * Proxy for roadway nodes
 *
 * @author Douglas Lau
 */
public class R_NodeProxy extends TmsMapProxy {

	/** Proxy type name */
	static public final String PROXY_TYPE = "R_Node";

	/** Get the proxy type name */
	public String getProxyType() {
		return PROXY_TYPE;
	}

	/** Remote roadway node reference */
	protected final R_Node r_node;

	/** Object ID */
	protected final Integer oid;

	/** Location of the roadway node */
	protected final LocationProxy loc;

	/** Roadway node type */
	protected int node_type;

	/** Get the roadway node type */
	public int getNodeType() {
		return node_type;
	}

	/** Pickable flag */
	protected boolean pickable;

	/** Transition type */
	protected int transition;

	/** Get the transition type */
	public int getTransition() {
		return transition;
	}

	/** Number of lanes */
	protected int lanes;

	/** Get the number of lanes */
	public int getLanes() {
		return lanes;
	}

	/** Attach side */
	protected boolean attach_side;

	/** Get the attach side */
	public boolean getAttachSide() {
		return attach_side;
	}

	/** Shift relative to attach_side */
	protected int shift;

	/** Get the lane shift */
	public int getShift() {
		return shift;
	}

	/** Station ID */
	protected String station_id;

	/** Speed limit */
	protected int speed_limit;

	/** Administrator notes */
	protected String notes;

	/** Cross-street label */
	String xStreet;

	/** Create a new roadway node proxy */
	public R_NodeProxy(R_Node n) throws RemoteException {
		super(n);
		r_node = n;
		oid = r_node.getOID();
		loc = new LocationProxy(r_node.getLocation());
		updateUpdateInfo();
	}

	/** Get a string representation of the node */
	public String toString() {
		return oid + " - " + loc.getDescription();
	}

	/** Update the proxy status information */
	public void updateStatusInfo() throws RemoteException {
		// FIXME: get node status
	}

	/** Update the proxy update information */
	public void updateUpdateInfo() throws RemoteException {
		loc.updateUpdateInfo();
		node_type = r_node.getNodeType();
		pickable = r_node.isPickable();
		transition = r_node.getTransition();
		lanes = r_node.getLanes();
		attach_side = r_node.getAttachSide();
		shift = r_node.getShift();
		station_id = r_node.getStationID();
		notes = r_node.getNotes();
		xStreet = loc.getCrossDescription();
	}

	/** Get location */
	public LocationProxy getLocation() {
		return loc;
	}

	/** Check if the location is valid */
	public boolean hasLocation() {
		return !loc.isZero();
	}

	/** Calculate the distance to another roadway node */
	public double distanceTo(R_NodeProxy other) {
		int x = loc.getTrueEasting() - other.loc.getTrueEasting();
		int y = loc.getTrueNorthing() - other.loc.getTrueNorthing();
		return Math.hypot(x, y);
	}

	/** Get the corridor which contains the roadway node */
	public String getCorridor() {
		return loc.getCorridor();
	}

	/** Get the transform to render as a map object */
	public AffineTransform getTransform() {
		return loc.getTransform();
	}

	/** Get the inverse transform */
	public AffineTransform getInverseTransform() {
		return loc.getInverseTransform();
	}

	/** Get the extent of the roadway node */
	protected Rectangle2D getExtent() {
		float x = loc.getEasting() + loc.getEastOffset();
		float y = loc.getNorthing() + loc.getNorthOffset();
		return new Rectangle2D.Float(x - 500, y - 500, 1000, 1000);
	}

	/** Get the union of the nodes extent with the given extent */
	public Rectangle2D getUnion(Rectangle2D e) {
		if(loc.isZero())
			return e;
		else if(e == null)
			return getExtent();
		else {
			Rectangle2D.union(e, getExtent(), e);
			return e;
		}
	}

	/** Show the properties form for the roadway node */
	public void showPropertiesForm(TmsConnection tc) throws RemoteException
	{
		tc.getDesktop().show(new R_NodeProperties(tc, r_node, oid));
	}

	/** Get a popup for this roadway node */
	public JPopupMenu getPopup(TmsConnection tc) {
		JPopupMenu popup = makePopup(toString());
		popup.add(new JMenuItem(new PropertiesAction(this, tc)));
		return popup;
	}
}
