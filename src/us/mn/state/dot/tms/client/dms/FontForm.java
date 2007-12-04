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
package us.mn.state.dot.tms.client.dms;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.ListSelectionJob;

/**
 * A form for displaying and editing DMS fonts
 *
 * @author Douglas Lau
 */
public class FontForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "DMS Fonts";

	/** Table model for fonts */
	protected FontModel f_model;

	/** Table to hold the font list */
	protected final JTable f_table = new JTable();

	/** Button to delete the selected font */
	protected final JButton del_font = new JButton("Delete Font");

	/** Font type cache */
	protected final TypeCache<Font> cache;

	/** Glyph type cache */
	protected final TypeCache<Glyph> glyphs;

	/** Graphic type cache */
	protected final TypeCache<Graphic> graphics;

	/** Selected font */
	protected Font font;

	/** Map of glyph data for currently selected font */
	protected final HashMap<String, GlyphData> gmap =
		new HashMap<String, GlyphData>();

	/** Glyph list */
	protected final JList glist = new JList();

	/** Glyph editor */
	protected GlyphEditor geditor;

	/** Admin privileges */
	protected final boolean admin = true;

	/** Create a new font form */
	public FontForm(TypeCache<Font> fc, TypeCache<Glyph> gc,
		TypeCache<Graphic> grc)
	{
		super(TITLE);
		cache = fc;
		glyphs = gc;
		graphics = grc;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		f_model = new FontModel(cache, admin);
		add(createFontPanel());
	}

	/** Close the form */
	protected void close() {
		super.close();
		f_model.dispose();
	}

	/** Create font panel */
	protected JPanel createFontPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridwidth = 2;
		bag.insets.left = HGAP;
		bag.insets.right = HGAP;
		bag.insets.top = VGAP;
		bag.insets.bottom = VGAP;
		final ListSelectionModel s = f_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() throws IOException {
				if(!event.getValueIsAdjusting())
					selectFont();
			}
		};
		f_table.setModel(f_model);
		f_table.setAutoCreateColumnsFromModel(false);
		f_table.setColumnModel(f_model.createColumnModel());
		// FIXME: why isn't there a JTable.setVisibleRowCount method???
		f_table.setPreferredScrollableViewportSize(new Dimension(500,
			100));
		JScrollPane pane = new JScrollPane(f_table);
		panel.add(pane, bag);
		if(admin) {
			del_font.setEnabled(false);
			bag.gridwidth = 1;
			panel.add(del_font, bag);
			new ActionJob(this, del_font) {
				public void perform() throws Exception {
					int row = s.getMinSelectionIndex();
					if(row >= 0)
						f_model.deleteRow(row);
				}
			};
		}
		JPanel gpanel = createGlyphPanel();
		bag.gridwidth = 1;
		bag.gridx = 0;
		bag.gridy = 1;
		bag.anchor = GridBagConstraints.WEST;
		panel.add(gpanel, bag);
		geditor = new GlyphEditor(admin);
		bag.gridwidth = 2;
		bag.gridx = 1;
		bag.anchor = GridBagConstraints.CENTER;
		panel.add(geditor, bag);
		return panel;
	}

	/** Create a glyph panel */
	protected JPanel createGlyphPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(
			"ASCII character set"));
		new ListSelectionJob(this, glist) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectGlyph();
			}
		};
		DefaultListModel model = new DefaultListModel();
		glist.setModel(model);
		glist.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		glist.setVisibleRowCount(12);
		glist.setFixedCellHeight(32);
		glist.setFixedCellWidth(48);
		for(int i = 32; i < 127; i++)
			model.addElement(String.valueOf((char)i));
		panel.add(glist);
		return panel;
	}

	/** Simple glyph structure */
	public class GlyphData {
		public final Glyph glyph;
		public final Graphic graphic;
		public final BitmapGraphic bmap;

		protected GlyphData(Glyph g) throws IOException {
			glyph = g;
			graphic = glyph.getGraphic();
			bmap = new BitmapGraphic(graphic.getWidth(),
				graphic.getHeight());
			bmap.setBitmap(Base64.decode(graphic.getPixels()));
		}
	}

	/** Lookup the glyphs in the selected font */
	protected void lookupGlyphs(Font font) throws IOException {
		gmap.clear();
		Map<String, Glyph> all_glyphs = glyphs.getAll();
		LinkedList<Glyph> glist = new LinkedList<Glyph>();
		synchronized(all_glyphs) {
			for(Glyph g: all_glyphs.values())
				if(g.getFont() == font)
					glist.add(g);
		}
		for(Glyph g: glist) {
			String c = String.valueOf((char)g.getCodePoint());
			gmap.put(c, new GlyphData(g));
		}
	}

	/** Change the selected font */
	protected void selectFont() throws IOException {
		ListSelectionModel s = f_table.getSelectionModel();
		font = f_model.getProxy(s.getMinSelectionIndex());
		del_font.setEnabled(font != null);
		lookupGlyphs(font);
		glist.setCellRenderer(new GlyphCellRenderer(gmap));
	}

	/** Change the selected glyph */
	protected void selectGlyph() {
		Object value = glist.getSelectedValue();
		GlyphData gdata = gmap.get(value.toString());
		geditor.setGlyph(gdata);
	}
}
