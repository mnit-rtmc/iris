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
package us.mn.state.dot.tms.client.lcs;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.PixmapGraphic;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying a table of graphics.
 *
 * @author Douglas Lau
 */
public class GraphicForm extends AbstractForm {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(Graphic.SONAR_TYPE);
	}

	/** Filename extension filter */
	static protected final FileNameExtensionFilter FILTER =
		new FileNameExtensionFilter("PNG, GIF and BMP Images",
		"png", "gif", "bmp");

	/** Maximum allowed graphic height */
	static private final int MAX_GRAPHIC_HEIGHT = 144;

	/** Maximum allowed graphic width */
	static private final int MAX_GRAPHIC_WIDTH = 200;

	/** Check if an image can be a valid graphic */
	static private boolean isImageValid(BufferedImage im) {
		return isImageSizeValid(im) && isImageColorModelValid(im);
	}

	/** Check if an image size is valid for a graphic */
	static private boolean isImageSizeValid(BufferedImage im) {
		return im.getHeight() <= MAX_GRAPHIC_HEIGHT &&
		       im.getWidth() <= MAX_GRAPHIC_WIDTH;
	}

	/** Check if an image color model is valid for a graphic */
	static private boolean isImageColorModelValid(BufferedImage im) {
		ColorModel cm = im.getColorModel();
		int bpp = cm.getPixelSize();
		return (bpp == 1 || bpp == 24) && !cm.hasAlpha();
	}

	/** Get the bits-per-pixel of an image */
	static private int imageBpp(BufferedImage im) {
		ColorModel cm = im.getColorModel();
		return cm.getPixelSize();
	}

	/** Table model for graphics */
	protected final GraphicModel model;

	/** Table to hold the Graphic list */
	protected final ZTable table = new ZTable();

	/** Action to create a new graphic */
	private final IAction create_gr = new IAction("graphic.create") {
		protected void do_perform() throws IOException {
			createGraphic();
		}
	};

	/** Action to delete the selected proxy */
	private final IAction del_gr = new IAction("graphic.delete") {
		protected void do_perform() {
			ListSelectionModel s = table.getSelectionModel();
			int row = s.getMinSelectionIndex();
			if(row >= 0)
				model.deleteRow(row);
		}
	};

	/** User session */
	protected final Session session;

	/** Type cache */
	protected final TypeCache<Graphic> cache;

	/** Create a new graphic form */
	public GraphicForm(Session s) {
		super(I18N.get("graphics"));
		session = s;
		cache = s.getSonarState().getGraphics();
		model = new GraphicModel(s);
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model.initialize();
		add(createGraphicPanel());
		table.setVisibleRowCount(6);
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create graphic panel */
	protected JPanel createGraphicPanel() {
		ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectProxy();
			}
		};
		FormPanel panel = new FormPanel(true);
		table.setRowHeight(UI.scaled(64));
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		panel.addRow(table);
		panel.add(new JButton(create_gr));
		panel.addRow(new JButton(del_gr));
		create_gr.setEnabled(model.canAdd());
		del_gr.setEnabled(false);
		return panel;
	}

	/** Change the selected proxy */
	protected void selectProxy() {
		Graphic proxy = model.getProxy(table.getSelectedRow());
		del_gr.setEnabled(model.canRemove(proxy));
	}

	/** Create a new graphic */
	protected void createGraphic() throws IOException {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(FILTER);
		int r = jfc.showOpenDialog(null);
		if(r == JFileChooser.APPROVE_OPTION) {
			BufferedImage im = ImageIO.read(jfc.getSelectedFile());
			if(isImageValid(im))
				createGraphic(im);
		}
	}

	/** Create a new graphic */
	protected void createGraphic(BufferedImage im) {
		String name = createUniqueName();
		Integer g_number = getGNumber();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("g_number", g_number);
			attrs.put("bpp", imageBpp(im));
			attrs.put("width", im.getWidth());
			attrs.put("height", im.getHeight());
			RasterGraphic rg = createRaster(im);
			if(rg != null) {
				attrs.put("pixels", encodePixels(rg));
				cache.createObject(name, attrs);
			}
		}
	}

	/** Create a raster graphic from a buffered image */
	private RasterGraphic createRaster(BufferedImage im) {
		switch(imageBpp(im)) {
		case 1:
			return createBitmap(im);
		case 24:
			return createPixmap(im);
		default:
			return null;
		}
	}

	/** Create a bitmap graphic from a buffered image */
	private RasterGraphic createBitmap(BufferedImage im) {
		BitmapGraphic bg = new BitmapGraphic(im.getWidth(),
			im.getHeight());
		for(int y = 0; y < im.getHeight(); y++) {
			for(int x = 0; x < im.getWidth(); x++) {
				if((im.getRGB(x, y) & 0xFFFFFF) > 0)
					bg.setPixel(x, y, DmsColor.AMBER);
			}
		}
		return bg;
	}

	/** Create a pixmap graphic from a buffered image */
	private RasterGraphic createPixmap(BufferedImage im) {
		PixmapGraphic pg = new PixmapGraphic(im.getWidth(),
			im.getHeight());
		for(int y = 0; y < im.getHeight(); y++) {
			for(int x = 0; x < im.getWidth(); x++) {
				DmsColor c = new DmsColor(im.getRGB(x, y));
				pg.setPixel(x, y, c);
			}
		}
		return pg;
	}

	/** Enocde the pixels of a raster to Base64 */
	private String encodePixels(RasterGraphic rg) {
		return Base64.encode(rg.getPixels());
	}

	/** Create a unique Graphic name */
	protected String createUniqueName() {
		for(int uid = 1; uid <= 256; uid++) {
			String n = "LUG_" + uid;
			if(GraphicHelper.lookup(n) == null)
				return n;
		}
		assert false;
		return null;
	}

	/** Get the next available graphic number */
	protected Integer getGNumber() {
		final TreeSet<Integer> gnums = new TreeSet<Integer>();
		GraphicHelper.find(new Checker<Graphic>() {
			public boolean check(Graphic g) {
				Integer gn = g.getGNumber();
				if(gn != null)
					gnums.add(gn);
				return false;
			}
		});
		for(int i = 1; i < 256; i++) {
			if(!gnums.contains(i))
				return i;
		}
		return null;
	}
}
