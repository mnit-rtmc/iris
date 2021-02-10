/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.alert;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.proxy.ProxyLayer;
import us.mn.state.dot.tms.client.proxy.ProxyLayerState;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;

/**
 * Alert layer state
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertLayerState extends ProxyLayerState<AlertInfo> {

	/** Alert DMS dispatcher */
	private final AlertDmsDispatcher dms_dispatcher;

	/** Alert selection model */
	private final ProxySelectionModel<AlertInfo> sel_mdl;

	/** Create an alert layer state */
	public AlertLayerState(ProxyLayer<AlertInfo> layer, MapBean mb,
		AlertDmsDispatcher dms_disp, ProxySelectionModel<AlertInfo> sm)
	{
		super(layer, mb);
		dms_dispatcher = dms_disp;
		sel_mdl = sm;
	}

	/** Search for DMS - use the map to transform the point
	 * then use the DMS manager to search */
	@Override
	protected void doLeftClick(MouseEvent e, MapObject o) {
		if (sel_mdl.getSingleSelection() != null) {
			Point2D p = map.transformPoint(e.getPoint());
			dms_dispatcher.selectDmsInTable(p);

			// check if they clicked out of the alert area
			if (o == null)
				sel_mdl.clearSelection();
		}
	}
}
