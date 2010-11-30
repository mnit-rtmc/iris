/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentSkipListMap;
import us.mn.state.dot.map.DynamicLayer;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.event.LayerChange;
import us.mn.state.dot.map.event.LayerChangedEvent;
import us.mn.state.dot.tdxml.SensorListener;
import us.mn.state.dot.tdxml.SensorSample;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.tdxml.XmlSensorClient;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * SegmentLayer is a class for drawing roadway segments.
 *
 * @author Douglas Lau
  */
public class SegmentLayer extends Layer implements DynamicLayer,
	Iterable<Segment>
{
	/** Mapping of corridor names to segment lists */
	protected final ConcurrentSkipListMap<String, List<Segment>> cor_segs =
		new ConcurrentSkipListMap<String, List<Segment>>();

	/** R_Node manager */
	protected final R_NodeManager manager;

	/** Client session */
	protected final Session session;

	/** Sample data set */
	protected final SampleDataSet samples = new SampleDataSet();

	/** Create a new segment layer */
	public SegmentLayer(R_NodeManager m, Session s) {
		super("Segments");
		manager = m;
		session = s;
	}

	/** Start reading sensor data */
	public void start(Properties props, Logger logger) throws IOException,
		TdxmlException
	{
		String loc = props.getProperty("tdxml.detector.url");
		XmlSensorClient sc = createSensorClient(loc, logger);
		if(sc != null)
			sc.start();
	}

	/** Create a sensor client */
	protected XmlSensorClient createSensorClient(String loc, Logger l)
		throws IOException, TdxmlException
	{
		if(loc == null)
			return null;
		XmlSensorClient sc = new XmlSensorClient(new URL(loc), l);
		sc.addSensorListener(new SensorListener() {
			public void update(boolean finish) {
				if(finish) {
					samples.swapSamples();
					notifyLayerChanged();
					return;
				}
			}
			public void update(SensorSample s) {
				samples.updateSample(s);
			}
		});
		return sc;
	}

	/** Notify listeners that the layer has changed status */
	protected void notifyLayerChanged() {
		LayerChangedEvent ev = new LayerChangedEvent(this,
			LayerChange.status);
		notifyLayerChangedListeners(ev);
	}

	/** Update a corridor on the segment layer */
	public void updateCorridor(CorridorBase corridor) {
		List<Segment> below = new LinkedList<Segment>();
		List<Segment> above = new LinkedList<Segment>();
		R_Node un = null;	// upstream node
		MapGeoLoc uloc = null;	// upstream node location
		MapGeoLoc ploc = null;	// previous node location
		R_NodeModel mdl = null;	// node model
		for(R_Node n: corridor) {
			MapGeoLoc loc = manager.findGeoLoc(n);
			if(un != null) {
				if(R_NodeHelper.isJoined(n) &&
				   !isTooDistant(ploc, loc))
				{
					mdl = new R_NodeModel(n, mdl);
					Segment seg = new Segment(mdl, un,
						ploc, loc, samples);
					if(!isTooDistant(uloc, loc))
					   seg.addDetection(getDetectors(un));
					if(n.getAbove())
						above.add(seg);
					else
						below.add(seg);
				} else
					mdl = null;
			} else
				mdl = null;
			ploc = loc;
			if(un == null || R_NodeHelper.isStationBreak(n)) {
				un = n;
				uloc = loc;
			}
		}
		cor_segs.put(corridor.getName(), below);
		// Prepend lowercase z, for sorting purposes
		cor_segs.put('z' + corridor.getName(), above);
	}

	/** Get a set of detectors for an r_node */
	protected Set<Detector> getDetectors(R_Node n) {
		return session.getDetectorManager().getDetectors(n);
	}

	/** Check if two locations are too distant */
	static protected boolean isTooDistant(MapGeoLoc l0, MapGeoLoc l1) {
		return GeoLocHelper.metersTo(l0.getGeoLoc(), l1.getGeoLoc()) >
		       SystemAttrEnum.MAP_SEGMENT_MAX_METERS.getInt();
	}

	/** Create a new layer state */
	public LayerState createState(MapBean mb) {
		return new SegmentLayerState(this, mb);
	}

	/** Create a segment iterator.  This uses the cor_segs mapping, which
	 * allows a corridor to be updated without regenerating all segments. */
	public Iterator<Segment> iterator() {
		final Iterator<List<Segment>> cors =
			cor_segs.values().iterator();
		return new Iterator<Segment>() {
			Iterator<Segment> segs;
			public boolean hasNext() {
				if(segs != null && segs.hasNext())
					return true;
				while(cors.hasNext()) {
					segs = cors.next().iterator();
					if(segs.hasNext())
						return true;
				}
				return false;
			}
			public Segment next() {
				if(segs != null)
					return segs.next();
				else
					throw new NoSuchElementException();
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
