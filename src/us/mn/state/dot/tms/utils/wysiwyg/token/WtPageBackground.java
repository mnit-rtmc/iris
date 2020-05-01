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

import java.awt.Color;

import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.wysiwyg.WRenderer;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Page-background color token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtPageBackground extends WToken implements Wt_ColorToken {

	Integer z;
	Integer r; // must be Integer; doubles as 1-or-3-parameter flag
	int g, b;
	int[] tagval;

	/**
	 * @param z
	 */
	public WtPageBackground(Integer z) {
		super(WTokenType.pageBackground, "[pb");
		this.z = z;
		tagval = toTagval(z);
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
		tagval = toTagval(r,g,b);
		updateString();
	}

	/** Get PageBackground tagval. */
	public int[] getColorTagval() {
		return tagval;
	}
	
	/** Get a DmsColor object in the color of this tag. */
	public DmsColor getDmsColor() {
		if (r == null)
			return new DmsColor(z);
		else
			return new DmsColor(r, g, b);
	}
	
	/** Get a Color object in the color of this tag. */
	public Color getColor() {
		return getDmsColor().color;
	}
	
	@Override
	public boolean isBlank() {
		return (r == null) && (z == null);
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
		wr.renderPageBackground(this);
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
	
	public Integer getZValue() {
		return z;
	}
	
	public Integer getRValue() {
		return r;
	}
	
	public int getGValue() {
		return g;
	}
	
	public int getBValue() {
		return b;
	}
}
