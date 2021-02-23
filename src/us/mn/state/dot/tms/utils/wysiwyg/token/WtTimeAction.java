/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/** Time action substitution field token for WYSIWYG editor.
 *
 * @author Gordon Parikh - SRF Consulting
 * @author Douglas Lau
 */
public class WtTimeAction extends Wt_IrisToken {

	/** Date object that should take up the max number of characters */
	private static final LocalDateTime BIG_DATE =
		LocalDateTime.of(2020, 12, 31, 23, 59, 59);

	private String dir;
	private String format;

	public WtTimeAction(String dir, String format) {
		super(WTokenType.timeAction, "[ta");
		this.dir = dir;
		this.format = format;
		updateString();
	}

	/** Get chronological direction ("n" or "p") */
	public String getDir() {
		return dir;
	}

	/** Get format string */
	public String getFormat() {
		return format;
	}

	/** Get width of WYSIWYG box
	 * @param chsp Character spacing (null = use font default)
	 */
	@Override
	public Integer getBoxWidth(Integer chsp) {
		// replace time format with "big" date
		// and get the width of the resulting text
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
		String str = BIG_DATE.format(fmt);
		return wfont.getTextWidth(chsp, str);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#doMulti(us.mn.state.dot.tms.utils.Multi)
	 */
	@Override
	public void doMulti(Multi cb) {
		cb.addTimeAction(dir, format);
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tms.utils.wysiwyg.WToken#appendParameters(java.lang.StringBuilder)
	 */
	@Override
	public void appendParameters(StringBuilder sb) {
		sb.append(dir);
		sb.append(',');
		sb.append(format);
	}
}
