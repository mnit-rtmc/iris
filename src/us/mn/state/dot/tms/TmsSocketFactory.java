/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/**
 * TmsSocketFactory is a simple socket factory for creating RMI sockets.
 * It is necessary to use this to have a larger backlog queue for server
 * sockets.
 *
 * @author Douglas Lau
 */
public class TmsSocketFactory extends RMISocketFactory {

	/** Server socket backlog queue length */
	static protected final int BACKLOG = 100;

	/** Create a client socket connected to the specified host and port */
	public Socket createSocket(String host, int port) throws IOException {
		Socket s = new Socket(host, port);
		s.setSoTimeout(10000);
		return s;
	}

	/** Create a server socket on the specified port
	 * (port 0 indicates an anonymous port). */
	public ServerSocket createServerSocket(int port) throws IOException {
		return new ServerSocket(port, BACKLOG);
	}
}
