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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtColorRectangle;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextRectangle;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtUnsupportedTag;

/**
 * WePage - WYSIWYG Editor Page Object
 * 
 * A group of WToken objects which, collectively,
 * represents a MULTI page.
 *  
 * @author John L. Stanley - SRF Consulting
 * @author Gordon Parikh - SRF Consulting
 */

public class WPage {

	/** The message this page is part of */
	WMessage wMsg;

	/** Tokens in the page.
	 * Does not include the implicit [np] token between pages. */
	private WTokenList tokenList = new WTokenList();

	/** Text rectangles on this page. Includes the implicit "whole-sign" text
	 *  rectangle (first in the list) along with any explicitly-declared text
	 *  rectangles.
	 */
	private ArrayList<WgTextRect> textRects;
	
	/** Color rectangles on this page. */
	private ArrayList<WgColorRect> colorRects;
	
	// TODO may need to rework how pageLines is handled with textRects  
	
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
	
	/* Remove the token provided, if it exists on the page */
	public void removeToken(WToken tok) {
		boolean removed = tokenList.remove(tok);
		if (removed) {
			wMsg.setChanged();
		}
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
	
	public boolean isEmpty() {
		return tokenList.isEmpty();
	}

	/** Get the index of the token in this page's token list. */
	public int getTokenIndex(WToken tok) {
		return tokenList.indexOf(tok);
	}
	
	/** Create WgTextRect and WgColorRect objects for this page to assist with
	 *  GUI operations. Requires that this page's WRaster has had it's WYSIWYG
	 *  image size set.
	 */
	public void makeGuiRectangles(int threshold) {
		makeGuiTextRects(threshold);
		makeGuiColorRects(threshold);
	}
	
	/** Create WgTextRect objects for this page to assist with GUI operations.
	 *  Requires that this page's WRaster has had it's WYSIWYG image size set.
	 *  WgTextRect objects in this list are in reverse order compared to their
	 *  order in MULTI so that searches happen from front to back.
	 */
	public void makeGuiTextRects(int threshold) {
		// initialize the ArrayList
		textRects = new ArrayList<WgTextRect>();
		
		// start the first text rectangle that covers the whole sign
		WgTextRect tr = new WgTextRect(null);
		
		// now iterate through the tokens to add tokens or make new text
		// rectangles
		for (WToken tok: tokenList) {
			// check if it's a text rectangle tag
			if (tok.isType(WTokenType.textRectangle)) {
				// if it is, we need to store the old text rectangle and
				// start a new one
				textRects.add(tr);
				tr = new WgTextRect((WtTextRectangle) tok);
				tr.initGeom(raster, threshold);
			} else if (!tok.isType(WTokenType.colorRectangle)
					&& !tok.isType(WTokenType.graphic))
				// if it's not a text rectangle, color rectangle, or graphic,
				// just add the token (CR and graphics we'll just skip)
				tr.addToken(tok);
		}
		textRects.add(tr);
		
		// reverse the list
		Collections.reverse(textRects);
	}

	/** Create WgColorRect objects for this page to assist with GUI
	 *  operations. Requires that this page's WRaster has had it's WYSIWYG
	 *  image size set.
	 */
	public void makeGuiColorRects(int threshold) {
		// initialize the ArrayList
		colorRects = new ArrayList<WgColorRect>();
		
		// look through the tokens to find color rectangle tags
		for (WToken tok: tokenList) {
			if (tok.isType(WTokenType.colorRectangle)) {
				// if we get one, make a new WColorRect object for it
				WgColorRect cr = new WgColorRect((WtColorRectangle) tok);
				cr.initGeom(raster, threshold);
				colorRects.add(cr);
			}
		}
		// reverse the list
		Collections.reverse(colorRects);
	}
	
	/** Return an ArrayList of WgTextRects that represent the text rectangles
	 *  on this page, used for GUI operations. WgTextRects must be initialized
	 *  first with the makeGuiTextRects method.
	 */
	public ArrayList<WgTextRect> getTextRects() {
		return textRects;
	}

	/** Return an ArrayList of WgColorRects that represent the text rectangles
	 *  on this page, used for GUI operations. WgColorRects must be
	 *  initialized first with the makeGuiColorRects method.
	 */
	public ArrayList<WgColorRect> getColorRects() {
		return colorRects;
	}
	
	/** Return an ArrayList of WTokenLists containing the lines of the message
	 *  on this page. Note that the [nl] tags ARE included at the end of each
	 *  array.
	 */
	public ArrayList<WTokenList> getLines() {
		return tokenList.getLines();
	}
	
	/** Get the index of the line on which this token is found.
	 *  @return the index of the line, or -1 if not found */
	public int getLineIndex(WToken tok) {
		return tokenList.getLineIndex(tok);
	}
	
	/** Get the line on which this token found.
	 *  @return the WTokenList representing the line, or null if not found
	 */
	public WTokenList getTokenLine(WToken tok) {
		return tokenList.getTokenLine(tok);
	}
	
	/** Return the number of lines on the page. */
	public int getNumLines() {
		return tokenList.getNumLines();
	}
	
	/** Return the closest text token to the point sx, sy (sign coordinates).
	 *  If includeNonPrint is false, only printable text characters (including
	 *  spaces and newlines) are included, otherwise all tokens that aren't
	 *  text/color rectangles or graphics are included.
	 */
	public WToken findClosestTextToken(WPoint p, boolean includeNonPrint) {
		// find the closest text token in the list of tokens on this page
		return tokenList.findClosestTextToken(p, includeNonPrint);
	}
	
	public WTokenList getTokensOfType(WTokenType tokType) {
		return tokenList.getTokensOfType(tokType);
	}
	
	public WToken findClosestTokenOfType(WPoint p, WTokenType tokType) {
		return tokenList.findClosestTokenOfType(p, tokType);
	}
	
	/** Return the next token with a type that matches any of the types
	 *  provided, starting the search at si.
	 */
	public WToken findNextTokenOfType(int si, WTokenType... tokTypes) {
		return tokenList.findNextTokenOfType(si, tokTypes);
	}

	/** Return the previous token with a type that matches any of the types
	 *  provided, starting the search at si.
	 */
	public WToken findPrevTokenOfType(int si, WTokenType... tokTypes) {
		return tokenList.findPrevTokenOfType(si, tokTypes);
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
					break;
				case CONDITIONAL:
					// only add an anchor if this token is blank
					tok.doRender(wr);
					if (tok.isBlank())
						wr.addAnchor(tok);
			}
		}
		// use the token to place the anchor for non-empty pages
		if (tok == null) {
			wr.addAnchor(this);
		} else {
			wr.addAnchor(this, tok);
		}
		// update page timing info
		WState state = wr.getState();
		setPageTiming(state.pageOn, state.pageOff);
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

	/** Set the width/height of the WYSIWYG image rendered from this page. */
	public void setWysiwygImageSize(int pixWidth, int pixHeight)
			throws InvalidMsgException {
		if (raster == null)
			throw new InvalidMsgException("Page not rendered");
		raster.setWysiwygImageSize(pixWidth, pixHeight);
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

	/**
	 * @param pageOn2
	 * @param pageOff2
	 */
	public void setPageTiming(int pageOn2, int pageOff2) {
		pageOn = pageOn2;
		pageOff = pageOff2;
	}

	//===============================

	/** Test for a specific token-type anywhere on the page.
	 * @param type WTokenType to look for
	 * @return true if a token of that type was found,
	 *  false if it was not.
	 */
	public boolean containsAny(WTokenType type) {
		WToken t;
		Iterator<WToken> it = tokenList.iterator();
		while (it.hasNext()) {
			t = it.next();
			if (t.isType(type))
				return true;
		}
		return false;
	}

	/** Remove all tokens of a specific token-type anywhere on the page.
	 * @param type WTokenType to look for
	 * @return true if any tokens of that type were removed,
	 *  false if none were found.
	 */
	public boolean removeAll(WTokenType type) {
		boolean removedOneOrMore = false;
		WToken t;
		Iterator<WToken> it = tokenList.iterator();
		while (it.hasNext()) {
			t = it.next();
			if (t.isType(type)) {
				it.remove();
				removedOneOrMore = true;
			}
		}
		return removedOneOrMore;
	}
}
