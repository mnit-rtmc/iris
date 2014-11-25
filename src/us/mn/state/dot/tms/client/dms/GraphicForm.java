/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.PixmapGraphic;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
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
		if(im.getHeight() > MAX_GRAPHIC_HEIGHT ||
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
		if(bpp != 1 && bpp != 24) {
			throw new ChangeVetoException(I18N.get(
				"graphic.image.wrong.bpp"));
		}
		if(cm.hasAlpha()) {
			throw new ChangeVetoException(I18N.get(
				"graphic.image.no.transparency"));
		}
	}

	/** Get the bits-per-pixel of an image */
	static private int imageBpp(BufferedImage im) {
		ColorModel cm = im.getColorModel();
		return cm.getPixelSize();
	}

	/** Table model for graphics */
	private final GraphicModel model;

	/** Table to hold the Graphic list */
	private final ZTable table = new ZTable();

	/** Action to create a new graphic */
	private final IAction create_gr = new IAction("graphic.create") {
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			createGraphic();
		}
	};

	/** Action to delete the selected proxy */
	private final IAction del_gr = new IAction("graphic.delete") {
		protected void doActionPerformed(ActionEvent e) {
			ListSelectionModel s = table.getSelectionModel();
			int row = s.getMinSelectionIndex();
			if(row >= 0)
				model.deleteRow(row);
		}
	};

	/** User session */
	private final Session session;

	/** Type cache */
	private final TypeCache<Graphic> cache;

	/** Create a new graphic form */
	public GraphicForm(Session s) {
		super(I18N.get("graphics"));
		session = s;
		cache = s.getSonarState().getGraphics();
		model = new GraphicModel(s);
	}

	/** Initializze the widgets in the form */
	@Override
	protected void initialize() {
		super.initialize();
		model.initialize();
		add(createGraphicPanel());
		table.setVisibleRowCount(5);
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		model.dispose();
		super.dispose();
	}

	/** Create graphic panel */
	private JPanel createGraphicPanel() {
		ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectProxy();
			}
		});
		table.setRowHeight(UI.scaled(MAX_GRAPHIC_HEIGHT / 2));
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		create_gr.setEnabled(model.canAdd());
		del_gr.setEnabled(false);
		IPanel p = new IPanel();
		p.add(table, Stretch.FULL);
		p.add(new JButton(create_gr));
		p.add(new JButton(del_gr), Stretch.RIGHT);
		return p;
	}

	/** Change the selected proxy */
	private void selectProxy() {
		Graphic proxy = model.getProxy(table.getSelectedRow());
		del_gr.setEnabled(model.canRemove(proxy));
	}

	/** Create a new graphic */
	private void createGraphic() throws IOException, ChangeVetoException {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(FILTER);
		int r = jfc.showOpenDialog(null);
		if(r == JFileChooser.APPROVE_OPTION) {
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
		attrs.put("pixels", encodePixels(rg));
		cache.createObject(name, attrs);
	}

	/** Create a unique Graphic name */
	private String createUniqueName() throws ChangeVetoException {
		for(int uid = 1; uid <= 256; uid++) {
			String n = "G_" + uid;
			if(GraphicHelper.lookup(n) == null)
				return n;
		}
		throw new ChangeVetoException(I18N.get("graphic.too.many"));
	}

	/** Get the next available graphic number */
	private Integer getGNumber() throws ChangeVetoException {
		TreeSet<Integer> gnums = new TreeSet<Integer>();
		Iterator<Graphic> it = GraphicHelper.iterator();
		while(it.hasNext()) {
			Graphic g = it.next();
			Integer gn = g.getGNumber();
			if(gn != null)
				gnums.add(gn);
		}
		for(int i = 1; i < 256; i++) {
			if(!gnums.contains(i))
				return i;
		}
		throw new ChangeVetoException(I18N.get("graphic.too.many"));
	}

	/** Create a raster graphic from a buffered image */
	private RasterGraphic createRaster(BufferedImage im)
		throws ChangeVetoException
	{
		switch(imageBpp(im)) {
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
}
