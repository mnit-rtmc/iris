/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm;

import java.io.IOException;

/**
 * AddressedMessage
 *
 * @author Douglas Lau
 */
public interface AddressedMessage {

	/** Add an object to this message */
	public void add(Object mo);

	/** Send a get request message */
	public void getRequest() throws IOException;

	/** Send a set request message */
	public void setRequest() throws IOException;

	/** Send a set request message */
	public void setRequest(String community) throws IOException;
}
