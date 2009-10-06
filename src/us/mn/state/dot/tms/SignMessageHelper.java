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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.utils.SString;

/**
 * Helper for dealing with sign messages.
 *
 * @author Douglas Lau
 */
public class SignMessageHelper extends BaseHelper {

	/** Do not allow objects of this class */
	private SignMessageHelper() {
		assert false;
	}

	/** Find a sign message with matching attributes */
	static public SignMessage find(final String multi, final String bitmaps,
		DMSMessagePriority ap, DMSMessagePriority rp, final boolean s,
		final Integer d)
	{
		final int api = ap.ordinal();
		final int rpi = rp.ordinal();
		return (SignMessage)namespace.findObject(SignMessage.SONAR_TYPE,
			new Checker<SignMessage>()
		{
			public boolean check(SignMessage sm) {
				return multi.equals(sm.getMulti()) &&
				       bitmaps.equals(sm.getBitmaps()) &&
				       api == sm.getActivationPriority() &&
				       rpi == sm.getRunTimePriority() &&
				       s == sm.getScheduled() &&
				       integerEquals(d, sm.getDuration());
			}
		});
	}

	/** Compare two (possibly-null) integers for equality */
	static protected boolean integerEquals(Integer i0, Integer i1) {
		if(i0 == null)
			return i1 == null;
		else
			return i0.equals(i1);
	}

	/** Compare 2 sign messages.
	 * @param sm1 SignMessage which may be null.
	 * @param sm2 SignMessage which may be null.
	 * @return True if both SignMessages have equal normalized
	 *         MULTIs, priorities, and bitmaps. */
	static public boolean equals(SignMessage sm1, SignMessage sm2) {
		if(sm1 == null && sm2 == null)
			return true;
		if(sm1 == null || sm2 == null)
			return false;
		if(!(new MultiString(sm1.getMulti()).equals(sm2.getMulti())))
			return false;
		if(sm1.getActivationPriority() != sm2.getActivationPriority())
			return false;
		if(sm1.getRunTimePriority() != sm2.getRunTimePriority())
			return false;
		final String bm1 = sm1.getBitmaps();
		final String bm2 = sm2.getBitmaps();
		if(bm1 == null)
			return bm2 == null;
		else
			return bm1.equals(bm2);
	}

	/** Return an array of font names in a message.
	 * @param f_num Default font number, one based.
	 * @return A string array with length equal to the number 
	 *	    of pages in the message */
	static public String[] getFontNames(SignMessage sm, int f_num) {
		int[] fn = getFonts(sm, f_num);
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

	/** Get an array of font numbers in a message.
	 * @param f_num Default font number, one based.
	 * @return An array of font numbers for each page of the message. */
	static protected int[] getFonts(SignMessage sm, int f_num) {
		if(sm == null)
			return new int[0];
		MultiString m = new MultiString(sm.getMulti());
		return m.getFonts(f_num);
	}

	/** Create an array of lines from the given message */
	static public String[] createLines(SignMessage m) {
		return createLines(m, 0);
	}

	/** Create an array of lines from the given message */
	static public String[] createLines(SignMessage m, final int n_lines) {
		if(m == null || m.getMulti() == null)
			return new String[0];
		final LinkedList<String> ls = new LinkedList<String>();
		MultiString multi = new MultiString(m.getMulti());
		multi.parse(new MultiStringStateAdapter() {
			public void addSpan(String span) {
				// note: fields in span use ms prefix
				int m_lines = Math.max(n_lines, ms_line + 1);
				while(ls.size() < (ms_page + 1) * m_lines)
					ls.add("");
				int i = ms_page * m_lines + ms_line;
				String v = ls.get(i);
				ls.set(i, SString.trimJoin(v, span));
			}
		});
		return ls.toArray(new String[0]);
	}

	/** Check if a sign message is blank */
	static public boolean isBlank(SignMessage m) {
		return isMultiBlank(m) && isBitmapBlank(m);
	}

	/** Check if the MULTI string is blank */
	static public boolean isMultiBlank(SignMessage m) {
		return new MultiString(m.getMulti()).isBlank();
	}

	/** Check if the bitmap is blank */
	static public boolean isBitmapBlank(SignMessage m) {
		try {
			for(byte b: Base64.decode(m.getBitmaps())) {
				if(b != 0)
					return false;
			}
			return true;
		}
		catch(IOException e) {
			return false;
		}
	}

	/** Test if a sign message should be activated.
	 * @param existing Message existing on DMS.
	 * @param activating Message to be activated.
	 * @return True if message should be activated; false otherwise. */
	static public boolean shouldActivate(final SignMessage existing,
		final SignMessage activating)
	{
		if(existing == null)
			return true;
		if(activating == null)
			return false;
		if(existing.getScheduled() && activating.getScheduled())
			return true;
		// This check is needed because even blank messages will always
		// activate if the priority is OVERRIDE.
		if(activating.getActivationPriority() ==
		   DMSMessagePriority.OVERRIDE.ordinal())
			return true;
		MultiString ms = new MultiString(activating.getMulti());
		if(ms.isBlank()) {
			// Only send a blank message if the new activation
			// priority matches the current runtime priority.  This
			// means that a blank AWS message will not blank the
			// sign unless the current message is an AWS message.
			return activating.getActivationPriority() ==
			       existing.getRunTimePriority();
		} else {
			return activating.getActivationPriority() >=
			       existing.getRunTimePriority();
		}
	}

	/** Render the SignMessage object as xml */
	static public void printXmlElement(SignMessage sm, PrintWriter out) {
		if(isBlank(sm))
			return;
		String[] ml = createLines(sm);
		if(ml != null && ml.length > 0) {
			out.print("<" + SignMessage.SONAR_TYPE + " ");
			String[] fonts = getFontNames(sm, 1);
			if(fonts.length > 0) {
				String f = SString.toString(fonts);
				out.print("font='" + f + "' ");
			}
			for(int i = 0; i < ml.length; i++)
				out.print("line_" + (i+1) + "='" + ml[i] +"' ");
			out.println("/>");
		}
	}
}
