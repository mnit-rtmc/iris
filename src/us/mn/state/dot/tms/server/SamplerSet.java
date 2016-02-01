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
import us.mn.state.dot.tms.LaneType;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * A sampler set is a logical grouping of vehicle samplers.
 *
 * @author Douglas Lau
 */
public class SamplerSet {

	/** Vehicle sampler filter */
	static public interface Filter {
		boolean check(VehicleSampler d);
	}

	/** Set of samplers */
	private final HashSet<VehicleSampler> samplers =
		new HashSet<VehicleSampler>();

	/** Create an empty sampler set */
	public SamplerSet() { }

	/** Create a new sampler set */
	public SamplerSet(Collection<? extends VehicleSampler> dets) {
		samplers.addAll(dets);
	}

	/** Filter a vehicle sampler set */
	public ArrayList<VehicleSampler> filter(Filter f) {
		ArrayList<VehicleSampler> dets = new ArrayList<VehicleSampler>();
		for (VehicleSampler d: samplers) {
			if (f.check(d))
				dets.add(d);
		}
		return dets;
	}

	/** Filter a vehicle sampler set */
	public ArrayList<VehicleSampler> filter(final LaneType lt) {
		return filter(new Filter() {
			public boolean check(VehicleSampler vs) {
				if (vs instanceof DetectorImpl) {
					DetectorImpl d = (DetectorImpl) vs;
					return lt.ordinal() == d.getLaneType();
				} else
					return false;
			}
		});
	}

	/** Get all samplers */
	public ArrayList<VehicleSampler> getAll() {
		return new ArrayList<VehicleSampler>(samplers);
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
		for (VehicleSampler vs: samplers) {
			if (vs instanceof DetectorImpl) {
				DetectorImpl det = (DetectorImpl) vs;
				if (!det.isSampling())
					return false;
			} else
				return false;
		}
		return (samplers.size() > 0);
	}

	/** Get the current total count */
	public int getCount() {
		boolean defined = false;
		int vol = 0;
		for (VehicleSampler vs: samplers) {
			int v = vs.getCount();
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
		for (VehicleSampler det: samplers) {
			int f = det.getFlow();
			if (f < 0)
				return MISSING_DATA;
			flow += f;
			defined = true;
		}
		return defined ? flow : MISSING_DATA;
	}

	/** Check if a detector is in the set */
	public boolean hasDetector(VehicleSampler vs) {
		return samplers.contains(vs);
	}

	/** Get the maximum occupancy */
	public float getMaxOccupancy() {
		float occ = 0;
		for (VehicleSampler vs: samplers) {
			if (vs instanceof DetectorImpl) {
				DetectorImpl det = (DetectorImpl) vs;
				occ = Math.max(det.getOccupancy(), occ);
			}
		}
		return occ;
	}

	/** Get a string representation (for debugging) */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (VehicleSampler vs: samplers) {
			if (sb.length() > 0)
				sb.append(' ');
			sb.append(vs.toString());
		}
		sb.append('}');
		return sb.toString();
	}
}
