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

import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Parent class for tokens that represent rectangles:
 *      (WtTextRectangle &amp; WtColorRectangle)
 *
 * @author John L. Stanley - SRF Consulting
 */

abstract public class Wt_Rectangle extends WToken {

	public Wt_Rectangle(WTokenType tt, String aPrefix) {
		super(tt, aPrefix);
		anchorLoc = AnchorLoc.NONE;
	}

	@Override
	public boolean isRect() {
		return true;
	}

	@Override
	public boolean isText() {
		return false;
	}
}
