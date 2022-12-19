/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcop;

import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.BasePoller;
import us.mn.state.dot.tms.server.comm.CamKeyboardPoller;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.utils.URIUtil.TCP;

/**
 * PelcoPPoller is a java implementation of the Pelco P camera control
 * communication protocol.
 *
 * @author Douglas Lau
 */
public class PelcoPPoller extends BasePoller implements CamKeyboardPoller {

	/** Create a new Pelco P poller */
	public PelcoPPoller(CommLink link) {
		super(link, TCP, true);
	}

	/** Create a listen operation */
	private void createListenOp(String n, ControllerImpl c) {
		Operation op = new Operation(n, c, new OpListenKeyboard());
		op.setPriority(PriorityLevel.IDLE);
		addOp(op);
	}

	/** Send a device request
	 * @param c The ControllerImpl object.
	 * @param dr The desired DeviceRequest. */
	@Override
	public void sendRequest(ControllerImpl c, DeviceRequest dr) {
		switch (dr) {
		case QUERY_STATUS:
			createListenOp("keyboard.op.listen", c);
			break;
		}
	}
}
