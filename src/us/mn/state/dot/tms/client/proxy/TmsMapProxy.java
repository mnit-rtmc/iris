/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.Shape;
import java.rmi.RemoteException;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.TmsObjectProxy;

/**
 * Proxy for a TMS map object
 *
 * @author Douglas Lau
 */
abstract public class TmsMapProxy extends TmsObjectProxy implements MapObject {

	/** Make a menu label */
	static protected Box makeMenuLabel(String id) {
		Box b = Box.createHorizontalBox();
		b.add(Box.createHorizontalStrut(6));
		b.add(Box.createHorizontalGlue());
		b.add(new JLabel(id));
		b.add(Box.createHorizontalGlue());
		b.add(Box.createHorizontalStrut(6));
		return b;
	}

	/** Make a popup with a menu label */
	static protected JPopupMenu makePopup(String id) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(id));
		p.addSeparator();
		return p;
	}

	/** Get the proxy type name */
	abstract public String getProxyType();

	/** Create a new TMS map proxy */
	protected TmsMapProxy(TMSObject o) {
		super(o);
	}

	/** Show the properties form */
	abstract public void showPropertiesForm(TmsConnection c)
		throws RemoteException;

	/** Get a popup menu */
	abstract public JPopupMenu getPopup(TmsConnection c);

	/** Get the shape to render */
	public Shape getShape() {
		return null;
	}

	/** Test if location is set */
	public boolean hasLocation() {
		return false;
	}
}
