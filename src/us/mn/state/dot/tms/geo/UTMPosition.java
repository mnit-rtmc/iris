/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.geo;

/**
 * A UTM position consists of the UTM zone, plus easting and northing.
 *
 * @author Douglas Lau
 */
public class UTMPosition {

	/** False Easting is at central meridian of zone */
	static private final int FALSE_EASTING = 500000;

	/** False Northing of 10,000 Km is used for the Southern hemisphere */
	static private final int FALSE_NORTHING = 10000000;

	/** Scale at meridian */
	static private final double K0 = 0.9996;

	/** Convert a (lat/lon) position to UTM */
	static public UTMPosition convert(GeodeticDatum gd, Position pos) {
		double a = gd.getEquatorialRadius();
		double e2 = gd.getEccentricitySquared();
		double ep2 = e2 / (1 - e2);
		double lat = Math.toRadians(pos.getLatitude());
		double lon = Math.toRadians(pos.getLongitude());
		double sin_lat = Math.sin(lat);
		double cos_lat = Math.cos(lat);
		double tan_lat = Math.tan(lat);
		UTMZone zone = new UTMZone(pos);
		// nu is the distance to the polar axis
		double nu = a / Math.sqrt(1 - e2 * Math.pow(sin_lat, 2));
		double p = lon - Math.toRadians(zone.meridian());
		double T2 = Math.pow(tan_lat, 2);
		double T4 = Math.pow(tan_lat, 4);
		double C = ep2 * Math.pow(cos_lat, 2);
		double A = p * cos_lat;
		double M = gd.getMeridionalArc(lat);
		double easting = K0 * nu * (A
			+ (1 - T2 + C)
			* Math.pow(A, 3) / 6
			+ (5 - 18 * T2 + T4 + 72 * C - 58 * ep2)
			* Math.pow(A, 5) / 120
		) + FALSE_EASTING;
		double northing = K0 * (M + nu * tan_lat * (
			Math.pow(A, 2) / 2
			+ (5 - T2 + 9 * C + 4 * Math.pow(C, 2))
			* Math.pow(A, 4) / 24
			+ (61 - 58 * T2 + T4 + 600 * C - 330 * ep2)
			* Math.pow(A, 6) / 720
		));
		// In Southern hemisphere, start from the South pole
		if (pos.getLatitude() < 0)
			northing += FALSE_NORTHING;
		return new UTMPosition(zone, easting, northing);
	}

	/** UTM zone */
	private final UTMZone zone;

	/** Get the UTM zone */
	public UTMZone getZone() {
		return zone;
	}

	/** Easting (meters) */
	private final double easting;

	/** Get the easting (meters) */
	public double getEasting() {
		return easting;
	}

	/** Northing (meters) */
	private final double northing;

	/** Get the northing (meters) */
	public double getNorthing() {
		return northing;
	}

	/** Create a new UTM position */
	public UTMPosition(UTMZone z, double e, double n) {
		zone = z;
		easting = e;
		northing = n;
	}

	/** Get a string representation of the UTM position */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(zone.toString());
		b.append(',');
		b.append(easting);
		b.append(',');
		b.append(northing);
		return b.toString();
	}

	/** Get the (lat/lon) position */
	public Position getPosition(GeodeticDatum gd) {
		double a = gd.getEquatorialRadius();
		double e2 = gd.getEccentricitySquared();
		double ep2 = e2 / (1 - e2);
		double e1 = (1 - Math.sqrt(1 - e2)) / (1 + Math.sqrt(1 - e2));
		double x = easting - FALSE_EASTING;
		double y = northing;
		if (!zone.isNorthernHemisphere())
			y -= FALSE_NORTHING;
		double M = y / K0;
		// FIXME: some of this could be done by the Geodetic Datum
		double mu = M / (a * (1
		       - e2 / 4
		       - 3 * Math.pow(e2, 2) / 64
		       - 5 * Math.pow(e2, 3) / 256)
		);
		double phi = (mu
		       + (3 * e1 / 2 - 27 * Math.pow(e1, 3) / 32)
		       * Math.sin(2 * mu)
		       + (21 * Math.pow(e1, 2) / 16 - 55 * Math.pow(e1, 4) / 32)
		       * Math.sin(4 * mu)
		       + (151 * Math.pow(e1, 3) / 96)
		       * Math.sin(6 * mu)
		);
		double sin_phi2 = Math.pow(Math.sin(phi), 2);
		double cos_phi = Math.cos(phi);
		double tan_phi = Math.tan(phi);
		double N1 = a / Math.sqrt(1 - e2 * sin_phi2);
		double T2 = Math.pow(tan_phi, 2);
		double T4 = Math.pow(tan_phi, 4);
		double C1 = ep2 * Math.pow(cos_phi, 2);
		double C2 = Math.pow(C1, 2);
		double R1 = a * (1 - e2) / Math.pow((1 - e2 * sin_phi2), 1.5);
		double D = x / (N1 * K0);
		double lat = phi - (N1 * tan_phi / R1) * (
		       + Math.pow(D, 2) / 2
		       - (5 + 3 * T2 + 10 * C1 - 4 * C2 - 9 * ep2)
		       * Math.pow(D, 4) / 24
		       + (61+ 90 * T2 + 298 * C1 + 45 * T4 - 252 * ep2 - 3 * C2)
		       * Math.pow(D, 6) / 720
		);
		double lon = (D
		       - (1 + 2 * T2 + C1)
		       * Math.pow(D, 3) / 6
		       + (5 - 2 * C1 + 28 * T2 - 3 * C2 + 8 * ep2 + 24 * T4)
		       * Math.pow(D, 5) / 120) / cos_phi;
		double lat_deg = Math.toDegrees(lat);
		double lon_deg = Math.toDegrees(lon);
		lon_deg += zone.meridian();
		return new Position(lat_deg, lon_deg);
	}

	/** Calculate the distance between two locations (in meters) */
	public double distancePythagoran(UTMPosition op) {
		return Math.hypot(op.easting - easting, op.northing - northing);
	}
}
