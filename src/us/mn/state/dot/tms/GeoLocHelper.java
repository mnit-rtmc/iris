/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2019  Minnesota Department of Transportation
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

import java.util.ArrayList;
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
		return (GeoLoc) namespace.lookupObject(GeoLoc.SONAR_TYPE,
			name);
	}

	/** Get a description of the location */
	static public String getLocation(GeoLoc l) {
		return getLocation(l, null);
	}

	/** Get a description of an on-ramp location */
	static public String getOnRampLocation(GeoLoc l) {
		return getLocation(l, "from");
	}

	/** Get a description of an off-ramp location */
	static public String getOffRampLocation(GeoLoc l) {
		return getLocation(l, "to");
	}

	/** Get a description of the location */
	static private String getLocation(GeoLoc l, String connect) {
		ArrayList<String> list = new ArrayList<String>();
		String rl = getRoadLocation(l);
		if (rl.length() > 0)
			list.add(rl);
		String xlm = getCrossLandmark(l);
		if (xlm.length() > 0) {
			if (rl.length() > 0) {
				if (connect != null)
					list.add(connect);
				else if (isCrossStreetAt(l))
					list.add("@");
			}
			list.add(xlm);
		}
		return (list.size() > 0)
		      ? String.join(" ", list)
		      : "Unknown location";
	}

	/** Get a description of the roadway location */
	static private String getRoadLocation(GeoLoc l) {
		if (l != null) {
			Road r = l.getRoadway();
			if (r != null) {
				short rd = l.getRoadDir();
				String road = r.getName() + " " +
					Direction.fromOrdinal(rd).abbrev;
				return road.trim();
			}
		}
		return "";
	}

	/** Check if the cross-street modifier is AT */
	static private boolean isCrossStreetAt(GeoLoc l) {
		return (l != null) &&
		       (l.getCrossMod() == LocModifier.AT.ordinal()) &&
		       (l.getCrossStreet() != null);
	}

	/** Get cross street / landmark label.
	 *
	 * @param l The location.
	 * @return Cross-street if specified, followed by landmark in
	 *         parentheses, if specified.  Cross-street modifier is only
	 *         prepended if not "@". */
	static public String getCrossLandmark(GeoLoc l) {
		ArrayList<String> list = new ArrayList<String>();
		String xloc = getCrossLocation(l);
		if (xloc.length() > 0)
			list.add(xloc);
		String lm = getLandmark(l);
		if (lm.length() > 0)
			list.add(lm);
		return String.join(" ", list);
	}

	/** Get a description of the cross-street location.
	 *
	 * @param l The location.
	 * @return Cross-street if specified.  Cross-street modifier is only
	 *         prepended if not "@". */
	static private String getCrossLocation(GeoLoc l) {
		if (l != null) {
			Road xs = l.getCrossStreet();
			if (xs != null) {
				ArrayList<String> list = new ArrayList<String>();
				String mod = getModifier(l);
				if (mod != null)
					list.add(mod);
				list.add(xs.getName());
				String dir = Direction.fromOrdinal(
					l.getCrossDir()).abbrev;
				if (dir.length() > 0)
					list.add(dir);
				return String.join(" ", list);
			}
		}
		return "";
	}

	/** Get the cross-street modifier (if not AT) */
	static private String getModifier(GeoLoc l) {
		short mod = (l != null) ? l.getCrossMod() : 0;
		return (mod > 0)
		      ? LocModifier.fromOrdinal(mod).description
		      : null;
	}

	/** Get the location landmark */
	static private String getLandmark(GeoLoc l) {
		if (l != null) {
			String lm = l.getLandmark();
			if (lm != null && lm.length() > 0)
				return '(' + lm + ')';
		}
		return "";
	}

	/** Get the corridor for a road */
	static public String getCorridorName(Road r, short d) {
		if (r != null) {
			Direction dir = Direction.fromOrdinal(d);
			String c = r.getName() + " " + dir.abbrev;
			return c.trim();
		} else
			return null;
	}

	/** Get the roadway corridor */
	static public String getCorridorName(GeoLoc l) {
		return (l != null)
		      ? getCorridorName(l.getRoadway(), l.getRoadDir())
		      : null;
	}

	/** Get the linked roadway corridor */
	static public String getLinkedCorridor(GeoLoc l) {
		return getCorridorName(l.getCrossStreet(), l.getCrossDir());
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
		return (p0 != null && p1 != null)
		      ? new Distance(p0.distanceHaversine(p1))
		      : null;
	}

	/** Calculate the distance between two locations */
	static public Distance distanceTo(GeoLoc l0, Position p1) {
		Position p0 = getWgs84Position(l0);
		return (p0 != null && p1 != null)
		      ? new Distance(p0.distanceHaversine(p1))
		      : null;
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
		       (l0.getRoadDir() == l1.getRoadDir()) &&
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
			(l0.getRoadDir() == l1.getRoadDir()) &&
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

	/** Return GeoLoc as a Position in WGS84 */
	static public Position getWgs84Position(GeoLoc p) {
		Double lat = getLat(p);
		Double lon = getLon(p);
		return (lat != null && lon != null)
		      ? new Position(lat, lon)
		      : null;
	}

	/** Create a spherical mercator position */
	static public SphericalMercatorPosition getPosition(GeoLoc p) {
		Position pos = getWgs84Position(p);
		return (pos != null)
		      ? SphericalMercatorPosition.convert(pos)
		      : null;
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

	/** Get the root label for a parking detector */
	static public String getParkingRoot(GeoLoc loc) {
		if (loc == null)
			return FUTURE;
		Road roadway = loc.getRoadway();
		if (roadway == null)
			return FUTURE;
		Direction rd = Direction.fromOrdinal(loc.getRoadDir());
		String lm = loc.getLandmark();
		StringBuilder b = new StringBuilder();
		b.append(roadway.getAbbrev());
		b.append("/");
		if (lm != null) {
			for (int i = 0; i < lm.length(); i++) {
				char c = lm.charAt(i);
				if (Character.isDigit(c))
					b.append(c);
			}
		}
		b.append(rd.det_dir);
		return b.toString();
	}

	/** Calculate the vector from one location to another.
	 * @param loc_a Starting location.
	 * @param loc_b Ending location.
	 * @return Vector from loc_a to loc_b, or null. */
	static public MapVector calculateVector(GeoLoc loc_a, GeoLoc loc_b) {
		MapVector va = createMapVector(loc_a);
		MapVector vb = createMapVector(loc_b);
		return (va != null && vb != null) ? va.subtract(vb) : null;
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

	/** North normal vector */
	static private final MapVector VEC_NORTH = new MapVector(1, 0);

	/** South normal vector */
	static private final MapVector VEC_SOUTH = new MapVector(-1, 0);

	/** East normal vector */
	static private final MapVector VEC_EAST = new MapVector(0, -1);

	/** West normal vector */
	static private final MapVector VEC_WEST = new MapVector(0, 1);

	/** Get normal vector for a given direction */
	static public MapVector normalVector(int dir) {
		switch (Direction.fromOrdinal((short) dir)) {
		case NORTH:
			return VEC_NORTH;
		case SOUTH:
			return VEC_SOUTH;
		case EAST:
			return VEC_EAST;
		case WEST:
			return VEC_WEST;
		default:
			return VEC_NORTH;
		}
	}
}
