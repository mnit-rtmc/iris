/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextRectangle;

/**
 * Class for working with text rectangles in the WYSIWYG DMS Message Editor.
 * Not to be confused with the text rectangle MULTI tag token WtTextRectangle.
 *
 * @author Gordon Parikh - SRF Consulting
 */

public class WTextRect extends WRectangle {
	
	/** Text rectangle tag that starts this text rectangle. If null, the text
	 *  rectangle is the entire sign (as per NTCIP 1203).
	 */
//	protected WtTextRectangle rectTok;
	
	/** The list of tokens that are contained within this text rectangle. */
	private WTokenList tokenList;
	
	public WTextRect(WtTextRectangle trTok) {
		super(trTok);
		tokenList = new WTokenList();
	}
	
	/** Add a token contained within this text rectangle. */
	public void addToken(WToken tok) {
		tokenList.add(tok);
	}
	
	/** Return the text rectangle tag associated with this text rectangle. */
	public WtTextRectangle getTextRectToken() {
		return (WtTextRectangle) rectTok;
	}
	
	/** Return the list of tokens inside this text rectangle. */
	public WTokenList getTokenList() {
		return tokenList;
	}
	
	/** Return whether or not the text rectangle is the implicit "whole-sign"
	 *  text rectangle.
	 */
	public boolean isWholeSign() {
		return rectTok == null;
	}
	
	// TODO take these next two methods and put them in a parent class
	// (WRect?) so we can do the same thing with color rectangles
	
	/** Return the closest text token to the point sx, sy (sign coordinates).
	 *  If includeNonPrint is false, only printable text characters (including
	 *  spaces and newlines) are included, otherwise all tokens that aren't
	 *  text/color rectangles or graphics are included.
	 */
	public WToken findClosestTextToken(WPoint p, boolean includeNonPrint) {
		// find the closest text token in the list of tokens on this page
		return tokenList.findClosestTextToken(p, includeNonPrint);
	}
	
	public WToken findClosestTokenOfType(WPoint p, WTokenType tokType) {
		return tokenList.findClosestTokenOfType(p, tokType);
	}
	
	public String toString() {
		if (rectTok != null)
			return rectTok.toString();
		return "null";
	}
}
