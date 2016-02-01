/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * A sampler set is a logical grouping of vehicle samplers.
 *
 * @author Douglas Lau
 */
public class SamplerSet {

	/** Vehicle sampler filter */
	static public interface Filter {
		boolean check(DetectorImpl d);
	}

	/** Set of samplers */
	private final HashSet<DetectorImpl> samplers =
		new HashSet<DetectorImpl>();

	/** Create an empty sampler set */
	public SamplerSet() {
	}

	/** Create a new sampler set */
	public SamplerSet(Collection<DetectorImpl> dets) {
		samplers.addAll(dets);
	}

	/** Filter a vehicle sampler set */
	public ArrayList<DetectorImpl> filter(Filter f) {
		ArrayList<DetectorImpl> dets = new ArrayList<DetectorImpl>();
		for (DetectorImpl d: samplers) {
			if (f.check(d))
				dets.add(d);
		}
		return dets;
	}

	/** Get all samplers */
	public ArrayList<DetectorImpl> getAll() {
		return new ArrayList<DetectorImpl>(samplers);
	}

	/** Get the number of samplers in the sampler set */
	public int size() {
		return samplers.size();
	}

	/** Check if another vehicle sampler set equals this */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SamplerSet) {
			SamplerSet ss = (SamplerSet) obj;
			return samplers.equals(ss.samplers);
		} else
			return false;
	}

	/** Test if the sampler set is (defined and) sampling "real" data */
	public boolean isPerfect() {
		for (DetectorImpl det: samplers) {
			if (!det.isSampling())
				return false;
		}
		return (samplers.size() > 0);
	}

	/** Get the current total volume */
	public int getVolume() {
		boolean defined = false;
		int vol = 0;
		for (DetectorImpl det: samplers) {
			int v = det.getVolume();
			if (v < 0)
				return MISSING_DATA;
			vol += v;
			defined = true;
		}
		return defined ? vol : MISSING_DATA;
	}

	/** Get the current total flow rate */
	public int getFlow() {
		boolean defined = false;
		int flow = 0;
		for (DetectorImpl det: samplers) {
			int f = det.getFlow();
			if (f < 0)
				return MISSING_DATA;
			flow += f;
			defined = true;
		}
		return defined ? flow : MISSING_DATA;
	}

	/** Check if a detector is in the set */
	public boolean hasDetector(DetectorImpl det) {
		return samplers.contains(det);
	}

	/** Get the maximum occupancy */
	public float getMaxOccupancy() {
		float occ = 0;
		for (DetectorImpl det: samplers)
			occ = Math.max(det.getOccupancy(), occ);
		return occ;
	}

	/** Get a string representation (for debugging) */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('\'');
		for (DetectorImpl det: samplers) {
			sb.append(det.getName());
			sb.append(' ');
		}
		if (samplers.size() == 0)
			sb.append('\'');
		else
			sb.setCharAt(sb.length() - 1, '\'');
		return sb.toString();
	}
}
