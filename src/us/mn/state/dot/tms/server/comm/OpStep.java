/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A step is one part of an operation to communicate with a field controller.
 *
 * @author Douglas Lau
 */
public interface OpStep {

	/** Check if polling is required */
	boolean isPolling();

	/** Set polling */
	void setPolling(boolean p);

	/** Poll the controller */
	void poll(Operation op, ByteBuffer tx_buf) throws IOException;

	/** Parse response from controller */
	void resp(Operation op, ByteBuffer rx_buf) throws IOException;

	/** Get the next step */
	OpStep next();
}
