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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * DetectorListImpl
 *
 * @author Douglas Lau
 */
class DetectorListImpl extends IndexedListImpl implements DetectorList {

	/** Detector list XML file */
	static protected final String DETS_XML = "detectors.xml";

	/** Detector sample file */
	static protected final String SAMPLE_XML = "det_sample.xml";

	/** Available detector list (not assigned to a controller) */
	protected transient SubsetList available;

	/** Get a list of 'free' detectors */
	public SortedList getAvailableList() { return available; }

	/** Free mainline detector list (not assigned to a station) */
	protected transient SubsetList mainFree;

	/** Get a list of the free mainline detectors */
	public SortedList getMainFreeList() { return mainFree; }

	/** Initialize the subset lists */
	protected void initialize() throws RemoteException {
		available = new SubsetList( new DeviceFilter() );
		mainFree = new SubsetList( new DeviceFilter() {
			public boolean allow( TMSObjectImpl obj ) {
				DetectorImpl det = (DetectorImpl)obj;
				return det.isFreeMainline();
			}
		} );
	}

	/** Create a new detector list */
	public DetectorListImpl() throws RemoteException {
		super( false );
		initialize();
	}

	/** Load the contents of the list from the ObjectVault */
	void load(Class c, String keyField) throws ObjectVaultException,
		TMSException, RemoteException
	{
		// NOTE: this is copied out of IndexedListImpl to move the
		// initTransients calls after all detectors have been loaded
		list.clear();
		Iterator it = vault.lookup(c, keyField);
		while(it.hasNext())
			list.add((TMSObjectImpl)vault.load(it.next()));
		available.addFiltered(this);
		mainFree.addFiltered(this);
		// This must happen last for fake detector lookups
		for(TMSObjectImpl object: list) {
			DetectorImpl det = (DetectorImpl)object;
			det.initTransients();
		}
	}

	/** Append a detector to the list */
	public synchronized TMSObject append() throws TMSException,
		RemoteException
	{
		int index = list.size();
		DetectorImpl det = new DetectorImpl( index + 1 );
		try { vault.save( det, getUserName() ); }
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		list.add( det );
		notifyAdd( index, det.toString() );
		available.update( det );
		mainFree.update( det );
		return det;
	}

	/** Update a detector in the list */
	public TMSObject update(int index) {
		DetectorImpl det = (DetectorImpl)super.update( index );
		available.update( det );
		mainFree.update( det );
		return det;
	}

	/** Remove the last detector from the list */
	public synchronized void removeLast() throws TMSException {
		if( list.isEmpty() ) throw new
			ChangeVetoException( "List is empty" );
		DetectorImpl det = (DetectorImpl)list.get( list.size() - 1 );
		GeoLocImpl geo_loc = det.lookupGeoLoc();
		String s = det.getId();
		super.removeLast();
		if(geo_loc != null)
			MainServer.server.removeObject(geo_loc);
		available.remove( s );
		mainFree.remove( s );
	}

	/** Get a thread-safe iterator over the list */
	protected synchronized Iterator getIterator() {
		ArrayList dets = (ArrayList)list.clone();
		return dets.iterator();
	}

	/** Get a filtered list of detectors matching a location */
	public synchronized List<DetectorImpl> getFiltered(GeoLoc loc) {
		LinkedList<DetectorImpl> r = new LinkedList<DetectorImpl>();
		Iterator it = list.iterator();
		while(it.hasNext()) {
			DetectorImpl d = (DetectorImpl)it.next();
			if(GeoLocHelper.matches(loc, d.lookupGeoLoc()))
				r.add(d);
		}
		return r;
	}

	/** Flush all detector data to disk */
	public void flush(Calendar stamp) {
		System.err.println("Starting FLUSH @ " + new Date() + " for " +
			stamp.getTime());
		Iterator it = getIterator();
		while(it.hasNext()) {
			DetectorImpl det = (DetectorImpl)it.next();
			det.flush(stamp);
		}
		System.err.println("Finished FLUSH @ " + new Date());
	}

	/** Calculate fake detector flow rates */
	public void calculateFakeFlows() {
		Iterator it = getIterator();
		while(it.hasNext()) {
			DetectorImpl det = (DetectorImpl)it.next();
			det.calculateFakeFlow();
		}
	}

	/** Write the detector list in XML format */
	public void writeXml() throws IOException {
		XmlWriter w = new XmlWriter(DETS_XML, false) {
			public void print(PrintWriter out) {
				printXmlBody(out);
			}
		};
		w.write();
	}

	/** Print the body of the detector list XML file */
	protected void printXmlBody(PrintWriter out) {
		Iterator it = getIterator();
		while(it.hasNext()) {
			DetectorImpl det = (DetectorImpl)it.next();
			det.printXmlElement(out);
		}
	}

	/** Write the sample data out as XML */
	public void writeSampleXml() {
		XmlWriter w = new XmlWriter(SAMPLE_XML, true) {
			public void print(PrintWriter out) {
				printSampleXmlHead(out);
				printSampleXmlBody(out);
				printSampleXmlTail(out);
			}
		};
		try { w.write(); }
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Print the header of the detector sample XML file */
	protected void printSampleXmlHead(PrintWriter out) {
		out.println("<?xml version='1.0'?>");
		out.println("<!DOCTYPE traffic_sample SYSTEM 'tms.dtd'>");
		out.println("<traffic_sample time_stamp='" + new Date() +
			"' period='30'>");
		out.println("\t&detectors;");
	}

	/** Print the body of the detector sample XML file */
	protected void printSampleXmlBody(PrintWriter out) {
		Iterator it = getIterator();
		while(it.hasNext()) {
			DetectorImpl det = (DetectorImpl)it.next();
			det.printSampleXmlElement(out);
		}
	}

	/** Print the tail of the detector sample XML file */
	protected void printSampleXmlTail(PrintWriter out) {
		out.println("</traffic_sample>");
	}
}
