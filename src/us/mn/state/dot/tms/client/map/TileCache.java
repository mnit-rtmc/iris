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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * A cache of image tiles.
 *
 * @author Douglas Lau
 */
public class TileCache {

	/** FIFO of tile names in cache */
	protected final LinkedList<String> tiles = new LinkedList<String>();

	/** Hash of tile names to image files */
	protected final HashMap<String, TempImageFile> tile_hash =
		new HashMap<String, TempImageFile>();

	/** Image fetcher */
	protected final ImageFetcher fetcher;

	/** Size of cache (number of tiles) */
	protected final int size;

	/** Get the size of cache */
	public int getSize() {
		return size;
	}

	/** Create a new tile cache */
	public TileCache(ImageFetcher f, int sz) {
		fetcher = f;
		size = sz;
	}

	/** Purge the oldest image from the cache */
	protected void purgeOldestImage() {
		removeFirst();
	}

	/** Remove a tile from the cache */
	protected TempImageFile removeFirst() {
		synchronized(tile_hash) {
			String name = tiles.removeFirst();
			return tile_hash.remove(name);
		}
	}

	/** Get the named tile from the cache */
	public BufferedImage getTile(String n) throws IOException {
		TempImageFile tif = getTempImageFile(n);
		if(tif != null)
			return tif.getImage();
		else
			return null;
	}

	/** Get the named temp image file from the cache */
	protected TempImageFile getTempImageFile(String n) {
		synchronized(tile_hash) {
			if(tile_hash.containsKey(n))
				return tile_hash.get(n);
			else
				return null;
		}
	}

	/** Lookup a tile and put it in the cache */
	public void lookupTile(String n) throws IOException {
		while(isFull())
			purgeOldestImage();
		TempImageFile tif = new TempImageFile(fetcher.fetchImage(n));
		// Update the FIFO and hash map last so that exceptions cannot
		// leave us in an inconsistent state
		synchronized(tile_hash) {
			tiles.addLast(n);
			tile_hash.put(n, tif);
		}
	}

	/** Check if the cache is full */
	protected boolean isFull() {
		synchronized(tile_hash) {
			return tile_hash.size() >= size;
		}
	}

	/** Destroy the tile cache */
	public void destroy() throws IOException {
		synchronized(tile_hash) {
			tiles.clear();
			tile_hash.clear();
		}
	}
}
