/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2016  Minnesota Department of Transportation
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
 * A comm message encapsulates properties which can be queried from or stored
 * in a controller.
 *
 * @author Douglas Lau
 */
public interface CommMessage<T extends ControllerProperty> {

	/** Add a controller property */
	void add(T cp);

	/** Query the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	void queryProps() throws IOException;

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	void storeProps() throws IOException;

	/** Log a property query */
	void logQuery(T prop);

	/** Log a property store */
	void logStore(T prop);

	/** Log an error */
	void logError(String m);
}
