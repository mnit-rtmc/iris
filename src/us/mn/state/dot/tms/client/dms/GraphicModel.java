/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.PixmapGraphic;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.Invokable;
import us.mn.state.dot.tms.client.widget.SwingRunner;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Table model for graphics.
 *
 * @author Douglas Lau
 */
public class GraphicModel extends ProxyTableModel<Graphic> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Graphic> descriptor(Session s) {
		return new ProxyDescriptor<Graphic>(
			s.getSonarState().getGraphics(),
			false,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Filename extension filter */
	static private final FileNameExtensionFilter FILTER =
		new FileNameExtensionFilter(I18N.get("graphic.image.filter"),
		"png", "gif", "bmp");

	/** Maximum allowed graphic height */
	static private final int MAX_GRAPHIC_HEIGHT = 144;

	/** Maximum allowed graphic width */
	static private final int MAX_GRAPHIC_WIDTH = 200;

	/** Check if an image can be a valid graphic */
	static private void checkImageValid(BufferedImage im)
		throws ChangeVetoException
	{
		checkImageSizeValid(im);
		checkImageColorModelValid(im);
	}

	/** Check if an image size is valid for a graphic */
	static private void checkImageSizeValid(BufferedImage im)
		throws ChangeVetoException
	{
		if (im.getHeight() > MAX_GRAPHIC_HEIGHT ||
		    im.getWidth() > MAX_GRAPHIC_WIDTH)
		{
			throw new ChangeVetoException(I18N.get(
				"graphic.image.too.large"));
		}
	}

	/** Check if an image color model is valid for a graphic */
	static private void checkImageColorModelValid(BufferedImage im)
		throws ChangeVetoException
	{
		ColorModel cm = im.getColorModel();
		int bpp = cm.getPixelSize();
		if (bpp != 1 && bpp != 24) {
			throw new ChangeVetoException(I18N.get(
				"graphic.image.wrong.bpp"));
		}
		if (cm.hasAlpha()) {
			throw new ChangeVetoException(I18N.get(
				"graphic.image.no.transparency"));
		}
	}

	/** Get the bits-per-pixel of an image */
	static private int imageBpp(BufferedImage im) {
		ColorModel cm = im.getColorModel();
		return cm.getPixelSize();
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Graphic>> createColumns() {
		ArrayList<ProxyColumn<Graphic>> cols =
			new ArrayList<ProxyColumn<Graphic>>(6);
		cols.add(new ProxyColumn<Graphic>("device.name", 60) {
			public Object getValueAt(Graphic g) {
				return g.getName();
			}
		});
		cols.add(new ProxyColumn<Graphic>("graphic.number", 60,
			Integer.class)
		{
			public Object getValueAt(Graphic g) {
				return g.getGNumber();
			}
			public boolean isEditable(Graphic g) {
				return canWrite(g);
			}
			public void setValueAt(Graphic g, Object value) {
				if (value instanceof Integer)
					g.setGNumber((Integer)value);
			}
		});
		cols.add(new ProxyColumn<Graphic>("graphic.bpp", 30,
			Integer.class)
		{
			public Object getValueAt(Graphic g) {
				return g.getBpp();
			}
		});
		cols.add(new ProxyColumn<Graphic>("graphic.width", 44,
			Integer.class)
		{
			public Object getValueAt(Graphic g) {
				return g.getWidth();
			}
		});
		cols.add(new ProxyColumn<Graphic>("graphic.height", 44,
			Integer.class)
		{
			public Object getValueAt(Graphic g) {
				return g.getHeight();
			}
		});
		cols.add(new ProxyColumn<Graphic>("graphic.image", 200) {
			public Object getValueAt(Graphic g) {
				return g;
			}
			protected TableCellRenderer createCellRenderer() {
				return new GraphicCellRenderer();
			}
		});
		return cols;
	}

	/** Create a new graphic table model */
	public GraphicModel(Session s) {
		super(s, descriptor(s), 5, MAX_GRAPHIC_HEIGHT / 2);
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(Graphic proxy) {
		return proxy.getGNumber() != null;
	}

	/** Create an object with the given name */
	@Override
	public void createObject(String name) {
		SwingRunner.runSwing(new Invokable() {
			public void invoke() throws Exception {
				createGraphic();
			}
		});
	}

	/** Create a new graphic */
	private void createGraphic() throws IOException, ChangeVetoException {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(FILTER);
		int r = jfc.showOpenDialog(null);
		if (r == JFileChooser.APPROVE_OPTION) {
			BufferedImage im = ImageIO.read(jfc.getSelectedFile());
			checkImageValid(im);
			createGraphic(im);
		}
	}

	/** Create a new graphic */
	private void createGraphic(BufferedImage im) throws ChangeVetoException{
		String name = createUniqueName();
		Integer g_number = getGNumber();
		RasterGraphic rg = createRaster(im);
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("g_number", g_number);
		attrs.put("bpp", imageBpp(im));
		attrs.put("width", im.getWidth());
		attrs.put("height", im.getHeight());
		attrs.put("pixels", rg.getEncodedPixels());
		descriptor.cache.createObject(name, attrs);
	}

	/** Create a unique Graphic name */
	private String createUniqueName() throws ChangeVetoException {
		for (int uid = 1; uid <= 256; uid++) {
			String n = "G_" + uid;
			if (GraphicHelper.lookup(n) == null)
				return n;
		}
		throw new ChangeVetoException(I18N.get("graphic.too.many"));
	}

	/** Get the next available graphic number */
	private Integer getGNumber() throws ChangeVetoException {
		TreeSet<Integer> gnums = new TreeSet<Integer>();
		Iterator<Graphic> it = GraphicHelper.iterator();
		while (it.hasNext()) {
			Graphic g = it.next();
			Integer gn = g.getGNumber();
			if (gn != null)
				gnums.add(gn);
		}
		for (int i = 1; i < 256; i++) {
			if (!gnums.contains(i))
				return i;
		}
		throw new ChangeVetoException(I18N.get("graphic.too.many"));
	}

	/** Create a raster graphic from a buffered image */
	private RasterGraphic createRaster(BufferedImage im)
		throws ChangeVetoException
	{
		switch (imageBpp(im)) {
		case 1:
			return createBitmap(im);
		case 24:
			return createPixmap(im);
		default:
			throw new ChangeVetoException(I18N.get(
				"graphic.image.wrong.bpp"));
		}
	}

	/** Create a bitmap graphic from a buffered image */
	private RasterGraphic createBitmap(BufferedImage im) {
		BitmapGraphic bg = new BitmapGraphic(im.getWidth(),
			im.getHeight());
		for (int y = 0; y < im.getHeight(); y++) {
			for (int x = 0; x < im.getWidth(); x++) {
				if ((im.getRGB(x, y) & 0xFFFFFF) > 0)
					bg.setPixel(x, y, DmsColor.AMBER);
			}
		}
		return bg;
	}

	/** Create a pixmap graphic from a buffered image */
	private RasterGraphic createPixmap(BufferedImage im) {
		PixmapGraphic pg = new PixmapGraphic(im.getWidth(),
			im.getHeight());
		for (int y = 0; y < im.getHeight(); y++) {
			for (int x = 0; x < im.getWidth(); x++) {
				DmsColor c = new DmsColor(im.getRGB(x, y));
				pg.setPixel(x, y, c);
			}
		}
		return pg;
	}
}
