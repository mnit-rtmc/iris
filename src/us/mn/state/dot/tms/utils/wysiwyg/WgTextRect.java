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
 * Class for GUI operations with text rectangles in the WYSIWYG DMS Message
 * Editor. Not to be confused with the text rectangle MULTI tag token
 * WtTextRectangle.
 *
 * @author Gordon Parikh - SRF Consulting
 */

public class WgTextRect extends WgRectangle {
	
	/** The list of tokens that are contained within this text rectangle. */
	private WTokenList tokenList;
	
	public WgTextRect(WtTextRectangle trTok) {
		super(trTok);
		tokenList = new WTokenList();
	}
	
	/** Add a token contained within this text rectangle. */
	public void addToken(WToken tok) {
		tokenList.add(tok);
	}
	
	/** Return the text rectangle tag associated with this text rectangle. If
	 *  null, the text rectangle is the entire sign (as per NTCIP 1203).
	 */
	public WtTextRectangle getTextRectToken() {
		return (WtTextRectangle) rt;
	}
	
	/** Return the last token associated with this text rectangle, either the
	 *  text rectangle token itself or the last token inside of it. If this
	 *  is the whole-sign text rectangle, null is returned.
	 */
	@Override
	public WToken getLastToken() {
		if (rt != null) {
			if (!tokenList.isEmpty())
				return tokenList.getLast();
		}
		return rt;
	}
	
	/** Return the list of tokens inside this text rectangle. */
	public WTokenList getTokenList() {
		return tokenList;
	}
	
	/** Return whether or not the text rectangle is the implicit "whole-sign"
	 *  text rectangle.
	 */
	public boolean isWholeSign() {
		return rt == null;
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
	
	public WToken findClosestTokenOfType(WPoint p, WTokenType tokType) {
		return tokenList.findClosestTokenOfType(p, tokType);
	}
	
}
