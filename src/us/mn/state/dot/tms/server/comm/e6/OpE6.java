/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.e6;

import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.e6.E6Poller.E6_LOG;

/**
 * Operation for E6 device.
 *
 * @author Douglas Lau
 */
abstract public class OpE6 extends OpDevice<E6Property> {

	/** Log an error msg */
	protected void logError(String msg) {
		if (E6_LOG.isOpen())
			E6_LOG.log(controller.getName() + "! " + msg);
	}

	/** Tag reader device */
	protected final TagReaderImpl tag_reader;

	/** Poller */
	protected final E6Poller poller;

	/** Create a new E6 operation */
	protected OpE6(PriorityLevel p, TagReaderImpl tr, E6Poller ep) {
		super(p, tr);
		tag_reader = tr;
		poller = ep;
	}
}
