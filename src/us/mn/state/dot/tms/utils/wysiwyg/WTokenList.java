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

import us.mn.state.dot.tms.utils.wysiwyg.token.WtNewLine;
import us.mn.state.dot.tms.utils.wysiwyg.token.Wt_IrisToken;


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

	/** Move all tokens in a list by a specified amount. */
	public void move(int offsetX, int offsetY) {
		for (WToken tok : this)
			tok.moveTok(offsetX, offsetY);
	}
	
	/** Slice the list. */
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
	
	/** Reverse the list in place. */
	public void reverse() {
		Collections.reverse(this);
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
	
	/** Get the list of all tokens of type tokType in this list. */
	public WTokenList getTokensOfType(WTokenType tokType) {
		WTokenList typeList = new WTokenList();
		for (WToken tok: this) {
			if (tok.isType(tokType))
				typeList.add(tok);
		}
		return typeList;
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
	
	/** Lines of text in this list */
	private ArrayList<WTokenList> listLines;
	
	/** Return an ArrayList of WTokenLists containing the lines of the message
	 *  in this list. Note that the [nl] tags ARE included at the end of each
	 *  array.
	 */
	public ArrayList<WTokenList> getLines() {
		// if the message has changed, re-initialize the lines
		if (listLines == null) {
			// initialize the ArrayList, then iterate through the tokens to fill it
			listLines = new ArrayList<WTokenList>();
			// initialize a WTokenList to hold the line
			WTokenList l = new WTokenList();
			
			for (WToken t: this) {
				l.add(t);
				
				if (t.isType(WTokenType.newLine)) {
					listLines.add(l);
					l = new WTokenList();
				}
			}
			
			// add the last line to the list
			listLines.add(l);
		}
		return listLines;
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
	
	/** Words in this list */
	private ArrayList<WTokenList> listWords;
	
	/** Return an ArrayList of WTokenLists containing the words of the message
	 *  in this list. Note that spaces are included at the end of each array.
	 *  Non-text tags (including newlines) are also considered "words" by
	 *  themselves.
	 */
	public ArrayList<WTokenList> getWords() {
		// if the message has changed, re-initialize the lines
		if (listWords == null) {
			// initialize the ArrayList, then iterate through the tokens to
			// fill it
			listWords = new ArrayList<WTokenList>();
			// initialize a WTokenList to hold the line
			WTokenList l = new WTokenList();
			
			for (WToken t: this) {
				if (t.isPrintableText() && !t.isType(WTokenType.newLine)) {
					// collect letters and spaces into the word
					l.add(t);
					
					// start a new word if we hit a space
					if (t.toString().equals(" ")) {
						listWords.add(l);
						l = new WTokenList();
					}
				} else {
					// any other type of tag is it's own word
					if (!l.isEmpty()) {
						// save any word we had been collecting (without this
						// token)
						listWords.add(l);
						l = new WTokenList();
					}
					// add this token as its own word
					l.add(t);
					listWords.add(l);
					l = new WTokenList();
				}
				
			}
			
			// add the last word to the list
			listWords.add(l);
		}
		return listWords;
	}

	/** Get the index of the word in which this token is found.
	 *  @return the index of the word, or -1 if not found */
	public int getWordIndex(WToken tok) {
		// get the list of words, then try to find the token in one of them
		ArrayList<WTokenList> words = getWords();
		for (int i = 0; i < words.size(); ++i) {
			WTokenList word = words.get(i);
			if (word.contains(tok)) {
				return i;
			}
		}
		return -1;
	}

	/** Get the index of the word represented by the token list provided.
	 *  @return the index of the word, or -1 if not found */
	public int getWordIndex(WTokenList word) {
		// get the list of words, then try to find the token in one of them
		ArrayList<WTokenList> words = getWords();
		return words.indexOf(word);
	}
	
	/** Get the line on which this token found.
	 *  @return the WTokenList representing the line, or null if not found
	 */
	public WTokenList getTokenWord(WToken tok) {
		int wi = getWordIndex(tok);
		if (wi != -1)
			return getWords().get(wi);
		return null;
	}
	
	/** Return the number of lines on the page. */
	public int getNumWords() {
		return getWords().size();
	}
	
	/** Return the next token with a type that matches any of the types
	 *  provided, starting the search at si.
	 */
	public WToken findNextTokenOfType(int si, WTokenType... tokTypes) {
		for (int i = si; i < size(); ++i) {
			WToken tok = get(i);
			for (WTokenType tt: tokTypes) {
				if (tok.isType(tt))
					return tok;
			}
		}
		return null;
	}
	
	/** Return the next printable text token in the list after index si 
	 *  (inclusive - if the token at si is printable text, it is returned).
	 *  If no tokens are found, null is returned. Includes newlines if 
	 *  includeNewLine is true, otherwise only WtTextChar tokens are included.
	 */
	public WToken findNextTextToken(int si, boolean includeNewLine,
			boolean includeIrisTags) {
		for (int i = si; i < size(); ++i) {
			WToken tok = get(i);
			
			if ((tok.isPrintableText()
					&& (includeNewLine || tok.isType(WTokenType.textChar)))
					|| (tok instanceof Wt_IrisToken && includeIrisTags
							&& !tok.isBlank()))
				return tok;
		}
		return null;
	}
	
	/** Find the first printable text token in the list. */
	public WToken findFirstTextToken(boolean includeNewLine,
			boolean includeIrisTags) {
		return findNextTextToken(0, includeNewLine, includeIrisTags);
	}
	
	/** Return the previous token with a type that matches any of the types
	 *  provided, starting the search at si.
	 */
	public WToken findPrevTokenOfType(int si, WTokenType... tokTypes) {
		if (si >= size())
			// clip to the list size
			si = size() - 1;
		for (int i = si; i >= 0; --i) {
			WToken tok = get(i);
			for (WTokenType tt: tokTypes) {
				if (tok.isType(tt))
					return tok;
			}
		}
		return null;
	}
	/** Return the previous printable text token in the list after index si
	 *  (inclusive - if the token at si is printable text, it is returned).
	 *  If no tokens are found, null is returned.
	 */
	public WToken findPrevTextToken(int si, boolean includeNewLine,
			boolean includeIrisTags) {
		if (si >= size())
			si = size() - 1;
		for (int i = si; i >= 0; --i) {
			WToken tok = get(i);
			
			if ((tok.isPrintableText()
					&& (includeNewLine || tok.isType(WTokenType.textChar)))
					|| (tok instanceof Wt_IrisToken && includeIrisTags
							&& !tok.isBlank()))
				return tok;
		}
		return null;
	}

	/** Find the last printable text token in the list. */
	public WToken findLastTextToken(boolean includeNewLine,
			boolean includeIrisTags) {
		return findPrevTextToken(size()-1, includeNewLine, includeIrisTags);
	}
	
}
