/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

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

	/** Frame title */
	static protected final String TITLE = "Graphics";

	/** Filename extension filter */
	static protected final FileNameExtensionFilter FILTER =
		new FileNameExtensionFilter("PNG, GIF and BMP Images",
		"png", "gif", "bmp");

	/** Maximum allowed graphic height */
	static protected final int MAX_GRAPHIC_HEIGHT = 144;

	/** Maximum allowed graphic width */
	static protected final int MAX_GRAPHIC_WIDTH = 144;

	/** Table model for graphics */
	protected final GraphicModel model;

	/** Table to hold the Graphic list */
	protected final ZTable table = new ZTable();

	/** Button to create a new graphic */
	protected final JButton createBtn = new JButton("Create");

	/** Button to delete the selected proxy */
	protected final JButton del_btn = new JButton("Delete");

	/** User session */
	protected final Session session;

	/** Type cache */
	protected final TypeCache<Graphic> cache;

	/** Create a new graphic form */
	public GraphicForm(Session s) {
		super(TITLE);
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
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectProxy();
			}
		};
		new ActionJob(this, createBtn) {
			public void perform() throws Exception {
				createGraphic();
			}
		};
		new ActionJob(this, del_btn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		FormPanel panel = new FormPanel(true);
		table.setRowHeight(64);
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		panel.addRow(table);
		panel.add(createBtn);
		panel.addRow(del_btn);
		del_btn.setEnabled(false);
		return panel;
	}

	/** Change the selected proxy */
	protected void selectProxy() {
		Graphic proxy = model.getProxy(table.getSelectedRow());
		del_btn.setEnabled(model.canRemove(proxy));
	}

	/** Create a new graphic */
	protected void createGraphic() throws IOException {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(FILTER);
		int r = jfc.showOpenDialog(null);
		if(r == JFileChooser.APPROVE_OPTION) {
			BufferedImage im = ImageIO.read(jfc.getSelectedFile());
			if(im.getHeight() <= MAX_GRAPHIC_HEIGHT &&
			   im.getWidth() <= MAX_GRAPHIC_WIDTH)
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
			attrs.put("bpp", 1);
			attrs.put("width", im.getWidth());
			attrs.put("height", im.getHeight());
			attrs.put("pixels", encodePixels(im));
			cache.createObject(name, attrs);
		}
	}

	/** Encode the pixel data for an image */
	protected String encodePixels(BufferedImage im) {
		BitmapGraphic bg = new BitmapGraphic(im.getWidth(),
			im.getHeight());
		for(int y = 0; y < im.getHeight(); y++) {
			for(int x = 0; x < im.getWidth(); x++) {
				if((im.getRGB(x, y) & 0xFFFFFF) > 0)
					bg.setPixel(x, y, 1);
			}
		}
		return Base64.encode(bg.getPixels());
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
