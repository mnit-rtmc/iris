/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Modem;
import us.mn.state.dot.tms.ModemHelper;
import us.mn.state.dot.tms.ModemState;
import us.mn.state.dot.tms.server.ModemImpl;

/**
 * A Modem Messenger provides modem dialup support on top of another messenger
 * (such as StreamMessenger).
 *
 * @author Douglas Lau
 */
public class ModemMessenger extends Messenger {

	/** Modem debug log */
	static private final DebugLog MODEM_LOG = new DebugLog("modem");

	/** Get the first available modem */
	static public ModemImpl getModem() {
		Iterator<Modem> it = ModemHelper.iterator();
		while (it.hasNext()) {
			Modem m = it.next();
			if (m instanceof ModemImpl) {
				ModemImpl mdm = (ModemImpl) m;
				if (mdm.acquire())
					return mdm;
			}
		}
		return null;
	}

	/** Wrapped stream messenger */
	private final StreamMessenger wrapped;

	/** Modem to dial */
	private final ModemImpl modem;

	/** Set the modem state */
	private void setState(ModemState ms) {
		modem.setStateNotify(ms);
		log("state: " + ms.toString());
	}

	/** Get the modem state */
	public ModemState getState() {
		return ModemState.fromOrdinal(modem.getState());
	}

	/** Phone number to dial */
	private final String phone_number;

	/** Time stamp of last activity */
	private long activity;

	/** Get last activity time stamp */
	public long getActivity() {
		return activity;
	}

	/** Log a message to debug log */
	private void log(String msg) {
		if (MODEM_LOG.isOpen())
			MODEM_LOG.log(modem.getName() + ": " + msg);
	}

	/** Create a new modem messenger */
	public ModemMessenger(SocketAddress a, int rt, ModemImpl mdm,
		String phone) throws IOException
	{
		wrapped = new StreamMessenger(a, rt, mdm.getTimeout());
		modem = mdm;
		phone_number = phone.replace("p", ",");
		activity = TimeSteward.currentTimeMillis();
		log("created ModemMessenger");
	}

	/** Open the messenger */
	@Override
	public void open() throws IOException {
		log("open");
		try {
			wrapped.open();
			setState(ModemState.connecting);
		}
		catch (IOException e) {
			setState(ModemState.open_error);
			throw e;
		}
		output = wrapped.getOutputStream();
		writer = new OutputStreamWriter(output, "US-ASCII");
		ModemInputStream mis = new ModemInputStream(
			wrapped.getInputStream(""));
		input = mis;
		reader = new InputStreamReader(input, "US-ASCII");
		try {
			connectModemRetry();
			mis.setConnected();
			wrapped.setConnected();
			log("connected");
			setState(ModemState.online);
		}
		catch (IOException e) {
			setState(ModemState.connect_error);
			throw e;
		}
	}

	/** Close the messenger */
	@Override
	public void close() {
		log("close");
		wrapped.close();
		writer = null;
		output = null;
		reader = null;
		input = null;
		if (!ModemState.isError(modem.getState()))
			setState(ModemState.offline);
		modem.release();
	}

	/** Connect the modem with up to three tries */
	private void connectModemRetry() throws IOException {
		int i = 0;
		while (true) {
			try {
				input.skip(input.available());
				connectModem();
				return;
			}
			catch (ModemException e) {
				if (i >= 3)
					throw e;
			}
			i++;
			log("connect retry #" + i);
		}
	}

    	/** Connect the modem to the specified phone number */
	private void connectModem() throws IOException {
		String config = modem.getConfig();
		if (config != null && config.length() > 0)
			configureModem(config);
		if (phone_number != null && phone_number.length() > 0)
			dialModem();
    	}

	/** Writer to send modem commands */
	private Writer writer;

	/** Write some text */
	private void write(String s) throws IOException {
		Writer w = writer;
		if (w != null) {
			w.write(s);
			w.flush();
		}
	}

	/** Reader to read modem responsess */
	private Reader reader;

	/** Configure the modem */
	private void configureModem(String config) throws IOException {
		log("configure: " + config);
		write(config + "\r\n");
		try {
			String resp = readResponse();
			if (!resp.toUpperCase().contains("OK")) {
				log("config error: " + resp);
				throw new ModemException("config " + resp);
			}
		}
		catch (SocketTimeoutException e) {
			throw new ModemException("config no response");
		}
	}

	/** Dial the modem */
	private void dialModem() throws IOException {
		log("dial: " + phone_number);
		write("ATDT" + phone_number + "\r\n\n");
		waitForConnect();
	}

	/** Wait for successful connection */
	private void waitForConnect() throws IOException {
		log("wait for CONNECT");
		String resp = readResponse();
		if (!resp.toUpperCase().contains("CONNECT")) {
			log("connect error: " + resp);
			throw new ModemException("connect " + resp);
		}
	}

	/** Read a reaponse from the modem */
	private String readResponse() throws IOException {
		Reader r = reader;
		if (r == null)
			throw new EOFException();
		char[] buf = new char[64];
		int n_chars = r.read(buf, 0, 64);
		if (n_chars < 0)
			throw new EOFException("END OF STREAM");
		String resp = new String(buf, 0, n_chars).trim();
		if (resp.contains("NO CARRIER"))
			throw new HangUpException();
		return resp;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() throws IOException {
		// Update last activity timestamp
		activity = TimeSteward.currentTimeMillis();
		while (input.available() > 0)
			readResponse();
	}

	/** Disconnect (hang up) the modem */
	public void disconnectModem() throws IOException {
		log("disconnect");
		write("+++");
		// Must wait 1 second after escape sequence (before too)
		TimeSteward.sleep_well(1000);
		// Send hang-up command
		write("ATH0;\r\n\n");
    	}
}
