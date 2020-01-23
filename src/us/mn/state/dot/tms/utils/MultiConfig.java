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
package us.mn.state.dot.tms.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.TMSException;

/** MultiConfig - Configuration-info
 *  for MULTI renderers and editors.
 * 
 * Combines info from _dms, sign_config,
 * and sign_detail records.
 * 
 * Can also generate a least-common-denominator
 * configuration for rendering sign-group messages.
 * 
 * Obtain a MultiConfig by calling one of:
 *   MultiConfig.from(DMS), 
 *   MultiConfig.from(SignGroup), 
 *   MultiConfig.fromSign("SignName"), or
 *   MultiConfig.fromSignGroup("GroupName").
 *   
 * Check that the from...(...) method returned
 * a non-null MultiConfig -and- that the
 * mcfg.isUseable() method returns true
 * before trying to use the MultiConfig object.
 *   
 * @author John L. Stanley - SRF Consulting
 */

public class MultiConfig {

	/* Create using from(..), fromSign(..),
	 * or fromSignGroup(..). **/
	private MultiConfig() {
	}

	//===========================================
	// "Errors" = Problems that prevent
	// creating a usable MultiConfig.

	private Set<String> errors =
			new LinkedHashSet<String>();

	private void logError(String errStr) {
		errors.add(errStr);
	}

	public String[] getErrors() {
		return (String[]) errors.toArray(new String[0]);
	}

	/* Returns true if no errors have been
	 * logged on this MultiConfig. */
	public boolean isUseable() {
		return errors.isEmpty();
	}
	
	//===========================================
	// "Warnings" = Problems that don't prevent
	// creating a usable MultiConfig (by using a
	// mix of compromises and/or "IRIS default"
	// values).

	private Set<String> warnings =
			new LinkedHashSet<String>();

	private void logWarning(String errStr) {
		warnings.add(errStr);
	}

	public String[] getWarnings() {
		return (String[]) warnings.toArray(new String[0]);
	}

	//===========================================

	final static public String BITMAP    = "Bitmap";
	final static public String SIGN      = "Sign";
	final static public String SIGNGROUP = "SignGroup";
	private String type;  // "Bitmap", "Sign", or "SignGroup"
	
	private String name;  // sign-name or sign-group-name

	/** This list contains a self-reference to this MultiConfig.
	 * If this is a composite (sign-group) MultiConfig, the
	 * self-reference is followed by a MultiConfig for each
	 * sign in the group. */
	private List<MultiConfig> configList = null;

	//-------------
	// values obtained from more than one source
	
	private DmsColor defaultBG;
	private DmsColor defaultFG;
	
	/** Values used in MultiExpandDafaults to replace
	 *  default-value color tags with explicit-value
	 *  tags. */
	private int[] defaultBGTagVal = null;
	private int[] defaultFGTagVal = null;
	
	/** Default-font number the database says we
	 *  should be using (after applying optional
	 *  sign-specific override). */
	private int defaultFontNo;

	/** Default-font (if it exists in IRIS database) */
	private Font defaultFont;

	/** Value used in MultiExpandDefaults to expand "default font" tag. */
	private Integer defaultFontTagVal;

	//-------------
	// values from SignConfig

	/** Width & height of the sign face (mm) */
	private int faceWidth; // (mm)
	private int faceHeight; // (mm)

	/** Horizontal & vertical border (mm) */
	private int borderHoriz;
	private int borderVert;

	/** Horizontal & vertical pitch (mm) */
	private int pitchHoriz;
	private int pitchVert;

	/** Sign width & height in pixels */
	private int pixelWidth;
	private int pixelHeight;
	
	/** Character width & height in pixels (0 means variable) */
	private int charWidth;
	private int charHeight;

	/** Monochrome foreground & background colors (24-bit). */
	private int monochromeFG;
	private int monochromeBG;

	/** DMS Color scheme */
	public ColorScheme colorScheme;

	//-------------
	// values from SignDetail
	
	/** Does sign have a beacon? */
	private boolean bHasBeacon;

	/** DMS type {VMS_CHAR, VMS_LINE, VMS_FULL, ...} */
	private DMSType dmsType;

	/** Supported MULTI tags (bitmap of tag values) */
	private int supportedTags;

	/** Maximum number of pages */
	private int maxPages;

	/** Maximum MULTI string length */
	private int maxMultiLen;

	//===========================================
	// Helper methods
	
	static private DmsColor genMono8bitColor(int iColor, DmsColor baseColor) {
		if ((0 > iColor) || (iColor > 255))
			return null;
		int red   = (baseColor.red   * iColor) >> 8;
		int green = (baseColor.green * iColor) >> 8;
		int blue  = (baseColor.blue  * iColor) >> 8;
		return new DmsColor(red, green, blue);
	}

	static private int[] genTagVal3(DmsColor c) {
		return new int[]{
				c.red,
				c.green,
				c.blue};
	}
	
	static private int[] genTagVal1(int iTagVal) {
		return new int[]{iTagVal};
	}
	
	/** Generate IRIS-default BG/FG colors */
	@SuppressWarnings("incomplete-switch")
	private void genIrisDefaultColors() {
		byte[] bgBytes = DMSHelper.getDefaultBackgroundBytes(colorScheme);
		byte[] fgBytes = DMSHelper.getDefaultForegroundBytes(colorScheme);
		int bg0 = bgBytes[0];
		int fg0 = fgBytes[0];
		DmsColor bgMonoColor;
		DmsColor fgMonoColor;
		switch (colorScheme) {
			case MONOCHROME_1_BIT:
				bgMonoColor = new DmsColor(monochromeBG);
				fgMonoColor = new DmsColor(monochromeFG);
				defaultBG = (bg0 == 0) ? bgMonoColor : fgMonoColor;
				defaultFG = (fg0 == 0) ? bgMonoColor : fgMonoColor;
				defaultBGTagVal = genTagVal1(bg0);
				defaultFGTagVal = genTagVal1(fg0);
				break;
			case MONOCHROME_8_BIT:
				fgMonoColor = new DmsColor(monochromeFG);
				defaultBG = genMono8bitColor(bg0, fgMonoColor);
				defaultFG = genMono8bitColor(fg0, fgMonoColor);
				defaultBGTagVal = genTagVal1(bg0);
				defaultFGTagVal = genTagVal1(fg0);
				break;
			case COLOR_CLASSIC:
				defaultBG = ColorClassic.fromOrdinal(bg0).clr;
				defaultFG = ColorClassic.fromOrdinal(fg0).clr;
				defaultBGTagVal = genTagVal1(bg0);
				defaultFGTagVal = genTagVal1(fg0);
				break;
			case COLOR_24_BIT:
				if (bgBytes.length == 3) {
					defaultBG = new DmsColor(bg0, bgBytes[1], bgBytes[2]);
					defaultBGTagVal = genTagVal3(defaultBG);
				}
				else {
					defaultBG = ColorClassic.fromOrdinal(bg0).clr;
					defaultBGTagVal = genTagVal1(bg0);
				}
				if (fgBytes.length == 3) {
					defaultFG = new DmsColor(fg0, fgBytes[1], fgBytes[2]);
					defaultFGTagVal = genTagVal3(defaultFG);
				}
				else {
					defaultFG = ColorClassic.fromOrdinal(fg0).clr;
					defaultFGTagVal = genTagVal1(fg0);
				}
		}
	}

	//===========================================
	// Supported MULTI-tag methods
	
	public boolean supportsTag(MultiTag tag) {
		int mask = 1 << tag.ordinal();
		return ((supportedTags & mask) != 0);
	}
	
	private void removeTagSupport(MultiTag tag) {
		int mask = 1 << tag.ordinal();
		if ((supportedTags & mask) != 0)
			supportedTags ^= mask;
	}
	
	//===========================================
	// Basic-colors mode
	//
	// If we have default-color problems, then:
	//		render using IRIS default colors (black/amber),
	//		prevent use of color tags, and
	//		deploy messages using only sign-default colors.

	private boolean bUsingBasicColorsMode = false;

	private void forceBasicColorsMode(String errStr) {
		if (bUsingBasicColorsMode == false) {
			genIrisDefaultColors();
			removeTagSupport(MultiTag.cb); // color background
			removeTagSupport(MultiTag.cf); // color foreground
			removeTagSupport(MultiTag.pb); // page background
			removeTagSupport(MultiTag.cr); // color rectangle
			bUsingBasicColorsMode = true;
		}
		if (errStr != null)
			logWarning(errStr+"  Using basic-colors mode.");
	}

	public boolean usingBasicColorsMode() {
		return bUsingBasicColorsMode;
	}
	
	//===========================================
	// Certain conditions prohibit use of WYSIWYG,
	// but we can still allow MULTI-only editing.
	
	private boolean bMultiOnlyEditing = false;

	private void forceMultiOnlyEditing(String errStr) {
		bMultiOnlyEditing = true;
		if (errStr != null)
			logWarning(errStr+"  Using MULTI-only editing.");
	}
	
	public boolean usingMultiOnlyEditing() {
		return bMultiOnlyEditing;
	}

	//===========================================
	//TODO: Use this flag to make changes to editor:
	// 1) Automatically add a [fo<groupDefaultFont>] at start of all messages
	// 2) Convert sign-default font tags [fo] to explicit group-default font tags.

	private boolean bMultipleDefaultFonts = false;

	public boolean usingMultipleDefaultFonts() {
		return bMultipleDefaultFonts;
	}

	//===========================================
	// load and reconcile methods

	/* Load SignConfig values */
	private boolean loadSignConfig(SignConfig signConfig) {
		if (signConfig == null) {
			logError("Sign configuration info is missing.");
			return false;
		}
		faceWidth    = signConfig.getFaceWidth();
		faceHeight   = signConfig.getFaceHeight();
		borderHoriz  = signConfig.getBorderHoriz();
		borderVert   = signConfig.getBorderVert();
		pitchHoriz   = signConfig.getPitchHoriz();
		pitchVert    = signConfig.getPitchVert();
		pixelWidth   = signConfig.getPixelWidth();
		pixelHeight  = signConfig.getPixelHeight();
		charWidth    = signConfig.getCharWidth();
		charHeight   = signConfig.getCharHeight();
		monochromeBG = signConfig.getMonochromeBackground();
		monochromeFG = signConfig.getMonochromeForeground();
		colorScheme  = ColorScheme.fromOrdinal(signConfig.getColorScheme());
		//NOTE:  defaultFont is handled in loadSign
		
		if (pixelWidth <= 0)
			logError("pixelWidth = "+pixelWidth);
		if (pixelHeight <= 0)
			logError("pixelHeight = "+pixelHeight);
		switch (colorScheme) {
			case MONOCHROME_1_BIT:
			case COLOR_24_BIT:
			case MONOCHROME_8_BIT:
			case COLOR_CLASSIC:
				break; 
			default:
				logError("Unknown color scheme = "+signConfig.getColorScheme());
		}
		return isUseable();
	}

	/** Reconcile SignConfig differences. */
	private void reconcileSignConfigs(MultiConfig mcfgNew) {
		faceWidth    = Math.min(faceWidth,   mcfgNew.faceWidth);
		faceHeight   = Math.min(faceHeight,  mcfgNew.faceHeight);
		borderHoriz  = Math.min(borderHoriz, mcfgNew.borderHoriz);
		borderVert   = Math.min(borderVert,  mcfgNew.borderVert);
		pitchHoriz   = Math.min(pitchHoriz,  mcfgNew.pitchHoriz);
		pitchVert    = Math.min(pitchVert,   mcfgNew.pitchVert);
		pixelWidth   = Math.min(pixelWidth,  mcfgNew.pixelWidth);
		pixelHeight  = Math.min(pixelHeight, mcfgNew.pixelHeight);
		charWidth    = Math.min(charWidth,   mcfgNew.charWidth);
		charHeight   = Math.min(charHeight,  mcfgNew.charHeight);
		if (!colorScheme.equals(mcfgNew.colorScheme)) {
			forceBasicColorsMode("Multiple color schemes detected.");
		}
	}

	//-------------

	/** Load SignDetail values */
	private void loadSignDetail(SignDetail signDetail) {
		if (signDetail == null) {
			// make-up a set of minimum details
			bHasBeacon    = false;
			if (charHeight > 0) {
				if (charWidth > 0)
					dmsType = DMSType.VMS_CHAR;
				else
					dmsType = DMSType.VMS_LINE;
			}
			else
				dmsType = DMSType.VMS_FULL;
			supportedTags = -53510145;
			maxMultiLen   = 312;
			maxPages      = 6;
			return;
		}
		String bt = signDetail.getBeaconType();
		bHasBeacon = !(bt.equals("none") || bt.equals("unknown"));
		dmsType       = DMSType.fromOrdinal(signDetail.getDmsType());
		supportedTags = signDetail.getSupportedTags();
		maxMultiLen   = signDetail.getMaxMultiLen();
		maxPages      = signDetail.getMaxPages();

		if (dmsType == DMSType.UNKNOWN)
			logError("Unknown sign type = "+signDetail.getDmsType());
//		if (maxMultiLen <= 0)
//			logError("maxMultiLen = "+maxMultiLen);
//		if (maxPages <= 0)
//			logError("maxPages = "+maxPages);

		// deal with problems in IRIS sign_details table
		if (maxMultiLen <= 0)
			maxMultiLen = 312;
		if (maxPages <= 0)
			maxPages = 3;
	}
	
	/** Reconcile SignDetail differences. */
	private void reconcileSignDetails(MultiConfig mcfgNew) {
		bHasBeacon    &= mcfgNew.bHasBeacon;    // allow beacon if both have one
		supportedTags &= mcfgNew.supportedTags; // allow tags supported by both signs
		maxPages      = Math.min(maxPages,    mcfgNew.maxPages);
		maxMultiLen   = Math.min(maxMultiLen, mcfgNew.maxMultiLen);

		if (dmsType != mcfgNew.dmsType) {
			// incompatible sign layouts (char/line/full matrix)
			forceMultiOnlyEditing("Sign-group contins multiple sign-types.");
		}
	}

	//-------------
	// values from DMS

	/* Load Sign/DMS values */
	@SuppressWarnings("incomplete-switch")
	private void loadSign(DMS dms) throws TMSException {

		// Load sign_config values
		if (!loadSignConfig(dms.getSignConfig()))
			return;

		// Load sign_detail values
		loadSignDetail(dms.getSignDetail());

		genIrisDefaultColors();

		// Apply DMS-specific "override" FG/BG colors
		Integer oBG = dms.getOverrideBackground();
		if (oBG != null) {
			defaultBG = new DmsColor(oBG);
			switch (colorScheme) {
				case MONOCHROME_1_BIT:
				case MONOCHROME_8_BIT:
				case COLOR_CLASSIC:
					defaultBGTagVal = genTagVal1(oBG);
					break;
				case COLOR_24_BIT:
					defaultBGTagVal = new int[]{
							defaultBG.red,
							defaultBG.green,
							defaultBG.blue};
			}
		}
		Integer oFG = dms.getOverrideForeground();
		if (oFG != null) {
			defaultFG = new DmsColor(oFG);
			switch (colorScheme) {
				case MONOCHROME_1_BIT:
				case MONOCHROME_8_BIT:
				case COLOR_CLASSIC:
					defaultFGTagVal = new int[]{oFG};
					break;
				case COLOR_24_BIT:
					defaultFGTagVal = new int[]{
							defaultFG.red,
							defaultFG.green,
							defaultFG.blue};
			}
		}

		// SanityCheck: Did we get usable default colors?
		String msg = null;
		if (defaultBG == null)
			msg = "Bad default background color.";
		if (defaultFG == null) {
			if (msg != null)
				msg = "Bad default colors.";
			else
				msg = "Bad default foreground color.";
		}
		if (msg != null)
			forceBasicColorsMode(msg);

		// Figure out what font we should be using
		defaultFontNo = DMSHelper.getDefaultFontNumber(dms);
		Font f = dms.getOverrideFont();
		if (f != null) {
			defaultFont = f;
			defaultFontTagVal = f.getNumber();
		}
		else
			defaultFont = FontHelper.find(defaultFontNo);
		
		// SanityCheck: Do we have a copy of the default font?
		if (defaultFont == null)
			logError("Default font ("+defaultFontNo+") not in database");
	}

	/** Reconcile Sign differences. */
	private void reconcileSigns(MultiConfig mcfgNew) {
		if (defaultFont != mcfgNew.defaultFont) {
			// Keep initial default font, but record warning.
			logWarning("Conflicting default fonts: "
					+defaultFont.getNumber()+" vs "
					+mcfgNew.defaultFont.getNumber()
					+" on sign "+mcfgNew.name);
			mcfgNew.logWarning("Conflicting default font: "+mcfgNew.defaultFontNo);
			bMultipleDefaultFonts = true;
		}

		//TODO:  Change to allow "similar" default-colors...
		try {
			if ((defaultFG.rgb() != mcfgNew.defaultFG.rgb())
			 || (defaultBG.rgb() != mcfgNew.defaultBG.rgb())) {
				forceBasicColorsMode("Conflicting default colors detected.");
				mcfgNew.logWarning("Conflicting default colors detected.");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	//===========================================
	// MultiConfig generators
	//===========================================
	
	/** Create a MultiConfig from a DMS object. */
	static public MultiConfig from(DMS dms) throws TMSException {
		if (dms == null)
			return null;
		MultiConfig mcfg = new MultiConfig();
		mcfg.loadSign(dms);
		mcfg.name = dms.getName();
		mcfg.type = SIGN;
		return mcfg;
	}

	/** Create a MultiConfig from a sign name. */
	static public MultiConfig fromSign(String signName) throws TMSException {
		DMS dms = DMSHelper.lookup(signName);
		return from(dms);
	}

	/** Create a composite MultiConfig from a SignGroup object. */
	static public MultiConfig from(SignGroup sg) {
		if (sg == null)
			return null;
		MultiConfig cfgGroup = null;
		MultiConfig cfgSign;
		DmsSignGroup dsg;
		DMS dms;
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while (it.hasNext()) {
			dsg = it.next();
			if (dsg.getSignGroup() == sg) {
				dms = dsg.getDms();
				if (dms == null)
					continue;
				try {
					cfgSign = from(dms);
					if ((cfgSign == null) || !cfgSign.isUseable())
						continue;
					if (cfgGroup == null) {
						// Make a copy of the 1st sign's MultiConfig
						// to start building a composite MultiConfig.
						cfgGroup = from(dms);
						cfgGroup.name = sg.getName();
						cfgGroup.type = SIGNGROUP;
						cfgGroup.configList = new ArrayList<MultiConfig>();
						cfgGroup.configList.add(cfgGroup);
					}
					else {
						cfgGroup.reconcileSignConfigs(cfgSign);
						cfgGroup.reconcileSignDetails(cfgSign);
						cfgGroup.reconcileSigns(cfgSign);
					}
					cfgGroup.configList.add(cfgSign);
				} catch (TMSException e) {
					// Should never happen...
					e.printStackTrace();
				}
			}
		}
		return cfgGroup;
	}

	/** Create a composite MultiConfig from a SignGroup name. */
	static public MultiConfig fromSignGroup(String groupName) {
		SignGroup sg = SignGroupHelper.lookup(groupName);
		return from(sg);
	}

	//===========================================
	// General MultiConfing getter methods

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public boolean isGroup() {
		return SIGNGROUP.equals(type);
	}

	public List<MultiConfig> getConfigList() {
		return configList;
	}

	//------
	
	public DmsColor getDefaultBG() {
		return defaultBG;
	}

	public DmsColor getDefaultFG() {
		return defaultFG;
	}

	/* Configured default background color tag
	 * value(s) for use in MultiExpandDefaults
	 * class.  Length of array indicates 1 or 3
	 * number tag value.
	 * If returns null, indicates no overrides. */
	public int[] getDefaultBGTagVal() {
		return defaultBGTagVal;
	}

	/* Configured default foreground color tag value(s).
	 * Length of array indicates 1 or 3 number tag value.
	 * If returns null, indicates no overrides. */
	public int[] getDefaultFGTagVal() {
		return defaultFGTagVal;
	}

	public Font getDefaultFont() {
		return defaultFont;
	}

	public int getDefaultFontNum() {
		if (defaultFont != null)
			return defaultFont.getNumber();
		return FontHelper.DEFAULT_FONT_NUM;
	}

	/* Configured default font tag value.
	 * If null, indicates no overrides */
	public Integer getDefaultFontTagVal() {
		return defaultFontTagVal;
	}

	/** Get horizontal pitch (mm)
	 * (center-to-center distance between LED pixels) */
	public int getPitchHoriz() {
		return pitchHoriz;
	}

	/** Get vertical pitch (mm)
	 * (center-to-center distance between LED pixels) */
	public int getPitchVert() {
		return pitchVert;
	}

	/** Get width of the sign face (mm) */
	public int getFaceWidth() {
		return faceWidth;
	}

	/** Get height of the sign face (mm) */
	public int getFaceHeight() {
		return faceHeight;
	}

	/** Get horizontal border (mm) */
	public int getBorderHoriz() {
		return borderHoriz;
	}
	
	/** Get vertical border (mm) */
	public int getBorderVert() {
		return borderVert;
	}
	
	/** Get sign width (pixels) */
	public int getPixelWidth() {
		return pixelWidth;
	}

	/** Get sign height (pixels) */
	public int getPixelHeight() {
		return pixelHeight;
	}

	/** Get character width (pixels; 0 means variable) */
	public int getCharWidth() {
		return charWidth;
	}

	/** Get character height (pixels; 0 means variable) */
	public int getCharHeight() {
		return charHeight;
	}

	public ColorScheme getColorScheme() {
		return colorScheme;
	}

	public DMSType getDmsType() {
		return dmsType;
	}

	public boolean hasBeacon() {
		return bHasBeacon;
	}

	public int getSupportedTags() {
		return supportedTags;
	}

	public int getMaxPages() {
		return maxPages;
	}

	public int getMaxMultiLen() {
		return maxMultiLen;
	}
}
