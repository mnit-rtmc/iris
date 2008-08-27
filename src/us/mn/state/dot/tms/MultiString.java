/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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

	/** New line MULTI tag */
	static public final String NEWLINE = "[nl]";

	/** New page MULTI tag */
	static public final String NEWPAGE = "[np]";

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

		static protected JustificationLine parse(String v) {
			try {
				int j = Integer.parseInt(v);
				return fromInt(j);
			}
			catch(NumberFormatException e) {
				return UNDEFINED;
			}
		}
	}

	/** Regular expression to match supported MULTI tags */
	static protected final Pattern TAG = Pattern.compile(
		"\\[(nl|np|jl|tt)([A-Za-z0-9]*)\\]");

	/** Regular expression to match MULTI tags */
	static protected final Pattern TEXT_PATTERN = Pattern.compile(
		"[ !#$%&()*+,-./0-9:;<=>?@A-Z]*");

	/** Regular expression to match travel time tag */
	static protected final Pattern TRAVEL_TAG = Pattern.compile(
		"(.*?)\\[tt([A-Za-z0-9]+)\\]");

	/** Validate message text */
	static public boolean isValid(String s) {
		for(String t: TAG.split(s)) {
			Matcher m = TEXT_PATTERN.matcher(t);
			if(!m.matches())
				return false;
		}
		return true;
	}

	/** MULTI string buffer */
	protected final StringBuilder b = new StringBuilder();

	/** Test if the MULTI string is equal to another MULTI string */
	public boolean equals(Object o) {
		if(o instanceof MultiString)
			return toString().equals(o.toString());
		if(o instanceof String)
			return toString().equals(o.toString());
		return false;
	}

	/** Calculate a hash code for the MULTI string */
	public int hashCode() {
		return toString().hashCode();
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
		b.append(NEWLINE);
	}

	/** Add a new page */
	public void addPage() {
		b.append(NEWPAGE);
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
				} else if(tag.equals("jl")) {
					String v = m.group(2);
					just = JustificationLine.parse(v);
				}
			}
		}
	}

	/** Travel time calculating callback interface */
	public interface TravelCallback {

		/** Calculate the travel time to a destination */
		String calculateTime(String sid) throws InvalidMessageException;

		/** Check if the callback changed state */
		boolean isChanged();
	}

	/** Replace travel time tags with current travel time data */
	public String replaceTravelTimes(TravelCallback cb)
		throws InvalidMessageException
	{
		int end = 0;
		StringBuilder _b = new StringBuilder();
		Matcher m = TRAVEL_TAG.matcher(b);
		while(m.find()) {
			_b.append(m.group(1));
			_b.append(cb.calculateTime(m.group(2)));
			end = m.end();
		}
		_b.append(b.substring(end));
		return _b.toString();
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

	/** Parsing callback to count the number of pages */
	protected class PageCallback implements Callback {
		int num_pages = 0;
		public void addText(int p, int l, JustificationLine j, String t)
		{
			num_pages = Math.max(p + 1, num_pages);
		}
	}

	/** Get the number of pages in the multistring */
	public int getNumPages() {
		PageCallback pc = new PageCallback();
		parse(pc);
		return pc.num_pages;
	}
}
