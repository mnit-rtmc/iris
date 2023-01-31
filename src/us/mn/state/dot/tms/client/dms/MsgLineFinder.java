/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.MsgLineHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * Finder for message lines for a single DMS.  It creates and contains
 * MsgLineCBoxModel objects for each combobox in MessageComposer.
 * It caches the default character widths in order to be fast.
 *
 * @author Douglas Lau
 */
public class MsgLineFinder {

	/** Create a message line finder */
	static public MsgLineFinder create(DMS d, MsgPattern pat) {
		return (d != null && pat != null)
		      ? new MsgLineFinder(d, pat)
		      : null;
	}

	/** The DMS */
	private final DMS dms;

	/** Default font number */
	private final int def_fnum;

	/** Character spacing for default font */
	private final int char_spacing;

	/** Code point widths for default font */
	private final HashMap<Integer, Integer> widths =
		new HashMap<Integer, Integer>();

	/** List of TextRect values for each line */
	private final ArrayList<TextRect> line_rects =
		new ArrayList<TextRect>();

	/** Mapping of line numbers to models */
	private final HashMap<Short, MsgLineCBoxModel> models =
		new HashMap<Short, MsgLineCBoxModel>();

	/** Create a new message line finder */
	private MsgLineFinder(DMS d, MsgPattern pat) {
		dms = d;
		SignConfig sc = dms.getSignConfig();
		def_fnum = SignConfigHelper.getDefaultFontNum(sc);
		Font font = FontHelper.find(def_fnum);
		if (font != null) {
			for (Map.Entry<Integer, Glyph> ent :
				FontHelper.lookupGlyphs(font).entrySet())
			{
				widths.put(
					ent.getKey(),
					ent.getValue().getWidth()
				);
			}
			char_spacing = font.getCharSpacing();
		} else
			char_spacing = 0;
		TextRect full_rect = SignConfigHelper.textRect(sc);
		// make line-to-TextRect array
		line_rects.add(null); // line 0 is invalid
		for (TextRect tr: full_rect.find(pat.getMulti())) {
			for (int i = 0; i < tr.getLineCount(); i++)
				line_rects.add(tr);
		}
		Iterator<MsgLine> it = MsgLineHelper.iterator();
		while (it.hasNext()) {
			MsgLine ml = it.next();
			if (ml.getMsgPattern() == pat)
				checkLine(ml);
		}
	}

	/** Check if a message line belongs */
	private void checkLine(MsgLine ml) {
		String rht = ml.getRestrictHashtag();
		if (rht == null || DMSHelper.hasHashtag(dms, rht)) {
			short line = ml.getLine();
			if (isWidthOk(line, ml))
				getLineModel(line).add(ml);
		}
	}

	/** Check if a message linee width is OK */
	private boolean isWidthOk(int line, MsgLine ml) {
		if (line < line_rects.size()) {
			TextRect tr = line_rects.get(line);
			String txt = new MultiString(ml.getMulti()).asText();
			int w;
			if (tr.font_num == def_fnum)
				w = calculateWidth(txt);
			else {
				Font font = FontHelper.find(tr.font_num);
				w = FontHelper.calculateWidth(font, txt);
			}
			return (w > 0 && w <= tr.width);
		} else
			return false;
	}

	/** Calculate the width of a span of text */
	private int calculateWidth(String txt) {
		int width = 0;
		for (char c: txt.toCharArray()) {
			if (width > 0)
				width += char_spacing;
			int w = widths.get((int) c);
			if (w >= 0)
				width += w;
			else
				return -1;
		}
		return width;
	}

	/** Get the model for the specified line */
	public MsgLineCBoxModel getLineModel(short line) {
		if (models.containsKey(line))
			return models.get(line);
		else {
			MsgLineCBoxModel mdl = new MsgLineCBoxModel(line);
			models.put(line, mdl);
			return mdl;
		}
	}
}
