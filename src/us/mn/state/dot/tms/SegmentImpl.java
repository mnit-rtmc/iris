/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import us.mn.state.dot.vault.FieldMap;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * SegmentImpl is an implementation of the Segment RMI interface. Each
 * object of this class represents one discrete segment of freeway.
 *
 * @author Douglas Lau
 */
abstract class SegmentImpl extends TMSObjectImpl implements Segment {

	/** ObjectVault table name */
	static public final String tableName = "segment";

	/** Table mapping for segment_detector relation */
	static public TableMapping mapping;

	/** Left-side segment */
	protected final boolean left;

	/** Is this a left-side segment? */
	public boolean isLeft() { return left; }

	/** Change in the number of mainline lanes */
	protected final int delta;

	/** Get the change in the number of mainline lanes */
	public int getDelta() { return delta; }

	/** Change in the number of collector-distributor lanes */
	protected final int cdDelta;

	/** Get the change in the number of collector-distributor lanes */
	public int getCdDelta() { return cdDelta; }

	/** Miles downstream of reference point */
	protected Float mile;

	/** Get the miles downstream of reference point */
	public Float getMile() { return mile; }

	/** Set the miles downstream of reference point */
	public synchronized void setMile(Float m) throws TMSException {
		if(m == mile)
			return;
		try { vault.update(this, "mile", m, getUserName()); }
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		mile = m;
	}

	/** Segment location */
	protected LocationImpl location;

	/** Get the segment location */
	public Location getLocation() {
		return location;
	}

	/** Test if a detector is valid for the segment type */
	abstract protected boolean validateDetector(DetectorImpl det);

	/** Get all detectors with a matching location */
	public Detector[] getMatchingDetectors() {
		List<DetectorImpl> dets = detList.getFiltered(location);
		Iterator<DetectorImpl> it = dets.iterator();
		while(it.hasNext()) {
			DetectorImpl det = it.next();
			if(!validateDetector(det))
				it.remove();
		}
		Detector[] result = new Detector[dets.size()];
		for(int i = 0; i < dets.size(); i++)
			result[i] = dets.get(i);
		return result;
	}

	/** Segment detectors */
	protected transient DetectorImpl[] detectors = new DetectorImpl[0];

	/** Set the array of segment detectors */
	protected synchronized void _setDetectors(DetectorImpl[] dets)
		throws TMSException
	{
		Arrays.sort(dets);
		if(dets.equals(detectors))
			return;
		mapping.update("segment", this, dets);
		detectors = dets;
		notifyUpdate();
	}

	/** Set the array of segment detectors */
	public void setDetectors(Detector[] dets) throws TMSException,
		RemoteException
	{
		DetectorImpl[] n_dets = new DetectorImpl[dets.length];
		for(int i = 0; i < n_dets.length; i++) {
			n_dets[i] = (DetectorImpl)detList.getElement(
				dets[i].getIndex());
		}
		_setDetectors(n_dets);
	}

	/** Get an array of all segment detectors */
	public Detector[] getDetectors() {
		DetectorImpl[] dets = detectors;	// Avoid race
		Detector[] result = new Detector[dets.length];
		for(int i = 0; i < dets.length; i++)
			result[i] = dets[i];
		return result;
	}

	/** Get the detector set for the given detector type */
	public DetectorSet getDetectorSet(short type) {
		DetectorImpl[] dets = detectors;	// Avoid race
		DetectorSet set = new DetectorSet();
		for(int i = 0; i < dets.length; i++) {
			DetectorImpl d = dets[i];
			if(type == d.getLaneType())
				set.addDetector(d);
		}
		return set;
	}

	/** Does this segment have the specified detector? */
	public boolean hasDetector(DetectorImpl det) {
		DetectorImpl[] dets = detectors;	// Avoid race
		for(int i = 0; i < dets.length; i++) {
			if(dets[i] == det)
				return true;
		}
		return false;
	}

	/**
	 * Create a new segment.
	 * @param left left-side segment flag
	 * @param delta change in the number of mainline lanes
	 * @param cdDelta change in the number of collector-distributor lanes
	 */
	public SegmentImpl(boolean left, int delta, int cdDelta)
		throws RemoteException
	{
		this.left = left;
		this.delta = delta;
		this.cdDelta = cdDelta;
		location = new LocationImpl();
		notes = "";
	}

	/** Create a segment from an ObjectVault field map */
	protected SegmentImpl(FieldMap fields) throws RemoteException {
		left = fields.getBoolean("left");
		delta = fields.getInt("delta");
		cdDelta = fields.getInt("cdDelta");
		location = (LocationImpl)fields.get("location");
	}

	/** Initialize transient fields */
	public void initTransients() throws TMSException {
		// NOTE: must be called after detList is populated
		LinkedList<DetectorImpl> dets =
			new LinkedList<DetectorImpl>();
		Set s = mapping.lookup("segment", this);
		Iterator it = s.iterator();
		while(it.hasNext()) {
			Integer det_no = (Integer)it.next();
			dets.add((DetectorImpl)detList.getElement(
				det_no.intValue()));
		}
		detectors = (DetectorImpl[])dets.toArray(new DetectorImpl[0]);
		Arrays.sort(detectors);
		segMap.add(getOID(), this);
	}

	/** Validate the segment from the previous segment's values */
	public boolean validate(int pl, int ps, int pc) {
		lanes = pl + delta;
		shift = ps;
		if(left)
			shift += delta;
		cd = pc + cdDelta;
		if(lanes < 1 || lanes > 5 || cd < 0 || cd > 2)
			return false;
		else
			return true;
	}

	/** Number of mainline lanes downstream of this segment */
	protected transient int lanes;

	/** Get the number of mainline lanes downstream of this segment */
	public int getLanes() { return lanes; }

	/** Yellow fog line (left side) reference shift */
	protected transient int shift;

	/** Get the yellow fog line (left side) reference shift */
	public int getShift() { return shift; }

	/** Number of collector-distributor lanes */
	protected transient int cd;

	/** Get the number of collector-distributor lanes */
	public int getCd() { return cd; }

	/** Administrator notes for this segment */
	protected String notes;

	/** Get the administrator notes */
	public String getNotes() { return notes; }

	/** Set the administrator notes */
	public synchronized void setNotes(String n) throws TMSException {
		if(n.equals(notes))
			return;
		validateText(n);
		try { vault.update(this, "notes", n, getUserName()); }
		catch(ObjectVaultException e) {
			throw new TMSException(e);
		}
		notes = n;
	}
}
