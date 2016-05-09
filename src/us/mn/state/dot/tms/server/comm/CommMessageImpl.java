/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * Comm message implementation.
 *
 * @author Douglas Lau
 */
public class CommMessageImpl<T extends ControllerProperty>
	implements CommMessage<T>
{
	/** Messenger object */
	private final Messenger messenger;

	/** Controller operation */
	private final OpController<T> op;

	/** Protocol debug log */
	private final DebugLog p_log;

	/** Controller properties */
	private final LinkedList<T> props;

	/** Create a new comm message.
	 * @param m Messenger to use for communication.
	 * @param o Controller operation.
	 * @param pl Protocol debug log. */
	public CommMessageImpl(Messenger m, OpController<T> o, DebugLog pl) {
		messenger = m;
		p_log = pl;
		op = o;
		props = new LinkedList<T>();
	}

	/** Add a controller property */
	@Override
	public void add(T cp) {
		props.add(cp);
	}

	/** Get the output stream */
	private OutputStream getOutputStream() throws IOException {
		ControllerImpl c = op.getController();
		return messenger.getOutputStream(c);
	}

	/** Query the controller properties.
	 * @throws IOException On any errors sending message or receiving
	 *         response */
	@Override
	public void queryProps() throws IOException {
		ControllerImpl c = op.getController();
		messenger.drain();
		OutputStream os = getOutputStream();
		if (os != null) {
			for (T p: props)
				p.encodeQuery(c, os);
			os.flush();
		}
		for (T p: props) {
			p.decodeQuery(c, messenger.getInputStream(p.getPath(),
				c));
			logQuery(p);
		}
	}

	/** Log a property query */
	@Override
	public void logQuery(T prop) {
		if (p_log != null && p_log.isOpen())
			p_log.log(op + ": " + prop);
	}

	/** Store the controller properties.
	 * @throws IOException On any errors sending a request or receiving
	 *         response */
	@Override
	public void storeProps() throws IOException {
		ControllerImpl c = op.getController();
		messenger.drain();
		OutputStream os = getOutputStream();
		for (T p: props) {
			logStore(p);
			if (os != null)
				p.encodeStore(c, os);
		}
		if (os != null)
			os.flush();
		for (T p: props) {
			p.decodeStore(c, messenger.getInputStream(p.getPath(),
				c));
		}
	}

	/** Log a property store */
	@Override
	public void logStore(T prop) {
		if (p_log != null && p_log.isOpen())
			p_log.log(op + ":= " + prop);
	}

	/** Log an error */
	@Override
	public void logError(String m) {
		if (p_log != null && p_log.isOpen())
			p_log.log(op + " ! " + m);
	}
}
