/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * DMSListImpl is a list containing all DMS objects, plus all global
 * message lists and sign parameters.
 *
 * @author Douglas Lau
 */
public class DMSListImpl extends SortedListImpl implements DMSList {

	/** Location of travel time XML file */
	static protected final String TRAVEL_XML = "device_status.xml";

	/** Create a new DMS list */
	public DMSListImpl() throws TMSException, RemoteException {
		super();
	}

	/** Add a dynamic message sign to the list */
	public synchronized TMSObject add( String key ) throws TMSException,
		RemoteException
	{
		DMSImpl sign = (DMSImpl)map.get( key );
		if( sign != null ) return sign;
		if(key.startsWith("V"))
			sign = new DMSImpl(key);
		else
			throw new ChangeVetoException("Must begin with V");
		try { vault.save( sign, getUserName() ); }
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		map.put( key, sign );
		Iterator<String> it = map.keySet().iterator();
		for(int index = 0; it.hasNext(); index++) {
			String search = it.next();
			if(key.equals(search)) {
				notifyAdd(index, key);
				break;
			}
		}
		return sign;
	}

	/** Remove a dynamic message sign from the list */
	public synchronized void remove( String key ) throws TMSException {
		super.remove( key );
		deviceList.remove( key );
	}

	/** Get an iterator of all signs */
	protected synchronized Iterator<TMSObjectImpl> iterator() {
		ArrayList<TMSObjectImpl> list =
			new ArrayList<TMSObjectImpl>(map.values());
		return list.iterator();
	}

	/** Send an alert to all signs in the specified group */
	public void sendGroup(String group, String owner, String[] text)
		throws TMSException
	{
		StringBuffer b = new StringBuffer();
		for(int i = 0; i < text.length; i++) {
			if(i > 0)
				b.append("[nl]");
			validateText(text[i]);
			b.append(text[i]);
		}
		// FIXME: log alert in log database
		// FIXME: add sign group support here
		Iterator<TMSObjectImpl> i = iterator();
		while(i.hasNext()) {
			DMSImpl dms = (DMSImpl)i.next();
			try {
				dms.setAlert(owner, b.toString());
			}
			catch(InvalidMessageException e) {
				// Ignore for this sign
			}
		}
	}

	/** Clear all signs in the specified group */
	public void clearGroup(String group, String owner) {
		// FIXME: log alert in log database
		// FIXME: add sign group support here
		Iterator<TMSObjectImpl> i = iterator();
		while(i.hasNext()) {
			DMSImpl dms = (DMSImpl)i.next();
			dms.clearAlert(owner);
		}
	}

	/** Update travel times for all signs */
	public void updateTravelTimes(final int interval) {
		XmlWriter w = new XmlWriter(TRAVEL_XML, false) {
			public void print(PrintWriter out) {
				printTravelXmlHead(out);
				printTravelXmlBody(out, interval);
				printTravelXmlTail(out);
			}
		};
		try { w.write(); }
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Print the header of the travel time XML file */
	protected void printTravelXmlHead(PrintWriter out) {
		out.println("<?xml version='1.0'?>");
		out.println("<device_status>");
	}

	/** Print the body of the travel time XML file */
	protected void printTravelXmlBody(PrintWriter out, int interval) {
		Iterator<TMSObjectImpl> it = iterator();
		while(it.hasNext()) {
			DMSImpl dms = (DMSImpl)it.next();
			dms.updateTravelTimes(interval);
		}
	}

	/** Print the body of the travel time XML file */
	protected void printTravelXmlTail(PrintWriter out) {
		out.println("</device_status>");
	}

	/** toString */
	public String toString() {
		String ret = "DMSListImpl: len=" + map.size() + "\n";
		Iterator<String> it = map.keySet().iterator();
		for(int index = 0; it.hasNext(); index++) {
			String id = it.next();
			DMSImpl dms = (DMSImpl)map.get(id);
			if(dms == null)
				continue;
			ret += "DMSList[" + index + "]:" + "Id=" + dms.getId() +
				", getStatusCode()="+dms.getStatusCode()+", notes=" +
				dms.getNotes() + "\n";
		}
		return ret;
	}
}
