/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * SegmentBuilder is a class for building roadway segments.
 *
 * @author Douglas Lau
  */
public class SegmentBuilder implements Iterable<Segment> {

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
		List<Segment> below = new ArrayList<Segment>();
		List<Segment> above = new ArrayList<Segment>();
		R_Node sn = null;       // station node
		MapGeoLoc sl = null;    // station node location
		MapGeoLoc al = null;    // upstream node location
		R_NodeModel mdl = null; // node model
		for (R_Node bn: corridor) {
			MapGeoLoc bl = findGeoLoc(bn);
			Segment seg = new Segment(mdl, al, bn, bl, sn, sl,
				samples, det_hash);
			if (seg.isGood()) {
				mdl = seg.getModel();
				if (bn.getAbove())
					above.add(seg);
				else
					below.add(seg);
			} else
				mdl = null;
			if (null == sn || R_NodeHelper.isStationBreak(bn)) {
				sn = bn;
				sl = bl;
			}
			al = bl;
		}
		cor_segs.put(corridor.getName(), below);
		// Prepend lowercase z, for sorting purposes
		cor_segs.put('z' + corridor.getName(), above);
	}

	/** Find the map geo loc */
	public MapGeoLoc findGeoLoc(R_Node n) {
		return manager.findGeoLoc(n);
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
