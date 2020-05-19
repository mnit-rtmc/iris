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
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.MultiSyntaxError;
import us.mn.state.dot.tms.utils.wysiwyg.WRenderer;
import us.mn.state.dot.tms.utils.wysiwyg.WState;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Line-justification token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtJustLine extends WToken {

	JustificationLine jl;

	/**
	 * @return the line justification
	 */
	public JustificationLine getJustification() {
		return jl;
	}

	/**
	 * @param jl
	 */
	public WtJustLine(JustificationLine jl) {
		super(WTokenType.justificationLine, "[jl");
		this.jl = jl;
		if ((jl != null)
		 && (jl == JustificationLine.UNDEFINED))
			addErr(MultiSyntaxError.unsupportedTagValue);
		updateString();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.setJustificationLine(jl);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.renderJustificationLine(this);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		if (jl != null)
			sb.append(jl.ordinal());
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#isNormalizeLine()
	 */
	public boolean isNormalizeLine() {
		return true;
	}
	
	public String getDescription() {
		return String.format("%s tag: %s",
				getType().getLabel(), getJustification().toString());
	}
}
