/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.PrintWriter;
import us.mn.state.dot.tms.Point;
import us.mn.state.dot.tms.utils.Transform;

/**
 * GeoLocHelper has static methods for dealing with geo locations.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class GeoLocHelper extends BaseHelper {

	/** The system attribute for the UTM zone */
	static protected final int UTM_ZONE =
		SystemAttrEnum.MAP_UTM_ZONE.getInt();

	/** The system attribute for nothern hemisphere */
	static protected final boolean NORTHERN_HEMISPHERE = 
		SystemAttrEnum.MAP_NORTHERN_HEMISPHERE.getBoolean();

	/** Don't create any instances */
	private GeoLocHelper() {
		assert false;
	}

	/** Get a description of the location */
	static public String getDescription(GeoLoc l) {
		StringBuilder b = new StringBuilder();
		if(l != null) {
			Road f = l.getFreeway();
			if(f != null) {
				short fd = l.getFreeDir();
				String free = f.getName() + " " +
					TMSObject.DIRECTION[fd];
				b.append(free.trim());
			}
		}
		String c = getCrossDescription(l);
		if(c != null) {
			if(b.length() > 0)
				b.append(' ');
			b.append(c);
		}
		if(b.length() > 0)
			return b.toString();
		else
			return "Unknown location";
	}

	/** Get a description of the cross-street location */
	static public String getCrossDescription(GeoLoc l) {
		if(l != null) {
			Road x = l.getCrossStreet();
			if(x != null) {
				short xd = l.getCrossDir();
				short xm = l.getCrossMod();
				String cross = TMSObject.MODIFIER[xm] +
					" " + x.getName() + " " +
					TMSObject.DIRECTION[xd];
				return cross.trim();
			}
		}
		return null;
	}

	/** Get a description of a ramp meter location */
	static public String getMeterDescription(GeoLoc l) {
		StringBuilder b = new StringBuilder();
		String c = getCrossDescription(l);
		if(c != null)
			b.append(c);
		if(l != null) {
			Road f = l.getFreeway();
			if(f != null) {
				if(b.length() > 0)
					b.append(" to ");
				short fd = l.getFreeDir();
				String free = f.getName() + " " +
					TMSObject.DIRECTION[fd];
				b.append(free.trim());
			}
		}
		if(b.length() > 0)
			return b.toString();
		else
			return "Unknown location";
	}

	/** Filter for alternate directions on a North-South road.
	 * @param d Direction to be filtered
	 * @param ad Alternate road direction
	 * @return Filtered direction */
	static protected short filterNorthSouth(short d, short ad) {
		if(ad == Road.EAST) {
			if(d == Road.EAST)
				return Road.NORTH;
			if(d == Road.WEST)
				return Road.SOUTH;
		} else if(ad == Road.WEST) {
			if(d == Road.WEST)
				return Road.NORTH;
			if(d == Road.EAST)
				return Road.SOUTH;
		}
		return d;
	}

	/** Filter for alternate directions on an East-West road.
	 * @param d Direction to be filtered
	 * @param ad Alternate road direction
	 * @return Filtered direction */
	static protected short filterEastWest(short d, short ad) {
		if(ad == Road.NORTH) {
			if(d == Road.NORTH)
				return Road.EAST;
			if(d == Road.SOUTH)
				return Road.WEST;
		} else if(ad == Road.SOUTH) {
			if(d == Road.SOUTH)
				return Road.EAST;
			if(d == Road.NORTH)
				return Road.WEST;
		}
		return d;
	}

	/** Filter the freeway direction which matches the given direction.
	 * @param d Direction to be filtered
	 * @param rd Main road direction (NORTH_SOUTH / EAST_WEST)
	 * @param ad Alternate road direction
	 * @return Filtered direction */
	static protected short filterDirection(short d, short rd, short ad) {
		if(rd == Road.NORTH_SOUTH)
			return filterNorthSouth(d, ad);
		else if(rd == Road.EAST_WEST)
			return filterEastWest(d, ad);
		else
			return d;
	}

	/** Filter the direction for the given road */
	static public short filterDirection(short d, Road r) {
		if(r != null) {
			short rd = r.getDirection();
			short ad = r.getAltDir();
			return filterDirection(d, rd, ad);
		} else
			return d;
	}

	/** Get the freeway corridor ID */
	static public String getCorridorID(GeoLoc l) {
		return getCorridorID(l.getFreeway(), l.getFreeDir());
	}

	/** Get the freeway corridor ID */
	static public String getCorridorID(Road f, short d) {
		if(f == null)
			return "null";
		StringBuilder b = new StringBuilder();
		String ab = f.getAbbrev();
		if(ab != null)
			b.append(ab);
		else
			return "null";
		short fd = filterDirection(d, f);
		if(fd > 0 && fd < TMSObject.DIRECTION.length)
			b.append(TMSObject.DIRECTION[fd]);
		return b.toString();
	}

	/** Get the corridor for a road */
	static protected String getCorridorName(Road r, short d) {
		if(r == null)
			return null;
		StringBuilder b = new StringBuilder();
		b.append(r.getName());
		short fd = filterDirection(d, r);
		if(fd > 0 && fd < TMSObject.DIRECTION.length) {
			b.append(' ');
			b.append(TMSObject.DIRECTION[fd]);
		}
		return b.toString();
	}

	/** Get the freeway corridor */
	static public String getCorridorName(GeoLoc l) {
		if(l != null)
			return getCorridorName(l.getFreeway(), l.getFreeDir());
		else
			return null;
	}

	/** Get the linked freeway corridor */
	static public String getLinkedCorridor(GeoLoc l) {
		return getCorridorName(l.getCrossStreet(), l.getCrossDir());
	}

	/** Check if two locations are on the same corridor */
	static public boolean isSameCorridor(GeoLoc l0, GeoLoc l1) {
		Road f0 = l0.getFreeway();
		Road f1 = l1.getFreeway();
		if(f0 == null || f1 == null)
			return false;
		return (f0 == f1) &&
			(filterDirection(l0.getFreeDir(), f0) ==
			 filterDirection(l1.getFreeDir(), f1));
	}

	/** Get the true easting */
	static public Integer getTrueEasting(GeoLoc l) {
		if(l == null)
			return null;
		Integer x = l.getEasting();
		if(x != null)
			return x;
		else
			return l.getEastOffset();
	}

	/** Get the true northing */
	static public Integer getTrueNorthing(GeoLoc l) {
		if(l == null)
			return null;
		Integer y = l.getNorthing();
		if(y != null)
			return y;
		else
			return l.getNorthOffset();
	}

	/** Get the combined easting */
	static public Integer getCombinedEasting(GeoLoc l) {
		if(l.getEasting() != null) {
			int easting = l.getEasting();
			if(l.getEastOffset() != null)
				easting += l.getEastOffset();
			return easting;
		} else
			return l.getEastOffset();
	}

	/** Get the combined northing */
	static public Integer getCombinedNorthing(GeoLoc l) {
		if(l.getNorthing() != null) {
			int northing = l.getNorthing();
			if(l.getNorthOffset() != null)
				northing += l.getNorthOffset();
			return northing;
		} else
			return l.getNorthOffset();
	}

	/** Check if the UTM coordinates are null */
	static public boolean isNull(GeoLoc l) {
		return (getTrueEasting(l) == null) ||
			(getTrueNorthing(l) == null);
	}

	/** Check if the coordinates are GPS data */
	static public boolean hasGPS(GeoLoc l) {
		return l != null && l.getNorthing() != null &&
			l.getEasting() != null;
	}

	/** Calculate the distance between two locations (in meters) */
	static public double metersTo(GeoLoc l0, GeoLoc l1) {
		Integer e0 = getTrueEasting(l0);
		Integer e1 = getTrueEasting(l1);
		Integer n0 = getTrueNorthing(l0);
		Integer n1 = getTrueNorthing(l1);
		if(e0 == null || e1 == null || n0 == null || n1 == null)
			return Double.POSITIVE_INFINITY;
		return Math.hypot(e0 - e1, n0 - n1);
	}

	/** Test if another location matches */
	static public boolean matches(GeoLoc l0, GeoLoc l1) {
		Road f0 = l0.getFreeway();
		Road x0 = l0.getCrossStreet();
		Road f1 = l1.getFreeway();
		Road x1 = l1.getCrossStreet();
		if(f0 == null || x0 == null || f1 == null || x1 == null)
			return false;
		return (f0 == f1) && (x0 == x1) &&
			(filterDirection(l0.getFreeDir(), f0) ==
			 filterDirection(l1.getFreeDir(), f1)) &&
			(l0.getCrossDir() == l1.getCrossDir()) &&
			(l0.getCrossMod() == l1.getCrossMod());
	}

	/** Test if two roads start with the same name */
	static protected boolean matchRootName(String n0, String n1) {
		return n0.startsWith(n1) || n1.startsWith(n0);
	}

	/** Test if another location matches (including CD roads) */
	static public boolean matchesRoot(GeoLoc l0, GeoLoc l1) {
		Road f0 = l0.getFreeway();
		Road x0 = l0.getCrossStreet();
		Road f1 = l1.getFreeway();
		Road x1 = l1.getCrossStreet();
		if(f0 == null || x0 == null || f1 == null || x1 == null)
			return false;
		return matchRootName(f0.getName(), f1.getName()) &&
			(x0 == x1) &&
			(filterDirection(l0.getFreeDir(), f0) ==
			 filterDirection(l1.getFreeDir(), f1)) &&
			(l0.getCrossDir() == l1.getCrossDir()) &&
			(l0.getCrossMod() == l1.getCrossMod());
	}

	/** Test if two locations have freeway/cross-street swapped */
	static protected boolean isSwapped(GeoLoc l0, GeoLoc l1) {
		Road f0 = l0.getFreeway();
		Road x0 = l0.getCrossStreet();
		Road f1 = l1.getFreeway();
		Road x1 = l1.getCrossStreet();
		if(f0 == null || x0 == null || f1 == null || x1 == null)
			return false;
		return (l0.getCrossMod() == l1.getCrossMod()) &&
			(f0 == x1) && (f1 == x0);
	}

	/** Test if a ramp matches another ramp location */
	static public boolean rampMatches(GeoLoc l0, GeoLoc l1) {
		return (l0.getCrossDir() == l1.getFreeDir()) &&
			(l0.getFreeDir() == l1.getCrossDir()) &&
			isSwapped(l0, l1);
	}

	/** Test if an access node matches a ramp location */
	static public boolean accessMatches(GeoLoc l0, GeoLoc l1) {
		Road f0 = l0.getFreeway();
		Road x0 = l0.getCrossStreet();
		Road f1 = l1.getFreeway();
		Road x1 = l1.getCrossStreet();
		if(f0 == null || x0 == null || f1 == null || x1 == null)
			return false;
		return (l0.getCrossMod() == l1.getCrossMod()) &&
			matchRootName(f0.getName(), f1.getName()) &&
			matchRootName(x0.getName(), x1.getName());
	}

	/** return GeoLoc as Point in WGS84 */
	static public Point getWgs84Point(GeoLoc p) {
		if(p == null)
			return new Point();
		if(p.getEasting() == null || p.getNorthing() == null)
			return null;
		double easting_d = p.getEasting().doubleValue();
		double northing_d = p.getNorthing().doubleValue();
		return Transform.toLatLonPoint(easting_d, 
			northing_d, UTM_ZONE, NORTHERN_HEMISPHERE);
	}
}
