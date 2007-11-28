/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.io.IOException;
import java.sql.ResultSet;
import us.mn.state.dot.sonar.server.Namespace;

/**
 * A graphic is an image which can be displayed on a DMS
 *
 * @author Douglas Lau
 */
public class GraphicImpl extends BaseObjectImpl implements Graphic {

	/** Load all the graphics */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading DMS graphics...");
		store.query("SELECT name, bpp, height, width, pixels " +
			"FROM graphic;", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new GraphicImpl(
					row.getString(1),	// name
					row.getInt(2),		// bpp
					row.getInt(3),		// height
					row.getInt(4),		// width
					row.getString(5)	// pixels
				));
			}
		});
	}

	/** Create a new graphic */
	static public Graphic doCreate(String name) throws TMSException {
		GraphicImpl graphic = new GraphicImpl(name);
		store.create(graphic);
		return graphic;
	}

	/** Destroy a graphic */
	public void destroy() {
		// Handled by doDestroy() method
	}

	/** Destroy a graphic */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new graphic */
	protected GraphicImpl(String n) {
		super(n);
		bpp = 1;
		height = 0;
		width = 0;
		pixels = "";
	}

	/** Create a graphic from database lookup */
	protected GraphicImpl(String n, int b, int h, int w, String p) {
		this(n);
		bpp = b;
		height = h;
		width = w;
		pixels = p;
	}

	/** Bits per pixel */
	protected int bpp;

	/** Set the bits-per-pixel (1, 8, 24) */
	public void setBpp(int b) {
		bpp = b;
	}

	/** Set the bits-per-pixel (1, 8, 24) */
	public void doSetBpp(int b) throws TMSException {
		if(b == bpp)
			return;
		store.update(this, "bpp", b);
		setBpp(b);
	}

	/** Get the bits-per-pixel */
	public int getBpp() {
		return bpp;
	}

	/** Height (number of pixels) */
	protected int height;

	/** Set the height (pixels) */
	public void setHeight(int h) {
		height = h;
	}

	/** Set the height (pixels) */
	public void doSetHeight(int h) throws TMSException {
		if(h == height)
			return;
		store.update(this, "height", h);
		setHeight(h);
	}

	/** Get the height (pixels) */
	public int getHeight() {
		return height;
	}

	/** Width (number of pixels) */
	protected int width;

	/** Set the width (pixels) */
	public void setWidth(int w) {
		width = w;
	}

	/** Set the width (pixels) */
	public void doSetWidth(int w) throws TMSException {
		if(w == width)
			return;
		store.update(this, "width", w);
		setWidth(w);
	}

	/** Get the width (pixels) */
	public int getWidth() {
		return width;
	}

	/** Pixel data (base64 encoded) */
	protected String pixels;

	/** Set the pixel data (base64 encoded) */
	public void setPixels(String p) {
		pixels = p;
	}

	/** Set the pixel data (base64 encoded) */
	public void doSetPixels(String p) throws TMSException {
		if(p.equals(pixels))
			return;
		try {
			Base64.decode(p);
		}
		catch(IOException e) {
			throw new ChangeVetoException("Invalid Base64 data");
		}
		store.update(this, "pixels", p);
		setPixels(p);
	}

	/** Get the pixel data (base64 encoded) */
	public String getPixels() {
		return pixels;
	}

	/** Render the graphic onto a bitmap graphic */
	public void renderOn(BitmapGraphic g, int x, int y) throws IOException {
		byte[] bitmap = Base64.decode(pixels);
		BitmapGraphic c = new BitmapGraphic(width, height);
		c.setBitmap(bitmap);
		for(int yy = 0; yy < height; yy++) {
			for(int xx = 0; xx < width; xx++) {
				int p = c.getPixel(xx, yy);
				g.setPixel(x + xx, y + yy, p);
			}
		}
	}
}
