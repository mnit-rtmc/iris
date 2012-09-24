/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A TMSException is a generic exception which is thrown whenever there is
 * any problems accessing TMS objects.
 *
 * @author Douglas Lau
 */
public class TMSException extends Exception {

	/** Stack trace from "cause" exception */
	protected final String stack_trace;

	/** Create a new TMS exception */
	public TMSException(String msg) {
		super(msg);
		stack_trace = null;
	}

	/** Create a TMS exception with a given cause */
	public TMSException(Throwable cause) {
		super(cause.getMessage());
		StringWriter writer = new StringWriter();
		cause.printStackTrace(new PrintWriter(writer));
		stack_trace = writer.toString();
	}

	/**
	 * Print the composite message and the embedded stack trace to
	 * the specified stream <code>ps</code>.
	 * @param ps the print stream
	 */
	public void printStackTrace(PrintStream ps) {
		synchronized(ps) {
			ps.println("TMSException:");
			if(stack_trace != null) {
				ps.println(this);
				ps.print(stack_trace);
			} else
				super.printStackTrace(ps);
		}
	}

	/** Prints the composite message to <code>System.err</code> */
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	/**
	 * Prints the composite message and the embedded stack trace to
	 * the specified print writer <code>pw</code>.
	 * @param pw the print writer
	 */
	public void printStackTrace(PrintWriter pw) {
		synchronized(pw) {
			pw.println("TMSException:");
			if(stack_trace != null) {
				pw.println(this);
				pw.print(stack_trace);
			} else
				super.printStackTrace(pw);
		}
	}
}
