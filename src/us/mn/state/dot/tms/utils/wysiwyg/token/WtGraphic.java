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
import us.mn.state.dot.tms.utils.wysiwyg.WToken.AnchorLoc;

/** Graphic token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtGraphic extends WToken {

	int     g_num;
	String  g_id;

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
	}

	/** get graphic number */
	public int getGraphicNum() {
		// TODO Auto-generated method stub
		return g_num;
	}

	@Override
	public boolean isBlank() {
		return false;
	}

	@Override
	public boolean isText() {
		return false;
	}
	
//	@Override
//	public boolean useAnchor() {
//		return false;
//	}

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
		cb.addGraphic(g_num, paramX, paramY, g_id);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.addGraphic(this);
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
