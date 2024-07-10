/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * Fake Detector
 *
 * @author Douglas Lau
 */
public class FakeDetector implements VehicleSampler {

	/** Calculate the average from a total and count */
	static private float calculateAverage(float total, int count) {
		return (count > 0) ? total / count : MISSING_DATA;
	}

	/** Array of detectors */
	private final DetectorImpl[] dets;

	/** Get a string representation of the fake detector */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (DetectorImpl det: dets) {
			if (b.length() > 0)
				b.append(' ');
			b.append(det.getName());
		}
		return b.toString();
	}

	/** Create a new fake detector */
	public FakeDetector(String d) throws NumberFormatException {
		ArrayList<DetectorImpl> p = new ArrayList<DetectorImpl>();
		for (String t: d.split(" ")) {
			Detector dt = DetectorHelper.lookup(t);
			if (dt instanceof DetectorImpl)
				p.add((DetectorImpl) dt);
		}
		dets = p.toArray(new DetectorImpl[0]);
	}

	/** Get a vehicle count */
	@Override
	public int getVehCount(long stamp, int per_ms) {
		int count = 0;
		for (int i = 0; i < dets.length; i++) {
			int c = dets[i].getVehCount(stamp, per_ms);
			if (c < 0)
				return MISSING_DATA;
			count += c;
		}
		return count;
	}

	/** Get total flow rate */
	@Override
	public int getFlow(long stamp, int per_ms) {
		int flow = 0;
		for (int i = 0; i < dets.length; i++) {
			int f = dets[i].getFlowRaw(stamp, per_ms);
			if (f < 0)
				return MISSING_DATA;
			flow += f;
		}
		return flow;
	}

	/** Get the fake density (vehicle per mile) */
	@Override
	public float getDensity(long stamp, int per_ms) {
		float t_density = 0;
		int n_density = 0;
		for (DetectorImpl det: dets) {
			float k = det.getDensityRaw(stamp, per_ms, false);
			if (k >= 0) {
				t_density += k;
				n_density++;
			}
		}
		return calculateAverage(t_density, n_density);
	}

	/** Get the fake speed (miles per hour) */
	@Override
	public float getSpeed(long stamp, int per_ms) {
		float t_speed = 0;
		int n_speed = 0;
		for (DetectorImpl det: dets) {
			float s = det.getSpeedRaw(stamp, per_ms);
			if (s > 0) {
				t_speed += s;
				n_speed++;
			}
		}
		return calculateAverage(t_speed, n_speed);
	}
}
