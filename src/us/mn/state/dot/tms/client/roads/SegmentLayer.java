/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import us.mn.state.dot.map.DynamicLayer;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.map.event.LayerChange;
import us.mn.state.dot.map.event.LayerChangedEvent;
import us.mn.state.dot.tdxml.SensorListener;
import us.mn.state.dot.tdxml.SensorSample;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.tdxml.XmlSensorClient;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * SegmentLayer is a class for drawing roadway segments.
 *
 * @author Douglas Lau
  */
public class SegmentLayer extends Layer implements DynamicLayer {

	/** List of segments in the layer */
	protected final List<Segment> segments = new LinkedList<Segment>();

	/** Get the list of segments */
	public List<Segment> getSegments() {
		return segments;
	}

	/** R_Node manager */
	protected final R_NodeManager manager;

	/** Create a new segment layer */
	public SegmentLayer(R_NodeManager m, Session s) {
		super("Segments");
		manager = m;
		Properties p = s.getProperties();
		String loc = p.getProperty("tdxml.detector.url");
		if(loc != null) {
			try {
				createSensorClient(loc, s.getLogger());
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			catch(TdxmlException e) {
				e.printStackTrace();
			}
		}
	}

	/** Create a sensor cleint */
	protected void createSensorClient(String loc, Logger l)
		throws IOException, TdxmlException
	{
		XmlSensorClient sc = new XmlSensorClient(new URL(loc), l);
		sc.addTdxmlListener(new SensorListener() {
			public void update(boolean finish) {
				if(finish) {
					for(Segment seg: segments)
						seg.swapSamples();
					notifyLayerChanged();
					return;
				}
			}
			public void update(SensorSample s) {
				for(Segment seg: segments)
					seg.updateSample(s);
			}
		});
		sc.start();
	}

	/** Notify listeners that the layer has changed status */
	protected void notifyLayerChanged() {
		LayerChangedEvent ev = new LayerChangedEvent(this,
			LayerChange.status);
		notifyLayerChangedListeners(ev);
	}

	/** Add a corridor to the segment layer */
	public void addCorridor(CorridorBase c) {
		Segment seg = new Segment();
		R_Node pn = null;
		MapGeoLoc ploc = null;
		for(R_Node n: c.getNodes()) {
			MapGeoLoc loc = manager.findGeoLoc(n);
			if(seg.isTooDistant(loc)) {
				segments.add(seg);
				seg = new Segment(pn);
				seg.addNode(ploc);
			}
			if(seg.isJoined(n) && !seg.isTooDistant(loc))
				seg.addNode(loc);
			if(seg.isBreak(n)) {
				segments.add(seg);
				R_Node sn = seg.getR_Node();
				if(seg.isStationBreak(n) || sn == null)
					seg = new Segment(n);
				else
					seg = new Segment(sn);
				seg.addNode(loc);
			}
			pn = n;
			ploc = loc;
		}
		segments.add(seg);
	}

	/** Create a new layer state */
	public LayerState createState() {
		return new SegmentLayerState(this);
	}
}
