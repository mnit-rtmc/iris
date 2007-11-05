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
package us.mn.state.dot.tms.client.dms;

import javax.swing.JList;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.device.DeviceHandlerImpl;
import us.mn.state.dot.tms.client.device.StatusSummary;
import us.mn.state.dot.tms.client.proxy.RefreshListener;

/**
 * Panel to display a summary of DMS device status and select status to list
 *
 * @author Douglas Lau
 */
public class DmsStatusSummary extends StatusSummary {

	/** Create a new DMS status summary panel */
	public DmsStatusSummary(final DeviceHandlerImpl h) {
		super(h);
		list.setCellRenderer(new DmsCellRenderer());
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(0);
		setStatus(DMS.STATUS_DEPLOYED);
		handler.addRefreshListener(new RefreshListener() {
			public void dataChanged() {
				repaint();
			}
		});
	}
}
