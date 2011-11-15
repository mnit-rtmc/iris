/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Modem;
import us.mn.state.dot.tms.ModemHelper;
import us.mn.state.dot.tms.server.IDebugLog;
import us.mn.state.dot.tms.server.ModemImpl;

/**
 * A Modem Messenger provides modem dialup support on top of another messenger
 * (such as StreamMessenger).
 *
 * @author Douglas Lau
 */
public class ModemMessenger extends Messenger {

	/** Modem debug log */
	static protected final IDebugLog MODEM_LOG = new IDebugLog("modem");

	/** Get the first available modem */
	static public ModemImpl getModem() {
		Modem mdm = ModemHelper.find(new Checker<Modem>() {
			public boolean check(Modem mdm) {
				// FIXME: find available modem
				return true;
			}
		});
		if(mdm instanceof ModemImpl)
			return (ModemImpl)mdm;
		else
			return null;
	}

	/** Wrapped messenger */
	protected final Messenger wrapped;

	/** Modem to dial */
	protected final Modem modem;

	/** Phone number to dial */
	protected final String phone_number;

	/** Read timeout (ms) */
	protected int timeout = 750;

	/** Log a message to debug log */
	private void log(String msg) {
		MODEM_LOG.log(modem.getName() + ": " + msg);
	}

	/** Create a new modem messenger */
	public ModemMessenger(Messenger m, Modem mdm, String phone) {
		wrapped = m;
		modem = mdm;
		phone_number = phone.replace("p", ",");
		log("created ModemMessenger");
	}

	/** Set the messenger timeout */
	public void setTimeout(int t) throws IOException {
		log("set timeout to " + t + " ms");
		wrapped.setTimeout(t);
		timeout = t;
	}

	/** Open the messenger */
	public void open() throws IOException {
		log("open");
		wrapped.open();
		output = wrapped.getOutputStream();
		input = new ModemInputStream(wrapped.getInputStream());
		connectModemRetry();
	}

	/** Close the messenger */
	public void close() {
		log("close");
		wrapped.close();
		output = null;
		input = null;
	}

	/** Connect the modem with up to three tries */
	private void connectModemRetry() throws IOException {
		int i = 0;
		while(true) {
			try {
				input.skip(input.available());
				connectModem();
				return;
			}
			catch(ModemException e) {
				if(i >= 3)
					throw e;
			}
			i++;
			log("connect retry #" + i);
		}
	}

    	/** Connect the modem to the specified phone number */
	protected void connectModem() throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(output,
			"US-ASCII");
		PrintWriter pw = new PrintWriter(osw, true);
		InputStreamReader isr = new InputStreamReader(input,"US-ASCII");
		String config = modem.getConfig();
		if(config != null && config.length() > 0)
			configureModem(pw, isr, config);
		if(phone_number != null && phone_number.length() > 0)
			dialModem(pw, isr);
		log("connected");
    	}

	/** Configure the modem */
	protected void configureModem(PrintWriter pw, InputStreamReader isr,
		String config) throws IOException
	{
		log("configure: " + config);
		pw.print(config + "\r\n");
		pw.flush();
		try {
			String resp = readResponse(isr).trim();
			if(!resp.toUpperCase().contains("OK")) {
				log("config error: " + resp);
				throw new ModemException("config " + resp);
			}
		}
		catch(SocketTimeoutException e) {
			throw new ModemException("config no response");
		}
	}

	/** Dial the modem */
	protected void dialModem(PrintWriter pw, InputStreamReader isr)
		throws IOException
	{
		log("dial: " + phone_number);
		pw.println("ATDT" + phone_number + "\r\n");
		try {
			wrapped.setTimeout(modem.getTimeout());
			waitForConnect(isr);
		}
		finally {
			wrapped.setTimeout(timeout);
		}
	}

	/** Wait for successful connection */
	protected void waitForConnect(InputStreamReader isr)
		throws IOException
	{
		log("wait for CONNECT");
		String resp = readResponse(isr).trim();
		if(!resp.toUpperCase().contains("CONNECT")) {
			log("connect error: " + resp);
			throw new ModemException("connect " + resp);
		}
	}

	/** Read a reaponse from the modem */
	protected String readResponse(InputStreamReader isr) throws IOException{
		char[] buf = new char[64];
		int n_chars = isr.read(buf, 0, 64);
		if(n_chars < 0)
			throw new EOFException("END OF STREAM");
		return new String(buf, 0, n_chars);
	}
}
