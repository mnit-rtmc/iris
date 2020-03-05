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

import java.util.ArrayList;
import java.util.List;
import us.mn.state.dot.tms.utils.Multi;
import us.mn.state.dot.tms.utils.MultiSyntaxError;

/**
 * WToken - WYSIWYG-Editor Token
 * 
 * An object holding WYSIWYG information about
 * a MULTI tag or individual text character.
 * This object includes the string representation
 * of the token, the 0-based coordinates of the
 * token on a page, and any errors detected by
 * the error detection code.
 * 
 * @author John L. Stanley - SRF Consulting
 */

abstract public class WToken {

	protected WTokenType tokType;
	protected String tagPrefix;
	protected List<WEditorError> errList = new ArrayList<WEditorError>();
	protected String tokStr;

	/** Token-parameter coordinates, if any */
	protected Integer paramX, paramY, paramW, paramH;

	/** Token rendering/WYSIWYG coordinates (0-based) */
	protected Integer coordX;
	protected Integer coordY;
	protected Integer coordW;
	protected Integer coordH;

	//-------------------------------------------

	/* Base constructor for all WToken child classes */
	public WToken(WTokenType tt, String aPrefix) {
		tokType = tt;
		tagPrefix = aPrefix;
	}

	//-------------------------------------------

	/** Is this token a MULTI tag? */
	public boolean isTag() {
		return true;
	}

	/** Is this token a rectangle? */
	public boolean isRect() {
		return false;
	}

	/** Is this token not visible? */
	public boolean isBlank() {
		return true;
	}

	/** Include this in the WMessage.MultiNormalizer() string? */
	public boolean isNormalizeLine() {
		return false;
	}

	/** Include this in the WMessage.MultiNormalizer2() string? */
	public boolean isNormalizeLine2() {
		// Normally, this is identical to isNormalizeLine()
		// The ONLY place it's different is in WtFont.
		return isNormalizeLine();
	}

	/** Is this token part of the message-text?
	 * (WYSIWYG flag.  The only tokens that are
	 *  not part of the message-text are graphics,
	 *  text-rectangles, and color-rectangles.) */
	public boolean isText() {
		return true;
	}

//	/** Do we want to use an AnchorChar to
//	 *  find the coordinates of this token? */
//	public boolean useAnchor() {
//		return true;
//	}
	
	//-------------------------------------------
	// helper methods

	/** create a 1-integer tagval */
	static public int[] toTagval(Integer i) {
		if (i == null)
			return null;
		return new int[]{i};
	}

	/** create a 3-integer tagval */
	static public int[] toTagval(int r, int g, int b) {
		return new int[]{r, g, b};
	}
	
	/** convert tagval array to a string */
	static public String toStr(int[] tagval) {
		assert((tagval==null)||(tagval.length==1)||(tagval.length==3));
		if (tagval == null)
			return "";
		if (tagval.length == 1)
			return ","+tagval[0];
		return ","+tagval[0]+","+tagval[1]+","+tagval[2];
	}

	//-------------------------------------------
	// Error handling

	public WEditorError addErr(MultiSyntaxError mse) {
		WEditorError wte = new WEditorError(mse);
		errList.add(wte);
		return wte;
	}

	public WEditorError addErr(String shortErrStr) {
		WEditorError wte = new WEditorError(shortErrStr);
		errList.add(wte);
		return wte;
	}

//	public WEditorError addErr(WTagException ex) {
//		WEditorError wte = new WEditorError(ex);
//		errList.add(wte);
//		return wte;
//	}

	public List<WEditorError> getErrorList() {
		return errList;
	}
	
	public void clearErrors() {
		errList.clear();
	}

	public boolean isValid() {
		return errList.isEmpty();
	}

	//-------------------------------------------
	// Get/Set coordinate parameters (0 based coordinates)

	/** Return token X parameter in pixels.
	 * @return the X parameter
	 */
	public Integer getParamX() {
		return paramX;
	}

	/** Get token's X parameter, or a default
	 *  value if that parameter is null. */
	public int getParamX(int defX) {
		return (paramX != null) ? paramX : defX;
	}

	/** Set token X parameter in pixels.
	 * @param the X parameter
	 */
	public void setParamX(Integer tokX) {
		this.paramX = tokX;
	}

	/** Return token Y parameter in pixels.
	 * @return the Y parameter
	 */
	public Integer getParamY() {
		return paramY;
	}

	/** Get token's X parameter, or a default
	 *  value if that parameter is null. */
	public int getParamY(int defY) {
		return (paramY != null) ? paramY : defY;
	}

	/** Set token Y parameter in pixels.
	 * @param the Y parameter
	 */
	public void setParamY(Integer tokY) {
		this.paramY = tokY;
	}

	/** Return token width in pixels.
	 * @return the width 
	 */
	public Integer getParamW() {
		return paramW;
	}

//	/** Get token's W parameter, or a default
//	 *  value if that parameter is null. */
//	public int getParamW(int defW) {
//		return (paramW != null) ? paramW : defW;
//	}

	/** Set token width in pixels.
	 * @param the width
	 */
	public void setParamW(Integer tokW) {
		this.paramW = tokW;
	}

	/** Return token height in pixels.
	 * @return the height
	 */
	public Integer getParamH() {
		return paramH;
	}

//	/** Get token's H parameter, or a default
//	 *  value if that parameter is null. */
//	public int getParamH(int defH) {
//		return (paramH != null) ? paramH : defH;
//	}

	/** Set token height in pixels.
	 * @param the height
	 */
	public void setParamH(Integer tokH) {
		this.paramH = tokH;
	}

	/** Clear token metadata. */
	public void clearMetadata() {
		coordX = null;
		coordY = null;
		coordW = null;
		coordH = null;
		clearErrors();
	}

	/** Set token coordinates */
	public void setCoordinates(int x, int y, int w, int h) {
		coordX = x;
		coordY = y;
		coordW = w;
		coordH = h;
		//TODO: Limit coordinates to within the main raster
	}
	
	public boolean validCoordinates() {
		return (coordX != null)
		    && (coordY != null)
		    && (coordW != null)
		    && (coordH != null);
		//TODO: Check that coordinates are within the main raster
	}

	public void moveTok(int offsetX, int offsetY) {
		paramX += offsetX;
		paramY += offsetY;
	}

	//TODO:  Add code for adding & reporting errors/warnings
	
	//-------------------------------------------
	
	/** Token categories for WMessage.getLines() */
	public enum GetLinesCategory {
		IGNORE,
		INCLUDE,
		LINEBREAK,
		PAGEBREAK
	}
	
	/** Token category for WMessage.getLines() */
	public GetLinesCategory getLinesCategory() {
		return GetLinesCategory.INCLUDE;
	}
	
	//-------------------------------------------
	
	/** Location of AnchorChar for this token */
	public enum AnchorLoc {
		BEFORE,
		NONE,
		AFTER
	}
	
	protected AnchorLoc anchorLoc =
			AnchorLoc.AFTER;
	
	public AnchorLoc getAnchorLoc() {
		return anchorLoc;
	}
	
	//-------------------------------------------
	
	/** Append string representation of token to
	 *  a StringBuilder (Overridden in WtTextChar
	 *  because that token has no tag prefix or
	 *  suffix) */
	public void append(StringBuilder sb) {
		sb.append(tagPrefix);
		appendParameters(sb);
		sb.append("]");
	}

	/** returns the MULTI representation of the tag (or text) */
	public String toString() {
		return tokStr;
	}

	/** returns a string showing token coordinates and token string */
	public String toStringVerbose() {
		return String.format("{%4d, %4d, %4d, %4d} %s",
				coordX, coordY, coordW, coordH, tokStr);
//		return "x:"+coordX+", y:"+coordY+", w:"+coordW+", h:"+coordH+", tok:"+tokStr;
	}
	
	/** Update internal copy of token string.
	 *  (Overridden in WtTextChar because that
	 *   token has no tag prefix or suffix) */
	public void updateString() {
		StringBuilder sb = new StringBuilder();
		sb.append(tagPrefix);
		appendParameters(sb);
		sb.append("]");
		tokStr = sb.toString();
	}

	public int length() {
		return tokStr.length();
	}

	//===========================================
	// abstract methods
	//===========================================

	/** Have token update a WState in a token-specific manner */
	abstract public WState updateState(WState before);
	
	/** Call token-specific Multi method */
	abstract public void doMulti(Multi cb);

	/** Call token-specific renderer method */
	abstract public void doRender(WRenderer wr);

	/** Append token parameters to a StringBuilder */
	abstract public void appendParameters(StringBuilder sb);
}

//-------------------------------------------
// Notes for reference:
//
//	static WToken fromTokenType(WTokenType tt) {
//		switch (tt) {
//			case ambientTempCelsius:
//				break;
//			case ambientTempFarenheit:
//				break;
//			case colorBackground:
//				break;
//			case colorForeground:
//				break;
//			case colorRectangle:
//				break;
//			case dateOfMonth:
//				break;
//			case dayOfWeek:
//				break;
//			case feedMsg:
//				break;
//			case font:
//				break;
//			case graphic:
//				break;
//			case incidentLoc:
//				break;
//			case justificationLine:
//				break;
//			case justificationPage:
//				break;
//			case localTime12hr:
//				break;
//			case localTime24hr:
//				break;
//			case monthOfYear:
//				break;
//			case newLine:
//				break;
//			case newPage:
//				break;
//			case pageBackground:
//				break;
//			case pageTime:
//				break;
//			case parkingAvail:
//				break;
//			case slowWarning:
//				break;
//			case spacingChar:
//				break;
//			case textChar:
//				break;
//			case speedAdvisory:
//				break;
//			case speedKPH:
//				break;
//			case speedMPH:
//				break;
//			case textRectangle:
//				break;
//			case time12_AMPM:
//				break;
//			case time12_ampm:
//				break;
//			case tolling:
//				break;
//			case travelTime:
//				break;
//			case unsupportedTag:
//				break;
//			case year2digits:
//				break;
//			case year4digits:
//				break;
//			default:
//				break;
//			
//		}
//	}
