/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
import java.util.concurrent.ConcurrentSkipListMap;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerChange;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.utils.I18N;

/**
 * SegmentLayer is a class for drawing roadway segments.
 *
 * @author Douglas Lau
  */
public class SegmentLayer extends Layer implements Iterable<Segment> {

	/** Mapping of corridor names to segment lists */
	private final ConcurrentSkipListMap<String, List<Segment>> cor_segs =
		new ConcurrentSkipListMap<String, List<Segment>>();

	/** Client session */
	private final Session session;

	/** R_Node manager */
	private final R_NodeManager manager;

	/** Detector hash */
	private final DetectorHash det_hash;

	/** Sample data set */
	private final SampleDataSet samples = new SampleDataSet();

	/** Sensor reader */
	private SensorReader reader;

	/** Create a new segment layer */
	public SegmentLayer(Session s, R_NodeManager m) {
		super(I18N.get("detector.segments"));
		manager = m;
		session = s;
		det_hash = new DetectorHash(s);
	}

	/** Initialize the segment layer */
	public void initialize() {
		det_hash.initialize();
	}

	/** Start reading sensor data */
	public void start(Properties props) throws IOException, SAXException,
		ParserConfigurationException
	{
		String loc = props.getProperty("tdxml.detector.url");
		if(loc != null)
			reader = new SensorReader(new URL(loc), this);
	}

	/** Dispose of the segment layer */
	public void dispose() {
		SensorReader sr = reader;
		if(sr != null)
			sr.dispose();
		reader = null;
		det_hash.dispose();
	}

	/** Update one sensor sample */
	public void update(SensorSample s) {
		samples.updateSample(s);
	}

	/** Complete one sample update */
	public void completeSamples() {
		samples.swapSamples();
		updateStatus();
	}

	/** Clear all sample data */
	public void clearSamples() {
		samples.clearSamples();
		updateStatus();
	}

	/** Update the layer status */
	public void updateStatus() {
		runSwing(new Runnable() {
			public void run() {
				fireLayerChanged(LayerChange.status);
			}
		});
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
			// Node may not be on selected corridor...
			if(loc == null)
				continue;
			if(un != null) {
				if(R_NodeHelper.isJoined(n) &&
				   !isTooDistant(ploc, loc))
				{
					boolean td = isTooDistant(uloc, loc);
					mdl = new R_NodeModel(n, mdl);
					Segment seg = new Segment(mdl, un,
						ploc, loc, samples, td);
					if(!td)
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
	private Set<Detector> getDetectors(R_Node n) {
		return det_hash.getDetectors(n);
	}

	/** Check if two locations are too distant */
	static private boolean isTooDistant(MapGeoLoc l0, MapGeoLoc l1) {
		Distance m = GeoLocHelper.distanceTo(l0.getGeoLoc(),
			l1.getGeoLoc());
		return m == null ||
		       m.m() > SystemAttrEnum.MAP_SEGMENT_MAX_METERS.getInt();
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
