/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

/**
 * Tag (vehicle transponder) type enumeration.
 *
 * @author Douglas Lau
 */
public enum TagType {
	UNKNOWN,
	SeGo,    // TransCore Super eGo
	IAG,     // E-Zpass InterAgency Group
	_6C;     // ISO/IEC 18000-63
}
