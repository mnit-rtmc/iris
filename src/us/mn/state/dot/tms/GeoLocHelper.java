/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.geo.MapLineSegment;
import us.mn.state.dot.tms.geo.MapVector;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;
import us.mn.state.dot.tms.units.Distance;

/**
 * GeoLocHelper has static methods for dealing with geo locations.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class GeoLocHelper extends BaseHelper {

	/** Future detector label */
	static public final String FUTURE = "FUTURE";

	/** Don't create any instances */
	private GeoLocHelper() {
		assert false;
	}

	/** Lookup a geo location */
	static public GeoLoc lookup(String name) {
		return (GeoLoc)namespace.lookupObject(GeoLoc.SONAR_TYPE,
			name);
	}

	/** Get a description of the location */
	static public String getDescription(GeoLoc l) {
		return getDescription(l, null);
	}

	/** Get a description of an on-ramp location */
	static public String getOnRampDescription(GeoLoc l) {
		return getDescription(l, "from");
	}

	/** Get a description of an off-ramp location */
	static public String getOffRampDescription(GeoLoc l) {
		return getDescription(l, "to");
	}

	/** Get a description of the location */
	static private String getDescription(GeoLoc l, String connect) {
		StringBuilder b = new StringBuilder();
		if (l != null) {
			Road r = l.getRoadway();
			if (r != null) {
				short rd = l.getRoadDir();
				String road = r.getName() + " " +
					Direction.fromOrdinal(rd).abbrev;
				b.append(road.trim());
			}
		}
		String c = getCrossDescription(l, connect);
		if (c != null) {
			if (b.length() > 0)
				b.append(' ');
			b.append(c);
		} else if (connect == null || connect.length() == 0) {
			String lm = getLandmark(l);
			if (lm != null) {
				if (b.length() > 0)
					b.append(' ');
				b.append(lm);
			}
		}
		return (b.length() > 0) ? b.toString() : "Unknown location";
	}

	/** Get a description of the cross-street location */
	static public String getCrossDescription(GeoLoc l) {
		return getCrossDescription(l, null);
	}

	/** Get a description of the cross-street location */
	static public String getCrossDescription(GeoLoc l, String connect) {
		if (l != null) {
			Road x = l.getCrossStreet();
			if (x != null) {
				StringBuilder cross = new StringBuilder();
				if (connect != null)
					cross.append(connect);
				else
					cross.append(getModifier(l));
				cross.append(' ');
				cross.append(x.getName());
				cross.append(' ');
				cross.append(Direction.fromOrdinal(
					l.getCrossDir()).abbrev);
				return cross.toString().trim();
			}
		}
		return null;
	}

	/** Get the cross-street modifier */
	static public String getModifier(GeoLoc l) {
		return (l != null)
		      ? LocModifier.fromOrdinal(l.getCrossMod()).description
		      : null;
	}

	/** Get the location landmark */
	static private String getLandmark(GeoLoc l) {
		if (l != null) {
			String lm = l.getLandmark();
			if (lm != null && lm.length() > 0)
				return '(' + lm + ')';
		}
		return null;
	}

	/** Filter for alternate directions on a North-South road.
	 * @param d Direction to be filtered.
	 * @param ad Alternate road direction.
	 * @return Filtered direction. */
	static private Direction filterNorthSouth(Direction d, Direction ad) {
		if (ad == Direction.EAST) {
			if (d == Direction.EAST)
				return Direction.NORTH;
			if (d == Direction.WEST)
				return Direction.SOUTH;
		} else if (ad == Direction.WEST) {
			if (d == Direction.WEST)
				return Direction.NORTH;
			if (d == Direction.EAST)
				return Direction.SOUTH;
		}
		return d;
	}

	/** Filter for alternate directions on an East-West road.
	 * @param d Direction to be filtered.
	 * @param ad Alternate road direction.
	 * @return Filtered direction. */
	static private Direction filterEastWest(Direction d, Direction ad) {
		if (ad == Direction.NORTH) {
			if (d == Direction.NORTH)
				return Direction.EAST;
			if (d == Direction.SOUTH)
				return Direction.WEST;
		} else if (ad == Direction.SOUTH) {
			if (d == Direction.SOUTH)
				return Direction.EAST;
			if (d == Direction.NORTH)
				return Direction.WEST;
		}
		return d;
	}

	/** Filter the roadway direction which matches the given direction.
	 * @param d Direction to be filtered.
	 * @param rd Main road direction (NORTH_SOUTH / EAST_WEST).
	 * @param ad Alternate road direction.
	 * @return Filtered direction. */
	static private Direction filterDirection(Direction d, Direction rd,
		Direction ad)
	{
		if (rd == Direction.NORTH_SOUTH)
			return filterNorthSouth(d, ad);
		else if (rd == Direction.EAST_WEST)
			return filterEastWest(d, ad);
		else
			return d;
	}

	/** Filter the direction for the given road.
	 * @param d Direction to be filtered.
	 * @param r Road in question.
	 * @return Filtered direction. */
	static private Direction filterDirection(Direction d, Road r) {
		if (r != null) {
			Direction rd = Direction.fromOrdinal(r.getDirection());
			Direction ad = Direction.fromOrdinal(r.getAltDir());
			return filterDirection(d, rd, ad);
		} else
			return d;
	}

	/** Filter the direction for the given road */
	static private Direction filterDirection(short d, Road r) {
		return filterDirection(Direction.fromOrdinal(d), r);
	}

	/** Get the roadway corridor ID */
	static public String getCorridorID(GeoLoc l) {
		return getCorridorID(l.getRoadway(), l.getRoadDir());
	}

	/** Get the roadway corridor ID */
	static public String getCorridorID(Road r, short d) {
		if (r == null)
			return "null";
		StringBuilder b = new StringBuilder();
		String ab = r.getAbbrev();
		if (ab != null)
			b.append(ab);
		else
			return "null";
		b.append(filterDirection(d, r).abbrev);
		return b.toString();
	}

	/** Get the corridor for a road */
	static public String getCorridorName(Road r, short d) {
		if (r == null)
			return null;
		String corridor = r.getName() + " " +
			filterDirection(d, r).abbrev;
		return corridor.trim();
	}

	/** Get the roadway corridor */
	static public String getCorridorName(GeoLoc l) {
		if (l != null)
			return getCorridorName(l.getRoadway(), l.getRoadDir());
		else
			return null;
	}

	/** Get the linked roadway corridor */
	static public String getLinkedCorridor(GeoLoc l) {
		return getCorridorName(l.getCrossStreet(), l.getCrossDir());
	}

	/** Check if two locations are on the same corridor */
	static public boolean isSameCorridor(GeoLoc l0, GeoLoc l1) {
		Road r0 = l0.getRoadway();
		Road r1 = l1.getRoadway();
		if (r0 == null || r1 == null)
			return false;
		return (r0 == r1) &&
		       (filterDirection(l0.getRoadDir(), r0) ==
		        filterDirection(l1.getRoadDir(), r1));
	}

	/** Get the latitude of a GeoLoc */
	static public Double getLat(GeoLoc l) {
		return (l != null) ? l.getLat() : null;
	}

	/** Get the longitude of a GeoLoc */
	static public Double getLon(GeoLoc l) {
		return (l != null) ? l.getLon() : null;
	}

	/** Check if the coordinates are null */
	static public boolean isNull(GeoLoc l) {
		return (getLat(l) == null) || (getLon(l) == null);
	}

	/** Calculate the distance between two locations */
	static public Distance distanceTo(GeoLoc l0, GeoLoc l1) {
		Position p0 = getWgs84Position(l0);
		Position p1 = getWgs84Position(l1);
		if (p0 != null && p1 != null)
			return new Distance(p0.distanceHaversine(p1));
		else
			return null;
	}

	/** Calculate the distance between two locations */
	static public Distance distanceTo(GeoLoc l0, Position p1) {
		Position p0 = getWgs84Position(l0);
		if (p0 != null && p1 != null)
			return new Distance(p0.distanceHaversine(p1));
		else
			return null;
	}

	/** Test if another location matches */
	static public boolean matches(GeoLoc l0, GeoLoc l1) {
		Road r0 = l0.getRoadway();
		Road x0 = l0.getCrossStreet();
		Road r1 = l1.getRoadway();
		Road x1 = l1.getCrossStreet();
		if (r0 == null || x0 == null || r1 == null || x1 == null)
			return false;
		return (r0 == r1) && (x0 == x1) &&
		       (filterDirection(l0.getRoadDir(), r0) ==
		        filterDirection(l1.getRoadDir(), r1)) &&
		       (l0.getCrossDir() == l1.getCrossDir()) &&
		       (l0.getCrossMod() == l1.getCrossMod());
	}

	/** Test if two roads start with the same name */
	static private boolean matchRootName(String n0, String n1) {
		return n0.startsWith(n1) || n1.startsWith(n0);
	}

	/** Test if another location matches (including CD roads) */
	static public boolean matchesRoot(GeoLoc l0, GeoLoc l1) {
		Road r0 = l0.getRoadway();
		Road x0 = l0.getCrossStreet();
		Road r1 = l1.getRoadway();
		Road x1 = l1.getCrossStreet();
		if (r0 == null || x0 == null || r1 == null || x1 == null)
			return false;
		return matchRootName(r0.getName(), r1.getName()) &&
			(x0 == x1) &&
			(filterDirection(l0.getRoadDir(), r0) ==
			 filterDirection(l1.getRoadDir(), r1)) &&
			(l0.getCrossDir() == l1.getCrossDir()) &&
			(l0.getCrossMod() == l1.getCrossMod());
	}

	/** Test if two locations have roadway/cross-street swapped */
	static private boolean isSwapped(GeoLoc l0, GeoLoc l1) {
		Road r0 = l0.getRoadway();
		Road x0 = l0.getCrossStreet();
		Road r1 = l1.getRoadway();
		Road x1 = l1.getCrossStreet();
		if (r0 == null || x0 == null || r1 == null || x1 == null)
			return false;
		return (l0.getCrossMod() == l1.getCrossMod()) &&
			(r0 == x1) && (r1 == x0);
	}

	/** Test if a ramp matches another ramp location */
	static public boolean rampMatches(GeoLoc l0, GeoLoc l1) {
		return (l0.getCrossDir() == l1.getRoadDir()) &&
			(l0.getRoadDir() == l1.getCrossDir()) &&
			isSwapped(l0, l1);
	}

	/** Test if an access node matches a ramp location */
	static public boolean accessMatches(GeoLoc l0, GeoLoc l1) {
		Road r0 = l0.getRoadway();
		Road x0 = l0.getCrossStreet();
		Road r1 = l1.getRoadway();
		Road x1 = l1.getCrossStreet();
		if (r0 == null || x0 == null || r1 == null || x1 == null)
			return false;
		return (l0.getCrossMod() == l1.getCrossMod()) &&
			matchRootName(r0.getName(), r1.getName()) &&
			matchRootName(x0.getName(), x1.getName());
	}

	/** Return GeoLoc as a Position in WGS84 */
	static public Position getWgs84Position(GeoLoc p) {
		Double lat = getLat(p);
		Double lon = getLon(p);
		if (lat != null && lon != null)
			return new Position(lat, lon);
		else
			return null;
	}

	/** Create a spherical mercator position */
	static public SphericalMercatorPosition getPosition(GeoLoc p) {
		Position pos = getWgs84Position(p);
		if (pos != null)
			return SphericalMercatorPosition.convert(pos);
		else
			return null;
	}

	/** Get the root label (for a detector or a station) */
	static public String getRootLabel(GeoLoc loc) {
		if (loc == null)
			return FUTURE;
		Road roadway = loc.getRoadway();
		Road cross = loc.getCrossStreet();
		if (roadway == null || cross == null)
			return FUTURE;
		Direction rd = Direction.fromOrdinal(loc.getRoadDir());
		Direction cd = Direction.fromOrdinal(loc.getCrossDir());
		LocModifier cm = LocModifier.fromOrdinal(loc.getCrossMod());
		StringBuilder b = new StringBuilder();
		b.append(roadway.getAbbrev());
		b.append("/");
		b.append(cd.abbrev);
		b.append(cm.abbrev);
		b.append(cross.getAbbrev());
		b.append(rd.det_dir);
		return b.toString();
	}

	/** Calculate the bearing from one location to another.
	 * @param loc_a Starting location.
	 * @param loc_b Ending location.
	 * @return Bearing from loc_a to loc_b (radians). */
	static public double calculateBearing(GeoLoc loc_a, GeoLoc loc_b) {
		MapVector va = createMapVector(loc_a);
		MapVector vb = createMapVector(loc_b);
		if (va != null && vb != null) {
			MapVector a = vb.subtract(va);
			return a.getAngle();
		} else
			return Double.NaN;
	}

	/** Get map vector to a location from the origin.  The units used are
	 * spherical mercator "meters".
	 * @param loc Location of vector.
	 * @return Map vector from origin to specified location, or null. */
	static private MapVector createMapVector(GeoLoc loc) {
		SphericalMercatorPosition pos = getPosition(loc);
		if (pos != null) {
			double x = pos.getX();
			double y = pos.getY();
			return new MapVector(x, y);
		} else
			return null;
	}

	/** Calculate the distance from a point to a line segment.
	 * @param l0 First end of line segment.
	 * @param l1 Second end of line segment.
	 * @param smp Selected point (spherical mercator position).
	 * @return Distance from point to segment (spherical mercator "meters").
	 */
	static public double segmentDistance(GeoLoc l0, GeoLoc l1,
		SphericalMercatorPosition smp)
	{
		SphericalMercatorPosition p0 = getPosition(l0);
		SphericalMercatorPosition p1 = getPosition(l1);
		if (p0 != null && p1 != null) {
			MapLineSegment seg = new MapLineSegment(p0.getX(),
				p0.getY(), p1.getX(), p1.getY());
			return seg.distanceTo(smp.getX(), smp.getY());
		} else
			return Double.POSITIVE_INFINITY;
	}

	/** Snap a point to a line segment on the map.
	 * @param l0 First end of line segment.
	 * @param l1 Second end of line segment.
	 * @param smp Selected point (spherical mercator position).
	 * @return GeoLoc snapped to line segment. */
	static public GeoLoc snapSegment(GeoLoc l0, GeoLoc l1,
		SphericalMercatorPosition smp)
	{
		SphericalMercatorPosition p0 = getPosition(l0);
		SphericalMercatorPosition p1 = getPosition(l1);
		if (p0 != null && p1 != null) {
			MapLineSegment seg = new MapLineSegment(p0.getX(),
				p0.getY(), p1.getX(), p1.getY());
			MapVector pnt = seg.snap(smp.getX(), smp.getY());
			return createTransient(pnt, l0);
		}
		return null;
	}

	/** Create a transient geo location */
	static private TransGeoLoc createTransient(MapVector pnt, GeoLoc l0) {
		SphericalMercatorPosition pos = new SphericalMercatorPosition(
			pnt.x, pnt.y);
		Position p = pos.getPosition();
		float lat = (float) p.getLatitude();
		float lon = (float) p.getLongitude();
		return new TransGeoLoc(l0.getRoadway(), l0.getRoadDir(), lat,
			lon);
	}
}
