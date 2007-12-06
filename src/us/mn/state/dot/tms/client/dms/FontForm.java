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
import us.mn.state.dot.sonar.client.ProxyListener;
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

	/** Deferred Glyph creation data */
	protected final HashMap<String, GlyphDeferred> deferred =
		new HashMap<String, GlyphDeferred>();

	/** Font type cache */
	protected final TypeCache<Font> cache;

	/** Glyph type cache */
	protected final TypeCache<Glyph> glyphs;

	/** Graphic type cache */
	protected final TypeCache<Graphic> graphics;

	/** Check if the specified Graphic is from the selected font */
	protected boolean isFromSelectedFont(Graphic p) {
		Font f = font;
		if(f != null)
			return p.getName().startsWith(f.getName());
		else
			return false;
	}

	/** Proxy listener for Graphic proxies */
	protected final ProxyListener<Graphic> gr_listener =
		new ProxyListener<Graphic>()
	{
		public void proxyAdded(Graphic p) { }
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
	protected boolean isFromSelectedFont(Glyph p) {
		Font f = font;
		if(f != null)
			return p.getName().startsWith(f.getName());
		else
			return false;
	}

	/** Lookup a graphic by name */
	protected Graphic lookupGraphic(String name) {
		Map<String, Graphic> all_graphics = graphics.getAll();
		synchronized(all_graphics) {
			return all_graphics.get(name);
		}
	}

	/** Get a deferred Glyph entry */
	protected GlyphDeferred getDeferredGlyph(Glyph g) {
		synchronized(deferred) {
			return deferred.remove(g.getName());
		}
	}

	/** Do deferred Glyph creation stuff */
	protected void doDeferredGlyph(Glyph g, GlyphDeferred gd) {
		g.setFont(gd.font);
		g.setCodePoint(gd.code_point);
		Graphic gr = lookupGraphic(g.getName());
		g.setGraphic(gr);
		gr.setBpp(1);
		gr.setHeight(gd.font.getHeight());
		gr.setWidth(1);
		int b = (gd.font.getHeight() + 7) / 8;
		gr.setPixels(Base64.encode(new byte[b]));
	}

	/** Proxy listener for Glyph proxies */
	protected final ProxyListener<Glyph> gl_listener =
		new ProxyListener<Glyph>()
	{
		public void proxyAdded(Glyph p) {
			GlyphDeferred gd = getDeferredGlyph(p);
			if(gd != null)
				doDeferredGlyph(p, gd);
			if(isFromSelectedFont(p)) {
				addGlyph(p);
				repaint();
			}
		}
		public void proxyRemoved(Glyph p) {
			if(isFromSelectedFont(p)) {
				removeGlyph(p);
				repaint();
			}
		}
		public void proxyChanged(Glyph p, String a) { }
	};

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
		graphics.addProxyListener(gr_listener);
		glyphs.addProxyListener(gl_listener);
	}

	/** Dispose of the form */
	protected void dispose() {
		f_model.dispose();
		graphics.removeProxyListener(gr_listener);
		glyphs.removeProxyListener(gl_listener);
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
			public void perform() {
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
		geditor = new GlyphEditor(this, admin);
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
		public BitmapGraphic bmap;

		protected GlyphData(Glyph g) {
			glyph = g;
			graphic = glyph.getGraphic();
			updateBitmap();
		}

		protected void updateBitmap() {
			bmap = new BitmapGraphic(graphic.getWidth(),
				graphic.getHeight());
			try {
				bmap.setBitmap(Base64.decode(
					graphic.getPixels()));
			}
			catch(IOException e) {
				// Oh well, the Graphic is invalid
				// Should we throw up an error dialog?
			}
			catch(IndexOutOfBoundsException e) {
				// Oh well, the Graphic is invalid
				// Should we throw up an error dialog?
			}
		}
	}

	/** Lookup the glyphs in the selected font */
	protected void lookupGlyphs(Font font) {
		synchronized(gmap) {
			gmap.clear();
		}
		Map<String, Glyph> all_glyphs = glyphs.getAll();
		LinkedList<Glyph> glist = new LinkedList<Glyph>();
		synchronized(all_glyphs) {
			for(Glyph g: all_glyphs.values())
				if(g.getFont() == font)
					glist.add(g);
		}
		synchronized(gmap) {
			for(Glyph g: glist)
				addGlyph(g);
		}
	}

	/** Check if the currently selected font is deletable */
	protected boolean isFontDeletable() {
		if(font == null)
			return false;
		synchronized(gmap) {
			return gmap.isEmpty();
		}
	}

	/** Add a Glyph to the glyph map */
	protected void addGlyph(Glyph g) {
		synchronized(gmap) {
			String c = String.valueOf((char)g.getCodePoint());
			gmap.put(c, new GlyphData(g));
			del_font.setEnabled(isFontDeletable());
		}
	}

	/** Remove a Glyph from the glyph map */
	protected void removeGlyph(Glyph g) {
		synchronized(gmap) {
			String c = String.valueOf((char)g.getCodePoint());
			gmap.remove(c);
			del_font.setEnabled(isFontDeletable());
		}
	}

	/** Update a Graphic in the GlyphData map */
	protected void updateGraphic(Graphic g) {
		synchronized(gmap) {
			for(GlyphData gd: gmap.values()) {
				if(gd.graphic == g) {
					gd.updateBitmap();
					repaint();
					break;
				}
			}
		}
	}

	/** Change the selected font */
	protected void selectFont() {
		ListSelectionModel s = f_table.getSelectionModel();
		font = f_model.getProxy(s.getMinSelectionIndex());
		lookupGlyphs(font);
		del_font.setEnabled(isFontDeletable());
		glist.setCellRenderer(new GlyphCellRenderer(gmap));
		geditor.setFont(font);
		selectGlyph();
	}

	/** Lookup the glyph data */
	protected GlyphData lookupGlyphData(String v) {
		synchronized(gmap) {
			return gmap.get(v);
		}
	}

	/** Change the selected glyph */
	protected void selectGlyph() {
		Object value = glist.getSelectedValue();
		if(value != null) {
			GlyphData gdata = lookupGlyphData(value.toString());
			geditor.setGlyph(gdata);
		}
	}

	/** Glyph deferred creation data */
	protected class GlyphDeferred {
		public final Font font;
		public final int code_point;

		public GlyphDeferred(Font f, int c) {
			font = f;
			code_point = c;
		}
	}

	/** Create a new Glyph */
	protected void createGlyph() {
		Object value = glist.getSelectedValue();
		Font f = font;
		if(value != null && f != null) {
			int c = value.toString().codePointAt(0);
			String name = f.getName() + "_" + c;
			synchronized(deferred) {
				deferred.put(name, new GlyphDeferred(f, c));
			}
			graphics.createObject(name);
			glyphs.createObject(name);
		}
	}
}
