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
import java.util.LinkedList;
import java.util.List;

import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.wysiwyg.WToken.GetLinesCategory;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtFont;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtNewPage;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextChar;

/** WYSIWYG-editor Message object
 * 
 * Contains a series of WPage objects and
 * methods for manipulating them.
 * 
 * Also contains methods for parsing and
 * building MULTI strings;
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WMessage {

	public WMessage() {
	}

	public WMessage(String multiStr) {
		parseMulti(multiStr);
	}

	//===========================================
	
	/** Get iterator for all tokens in message. */
	public Iterator<WToken> tokens() {
		List<WToken> list = new LinkedList<WToken>();
		WPage page;
		WToken tok;
		final WToken np = new WtNewPage();
		Iterator<WPage> itp = pagelist.iterator();
		while (itp.hasNext()) {
			page = itp.next();
			Iterator<WToken> itt = page.tokens();
			while (itt.hasNext()) {
				tok = itt.next();
				list.add(tok);
			}
			if (itp.hasNext())
				list.add(np);
		}
		return list.iterator();
	}

	//===========================================
	
	/* Has message changed since it was last rendered? */
	private boolean bChanged;

	public void setChanged() {
		bChanged = true;
	}
	
	public boolean isChanged() {
		return bChanged;
	}
	
	//===========================================
	
	private List<WPage> pagelist = null;
	
	public int getPageCount() {
		return pagelist.size();		
	}

	/** Get WPage from message.
	 * @param pageNo runs from 1 to getPageCount()
	 * @throws IndexOutOfBoundsException if page doesn't exist */
	public WPage getPage(int pageNo) {
		return pagelist.get(pageNo-1);
	}

	public Iterator<WPage> pages() {
		return pagelist.iterator();
	}

	public WPage startPage1() {
		pagelist = new ArrayList<WPage>();
		return newPage();
	}

	public WPage newPage() {
		WPage np = new WPage(this);
		pagelist.add(np);
		bChanged = true;
		return np;
	}
	
	/* Remove (and return) page number provided */
	public WPage removePage(int pageNo) {
		bChanged = true;
		return pagelist.remove(pageNo);
	}

	public void addPage(int pageNo, WPage page) {
		bChanged = true;
		pagelist.add(pageNo, page);
	}
	
	//===========================================
	
	/** Load MULTI string into a WMessage */
	public void parseMulti(String multiStr) {
		startPage1();
		MultiString ms = new MultiString(multiStr);
		WMultiParser parser = new WMultiParser(this);
		ms.parse(parser);
		bChanged = true;
	}
	
	/** Create a MULTI string from a WMessage */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<WPage> it = pagelist.iterator();
		WPage page;
		while (it.hasNext()) {
			page = it.next();
			sb.append(page.getMulti());
			if (it.hasNext())
				sb.append("[np]");
		}
		return sb.toString();
	}
	
	/** Execute Multi callbacks for all tokens in a WMessage */
	public void doMulti(Multi multi) {
		Iterator<WPage> it = pagelist.iterator();
		WPage page;
		while (it.hasNext()) {
			page = it.next();
			page.doMulti(multi);
			if (it.hasNext())
				multi.addPage();
		}
	}

	/** Execute doRender callbacks for all tokens in a message */
	public void doRender(WRenderer wr) {
		Iterator<WPage> it = pagelist.iterator();
		WPage page;
		while (it.hasNext()) {
			page = it.next();
			page.doRender(wr);
			if (it.hasNext())
				wr.renderNewPage();
		}
	}

	//===========================================
	// Implement various MultiString
	// methods for compatibility.

	public boolean isValid() {
		Iterator<WPage> it = pagelist.iterator();
		WPage page;
		while (it.hasNext()) {
			page = it.next();
			if (!page.isValid())
				return false;
		}
		return true;
	}

	/** Get a MULTI string as text only (tags stripped) */
	public String asText() {
		StringBuilder sb = new StringBuilder();
		boolean betweenWords = false;
		Iterator<WToken> it = tokens();
		WToken tok;
		while (it.hasNext()) {
			tok = it.next();
			if (!tok.isTag()) {
				if ((tok instanceof WtTextChar)
				 && ((WtTextChar)tok).isBlank()) {
					betweenWords = true;
				}
				else {
					if (betweenWords)
						sb.append(' ');
					betweenWords = false;
					sb.append(tok.toString());
				}
			}
			else
				betweenWords = true;
		}
		return sb.toString();
	}

	/** Return a value indicating if the message is single or multi-page.
	 * @return True if the message contains a single page else false
	 * for multi-page. */
	public boolean singlePage() {
		return getNumPages() <= 1;
	}

	/** Get the number of pages in the WMessage */
	public int getNumPages() {
		return pagelist.size();
	}

	/** Get MULTI string for specified page */
	public String getPageMulti(int p) {
		if (p >= 0 && p < pagelist.size())
			return pagelist.get(p).getMulti();
		else
			return "";
	}

	/** Does this message NOT do anything visible on the sign? */
	public boolean isBlank() {
		Iterator<WToken> it = tokens();
		while (it.hasNext())
			if (it.next().isBlank() == false)
				return false;
		return true;
	}

	/** Normalize a MULTI string.
	 * @return A MULTI string containing only valid characters and tags. */
	public String normalize() {
		StringBuilder sb = new StringBuilder();
		Iterator<WToken> it = tokens();
		WToken tok;
		while (it.hasNext()) {
			tok = it.next();
			if (tok.isValid())
				sb.append(tok.toString());
		}
		return sb.toString();
	}

	/** Normalize a single line MULTI string.
	 * @return The normalized MULTI string. */
	public String normalizeLine() {
		StringBuilder sb = new StringBuilder();
		Iterator<WToken> it = tokens();
		WToken tok;
		while (it.hasNext()) {
			tok = it.next();
			if (tok.isNormalizeLine())
				sb.append(tok.toString());
		}
		return sb.toString();
	}

	/** Normalize a single line MULTI string
	 *  using different normalization criteria.
	 * @return The normalized MULTI string. */
	public String normalizeLine2() {
		StringBuilder sb = new StringBuilder();
		Iterator<WToken> it = tokens();
		WToken tok;
		while (it.hasNext()) {
			tok = it.next();
			if (tok.isNormalizeLine2())
				sb.append(tok.toString());
		}
		return sb.toString();
	}

	/** Test if the WMessage is equal to another WMessage */
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o != null) {
			String ms = normalize();
			String oms = new WMessage(o.toString()).normalize();
			return ms.equals(oms);
		}
		return false;
	}

	/** Get message lines as an array of strings (with tags).
	 * Every n_lines elements in the returned array represent one page.
	 * @param n_lines Number of lines per page.
	 * @return A string array containing text for each line. */
	public String[] getLines(int n_lines) {
		if (n_lines <= 0)
			return new String[0];
		int pgcnt = getPageCount();
		int maxlines = n_lines * pgcnt;
		String[] lines = new String[maxlines];
		for (int i = 0; i < maxlines; i++)
			lines[i] = "";
		int ln = 0;   // line in lines[] array
		StringBuilder sb = new StringBuilder();
		GetLinesCategory glc = GetLinesCategory.PAGEBREAK;
		Iterator<WToken> it = tokens();
		WToken tok;
		for (int pg = 0; (pg < pgcnt); ++pg) {
			ln = pg * n_lines;
			for (int pgln = 0; (pgln < n_lines); ++pgln) {
				while (it.hasNext()) {
					tok = it.next();
					glc = tok.getLinesCategory();
					if (glc == GetLinesCategory.IGNORE)
						continue;
					sb.append(tok.toString());
					if (glc == GetLinesCategory.INCLUDE)
						continue;
					break;
				}
				// hit LINEBREAK or PAGEBREAK
				lines[ln++] = sb.toString();
				sb = new StringBuilder();
				if (glc == GetLinesCategory.PAGEBREAK)
					break;
			}
			if (glc != GetLinesCategory.PAGEBREAK) {
				// hit max lines per page, skip to next page break
				while (it.hasNext()) {
					tok = it.next();
					if (tok.getLinesCategory() == GetLinesCategory.PAGEBREAK)
						break;
				}
			}
		}
		return lines;
	}

	/** Get an array of font numbers.
	 * @param f_num Default font number, one based.
	 * @return An array of font numbers for each page of the message. */
	public int[] getFonts(final int f_num) {
		if (f_num < 1 || f_num > 255)
			return new int[0];
		int[] fonts = new int[getPageCount()];
		Iterator<WToken> it = tokens();
		WToken tok;
		int page = 0;
		Integer font_num = f_num;
		while (it.hasNext()) {
			tok = it.next();
			if (tok instanceof WtFont) {
				font_num = ((WtFont)tok).getFontNum();
				if (font_num == null)
					font_num = f_num;
				fonts[page] = font_num;
			}
			else if (tok instanceof WtNewPage) {
				++page;
				fonts[page] = font_num;
			}
			else if (tok instanceof WtTextChar) {
				fonts[page] = font_num;
			}
		}
		return fonts;
	}
	
	//===========================================
	
	List<WMsgWatcher> watchers = new ArrayList<WMsgWatcher>();

	public void addWatcher(WMsgWatcher watcher) {
		watchers.add(watcher);
	}

	public void removeWatcher(WMsgWatcher watcher) {
		watchers.remove(watcher);
	}

	/** Re-render page and then tell watchers */
	public void pageChanged(int pageNo) {
		Iterator<WMsgWatcher> itw = watchers.iterator();
		WMsgWatcher watcher;
		while (itw.hasNext()) {
			watcher = itw.next();
			watcher.pageChanged(this, pageNo);
		}
	}

	/** Render all pages in message.
	 * Updates the raster image in all WPage(s) */
	public void renderMsg(MultiConfig mcfg, WEditorErrorManager errMan) {
		WRenderer wr = new WRenderer(mcfg, errMan);
		for (WPage pg: pagelist) {
			pg.doRender(wr);
			wr.complete();
			pg.setRaster(wr.getRaster());
			wr.renderNewPage();
		}
	}
	
	/** Set the WYSIWYG image size on each page's raster. */
	public void setWysiwygImageSize(int pixWidth, int pixHeight)
			throws InvalidMsgException {
		for (WPage pg: pagelist)
			pg.setWysiwygImageSize(pixWidth, pixHeight);
	}
	
	/** Render all pages in message without any error management
	 * Updates the raster image in all WPage(s) */
	public void renderMsg(MultiConfig mcfg) {
		renderMsg(mcfg, null);
	}
}
