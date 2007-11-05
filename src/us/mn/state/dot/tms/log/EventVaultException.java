/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.log;

/**
 * Super class of all events thrown from the event vault.
 *
 * @author    <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @created   May 17, 2002
 */
public class EventVaultException extends Exception {

	/** Nested exception */
	protected final Exception nested;


	/**
	 * Create a new event vault exception
	 *
	 * @param e  The exception wrapped by the EventVaultException
	 */
	public EventVaultException( Exception e ) {
		nested = e;
	}


	/**
	 * Create a new event vault exception
	 *
	 * @param m  The message for the new EventVaultException
	 */
	public EventVaultException( String m ) {
		super( m );
		nested = null;
	}


	/**
	 * Create a new event vault exception
	 *
	 * @param m  The message for the new EventVaultException
	 * @param e  The exception wrapped by the EventVaultException
	 */
	public EventVaultException( String m, Exception e ) {
		super( m );
		nested = e;
	}


	/**
	 * Returns the detail message, including the message from the nested exception
	 * if there is one.
	 *
	 * @return   The exception's message
	 */
	public String getMessage() {
		if ( nested != null ) {
			return "EventVaultException; " +
					super.getMessage() +
					" nested exception is: \n\t" + nested.toString();
		} else {
			return super.getMessage();
		}
	}


	/**
	 * Prints the composite message and the embedded stack trace to the specified
	 * stream <code>ps</code>.
	 *
	 * @param ps  the print stream
	 */
	public void printStackTrace( java.io.PrintStream ps ) {
		synchronized ( ps ) {
			if ( nested != null ) {
				ps.println( this );
				nested.printStackTrace( ps );
			} else {
				super.printStackTrace( ps );
			}
		}
	}


	/** Prints the composite message to <code>System.err</code> */
	public void printStackTrace() {
		printStackTrace( System.err );
	}


	/**
	 * Prints the composite message and the embedded stack trace to the specified
	 * print writer <code>pw</code>.
	 *
	 * @param pw  the print writer
	 */
	public void printStackTrace( java.io.PrintWriter pw ) {
		synchronized ( pw ) {
			if ( nested != null ) {
				pw.println( this );
				nested.printStackTrace( pw );
			} else {
				super.printStackTrace( pw );
			}
		}
	}
}
