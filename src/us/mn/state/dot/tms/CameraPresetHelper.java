/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2018  Minnesota Department of Transportation
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

import java.util.Iterator;

/**
 * Camera preset helper methods.
 *
 * @author Douglas Lau
 */
public class CameraPresetHelper extends BaseHelper {

	/** Disallow instantiation */
	protected CameraPresetHelper() {
		assert false;
	}

	/** Get a preset iterator */
	static public Iterator<CameraPreset> iterator() {
		return new IteratorWrapper<CameraPreset>(namespace.iterator(
			CameraPreset.SONAR_TYPE));
	}

	/** Lookup the preset with the specified name */
	static public CameraPreset lookup(String name) {
		return (CameraPreset) namespace.lookupObject(
			CameraPreset.SONAR_TYPE, name);
	}

	/** Lookup the preset with the camera / number */
	static public CameraPreset lookup(Camera c, int pn) {
		Iterator<CameraPreset> it = iterator();
		while (it.hasNext()) {
			CameraPreset cp = it.next();
			if (cp.getCamera() == c && cp.getPresetNum() == pn)
				return cp;
		}
		return null;
	}

	/** Lookup camera preset text */
	static public String lookupText(CameraPreset cp) {
		Device d = devicePreset(cp);
		if (d != null)
			return d.getName();
		ParkingArea pa = parkingAreaPreset(cp);
		if (pa != null)
			return pa.getName();
		Direction dir = Direction.fromOrdinal(cp.getDirection());
		if (dir != Direction.UNKNOWN)
			return dir.det_dir;
		else
			return Integer.toString(cp.getPresetNum());
	}

	/** Get a device associated with a preset */
	static private Device devicePreset(CameraPreset cp) {
		Device d = beaconPreset(cp);
		if (d != null)
			return d;
		d = dmsPreset(cp);
		if (d != null)
			return d;
		d = meterPreset(cp);
		if (d != null)
			return d;
		return null;
	}

	/** Get a beacon associated with a preset */
	static private Device beaconPreset(CameraPreset cp) {
		Iterator<Beacon> it = BeaconHelper.iterator();
		while (it.hasNext()) {
			Beacon b = it.next();
			if (b.getPreset() == cp)
				return b;
		}
		return null;
	}

	/** Get a DMS associated with a preset */
	static private Device dmsPreset(CameraPreset cp) {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS d = it.next();
			if (d.getPreset() == cp)
				return d;
		}
		return null;
	}

	/** Get a ramp meter associated with a preset */
	static private Device meterPreset(CameraPreset cp) {
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter m = it.next();
			if (m.getPreset() == cp)
				return m;
		}
		return null;
	}

	/** Get a parking area associated with a preset */
	static private ParkingArea parkingAreaPreset(CameraPreset cp) {
		Iterator<ParkingArea> it = ParkingAreaHelper.iterator();
		while (it.hasNext()) {
			ParkingArea pa = it.next();
			if ((pa.getPreset1() == cp)
			 || (pa.getPreset2() == cp)
			 || (pa.getPreset3() == cp))
				return pa;
		}
		return null;
	}
}
