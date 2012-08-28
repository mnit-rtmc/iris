/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.RoadClass;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A segment is the shape of a roadway segment on a map.
 *
 * @author Douglas Lau
 */
public class MapSegment implements MapObject {

	/** Identity transform */
	static protected final AffineTransform IDENTITY_TRANSFORM =
		new AffineTransform();

	/** Get the coordinate transform */
	public AffineTransform getTransform() {
		return IDENTITY_TRANSFORM;
	}

	/** Get the inverse coordinate transform */
	public AffineTransform getInverseTransform() {
		return IDENTITY_TRANSFORM;
	}

	/** Segment object */
	protected final Segment segment;

	/** Lane for segment (null for all lanes) */
	protected final Integer lane;

	/** Shape to render */
	protected final Shape shape;

	/** Get the shape to draw this object */
	public Shape getShape() {
		return shape;
	}

	/** Shape to draw outline */
	protected final Shape outline;

	/** Get the outline to draw this object */
	public Shape getOutlineShape() {
		return outline;
	}

	/** Create a new map segment */
	public MapSegment(Segment s, float scale) {
		segment = s;
		lane = null;
		float inner = calculateInner(scale);
		float outer = inner + calculateWidth(scale);
		shape = createShape(inner, outer, inner, outer);
		outline = createOutline(inner, outer, inner, outer);
	}

	/** Calculate the spacing between the centerline and segment */
	protected float calculateInner(float scale) {
		return scale * roadClassScale() / 14;
	}

	/** Calculate the ideal segment width */
	protected float calculateWidth(float scale) {
		return scale * roadClassScale();
	}

	/** Get the scale factor for the road class */
	protected float roadClassScale() {
		Road r = segment.getModel().r_node.getGeoLoc().getRoadway();
		RoadClass rc = RoadClass.fromOrdinal(r.getRClass());
		return roadClassScale(rc) * UI.scale;
	}

	/** Get the scale factor for the road class */
	static protected float roadClassScale(RoadClass rc) {
		switch(rc) {
		case ARTERIAL:
		case EXPRESSWAY:
			return 4;
		case FREEWAY:
			return 6;
		case CD_ROAD:
			return 5;
		default:
			return 3;
		}
	}

	/** Create a new map segment */
	public MapSegment(Segment s, int sh, float scale) {
		segment = s;
		lane = segment.getLane(sh);
		float inner = calculateInner(scale);
		float width = calculateLaneWidth(scale);
		R_NodeModel mdl = segment.getModel();
		float in_a = inner + width * mdl.getUpstreamOffset(sh);
		float out_a = inner + width * mdl.getUpstreamOffset(sh + 1);
		float in_b = inner + width * mdl.getDownstreamOffset(sh);
		float out_b = inner + width * mdl.getDownstreamOffset(sh + 1);
		shape = createShape(in_a, out_a, in_b, out_b);
		outline = createOutline(in_a, out_a, in_b, out_b);
	}

	/** Calculate the width of one lane */
	protected float calculateLaneWidth(float scale) {
		return calculateWidth(scale) / 2 +
		       5 * (20 - scale) / 20;
	}

	/** Create the shape to draw this object */
	protected Shape createShape(float inner_a, float outer_a, float inner_b,
		float outer_b)
	{
		Point2D.Float p = new Point2D.Float();
		Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO);
		MapGeoLoc loc_a = segment.loc_up;
		MapGeoLoc loc_b = segment.loc_dn;
		loc_a.setPoint(p, outer_a);
		path.moveTo(p.getX(), p.getY());
		loc_b.setPoint(p, outer_b);
		path.lineTo(p.getX(), p.getY());
		loc_b.setPoint(p, inner_b);
		path.lineTo(p.getX(), p.getY());
		loc_a.setPoint(p, inner_a);
		path.lineTo(p.getX(), p.getY());
		path.closePath();
		return path;
	}

	/** Create the outline to draw this object */
	protected Shape createOutline(float inner_a, float outer_a,
		float inner_b, float outer_b)
	{
		Point2D.Float p = new Point2D.Float();
		Path2D.Float path = new Path2D.Float(Path2D.WIND_NON_ZERO);
		MapGeoLoc loc_a = segment.loc_up;
		MapGeoLoc loc_b = segment.loc_dn;
		loc_a.setPoint(p, outer_a);
		path.moveTo(p.getX(), p.getY());
		loc_b.setPoint(p, outer_b);
		path.lineTo(p.getX(), p.getY());
		loc_b.setPoint(p, inner_b);
		path.moveTo(p.getX(), p.getY());
		loc_a.setPoint(p, inner_a);
		path.lineTo(p.getX(), p.getY());
		return path;
	}

	/** Get the map segment tool tip */
	public String getTip() {
		StringBuilder sb = new StringBuilder();
		String label = segment.getLabel(lane);
		if(label != null)
			sb.append(label);
		Integer flow = getFlow();
		if(flow != null) {
			sb.append("\n ");
			sb.append(I18N.get("units.flow"));
			sb.append(" = ");
			sb.append(flow);
		}
		Integer density = getDensity();
		if(density != null) {
			sb.append("\n ");
			sb.append(I18N.get("units.density"));
			sb.append(" = ");
			sb.append(density);
		}
		Integer speed = getSpeed();
		if(speed != null) {
			sb.append("\n ");
			sb.append(I18N.get("units.speed"));
			sb.append(" = ");
			sb.append(speed);
		}
		if(sb.length() > 0)
			return sb.toString();
		else
			return null;
	}

	/** Get the segment flow */
	public Integer getFlow() {
		return segment.getFlow(lane);
	}

	/** Get the segment density */
	public Integer getDensity() {
		return segment.getDensity(lane);
	}

	/** Get the segment speed */
	public Integer getSpeed() {
		return segment.getSpeed(lane);
	}
}
