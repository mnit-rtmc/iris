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

/** Font token for WYSIWYG editor.
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class WtFont extends WToken {

	Integer f_num;
	String f_id;

	/** Construct loaded-font token
	 * @param f_num
	 * @param f_id
	 */
	public WtFont(Integer f_num, String f_id) {
		super(WTokenType.font, "[fo");
		this.f_num = f_num;
		this.f_id  = f_id;
		updateString();
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.setFont(f_num, f_id);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doRender(us.mn.state.dot.tms.utils.wysiwyg.WRenderer)
	 */
	@Override
	public void doRender(WRenderer wr) {
		wr.renderFont(this);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		if (f_num != null) {
			sb.append(f_num);
			if (f_id != null) {
				sb.append(",");
				sb.append(f_id);
			}
		}
	}


	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#isNormalizeLine()
	 */
	public boolean isNormalizeLine() {
		return true;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#isNormalizeLine2()
	 */
	public boolean isNormalizeLine2() {
		return false;
	}
	
	/**
	 * @return the f_num
	 */
	public Integer getFontNum() {
		return f_num;
	}

	/**
	 * @return the f_id
	 */
	public String getFontId() {
		return f_id;
	}

}
