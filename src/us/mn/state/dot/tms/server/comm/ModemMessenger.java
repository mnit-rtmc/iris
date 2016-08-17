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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Iterator;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Modem;
import us.mn.state.dot.tms.ModemHelper;
import us.mn.state.dot.tms.ModemState;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.ModemImpl;

/**
 * A Modem Messenger provides modem dialup support on top of another messenger
 * (such as StreamMessenger).
 *
 * @author Douglas Lau
 */
public class ModemMessenger extends Messenger {

	/** Exception thrown for no modem */
	static private final NoModemException NO_MODEM = new NoModemException();

	/** Modem debug log */
	static private final DebugLog MODEM_LOG = new DebugLog("modem");

	/** Create a modem messenger */
	static protected ModemMessenger create(URI u, int rt)
		throws MessengerException, IOException
	{
		ModemImpl modem = acquireModem();
		if (modem != null)
			return create(modem, u, rt);
		else
			throw NO_MODEM;
	}

	/** Acquire the first available modem */
	static private ModemImpl acquireModem() {
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

	/** Create a modem messenger */
	static private ModemMessenger create(ModemImpl modem, URI u, int rt)
		throws MessengerException, IOException
	{
		// NOTE: we have acquired the modem, so we must release it
		//       if the ModemMessenger isn't fully constructed
		try {
			URI um = createURI(modem.getUri());
			return new ModemMessenger(um, rt, modem, u.getHost());
		}
		catch (MessengerException | IOException e) {
			modem.release();
			throw e;
		}
	}

	/** Wrapped stream messenger */
	private final StreamMessenger wrapped;

	/** Modem to dial */
	private final ModemImpl modem;

	/** Phone number to dial */
	private final String phone_number;

	/** Reader to read modem responsess */
	private final Reader reader;

	/** Writer to send modem commands */
	private final Writer writer;

	/** Log a message to debug log */
	private void log(String msg) {
		if (MODEM_LOG.isOpen())
			MODEM_LOG.log(modem.getName() + ": " + msg);
	}

	/** Set the modem state */
	private void setState(ModemState ms) {
		modem.setStateNotify(ms);
		log("state: " + ms.toString());
	}

	/** Get the modem state */
	public ModemState getState() {
		return ModemState.fromOrdinal(modem.getState());
	}

	/** Create a new modem messenger */
	private ModemMessenger(URI um, int rt, ModemImpl mdm,
		String phone) throws MessengerException, IOException
	{
		modem = mdm;
		phone_number = phone.replace("p", ",");
		log("create");
		try {
			int ct = mdm.getTimeout();
			wrapped = StreamMessenger.create(um, rt, ct);
		}
		catch (IOException e) {
			setState(ModemState.open_error);
			throw e;
		}
		setState(ModemState.connecting);
		try {
			reader = new InputStreamReader(getInputStream(""),
				"US-ASCII");
			writer = new OutputStreamWriter(getOutputStream(),
				"US-ASCII");
			connectModemRetry();
		}
		catch (IOException e) {
			setState(ModemState.connect_error);
			throw e;
		}
		wrapped.setConnected();
		log("connected");
		setState(ModemState.online);
	}

	/** Connect the modem with up to three tries */
	private void connectModemRetry() throws IOException {
		int i = 0;
		while (true) {
			try {
				drain();
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

	/** Write some text */
	private void write(String s) throws IOException {
		writer.write(s);
		writer.flush();
	}

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
		char[] buf = new char[64];
		int n_chars = reader.read(buf, 0, 64);
		if (n_chars < 0)
			throw new HangUpException();
		String resp = new String(buf, 0, n_chars).trim();
		if (resp.contains("NO CARRIER"))
			throw new HangUpException();
		return resp;
	}

	/** Get the input stream.
	 * @param path Relative path name.
	 * @return An input stream for reading from the messenger. */
	@Override
	public InputStream getInputStream(String path) {
		return wrapped.getInputStream(path);
	}

	/** Get the output stream */
	@Override
	public OutputStream getOutputStream(ControllerImpl c) {
		return wrapped.getOutputStream(c);
	}

	/** Close the messenger */
	@Override
	public void close() throws IOException {
		try {
			disconnect();
		}
		finally {
			log("close");
			modem.release();
			if (!ModemState.isError(modem.getState()))
				setState(ModemState.offline);
			wrapped.close();
		}
	}

	/** Disconnect (hang up) the modem */
	private void disconnect() throws IOException {
		log("disconnect");
		write("+++");
		// Must wait 1 second after escape sequence (before too)
		TimeSteward.sleep_well(1000);
		// Send hang-up command
		write("ATH0;\r\n\n");
    	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() throws IOException {
		while (getInputStream("").available() > 0)
			readResponse();
	}
}
