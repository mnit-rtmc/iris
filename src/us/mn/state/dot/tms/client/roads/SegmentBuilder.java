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

/**
 * SegmentBuilder is a class for building roadway segments.
 *
 * @author Douglas Lau
  */
public class SegmentBuilder implements Iterable<Segment> {

	/** Check if two locations are too distant */
	static private boolean isTooDistant(MapGeoLoc l0, MapGeoLoc l1) {
		Distance m = GeoLocHelper.distanceTo(l0.getGeoLoc(),
			l1.getGeoLoc());
		return m == null ||
		       m.m() > SystemAttrEnum.MAP_SEGMENT_MAX_METERS.getInt();
	}

	/** Mapping of corridor names to segment lists */
	private final ConcurrentSkipListMap<String, List<Segment>> cor_segs =
		new ConcurrentSkipListMap<String, List<Segment>>();

	/** R_Node manager */
	private final R_NodeManager manager;

	/** Detector hash */
	private final DetectorHash det_hash;

	/** Sample data set */
	private final SampleDataSet samples = new SampleDataSet();

	/** Sensor reader */
	private final SensorReader reader;

	/** Create a new segment builder */
	public SegmentBuilder(Session s, R_NodeManager m, Properties p)
		throws IOException, SAXException, ParserConfigurationException
	{
		manager = m;
		det_hash = new DetectorHash(s);
		reader = createReader(p);
	}

	/** Create a sensor reader */
	private SensorReader createReader(Properties props) throws IOException,
		SAXException, ParserConfigurationException
	{
		String loc = props.getProperty("tdxml.detector.url");
		return (loc != null)
		     ? new SensorReader(new URL(loc), this)
		     : null;
	}

	/** Initialize the segment builder */
	public void initialize() {
		det_hash.initialize();
	}

	/** Dispose of the segment builder */
	public void dispose() {
		if (reader != null)
			reader.dispose();
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
				manager.getLayer().updateStatus();
			}
		});
	}

	/** Update a corridor */
	public void updateCorridor(CorridorBase<R_Node> corridor) {
		List<Segment> below = new LinkedList<Segment>();
		List<Segment> above = new LinkedList<Segment>();
		R_Node un = null;	// upstream node
		MapGeoLoc uloc = null;	// upstream node location
		MapGeoLoc ploc = null;	// previous node location
		R_NodeModel mdl = null;	// node model
		for (R_Node n: corridor) {
			MapGeoLoc loc = findGeoLoc(n);
			// Node may not be on selected corridor...
			if (loc == null)
				continue;
			if (un != null) {
				if (R_NodeHelper.isJoined(n) &&
				    !isTooDistant(ploc, loc))
				{
					boolean td = isTooDistant(uloc, loc);
					mdl = new R_NodeModel(n, mdl);
					Segment seg = new Segment(mdl, un,
						ploc, loc, samples, td);
					if (!td)
					    seg.addDetection(getDetectors(un));
					if (n.getAbove())
						above.add(seg);
					else
						below.add(seg);
				} else
					mdl = null;
			} else
				mdl = null;
			ploc = loc;
			if (un == null || R_NodeHelper.isStationBreak(n)) {
				un = n;
				uloc = loc;
			}
		}
		cor_segs.put(corridor.getName(), below);
		// Prepend lowercase z, for sorting purposes
		cor_segs.put('z' + corridor.getName(), above);
	}

	/** Find the map geo loc */
	public MapGeoLoc findGeoLoc(R_Node n) {
		return manager.findGeoLoc(n);
	}

	/** Get a set of detectors for an r_node */
	private Set<Detector> getDetectors(R_Node n) {
		return det_hash.getDetectors(n);
	}

	/** Create a segment iterator.  This uses the cor_segs mapping, which
	 * allows a corridor to be updated without regenerating all segments. */
	public Iterator<Segment> iterator() {
		final Iterator<List<Segment>> cors =
			cor_segs.values().iterator();
		return new Iterator<Segment>() {
			Iterator<Segment> segs;
			public boolean hasNext() {
				if (segs != null && segs.hasNext())
					return true;
				while (cors.hasNext()) {
					segs = cors.next().iterator();
					if (segs.hasNext())
						return true;
				}
				return false;
			}
			public Segment next() {
				if (segs != null)
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
