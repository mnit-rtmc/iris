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
import java.util.Iterator;
import java.util.List;

import us.mn.state.dot.tms.utils.Multi;
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

	// End-of-page cursor-coordinates
	int eopX;
	int eopY;
	int eopW;
	int eopH;

	//===========================================
	
	public WPage(WMessage msg) {
		this.wMsg = msg;
	}

	/** Add token to page
	 * 
	 * Called during initial MULTI string parsing.
	 * 
	 * @param tok WToken to be added.
	 */
	public void addToken(WToken tok) {
		tokenList.add(tok);
		wMsg.setChanged();
	}

	public Iterator<WToken> tokens() {
		return tokenList.iterator();
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
		WToken tok;
		Iterator<WToken> it = tokenList.iterator();
		while (it.hasNext()) {
			tok = it.next();
			System.out.println(tok.toString());
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
		wr.addAnchor(this);
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

	/** Dump the end-of-page cursor-coordinates */
	public void dumpEOP(PrintWriter out) {
		String s = String.format("{%4d, %4d, %4d, %4d} <EOP>",
					eopX, eopY, eopW, eopH);
		out.println(s);
	}
}
