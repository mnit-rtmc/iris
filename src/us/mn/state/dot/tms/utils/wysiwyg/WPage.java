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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtColorForeground;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtFont;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtNewLine;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtUnsupportedTag;

/**
 * WePage - WYSIWYG Editor Page Object
 * 
 * A group of WToken objects which, collectively,
 * represents a MULTI page.
 *  
 * @author John L. Stanley - SRF Consulting
 */

public class WPage {

	/** The message this page is part of */
	WMessage wMsg;

	/** Tokens in the page.
	 * Does not include the implicit [np] token between pages. */
	private WTokenList tokenList = new WTokenList();

	/** Lines on page. DOES include the [nl] token at the end of each line. */
	private ArrayList<WTokenList> pageLines;
	
	// End-of-page cursor-coordinates
	int eopX;
	int eopY;
	int eopW;
	int eopH;

	//===========================================
	
	public WPage(WMessage msg) {
		this.wMsg = msg;
	}

	/** Add token to the end of the page
	 * 
	 * Called during initial MULTI string parsing.
	 * 
	 * @param tok WToken to be added.
	 */
	public void addToken(WToken tok) {
		tokenList.add(tok);
		wMsg.setChanged();
	}
	
	/** Add token tok at the specified index */
	public void addToken(int tokIndx, WToken tok) {
		tokenList.add(tokIndx, tok);
		wMsg.setChanged();
	}

	/* Remove (and return) the token at the index provided */
	public WToken removeToken(int tokIndx) {
		wMsg.setChanged();
		return tokenList.remove(tokIndx);
	}

	public Iterator<WToken> tokens() {
		return tokenList.iterator();
	}
	
	public WTokenList getTokenList() {
		return tokenList;
	}
	
	public int getNumTokens() {
		return tokenList.size();
	}

	/** Get the index of the token in this page's token list. */
	public int getTokenIndex(WToken tok) {
		return tokenList.indexOf(tok);
	}

	/** Return an ArrayList of WTokenLists containing the lines of the message
	 *  on this page. Note that the [nl] tags ARE included at the end of each
	 *  array.
	 */
	public ArrayList<WTokenList> getLines() {
		// if the message has changed, re-initialize the lines
		if (pageLines == null || wMsg.isChanged()) {
			// initialize the ArrayList, then iterate through the tokens to fill it
			pageLines = new ArrayList<WTokenList>();
			Iterator<WToken> it = tokens();
			
			// initialize a WTokenList to hold the line
			WTokenList line = new WTokenList();
			while(it.hasNext()) {
				// get the token and add it to the list (even if it's a newline,
				// since that is included at the end)
				WToken t = it.next();
				line.add(t);
				
				// if this is a newline, store the current line and start a new one
				if (t.getClass() == WtNewLine.class) {
					pageLines.add(line);
					line = new WTokenList();
				}
			}
			// add the last line to the list
			pageLines.add(line);
		}
		return pageLines;
	}
	
	/** Get the index of the line on which this token is found.
	 *  @return the index of the line, or -1 if not found */
	public int getLineIndex(WToken tok) {
		// get the list of lines, then try to find the token in one of them
		ArrayList<WTokenList> lines = getLines();
		for (int i = 0; i < lines.size(); ++i) {
			WTokenList line = lines.get(i);
			if (line.contains(tok)) {
				return i;
			}
		}
		return -1;
	}
	
	/** Get the line on which this token found.
	 *  @return the WTokenList representing the line, or null if not found
	 */
	public WTokenList getTokenLine(WToken tok) {
		int li = getLineIndex(tok);
		if (li != -1)
			return getLines().get(li);
		return null;
	}
	
	/** Return the number of lines on the page. */
	public int getNumLines() {
		return getLines().size();
	}
	
	/** Return the closest text token to the point sx, sy (sign coordinates) */
	public WToken findClosestToken(int sx, int sy) {
		// first look through all the tokens on the page to find any that were
		// clicked directly on
		WToken tok = null;
		Iterator<WToken> it = tokens();
		boolean caretAtEnd = false;
		while (it.hasNext()) {
			WToken t = it.next();
			if (t.isInside(sx, sy) && t.isText()) {
				tok = t;
				break;
			}
		}
		
		// if we didn't get anything, find the token that was closest AND on
		// the same line
		if (tok == null) {
			it = tokens();
			double minDist = 999999;
			while (it.hasNext()) {
				WToken t = it.next();
				
				// calculate distance
				double d = t.distance(sx, sy);
				
				// if this token is closer and on the same line then take it
				if (d < minDist && t.sameLine(sy) && t.isText()) {
					tok = t;
					minDist = d;
				}
			}
		}
		return tok;
	}
	
	/** Determine if this is the last token of the page. */
	public boolean isLast(WToken tok) {
		if (tok == tokenList.getLast())
			return true;
		else
			return false;
	}
	
	public boolean isValid() {
		WToken tok;
		Iterator<WToken> it = tokenList.iterator();
		while (it.hasNext()) {
			tok = it.next();
			if (!tok.isValid())
				return false;
			if (tok instanceof WtUnsupportedTag)
				return false;
		}
		return true;
	}
	
	/** Convert page to equivalent MULTI string */
	public String getMulti() {
		StringBuilder sb = new StringBuilder();
		Iterator<WToken> it = tokenList.iterator();
		WToken tok;
		while (it.hasNext()) {
			tok = it.next();
			sb.append(tok.toString());
		}
		return sb.toString();
	}

	/** Execute Multi callbacks for all tokens in page */
	public void doMulti(Multi cb) {
		WToken tok;
		Iterator<WToken> it = tokenList.iterator();
		while (it.hasNext()) {
			tok = it.next();
			tok.doMulti(cb);
		}
	}
	
	/** Execute doRender callbacks for all tokens in a page */
	public void doRender(WRenderer wr) {
		WToken tok = null;
		Iterator<WToken> it = tokenList.iterator();
		while (it.hasNext()) {
			tok = it.next();
//			System.out.println(tok.toString());
			switch (tok.getAnchorLoc()) {
				case BEFORE:
					wr.addAnchor(tok);
					tok.doRender(wr);
					break;
				case NONE:
					tok.doRender(wr);
					break;
				case AFTER:
					tok.doRender(wr);
					wr.addAnchor(tok);
			}
//			if ((tok instanceof WtColorForeground)
//			 || (tok instanceof WtNewLine))
//				wr.addAnchor(tok);
//			tok.doRender(wr);
		}
		// use the token to place the anchor for non-empty pages
		if (tok == null) {
			wr.addAnchor(this);
		} else {
			wr.addAnchor(this, tok);
		}
	}
	
	/** Re-render the page */
	public void renderPage(MultiConfig mcfg) {
		WRenderer wr = new WRenderer(mcfg);
		doRender(wr);
		wr.complete();
		setRaster(wr.getRaster());
	}
	
	//===============================

	/** Rendered page */
	private WRaster raster;

	/** Page-on time in deciseconds */
	private int pageOn;
	
	/** Page-off time in deciseconds */
	private int pageOff;

	/**
	 * @return the rendered raster
	 */
	public WRaster getRaster() {
		return raster;
	}

	/** Set the rendered raster
	 * @param raster
	 */
	public void setRaster(WRaster raster) {
		this.raster = raster;
	}

	/**
	 * @return the pageOn
	 */
	public int getPageOn() {
		return pageOn;
	}

	/**
	 * @return the pageOff
	 */
	public int getPageOff() {
		return pageOff;
	}

	//===========================================
	
	/**
	 * @param out
	 */
	public void dumpTokens(PrintWriter out) {
		WToken tok;
		Iterator<WToken> it = tokenList.iterator();
		String s;
		while (it.hasNext()) {
			tok = it.next();
			switch (tok.getAnchorLoc()) {
				case BEFORE: s = "< "; break;
				case NONE:   s = "  "; break;
				case AFTER:  s = "> "; break;
				default:     s = "  ";
			}
//			s = tok.useAnchor() ? "@ " : "  ";
			out.println(s+tok.toStringVerbose());
			List<WEditorError> errors = tok.getErrorList();
			for (WEditorError err : errors) {
				out.println("\t"+err.toString());
			}
		}
		dumpEOP(out);
	}

	/** Set the end-of-page cursor-coordinates
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void setEOP(int x, int y, int w, int h) {
		eopX = x;
		eopY = y;
		eopW = w;
		eopH = h;
	}
	
	public int getEOPX() {
		return eopX;
	}
	
	public int getEOPY() {
		return eopY;
	}
	
	public int getEOPW() {
		return eopW;
	}
	
	public int getEOPH() {
		return eopH;
	}

	/** Dump the end-of-page cursor-coordinates */
	public void dumpEOP(PrintWriter out) {
		String s = String.format("{%4d, %4d, %4d, %4d} <EOP>",
					eopX, eopY, eopW, eopH);
		out.println(s);
	}
}
