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

import us.mn.state.dot.tms.utils.wysiwyg.token.WtColorForeground;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtFont;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtJustLine;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtJustPage;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtNewLine;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtPageBackground;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextRectangle;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.Multi.JustificationPage;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Convenience class for grouping text in a text rectangle in the WYSIWYG DMS 
 * Message Editor. Not to be confused with the text rectangle MULTI tag token
 * WtTextRectangle.
 *
 * @author Gordon Parikh - SRF Consulting
 */

public class WTextRect {
	
	/** Text rectangle tag that starts this text rectangle. If null, the text
	 *  rectangle is the entire sign (as per NTCIP 1203).
	 */
	private WtTextRectangle textRectTok;
	
	/** The list of tokens that are contained within this text rectangle. */
	private WTokenList tokenList;
	
	public WTextRect(WtTextRectangle trTok) {
		textRectTok = trTok;
		tokenList = new WTokenList();
	}
	
	/** Add a token contained within this text rectangle. */
	public void addToken(WToken tok) {
		tokenList.add(tok);
	}
	
	/** Return the text rectangle tag associated with this text rectangle. */
	public WtTextRectangle getTextRextToken() {
		return textRectTok;
	}
}
