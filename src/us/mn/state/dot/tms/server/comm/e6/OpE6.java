/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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

import java.io.IOException;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation for E6 device.
 *
 * @author Douglas Lau
 */
abstract public class OpE6 extends OpDevice<E6Property> {

	/** Tag reader device */
	protected final TagReaderImpl tag_reader;

	/** Create a new E6 operation */
	protected OpE6(PriorityLevel p, TagReaderImpl tr) {
		super(p, tr);
		tag_reader = tr;
	}

	/** Send a store */
	protected void sendStore(CommMessage<E6Property> mess, E6Property p)
		throws IOException
	{
		if (mess instanceof E6Message) {
			E6Message m = (E6Message) mess;
			m.sendStore(p);
		}
	}

	/** Send a query */
	protected void sendQuery(CommMessage<E6Property> mess, E6Property p)
		throws IOException
	{
		if (mess instanceof E6Message) {
			E6Message m = (E6Message) mess;
			m.sendQuery(p);
		}
	}

	/** Get the timeout */
	protected int getTimeout(CommMessage<E6Property> mess) {
		if (mess instanceof E6Message) {
			E6Message m = (E6Message) mess;
			return m.getTimeout();
		} else
			return 750;
	}
}
