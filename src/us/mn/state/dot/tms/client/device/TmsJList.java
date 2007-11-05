/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.device;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsSelectionEvent;
import us.mn.state.dot.tms.client.TmsSelectionListener;
import us.mn.state.dot.tms.client.TmsSelectionModel;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;
import us.mn.state.dot.tms.utils.AbstractJob;

/**
 * This subclass of JList synchs selection events with a TmsSelectionModel.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class TmsJList extends JList {

	protected final DeviceHandlerImpl handler;

	protected final TmsSelectionModel mod;

	/** Create a new TmsJList */
	public TmsJList(DeviceHandlerImpl h) {
		handler = h;
		mod = handler.getSelectionModel();
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting())
					return;
				Object o = getSelectedValue();
				if(o instanceof TmsMapProxy)
					mod.setSelected((TmsMapProxy)o);
			}
		});
		mod.addTmsSelectionListener(new TmsSelectionListener() {
			public void selectionChanged(TmsSelectionEvent e) {
				setSelected(e.getSelected());
			}
			public void refreshUpdate() {}
			public void refreshStatus() {}
		});
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
					doDoubleClick();
			}
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger())
					popupMenu(e);
			}
			// NOTE: needed for cross-platform functionality
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger())
					popupMenu(e);
			}
		});
	}

	/** Respond to a double-click event */
	protected void doDoubleClick() {
		Object o = getSelectedValue();
		if(o instanceof TrafficDeviceProxy) {
			final TrafficDeviceProxy p = (TrafficDeviceProxy)o;
			new AbstractJob() {
				public void perform() throws RemoteException {
					p.showPropertiesForm(
						handler.getConnection());
				}
			}.addToScheduler();
		}
	}

	/** Set the selected traffic device */
	protected void setSelected(TMSObject o) {
		setSelectedValue(o, true);
		if(getSelectedValue() != o)
			clearSelection();
	}

	/** Popup a context-sensitive menu */
	protected void popupMenu(MouseEvent e, int index) {
		Rectangle bounds = getCellBounds(index, index);
		if(bounds.contains(e.getPoint())) {
			setSelectedIndex(index);
			Object o = getSelectedValue();
			if(o instanceof TrafficDeviceProxy) {
				TrafficDeviceProxy p = (TrafficDeviceProxy)o;
				JPopupMenu popup = p.getPopup(
					handler.getConnection());
				popup.setInvoker(this);
				popup.show(e.getComponent(), e.getX(),
					e.getY());
			}
		}
	}

	/** Popup a context-sensitive menu */
	protected void popupMenu(MouseEvent e) {
		int index = locationToIndex(e.getPoint());
		if(index > -1)
			popupMenu(e, index);
	}
}
