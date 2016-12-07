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
public class SamplerSet implements VehicleSampler {

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

	/** Calculate a hash code */
	@Override
	public int hashCode() {
		return samplers.hashCode();
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

	/** Check if the set contains a vehicle sampler */
	public boolean contains(VehicleSampler vs) {
		return samplers.contains(vs);
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
	@Override
	public int getCount() {
		int count = 0;
		int n_count = 0;
		for (VehicleSampler vs: samplers) {
			int c = vs.getCount();
			if (c >= 0) {
				count += c;
				n_count++;
			} else
				return MISSING_DATA;
		}
		return (n_count > 0) ? count : MISSING_DATA;
	}

	/** Get the current total flow rate */
	@Override
	public int getFlow() {
		int flow = 0;
		int n_flow = 0;
		for (VehicleSampler vs: samplers) {
			int f = vs.getFlow();
			if (f >= 0) {
				flow += f;
				n_flow++;
			} else
				return MISSING_DATA;
		}
		return (n_flow > 0) ? flow : MISSING_DATA;
	}

	/** Get the current density (vehicle per mile) */
	@Override
	public float getDensity() {
		float t_density = 0;
		int n_density = 0;
		for (VehicleSampler vs: samplers) {
			float k = vs.getDensity();
			if (k >= 0) {
				t_density += k;
				n_density++;
			}
		}
		return (n_density > 0) ? (t_density / n_density) : MISSING_DATA;
	}

	/** Get the current average speed */
	@Override
	public float getSpeed() {
		float t_speed = 0;
		int n_speed = 0;
		for (VehicleSampler vs: samplers) {
			float s = vs.getSpeed();
			if (s > 0) {
				t_speed += s;
				n_speed++;
			}
		}
		return (n_speed > 0) ? (t_speed / n_speed) : MISSING_DATA;
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
			if (sb.length() > 1)
				sb.append(' ');
			sb.append(vs.toString());
		}
		sb.append('}');
		return sb.toString();
	}
}
