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

import us.mn.state.dot.tms.utils.wysiwyg.WFont;
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

	/** Font used for this token */
	protected WFont wfont;
	
	/** Box width computed when rendering */
	protected Integer boxWidth;

	/** Parent constructor for Iris-tag (a.k.a 
	 *  "action tag") tokens
	 *  
	 * @param tt WtokenType
	 * @param aPrefix Prefix string for token
	 */
	public Wt_IrisToken(WTokenType tt, String aPrefix) {
		super(tt, aPrefix);
		anchorLoc = AnchorLoc.CONDITIONAL;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.renderIrisToken(this);
	}

	//-------------------------------------------
	
	/** Is this token blank? */
	@Override
	public boolean isBlank() {
		return boxWidth == null;
	}

	//-------------------------------------------
	
	/** Set font.  Called in pre-render phase. */
	public void setFont(WFont wfont) {
		this.wfont = wfont;
	}
	
	/** Set box width. Called during rendering */
	public void setBoxWidth(Integer w) {
		boxWidth = w;
	}

	//-------------------------------------------
	// Abstract methods for child classes

	/** Get the box width from the child class.
	 * If a null is returned, the token is hidden,
	 * but is traversable like a page-time tag.
	 * @param chsp Character spacing (null = use font default)
	 */
	public abstract Integer getBoxWidth(Integer chsp);
}
