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
import java.util.Iterator;
import java.rmi.RemoteException;
import us.mn.state.dot.vault.ObjectVaultException;
import us.mn.state.dot.sonar.NamespaceError;

/**
 * RampMeterListImpl is the implementation of the DeviceList RMI
 * interface. It maintains a list of all ramp meters plus "available"
 * meters (not assigned to a controller).
 *
 * @author Douglas Lau
 */
class RampMeterListImpl extends SortedListImpl implements DeviceList {

	/** Ramp meter list XML file */
	static protected final String METER_XML = "ramp_meters.xml";

	/** Available ramp meter list (not assigned to a controller) */
	protected transient SubsetList available;

	/** Get the list of available ramp meters */
	public SortedList getAvailableList() { return available; }

	/** Initialize the subset list */
	protected void initialize() throws RemoteException {
		available = new SubsetList( new DeviceFilter() );
	}

	/** Create a new ramp meter list */
	public RampMeterListImpl() throws RemoteException {
		super();
		initialize();
	}

	/** Load the contents of the list from the ObjectVault */
	void load(Class c, String keyField) throws ObjectVaultException,
		TMSException, RemoteException
	{
		super.load(c, keyField);
		available.addFiltered(this);
	}

	/** Add a ramp meter to the list */
	public synchronized TMSObject add( String key ) throws TMSException,
		RemoteException
	{
		RampMeterImpl meter = (RampMeterImpl)map.get( key );
		if( meter != null ) return meter;
		meter = new RampMeterImpl( key );
		try { vault.save( meter, getUserName() ); }
		catch( ObjectVaultException e ) {
			throw new TMSException( e );
		}
		map.put( key, meter );
		Iterator iterator = map.keySet().iterator();
		for( int index = 0; iterator.hasNext(); index++ ) {
			String search = (String)iterator.next();
			if( key.equals( search ) ) {
				notifyAdd( index, key );
				break;
			}
		}
		available.add( key, meter );
		return meter;
	}

	/** Update a device in the list */
	public TMSObject update(String key) {
		DeviceImpl device = (DeviceImpl)super.update(key);
		available.update(device);
		return device;
	}

	/** Remove a ramp meter from the list */
	public synchronized void remove( String key ) throws TMSException {
		GeoLocImpl geo_loc = lookupDeviceLoc(key);
		super.remove( key );
		if(geo_loc != null)
			MainServer.server.removeObject(geo_loc);
		available.remove( key );
	}

	/** Compute all ramp meter demand values */
	public synchronized void computeDemand(int interval) {
		Iterator it = iterator();
		while( it.hasNext() ) {
			RampMeterImpl meter = (RampMeterImpl)it.next();
			meter.computeDemand(interval);
		}
	}

	/** Validate all ramp meter timing plans */
	public synchronized void validateTimingPlans(int interval) {
		Iterator it = iterator();
		while( it.hasNext() ) {
			RampMeterImpl meter = (RampMeterImpl)it.next();
			meter.validateTimingPlans(interval);
		}
	}

	/** Test if a meter is on the specified corridor */
	static protected boolean matchesCorridor(Road freeway, short fd,
		RampMeterImpl meter)
	{
		GeoLoc loc = meter.lookupGeoLoc();
		Road f = loc.getFreeway();
		if(freeway == null || f == null)
			return false;
		return (freeway == f) &&
			(GeoLocHelper.filterDirection(fd, f) ==
			 GeoLocHelper.filterDirection(loc.getFreeDir(), f));
	}

	/** Find a stratified timing plan from the same freeway/direction */
	public synchronized StratifiedPlanImpl findStratifiedPlan(Road freeway,
		short freeDir, int period)
	{
		Iterator it = iterator();
		while(it.hasNext()) {
			RampMeterImpl meter = (RampMeterImpl)it.next();
			if(matchesCorridor(freeway, freeDir, meter)) {
				StratifiedPlanImpl plan =
					meter.findStratifiedPlan(period);
				if(plan != null)
					return plan;
			}
		}
		return null;
	}

	/** Write the ramp meter list in XML format */
	public void writeXml() throws IOException {
		XmlWriter w = new XmlWriter(METER_XML, false) {
			public void print(PrintWriter out) {
				printXmlBody(out);
			}
		};
		try {
			w.write();
		}
		catch(NamespaceError e) {
			e.printStackTrace();
		}
	}

	/** Print the body of the ramp meter list XML file */
	protected void printXmlBody(PrintWriter out) {
		Iterator it = getIterator();
		while(it.hasNext()) {
			RampMeterImpl meter = (RampMeterImpl)it.next();
			meter.printXmlElement(out);
		}
	}
}
