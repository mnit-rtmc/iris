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

interface ProtocolDriver {
	ubyte[] recv(ubyte[] buf);
}

class EchoDriver : ProtocolDriver {
public:
	ubyte[] recv(ubyte[] buf) {
		return buf;
	}
}

class STCController {
private:
	int address;
public:
	this(int a) {
		address = a;
	}
	ubyte[] process_packet(ubyte pkt[]) {
		final switch(pkt[3]) {
		case 'V':
			return cast(ubyte[])"Vh4.33, Boot Loader: V1.0\0";
		}
		return null;
	}
}

class STCDriver : ProtocolDriver {
private:
	ubyte[] rx_buf;
	ubyte[] tx_buf;
	STCController[int] controllers;
	ulong sentinel() {
		for(int i = 0; i < rx_buf.length; i++) {
			if(rx_buf[i] == 0xFF)
				return i;
		}
		return rx_buf.length;
	}
	void skip_garbage() {
		rx_buf = rx_buf[sentinel() .. $];
	}
	void check_packet() {
		// Check for minimum packet size
		if(rx_buf.length < 4)
			return;
		// Check for sentinel
		if(rx_buf[0] != 0xFF)
			return;
		int n_bytes = rx_buf[2] + 4;
		// Check for a full packet
		if(rx_buf.length < n_bytes)
			return;
		process_packet(rx_buf[0 .. n_bytes]);
		// Don't process that packet again
		rx_buf = rx_buf[n_bytes .. $];
	}
	void process_packet(ubyte pkt[]) {
		int xsum = 0;
		for(int i = 0; i < pkt.length; i++)
			xsum += pkt[i];
		if((xsum & 0xFF) != 0)
			return;
		// Check for valid message size
		if(pkt[2] < 1 || pkt[2] > 254)
			return;
		int address = pkt[1];
		STCController c = controllers[address];
		if(c !is null)
			process_packet(pkt, c);
	}
	void process_packet(ubyte pkt[], STCController c) {
		ubyte[] msg = c.process_packet(pkt);
		if(msg !is null) {
			ubyte[] rsp = [cast(ubyte)0xFF, cast(ubyte)0x00,
				cast(ubyte)msg.length] ~ msg;
			int xsum = 0;
			for(int i = 0; i < rsp.length; i++)
				xsum += rsp[i];
			rsp ~= cast(ubyte)((~xsum) + 1);
			tx_buf ~= rsp;
		}
	}
public:
	this() {
		controllers[1] = new STCController(1);
	}
	ubyte[] recv(ubyte[] buf) {
		tx_buf.length = 0;
		rx_buf ~= buf;
		skip_garbage();
		check_packet();
		return tx_buf;
	}
}
