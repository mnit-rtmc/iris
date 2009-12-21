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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for graphics
 *
 * @author Douglas Lau
 */
public class GraphicModel extends ProxyTableModel<Graphic> {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 6;

	/** Name column number */
	static protected final int COL_NAME = 0;

	/** Graphic number column number */
	static protected final int COL_NUMBER = 1;

	/** Bpp column number */
	static protected final int COL_BPP = 2;

	/** Width column number */
	static protected final int COL_WIDTH = 3;

	/** Height column number */
	static protected final int COL_HEIGHT = 4;

	/** Image column number */
	static protected final int COL_IMAGE = 5;

	/** Create a new table column */
	static protected TableColumn createColumn(int col, String name) {
		TableColumn c = new TableColumn(col);
		c.setHeaderValue(name);
		return c;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createColumn(COL_NAME, "Name"));
		m.addColumn(createColumn(COL_NUMBER, "Number"));
		m.addColumn(createColumn(COL_BPP, "Bpp"));
		m.addColumn(createColumn(COL_WIDTH, "Width"));
		m.addColumn(createColumn(COL_HEIGHT, "Height"));
		m.addColumn(createImageColumn());
		return m;
	}

	/** Create the image column */
	static protected TableColumn createImageColumn() {
		TableColumn c = createColumn(COL_IMAGE, "Image");
		c.setCellRenderer(new TableCellRenderer() {
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
		});
		return c;
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
					if(bg.getPixel(x, y) > 0)
						im.setRGB(x, y, rgb);
				}
			}
			return im;
		} else
			return null;
	}

	/** Create a new graphic table model */
	public GraphicModel(TypeCache<Graphic> c) {
		super(c);
		initialize();
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(Graphic proxy) {
		if(proxy.getGNumber() != null)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the column class */
	public Class getColumnClass(int column) {
		if(column == COL_NAME)
			return String.class;
		else
			return Integer.class;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		Graphic p = getProxy(row);
		if(p == null)
			return null;
		switch(column) {
		case COL_NAME:
			return p.getName();
		case COL_NUMBER:
			return p.getGNumber();
		case COL_BPP:
			return p.getBpp();
		case COL_WIDTH:
			return p.getWidth();
		case COL_HEIGHT:
			return p.getHeight();
		case COL_IMAGE:
			return p;
		default:
			return null;
		}
	}

	/** Check if the specified cell is editable */
	public boolean isCellEditable(int row, int column) {
		return column == COL_NUMBER;
	}

	/** Set the value at the specified cell */
	public void setValueAt(Object value, int row, int column) {
		Graphic p = getProxy(row);
		if(p != null && column == COL_NUMBER) {
			if(value instanceof Integer)
				p.setGNumber((Integer)value);
		}
	}
}
