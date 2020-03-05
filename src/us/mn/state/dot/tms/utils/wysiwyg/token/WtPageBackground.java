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
import us.mn.state.dot.tms.utils.wysiwyg.WState;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Page-background color token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtPageBackground extends WToken {

	Integer z;
	Integer r; // must be Integer; doubles as 1-or-3-parameter flag
	int g, b;

	/**
	 * @param z
	 */
	public WtPageBackground(Integer z) {
		super(WTokenType.pageBackground, "[pb");
		this.z = z;
		updateString();
	}

	/**
	 * @param r
	 * @param g
	 * @param b
	 */
	public WtPageBackground(int r, int g, int b) {
		super(WTokenType.pageBackground, "[pb");
		this.r = r;
		this.g = g;
		this.b = b;
		updateString();
	}

	/** Get PageBackground tagval */
	public int[] getColorTagval() {
		return (r == null)
		      ? toTagval(z)
		      : toTagval(r, g, b);
	}

	@Override
	public boolean isBlank() {
		return (r == null) && (z == null);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#updateState(us.mn.state.dot.tms.utils.wysiwyg.WState)
	 */
	@Override
	public WState updateState(WState before) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		if (r == null)
			cb.setPageBackground(z);
		else
			cb.setPageBackground(r, g, b);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.setPageBackground(this);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		if (r == null) {
			if (z != null)
				sb.append(z);
		}
		else {
			sb.append(r);
			sb.append(',');
			sb.append(g);
			sb.append(',');
			sb.append(b);
		}
	}
}
