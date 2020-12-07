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

import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.wysiwyg.WPoint;
import us.mn.state.dot.tms.utils.wysiwyg.WRenderer;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Graphic token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 * @author Gordon Parikh - SRF Consulting
 *
 */
public class WtGraphic extends WToken {

	int     g_num;
	String  g_id;
	
	/** Keep a handle to the graphic */
	Graphic graphic;

	/**
	 * @param g_num
	 * @param x
	 * @param y
	 * @param g_id
	 */
	public WtGraphic(int g_num, Integer x, Integer y, String g_id) {
		super(WTokenType.graphic, "[g");
		this.g_num  = g_num;
		this.paramX = x;
		this.paramY = y;
		this.g_id   = g_id;
		anchorLoc = AnchorLoc.NONE;
		updateString();
		graphic = GraphicHelper.find(g_num);
	}

	/** Get graphic number */
	public int getGraphicNum() {
		return g_num;
	}
	
	/** Get graphic version ID */
	public String getVersionId() {
		return g_id;
	}
	
	public Graphic getGraphic() {
		return graphic;
	}
	
	@Override
	public boolean isBlank() {
		return false;
	}

	@Override
	public boolean isText() {
		return false;
	}
	
	/** Check if the point p is inside (over) this graphic. */
	@Override
	public boolean isInside(WPoint p) {
		// calculate right/bottom edges based on params and graphic dimensions
		int rX = getRightEdge();
		int bY = getBottomEdge();
		boolean inX = (p.getSignX() >= paramX) && (p.getSignX() <= rX);
		boolean inY = (p.getSignY() >= paramY) && (p.getSignY() <= bY);
		return inX && inY;
	}
	
	/** Return the graphic's right edge X coordinate in pixels */
	@Override
	public Integer getRightEdge() {
		return paramX + graphic.getWidth() - 1;
	}
	
	/** Return the graphic's bottom edge Y coordinate in pixels */
	@Override
	public Integer getBottomEdge() {
		return paramY + graphic.getHeight() - 1;
	}
	
	/** Return the width of the graphic. Note that this is different from
	 *  other tokens.
	 */
	@Override
	public Integer getParamW() {
		return graphic.getWidth();
	}

	/** Return the height of the graphic. Note that this is different from
	 *  other tokens.
	 */
	@Override
	public Integer getParamH() {
		return graphic.getHeight();
	}
	
	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addGraphic(g_num, paramX, paramY, g_id);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.renderGraphic(this);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		sb.append(g_num);
		if (paramX != null) {
			sb.append(',');
			sb.append(paramX);
			sb.append(',');
			sb.append(paramY);
			if (g_id != null) {
				sb.append(",");
				sb.append(g_id);
			}
		}
	}
}
