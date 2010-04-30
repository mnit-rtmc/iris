/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A controller property is one element of a CommMessage.  It represents a
 * property which can be sent to or queried from a controller.
 *
 * @author Douglas Lau
 */
public interface ControllerProperty {

	/** Encode a QUERY request */
//	void encodeQuery(OutputStream os, int drop) throws IOException;

	/** Encode a STORE request */
//	void encodeStore(OutputStream os, int drop) throws IOException;

	/** Decode a QUERY response */
//	void decodeQuery(InputStream is, int drop) throws IOException;

	/** Decode a STORE response */
//	void decodeStore(InputStream is, int drop) throws IOException;
}
