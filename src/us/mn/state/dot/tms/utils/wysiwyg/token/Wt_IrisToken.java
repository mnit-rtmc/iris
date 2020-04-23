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

import us.mn.state.dot.tms.utils.wysiwyg.WRenderer;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Parent class for tokens that represent
 *  IRIS-specific non-MULTI tags.
 *  (a.k.a. "action tags")
 *  
 * @author John L. Stanley - SRF Consulting
 */

abstract public class Wt_IrisToken extends WToken {

	/** Number of horizontal characters used to
	 *  draw this token in WYSIWYG renderer */
	protected Integer charCntX;

	/** Parent constructor for Iris-tag (a.k.a 
	 *  "action tag") tokens
	 *  
	 * @param tt WtokenType
	 * @param aPrefix Prefix string for token
	 * @param charCntX Number of character spaces to use
	 */
	public Wt_IrisToken(WTokenType tt, String aPrefix) {
		super(tt, aPrefix);
//		this.charCntX = charCntX;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		//TODO: Rework once WRenderer is updated to handle it
		wr.addAnchor(this);
//		wr.addIrisToken(this);
	}

	/** Append the charCntX parameter to the tag */
	public void appendCharCntXParameter(StringBuilder sb) {
		//TODO: Uncomment once both parsers can handle the extra argument
//		if (charCntX != null) {
//			sb.append(';');
//			sb.append(charCntX);
//		}
	}

	//-------------------------------------------
	
	/** Is this token blank? */
	@Override
	public boolean isBlank() {
		return false;
	}

//	/** Set the number of horizontal characters
//	 *  used to draw this token in WYSIWYG
//	 *  renderer. */
//	public void setCharCntX(Integer charCntX) {
//		this.charCntX = charCntX;
//	}
//		
//	/** Get the number of horizontal characters
//	 *  used to draw this token in WYSIWYG
//	 *  renderer.  If charCntX is set to null,
//	 *  this method returns the child-class's
//	 *  getDefaultCharCntX() value. */
//	public Integer getCharCntX() {
//		if (charCntX == null)
//			return getDefaultCharCntX();
//		return charCntX;
//	}

	//-------------------------------------------
	// Abstract method for child classes
	
	/** Get the default charCntX for the child class */
	abstract public Integer getDefaultCharCntX();
}
