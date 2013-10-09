/*
 * protest -- Protocol Tester
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
import core.thread;
import std.socket;
import std.stdio;
import protocol;

/** Socket listener.
 */
class Listener {
private:
	/** Listener socket */
	Socket sock;

	/** Protocol driver */
	ProtocolDriver driver;
public:
	/** Create a listener */
	this(ushort port, ProtocolDriver d) {
		sock = new TcpSocket();
		sock.blocking = false;
		sock.bind(new InternetAddress(port));
		sock.listen(5);
		driver = d;
	}

	/** Accept a new connection */
	Connection accept_connection() {
		return new Connection(sock.accept(), driver);
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

	/** Protocol driver */
	ProtocolDriver driver;

	/** Receive data buffer */
	ubyte[] rx_buf;

	/** Transmit data buffer */
	ubyte[] tx_buf;
public:
	/** Create a client connection */
	this(Socket s, ProtocolDriver d) {
		sock = s;
		address = s.remoteAddress();
		driver = d;
		rx_buf.length = 1024;
		tx_buf.length = 0;
	}

	/** Destroy a client connection */
	~this() {
		close();
	}

	/** Receive data from client connection */
	bool receive() {
		auto n_bytes = sock.receive(rx_buf);
		if (n_bytes > 0) {
			tx_buf ~= driver.recv(rx_buf[0 .. n_bytes]);
			return true;
		} else if (n_bytes == Socket.ERROR)
			writefln("Connection socket error: %s", address);
		return false;
	}

	/** Transmit data to client connection */
	bool transmit() {
		auto n_bytes = sock.send(tx_buf);
		if (n_bytes > 0) {
			tx_buf = tx_buf[n_bytes .. $];
			return true;
		} else if (n_bytes == Socket.ERROR)
			writefln("Connection socket error: %s", address);
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
	/** Maximum number of simultaneous sockets */
	const int MAX_SOCKETS = 150;

	/** Listener sockets */
	Listener[] listeners;

	/** Client connections */
	Connection[] conns;

	/** Read-ready socket set */
	SocketSet r_set;

	/** Write-ready socket set */
	SocketSet w_set;

	/** Prepare read socket set for polling */
	void prepare_read_set() {
		r_set.reset();
		foreach (Listener l; listeners)
			r_set.add(l.sock);
		foreach (Connection c; conns)
			r_set.add(c.sock);
	}

	/** Prepare write socket set for polling */
	void prepare_write_set() {
		w_set.reset();
		foreach (Connection c; conns) {
			if(c.tx_buf.length > 0)
				w_set.add(c.sock);
		}
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
		if (listeners.length + conns.length <= MAX_SOCKETS) {
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
		for (long i = conns.length - 1; i >= 0; --i) {
			if (w_set.isSet(conns[i].sock))
				transmit_client(i);
		}
	}

	/** Receive data from a client connection */
	void receive_client(long i) {
		if(!conns[i].receive())
			close_connection(i);
	}

	/** Transmit data to a client connection */
	void transmit_client(long i) {
		if(!conns[i].transmit())
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
		r_set = new SocketSet(MAX_SOCKETS);
		w_set = new SocketSet(MAX_SOCKETS);
	}

	/** Add a connection listener */
	void add_listener(ushort port, ProtocolDriver d) {
		listeners ~= new Listener(port, d);
	}

	/** Poll the socket server */
	void poll() {
		prepare_read_set();
		prepare_write_set();
		Socket.select(r_set, w_set, null);
		poll_listeners();
		poll_clients();
		Thread.sleep(dur!("msecs")(10));
	}
}

/** Main entry point */
int main(char[][] args) {
	Server srv = new Server();
	srv.add_listener(8001, new STCDriver());
	srv.add_listener(8002, new STCDriver());
	srv.add_listener(8003, new STCDriver());
	srv.add_listener(8004, new STCDriver());
	srv.add_listener(8005, new STCDriver());
	srv.add_listener(8006, new STCDriver());
	srv.add_listener(8007, new STCDriver());
	srv.add_listener(8008, new STCDriver());
	srv.add_listener(8009, new STCDriver());
	srv.add_listener(8010, new STCDriver());
	while(true)
		srv.poll();
	return 0;
}
