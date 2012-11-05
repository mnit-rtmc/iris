/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.PixmapGraphic;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for graphics
 *
 * @author Douglas Lau
 */
public class GraphicModel extends ProxyTableModel<Graphic> {

	/** Create the columns in the model */
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
				return canUpdate(g);
			}
			public void setValueAt(Graphic g, Object value) {
				if(value instanceof Integer)
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
				return new ImageCellRenderer();
			}
		});
		return cols;
	}

	/** Create the image column */
	protected class ImageCellRenderer implements TableCellRenderer {
		protected final ImageIcon icon = new ImageIcon();
		public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column)
		{
			BufferedImage im = createImage(value);
			if(im != null) {
				icon.setImage(im);
				return new JLabel(icon);
			} else
				return new JLabel();
		}
	}

	/** Create an image */
	static protected BufferedImage createImage(Object value) {
		if(value instanceof Graphic) {
			RasterGraphic rg = GraphicHelper.createRaster(
				(Graphic)value);
			if(rg instanceof BitmapGraphic)
				return createBitmapImage((BitmapGraphic)rg);
			if(rg instanceof PixmapGraphic)
				return createPixmapImage((PixmapGraphic)rg);
		}
		return null;
	}

	/** Create a bitmap image */
	static private BufferedImage createBitmapImage(BitmapGraphic bg) {
		BufferedImage im = new BufferedImage(bg.getWidth(),
			bg.getHeight(), BufferedImage.TYPE_INT_RGB);
		final int rgb = 0xFFFFFF;
		for(int y = 0; y < bg.getHeight(); y++) {
			for(int x = 0; x < bg.getWidth(); x++) {
				if(bg.getPixel(x, y).isLit())
					im.setRGB(x, y, rgb);
			}
		}
		return im;
	}

	/** Create a pixmap image */
	static private BufferedImage createPixmapImage(PixmapGraphic pg) {
		BufferedImage im = new BufferedImage(pg.getWidth(),
			pg.getHeight(), BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < pg.getHeight(); y++) {
			for(int x = 0; x < pg.getWidth(); x++)
				im.setRGB(x, y, pg.getPixel(x, y).rgb());
		}
		return im;
	}

	/** Create a new graphic table model */
	public GraphicModel(Session s) {
		super(s, s.getSonarState().getGraphics());
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(Graphic proxy) {
		if(proxy.getGNumber() != null)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return super.getRowCount() - 1;
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Graphic.SONAR_TYPE;
	}
}
