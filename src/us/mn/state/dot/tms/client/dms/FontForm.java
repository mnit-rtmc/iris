/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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

import java.util.Collection;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;

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

	/** Listener for Graphic proxy events */
	private final SwingProxyAdapter<Graphic> gr_listener =
		new SwingProxyAdapter<Graphic>(true)
	{
		@Override
		protected void proxyChangedSwing(Graphic proxy, String attr) {
			if (isFromSelectedFont(proxy))
				updateGraphic(proxy);
		}
		@Override
		protected boolean checkAttributeChange(String attr) {
			// The "pixels" attribute should be the
			// last one changed (after width)
			return attr.equals("pixels");
		}
	};

	/** Check if the specified Graphic is from the selected font */
	private boolean isFromSelectedFont(Graphic p) {
		Font f = font;
		if (f != null)
			return p.getName().startsWith(f.getName());
		else
			return false;
	}

	/** Proxy listener for Glyph proxies */
	private final SwingProxyAdapter<Glyph> gl_listener =
		new SwingProxyAdapter<Glyph>(true)
	{
		@Override
		protected void proxyAddedSwing(Glyph proxy) {
			if (isFromSelectedFont(proxy))
				addGlyph(proxy);
		}
		@Override
		protected void proxyRemovedSwing(Glyph proxy) {
			if (isFromSelectedFont(proxy))
				removeGlyph(proxy);
		}
	};

	/** Check if the specified Glyph is from the selected font */
	private boolean isFromSelectedFont(Glyph p) {
		return p.getFont() == font;
	}

	/** Glyph type cache */
	private final TypeCache<Glyph> glyphs;

	/** Graphic type cache */
	private final TypeCache<Graphic> graphics;

	/** Map of glyph data for currently selected font */
	private final HashMap<Integer, GlyphInfo> gmap =
		new HashMap<Integer, GlyphInfo>();

	/** Font panel */
	private final ProxyTablePanel<Font> font_pnl;

	/** Sign pixel panel */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(40, 400,
		true);

	/** Glyph list */
	private final JList<Integer> glist = new JList<Integer>();

	/** Glyph list cell renderer */
	private final GlyphCellRenderer renderer = new GlyphCellRenderer();

	/** Glyph list panel */
	private final JPanel glist_pnl;

	/** Glyph panel */
	private final GlyphPanel glyph_pnl;

	/** Selected font */
	private Font font;

	/** Create a new font form */
	public FontForm(Session s) {
		super(I18N.get("font.title"));
		glyphs = s.getSonarState().getDmsCache().getGlyphs();
		graphics = s.getSonarState().getGraphics();
		font_pnl = new ProxyTablePanel<Font>(new FontModel(s)) {
			protected void selectProxy() {
				super.selectProxy();
				selectFont();
			}
		};
		glist.setCellRenderer(renderer);
		glist_pnl = createGlyphListPanel();
		glyph_pnl = new GlyphPanel(s);
	}

	/** Initializze the widgets in the form */
	@Override
	protected void initialize() {
		super.initialize();
		font_pnl.initialize();
		graphics.addProxyListener(gr_listener);
		glyphs.addProxyListener(gl_listener);
		layoutPanel();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		font_pnl.dispose();
		glyph_pnl.dispose();
		graphics.removeProxyListener(gr_listener);
		glyphs.removeProxyListener(gl_listener);
		super.dispose();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		hg.addComponent(font_pnl);
		hg.addComponent(pixel_pnl);
		GroupLayout.SequentialGroup g0 = gl.createSequentialGroup();
		g0.addComponent(glist_pnl);
		g0.addGap(UI.hgap);
		g0.addComponent(glyph_pnl);
		hg.addGroup(g0);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		vg.addComponent(font_pnl);
		vg.addGap(UI.vgap);
		vg.addComponent(pixel_pnl);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup g0 = gl.createParallelGroup();
		g0.addComponent(glist_pnl);
		g0.addComponent(glyph_pnl);
		vg.addGroup(g0);
		return vg;
	}

	/** Create a glyph list panel */
	private JPanel createGlyphListPanel() {
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder(
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
		p.add(glist);
		return p;
	}

	/** Create a list model containing font code points */
	private ListModel<Integer> createCodePointModel() {
		DefaultListModel<Integer> mdl = new DefaultListModel<Integer>();
		for (int i = 32; i < 127; i++)
			mdl.addElement(i);
		return mdl;
	}

	/** Lookup the glyphs in the selected font */
	private void lookupGlyphs(Font font) {
		Collection<Glyph> gs = FontHelper.lookupGlyphs(font);
		gmap.clear();
		renderer.clearBitmaps();
		for (Glyph g: gs)
			addGlyph(g);
		glist.repaint();
		selectGlyph();
	}

	/** Check if the currently selected font is deletable */
	private boolean isFontDeletable() {
		return (font != null) && gmap.isEmpty();
	}

	/** Add a Glyph to the glyph map */
	private void addGlyph(Glyph g) {
		int c = g.getCodePoint();
		GlyphInfo gi = new GlyphInfo(c, g);
		renderer.setBitmap(c, gi.bmap);
		gmap.put(c, gi);
		glyph_pnl.setGlyph(gi);
		font_pnl.updateButtonPanel();
	}

	/** Remove a Glyph from the glyph map */
	private void removeGlyph(Glyph g) {
		int c = g.getCodePoint();
		renderer.setBitmap(c, null);
		gmap.remove(c);
		glyph_pnl.setGlyph(glyphInfo(c));
		font_pnl.updateButtonPanel();
	}

	/** Update a Graphic in the GlyphInfo map */
	private void updateGraphic(Graphic g) {
		GlyphInfo gi = findGlyph(g);
		if (gi != null) {
			addGlyph(gi.glyph);
			glist.repaint();
			pixel_pnl.repaint();
		}
	}

	/** Find glyph data for a graphic */
	private GlyphInfo findGlyph(Graphic g) {
		for (GlyphInfo gi: gmap.values()) {
			if (gi.graphic == g)
				return gi;
		}
		return null;
	}

	/** Change the selected font */
	private void selectFont() {
		Font f = font_pnl.getSelectedProxy();
		glyph_pnl.setFont(f);
		font = f;
		lookupGlyphs(f);
		font_pnl.updateButtonPanel();
		int h = fontHeight(f);
		int cw = fontWidth(f);
		int w = (cw > 0) ? (512 / cw) * cw : 512;
		pixel_pnl.setPhysicalDimensions(w, h, 4, 4, 1, 1);
		pixel_pnl.setLogicalDimensions(w, h, cw, 0);
		pixel_pnl.setGraphic(renderMessage(f));
		pixel_pnl.repaint();
	}

	/** Get the font height */
	private int fontHeight(Font f) {
		return (f != null) ? f.getHeight() : 0;
	}

	/** Get the font width */
	private int fontWidth(Font f) {
		return (f != null) ? f.getWidth() : 0;
	}

	/** Render a message to a raster graphic */
	private RasterGraphic renderMessage(Font f) {
		if (f != null) {
			MultiString ms = new MultiString(I18N.get(
				"font.glyph.sample"));
			RasterGraphic[] pages = renderPages(f, ms);
			if (pages != null && pages.length > 0)
				return pages[0];
		}
		return null;
	}

	/** Render the pages of a text message */
	private RasterGraphic[] renderPages(Font f, MultiString ms) {
		int h = fontHeight(f);
		int cw = fontWidth(f);
		int w = (cw > 0) ? (512 / cw) * cw : 512;
		int df = f.getNumber();
		RasterBuilder b = new RasterBuilder(w, h, cw, 0, df);
		try {
			return b.createPixmaps(ms);
		}
		catch (InvalidMsgException e) {
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
		return (value instanceof Integer) ? (Integer)value : 0;
	}

	/** Get glyph information */
	private GlyphInfo glyphInfo(int c) {
		GlyphInfo gi = gmap.get(c);
		return (gi != null) ? gi : new GlyphInfo(c, null);
	}
}
