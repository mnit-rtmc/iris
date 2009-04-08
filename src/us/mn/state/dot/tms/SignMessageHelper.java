/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.LinkedList;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.utils.SString;

/**
 * Helper for dealing with sign messages.
 *
 * @author Douglas Lau
 */
public class SignMessageHelper {

	/** Do not allow objects of this class */
	private SignMessageHelper() {
		assert false;
	}

	/** Return an array of font numbers in a message.
	 * @param f_num Default font number, one based.
	 * @return An integer array with length equal to the number 
	 *	    of pages in the message */
	static public int[] getFont(SignMessage sm, int f_num) {
		if(sm == null)
			return new int[0];
		MultiString m = new MultiString(sm.getMulti());
		if(m == null)
			return new int[0];
		return m.getFont(f_num);
	}

	/** Return an array of font names in a message.
	 * @param f_num Default font number, one based.
	 * @return A string array with length equal to the number 
	 *	    of pages in the message */
	static public String[] getFontName(SignMessage sm, int f_num) {
		int[] fn = getFont(sm, f_num);
		if(fn == null || fn.length <= 0)
			return new String[0];
		String[] fns = new String[fn.length];
		for(int i=0; i < fns.length; ++i) {
			Font font = FontHelper.find(fn[i]);
			if(font != null)
				fns[i] = font.getName();
			else
				fns[i] = "Font #" + fn[i];
		}
		return fns;
	}

	/** Create an array of lines from the given message */
	static public String[] createLines(SignMessage m) {
		return createLines(m, 0);
	}

	/** Create an array of lines from the given message */
	static public String[] createLines(SignMessage m, final int n_lines) {
		final LinkedList<String> ls = new LinkedList<String>();
		MultiString multi = new MultiString(m.getMulti());
		multi.parse(new MultiString.SpanCallback() {
			public void addSpan(
				int p, MultiString.JustificationPage jp,
				int l, MultiString.JustificationLine jl,
				int f_num, String t)
			{
				int m_lines = Math.max(n_lines, l + 1);
				while(ls.size() < (p + 1) * m_lines)
					ls.add("");
				int i = p * m_lines + l;
				String v = ls.get(i);
				ls.set(i, SString.trimJoin(v, t));
			}
		}, 1);
		return ls.toArray(new String[0]);
	}
}
