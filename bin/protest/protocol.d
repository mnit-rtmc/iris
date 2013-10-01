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
import std.array;
import std.datetime;
import std.format;
import std.stdio;

interface ProtocolDriver {
	ubyte[] recv(ubyte[] buf);
}

class EchoDriver : ProtocolDriver {
public:
	ubyte[] recv(ubyte[] buf) {
		return buf;
	}
}

enum command_status {
	reset,
	open_in_progress,
	open_complete,
	close_in_progress,
	close_complete,
	stopped,
}

enum operator_status {
	reset,
	learn_limit_stop, learn_limit_open, learn_limit_close,
	normal_stop,
	check_pe_open, pep_2_open, warn_b4_open,
	normal_open,
	reverse_2_close_peo, wait_peo, delay_peo,
	check_pe_close, pep_2_close, warn_b4_close,
	normal_close,
	wait_vd, reverse_2_open_pec, wait_pe, delay_pe,
	reverse_2_close, reverse_2_open,
	safety_stop, entrapment_stop,
	fault_1, fault_2, fault_3, fault_5,
	error_1, error_2, error_6, error_8, error_9, error_10,
	alert_1, alert_2, alert_4, alert_5, alert_6,
}

class STCController {
private:
	int address;
	command_status cmd_stat = command_status.reset;
	operator_status op_stat = operator_status.reset;
	SysTime tm;
	bool open_limit = false;
	bool close_limit = true;

	void update_status() {
		if(tm < Clock.currTime())
			update_op_status();
	}
	void update_op_status() {
		switch(op_stat) {
		case operator_status.normal_close:
			cmd_stat = command_status.close_complete;
			op_stat = operator_status.normal_stop;
			close_limit = true;
			break;
		case operator_status.normal_open:
			cmd_stat = command_status.open_complete;
			op_stat = operator_status.normal_stop;
			open_limit = true;
			break;
		default:
			op_stat = operator_status.normal_stop;
			break;
		}
	}
	ubyte[] process_status() {
		auto rsp = appender!string();
		formattedWrite(rsp, "S%02X%02X041%1X%1X00000000000", cmd_stat,
			op_stat, open_limit, close_limit);
		return cast(ubyte[])rsp.data();
	}
	ubyte[] process_status_n() {
		auto rsp = appender!string();
		formattedWrite(rsp,
			"N%02X%02X041%1X%1X00000000000000000000000000000",
			cmd_stat, op_stat, open_limit, close_limit);
		return cast(ubyte[])rsp.data();
	}
	ubyte[] control_request(ubyte[] pkt) {
		if(pkt.length == 9) {
			if(pkt[1] == '1' && pkt[2] == '0' && pkt[3] == '0')
				control_open();
			if(pkt[1] == '0' && pkt[2] == '1' && pkt[3] == '0')
				control_close();
			if(pkt[1] == '0' && pkt[2] == '0' && pkt[3] == '1')
				control_stop();
			return cast(ubyte[])"C";
		}
		return null;
	}
	void control_open() {
		cmd_stat = command_status.open_in_progress;
		op_stat = operator_status.normal_open;
		close_limit = false;
		tm = Clock.currTime() + dur!"seconds"(7);
	}
	void control_close() {
		cmd_stat = command_status.close_in_progress;
		op_stat = operator_status.normal_close;
		open_limit = false;
		tm = Clock.currTime() + dur!"seconds"(7);
	}
	void control_stop() {
		cmd_stat = command_status.stopped;
		op_stat = operator_status.normal_stop;
	}
public:
	this(int a) {
		address = a;
		tm = Clock.currTime();
	}
	ubyte[] process_packet(ubyte pkt[]) {
		final switch(pkt[0]) {
		case 'R':
			break;
		case 'V':
			return cast(ubyte[])"Vh4.33, Boot Loader: V1.0\0";
		case 'S':
			update_status();
			return process_status_n();
		case 'C':
			return control_request(pkt);
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
			process_packet(pkt[3 .. $-1], c);
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
		foreach(int a; 1 .. 8)
			controllers[a] = new STCController(a);
	}
	ubyte[] recv(ubyte[] buf) {
		tx_buf.length = 0;
		rx_buf ~= buf;
		skip_garbage();
		check_packet();
		return tx_buf;
	}
}
