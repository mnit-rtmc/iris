/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2010  Minnesota Department of Transportation
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

/**
 * A comm message encapsulates requests to GET or SET information from a
 * controller.
 *
 * @author Douglas Lau
 */
public interface CommMessage {

	/** Add a request to this message */
	public void add(Object mo);

	/** Send a get request message */
	public void getRequest() throws IOException;

	/** Send a set request message */
	public void setRequest() throws IOException;
}
