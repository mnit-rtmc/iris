/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.LinkedList;

/**
 * A MULTI renderer is for rendering MULTI on a raster graphic.
 *
 * @author Douglas Lau
 */
public class MultiRenderer extends MultiAdapter {

	/** Raster graphic to render */
	protected final RasterGraphic raster;

	/** Page to render */
	protected final int page;

	/** Character width (pixels) for character-matrix signs.  Use 0 for
	 * line-matrix or full-matrix signs. */
	protected final int c_width;

	/** Character height (pixels) for character- or line-matrix signs.
	 * Use 0 for full-matrix signs. */
	protected final int c_height;

	/** MULTI syntax error */
	private MultiSyntaxError syntax_err = MultiSyntaxError.none;

	/** X position of text rectangle (1-based) */
	protected int tr_x;

	/** Y position of text rectangle (1-based) */
	protected int tr_y;

	/** Width of text rectangle */
	protected int tr_width;

	/** Height of text rectangle */
	protected int tr_height;

	/** Character spacing */
	protected Integer char_spacing;

	/** List of all blocks within the current text rectangle */
	protected final LinkedList<Block> blocks = new LinkedList<Block>();

	/**
	 * Create a new MULTI renderer.
	 * @param r Raster graphic to render.
	 * @param p Page to render.
	 * @param cw Character width (pixels) for character-matrix signs.
	 *           Use 0 for line-matrix or full-matrix signs.
	 * @param ch Character height (pixels) for character- or line-matrix
	 *           signs.  Use 0 for full-matrix signs.
	 * @param f Default font number.
	 */
	public MultiRenderer(RasterGraphic r, int p, int cw, int ch, int f) {
		raster = r;
		page = p;
		c_width = cw;
		c_height = ch;
		ms_fnum = f;
		resetTextRectangle();
	}

	/** Reset the text rectangle to the size of the raster */
	protected void resetTextRectangle() {
		tr_x = 1;
		tr_y = 1;
		tr_width = raster.getWidth();
		tr_height = raster.getHeight();
	}

	/** Set the page justification */
	@Override
	public void setJustificationPage(JustificationPage jp) {
		super.setJustificationPage(jp);
		Block block = new Block();
		Block cb = currentBlock();
		if (block.justp.ordinal() < cb.justp.ordinal())
			syntax_err = MultiSyntaxError.tagConflict;
		if (block.justp.ordinal() > cb.justp.ordinal())
			blocks.addLast(block);
	}

	/** Set the character spacing.
	 * @param sc Character spacing (null means use font spacing) */
	@Override
	public void setCharSpacing(Integer sc) {
		char_spacing = sc;
	}

	/** Add a span of text */
	@Override
	public void addSpan(String span) {
		if (page == ms_page) {
			Span s = new Span(span);
			if (s.font != null) {
				Block block = currentBlock();
				block.addSpan(s);
			} else
				syntax_err = MultiSyntaxError.fontNotDefined;
		}
	}

	/** Add a new line */
	@Override
	public void addLine(Integer spacing) {
		super.addLine(spacing);
		Block block = currentBlock();
		block.addLine(spacing);
	}

	/** Get the current text block */
	protected Block currentBlock() {
		if (blocks.isEmpty())
			blocks.addLast(new Block());
		return blocks.peekLast();
	}

	/** Add a page */
	@Override
	public void addPage() {
		renderText();
		resetTextRectangle();
		super.addPage();
		fillBackground();
	}

	/** Set the page background color for monochrome1bit, monochrome8bit,
	 * and colorClassic color schemes.
	 * @param z Background color (0-1 for monochrome1bit),
	 *                           (0-255 for monochrome8bit),
	 *                           (0-9 for colorClassic). */
	@Override
	public void setPageBackground(int z) {
		super.setPageBackground(z);
		fillBackground();
	}

	/** Set the page background color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	@Override
	public void setPageBackground(int r, int g, int b) {
		super.setPageBackground(r, g, b);
		fillBackground();
	}

	/** Fill the page with the current background color */
	private void fillBackground() {
		fillRectangle(1, 1, raster.getWidth(), raster.getHeight(),
			ms_background);
	}

	/** Add a color rectangle for monochrome1bit, monochrome8bit, and
	 * colorClassic color schemes.
	 * @param x X pixel position of upper left corner.
	 * @param y Y pixel position of upper left corner.
	 * @param w Width in pixels.
	 * @param h Height in pixels.
	 * @param z Color of rectangle (0-1 for monochrome1bit),
	 *                             (0-255 for monochrome8bit),
	 *                             (0-9 for colorClassic). */
	@Override
	public void addColorRectangle(int x, int y, int w, int h, int z) {
		DmsColor clr = schemeColor(z);
		if (clr != null)
			fillRectangle(x, y, w, h, clr);
	}

	/** Add a color rectangle for color24bit color scheme.
	 * @param x X pixel position of upper left corner.
	 * @param y Y pixel position of upper left corner.
	 * @param w Width in pixels.
	 * @param h Height in pixels.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	@Override
	public void addColorRectangle(int x, int y, int w, int h, int r, int g,
		int b)
	{
		fillRectangle(x, y, w, h, new DmsColor(r, g, b));
	}

	/** Fill a rectangle with a specified color */
	private void fillRectangle(int x, int y, int w, int h, DmsColor clr) {
		if (page == ms_page) {
			x--;	/* make X zero-based for raster */
			y--;	/* make Y zero-based for raster */
			for (int yy = 0; yy < h; yy++) {
				for (int xx = 0; xx < w; xx++)
					raster.setPixel(x + xx, y + yy, clr);
			}
		}
	}

	/** Set the text rectangle */
	@Override
	public void setTextRectangle(int x, int y, int w, int h) {
		renderText();
		tr_x = x;
		tr_y = y;
		if (w == 0)
			w = raster.getWidth() - (x - 1);
		if (h == 0)
			h = raster.getHeight() - (y - 1);
		tr_width = w;
		tr_height = h;
		if (tr_x + tr_width > raster.getWidth() + 1)
			syntax_err = MultiSyntaxError.unsupportedTagValue;
		if (tr_y + tr_height > raster.getHeight() + 1)
			syntax_err = MultiSyntaxError.unsupportedTagValue;
	}

	/** Complete the rendering */
	public void complete() {
		renderText();
	}

	/** Get the syntax error state */
	public MultiSyntaxError getSyntaxError() {
		return syntax_err;
	}

	/** Render the current text rectangle */
	protected void renderText() {
		if (page == ms_page) {
			try {
				for (Block block: blocks)
					block.render();
			}
			catch (InvalidMessageException e) {
				syntax_err=MultiSyntaxError.characterNotDefined;
			}
			catch (IndexOutOfBoundsException e) {
				syntax_err = MultiSyntaxError.textTooBig;
			}
		}
		blocks.clear();
	}

	/** Add a graphic */
	@Override
	public void addGraphic(int g_num, Integer x, Integer y, String g_id) {
		if (page != ms_page)
			return;
		Graphic graphic = GraphicHelper.find(g_num);
		if (graphic == null) {
			syntax_err = MultiSyntaxError.graphicNotDefined;
			return;
		}
		int x0 = (x != null) ? x : 1;
		int y0 = (y != null) ? y : 1;
		renderGraphic(graphic, ms_foreground, x0, y0);
	}

	/** Render a graphic onto the raster.
	 * @param g Graphic to render.
	 * @param fg Foreground color.
	 * @param x X-position on raster (1-based)
	 * @param y Y-position on raster (1-based) */
	protected void renderGraphic(Graphic g, DmsColor fg, int x, int y) {
		x--;
		y--;
		RasterGraphic rg = GraphicHelper.createRaster(g);
		try {
			if (rg instanceof BitmapGraphic)
				renderBitmap((BitmapGraphic)rg, fg, x, y);
			else if (rg instanceof PixmapGraphic)
				renderPixmap((PixmapGraphic)rg, x, y);
			else
				syntax_err = MultiSyntaxError.graphicNotDefined;
		}
		catch (IndexOutOfBoundsException e) {
			// No MULTI syntax error for graphic too big
			syntax_err = MultiSyntaxError.other;
		}
	}

	/** Render a bitmap graphic onto the raster.
	 * @param bg BitmapGraphic to render.
	 * @param fg Foreground color.
	 * @param x X-position on raster (0-based)
	 * @param y Y-position on raster (0-based) */
	private void renderBitmap(BitmapGraphic bg, DmsColor fg, int x, int y) {
		int w = bg.getWidth();
		int h = bg.getHeight();
		for (int yy = 0; yy < h; yy++) {
			for (int xx = 0; xx < w; xx++) {
				if (bg.getPixel(xx, yy).isLit())
					raster.setPixel(x + xx, y + yy, fg);
			}
		}
	}

	/** Render a pixmap graphic onto the raster.
	 * @param pg PixmapGraphic to render.
	 * @param x X-position on raster (0-based)
	 * @param y Y-position on raster (0-based) */
	private void renderPixmap(PixmapGraphic pg, int x, int y) {
		int w = pg.getWidth();
		int h = pg.getHeight();
		for (int yy = 0; yy < h; yy++) {
			for (int xx = 0; xx < w; xx++) {
				DmsColor c = pg.getPixel(xx, yy);
				raster.setPixel(x + xx, y + yy, c);
			}
		}
	}

	/** A block of text to be rendered */
	protected class Block {
		protected final LinkedList<Line> lines = new LinkedList<Line>();
		protected final JustificationPage justp = ms_justp;
		void addSpan(Span s) {
			Line line = currentLine();
			line.addSpan(s);
		}
		void addLine(Integer spacing) {
			Line line = currentLine();
			if (line.getHeight() == 0) {
				// The line height can be zero on full-matrix
				// signs when no text has been specified.
				// Adding an empty span to the line allows the
				// height to be taken from the current font.
				line.addSpan(new Span(""));
			}
			lines.addLast(new Line(spacing));
		}
		Line currentLine() {
			if (lines.isEmpty())
				lines.addLast(new Line(null));
			return lines.peekLast();
		}
		void render() throws InvalidMessageException {
			int top = getTop();
			if (top < tr_y) {
				syntax_err = MultiSyntaxError.textTooBig;
				return;
			}
			int y = 0;
			Line pline = null;
			for (Line line: lines) {
				y += line.getSpacing(pline);
				y += line.getHeight();
				line.render(top + y);
				pline = line;
			}
		}
		int getTop() {
			switch (justp) {
			case TOP:
				return tr_y;
			case MIDDLE:
				int ch = (c_height > 0) ? c_height : 1;
				int h = tr_height / ch;
				int r = getHeight() / ch;
				return tr_y + (h - r) / 2 * ch;
			case BOTTOM:
				return tr_y + tr_height - getHeight();
			default:
				return 0;
			}
		}
		int getHeight() {
			int h = 0;
			Line pline = null;
			for (Line line: lines) {
				int lh = line.getHeight();
				if (lh > 0) {
					h += line.getSpacing(pline) + lh;
					pline = line;
				}
			}
			return h;
		}
	}

	/** A line of text to be rendered */
	protected class Line {
		protected final LinkedList<Fragment> fragments =
			new LinkedList<Fragment>();
		private final Integer spacing;
		Line(Integer s) {
			spacing = s;
		}
		int getHeight() {
			int h = c_height;
			for (Fragment f: fragments)
				h = Math.max(h, f.getHeight());
			return h;
		}
		private int getFragmentSpacing() {
			int ls = 0;
			for (Fragment f: fragments)
				ls = Math.max(ls, f.getSpacing());
			return ls;
		}
		int getSpacing(Line prev) {
			if (spacing != null)
				return spacing;
			else {
				if (prev == null)
					return 0;
				int sp0 = getFragmentSpacing();
				int sp1 = prev.getFragmentSpacing();
				// NTCIP 1203 fontLineSpacing:
				// "The number of pixels between adjacent lines
				// is the average of the 2 line spacings of each
				// line, rounded up to the nearest whole pixel."
				return Math.round((sp0 + sp1) / 2.0f);
			}
		}
		void addSpan(Span s) {
			Fragment f = new Fragment();
			Fragment cf = currentFragment();
			if (f.justl.ordinal() < cf.justl.ordinal())
				syntax_err = MultiSyntaxError.tagConflict;
			if (f.justl.ordinal() > cf.justl.ordinal())
				fragments.addLast(f);
			currentFragment().addSpan(s);
		}
		Fragment currentFragment() {
			if (fragments.isEmpty())
				fragments.addLast(new Fragment());
			return fragments.peekLast();
		}
		void render(int base) throws InvalidMessageException {
			for (Fragment f: fragments)
				f.render(base);
		}
	}

	/** A fragment of text to be rendered */
	protected class Fragment {
		protected final LinkedList<Span> spans = new LinkedList<Span>();
		protected final JustificationLine justl = ms_justl;
		int getHeight() {
			int h = c_height;
			for (Span s: spans)
				h = Math.max(h, s.getHeight());
			return h;
		}
		int getSpacing() {
			int ls = 0;
			for (Span s: spans)
				ls = Math.max(ls, s.getLineSpacing());
			return ls;
		}
		void addSpan(Span s) {
			spans.add(s);
		}
		void render(int base) throws InvalidMessageException {
			int ex = getExtra();
			if (ex < 0) {
				syntax_err = MultiSyntaxError.textTooBig;
				return;
			}
			int left = getLeft(ex);
			int x = 0;
			Span pspan = null;
			for (Span span: spans) {
				x += span.getCharSpacing(pspan);
				span.render(left + x, base);
				x += span.getWidth();
				pspan = span;
			}
		}
		int getExtra() {
			int cw = (c_width <= 0 ? 1 : c_width);
			int w = tr_width / cw;
			int r = getWidth() / cw;
			return (w - r) * cw;
		}
		int getLeft(int ex) {
			switch (justl) {
			case LEFT:
				return tr_x;
			case CENTER:
				return tr_x + ex / 2;
			case RIGHT:
				return tr_x + ex;
			default:
				return 0;
			}
		}
		int getWidth() {
			int w = 0;
			Span ps = null;
			for (Span span: spans) {
				w += span.getCharSpacing(ps) + span.getWidth();
				ps = span;
			}
			return w;
		}
	}

	/** A span of text to be rendered */
	protected class Span {
		protected final String span;
		protected final Font font;
		protected final DmsColor foreground;
		protected final int c_space;
		protected Span(String s) {
			span = s;
			font = FontHelper.find(ms_fnum);
			foreground = ms_foreground;
			c_space = getCharSpacing();
		}
		int getCharSpacing() {
			Integer cs = char_spacing;
			if (cs != null)
				return cs;
			else if (font != null)
				return font.getCharSpacing();
			else
				return 1;
		}
		int getCharSpacing(Span other) {
			if (other == null)
				return 0;
			int sp0 = c_space;
			int sp1 = other.c_space;
			// NTCIP 1203 fontCharSpacing:
			// "... the average character spacing of the two fonts,
			// rounded up to the nearest whole pixel ..." ???
			return Math.round((sp0 + sp1) / 2.0f);
		}
		int getHeight() {
			return font != null ? font.getHeight() : 0;
		}
		int getWidth() {
			try {
				return FontHelper.calculateWidth(font, span,
					c_space);
			}
			catch (InvalidMessageException e) {
				syntax_err=MultiSyntaxError.characterNotDefined;
				return 0;
			}
		}
		int getLineSpacing() {
			return (font != null) ? font.getLineSpacing() : 0;
		}
		void render(int x, int base) throws InvalidMessageException {
			int y = base - getHeight();
			for (int i = 0; i < span.length(); i++) {
				int cp = span.charAt(i);
				Graphic g = FontHelper.lookupGraphic(font, cp);
				renderGraphic(g, foreground, x, y);
				x += g.getWidth() + c_space;
			}
		}
	}
}
