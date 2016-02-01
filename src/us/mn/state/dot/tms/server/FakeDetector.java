/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2016  Minnesota Department of Transportation
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

import java.util.LinkedList;
import java.util.StringTokenizer;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * Fake Detector
 *
 * @author Douglas Lau
 */
public class FakeDetector implements VehicleSampler {

	/** Calculate the average from a total and sample count */
	static private float calculateAverage(float total, int count) {
		return (count > 0) ? total / count : MISSING_DATA;
	}

	/** Enum of composites */
	private enum Composite {
		PLUS, CONSTANT, PERCENT;
	}

	/** Array of detectors which add to the fake detector */
	private final DetectorImpl[] plus;

	/** Constant flow rate to begin estimation */
	private int constant = 0;

	/** Percent to apply at end of estimation */
	private int percent = 100;

	/** Get a string representation of the fake detector */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (constant != 0) {
			b.append('#');
			b.append(constant);
		}
		for (DetectorImpl det: plus) {
			if (b.length() > 0)
				b.append('+');
			b.append(det.getName());
		}
		if (percent != 100) {
			b.append('%');
			b.append(percent);
		}
		return b.toString();
	}

	/** Create a new fake detector */
	public FakeDetector(String d) throws NumberFormatException {
		LinkedList<DetectorImpl> p = new LinkedList<DetectorImpl>();
		Composite comp = Composite.PLUS;
		StringTokenizer tok = new StringTokenizer(d, " +#%", true);
		while (tok.hasMoreTokens()) {
			String t = tok.nextToken();
			if (t.equals("+")) {
				comp = Composite.PLUS;
				continue;
			}
			if (t.equals("#")) {
				comp = Composite.CONSTANT;
				continue;
			}
			if (t.equals("%")) {
				comp = Composite.PERCENT;
				continue;
			}
			if (t.equals(" "))
				continue;
			if (comp == Composite.CONSTANT) {
				constant = Integer.parseInt(t);
				continue;
			}
			if (comp == Composite.PERCENT) {
				percent = Integer.parseInt(t);
				continue;
			}
			Detector dt = DetectorHelper.lookup(t);
			if (dt instanceof DetectorImpl) {
				DetectorImpl det = (DetectorImpl) dt;
				if (comp == Composite.PLUS)
					p.add(det);
			}
		}
		plus = (DetectorImpl []) p.toArray(new DetectorImpl[0]);
	}

	/** Get the most recent sample count */
	@Override
	public int getCount() {
		int count = 0;
		for (int i = 0; i < plus.length; i++) {
			int c = plus[i].getCount();
			if (c < 0)
				return MISSING_DATA;
			count += c;
		}
		return count;
	}

	/** Get the calculated flow rate */
	@Override
	public int getFlow() {
		float flow = 0;
		for (int i = 0; i < plus.length; i++) {
			int f = plus[i].getFlowRaw();
			if (f < 0)
				return MISSING_DATA;
			flow += f;
		}
		return Math.round((constant + flow) * percent / 100.0f);
	}

	/** Get the fake density (vehicle per mile) */
	@Override
	public float getDensity() {
		float t_density = 0;
		int n_density = 0;
		for (DetectorImpl det: plus) {
			float k = det.getDensityRaw();
			if (k >= 0) {
				t_density += k;
				n_density++;
			}
		}
		return calculateAverage(t_density, n_density);
	}

	/** Get the fake speed (miles per hour) */
	@Override
	public float getSpeed() {
		float t_speed = 0;
		int n_speed = 0;
		for (DetectorImpl det: plus) {
			float s = det.getSpeedRaw();
			if (s > 0) {
				t_speed += s;
				n_speed++;
			}
		}
		return calculateAverage(t_speed, n_speed);
	}
}
