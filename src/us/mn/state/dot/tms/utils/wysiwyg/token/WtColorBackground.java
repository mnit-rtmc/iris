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

import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.wysiwyg.WRenderer;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** (Deprecated) Color-background token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtColorBackground extends WToken {

	Integer z;
	
	/** Construct background-color [cbZ] token
	 * @param z
	 */
	public WtColorBackground(Integer z) {
		super(WTokenType.colorBackground, "[cb");
		this.z = z;
		updateString();
	}

	/** Get the color background */
	public Integer getColorBackground() {
		return z;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#isLineToken()
	 */
	@Override
	public GetLinesCategory getLinesCategory() {
		return GetLinesCategory.IGNORE;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.setColorBackground(z);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.renderColorBackground(this);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		if (z == null)
			return;
		sb.append(z);
	}
}
