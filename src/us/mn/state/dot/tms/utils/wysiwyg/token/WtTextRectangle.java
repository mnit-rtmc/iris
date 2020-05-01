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
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Text-rectangle token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtTextRectangle extends Wt_Rectangle {

	/**
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public WtTextRectangle(int x, int y, int w, int h) {
		super(WTokenType.textRectangle, "[tr");
		this.paramX = x;
		this.paramY = y;
		this.paramW = w;
		this.paramH = h;
		updateString();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#isLineToken()
	 */
	@Override
	public GetLinesCategory getLinesCategory() {
		return GetLinesCategory.LINEBREAK;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.setTextRectangle(paramX, paramY, paramW, paramH);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.renderTextRectangle(this);
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
	}
	
	/** Get the right edge X coordinate of this text rectangle.
	 * 
	 *  NOTE for some reason this needs to be adjusted by 1 compared to color
	 *  rectangles (probably due to some quirk in the renderer...)
	 */
	@Override
	public Integer getRightEdge() {
		Integer rx = super.getRightEdge();
		if (rx != null)
			return rx - 1;
		return null;
	}
	
	/** Get the bottom edge Y coordinate of this text rectangle.
	 * 
	 *  NOTE for some reason this needs to be adjusted by 1 compared to color
	 *  rectangles (probably due to some quirk in the renderer...)
	 */
	@Override
	public Integer getBottomEdge() {
		Integer ry = super.getBottomEdge();
		if (ry != null)
			return ry - 1;
		return null;
	}
}
