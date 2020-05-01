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

/** New-line token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtNewLine extends WToken {

	Integer spacing;
	
	int nextLineTop;
	int nextLineHeight;
	int nextLineLeft;

	/**
	 * @param spacing
	 */
	public WtNewLine(Integer spacing) {
		super(WTokenType.newLine, "[nl");
		this.spacing = spacing;
		anchorLoc = AnchorLoc.BEFORE;
		updateString();
	}

	@Override
	public boolean isPrintableText() {
		return true;
	}
	
	/** Get line spacing.
	 * null = use default font line-spacing. */
	public Integer getLineSpacing() {
		return spacing;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addLine(spacing);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.renderNewLine(this);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		if (spacing != null)
			sb.append(spacing);
	}
	
	/** Set parameters of the next line that is created by this newline. */
	public void setNextLineParams(int top, int height, int left) {
		nextLineTop = top;
		nextLineHeight = height;
		nextLineLeft = left;
	}
	
	public int getNextLineTop() {
		return nextLineTop;
	}
	
	public int getNextLineHeight() {
		return nextLineHeight;
	}
	
	public int getNextLineLeft() {
		return nextLineLeft;
	}
}
