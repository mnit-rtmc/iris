/*
 * psim -- Protocol simulator
 * Copyright (C) 2013  Minnesota Department of Transportation
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
import std.socket;
import std.stdio;

/** Socket listener.
 */
class Listener {
private:
	/** Listener socket */
	Socket sock;
	// FIXME: protocol enum
public:
	/** Create a listener */
	this(ushort port) {
		sock = new TcpSocket();
		sock.blocking = false;
		sock.bind(new InternetAddress(port));
		sock.listen(5);
	}

	/** Accept a new connection */
	Connection accept_connection() {
		return new Connection(sock.accept());
	}
}

/** Simple client connection.
 */
class Connection {
private:
	/** Socket to client connection */
	Socket sock;

	/** Remote client socket address */
	Address address;
public:
	/** Create a client connection */
	this(Socket s) {
		sock = s;
		address = s.remoteAddress();
	}

	/** Destroy a client connection */
	~this() {
		close();
	}

	/** Receive data from client connection */
	bool receive() {
		char[1024] buf;
		auto n_bytes = sock.receive(buf);
		if (n_bytes > 0) {
			writefln("Received %d bytes from %s: \"%s\"", n_bytes,
				address, buf[0 .. n_bytes]);
			return true;
		} else if (n_bytes == Socket.ERROR)
			writeln("Connection socket error: %s", address);
		return false;
	}

	/** Get a string representation of client connection */
	override string toString() {
		return address.toString();
	}

	/** Close a client connection */
	void close() {
		sock.close();
	}
}

/** Simple socket server.
 */
class Server {
private:
	const int MAX_CONNECTIONS = 150;

	/** Listener sockets */
	Listener[] listeners;

	/** Client connections */
	Connection[] conns;

	/** Read-ready socket set */
	SocketSet r_set;

	/** Prepare read socket set for polling */
	void prepare_read_set() {
		r_set.reset();
		foreach (Listener l; listeners)
			r_set.add(l.sock);
		foreach (Connection c; conns)
			r_set.add(c.sock);
	}

	/** Poll listener sockets for new connections */
	void poll_listeners() {
		foreach (Listener l; listeners) {
			if (r_set.isSet(l.sock))
				accept_connection(l);
		}
	}

	/** Accept a connection from a listener */
	void accept_connection(Listener l) {
		Connection c = l.accept_connection();
		if (conns.length < MAX_CONNECTIONS) {
			writefln("Connection established: %s", c);
			conns ~= c;
		} else {
			writefln("Connection rejected: %s", c);
			c.close();
		}
		writefln("\tTotal connections: %d", conns.length);
	}

	/** Poll clients connections for received data */
	void poll_clients() {
		// Loop backwards so connections can be removed while looping
		for (long i = conns.length - 1; i >= 0; --i) {
			if (r_set.isSet(conns[i].sock))
				receive_client(i);
		}
	}

	/** Receive data from a client connection */
	void receive_client(long i) {
		if(!conns[i].receive())
			close_connection(i);
	}

	/** Close a connection */
	void close_connection(long i) {
		Connection c = conns[i];
		c.close();
		writefln("Connection closed: %s", c);
		conns = conns[0 .. i] ~ conns[i + 1 .. $];
		writefln("\tTotal connections: %d", conns.length);
	}
public:
	/** Create a socket server */
	this() {
		r_set = new SocketSet(MAX_CONNECTIONS);
	}

	/** Add a connection listener */
	void add_listener(ushort port) {
		listeners ~= new Listener(port);
	}

	/** Poll the socket server */
	void poll() {
		prepare_read_set();
		Socket.select(r_set, null, null);
		poll_listeners();
		poll_clients();
	}
}

/** Main entry point */
int main(char[][] args) {
	Server srv = new Server();
	srv.add_listener(1234);
	while(true)
		srv.poll();
	return 0;
}
