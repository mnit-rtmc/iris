/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.parking;

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.client.proxy.ProxyAction;

/**
 * Open or close a parking area.
 *
 * @author Douglas Lau
 */
public class OpenCloseAction extends ProxyAction<ParkingArea> {

	/** Flag to open parking area */
	private final boolean open;

	/** Create a new action to open or close the selected parking area */
	public OpenCloseAction(ParkingArea pa, boolean op, boolean e) {
		super(op ? "parking_area.open" : "parking_area.closed", pa);
		open = op;
		setEnabled(e);
	}

	/** Actually perform the action */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		if (proxy != null)
			proxy.setOpen(open);
	}
}
