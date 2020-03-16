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
import java.util.Iterator;
import java.util.List;


/**
 * WTokenList - List of WYSIWYG Tokens
 * 
 * @author John L. Stanley - SRF Consulting
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
	
	/** Slice the list */
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
}
