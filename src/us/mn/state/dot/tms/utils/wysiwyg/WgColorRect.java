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

import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtColorRectangle;

/**
 * Class for GUI operations with color rectangles in the WYSIWYG DMS Message
 * Editor. Not to be confused with the color rectangle MULTI tag token
 * WtColorRectangle.
 *
 * @author Gordon Parikh - SRF Consulting
 */

public class WgColorRect extends WgRectangle {
	
	public WgColorRect(WtColorRectangle crTok) {
		super(crTok);
	}
	
	/** Return the color rectangle tag associated with this color rectangle. */
	public WtColorRectangle getColorRectToken() {
		return (WtColorRectangle) rt;
	}
	
	/** Set the color on the color rectangle tag. */
	public void setColor(DmsColor c) {
		getColorRectToken().setColor(c.red, c.green, c.blue);
		rt.updateString();
	}
}
