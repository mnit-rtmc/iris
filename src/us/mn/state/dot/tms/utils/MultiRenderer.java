/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.util.LinkedList;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A MULTI renderer is for rendering MULTI on a raster graphic.
 *
 * @author Douglas Lau
 */
public class MultiRenderer extends MultiAdapter {

	/** Default line justification */
	static public JustificationLine defaultJustificationLine() {
		return JustificationLine.fromOrdinal(SystemAttrEnum
			.DMS_DEFAULT_JUSTIFICATION_LINE.getInt());
	}

	/** Default page justification */
	static public JustificationPage defaultJustificationPage() {
		return JustificationPage.fromOrdinal(SystemAttrEnum
			.DMS_DEFAULT_JUSTIFICATION_PAGE.getInt());
	}

	/** Get a color for the color scheme.
	 * @param x Color value.
	 * @return DmsColor or null for invalid color. */
	static private DmsColor schemeColor(int x) {
		// FIXME: add support for monochrome color schemes
		ColorClassic cc = ColorClassic.fromOrdinal(x);
		return (cc != null) ? cc.clr : null;
	}

	/** Raster graphic factory */
	private final RasterGraphic.Factory factory;

	/** Character width (pixels) for character-matrix signs.  Set to 1 for
	 * line-matrix or full-matrix signs. */
	private final int c_width;

	/** Character height (pixels) for character- or line-matrix signs.
	 * Set to 1 for full-matrix signs. */
	private final int c_height;

	/** MULTI syntax error */
	private MultiSyntaxError syntax_err = MultiSyntaxError.none;

	/** X position of text rectangle (1-based) */
	private int tr_x;

	/** Y position of text rectangle (1-based) */
	private int tr_y;

	/** Width of text rectangle */
	private int tr_width;

	/** Height of text rectangle */
	private int tr_height;

	/** Character spacing (set by [sc] tag) */
	private Integer char_spacing;

	/** List of all blocks within the current text rectangle */
	private final LinkedList<Block> blocks = new LinkedList<Block>();

	/** Current page justification */
	private JustificationPage just_page = defaultJustificationPage();

	/** Current line justification */
	private JustificationLine just_line = defaultJustificationLine();

	/** Current raster graphic to render */
	private RasterGraphic raster;

	/** Current font number */
	private int font_num;

	/** Page background color */
	private DmsColor background_clr = DmsColor.BLACK;

	/** Foreground color */
	private DmsColor foreground_clr = DmsColor.AMBER;

	/**
	 * Create a new MULTI renderer.
	 * @param fct Raster graphic factory.
	 * @param cw Character width (pixels) for character-matrix signs.
	 *           Use 0 for line-matrix or full-matrix signs.
	 * @param ch Character height (pixels) for character- or line-matrix
	 *           signs.  Use 0 for full-matrix signs.
	 * @param f Default font number.
	 */
	public MultiRenderer(RasterGraphic.Factory fct, int cw, int ch, int f) {
		factory = fct;
		raster = factory.create();
		c_width = (cw > 0) ? cw : 1;
		c_height = (ch > 0) ? ch : 1;
		font_num = f;
		resetTextRectangle();
	}

	/** Check for character-matrix sign */
	private boolean isCharMatrix() {
		return c_width > 1;
	}

	/** Check for full-matrix sign */
	private boolean isFullMatrix() {
		return c_height == 1;
	}

	/** Reset the text rectangle to the size of the raster */
	private void resetTextRectangle() {
		tr_x = 1;
		tr_y = 1;
		tr_width = raster.getWidth();
		tr_height = raster.getHeight();
	}

	/** Handle an unsupported tag */
	@Override
	public void unsupportedTag(String tag) {
		syntax_err = MultiSyntaxError.unsupportedTag;
	}

	/** Set the page justification */
	@Override
	public void setJustificationPage(JustificationPage jp) {
		just_page = jp;
		Block block = new Block();
		Block cb = currentBlock();
		if (block.justp.ordinal() < cb.justp.ordinal())
			syntax_err = MultiSyntaxError.tagConflict;
		if (block.justp.ordinal() > cb.justp.ordinal())
			blocks.addLast(block);
	}

	/** Set the line justification */
	@Override
	public void setJustificationLine(JustificationLine jl) {
		just_line = jl;
	}

	/** Set the font number.
	 * @param f_num Font number (1 to 255)
	 * @param f_id Font version ID (4-digit hex) */
	@Override
	public void setFont(int f_num, String f_id) {
		font_num = f_num;
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
		Span s = new Span(span);
		if (s.font != null) {
			Block block = currentBlock();
			block.addSpan(s);
		} else
			syntax_err = MultiSyntaxError.fontNotDefined;
	}

	/** Add a new line */
	@Override
	public void addLine(Integer ls) {
		super.addLine(ls);
		Block block = currentBlock();
		block.addLine(ls);
	}

	/** Get the current text block */
	private Block currentBlock() {
		if (blocks.isEmpty())
			blocks.addLast(new Block());
		return blocks.peekLast();
	}

	/** Add a page */
	@Override
	public void addPage() {
		renderText();
		raster = factory.create();
		fillBackground();
		resetTextRectangle();
	}

	/** Set the (deprecated) message background color.
	 * @param x Background color (0-9; colorClassic value). */
	@Override
	public void setColorBackground(int x) {
		ColorClassic cc = ColorClassic.fromOrdinal(x);
		if (cc != null)
			background_clr = cc.clr;
		fillBackground();
	}

	/** Set the page background color for monochrome1bit, monochrome8bit,
	 * and colorClassic color schemes.
	 * @param z Background color (0-1 for monochrome1bit),
	 *                           (0-255 for monochrome8bit),
	 *                           (0-9 for colorClassic). */
	@Override
	public void setPageBackground(int z) {
		DmsColor clr = schemeColor(z);
		if (clr != null)
			background_clr = clr;
		fillBackground();
	}

	/** Set the page background color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	@Override
	public void setPageBackground(int r, int g, int b) {
		background_clr = new DmsColor(r, g, b);
		fillBackground();
	}

	/** Fill the page with the current background color */
	private void fillBackground() {
		fillRectangle(1, 1, raster.getWidth(), raster.getHeight(),
			background_clr);
	}

	/** Set the foreground color for monochrome1bit, monochrome8bit, and
	 * colorClassic color schemes.
	 * @param x Foreground color (0-1 for monochrome1bit),
	 *                           (0-255 for monochrome8bit),
	 *                           (0-9 for colorClassic). */
	@Override
	public void setColorForeground(int x) {
		DmsColor clr = schemeColor(x);
		if (clr != null)
			foreground_clr = clr;
	}

	/** Set the foreground color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	@Override
	public void setColorForeground(int r, int g, int b) {
		foreground_clr = new DmsColor(r, g, b);
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
		x--;	/* make X zero-based for raster */
		y--;	/* make Y zero-based for raster */
		for (int yy = 0; yy < h; yy++) {
			for (int xx = 0; xx < w; xx++)
				raster.setPixel(x + xx, y + yy, clr);
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
	private void renderText() {
		try {
			for (Block block: blocks)
				block.render();
		}
		catch (InvalidMsgException e) {
			syntax_err = MultiSyntaxError.characterNotDefined;
		}
		catch (IndexOutOfBoundsException e) {
			syntax_err = MultiSyntaxError.textTooBig;
		}
		blocks.clear();
	}

	/** Add a graphic */
	@Override
	public void addGraphic(int g_num, Integer x, Integer y, String g_id) {
		Graphic graphic = GraphicHelper.find(g_num);
		if (graphic == null) {
			syntax_err = MultiSyntaxError.graphicNotDefined;
			return;
		}
		int x0 = (x != null) ? x : 1;
		int y0 = (y != null) ? y : 1;
		renderGraphic(graphic, foreground_clr, x0, y0);
	}

	/** Render a graphic onto the raster.
	 * @param g Graphic to render.
	 * @param fg Foreground color.
	 * @param x X-position on raster (1-based)
	 * @param y Y-position on raster (1-based) */
	private void renderGraphic(Graphic g, DmsColor fg, int x, int y) {
		x--;
		y--;
		RasterGraphic rg = GraphicHelper.createRaster(g);
		if (rg != null) {
			try {
				raster.copy(rg, x, y, fg);
			}
			catch (IndexOutOfBoundsException e) {
				// No MULTI syntax error for graphic too big
				syntax_err = MultiSyntaxError.other;
			}
		} else
			syntax_err = MultiSyntaxError.graphicNotDefined;
	}

	/** A block of text to be rendered */
	private class Block {
		private final LinkedList<Line> lines = new LinkedList<Line>();
		private final JustificationPage justp = just_page;
		void addSpan(Span s) {
			Line line = currentLine();
			line.addSpan(s);
		}
		void addLine(Integer ls) {
			Line line = currentLine();
			if (line.getHeight() == 0) {
				// The line height can be zero on full-matrix
				// signs when no text has been specified.
				// Adding an empty span to the line allows the
				// height to be taken from the current font.
				line.addSpan(new Span(""));
			}
			lines.addLast(new Line(ls));
		}
		Line currentLine() {
			if (lines.isEmpty())
				lines.addLast(new Line(null));
			return lines.peekLast();
		}
		void render() throws InvalidMsgException {
			int ex = getExtraHeight();
			if (ex < 0) {
				syntax_err = MultiSyntaxError.textTooBig;
				return;
			}
			int top = getTop(ex);
			int y = 0;
			Line pline = null;
			for (Line line: lines) {
				y += line.getLineSpacing(pline);
				y += line.getHeight();
				line.render(top + y);
				pline = line;
			}
		}
		int getExtraHeight() {
			int h = tr_height / c_height;
			int r = getHeight() / c_height;
			return (h - r) * c_height;
		}
		int floorCharHeight(int ex) {
			return (ex / c_height) * c_height;
		}
		int getTop(int ex) {
			switch (justp) {
			case TOP:
				return tr_y;
			case MIDDLE:
				return tr_y + floorCharHeight(ex / 2);
			case BOTTOM:
				return tr_y + ex;
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
					h += line.getLineSpacing(pline) + lh;
					pline = line;
				}
			}
			return h;
		}
	}

	/** A line of text to be rendered */
	private class Line {
		private final LinkedList<Fragment> fragments =
			new LinkedList<Fragment>();
		private final Integer line_spacing;
		Line(Integer s) {
			line_spacing = s;
		}
		int getHeight() {
			int h = 0;
			for (Fragment f: fragments)
				h = Math.max(h, f.getHeight());
			return h;
		}
		private int getFragmentSpacing() {
			int ls = 0;
			for (Fragment f: fragments)
				ls = Math.max(ls, f.getLineSpacing());
			return ls;
		}
		int getLineSpacing(Line prev) {
			if (!isFullMatrix())
				return 0;
			if (line_spacing != null)
				return line_spacing;
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
		void render(int base) throws InvalidMsgException {
			for (Fragment f: fragments)
				f.render(base);
		}
	}

	/** A fragment of text to be rendered */
	private class Fragment {
		private final LinkedList<Span> spans = new LinkedList<Span>();
		private final JustificationLine justl = just_line;
		int getHeight() {
			int h = 0;
			for (Span s: spans)
				h = Math.max(h, s.getHeight());
			return h;
		}
		int getLineSpacing() {
			int ls = 0;
			for (Span s: spans)
				ls = Math.max(ls, s.getLineSpacing());
			return ls;
		}
		void addSpan(Span s) {
			spans.add(s);
		}
		void render(int base) throws InvalidMsgException {
			int ex = getExtraWidth();
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
		int getExtraWidth() {
			int w = tr_width / c_width;
			int r = getWidth() / c_width;
			return (w - r) * c_width;
		}
		int floorCharWidth(int ex) {
			return (ex / c_width) * c_width;
		}
		int getLeft(int ex) {
			switch (justl) {
			case LEFT:
				return tr_x;
			case CENTER:
				return tr_x + floorCharWidth(ex / 2);
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
	private class Span {
		private final String span;
		private final Font font;
		private final DmsColor foreground;
		private final int c_space;
		private Span(String s) {
			span = s;
			font = FontHelper.find(font_num);
			foreground = foreground_clr;
			c_space = getCharSpacing();
		}
		int getCharSpacing() {
			if (isCharMatrix())
				return 0;
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
			assert font != null;
			return (font != null) ? font.getHeight() : 0;
		}
		int getWidth() {
			try {
				return FontHelper.calculateWidth(font, span,
					c_space);
			}
			catch (InvalidMsgException e) {
				syntax_err=MultiSyntaxError.characterNotDefined;
				return 0;
			}
		}
		int getLineSpacing() {
			assert font != null;
			return (font != null) ? font.getLineSpacing() : 0;
		}
		void render(int x, int base) throws InvalidMsgException {
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
