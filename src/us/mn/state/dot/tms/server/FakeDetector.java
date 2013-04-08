/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.Detector;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * Fake Detector
 *
 * @author Douglas Lau
 */
public class FakeDetector {

	/** Calculate the average from a total and sample count */
	static protected float calculateAverage(float total, int count) {
		if(count > 0)
			return total / count;
		else
			return MISSING_DATA;
	}

	/** Plus composite type */
	static protected final int PLUS = 1;

	/** Minus composite type */
	static protected final int MINUS = -1;

	/** Constant composite type */
	static protected final int CONSTANT = 0;

	/** Percent composite type */
	static protected final int PERCENT = 2;

	/** Array of detectors which add to the fake detector */
	protected DetectorImpl[] plus;

	/** Array of detectors which subtract from the fake detector */
	protected DetectorImpl[] minus;

	/** Constant flow rate to begin estimation */
	protected int constant = 0;

	/** Percent to apply at end of estimation */
	protected int percent = 100;

	/** Get a string representation of the fake detector */
	public String toString() {
		StringBuilder b = new StringBuilder();
		if(constant != 0) {
			b.append('#');
			b.append(constant);
		}
		for(DetectorImpl det: plus) {
			if(b.length() > 0)
				b.append('+');
			b.append(det.getName());
		}
		for(DetectorImpl det: minus) {
			b.append('-');
			b.append(det.getName());
		}
		if(percent != 100) {
			b.append('%');
			b.append(percent);
		}
		return b.toString();
	}

	/** Create a new fake detector */
	public FakeDetector(String d, Namespace ns)
		throws NumberFormatException
	{
		LinkedList<DetectorImpl> p = new LinkedList<DetectorImpl>();
		LinkedList<DetectorImpl> m = new LinkedList<DetectorImpl>();
		int type = PLUS;
		StringTokenizer tok = new StringTokenizer(d, " +-#%", true);
		while(tok.hasMoreTokens()) {
			String t = tok.nextToken();
			if(t.equals("+")) {
				type = PLUS;
				continue;
			}
			if(t.equals("-")) {
				type = MINUS;
				continue;
			}
			if(t.equals("#")) {
				type = CONSTANT;
				continue;
			}
			if(t.equals("%")) {
				type = PERCENT;
				continue;
			}
			if(t.equals(" "))
				continue;
			if(type == CONSTANT) {
				constant = Integer.parseInt(t);
				continue;
			}
			if(type == PERCENT) {
				percent = Integer.parseInt(t);
				continue;
			}
			DetectorImpl det = (DetectorImpl)ns.lookupObject(
				Detector.SONAR_TYPE, t);
			if(det != null) {
				if(type == PLUS)
					p.add(det);
				if(type == MINUS)
					m.add(det);
			}
		}
		plus = (DetectorImpl [])p.toArray(new DetectorImpl[0]);
		minus = (DetectorImpl [])m.toArray(new DetectorImpl[0]);
	}

	/** Calculate the fake detector data */
	public void calculate() {
		flow = calculateFlow();
		calculateSpeed();
	}

	/** Flow rate from earlier sampling interval */
	protected transient float flow = MISSING_DATA;

	/** Left over flow from earlier sampling intervals */
	protected transient float leftover = 0;

	/** Calculate the fake detector flow rate */
	private float calculateFlow() {
		float flw = 0;
		for(int i = 0; i < plus.length; i++) {
			int f = plus[i].getFlowRaw();
			if(f < 0) {
				leftover = 0;
				return MISSING_DATA;
			}
			flw += f;
		}
		for(int i = 0; i < minus.length; i++) {
			int f = minus[i].getFlowRaw();
			if(f < 0) {
				leftover = 0;
				return MISSING_DATA;
			}
			flw -= f;
		}
		if(flw < 0) {
			leftover += flw;
			flw = 0;
		}
		else if(leftover < 0) {
			float diff = Math.max(leftover, -flw);
			leftover -= diff;
			flw += diff;
		}
		float ff = (constant + flw) * percent / 100.0f;
		float adj = flow > 0 ? ff - flow : ff;
		return flow + 0.01f * adj;
	}

	/** Get the calculated flow rate */
	public float getFlow() {
		return flow;
	}

	/** Speed from earlier sampling interval */
	protected transient float speed = MISSING_DATA;

	/** Calculate the fake detector speed */
	private void calculateSpeed() {
		float t_speed = 0;
		int n_speed = 0;
		for(DetectorImpl det: plus) {
			float s = det.getSpeedRaw();
			if(s > 0) {
				t_speed += s;
				n_speed++;
			}
		}
		speed = calculateAverage(t_speed, n_speed);
	}

	/** Get the calculated speed */
	public float getSpeed() {
		return speed;
	}
}
