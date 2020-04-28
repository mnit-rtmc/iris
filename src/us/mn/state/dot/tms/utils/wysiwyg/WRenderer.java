/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

package us.mn.state.dot.tms.utils.wysiwyg;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.MultiSyntaxError;
import us.mn.state.dot.tms.utils.wysiwyg.token.*;
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.Multi.JustificationPage;

/** Class that renders a WMessage (Multi
 *  message) for the IRIS WYSIWYG editor.
 *
 * @author John L. Stanley - SRF Consulting
 * (Parts of this class are derived from
 *  MultiRenderer, written by Doug Lau.)
 */
public class WRenderer {
	
	/** Flag to enable/disable verbose logging output */
	private final static boolean DEBUG = false; 
	
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

	private WFontCache fontCache = new WFontCache();
	
	private WGraphicCache graphicCache = new WGraphicCache();
	
	// Render state variables
	/** Sign/group default settings from MultiConfig */
	private WState startState;
	/** Work copy of render state */
	private WState state;

	/** List of all blocks within the current text rectangle */
	private final LinkedList<Block> blocks = new LinkedList<Block>();

	/** Current raster buffer to render into */
	private WRaster raster;

	/** Error manager */
	private WEditorErrorManager errMan;
	
	/**
	 * Create a new MULTI renderer.
	 * 
	 * @param mc MultiConfig for sign or sign-group.
	 */
	public WRenderer(MultiConfig mcfg) {
		setConfig(mcfg);
	}

	/**
	 * Create a new MULTI renderer with an error manager.
	 * 
	 * @param mc MultiConfig for sign or sign-group.
	 * @param em WRenderErrorManager for collecting errors.
	 */
	public WRenderer(MultiConfig mcfg, WEditorErrorManager em) {
		setConfig(mcfg);
		errMan = em;
	}

	public void setConfig(MultiConfig mcfg) {
//		this.mcfg       = mcfg;
		this.raster     = WRaster.create(mcfg);
		
		this.startState = new WState(mcfg, fontCache);
		this.state      = new WState(startState);
		resetTextRectangle();
	}
	
	/** Get current raster */
	public WRaster getRaster() {
		return raster;
	}

	/** Check for character-matrix sign */
	private boolean isCharMatrix() {
		return state.charWidth > 1;
	}

	/** Check for full-matrix sign */
	private boolean isFullMatrix() {
		return state.charHeight == 1;
	}

	/** Reset the text rectangle to the size of the raster */
	private void resetTextRectangle() {
		state.trX = 1;
		state.trY = 1;
		state.trW = raster.getWidth();
		state.trH = raster.getHeight();
	}

	//---------------------------------
	// WToken.doRender methods

	/** Handle an unsupported tag */
	public void unsupportedTag(WtUnsupportedTag tok) {
		renderError(MultiSyntaxError.unsupportedTag, tok);
	}

	/** Set the page justification.
	 * Use the sign's default page justification if jp is null. */
	/** Render a WtJustPage token */
	public void setJustificationPage(WtJustPage tok) {
		JustificationPage jp = tok.getJustification();
		if (jp == null)
			state.justPage = defaultJustificationPage();
		else
			state.justPage = jp;
		Block block = new Block();
		Block cb = currentBlock();
		if (block.justp.ordinal() < cb.justp.ordinal()) {
			renderError(MultiSyntaxError.tagConflict, tok);
		}
		else if (block.justp.ordinal() > cb.justp.ordinal())
			blocks.addLast(block);
	}

	/** Set the line justification.
	 * Use the sign's default line justification if jl is null. */
	/** Render a WtJustLine token */
	public void setJustificationLine(WtJustLine tok) {
		JustificationLine jl = tok.getJustification();
		if (jl == null)
			state.justLine = defaultJustificationLine();
		else
			state.justLine = jl;
	}

//	/** Set the font number.
//	 * @param f_num Font number (1 to 255)
//	 * @param f_id Font version ID (4-digit hex)
//	 * Use the sign's default font if f_num is null. */
//	@Override
//	public void setFont(Integer f_num, String f_id) {
//		if (f_num == null)
//			state.wfont = startState.wfont;
//		else {
//			state.setFont(f_num);
//		}
//	}

	/** Set the font number using a WtFont token.
	 * f_num Font number (1 to 255)
	 * Use the sign's default font if tok or f_num is null. */
	/** Render a WtFont token */
	public void setFont(WtFont tok) {
		Integer fontNum = null;
		if (tok != null)
			fontNum = tok.getFontNum();
		if (!state.setFont(fontNum))
			renderError(MultiSyntaxError.fontNotDefined, tok);
	}

//	/** Set the character spacing.
//	 * @param sc Character spacing (null means use font spacing) */
//	@Override
//	public void setCharSpacing(Integer sc) {
//		if (sc == null)
//			state.charSpacing = state.getWFont().getCharSpacing();
//		else
//			state.charSpacing = sc;
//	}

	/** Set the character spacing.
	 * @param sc Character spacing (null means use font spacing) */
	/** Render a WtCharSpacing token */
	public void setCharSpacing(WtCharSpacing tok) {
		Integer sc = tok.getCharSpacing();
		if (sc != null)
			state.charSpacing = sc;
		else
			state.charSpacing = state.getWFont().getCharSpacing();
	}

	/** Add a text character. */
	/** Render a WtTextChar token */
	public void addText(WtTextChar tok) {
		TextChar tc = new TextChar(tok);
		Block block = currentBlock();
		block.addText(tc);
		if (tc.wfont == null)
			renderError(MultiSyntaxError.fontNotDefined, tok);
	}

	/** Add an Iris-specific token ("action tag")
	 *  to the current rendering block.
	 *  To most of the rendering code, this looks
	 *  like a single, extra-wide, TextChar.
	 *  (Renders as a solid greenish box.)
	 * @param wt_Iris
	 */
	public void addIrisToken(Wt_IrisToken itok) {
		IrisTagBox itb = new IrisTagBox(itok);
		Block block = currentBlock();
		block.addText((TextChar)itb);
		if (itb.wfont == null)
			renderError(MultiSyntaxError.fontNotDefined, itok);
	}

	/** Add an anchor character. */
	public void addAnchor(WToken tok) {
		TextChar tc = new AnchorChar(tok);
		Block block = currentBlock();
		block.addText(tc);
	}

	/** Add an end-of-page anchor-character */
	public void addAnchor(WPage wPage) {
		TextChar tc = new AnchorChar(wPage);
		Block block = currentBlock();
		block.addText(tc);
	}
	
	/** Add an end-of-page anchor-character for non-empty pages */
	public void addAnchor(WPage wPage, WToken tok) {
		TextChar tc = new AnchorChar(wPage, tok);
		Block block = currentBlock();
		block.addText(tc);
	}

//	/** Add a new line */
//	@Override
//	public void addLine(Integer ls) {
//		Block block = currentBlock();
//		block.addLine(ls);
//	}

	/** Add a new line */
	/** Render a WtNewLine token */
	public void addLine(WtNewLine tok) {
		Integer ls = tok.getLineSpacing();
		Block block = currentBlock();
		Line pl = block.currentLine();
		block.addLine(ls);
		
		// calculate where the next line will go
		// first add an anchor character
		block.addText(new AnchorChar());
		
		// top and height of new line
		Line l = block.currentLine();
		int nl = block.getNumLines() - 1;
		int height = l.getHeight();
		int ntop = block.getTop(block.getExtraHeight())
				+ nl*(pl.getHeight() + l.getLineSpacing(pl));
		
		// get a fragment from the line to get the left
		Fragment f = block.currentLine().currentFragment();
		int left = f.getLeft(f.getExtraWidth());
		
		// set the parameters of the next line on the token
		tok.setNextLineParams(ntop, height, left);
	}

	/** Get the current text block */
	private Block currentBlock() {
		if (blocks.isEmpty())
			blocks.addLast(new Block());
		return blocks.peekLast();
	}

	/** Add a page */
	/** Render a WtNewPage token */
	public void addPage() {
		renderText();
		raster = WRaster.create(raster);
		fillBackground();
		resetTextRectangle();
	}

	/** Set the (deprecated) message background
	 *  color using a WtColorBackground token. [cb...]
	 * x = Background color (0-9; colorClassic value).
	 * Use sign's default background color if x is null. */
	/** Render a WtColorBackground token */
	public void setColorBackground(WtColorBackground tok) {
		Integer x = tok.getColorBackground();
		if (x == null)
			state.bgPixel = raster.defaultBgPixel;
		else {
			Integer pix = raster.classicColorToPixel(x);
			if (pix == null) {
				renderError(MultiSyntaxError.unsupportedTagValue, tok);
				return;
			}
			state.bgPixel = pix;
		}
		fillBackground();
	}

	/** Set the page background color
	 *  using a WtPageBackground token. [pbZ]
	 * tagval = (0-1 for monochrome1bit),
	 *          (0-255 for monochrome8bit),
	 *          (0-9 for colorClassic & color24bit)
	 *          (r,g,b for color24bit).
	 * Use sign's default background color if tagval is null. */
	/** Render a WtPageBackground token */
	public void setPageBackground(WtPageBackground tok) {
		int[] tagval = tok.getColorTagval();
		Integer pix = raster.tagvalToBgPixel(tagval);
		if (pix == null) {
			renderError(MultiSyntaxError.unsupportedTagValue, tok);
			return;
		}
		state.bgPixel = pix;
		fillBackground();
	}

	/** Fill the page with the current background color */
	private void fillBackground() {
		fillRectangle(1, 1, raster.getWidth(), raster.getHeight(),
			state.bgPixel);
	}

	/** Set the foreground color using
	 *  a WtColorForeground token.   [cfX] or [cfR,G,B]
	 * tvColor = (0-1 for monochrome1bit),
	 *           (0-255 for monochrome8bit),
	 *           (0-9 for colorClassic &  & color24bit).
	 *           (r,g,b for color24bit).
	 * If x is null, use the sign's default foreground color . */
	/** Render a WtColorForeground token */
	public void setColorForeground(WtColorForeground tok) {
		int[] tvColor = tok.getColorTagval();
		Integer pix = raster.tagvalToFgPixel(tvColor);
		if (pix == null) {
			renderError(MultiSyntaxError.unsupportedTagValue, tok);
			return;
		}
		state.fgPixel = pix;
	}

	/** Add a color rectangle using a WtColorRectangle token.
	 *   [crX,Y,W,H,Z] or [crX,Y,W,H,R,G,B]
	 * @param x X pixel position of upper left corner.
	 * @param y Y pixel position of upper left corner.
	 * @param w Width in pixels.
	 * @param h Height in pixels.
	 * tvColor = (0-1 for monochrome1bit),
	 *           (0-255 for monochrome8bit),
	 *           (0-9 for colorClassic &  & color24bit).
	 *           (r,g,b for color24bit). */
	/** Render a WtColorRectangle token */
	public void addColorRectangle(WtColorRectangle tok) {
		int[] tvColor = tok.getColor();
		Integer pix = raster.tagvalToPixel(tvColor, null);
		fillRectangle(tok, pix);
	}

	/** Fill a rectangle with a specified color.
	 * @return True if successful.  False if rectangle was too big. */
	private boolean fillRectangle(int x, int y, int w, int h, int pixel) {
		x--;	/* make X zero-based for raster */
		y--;	/* make Y zero-based for raster */
		boolean bOk = true;
		for (int yy = 0; yy < h; yy++) {
			for (int xx = 0; xx < w; xx++)
				try {
					raster.setPixel(x + xx, y + yy, pixel);
				}
				catch (IndexOutOfBoundsException ex) {
					bOk = false;
				}
		}
		return bOk;
	}

	/** Fill a token's rectangle with a specified color.
	 *  Also records rectangle's coordinates in token. */
	private void fillRectangle(WToken tok, Integer pixel) {
		if (pixel == null) {
			renderError(MultiSyntaxError.unsupportedTagValue, tok);
			return;
		}
		Integer x = tok.getParamX();
		Integer y = tok.getParamY();
		Integer w = tok.getParamW();
		Integer h = tok.getParamH();
		if (w == 0)
			w = raster.getWidth();
		if (h == 0)
			h = raster.getHeight();
		tok.setCoordinates(x-1, y-1, w, h);
		if (!fillRectangle(x, y, w, h, pixel))
			renderError(MultiSyntaxError.unsupportedTagValue, tok);
	}

	/** Set the text rectangle */
	/** Render a WtTextRectangle token */
	public void setTextRectangle(WtTextRectangle tok) {
		int x = tok.getParamX();
		int y = tok.getParamY();
		int w = tok.getParamW();
		int h = tok.getParamH();
		renderText();
		state.trX = x;
		state.trY = y;
		if (w == 0)
			w = raster.getWidth() - (x - 1);
		if (h == 0)
			h = raster.getHeight() - (y - 1);
		state.trW = w;
		state.trH = h;
		tok.setCoordinates(x, y, w, h);
		if (state.trX + state.trW > raster.getWidth() + 1)
			renderError(MultiSyntaxError.unsupportedTagValue, tok);
		if (state.trY + state.trH > raster.getHeight() + 1)
			renderError(MultiSyntaxError.unsupportedTagValue, tok);
	}

	/** Complete the rendering */
	public void complete() {
		renderText();
	}

//	/** Get the syntax error state */
//	public MultiSyntaxError getSyntaxError() {
//		return syntax_err;
//	}

	/** Render the current text rectangle */
	private void renderText() {
//		println("\nrenderText(): blocks = "+blocks.size());
		try {
			for (Block block: blocks)
				block.render();
		}
//		catch (InvalidMsgException e) {
//			syntax_err = MultiSyntaxError.characterNotDefined;
//		}
//		catch (IndexOutOfBoundsException e) {
//			syntax_err = MultiSyntaxError.textTooBig;
//		}
		catch (Exception e) {
			e.printStackTrace();
		}
		blocks.clear();
	}

	//===========================================

	/** Add a graphic */
	/** Render a WtGraphic token */
	public void addGraphic(WtGraphic tok) {
		int g_num = tok.getGraphicNum();
		int x1 = tok.getParamX(1); // 1-based
		int y1 = tok.getParamY(1); // 1-based
		int w, h;
		WRaster wr = graphicCache.getWRaster(g_num);
		if (wr == null) {
			// non-zero width/height so it can be selected in WYSIWYG
			w = 5;
			h = 5;
			renderError(MultiSyntaxError.graphicNotDefined, tok);
		}
		else {
			w = wr.getWidth();
			h = wr.getHeight();
//&&&&			tok.setCoordinates(x1-1, y1-1, w, h);
			if (!renderGraphic(wr, state.fgPixel, x1, y1))
				// No MultiSyntaxError for graphic too big
				renderError(MultiSyntaxError.other, tok);
		}
		tok.setCoordinates(x1-1, y1-1, w, h);
	}

	/** Render a graphic onto the raster.
	 * @param g Graphic to render.
	 * @param fg Foreground color.
	 * @param x X-position on raster (1-based)
	 * @param y Y-position on raster (1-based)
	 * @return True if successful.  False if graphic too big. */
	private boolean renderGraphic(WRaster wg, int fg, int x, int y) {
		x--;
		y--;
		try {
			if (wg instanceof WRasterMono1) {
				// A mono bitmap can be rendered to any sign
				raster.copy(wg, x, y, fg);
			}
			else if (raster instanceof WRasterColor24) {
				// Any bitmap can be converted to RGB
				raster.copy(wg, x, y);
			}
			else if (raster.getColorScheme() == wg.getColorScheme()) {
				// Any bitmap can be copied to same color scheme
				raster.copy(wg, x, y);
			}
		}
		catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

	/** Render a WtPageTime token */
	public void setPageTimes(WtPageTime tok) {
		Integer pt_on = tok.getPageOnTime();
		if (pt_on == null)
			state.pageOn = WState.getIrisDefaultPageOnTime();
		else if ((pt_on < 1) || (pt_on > 255))
			renderError(MultiSyntaxError.unsupportedTagValue, tok);
		else
			state.pageOn = pt_on;
		Integer pt_off = tok.getPageOffTime();
		if (pt_off == null)
			state.pageOff = WState.getIrisDefaultPageOffTime();
		else if ((pt_off < 1) || (pt_off > 255))
			renderError(MultiSyntaxError.unsupportedTagValue, tok);
		else
			state.pageOff = pt_off;
	}

	//===========================================

	/** A block of text to be rendered */
	private class Block {
		private final LinkedList<Line> lines = new LinkedList<Line>();
		private final JustificationPage justp = state.justPage;
		void addText(TextChar tc) {
			Line line = currentLine();
			line.addText(tc);
		}
		void addLine(Integer ls) {
//			Line line = currentLine();
//			if (line.getHeight() == 0) {
//				// The line height can be zero on full-matrix
//				// signs when no text has been specified.
//				// Adding a null TextChar to the line allows
//				// the height to be taken from the current font.
//				line.addText(new TextChar());
//			}
			lines.addLast(new Line(ls));
		}
		Line currentLine() {
			if (lines.isEmpty())
				lines.addLast(new Line(null));
			return lines.peekLast();
		}
		int getNumLines() {
			if (lines.isEmpty())
				lines.addLast(new Line(null));
			return lines.size();
		}
		void render() throws InvalidMsgException {
//			println("Block.render(): lines = "+lines.size());
			int ex = getExtraHeight();
//			println("getExtraHeight()="+ex);
			if (ex < 0) {
				renderError(MultiSyntaxError.textTooBig);
				println("TEXT TOO TALL");
				return;
			}
			int top = getTop(ex);
//			println("getTop()="+top);
			int y = 0;
			Line pline = null;
			for (Line line: lines) {
				y += line.getLineSpacing(pline);
				y += line.getHeight();
//				println("line.render: baseline = "+(top + y)+"");
				line.render(top + y);
				pline = line;
			}
		}
		int getExtraHeight() {
			int h = state.trH / state.charHeight;
			int r = getHeight() / state.charHeight;
			return (h - r) * state.charHeight;
		}
		int floorCharHeight(int ex) {
			return (ex / state.charHeight) * state.charHeight;
		}
		int getTop(int ex) {
			switch (justp) {
			case TOP:
				return state.trY;
			case MIDDLE:
				return state.trY + floorCharHeight(ex / 2);
			case BOTTOM:
				return state.trY + ex;
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
		void addText(TextChar tc) {
			Fragment f = new Fragment();
			Fragment cf = currentFragment();
			if (cf.hasTextAndOrBox) {
				if (f.justl.ordinal() < cf.justl.ordinal()) {
					renderError(MultiSyntaxError.tagConflict);
					//TODO: Figure out how to handle this error
				}
				if (f.justl.ordinal() > cf.justl.ordinal())
					fragments.addLast(f);
			}
			else
				cf.justl = f.justl;
			currentFragment().addText(tc);
		}
		Fragment currentFragment() {
			if (fragments.isEmpty())
				fragments.addLast(new Fragment());
			return fragments.peekLast();
		}
		void render(int base) throws InvalidMsgException {
			println("Line.render(): fragments = "+fragments.size());
			for (Fragment f: fragments)
				f.render(base);
		}
	}

	/** A fragment of text to be rendered */
	private class Fragment {
		private final LinkedList<TextChar> textchars
			= new LinkedList<TextChar>();
		private JustificationLine justl = state.justLine;
		boolean hasTextAndOrBox = false;
		int getHeight() {
			int h = 0;
			for (TextChar tc: textchars)
				h = Math.max(h, tc.getHeight());
			return h;
		}
		int getLineSpacing() {
			int ls = 0;
			for (TextChar tc: textchars)
				ls = Math.max(ls, tc.getLineSpacing());
			return ls;
		}
		void addText(TextChar ch) {
			textchars.add(ch);
			if ((ch instanceof AnchorChar) == false)
				hasTextAndOrBox = true;
		}
		void render(int base) {
			println("Fragment.render(): textchars = "+textchars.size());
			boolean widthErr = false;
			int ex = getExtraWidth();
			if (ex < 0) {
				renderError(MultiSyntaxError.textTooBig);
				println("TEXT TOO WIDE");
				widthErr = true;
//				return;
			}
			int left = getLeft(ex);
			int x = 0;
			TextChar prevCh = null;
			for (TextChar ch: textchars) {
				println("ch.render("+(left + x)+", "+base+")");
				ch.widthErr = widthErr;
				if (ch instanceof AnchorChar)
					try {
						ch.render(left + x, base);
					} catch (InvalidMsgException e) {
						e.printStackTrace();
					}
				else {
					x += ch.getCharSpacing(prevCh);
					try {
						ch.render(left + x, base);
					} catch (InvalidMsgException e) {
						e.printStackTrace();
					}
					x += ch.getWidth();
					prevCh = ch;
				}
			}
		}
		int getExtraWidth() {
			int w = state.trW / state.charWidth;
			int r = getWidth() / state.charWidth;
			return (w - r) * state.charWidth;
		}
		int floorCharWidth(int ex) {
			return (ex / state.charWidth) * state.charWidth;
		}
		int getLeft(int ex) {
			switch (justl) {
			case LEFT:
				return state.trX;
			case CENTER:
				return state.trX + floorCharWidth(ex / 2);
			case RIGHT:
				return state.trX + ex;
			default:
				return 0;
			}
		}
		int getWidth() {
			int w = 0;
			TextChar ps = null;
			StringBuilder sb = new StringBuilder();
			for (TextChar ch: textchars) {
				if (ch instanceof AnchorChar)
					continue;
				if (sb.length() > 0)
					sb.append(",["+ch.getCharSpacing(ps)+"],");
				sb.append(ch.getWidth());
				w += ch.getCharSpacing(ps) + ch.getWidth();
				ps = ch;
			}
			println("getWidth: "+w+" = "+sb.toString());
			return w;
		}
	}

	/** A single char in a fragment of text to be rendered */
	private class TextChar {
		private final WtTextChar tok;
		private final int cp;
		private final int foreground;
		protected final WFont wfont;
		protected final int c_space;
		private final WGlyph wg;
		protected boolean widthErr = false;

		/** normal TextChar */
		private TextChar(WtTextChar tok) {
			this.tok    = tok;
			cp          = tok.getCh();
			wfont       = state.getWFont();
			foreground  = state.fgPixel;
			wg          = wfont.getGlyph(cp);
			c_space     = getCharSpacing();
			
			// save font and color on the token
			tok.setFont(wfont);
			int fgpix = (foreground == WRaster.DEFAULT_FG)
					? raster.defaultFgPixel : foreground;
			tok.setColor(raster.pixelToColor(fgpix));
		}

		/** Null constructor, used for creating AnchorChars */
		private TextChar() {
			this.tok   = new WtTextChar();
			cp         = 0;
			wfont      = state.getWFont();
			foreground = state.fgPixel;
			wg         = null;
			c_space    = getCharSpacing();
		}

		/** Extra constructor, used for creating
		 *  IrisTagBox objects. */
		private TextChar(int ignored) {
			this.tok   = null;
			cp         = 0;
			wfont      = state.getWFont();
			foreground = IRIS_TAG_BOX_COLOR;
			wg         = null;
			c_space    = getCharSpacing();
		}

		int getCharSpacing() {
			if (isCharMatrix())
				return 0;
			if (wfont != null)
				return wfont.getCharSpacing();
			else
				return 1;
		}

		int getCharSpacing(TextChar other) {
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
			assert wfont != null;
			return (wfont != null) ? wfont.getHeight() : 0;
		}

		int getWidth() {
			if (wg == null)
				return 0;
			return wg.getWidth();
		}

		int getLineSpacing() {
			assert wfont != null;
			return (wfont != null) ? wfont.getLineSpacing() : 0;
		}

		void render(int x, int base) throws InvalidMsgException {
			int h = getHeight();
			int y = base - h;
			int w;
			int fg = foreground;
			if (widthErr)
				fg = WRaster.ERROR_PIXEL;
			if (wg == null) {
				renderError(MultiSyntaxError.characterNotDefined, tok);
				w = 0;
			}
			else {
				w = wg.getWidth() + c_space;
				if (!renderGlyph(wg, fg, x, y)) {
					renderError(MultiSyntaxError.textTooBig, tok);
					//TODO: Add code to flag textTooBig if char is outside text rectangle, not just off-sign
				}
			}
			tok.setCoordinates(x, y, w, h);
		}
	}

	static final int IRIS_TAG_BOX_COLOR =
		new DmsColor(110, 163, 120).rgb(); // Oxley green
	
	/** A solid box, rendered in WYSIWYG mode, which
	 *  represents an IRIS-specific message-tag like
	 *  TravelTime, Tolling, etc... */
	private class IrisTagBox extends TextChar {

		/** Wt_Iris child token */
		private final Wt_IrisToken itok;

		/** Construct IrisTagBox */
		private IrisTagBox(Wt_IrisToken itok) {
			super(1);
			this.itok = itok;
			itok.setFont(state.getWFont());
		}

		/** get pixel width of box */
		@Override
		int getWidth() {
			Integer wid = itok.getBoxWidth();
			if ((wid == null) || (wid < 1))
				return 0;
			return wid;
		}

		@Override
		void render(int x, int base) throws InvalidMsgException {
			int w = getWidth();
			int h = getHeight();
			int y = base - h;
			int fg = IRIS_TAG_BOX_COLOR;
			if (widthErr)
				fg = WRaster.ERROR_PIXEL;
			if (w > 0) {
				drawSolidBox(fg, x, y, w, h);
				w += c_space;
			}
			itok.setCoordinates(x, y, w, h);
		}
	}

	/** Render a glyph onto the raster.
	 * @param wg WGlyph to render.
	 * @param fg Foreground color.
	 * @param x X-position on raster (1-based)
	 * @param y Y-position on raster (1-based)
	 * @return True if rendered ok.  False if out of bounds. */
	protected boolean renderGlyph(WGlyph wg, int fg, int x, int y) {
		x--;
		y--;
		try {
			raster.copy(wg, x, y, fg);
		}
		catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

	/** Draw a box onto the raster.
	 * @param fg Box color.
	 * @param x X-position on raster (1-based)
	 * @param y Y-position on raster (1-based)
	 * @param w Width of box
	 * @param h Height of box
	 * @return True if rendered ok.  False if out of bounds. */
	protected boolean drawSolidBox(int fg, int x, int y, int w, int h) {
		x--;
		y--;
		try {
			raster.drawSolidBox(fg, x, y, w, h);
		}
		catch (IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

	/**
	 * @param texttoobig
	 */
	public void renderError(MultiSyntaxError mse) {
//		println("RENDERERROR: "+mse);
		
		// pass the error to the error manager
		saveError(mse, null);
	}

	/**
	 * @param tagconflict
	 * @param tok
	 */
	private void renderError(MultiSyntaxError mse, WToken tok) {
		tok.addErr(mse);
//		println("RENDERERROR: "+mse);
		saveError(mse, tok);
	}

	/** Save the error in the error manager */
	private void saveError(MultiSyntaxError mse, WToken tok) {
		if (errMan != null) {
			errMan.addError(mse, tok);
		}
	}
	
	//===========================================

	/** AnchorChar: Used as a zero-width TextChar
	 *  to find the coordinates of MULTI-tags that
	 *  don't contain WYSIWYG-renderable text, and
	 *  to find the end-of-page text-cursor
	 *  location.*/
	private class AnchorChar extends TextChar {
		private WToken tok = null;
		private int w = 0;
		private WPage wpage = null;

		/** Construct an empty AnchorChar */
		public AnchorChar() {
			
		}
		
		/** construct a generic WToken AnchorChar */
		public AnchorChar(WToken tok) {
			this.tok = tok;
		}

//		/** construct an AnchorChar of width w */
//		public AnchorChar(WToken tok, int w) {
//			this.tok = tok;
//			this.w   = w;
//		}

		/** construct an end-of-page anchor */
		public AnchorChar(WPage p) {
			wpage = p;
		}

		/** construct an end-of-page anchor for non-empty pages */
		public AnchorChar(WPage p, WToken tok) {
			wpage = p;
			this.tok = tok;
		}
		
		@Override
		int getWidth() {
			return w;
		}

		/** Save anchor coordinates to token or page */
		@Override
		void render(int x, int base) throws InvalidMsgException {
			int h = getHeight();
			int y = base - h;
			if (tok != null) {
				if (wpage == null)
					// anchor for non-visible tokens
					tok.setCoordinates(x, y, w, h);
				else {
					// anchor (EOP) for pages with tokens
					int tx = tok.getCoordX();
					int ty = tok.getCoordY();
					int th = tok.getCoordH();
					int tw = tok.getCoordW();
					wpage.setEOP(tx+tw, ty, w, th);
				}
			} else if (wpage != null)
				// anchor (EOP) for pages with no tokens (empty pages)
				wpage.setEOP(x, y, w, h);
		}
	}

	//================================================
	// TBD - tags with indeterminate sign geometries
	//TODO: Figure out how to handle these tokens
	//================================================

//	/** Render a WtTravelTime token */
//	public void addTravelTime(WtTravelTime tok) {
////		String sid;
////		OverLimitMode mode;
////		String o_txt;
//		
//		//TODO:  Figure out how to handle this token...
//	}
//
//	/** Render a WtSpeedAdvisory token */
//	public void addSpeedAdvisory(WtSpeedAdvisory tok) {
//		
//		//TODO:  Figure out how to handle this token...
//	}
//
//	/** Render a WtSlowWarning token */
//	public void addSlowWarning(WtSlowWarning tok) {
////		int    spd  = tok.getSpeed();
////		int    dist = tok.getDistance();
////		String mode = tok.getMode();
//		
//		//TODO:  Figure out how to handle this token...
//	}
//
//	/** Render a WtTolling token */
//	public void addTolling(WtTolling tok) {
////		String   mode  = tok.getMode();
////		String[] zones = tok.getZones();
//		
//		//TODO:  Figure out how to handle this token...
//	}
//
//	/** Render a WtParkingAvail token */
//	public void addParking(WtParkingAvail tok) {
////		String pid   = tok.getParkingID();
////		String l_txt = tok.getParkingLowText();
////		String c_txt = tok.getClosedText();
//		
//		//TODO:  Figure out how to handle this token...
//	}
	
	/** Render a WtFeedMsg token */
	public void addFeed(WtFeedMsg tok) {
//		String fid = tok.getFeedID();
		
		//TODO:  Figure out how to handle this token...
	}

	/** Render a WtLocator token */
	public void addLocator(WtLocator tok) {
//		String code = tok.getCode();
//// idea-code from LocMultiBuilder class
//		if ("rn".equals(code))
//			addRoadway();
//		else if ("rd".equals(code))
//			addRoadDir();
//		else if ("md".equals(code))
//			addModifier();
//		else if ("xn".equals(code))
//			addCrossStreet();
//		else if ("xa".equals(code))
//			addCrossAbbrev();
//		else if ("mi".equals(code))
//			addMiles();

		//TODO:  Figure out how to handle this token...
	}

	//===========================================
	//TODO: Remove test code.
	// Test-rendering in IRIS-server context.
	// (WRrenderer.test() is called from MainServer.main(...).)
	// Saves rendered page images to local folder as .png files.

//	static private int testNo = 1;
	static private String testName;
	static private PrintWriter out;
	static private int msgCnt = 0;

	
	static public void println(String str) {
		if (DEBUG) {
			System.out.println(str);
			if (out != null) {
				out.println(str);
			}
		}
	}
	
//	static public void test2(MultiConfig mcfg, String multiStr) {
//		println("MULTI: "+multiStr);
//		if (!mcfg.isUseable()) {
//			String[] errs = mcfg.getErrors();
//			for (String err : errs) {
//				println("Error: "+err);
//			}
//			return;
//		}
//		String[] warns = mcfg.getWarnings();
//		for (String warn : warns) {
//			println("Warning: "+warn);
//		}
//		WMessage m = new WMessage(multiStr);
////		WRenderer r = new WRenderer(mcfg);
//		m.renderMsg(mcfg);
//		int len = m.getPageCount();
//		WPage pg;
//		BufferedImage bi;
//		WRaster wr;
//		String pngName;
//		for (int i = 1; (i <= len); ++i) {
//			pngName = testName+".pg"+i;
//			pg = m.getPage(i);
//			wr = pg.getRaster();
//			
//			println("-----");
//			pg.dumpTokens(out);
//			wr.dumpGray(out);
//
//			bi = wr.getImage();
//			wr.dumpPng(bi, pngName+".png");
//			bi = wr.getPreviewImage();
//			wr.dumpPng(bi, pngName+"_preview.png");
//		}
//	}

	static public void test3(String testType, String name, MultiConfig mcfg, String multiStr) {
//		if (!"PIXEL_125_WIDE".equals(mcfg.getName()))
//				return;
//		testName = "test"+(testNo++);
		println("==== TESTING: "+testType+"(\""+name+"\", \""+multiStr+"\")");
//		testName = String.format("%s_%03d", name, ++testNo);
		testName = name;
		println("Testname: \""+testName+"\"");
		if (mcfg == null) {
			println("ERROR: Null mcfg");
			return;
		}
		println("Config: "+mcfg.genHashKey2());
		println("MULTI string: "+multiStr);
		if (!mcfg.isUseable()) {
			String[] errs = mcfg.getErrors();
			for (String err : errs) {
				println("Error: "+err);
			}
			return;
		}
		String[] warns = mcfg.getWarnings();
		for (String warn : warns) {
			println("Warning: "+warn);
		}
		WMessage wmsg = new WMessage(multiStr);
//		WRenderer r = new WRenderer(mcfg);
		wmsg.renderMsg(mcfg);
		int len = wmsg.getPageCount();
		println("Page count: "+len);
		WPage pg;
		BufferedImage bi;
		WRaster wr;
		String pngName;
		for (int i = 1; (i <= len); ++i) {
			pngName = testName+".pg"+i;
			pg = wmsg.getPage(i);
			wr = pg.getRaster();
			
			println("----- page "+i+" -----");
			pg.dumpTokens(out);
			wr.dumpGray(out);

			try {
				wr.setWysiwygImageSize(1200, 700);
				bi = wr.getWysiwygImage();
				wr.dumpPng(bi, pngName+".png");
				if (wr.isBlank()) {
					println("POSSIBLE ERROR:  Blank render image; isValid = "+wmsg.isValid());
				}
	//			bi = wr.getPreviewImage();
	//			wr.dumpPng(bi, pngName+"_preview.png");
			} catch (InvalidMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	static public void testSign(String signName, String multiStr) {
		String testType = "testSign";
		try {
			MultiConfig mcfg = MultiConfig.fromSign(signName);
			test3(testType, signName, mcfg, multiStr);
		} catch (Exception e) {
			test3(testType, signName, null, multiStr);
			e.printStackTrace(out);
		}
	}

	static public void testGroup(String groupName, String multiStr) {
		String testType = "testGroup";
		try {
			MultiConfig mcfg = MultiConfig.fromSignGroup(groupName);
			test3(testType, groupName, mcfg, multiStr);
		} catch (Exception e) {
			test3(testType, groupName, null, multiStr);
			e.printStackTrace(out);
		}
	}

	static public void test(DMS sign, String multiStr) {
		String testType = "test<DMS>";
		String name = (sign == null) ? sign.getName() : "(DMS)<null>";
		try {
			MultiConfig mcfg = MultiConfig.from(sign);
			test3(testType, name, mcfg, multiStr);
		} catch (Exception e) {
			test3(testType, name, null, multiStr);
			e.printStackTrace(out);
		}
	}

//	static public void test(SignGroup group, String multiStr) {
//		String testType = "test<SignGroup>";
//		String name = (group != null) ? group.getName() : "(SignGroup)<null>";
//		try {
//			MultiConfig mcfg = MultiConfig.from(group);
//			test3(testType, name, mcfg, multiStr);
//		} catch (Exception e) {
//			test3(testType, name, null, multiStr);
//			e.printStackTrace(out);
//		}
//	}

	static public void test(SignGroup group, String multiStr) {
		String testType = "test<SignGroup>";
		if (group == null) {
			println("ERROR:  Null SignGroup");
			return;
		}
		MultiConfig mcSignGroup = MultiConfig.from(group);
		if (mcSignGroup == null) {
			println("ERROR:  Null MultiConfig for SignGroup "+group.getName());
			return;
		}
		String name1 = String.format("msg%03d.grp%s", msgCnt,
				(group != null) ? group.getName() : "NULL");
		String name2;
		List<MultiConfig> mcaConfigs = mcSignGroup.getConfigList();
		int cfgNum = 1;
		for (MultiConfig mcfg : mcaConfigs) {
			if (mcfg == null) {
				println("ERROR:  Null MultiConfig for config");
				return;
			}
			name2 = String.format("%s.cfg%d", name1, cfgNum++);
			try {
				test3(testType, name2, mcfg, multiStr);
			} catch (Exception e) {
				test3(testType, name2, null, multiStr);
				e.printStackTrace(out);
			}
		}
	}

//	static public void testWysiwygSign(String signName, String multiStr) {
//		MultiConfig mcfg;
////		testName = "test"+(testNo++);
//		String title = "testWysiwygSign(\""+signName+"\", \""+multiStr+"\")";
//		try {
//			mcfg = MultiConfig.fromSign(signName);
//		} catch (TMSException e) {
//			mcfg = null;
//		}
//		if (mcfg == null) {
//			println("Null mcfg for sign: "+signName);
//			return;
//		}
//		println("MultiConfig: "+mcfg.getName());
//		test3(title, mcfg, multiStr);
//	}
//	
//	static public void testWysiwygGroup(SignGroup group, String multiStr) {
//		MultiConfig mcfg;
////		testName = "test"+(testNo++);
//		testName = String.format("%s_%-3d", group.getName(), ++testNo);
//		String title = "testGroup(\""+groupName+"\", \""+multiStr+"\")";
//		println("test: "+group.getName());
//		mcfg = MultiConfig.from(group);
//		if (mcfg == null) {
//			println("Null mcfg for group: "+group.getName());
//			return;
//		}
//		println("MultiConfig: "+mcfg.getName());
//		test3(mcfg, multiStr);
//	}
//	
//	static public void doTestSign(String signName, String multiStr) {
////		testSign(signName, multiStr);
//		testWysiwygSign(signName, multiStr);
//	}
//
//	static public void doTest(SignGroup group, String multiStr) {
////		testGroup(group, multiStr);
//		testWysiwygGroup(group, multiStr);
//	}

	static public void test() {
		long start = System.currentTimeMillis();
		try (PrintWriter tryout = new PrintWriter(new BufferedWriter(new FileWriter("rendertest.txt")))) {
			out = tryout;
//			// 8x3 char matrix
//			testSign("V94W05T", "[jp3]TEST[nl]MSG[nl]3");
//			// large color DMS
//			testSign("V35ES02B", "[g11,1,6][cf255,255,255][tr70,8,80,18][fo3][jl3]OPEN TO ALL[tr70,24,80,18][fo3][jl3]TRAFFIC[g7,70,38]");
//			testWysiwygSign("V35ES02B", "[g11,1,6][cf255,255,255][tr70,8,80,18][fo3][jl3]OPEN TO ALL[tr70,24,80,18][fo3][jl3]TRAFFIC[g7,70,38]");
//			testWysiwygSign("VIMAGO", "[pt10o0][tr1,8,80,32][cf255,208,0][jl3]MERGE[g3,26,35][np][tr1,8,80,32][jl3]MERGE[g3,26,35][g3,38,35][np][tr1,8,80,32][jl3]MERGE[g3,26,35][g3,38,35][g3,50,35]");
//			testGroup("6680_113", "RDWK[nl]BEGINS[nl]JULY 9");
//			testGroup("V94E11", "[cf255,255,255][tr1,1,56,15][fo7]EAST[g16,7,16][tr57,1,214,56][fo13][cf255,208,0]TEST[nl]TEST[nl]TEST");
//			testGroup("V94E11", "[tr57,1,214,56][fo13][cf255,208,0]TEST[nl]TEST[nl]TEST");
//			testGroup("V394W01", "[g21,4,21][tr1,1,53,20][cf255,255,255]SOUTH[tr54,1,217,56][cf255,191,0]DETOUR[nl]FOLLOW[nl]HWY 100 SOUTH");
//			testGroup("V394W00B", "[g11,1,6][cf255,255,255][tr70,8,80,30][fo7][jp3]OPEN TO[nl6]ALL TRAFFIC");
//			testGroup("VDL94W90", "I-94 CLOSED[nl]AT FARGO");
			testGroup("VT94E30", "BLIZZARD WARNING[nl]UNTIL 1 PM");
			
//			Iterator<QuickMessage> itq = QuickMessageHelper.iterator();
//			QuickMessage qm;
//			SignGroup group;
//			while (itq.hasNext()) {
//				qm = itq.next();
//				if (qm == null)
//					continue;
//				group = qm.getSignGroup();
//				if (group == null)
//					continue;
//				String multiStr = qm.getMulti();
//				if ((multiStr == null) || multiStr.isEmpty())
//					continue;
//				println("##############################");
//				test(group, multiStr);
//				msgCnt++;
//			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("FATAL ERROR: TEST TERMINATED");
			return;
		}
		long end = System.currentTimeMillis();
		float sec = (end - start) / 1000F;
		System.err.println("WRenderer.test() done.  "+msgCnt+"msgs, "+sec+" seconds");
	}
}
