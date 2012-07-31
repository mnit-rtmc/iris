/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010 AHMCT, University of California, Davis
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

/**
 * Cell renderer sizes.
 *
 * @see StyleSummary
 * @author Michael Darter
 * @author Douglas Lau
 */
public enum CellRendererSize {
	SMALL("cell.size.small"),
	MEDIUM("cell.size.medium"),
	LARGE("cell.size.large");

	/** Text ID */
	public final String text_id;

	/** Create a new cell renderer size */
	private CellRendererSize(String tid) {
		text_id = tid;
	}
}
