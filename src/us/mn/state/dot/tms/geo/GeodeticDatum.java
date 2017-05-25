/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
 * A geodetic datum is an ellipsoid which is an approximation of the shape of
 * the Earth.
 *
 * @author Douglas Lau
 */
public class GeodeticDatum {

	/** Radius at the equator (in meters) */
	private final double equatorial_radius;

	/** Get the equatorial radius (meters) */
	public double getEquatorialRadius() {
		return equatorial_radius;
	}

	/** Radius at the poles (in meters) */
	private final double polar_radius;

	/** Get the polar radius */
	public double getPolarRadius() {
		return polar_radius;
	}

	/** Get the mean radius */
	public double getMeanRadius() {
		return (2 * equatorial_radius + polar_radius) / 3;
	}

	/** Square of elliptic eccentricity */
	private final double e2;

	/** Get the square of elliptic eccentricity */
	public double getEccentricitySquared() {
		return e2;
	}

	/** Create a new geodetic datum */
	private GeodeticDatum(double er, double pr) {
		equatorial_radius = er;
		polar_radius = pr;
		e2 = 1 - Math.pow(pr, 2) / Math.pow(er, 2);
		double e4 = Math.pow(e2, 2);
		double e6 = Math.pow(e2, 3);
		term1 = calculateTerm1(e4, e6);
		term2 = calculateTerm2(e4, e6);
		term3 = calculateTerm3(e4, e6);
		term4 = calculateTerm4(e6);
	}

	/** Term 1 for calculating the meridional arc */
	private final double term1;

	/** Term 2 for calculating the meridional arc */
	private final double term2;

	/** Term 3 for calculating the meridional arc */
	private final double term3;

	/** Term 4 for calculating the meridional arc */
	private final double term4;

	/** Calculate the first term for meridional arc */
	private double calculateTerm1(double e4, double e6) {
		return 1 - e2 / 4 - 3 * e4 / 64 - 5 * e6 / 256;
	}

	/** Calculate the second term for meridonal arc */
	private double calculateTerm2(double e4, double e6) {
		return 3 * e2 / 8 + 3 * e4 / 32 + 45 * e6 / 1024;
	}

	/** Calculate the third term for meridonal arc */
	private double calculateTerm3(double e4, double e6) {
		return 15 * e4 / 256 + 45 * e6 / 1024;
	}

	/** Calculate the fourth term for meridonal arc */
	private double calculateTerm4(double e6) {
		return 35 * e6 / 3072;
	}

	/** Calculate the meridional arc for the given latitude */
	public double getMeridionalArc(double lat) {
		return equatorial_radius * (
		       + term1 * lat
		       - term2 * Math.sin(2 * lat)
		       + term3 * Math.sin(4 * lat)
		       - term4 * Math.sin(6 * lat)
		);
	}

	/** World Geodetic System of 1984 (used by GPS) */
	static public final GeodeticDatum WGS_84 =
		new GeodeticDatum(6378137, 6356752.314245);

	/** North Americaon Datum of 1983 */
	static public final GeodeticDatum NAD_83 = WGS_84;

	static public final GeodeticDatum GRS_80 =
		new GeodeticDatum(6378137, 6356752.314140);
	static public final GeodeticDatum WGS_72 =
		new GeodeticDatum(6378135, 6356750.5);
	static public final GeodeticDatum Australian_1965 =
		new GeodeticDatum(6378160, 6356774.7);
	static public final GeodeticDatum Krasovsky_1940 =
		new GeodeticDatum(6378245, 6356863);
	static public final GeodeticDatum International_1924 =
		new GeodeticDatum(6378388,6356911.9);
	static public final GeodeticDatum Clarke_1880 =
		new GeodeticDatum(6378249.1, 6356514.9);
	static public final GeodeticDatum Clarke_1866 =
		new GeodeticDatum(6378206.4, 6356583.8);
	static public final GeodeticDatum Airy_1830 =
		new GeodeticDatum(6377563.4, 6356256.9);
	static public final GeodeticDatum Bessel_1841 =
		new GeodeticDatum(6377397.2, 6356079);
	static public final GeodeticDatum Everest_1830 =
		new GeodeticDatum(6377276.3, 6356075.4);

	/** Spherical Mercator Datum */
	static public final GeodeticDatum SPHERICAL =
		new GeodeticDatum(6378137, 6378137);
}
