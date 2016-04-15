/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.map;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import javax.swing.SwingWorker;
import us.mn.state.dot.tms.geo.ZoomLevel;

/**
 * A tile layer state for drawing a Google-style tile map.
 *
 * @author Douglas Lau
 */
public class TileLayerState extends LayerState {

	/** Cache of tiles */
	private final TileCache cache;

	/** Set of missing tiles */
	private final HashSet<String> no_tile = new HashSet<String>();

	/** Create a new tile layer state */
	public TileLayerState(TileLayer layer, MapBean mb, TileCache c) {
		super(layer, mb, new Theme("Tile", new TileSymbol(),
			new Style("Tile")));
		cache = c;
	}

	/** Call the specified callback for each map object in the layer */
	@Override
	public MapObject forEach(MapSearcher s) {
		MapModel model = map.getModel();
		ZoomLevel zoom = model.getZoomLevel();
		Dimension sz = map.getSize();
		Point2D center = model.getCenter();
		int hx = (int) sz.getWidth() / 2;
		int hy = (int) sz.getHeight() / 2;
		int px = (int) zoom.getPixelX(center.getX());
		int py = (int) zoom.getPixelY(center.getY());
		int x0 = zoomLimit(zoom, (px - hx) / 256);
		int x1 = zoomLimit(zoom, ((px + hx) / 256) + 1);
		int ox = (px - hx) % 256;
		int y0 = zoomLimit(zoom, (py - hy) / 256);
		int y1 = zoomLimit(zoom, ((py + hy) / 256) + 1);
		int oy = (py + hy) % 256 - 512;
		for (int x = x0; x <= x1; x++) {
			int xp = (x - x0) * 256 - ox;
			for (int y = y0; y <= y1; y++) {
				int yp = (y1 - y) * 256 + oy;
				String tile = getTileName(zoom, x, y);
				Image img = getTile(tile);
				if (img != null)
					s.next(new TileMapObject(img, xp, yp));
				else {
					if (!isTileMissing(tile))
						createTileWorker(tile);
				}
			}
		}
		return null;
	}

	/** Limit X or Y tile based on zoom level */
	private int zoomLimit(ZoomLevel zoom, int xory) {
		return Math.max(0, Math.min(zoom.n_tiles - 1, xory));
	}

	/** Get a tile name */
	private String getTileName(ZoomLevel zoom, int tx, int ty) {
		int gy = zoom.n_tiles - 1 - ty;
		return "" + zoom.ordinal() + '/' + tx + '/' + gy;
	}

	/** Is the given tile missing? */
	private boolean isTileMissing(String tile) {
		synchronized (no_tile) {
			return no_tile.contains(tile);
		}
	}

	/** Get a tile from the tile cache */
	private Image getTile(String tile) {
		try {
			return cache.getTile(tile);
		}
		catch (IOException e) {
			System.err.print("I/O Error ");
			System.err.print(e.getMessage());
			System.err.println(" reading tile: " + tile);
			return null;
		}
	}

	/** Create a worker to lookup one tile */
	private void createTileWorker(final String tile) {
		SwingWorker worker = new SwingWorker<String, Void>() {
			@Override
			public String doInBackground() {
				return lookupTile(tile);
			}
			@Override
			public void done() {
				try {
					String tile = get();
					if (tile != null) {
						fireLayerChanged(
							LayerChange.geometry);
					}
				}
				catch (Exception e) {
					// ignore
				}
			}
		};
		worker.execute();
	}

	/** Lookup one tile */
	private String lookupTile(String tile) {
		try {
			cache.lookupTile(tile);
			return tile;
		}
		catch (FileNotFoundException e) {
			synchronized (no_tile) {
				no_tile.add(tile);
			}
		}
		catch (IOException e) {
			System.err.print("I/O Error ");
			System.err.print(e.getMessage());
			System.err.println(" loading tile: " + tile);
		}
		return null;
	}
}
