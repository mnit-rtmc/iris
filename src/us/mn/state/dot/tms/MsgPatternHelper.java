/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import us.mn.state.dot.tms.utils.MultiAdapter;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * Helper class for messages patterns.
 *
 * @author Douglas Lau
 */
public class MsgPatternHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private MsgPatternHelper() {
		assert false;
	}

	/** Lookup the message pattern with the specified name */
	static public MsgPattern lookup(String name) {
		return (MsgPattern) namespace.lookupObject(
			MsgPattern.SONAR_TYPE, name);
	}

	/** Get a message pattern iterator */
	static public Iterator<MsgPattern> iterator() {
		return new IteratorWrapper<MsgPattern>(namespace.iterator(
			MsgPattern.SONAR_TYPE));
	}

	/** Find a message pattern with the specified MULTI string.
	 * @param ms MULTI string.
	 * @return A matching message pattern or null if no match is found. */
	static public MsgPattern find(String ms) {
		if (ms != null && !ms.isEmpty()) {
			MultiString multi = new MultiString(ms);
			Iterator<MsgPattern> it = iterator();
			while (it.hasNext()) {
				MsgPattern pat = it.next();
				if (multi.equals(pat.getMulti()))
					return pat;
			}
		}
		return null;
	}

	/** Is message allowed to combine first? */
	static public boolean isMsgCombiningFirst(MsgPattern pat) {
		return pat != null &&
		      (pat.getMsgCombining() == MsgCombining.FIRST.ordinal() ||
		       pat.getMsgCombining() == MsgCombining.EITHER.ordinal());
	}

	/** Is message allowed to combine second? */
	static public boolean isMsgCombiningSecond(MsgPattern pat) {
		return pat != null &&
		      (pat.getMsgCombining() == MsgCombining.SECOND.ordinal() ||
		       pat.getMsgCombining() == MsgCombining.EITHER.ordinal());
	}

	/** Finder for text rectangles in MULTI strings */
	static private class TextRectFinder extends MultiAdapter {
		final int width;
		final int height;
		final int font_def;
		ArrayList<TextRect> rects = new ArrayList<TextRect>();
		int font_num;
		int page = 1;
		boolean page_clean = true;

		private TextRectFinder(int w, int h, int fn) {
			width = w;
			height = h;
			font_def = fn;
			font_num = fn;
		}

		@Override public void addSpan(String span) {
			page_clean = false;
		}
		@Override public void setFont(Integer f_num, String f_id) {
			font_num = (f_num != null) ? f_num : font_def;
		}
		@Override public void addGraphic(int g_num, Integer x,
			Integer y, String g_id)
		{
			page_clean = false;
		}
		@Override public void addLine(Integer spacing) {
			page_clean = false;
		}
		@Override public void addPage() {
			if (page_clean) {
				rects.add(new TextRect(page, width,
					height, font_num));
			}
			page++;
			page_clean = true;
		}
		@Override public void setTextRectangle(int x, int y,
			int w, int h)
		{
			rects.add(new TextRect(page, w, h, font_num));
			page_clean = false;
		}
	}

	/** Find text rectangles in a pattern (including implicit) */
	static public List<TextRect> findTextRectangles(MsgPattern pat) {
		if (pat == null)
			return new ArrayList<TextRect>();
		SignConfig sc = pat.getSignConfig();
		if (sc == null)
			return new ArrayList<TextRect>();
		int width = sc.getPixelWidth();
		int height = sc.getPixelHeight();
		int fn = SignConfigHelper.getDefaultFontNum(sc);
		TextRectFinder trf = new TextRectFinder(width, height, fn);
		// find text rectangles in message pattern
		new MultiString(pat.getMulti()).parse(trf);
		// this creates a text rect on last page only if it's clean
		trf.addPage();
		return trf.rects;
	}

	/** Filler for text rectangles in MULTI strings */
	static private class TextRectFiller extends MultiBuilder {
		final String[] mess;
		int n_mess = 0;
		boolean page_clean = true;
		boolean page_rect = false;

		private TextRectFiller(String[] m) {
			mess = m;
		}

		private void fillRectangle() {
			if (n_mess < mess.length) {
				super.append(new MultiString(mess[n_mess]));
				n_mess++;
			}
		}

		@Override public void addSpan(String span) {
			super.addSpan(span);
			page_clean = false;
			page_rect = false;
		}
		@Override public void addGraphic(int g_num, Integer x,
			Integer y, String g_id)
		{
			super.addGraphic(g_num, x, y, g_id);
			page_clean = false;
			page_rect = false;
		}
		@Override public void addLine(Integer spacing) {
			super.addLine(spacing);
			page_clean = false;
			page_rect = false;
		}
		@Override public void addPage() {
			if (page_clean || page_rect)
				fillRectangle();
			super.addPage();
			page_clean = true;
			page_rect = false;
		}
		@Override public void setTextRectangle(int x, int y,
			int w, int h)
		{
			if (page_rect)
				fillRectangle();
			super.setTextRectangle(x, y, w, h);
			page_clean = false;
			page_rect = true;
		}
	}

	/** Fill text rectangles in a pattern */
	static public String fillTextRectangles(MsgPattern pat, String[] mess) {
		if (pat == null)
			return "";
		TextRectFiller trf = new TextRectFiller(mess);
		// fill text rectangles in message pattern
		new MultiString(pat.getMulti()).parse(trf);
		// if there's still a text rectangle, fill it at the end
		if (trf.page_clean || trf.page_rect)
			trf.fillRectangle();
		return trf.toString();
	}
}
