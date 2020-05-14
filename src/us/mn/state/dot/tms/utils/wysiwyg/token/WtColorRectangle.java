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
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Color-rectangle token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtColorRectangle extends Wt_Rectangle implements Wt_ColorToken {
	
	Integer z;
	Integer r; // must be Integer; doubles as 1-or-3-parameter flag
	int g, b;
	private int[] tvColor;

	public WtColorRectangle(int x, int y, int w, int h, int z) {
		super(WTokenType.colorRectangle, "[cr");
		this.paramX = x;
		this.paramY = y;
		this.paramW = w;
		this.paramH = h;
		this.z = z;
		this.tvColor = toTagval(z);
		updateString();
	}

	public WtColorRectangle(int x, int y, int w, int h, int r, int g, int b) {
		super(WTokenType.colorRectangle, "[cr");
		this.paramX = x;
		this.paramY = y;
		this.paramW = w;
		this.paramH = h;
		this.r = r;
		this.g = g;
		this.b = b;
		this.tvColor = toTagval(r,g,b);
		updateString();
	}

	/** Get ColorForeground tagval. */
	public int[] getColor() {
		return tvColor;
	}

	/** Set the color rectangle's color */
	public void setColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		tvColor = toTagval(r,g,b);
	}
	
	@Override
	public boolean isBlank() {
		return false;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		if (tvColor == null)
			return;  //TODO: Figure out what to do here
		if (tvColor.length == 1)
			cb.addColorRectangle(paramX, paramY,
					paramW, paramH,
					tvColor[0]);
		else
			cb.addColorRectangle(paramX, paramY,
					paramW, paramH,
					tvColor[0], tvColor[1], tvColor[2]);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.renderColorRectangle(this);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		sb.append(paramX);
		sb.append(',');
		sb.append(paramY);
		sb.append(',');
		sb.append(paramW);
		sb.append(',');
		sb.append(paramH);
		sb.append(toStr(tvColor));
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
