/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NTCIP -- MULTI (MarkUp Language for Transportation Information)
 *
 * @author Douglas Lau
 */
public class MultiString implements Serializable {

	/** Line Justification enumeration */
	public enum JustificationLine {
		UNDEFINED, OTHER, LEFT, CENTER, RIGHT, FULL;

		static public JustificationLine fromInt(int v) {
			for(JustificationLine lj: JustificationLine.values()) {
				if(lj.ordinal() == v)
					return lj;
			}
			return UNDEFINED;
		}

		static protected JustificationLine parse(String tag) {
			int j = Integer.parseInt(String.valueOf(tag.charAt(2)));
			return fromInt(j);
		}
	}

	/** Regular expression to match supported MULTI tags */
	static protected final Pattern TAG = Pattern.compile(
		"\\[(nl|np|jl[2345])\\]");

	/** Line justification pattern */
	static protected final Pattern JUST_PATTERN =
		Pattern.compile("\\[jl[2345]\\]");

	/** Text validation regex pattern */
	static protected final Pattern TEXT_PATTERN =
		Pattern.compile("[\\p{Upper}\\p{Digit}\\p{Blank}:,.;%-]*");

	/** Validate message text */
	static public boolean isValid(String s) {
		for(String t: JUST_PATTERN.split(s)) {
			Matcher m = TEXT_PATTERN.matcher(t);
			if(!m.matches())
				return false;
		}
		return true;
	}

	/** MULTI string buffer */
	protected final StringBuilder b = new StringBuilder();

	/** Test if the MULTI string is equal to another string */
	public boolean equals(Object o) {
		return toString().equals(o.toString());
	}

	/** Create an empty MULTI string */
	public MultiString() {
	}

	/** Create a new MULTI string */
	public MultiString(String t) {
		addText(t);
	}

	/** Add text to the current line */
	public void addText(String s) {
		b.append(s);
	}

	/** Add a new line */
	public void addLine() {
		b.append("[nl]");
	}

	/** Add a new page */
	public void addPage() {
		b.append("[np]");
	}

	/** Get the value of the MULTI string */
	public String toString() {
		return b.toString();
	}

	/** MULTI string parsing callback interface */
	public interface Callback {
		void addText(int page, int line, JustificationLine just,
			String text);
	}

	/** Parse the MULTI string */
	public void parse(Callback cb) {
		int page = 0;
		int line = 0;
		JustificationLine just = JustificationLine.CENTER;
		Matcher m = TAG.matcher(b);
		for(String t: TAG.split(b)) {
			if(t.length() > 0)
				cb.addText(page, line, just, t);
			if(m.find()) {
				String tag = m.group(1);
				if(tag.equals("nl"))
					line++;
				else if(tag.equals("np")) {
					line = 0;
					page++;
				} else if(tag.startsWith("jl"))
					just = JustificationLine.parse(tag);
			}
		}
	}

	/** Is the MULTI string blank? */
	public boolean isBlank() {
		final StringBuilder _b = new StringBuilder();
		parse(new Callback() {
			public void addText(int p, int l, JustificationLine j,
				String t)
			{
				_b.append(t);
			}
		});
		return _b.toString().trim().equals("");
	}
}
