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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
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
 * Obtain a MultiConfig by calling one of:
 *   MultiConfig.from(DMS), 
 *   MultiConfig.from(SignGroup), 
 *   MultiConfig.fromSign("SignName"), or
 *   MultiConfig.fromSignGroup("GroupName").
 *   
 * WARNING:  Check that the MultiCongig's
 *   isUseable() method returns true before
 *   trying to use the MultiConfig object
 *   for rendering.
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
		return errors.toArray(new String[0]);
	}

	/* Returns true if no errors have been
	 * logged on this MultiConfig. */
	public boolean isUseable() {
		return errors.isEmpty();
	}
	
	private void clearErrors() {
		errors = new LinkedHashSet<String>();
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
		return warnings.toArray(new String[0]);
	}

	//===========================================

	final static public String SIGN      = "Sign";
	final static public String CONFIG    = "Config";
	final static public String SIGNGROUP = "SignGroup";
	private String type;  // "Sign", "Config", or "SignGroup"
	
	private String name;

	/** If the MultiConfig is for a single Sign,
	 *  or a config, this list is null.
	 *  
	 *  If this MultiConfig is for a SignGroup,
	 *  this list contains one MultiConfig for each
	 *  configuration (same dmsType, geometry,
	 *  default font, and default BG/FG colors)
	 *  in the SignGroup.  This list is sorted
	 *  with the most common configuration first,
	 *  and has names "Config_1", "Config_2", etc... 
	 */
	private List<MultiConfig> configList = null;

	/** If the MultiConfig is for a single Sign,
	 *  this list is null.
	 *  
	 *  If this MultiConfig is for a config, this
	 *  list contains a MultiConfig for each sign
	 *  with that config.
	 *  
	 *  If this MultiConfig is for a SignGroup,
	 *  this list contains a MultiConfig for each
	 *  sign in that sign group.
	 */
	private List<MultiConfig> signList = null;

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
	
	/** Convert a grayscale (0-255) and an LED color into
	 *  a 24bit render-color for rendering 8bitMono signs. */
	static private DmsColor genMono8bitColor(int iColor, DmsColor baseColor) {
		if ((0 > iColor) || (iColor > 255))
			return null;
		int red   = (baseColor.red   * iColor) >> 8;
		int green = (baseColor.green * iColor) >> 8;
		int blue  = (baseColor.blue  * iColor) >> 8;
		return new DmsColor(red, green, blue);
	}

	/** Convert a DmsColor into a 3-int tag-value */
	static private int[] genTagVal3(DmsColor c) {
		return new int[]{
				c.red,
				c.green,
				c.blue};
	}
	
	/** Convert an integer into a 1-int tag-value */
	static private int[] genTagVal1(int iTagVal) {
		return new int[]{iTagVal};
	}
	
	/** Generate IRIS-default BG/FG colors */
	@SuppressWarnings("incomplete-switch")
	private void genIrisDefaultColors() {
		byte[] bgBytes = DMSHelper.getDefaultBackgroundBytes(colorScheme);
		byte[] fgBytes = DMSHelper.getDefaultForegroundBytes(colorScheme);
		int bg0 = bgBytes[0] & 0x0ff;
		int fg0 = fgBytes[0] & 0x0ff;
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
					defaultBG = new DmsColor(bg0, bgBytes[1] & 0x0ff, bgBytes[2] & 0x0ff);
					defaultBGTagVal = genTagVal3(defaultBG);
				}
				else {
					defaultBG = ColorClassic.fromOrdinal(bg0).clr;
					defaultBGTagVal = genTagVal1(bg0 & 0x0ff);
				}
				if (fgBytes.length == 3) {
					defaultFG = new DmsColor(fg0, fgBytes[1] & 0x0ff, fgBytes[2] & 0x0ff);
					defaultFGTagVal = genTagVal3(defaultFG);
				}
				else {
					defaultFG = ColorClassic.fromOrdinal(fg0).clr;
					defaultFGTagVal = genTagVal1(fg0 & 0x0ff);
				}
		}
	}

	//===========================================
	// Methods to test/modify MULTI-tags supported
	// by a given MultiConfig
	
	/** Does this MultiConfig support a given MULTI tag? */
	public boolean supportsTag(MultiTag tag) {
		int mask = 1 << tag.ordinal();
		return ((supportedTags & mask) != 0);
	}
	
	/** Remove support-flag for a MULTI tag */
	private void removeTagSupport(MultiTag tag) {
		int mask = 1 << tag.ordinal();
		if ((supportedTags & mask) != 0)
			supportedTags ^= mask;
	}
	
	//===========================================
	// Basic-colors mode
	//
	// If we have default-color problems, then:
	//		render using IRIS-basic colors (black/amber),
	//		prevent use of color tags, and
	//		save messages using only sign-default colors.

	private boolean bUsingBasicColorsMode = false;

	private void forceBasicColorsMode(String errStr) {
		if (bUsingBasicColorsMode == false) {
			genIrisDefaultColors();
			removeTagSupport(MultiTag.cb); // color background
			removeTagSupport(MultiTag.cf); // color foreground
			removeTagSupport(MultiTag.pb); // page background
			removeTagSupport(MultiTag.cr); // color rectangle
			bUsingBasicColorsMode = true;
			if (errStr != null)
				logWarning(errStr+"  Using basic-colors mode.");
			else
				logWarning("Using basic-colors mode.");
		}
	}

	public boolean usingBasicColorsMode() {
		return bUsingBasicColorsMode;
	}
	
	//===========================================
	// Methods that copy data from a SignConfig,
	// SignDetail, or DMS object 

	/* Load SignConfig values */
	private boolean loadSignConfig(DMS dms) {
		SignConfig signConfig = null;
		if (dms != null)
			signConfig = dms.getSignConfig();
		if (signConfig == null) {
			logError("Sign configuration missing for sign "+dms.getName());
			logError("  Substituting 40x21, mono1");
			// throw in some default values to let things progress
			faceWidth    = 3308;
			faceHeight   = 1721;
			borderHoriz  = 46;
			borderVert   = 82;
			pitchHoriz   = 70;
			pitchVert    = 70;
			pixelWidth   = 40;
			pixelHeight  = 21;
			charWidth    = 5;
			charHeight   = 7;
			monochromeBG = 0;
			monochromeFG = 16764928;
			colorScheme  = ColorScheme.MONOCHROME_1_BIT;
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
				logWarning("Unknown color scheme = "+signConfig.getColorScheme()+".  Changed to mono1");
				colorScheme = ColorScheme.MONOCHROME_1_BIT;
		}
		return isUseable();
	}

	/** Load SignDetail values */
	private boolean loadSignDetail(DMS dms) {
		SignDetail signDetail = null;
		if (dms != null)
			signDetail = dms.getSignDetail();
		if (signDetail == null) {
			logError("Sign detail info is missing for sign "+dms.getName());
			logError("  Substituting basic full-matrix sign");
			// throw in some default values to let things progress
			bHasBeacon = false;
			dmsType = DMSType.VMS_FULL;
			supportedTags = 0;
			maxMultiLen = 312;
			maxPages = 3;
			return false;
		}

		String bt = signDetail.getBeaconType();
		bHasBeacon = !(bt.equals("none") || bt.equals("unknown"));
		dmsType       = DMSType.fromOrdinal(signDetail.getDmsType());
		supportedTags = signDetail.getSupportedTags();
		maxMultiLen   = signDetail.getMaxMultiLen();
		maxPages      = signDetail.getMaxPages();

		if (dmsType == DMSType.UNKNOWN) {
			logWarning("Unknown sign type; changed to full-matrix");
			dmsType = DMSType.VMS_FULL;
		}
		if (maxMultiLen <= 0) {
			logWarning("maxMultiLen = "+maxMultiLen+"; changed to 312");
			maxMultiLen = 312;
		}
		if (maxPages <= 0) {
			logWarning("maxPages = "+maxPages+"; changed to 3");
			maxPages = 3;
		}
		return true;
	}
	
	/* Load Sign/DMS values */
	@SuppressWarnings("incomplete-switch")
	private void loadSign(DMS dms) throws TMSException {

		// Load sign_config values
		loadSignConfig(dms);

		// Load sign_detail values
		loadSignDetail(dms);

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

	//===========================================
	// Static-private methods for creating a MultiConfig
	
	/** Private method to generate a dummy
	 *  MultiConfig from an exception */
	static private MultiConfig fromException(Exception ex) {
		ex.printStackTrace();
		MultiConfig mcfg = new MultiConfig();
		mcfg.logError("Unexpected exception while getting sign configuration:");
		mcfg.logError("   "+ex.getLocalizedMessage());
		return mcfg;
	}
	
	/** Private method to generate a dummy
	 *  MultiConfig from an error message */
	static private MultiConfig fromErrorMsg(String msg) {
		MultiConfig mcfg = new MultiConfig();
		mcfg.logError("ERROR:  "+msg);
		return mcfg;
	}
	
	/** Private base method used for generating
	 *  all types of MultiConfig */
	static private MultiConfig fromDms(DMS dms, String type) {
		try {
			if (dms == null)
				return fromErrorMsg("Unable to load a null sign configuration");
			if ((dms.getName() == null) || dms.getName().isEmpty())
				return fromErrorMsg("Unable to load a nameless sign configuration");
			
			MultiConfig mcfg = new MultiConfig();
			mcfg.loadSign(dms);
			mcfg.name = dms.getName();
			mcfg.type = type;
			if (!mcfg.isUseable()) {
				String typ = type.toLowerCase();
					mcfg.logError("Error loading configuration for "+typ+" "+dms.getName());
			}
			return mcfg;
		}
		catch (Exception ex) {
			return fromException(ex);
		}
	}

	/** Private method to create a MultiConfig 
	 *  from a sign name and MultiConfig type. */
	static private MultiConfig fromSignName(String signName, String type) {
		DMS dms = DMSHelper.lookup(signName);
		return fromDms(dms, type);
	}
	
	//===========================================
	// static public methods for creating a MultiConfig

	/** Create a MultiConfig from a DMS (sign) object. */
	static public MultiConfig from(DMS dms) throws TMSException {
		return fromDms(dms, SIGN);
	}

	/** Create a MultiConfig from a DMS (sign) name. */
	static public MultiConfig fromSign(String signName) throws TMSException {
		return fromSignName(signName, SIGN);
	}

	/** Create a MultiConfig tree from a SignGroup object. */
	static public MultiConfig from(SignGroup sg) {
		if (sg == null)
			return fromErrorMsg("Unable to load a null SignGroup configuration");
		if ((sg.getName() == null) || sg.getName().isEmpty())
			return fromErrorMsg("Unable to load a nameless SignGroup configuration");

		// build an array list of sign MultiConfig objects
		MultiConfig mcSign;
		DmsSignGroup dsg;
		DMS dms;
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		ArrayList<MultiConfig> mcaSigns = new ArrayList<MultiConfig>();
		while (it.hasNext()) {
			dsg = it.next();
			if (dsg.getSignGroup() == sg) {
				dms = dsg.getDms();
				if (dms == null)
					continue;
				try {
					mcSign = from(dms);
					if (mcSign == null)
						continue;
					mcaSigns.add(mcSign);
				} catch (TMSException e) {
					// Should never happen...
					e.printStackTrace();
				}
			}
		}
		if (mcaSigns.isEmpty())
			return fromErrorMsg("Unable to load any configurations for sign group "+sg.getName());

		// Sort and group the sign-MultiConfigs,
		// creating a new config-MultiConfig for
		// each set of identical signs.
		ArrayList<MultiConfig> mcaConfigs;
		mcaConfigs = sortAndGroup(mcaSigns);
		if (mcaConfigs.isEmpty())
			return null;

		// Create a primary SignGroup-MultiConfig
		// based on the most common config in
		// in the SignGroup.
		MultiConfig mcConfig = mcaConfigs.get(0);
		MultiConfig mcSignGroup = fromSignName(mcConfig.name, SIGNGROUP);
		mcSignGroup.name = sg.getName();
		mcSignGroup.configList = mcaConfigs;
		
		// Sort final list of sign MultiConfigs by sign name
		mcaSigns.sort(compareByName);
		mcSignGroup.signList = mcaSigns;

		// Rename config MultiConfigs based on
		// position in list and number of signs
		// in each config.
		int cfgNo = 1;
		int cnt;
		String suffix;
		boolean oneGood = false;
		for (MultiConfig mc : mcaConfigs) {
			if (mc.signList != null) {
				cnt = mc.signList.size();
				suffix = (cnt == 1) ? " sign)" : " signs)";
				mc.name = CONFIG+"_"+cfgNo+" ("+cnt+suffix;
			}
			else {
				// This "shouldn't be possible", but...
				mc.name = CONFIG+"_"+cfgNo+" (no signs)";
			}
			if (mc.isUseable())
				oneGood = true;
			++cfgNo;
		}
		if (oneGood) {
			mcSignGroup.clearErrors();
		}

		return mcSignGroup;
	}

	/** Create a MultiConfig tree from a SignGroup name. */
	static public MultiConfig fromSignGroup(String groupName) {
		SignGroup sg = SignGroupHelper.lookup(groupName);
		return from(sg);
	}

	//===========================================
	// Sorting and Grouping

	/** Compare by DmsType, geometry, and
	 *  default font. */
	private int compare1(MultiConfig mc2) {
		if (dmsType == null) {
			System.out.println("MultiConfig.compare1: dmsType is null");
			return 0;
		}
		if (mc2.dmsType == null) {
			System.out.println("MultiConfig.compare1: mc2.dmsType is null");
			return 0;
		}
		int cmp = dmsType.compareTo(mc2.dmsType);
		if (cmp != 0)
			return cmp;
		cmp = pixelWidth - mc2.pixelWidth;
		if (cmp != 0)
			return cmp;
		cmp = pixelHeight - mc2.pixelHeight;
		if (cmp != 0)
			return cmp;
		cmp = charWidth - mc2.charWidth;
		if (cmp != 0)
			return cmp;
		cmp = charHeight - mc2.charHeight;
		if (cmp != 0)
			return cmp;
		return defaultFontNo - mc2.defaultFontNo;
	}

	/** Compare two DmsColor values.
	 *  (also checks for nulls) */
	private int compareDmsColors(DmsColor dc1, DmsColor dc2) {
		if (dc1 == dc2)
			return 0;
		if (dc1 == null)
			return -1;
		if (dc2 == null)
			return 1;
		return dc1.rgb() - dc2.rgb();
	}
	
	/** Compare by DmsType, geometry, default font,
	 *  and default background/foreground colors. */
	private int compare2(MultiConfig mc2) {
		if (mc2 == null) {
			System.out.println("MultiConfig.compare1: mc2 is null");
			return 0;
		}
		int cmp = compare1(mc2);
		if (cmp != 0)
			return cmp;
		if (defaultBG == null)
		cmp = compareDmsColors(defaultBG, mc2.defaultBG);
		if (cmp != 0)
			return cmp;
		return compareDmsColors(defaultFG, mc2.defaultFG);
	}

	/** Comparator to sort MultiConfig(s) by DmsType,
	 *  geometry, and default font. */
	private static Comparator<MultiConfig> comparator1 =
		new Comparator<MultiConfig>() {
			public int compare(MultiConfig mc1, MultiConfig mc2) {
				return mc1.compare1(mc2);
			}
		};
		
	/** Comparator to sort MultiConfig(s) by name */
	private static Comparator<MultiConfig> compareByName =
		new Comparator<MultiConfig>() {
			public int compare(MultiConfig mc1, MultiConfig mc2) {
				return mc1.getName().compareTo(mc2.getName());
			}
		};
			
	/** Comparator to sort MultiConfig(s) by DmsType,
	 *  geometry, default font, and BG/FG colors. */
	private static Comparator<MultiConfig> comparator2 =
		new Comparator<MultiConfig>() {
			public int compare(MultiConfig mc1, MultiConfig mc2) {
				boolean iu1 = mc1.isUseable();
				boolean iu2 = mc2.isUseable();
				if (iu1 != iu2)
					return iu1 ? -1 : 1;
				return mc1.compare2(mc2);
			}
		};
	
	/** Comparator to sort by number of entries
	 *  in each Multiconfig's signList.  Usable
	 *  configurations come before unusable.
	 *  And largest equally-usable signLists comes
	 *  before smaller signLists. */
	private static Comparator<MultiConfig> comparatorSignListSize =
		new Comparator<MultiConfig>() {
			public int compare(MultiConfig mc1, MultiConfig mc2) {
				boolean usable1 = mc1.isUseable();
				boolean usable2 = mc2.isUseable();
				if (usable1 != usable2)
					return usable1 ? -1 : 1;
				int s1, s2;
				if (mc1.signList == null)
					s1 = 0;
				else
					s1 = mc1.signList.size();
				if (mc2.signList == null)
					s2 = 0;
				else
					s2 = mc2.signList.size();
				return s2 - s1;
			}
		};
		
	/* Generate a hashkey-string containing
	 * dmsType, sign geometry, and
	 * default font. */
	private String genHashKey1() {
		StringBuilder sb = new StringBuilder();
		if (dmsType != null)
			sb.append(dmsType.ordinal());
		sb.append('\t');
		sb.append(pixelWidth);
		sb.append('\t');
		sb.append(pixelHeight);
		sb.append('\t');
		sb.append(charWidth);
		sb.append('\t');
		sb.append(charHeight);
		sb.append('\t');
		sb.append(defaultFontNo);
		return sb.toString();
	}

	/* Generate a hashkey-string containing
	 * dmsType, sign geometry, default font,
	 * and default BG/FG colors. */
	private String genHashKey2() {
		StringBuilder sb = new StringBuilder();
		sb.append(genHashKey1());
		sb.append('\t');
		if (defaultBG != null)
			sb.append(defaultBG.rgb());
		sb.append('\t');
		if (defaultFG != null)
		sb.append(defaultFG.rgb());
		sb.append('\t');
		sb.append(Arrays.toString(defaultBGTagVal));
		sb.append('\t');
		sb.append(Arrays.toString(defaultFGTagVal));
		return sb.toString();
	}

	/** Convert a MultiConfig array-list to a sign-group
	 *  MultiConfig and a set of config-MultiConfig(s).
	 *  All the signs represented in a config-MultiConfig
	 *  have the same geometry and default font.
	 * @param mca  Source MultiConfig array-list
	 * @return
	 */
	private static ArrayList<MultiConfig>
			sortAndGroup(ArrayList<MultiConfig> mcaAll) {
		// Sort by DmsType, geometry, font, and color(s)
		mcaAll.sort(comparator2);
		// Bin all MultiConfigs with the same config.
		// (Creates a new MC for each config.)
		HashMap<String,MultiConfig> mchConfigMap =
				new HashMap<String,MultiConfig>();
		ArrayList<MultiConfig> mcaConfigList =
				new ArrayList<MultiConfig>();
		String key;
		MultiConfig mcGroup;
		for (MultiConfig mc : mcaAll) {
			key = mc.genHashKey1();
			mcGroup = mchConfigMap.get(key);
			if (mcGroup == null) {
				mcGroup = fromSignName(mc.name, CONFIG);
				if (mcGroup == null)
					continue;
				mcGroup.signList = new ArrayList<MultiConfig>();
				mchConfigMap.put(key, mcGroup);
				mcaConfigList.add(mcGroup);
			}
			mcGroup.signList.add(mc);
		}
		// Sort bins by number of signs in each bin (largest first)
		mcaConfigList.sort(comparatorSignListSize);
		return mcaConfigList;
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

	public List<MultiConfig> getSignList() {
		return signList;
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
