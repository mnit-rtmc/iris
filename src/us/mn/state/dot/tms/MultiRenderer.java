/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.LinkedList;

/**
 * A MULTI renderer is for rendering MULTI on a raster graphic.
 *
 * @author Douglas Lau
 */
public class MultiRenderer extends MultiStringStateAdapter {

	/** Enumeration of MULTI syntax errors */
	static public enum SyntaxError {
		undefined, other, none, unsupportedTag, unsupportedTagValue,
		textTooBig, fontNotDefined, characterNotDefined,
		fieldDeviceNotExist, fieldDeviceError, flashRegionError,
		tagConflict, tooManyPages, fontVersionID, graphicID,
		graphicNotDefined;
	}

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
	protected SyntaxError syntax_err = SyntaxError.none;

	/** X position of text rectangle (1-based) */
	protected int tr_x;

	/** Y position of text rectangle (1-based) */
	protected int tr_y;

	/** Width of text rectangle */
	protected int tr_width;

	/** Height of text rectangle */
	protected int tr_height;

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
	public void setJustificationPage(MultiString.JustificationPage jp) {
		super.setJustificationPage(jp);
		Block block = new Block();
		Block cb = currentBlock();
		if(block.justp.ordinal() < cb.justp.ordinal())
			syntax_err = SyntaxError.tagConflict;
		if(block.justp.ordinal() > cb.justp.ordinal())
			blocks.addLast(block);
	}

	/** Add a span of text */
	public void addSpan(String span) {
		if(page == ms_page) {
			Span s = new Span(span);
			if(s.font != null) {
				Block block = currentBlock();
				block.addSpan(s);
			} else
				syntax_err = SyntaxError.fontNotDefined;
		}
	}

	/** Add a line */
	public void addLine() {
		super.addLine();
		Block block = currentBlock();
		block.addLine();
	}

	/** Get the current text block */
	protected Block currentBlock() {
		if(blocks.isEmpty())
			blocks.addLast(new Block());
		return blocks.peekLast();
	}

	/** Add a page */
	public void addPage() {
		renderText();
		resetTextRectangle();
		super.addPage();
	}

	/** Set the text rectangle */
	public void setTextRectangle(int x, int y, int w, int h) {
		renderText();
		tr_x = x;
		tr_y = y;
		if(w == 0)
			w = raster.getWidth() - (x - 1);
		if(h == 0)
			h = raster.getHeight() - (y - 1);
		tr_width = w;
		tr_height = h;
		if(tr_x + tr_width > raster.getWidth() + 1)
			syntax_err = SyntaxError.unsupportedTagValue;
		if(tr_y + tr_height > raster.getHeight() + 1)
			syntax_err = SyntaxError.unsupportedTagValue;
	}

	/** Complete the rendering */
	public void complete() {
		renderText();
	}

	/** Render the current text rectangle */
	protected void renderText() {
		if(page == ms_page) {
			try {
				for(Block block: blocks)
					block.render();
			}
			catch(InvalidMessageException e) {
				syntax_err = SyntaxError.characterNotDefined;
			}
			catch(IndexOutOfBoundsException e) {
				syntax_err = SyntaxError.textTooBig;
			}
		}
		blocks.clear();
	}

	/** Add a graphic */
	public void addGraphic(int g_num, Integer x, Integer y, String g_id) {
		if(page != ms_page)
			return;
		Graphic graphic = GraphicHelper.find(g_num);
		if(graphic == null) {
			syntax_err = SyntaxError.graphicNotDefined;
			return;
		}
		int x0 = 1;
		int y0 = 1;
		if(x != null)
			x0 = x;
		if(y != null)
			y0 = y;
		renderGraphic(graphic, x0, y0);
	}

	/** Render a graphic onto the raster.
	 * @param g Graphic to render
	 * @param x X-position on raster (1-based)
	 * @param y Y-position on raster (1-based) */
	protected void renderGraphic(Graphic g, int x, int y) {
		x--;
		y--;
		int w = g.getWidth();
		int h = g.getHeight();
		try {
			byte[] pixels = Base64.decode(g.getPixels());
			BitmapGraphic bg = new BitmapGraphic(w, h);
			bg.setPixels(pixels);
			for(int yy = 0; yy < h; yy++) {
				for(int xx = 0; xx < w; xx++) {
					if(bg.getPixel(xx, yy).isLit()) {
						raster.setPixel(x + xx, y + yy,
							ms_foreground);
					}
				}
			}
		}
		catch(IOException e) {
			// This happens if the graphic contains
			// invalid Base64 pixel data.
			syntax_err = SyntaxError.other;
		}
	}

	/** A block of text to be rendered */
	protected class Block {
		protected final LinkedList<Line> lines = new LinkedList<Line>();
		protected final MultiString.JustificationPage justp = ms_justp;
		void addSpan(Span s) {
			Line line = currentLine();
			line.addSpan(s);
		}
		void addLine() {
			Line line = currentLine();
			if(line.getHeight() == 0) {
				// The line height can be zero on full-matrix
				// signs when no text has been specified.
				// Adding an empty span to the line allows the
				// height to be taken from the current font.
				line.addSpan(new Span(""));
			}
			lines.addLast(new Line());
		}
		Line currentLine() {
			if(lines.isEmpty())
				lines.addLast(new Line());
			return lines.peekLast();
		}
		void render() throws InvalidMessageException {
			int top = getTop();
			if(top < tr_y) {
				syntax_err = SyntaxError.textTooBig;
				return;
			}
			int y = 0;
			Line pline = null;
			for(Line line: lines) {
				y += line.getSpacing(pline);
				y += line.getHeight();
				line.render(top + y);
				pline = line;
			}
		}
		int getTop() {
			switch(justp) {
			case TOP:
				return tr_y;
			case MIDDLE:
				int ch = (c_height <= 0 ? 1 : c_height);
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
			for(Line line: lines) {
				int lh = line.getHeight();
				if(lh > 0) {
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
		int getHeight() {
			int h = c_height;
			for(Fragment f: fragments)
				h = Math.max(h, f.getHeight());
			return h;
		}
		int getSpacing() {
			int ls = 0;
			for(Fragment f: fragments)
				ls = Math.max(ls, f.getSpacing());
			return ls;
		}
		int getSpacing(Line other) {
			if(other == null)
				return 0;
			int sp0 = getSpacing();
			int sp1 = other.getSpacing();
			// NTCIP 1203 fontLineSpacing:
			// "The number of pixels between adjacent lines is the
			// average of the 2 line spacings of each line, rounded
			// up to the nearest whole pixel." ???
			return Math.round((sp0 + sp1) / 2.0f);
		}
		void addSpan(Span s) {
			Fragment f = new Fragment();
			Fragment cf = currentFragment();
			if(f.justl.ordinal() < cf.justl.ordinal())
				syntax_err = SyntaxError.tagConflict;
			if(f.justl.ordinal() > cf.justl.ordinal())
				fragments.addLast(f);
			currentFragment().addSpan(s);
		}
		Fragment currentFragment() {
			if(fragments.isEmpty())
				fragments.addLast(new Fragment());
			return fragments.peekLast();
		}
		void render(int base) throws InvalidMessageException {
			for(Fragment f: fragments)
				f.render(base);
		}
	}

	/** A fragment of text to be rendered */
	protected class Fragment {
		protected final LinkedList<Span> spans = new LinkedList<Span>();
		protected final MultiString.JustificationLine justl = ms_justl;
		int getHeight() {
			int h = c_height;
			for(Span s: spans)
				h = Math.max(h, s.font.getHeight());
			return h;
		}
		int getSpacing() {
			int ls = 0;
			for(Span s: spans)
				ls = Math.max(ls, s.font.getLineSpacing());
			return ls;
		}
		void addSpan(Span s) {
			spans.add(s);
		}
		void render(int base) throws InvalidMessageException {
			int left = getLeft();
			if(left < tr_x) {
				syntax_err = SyntaxError.textTooBig;
				return;
			}
			int x = 0;
			Span pspan = null;
			for(Span span: spans) {
				x += span.getCharSpacing(pspan);
				span.render(left + x, base);
				x += span.getWidth();
				pspan = span;
			}
		}
		int getLeft() {
			switch(justl) {
			case LEFT:
				return tr_x;
			case CENTER:
				int cw = (c_width <= 0 ? 1 : c_width);
				int w = tr_width / cw;
				int r = getWidth() / cw;
				return tr_x + (w - r) / 2 * cw;
			case RIGHT:
				return tr_x + tr_width - getWidth();
			default:
				return 0;
			}
		}
		int getWidth() {
			int w = 0;
			Span ps = null;
			for(Span span: spans) {
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
		protected Span(String s) {
			span = s;
			font = FontHelper.find(ms_fnum);
		}
		int getCharSpacing(Span other) {
			if(other == null)
				return 0;
			int sp0 = font.getCharSpacing();
			int sp1 = other.font.getCharSpacing();
			// NTCIP 1203 fontCharSpacing:
			// "... the average character spacing of the two fonts,
			// rounded up to the nearest whole pixel ..." ???
			return Math.round((sp0 + sp1) / 2.0f);
		}
		int getWidth() {
			try {
				return FontHelper.calculateWidth(font, span);
			}
			catch(InvalidMessageException e) {
				syntax_err = SyntaxError.characterNotDefined;
				return 0;
			}
		}
		void render(int x, int base) throws InvalidMessageException {
			int y = base - font.getHeight();
			for(int i = 0; i < span.length(); i++) {
				int cp = span.charAt(i);
				Graphic g = FontHelper.lookupGraphic(font, cp);
				renderGraphic(g, x, y);
				x += g.getWidth() + font.getCharSpacing();
			}
		}
	}
}
