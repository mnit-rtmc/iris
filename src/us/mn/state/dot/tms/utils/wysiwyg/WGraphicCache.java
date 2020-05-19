/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

package us.mn.state.dot.tms.utils.wysiwyg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.ColorScheme;

/** Cache of graphics for use by WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 */
public class WGraphicCache {
	
	/** HashMap (indexed by graphicNum) of Graphic objects */
	private Map<Integer,Graphic> graphicMap;
	
	/** HashMap (indexed by graphicNum) of WRaster objects.
	 * WRaster(s) are created, loaded with graphic data, and added
	 * to this array as needed by getWRaster(int graphicNum) */
	private Map<Integer,WRaster> wgraphicMap;
	
	/** WGraphicCache Constructor
	 * Initializes both hashmaps and preloads the graphic
	 * hashmap with references to all existing graphics.
	 */
	public WGraphicCache() {
		int graphicNum;
		Graphic graphic;
		Iterator<Graphic> itg = GraphicHelper.iterator();

		// construct graphic HashMap(s)
		graphicMap  = new HashMap<Integer,Graphic>();
		wgraphicMap = new HashMap<Integer,WRaster>();
		// preload all Graphic(s)
		itg = GraphicHelper.iterator();
		while (itg.hasNext()) {
			graphic = itg.next();
			graphicNum = graphic.getGNumber();
			graphicMap.put(graphicNum, graphic);
		}
	}

	/** Returns a WRaster for the given graphic number.
	 *  Returns null if there is no such graphic.
	 *  
	 * @param graphicNum
	 * @return Matching WRaster or null
	 */
	public WRaster getWRaster(int graphicNum) {
		try {
			WRaster wr = wgraphicMap.get(graphicNum);
			if (wr != null)
				return wr;
			Graphic g = graphicMap.get(graphicNum);
			if (g == null)
				return null;  // graphic not in DB
			int csi = g.getColorScheme();
			ColorScheme cs = ColorScheme.fromOrdinal(csi);
			wr = WRaster.create(cs, g.getWidth(), g.getHeight());
			try {
				wr.setEncodedPixels(g.getPixels());
				wgraphicMap.put(graphicNum, wr);
				return wr;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}

	/** Returns a hashmap of Graphics indexed by graphic number. */
	public Map<Integer,Graphic> getGraphics() {
		return graphicMap;
	}
}

