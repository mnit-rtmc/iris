/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2024  Minnesota Department of Transportation
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

import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * Speed smoother uses heuristics to calculate a variable-length average of
 * 30-second speed data.
 *
 * @author Douglas Lau
 */
public class SpeedSmoother {

	/** Speed ranks for extending rolling bin averaging */
	static private enum SpeedRank {
		First(40, 2),   // 40+ mph => 2 bins (1 minute)
		Second(25, 4),  // 25-40 mph => 4 bins (2 minutes)
		Third(20, 6),   // 20-25 mph => 6 bins (3 minutes)
		Fourth(15, 8),  // 15-20 mph => 8 bins (4 minutes)
		Last(0, 10);    // 0-15 mph => 10 bins (5 minutes)
		private final int speed;
		private final int n_bins;
		private SpeedRank(int s, int b) {
			speed = s;
			n_bins = b;
		}
		/** Get the rolling bin count for the given speed */
		static private int bins(float s) {
			for (SpeedRank sr: values()) {
				if (s > sr.speed)
					return sr.n_bins;
			}
			return Last.n_bins;
		}
	}

	/** Density ranks for calculating rolling bin count */
	static private enum DensityRank {
		First(55, 6),   // 55+ vpm => 6 bins (3 minutes)
		Second(40, 4),  // 40-55 vpm => 4 bins (2 minutes)
		Third(25, 3),   // 25-40 vpm => 3 bins (1.5 minutes)
		Fourth(15, 4),  // 15-25 vpm => 4 bins (2 minutes)
		Fifth(10, 6),   // 10-15 vpm => 6 bins (3 minutes)
		Last(0, 0);     // less than 10 vpm => 0 bins
		private final int density;
		private final int n_bins;
		private DensityRank(int k, int b) {
			density = k;
			n_bins = b;
		}
		/** Get the rolling bin count for the given density */
		static private int bins(float k) {
			for (DensityRank dr: values()) {
				if (k > dr.density)
					return dr.n_bins;
			}
			return Last.n_bins;
		}
	}

	/** Binned speed data (most recent first) */
	private final float[] speeds;

	/** Current 30-second bin count for density ranking */
	private int n_bins;

	/** Create a new speed data smoother */
	public SpeedSmoother() {
		speeds = new float[10];
		for (int i = 0; i < speeds.length; i++)
			speeds[i] = MISSING_DATA;
		n_bins = 0;
	}

	/** Push a binned 30-second speed */
	public void push(float s) {
		System.arraycopy(speeds, 0, speeds, 1, speeds.length - 1);
		speeds[0] = (s > 0) ? s : MISSING_DATA;
	}

	/** Get smoothed value using speed ranking */
	public float speedRankedValue(int limit) {
		return average(speedBins(), limit);
	}

	/** Get the number of rolling bins for a set of speeds */
	private int speedBins() {
		int b = SpeedRank.First.n_bins;
		// NOTE: b might be changed inside loop,
		//       extending the for loop bounds
		for (int i = 0; i < b; i++) {
			float s = speeds[i];
			if (s > 0)
				b = Math.max(b, SpeedRank.bins(s));
		}
		return b;
	}

	/** Calculate the average of binned data */
	private float average(int n, float limit) {
		float total = 0;
		int count = 0;
		for (int i = 0; i < n; i++) {
			float s = Math.min(speeds[i], limit);
			if (s > 0) {
				total += s;
				count += 1;
			}
		}
		return (count > 0) ? total / count : MISSING_DATA;
	}

	/** Get smoothed value using density ranking */
	public float densityRankedValue(int limit) {
		return average(n_bins, limit);
	}

	/** Set density (updating 30-second bin count) */
	public void setDensity(float density) {
		n_bins = Math.min(calculateMaxBins(density), n_bins + 1);
	}

	/** Calculate the maximum number of bins for smoothing */
	private int calculateMaxBins(float density) {
		return isTrending() ? 2 : DensityRank.bins(density);
	}

	/** Is the speed trending over the last three time steps? */
	private boolean isTrending() {
		return isTrendValid() &&
		      (isTrendingDownward() || isTrendingUpward());
	}

	/** Is trending data valid? */
	private boolean isTrendValid() {
		return n_bins >= 2 &&
		       speeds[0] > 0 && speeds[1] > 0 && speeds[2] > 0;
	}

	/** Is the speed trending downward? */
	private boolean isTrendingDownward() {
		return speeds[0] < speeds[1] && speeds[1] < speeds[2];
	}

	/** Is the speed trending upward? */
	private boolean isTrendingUpward() {
		return speeds[0] > speeds[1] && speeds[1] > speeds[2];
	}
}
