/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.json.JSONObject;

/**
 * Common Alerting Protocol (CAP) alert parser.
 *
 * @author Douglas Lau
 */
public interface AlertParser {

	/** Parse alerts */
	List<JSONObject> parse(String doc) throws IOException;
}
