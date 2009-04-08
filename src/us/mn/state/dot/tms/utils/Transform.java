/*
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
package us.mn.state.dot.tms.utils;

import java.lang.Math;

import us.mn.state.dot.tms.Point;

/**
 * Transformation between coordinate systems. All equations are from the 
 * following reference and assume the WGS-84 model.
 * Map Projections - A Working Manual U.S. Geological Survey Professional Paper 1395
 * John P. Snyder 1987
 *
 * @author Stephen Donecker
 * @company University of California, Davis
 * @created October 30, 2008
 */
public class Transform {

	/** WGS-84 equatorial radius (meters) */
	private	static final double a = 6378137;

	/** WGS-84 polar radius (meters) */
	private static final double b = 6356752.3142; 

	/** Scale on central meridian */
	private static final double ko = 0.9996;

	/** 
 	 * Convert UTM coordinates to latitude-longitude coordinates 
 	 * 
 	 * @param easting the easting UTM coordinate in meters
 	 * @param northing the northing UTM coordinate in meters
 	 * @param utmZone the UTM zone
 	 * @param nothernHemisphere true if point is located in the 
 	 * northern hemisphere, false otherwise.
 	 * @return an array containing the latitude and longitude in
 	 * degrees.
	 */
	public static Point toLatLonPoint(double easting, double northing, 
		int utmZone, boolean northernHemisphere) 
	{

		// correct for false easting
		double x = 500000 - easting;

		// correct for false northing
		double y;
		if (northernHemisphere) {
			y = northing;
		} else {
			y = 10000000 - northing;
		}
	
		// The distance along the meridian from the equator to latitude phi0 which is 0
		double M0 = 0; // (3-21)		

		double M = M0 + northing / ko; // (8-20)
		
		// calculate the eccentricity of the ellipsoid
		double e = Math.sqrt(1-Math.pow(b, 2) / Math.pow(a, 2)); 
		
		// calculate the footprint latitude
		double mu = M / (a * (1 - Math.pow(e, 2) / 4 - 3 * Math.pow(e, 4) / 64 - 5 * Math.pow(e, 6) / 256)); // (7-19)
		double e1 = (1 - Math.sqrt(1 - Math.pow(e, 2))) / (1 + Math.sqrt(1 - Math.pow(e, 2))); // (3-24)
		double P1 = 3 * e1 / 2 - 27 * Math.pow(e1, 3) / 32;
		double P2 = 21 * Math.pow(e1, 2) / 16 - 55 * Math.pow(e1, 4) / 32;
		double P3 = 151 * Math.pow(e1, 3) / 96;
		double P4 = 1097 * Math.pow(e1, 4) / 512;
		double phi1 = mu + P1 * Math.sin(2 * mu) + P2 * Math.sin(4 * mu) + P3 * Math.sin(6 * mu) + P4 * Math.sin(8 * mu); // (3-26)

		// precalculations for the latitude and longitude
		double ePrimeSquared = Math.pow(e, 2) / (1 - Math.pow(e, 2)); // (8-12)
		double C1 = ePrimeSquared * Math.pow(Math.cos(phi1), 2); // (8-21)
		double T1 = Math.pow(Math.tan(phi1), 2); // (8-22)
		double N1 = a / Math.sqrt(1 - Math.pow(e, 2) * Math.pow(Math.sin(phi1), 2)); // (8-23)
		double R1 = a * (1 - Math.pow(e, 2)) / Math.pow((1 - Math.pow(e, 2) * Math.pow(Math.sin(phi1), 2)), 3.0/2.0); // (8-24)
		double D = x / (N1 * ko); // (8-25)
	
		// calculate the latitude
		double P5 = N1 * Math.tan(phi1) / R1;
		double P6 = Math.pow(D, 2) / 2;
		double P7 = (5 + 3 * T1 + 10 * C1 - 4 * Math.pow(C1, 2) - 9 * ePrimeSquared) * Math.pow(D, 4) / 24;
		double P8 = (61 + 90 * T1 + 298 * C1 + 45 * Math.pow(T1, 2) - 3 * Math.pow(C1, 2) - 252 * ePrimeSquared) * Math.pow(D, 6) / 720;
		double phi = phi1 - P5 * (P6 + P7 + P8); // (8-17)
		
		// latitude correction
		if (!northernHemisphere) {
			phi = -phi;
		}

		// calculate the central meridian of zone
		double lambda0 = Math.toRadians(6 * utmZone - 183);

		// calculate the longitude
		double P9 = D;
		double P10 = (1 + 2 * T1 + C1) * Math.pow(D, 3) / 6;
		double P11 = (5 - 2 * C1 + 28 * T1 - 3 * Math.pow(C1, 2) + 8 * ePrimeSquared + 24 * Math.pow(T1, 2)) * Math.pow(D, 5) / 120;
		double lambda = lambda0 - (P9 - P10 + P11) / Math.cos(phi1); // (8-18)
		
		// convert from radians to degrees		
		double lat = Math.toDegrees(phi);
		double lon = Math.toDegrees(lambda);

		return new Point(lon, lat);
	}

	public static Point toUtmPoint(double latitude, double longitude, double utmZone) {
		return new Point(0,0);
	}
}
