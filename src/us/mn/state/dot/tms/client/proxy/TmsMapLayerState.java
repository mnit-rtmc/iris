/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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

import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.event.LayerChangedEvent;
import us.mn.state.dot.tms.client.TmsSelectionEvent;
import us.mn.state.dot.tms.client.TmsSelectionListener;
import us.mn.state.dot.tms.client.TmsSelectionModel;

/**
 * Base class for all TMS map layer states.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class TmsMapLayerState extends LayerState {

	/** Proxy handler */
	protected final ProxyHandlerImpl handler;

	/** Selection model for the proxy handler */
	protected final TmsSelectionModel model;

	/** Listener for TMS selection events */
	protected final TmsSelectionListener listener;

	/** Create a new TMS map layer state */
	public TmsMapLayerState(TmsMapLayer layer) {
		super(layer);
		handler = (ProxyHandlerImpl)layer.getHandler();
		model = handler.getSelectionModel();
		listener = new TmsSelectionListener() {
			public void selectionChanged(TmsSelectionEvent e) {
				MapObject s = model.getSelected();
				if(s != null)
					setSelections(new MapObject[] { s });
				else
					clearSelections();
			}
			public void refreshStatus() {
				notifyLayerChangedListeners(
					LayerChangedEvent.DATA);
			}
			public void refreshUpdate() {
				notifyLayerChangedListeners(
					LayerChangedEvent.GEOGRAPHY);
			}
		};
		model.addTmsSelectionListener(listener);
	}

	/** Dispose of the TMS map theme */
	public void dispose() {
		super.dispose();
		((TmsMapLayer)layer).dispose();
		model.removeTmsSelectionListener(listener);
	}

	/** Do left-click event processing */
	protected void doLeftClick(MouseEvent e, MapObject o) {
		if(o instanceof TmsMapProxy) {
			setSelections(new MapObject[] { o });
			model.setSelected((TmsMapProxy)o);
		} else
			clearSelections();
	}

	/** Show a popup menu for the given proxy */
	protected void showPopupMenu(MouseEvent e, TmsMapProxy proxy) {
		JPopupMenu menu = proxy.getPopup(handler.getConnection());
		Component component = e.getComponent();
		int x = e.getX();
		int y = e.getY();
		int menuHeight = (int)menu.getPreferredSize().getHeight();
		int height = component.getHeight();
		if(height < y + menuHeight)
			y = height - menuHeight;
		int menuWidth = (int)menu.getPreferredSize().getWidth();
		int width = component.getWidth();
		if(width < x + menuWidth)
			x = width - menuWidth;
		menu.show(component, x, y);
	}

	/** Do right-click event processing */
	protected void doRightClick(MouseEvent e, MapObject o) {
		if(o instanceof TmsMapProxy) {
			TmsMapProxy proxy = (TmsMapProxy)o;
			setSelections(new MapObject[] { o });
			model.setSelected(proxy);
			showPopupMenu(e, proxy);
		} else
			clearSelections();
	}
}
