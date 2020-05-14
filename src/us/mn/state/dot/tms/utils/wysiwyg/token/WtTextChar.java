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

package us.mn.state.dot.tms.utils.wysiwyg.token;

import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.wysiwyg.WFont;
import us.mn.state.dot.tms.utils.wysiwyg.WRenderer;
import us.mn.state.dot.tms.utils.wysiwyg.WState;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Text character token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtTextChar extends WToken {

	private char ch;
	private boolean bWhitespace;
	static String invalidChars = "\t\b\n\r\f";

	static public final char NULL_CHAR = (char)0;
	
	private WFont wfont;
	private DmsColor fgColor;
	
	/** Standard MULTI-text character.
	 * @param ch
	 */
	public WtTextChar(char ch) {
		super(WTokenType.textChar, null);
		this.ch = ch;
		bWhitespace = Character.isWhitespace(ch);
		anchorLoc = AnchorLoc.NONE;
		updateString();
	}

	/** Null MULTI-text character.
	 * (Used to get coordinate info on empty text rows.) */
	public WtTextChar() {
		super(WTokenType.textChar, null);
		ch = NULL_CHAR;
		bWhitespace = true;
		anchorLoc = AnchorLoc.NONE;
		updateString();
	}

	/** Get token char */
	public int getCh() {
		return ch;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addSpan(tokStr);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.renderText(this);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		sb.append(ch);
	}

	//-------------------------------------------
	
	@Override
	public boolean isTag() {
		return false;
	}
	
	@Override
	public boolean isPrintableText() {
		return true;
	}
	
	/** Update internal copy of token string.
	 * (Overridden here because WtTextChar
	 *  has no MULTI-tag prefix or suffix.) */
	@Override
	public void updateString() {
		if (ch == '[')
			tokStr = "[[";
		else if (ch == ']')
			tokStr = "]]";
		else if (ch == NULL_CHAR)
			tokStr = "";
		else
			tokStr = String.valueOf(ch);
	}
	
	/** Is this character blank? */
	@Override
	public boolean isBlank() {
		return bWhitespace;
	}

	@Override
	public boolean isValid() {
		if (invalidChars.indexOf(ch) >= 0)
			return false;
		return super.isValid();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#isNormalizeLine()
	 */
	public boolean isNormalizeLine() {
		return true;
	}
	
	/** Set the font used for this token. Called by the renderer. */
	public void setFont(WFont wf) {
		wfont = wf;
	}
	
	/** Get the font used for this token. */
	public WFont getFont() {
		return wfont;
	}

	/** Set the color used for this token. Called by the renderer. */
	public void setColor(DmsColor fg) {
		fgColor = fg;
	}
	
	/** Get the color used for this token */
	public DmsColor getColor() {
		return fgColor;
	}
	
//	@Override
//	public boolean useAnchor() {
//		return false;
//	}
}
