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

import java.io.IOException;

/**
 * A tile layer for drawing a Google-style tile map.
 *
 * @author Douglas Lau
 */
public class TileLayer extends Layer {

	/** URL where tiles are hosted */
	private final String url;

	/** Number of tiles to cache */
	private final int n_cached;

	/** Tile cache */
	private TileCache cache;

	/** Create a new tile layer */
	public TileLayer(String n, String url, int n_cached) {
		super(n);
		this.url = url;
		this.n_cached = n_cached;
	}

	/** Initialize the tile layer */
	public void initialize() throws IOException {
		ImageFetcher f = new ImageFetcher(url);
		cache = new TileCache(f, n_cached);
	}

	/** Create a new layer state */
	public LayerState createState(MapBean mb) {
		assert (cache != null);
		return new TileLayerState(this, mb, cache);
	}

	/** Check if the layer is searchable */
	@Override
	public boolean isSearchable() {
		return false;
	}
}
