/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing DMS fonts
 *
 * @author Douglas Lau
 */
public class FontForm extends AbstractForm {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(Font.SONAR_TYPE) &&
		       s.canRead(Glyph.SONAR_TYPE) &&
		       s.canRead(Graphic.SONAR_TYPE);
	}

	/** Table model for fonts */
	private final FontModel f_model;

	/** Table to hold the font list */
	private final ZTable f_table = new ZTable();

	/** Action to delete the selected font */
	private final IAction del_font = new IAction("font.delete") {
		@Override protected void do_perform() {
			ListSelectionModel s = f_table.getSelectionModel();
			int row = s.getMinSelectionIndex();
			if(row >= 0)
				f_model.deleteRow(row);
		}
	};

	/** Glyph type cache */
	private final TypeCache<Glyph> glyphs;

	/** Graphic type cache */
	private final TypeCache<Graphic> graphics;

	/** Check if the specified Graphic is from the selected font */
	private boolean isFromSelectedFont(Graphic p) {
		Font f = font;
		if(f != null)
			return p.getName().startsWith(f.getName());
		else
			return false;
	}

	/** Proxy listener for Graphic proxies */
	private final ProxyListener<Graphic> gr_listener =
		new ProxyListener<Graphic>()
	{
		public void proxyAdded(Graphic p) { }
		public void enumerationComplete() { }
		public void proxyRemoved(Graphic p) { }
		public void proxyChanged(Graphic p, String a) {
			if(isFromSelectedFont(p)) {
				// The "pixels" attribute should be the
				// last one changed (after width)
				if(a.equals("pixels"))
					updateGraphic(p);
			}
		}
	};

	/** Check if the specified Glyph is from the selected font */
	private boolean isFromSelectedFont(Glyph p) {
		Font f = font;
		if(f != null)
			return p.getName().startsWith(f.getName());
		else
			return false;
	}

	/** Proxy listener for Glyph proxies */
	private final ProxyListener<Glyph> gl_listener =
		new ProxyListener<Glyph>()
	{
		public void proxyAdded(Glyph p) {
			if(isFromSelectedFont(p)) {
				addGlyph(p);
				repaint();
			}
		}
		public void enumerationComplete() { }
		public void proxyRemoved(Glyph p) {
			if(isFromSelectedFont(p)) {
				removeGlyph(p);
				repaint();
			}
		}
		public void proxyChanged(Glyph p, String a) { }
	};

	/** Selected font */
	private Font font;

	/** Map of glyph data for currently selected font */
	private final HashMap<Integer, GlyphInfo> gmap =
		new HashMap<Integer, GlyphInfo>();

	/** Glyph list */
	private final JList glist = new JList();

	/** Glyph cell renderer */
	private final GlyphCellRenderer renderer = new GlyphCellRenderer();

	/** Glyph panel */
	private final GlyphPanel glyph_pnl;

	/** Sign pixel panel */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(40, 400,
		true);

	/** Create a new font form */
	public FontForm(Session s) {
		super(I18N.get("font.title"));
		f_model = new FontModel(s);
		glyphs = s.getSonarState().getDmsCache().getGlyphs();
		graphics = s.getSonarState().getGraphics();
		glyph_pnl = new GlyphPanel(s);
		glist.setCellRenderer(renderer);
	}

	/** Initializze the widgets in the form */
	@Override protected void initialize() {
		f_model.initialize();
		add(createFontPanel());
		graphics.addProxyListener(gr_listener);
		glyphs.addProxyListener(gl_listener);
	}

	/** Dispose of the form */
	@Override protected void dispose() {
		glyph_pnl.dispose();
		f_model.dispose();
		graphics.removeProxyListener(gr_listener);
		glyphs.removeProxyListener(gl_listener);
	}

	/** Create font panel */
	private JPanel createFontPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(UI.border);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridwidth = 2;
		bag.insets = UI.insets();
		ListSelectionModel s = f_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectFont();
			}
		});
		f_table.setModel(f_model);
		f_table.setAutoCreateColumnsFromModel(false);
		f_table.setColumnModel(f_model.createColumnModel());
		f_table.setVisibleRowCount(6);
		JScrollPane pane = new JScrollPane(f_table);
		panel.add(pane, bag);
		del_font.setEnabled(false);
		bag.gridwidth = 1;
		panel.add(new JButton(del_font), bag);
		JPanel fviewer = createFontViewer();
		bag.gridwidth = 1;
		bag.gridx = 0;
		bag.gridy = 1;
		bag.anchor = GridBagConstraints.WEST;
		panel.add(fviewer, bag);
		bag.gridwidth = 2;
		bag.gridx = 1;
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.BOTH;
		panel.add(glyph_pnl, bag);
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 3;
		panel.add(pixel_pnl, bag);
		return panel;
	}

	/** Create a font viewer panel */
	private JPanel createFontViewer() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(
			I18N.get("font.ascii")));
		glist.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectGlyph();
			}
		});
		glist.setModel(createCodePointModel());
		glist.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		glist.setVisibleRowCount(12);
		glist.setFixedCellHeight(UI.scaled(24));
		glist.setFixedCellWidth(UI.scaled(36));
		panel.add(glist);
		return panel;
	}

	/** Create a list model containing font code points */
	private DefaultListModel createCodePointModel() {
		DefaultListModel model = new DefaultListModel();
		for(int i = 32; i < 127; i++)
			model.addElement(i);
		return model;
	}

	/** Lookup the glyphs in the selected font */
	private void lookupGlyphs(Font font) {
		Collection<Glyph> gs = FontHelper.lookupGlyphs(font);
		synchronized(gmap) {
			gmap.clear();
		}
		renderer.clearBitmaps();
		for(Glyph g: gs)
			addGlyph(g);
		glist.repaint();
		selectGlyph();
	}

	/** Check if the currently selected font is deletable */
	private boolean isFontDeletable() {
		if(font == null)
			return false;
		synchronized(gmap) {
			return gmap.isEmpty();
		}
	}

	/** Add a Glyph to the glyph map */
	private void addGlyph(Glyph g) {
		int c = g.getCodePoint();
		GlyphInfo gi = new GlyphInfo(c, g);
		renderer.setBitmap(c, gi.bmap);
		synchronized(gmap) {
			gmap.put(c, gi);
		}
		glyph_pnl.setGlyph(gi);
		del_font.setEnabled(isFontDeletable());
	}

	/** Remove a Glyph from the glyph map */
	private void removeGlyph(Glyph g) {
		int c = g.getCodePoint();
		renderer.setBitmap(c, null);
		synchronized(gmap) {
			gmap.remove(c);
		}
		glyph_pnl.setGlyph(glyphInfo(c));
		del_font.setEnabled(isFontDeletable());
	}

	/** Update a Graphic in the GlyphInfo map */
	private void updateGraphic(Graphic g) {
		GlyphInfo gi = findGlyph(g);
		if(gi != null) {
			addGlyph(gi.glyph);
			glist.repaint();
			pixel_pnl.repaint();
		}
	}

	/** Find glyph data for a graphic */
	private GlyphInfo findGlyph(Graphic g) {
		synchronized(gmap) {
			for(GlyphInfo gi: gmap.values()) {
				if(gi.graphic == g)
					return gi;
			}
		}
		return null;
	}

	/** Change the selected font */
	private void selectFont() {
		ListSelectionModel s = f_table.getSelectionModel();
		Font f = f_model.getProxy(s.getMinSelectionIndex());
		glyph_pnl.setFont(f);
		font = f;
		lookupGlyphs(f);
		del_font.setEnabled(isFontDeletable());
		int h = fontHeight(f);
		int cw = fontWidth(f);
		int w = cw > 0 ? (512 / cw) * cw : 512;
		pixel_pnl.setPhysicalDimensions(w, h, 4, 4, 1, 1);
		pixel_pnl.setLogicalDimensions(w, h, cw, 0);
		pixel_pnl.setGraphic(renderMessage(f));
		pixel_pnl.repaint();
	}

	/** Get the font height */
	private int fontHeight(Font f) {
		return f != null ? f.getHeight() : 0;
	}

	/** Get the font width */
	private int fontWidth(Font f) {
		return f != null ? f.getWidth() : 0;
	}

	/** Render a message to a raster graphic */
	private RasterGraphic renderMessage(Font f) {
		if(f != null) {
			MultiString ms = new MultiString(I18N.get(
				"font.glyph.sample"));
			RasterGraphic[] pages = renderPages(f, ms);
			if(pages != null && pages.length > 0)
				return pages[0];
		}
		return null;
	}

	/** Render the pages of a text message */
	private RasterGraphic[] renderPages(Font f, MultiString ms) {
		int h = fontHeight(f);
		int cw = fontWidth(f);
		int w = cw > 0 ? (512 / cw) * cw : 512;
		int df = f.getNumber();
		RasterBuilder b = new RasterBuilder(w, h, cw, 0, df);
		try {
			return b.createPixmaps(ms);
		}
		catch(InvalidMessageException e) {
			return null;
		}
	}

	/** Change the selected glyph */
	private void selectGlyph() {
		glyph_pnl.setGlyph(glyphInfo(selectedCodePoint()));
	}

	/** Get selected code point */
	private int selectedCodePoint() {
		Object value = glist.getSelectedValue();
		return value instanceof Integer ? (Integer)value : 0;
	}

	/** Get glyph information */
	private GlyphInfo glyphInfo(int c) {
		GlyphInfo gi = lookupGlyphInfo(c);
		return gi != null ? gi : new GlyphInfo(c, null);
	}

	/** Lookup cached glyph information */
	private GlyphInfo lookupGlyphInfo(int c) {
		synchronized(gmap) {
			return gmap.get(c);
		}
	}
}
