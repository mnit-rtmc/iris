/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Graphic;
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
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Graphic>("Name") {
			public Object getValueAt(Graphic g) {
				return g.getName();
			}
		},
		new ProxyColumn<Graphic>("Number", 0, Integer.class) {
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
		},
		new ProxyColumn<Graphic>("Bpp", 0, Integer.class) {
			public Object getValueAt(Graphic g) {
				return g.getBpp();
			}
		},
		new ProxyColumn<Graphic>("Width", 0, Integer.class) {
			public Object getValueAt(Graphic g) {
				return g.getWidth();
			}
		},
		new ProxyColumn<Graphic>("Height", 0, Integer.class) {
			public Object getValueAt(Graphic g) {
				return g.getHeight();
			}
		},
		new ProxyColumn<Graphic>("Image") {
			public Object getValueAt(Graphic g) {
				return g;
			}
			protected TableCellRenderer createCellRenderer() {
				return new ImageCellRenderer();
			}
		}
	    };
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
			Graphic g = (Graphic)value;
			BitmapGraphic bg = new BitmapGraphic(g.getWidth(),
				g.getHeight());
			try {
				bg.setPixels(Base64.decode(g.getPixels()));
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			BufferedImage im = new BufferedImage(g.getWidth(),
				g.getHeight(), BufferedImage.TYPE_INT_RGB);
			final int rgb = 0xFFFFFF;
			for(int y = 0; y < g.getHeight(); y++) {
				for(int x = 0; x < g.getWidth(); x++) {
					if(bg.getPixel(x, y).isLit())
						im.setRGB(x, y, rgb);
				}
			}
			return im;
		} else
			return null;
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

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Graphic.SONAR_TYPE;
	}
}
