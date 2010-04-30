/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.aws;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.LinkedList;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.utils.Log;

/**
 * Container for AWS messages.
 * FIXME: convert to use ControllerProperty encode/decode methods.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class AwsProperty extends ControllerProperty {

	/** AWS messages */
	protected final LinkedList<AwsMsg> messages = new LinkedList<AwsMsg>();

	/** Perform a get request, parsing all AWS messages */
	public void doGetRequest(InputStream input) throws IOException {
		messages.clear();
		if(input == null)
			throw new EOFException();
		InputStreamReader isr = new InputStreamReader(input,
			"ISO-8859-1");
		LineNumberReader lnr = new LineNumberReader(isr);
		while(true) {
			String line = lnr.readLine();
			if(line == null)
				break;
			AwsMsg msg = new AwsMsg();
			msg.parse(line);
			if(msg.getValid())
				messages.add(msg);
		}
	}

	/** Activate the messages */
	public void activate() {
		Log.finest("=======Starting activating AWS messages");
		for(AwsMsg msg: messages) {
			DMS dms = DMSHelper.lookup(msg.getIrisDmsId());
			if(dms instanceof DMSImpl)
				msg.activate((DMSImpl)dms);
		}
		Log.finest("=======End activating AWS messages");
	}
}
