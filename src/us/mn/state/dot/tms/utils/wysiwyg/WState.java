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

package us.mn.state.dot.tms.utils.wysiwyg;

import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.Multi.JustificationPage;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.MultiRenderer;

/** WYSIWYG-editor State
 * 
 * @author John L. Stanley - SRF Consulting
 * @author Gordon Parikh - SRF Consulting
 *
 */
public class WState {
	
	/** MultiConfig used to initialize this WState */
	public MultiConfig mcfg;
	
	// colors
	public int bgPixel = WRaster.DEFAULT_BG;
	public int fgPixel = WRaster.DEFAULT_FG;

	// font info
	public WFontCache fontcache;
	private WFont      wfont;
	public int        charWidth;
	public int        charHeight;
	public Integer    charSpacing;

	// text rectangle area (1-based coordinates)
	public int trX;
	public int trY;
	public int trW;
	public int trH;
	
	// Justification
	public JustificationLine justLine;
	public JustificationPage justPage;
	
	// Page timing (deci-seconds)
	public int pageOn;
	public int pageOff;

	//-------------------------------------------

	public static int getDeciseconds(SystemAttrEnum attr) {
		float f = attr.getFloat() * 10;
		return Math.round(f);
	}
	
	/** Get IRIS default page-on time in deci-seconds. */
	public static int getIrisDefaultPageOnTime() {
		return getDeciseconds(SystemAttrEnum.DMS_PAGE_ON_DEFAULT_SECS);
	}

	/** Get IRIS default page-off time in deci-seconds. */
	public static int getIrisDefaultPageOffTime() {
		return getDeciseconds(SystemAttrEnum.DMS_PAGE_OFF_DEFAULT_SECS);
	}

	//-------------------------------------------
	
	/** Copy constructor */
	// TODO need to add more stuff here 
	public WState(WState old) {
		bgPixel     = old.bgPixel;
		fgPixel     = old.fgPixel;
		trX         = old.trX;
		trY         = old.trY;
		trW         = old.trW;
		trH         = old.trH;
		justLine    = old.justLine;
		justPage    = old.justPage;
		pageOn      = old.pageOn;
		pageOff     = old.pageOff;
		wfont       = old.wfont;
		fontcache   = old.fontcache;
		charWidth   = old.charWidth;
		charHeight  = old.charHeight;
		charSpacing = old.charSpacing;
	}

	/** Construct from a MultiConfig
	 * (Gets the initial render-state) */
	public WState(MultiConfig mcfg, WFontCache fc) {
		this.mcfg = mcfg;
		bgPixel     = WRaster.DEFAULT_BG;
		fgPixel     = WRaster.DEFAULT_FG;
		trX         = 1;
		trY         = 1;
		trW         = mcfg.getPixelWidth();
		trH         = mcfg.getPixelHeight();
		justLine    = MultiRenderer.defaultJustificationLine();
		justPage    = MultiRenderer.defaultJustificationPage();
		pageOn      = getIrisDefaultPageOnTime();
		pageOff     = getIrisDefaultPageOffTime();

		fontcache   = fc;
		setFont(null);
		charWidth   = Math.max(mcfg.getCharWidth(), 1);
		charHeight  = Math.max(mcfg.getCharHeight(), 1);
		charSpacing = wfont.getCharSpacing();
	}
	
	/** Set current font.
	 * @param fontNum font number to set or null to set default font.
	 * @return True if successful. False if no such font. */
	public boolean setFont(Integer fontNum) {
		if (fontNum == null)
			fontNum = mcfg.getDefaultFont().getNumber();
		WFont wf = fontcache.getWFont(fontNum);
		if (wf == null)
			return false;
		wfont = wf;
		return true;
	}
	
	/** Get current font */
	public WFont getWFont() {
		return wfont;
	}
}
