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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * WTokenList - List of WYSIWYG Tokens
 * 
 * @author John L. Stanley - SRF Consulting
 * @author Gordon Parikh - SRF Consulting
 */

@SuppressWarnings("serial")
public class WTokenList extends ArrayList<WToken> {

//	/**
//	 * 
//	 */
//	public WTokenList() {
//		// TODO Auto-generated constructor stub
//	}

	/** Print-out all tokens in the list */
	public void print() {
		for (WToken tok : this) {
			System.out.println(tok.toString());
		}
	}
	
	/** Print-out (verbose) all tokens in the list */
	public void printVerbose() {
		for (WToken tok : this) {
			System.out.println(tok.toStringVerbose());
		}
	}

	/** Move all tokens in a list */
	public void move(int offsetX, int offsetY) {
		for (WToken tok : this) {
			tok.moveTok(offsetX, offsetY);
		}
	}
	
	/** Slice the list. If indeces are out of bounds, they are truncated to
	 *  the bounds of the list. 
	 */
	public WTokenList slice(int fromIndex, int toIndex) {
		// make a new WTokenList then add items from the sublist
		WTokenList tl = new WTokenList();
		List<WToken> sl = subList(fromIndex, toIndex);
		tl.addAll(0, sl);
		return tl;
	}
	
	/** Get the last token from the list. This is just a shortcut method for
	 *  list.get(list.size()-1).
	 */
	public WToken getLast() {
		return get(size()-1);
	}
	
	/** Remove and return the last token from the list. This is a shortcut
	 *  method for list.remove(list.size()-1). */
	public WToken removeLast() {
		return remove(size()-1);
	}
	
	/** Return a copy of the list with tokens in reverse order. */
	public WTokenList reversed() {
		WTokenList rev = new WTokenList();
		rev.addAll(this);
		Collections.reverse(rev);
		return rev;
	}
	
	/** Return the closest text token to the point sx, sy (sign coordinates).
	 *  If includeNonPrint is false, only printable text characters (including
	 *  spaces and newlines) are included, otherwise all tokens that aren't
	 *  text/color rectangles or graphics are included.
	 */
	public WToken findClosestTextToken(WPoint p, boolean includeNonPrint) {
		// first look through all the tokens on the page to find any that were
		// clicked directly on
		WToken tok = null;
		for (WToken t: this) {
			if (t.isInside(p) && t.isText()
					&& (includeNonPrint || t.isPrintableText())) {
				tok = t;
				break;
			}
		}
		
		// if we didn't get anything, find the token that was closest AND on
		// the same line
		if (tok == null) {
			double minDist = 999999;
			for (WToken t: this) {
				// calculate distance
				double d = t.distance(p);
				
				// if this token is closer and on the same line then take it
				if (d < minDist && t.sameLine(p.getSignY()) && t.isText()
						&& (includeNonPrint || t.isPrintableText())) {
					tok = t;
					minDist = d;
				}
			}
		}
		return tok;
	}
	
	public WToken findClosestTokenOfType(WPoint p, WTokenType tokType) {
		// first look through all the tokens on the page to find any that were
		// clicked directly on
		WToken tok = null;
		for (WToken t: this) {
			if (t.isInside(p) && t.isType(tokType)) {
				tok = t;
				break;
			}
		}
		
		// if we didn't get anything, find the token that was closest AND on
		// the same line
		if (tok == null) {
			double minDist = 999999;
			for (WToken t: this) {
				// calculate distance
				double d = t.distance(p);
				
				// if this token is closer and on the same line then take it
				if (d < minDist && t.sameLine(p.getSignY()) && t.isType(tokType)) {
					tok = t;
					minDist = d;
				}
			}
		}
		return tok;
	}
}
