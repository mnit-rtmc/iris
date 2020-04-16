/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.wysiwyg.WFontCache;
import us.mn.state.dot.tms.utils.wysiwyg.WGraphicCache;
import us.mn.state.dot.tms.utils.wysiwyg.WMessage;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;
import us.mn.state.dot.tms.utils.wysiwyg.WPoint;
import us.mn.state.dot.tms.utils.wysiwyg.WRaster;
import us.mn.state.dot.tms.utils.wysiwyg.WgRectangle;
import us.mn.state.dot.tms.utils.wysiwyg.WgTextRect;
import us.mn.state.dot.tms.utils.wysiwyg.WEditorErrorManager;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenList;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.WgColorRect;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtColorForeground;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtColorRectangle;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtFont;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtGraphic;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtJustLine;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtJustPage;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtNewLine;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtPageBackground;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextChar;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextRectangle;
import us.mn.state.dot.tms.utils.wysiwyg.token.Wt_Rectangle;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.Multi.JustificationPage;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * WYSIWYG DMS Message Editor Controller for handling exchanges between the
 * editor GUI form and the renderer.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WController {

	/** Flag to enable/disable verbose logging output */
	private final static boolean DEBUG = true; 
	
	/** Editing modes */
	private final static String MODE_TEXT = "text";
	private final static String MODE_GRAPHIC = "graphic";
	private final static String MODE_COLORRECT = "color_rectangle";
	private final static String MODE_TEXTRECT = "text_rectangle";
	private final static String MODE_MULTITAG = "multi_tag";
	private String editingMode = MODE_TEXT;
	
	/** Client Session and SmartDesktop */
	private Session session;
	private SmartDesktop desktop;
	
	/** Keep a handle to the editor form and sign pixel panel (TODO may 
	 * change) for any updates we need to make from here */
	private WMsgEditorForm editor;
	private WImagePanel signPanel;
	private WMsgMultiPanel multiPanel;
	
	/** Keep a list of MULTI strings for undoing/redoing */
	private ArrayList<WHistory> undoStack = new ArrayList<WHistory>();
	private ArrayList<WHistory> redoStack = new ArrayList<WHistory>();
	
	/** Keep the size of the undo stack as of the last good state (i.e. with
	 *  no MULTI/renderer errors) for restoring. */
	private int lastGoodState = 0;
	
	/** Cursor that will change depending on mode, etc. */
	private Cursor textCursor;
	private Cursor graphicCursor;
	private Cursor multiTagCursor;
	private Cursor moveCursor;
	private HashMap<String, Cursor> resizeCursors;
	private Cursor newRectCursor;
	private Cursor cursor;
	
	/** Mouse drag parameters */
	private boolean dragStarted = false;
	private WPoint lastPress;
	private WToken startTok;
	private WToken endTok;
	private String resizeDir;
	
	/** Sign/Group and Message being edited */
	private DMS sign;
	private SignGroup sg;
	private QuickMessage qm;
	private MultiString multiString;
	private String multiStringText = null;
	
	/** MultiConfig for config-related stuff  */
	private MultiConfig multiConfig;

	/** Currently selected page (defaults to first available) */
	private int selectedPageIndx = 0;
	private WPage selectedPage;
	
	/** Click threshold for selecting rectangles/graphics (in WYSIWYG
	 *  coordinates) */
	public final static int rThreshold = 3;
	
	/** Text rectangle(s) on the selected page */
	private ArrayList<WgTextRect> textRects;
	
	/** Currently selected/active text rectangle (may be the implicit "whole-
	 *  sign" text rectangle) and the tokens it contains. */
	private int selectedTrIndx = 0;
	private WgTextRect selectedTextRect;
	private WTokenList trTokens;

	/** Color rectangle(s) on the selected page */
	private ArrayList<WgColorRect> colorRects;
	
	/** Either text or color rectangles on the selected page, depending on
	 *  the mode
	 */
	private ArrayList<WgRectangle> modeRects = new ArrayList<WgRectangle>();
	
	/** Index of the selected rectangle in the rectangles for the cureent mode */
	private int selectedRectIndx = 0;
	
	/** Currently selected rectangle (text or color). If in text rectangle
	 *  mode, this will be the same as the selectedTextRectangle.
	 */
	private WgRectangle selectedRectangle;
	
	/** Handles of currently selected text or color rectangles */
	private HashMap<String,Rectangle> resizeHandles;

	/** Graphic list model */
	private DefaultComboBoxModel<Graphic> supportedGraphics =
			new DefaultComboBoxModel<Graphic>();
	
	/** Graphics on the selected page */
	private WTokenList graphics = new WTokenList();
	
	/** Currently selected graphic and index in the list */
	private WtGraphic selectedGraphic;
	private int selectedGraphicIndx = -1;
	
	/** Token lists for helping with caret placement and stuff */
	private WTokenList tokensBefore = new WTokenList();
	private WTokenList tokensSelected = new WTokenList();
	private WTokenList tokensAfter = new WTokenList();
	private int caretIndx = 0;
//	private boolean caretAtEnd = false;
	public final static int CARET_EOP = -1;
	
	/** Toggle for how non-text tags are handled. If false (default), non-text
	 *  tags are skipped, otherwise they can be manipulated through caret
	 *  navigation and a status bar below the sign panel.
	 */
	private boolean stepNonTextTags = false;
	
	/** WMessage for working with rendered message */
	private WMessage wmsg = null;
	
	/** Render Error Manager for receiving errors from renderer */
	private WEditorErrorManager errMan = new WEditorErrorManager(); 
	
	/** Current and default fonts */
	private Font font;
	private Font defaultFont;
	
	/** Current and default colors */
	private DmsColor fgColor;
	private DmsColor bgColor;
	private DmsColor fgColorDefault;
	private DmsColor bgColorDefault;
	private DmsColor colorRectColor;
	
	/** Page list */
	private WPageList pageList;
	
	/** DMS List (for sign groups) */
	private Map<String,DMS> dmsList;
	private JComboBox<String> dms_list;
	String[] dmsNames;
	
	public WController() {
		// empty controller - everything will be set later as it is available
	}	
	
	public WController(WMsgEditorForm e) {
		init(e);
	}	
	
	public WController(WMsgEditorForm e, DMS d) {
		setSign(d);
		init(e);
	}
	
	public WController(WMsgEditorForm e, SignGroup g) {
		setSignGroup(g);
		init(e);
	}
	
	public WController(WMsgEditorForm e, QuickMessage q, DMS d) {
		setSign(d);
		setQuickMessage(q);
		init(e);
	}
	
	public WController(WMsgEditorForm e, QuickMessage q, SignGroup g) {
		setSignGroup(g);
		setQuickMessage(q);
		init(e);
	}
	
	/** Print the message to stdout */
	public static void println(String msg) {
		System.out.println(msg);
	}
	
	/** Print a String.formatted message to stdout */
	public static void println(String fmt, Object... args) {
		if (DEBUG)
			println(String.format(fmt, args));
	}
	
	/** Print the tokens before, selected, and after the caret */
	public void printCaretTokens() {
		println("trTokens: %s", trTokens.toString());
		println("Before: %s", tokensBefore.toString());
		println("Selected: %s", tokensSelected.toString());
		println("After: %s", tokensAfter.toString());
	}
	
	/** Perform some initialization on the controller. Sets the editor form
	 *  handle, sets up the mouse cursor, etc.
	 */
	public void init(WMsgEditorForm e) {
		editor = e;
		session = editor.getSession();
		desktop = session.getDesktop();
		
		// initialize the cursor, starting in text mode
		initCursors();

		// initialize the page list
		makePageList();
	}
	
	/** Initialize some things that need to take place after other elements of
	 *  the editor are in place. Called by the WMsgEditorForm at the end of
	 *  initialize().
	 *  
	 *  TODO/NOTE there is probably a better way to do this.
	 */
	public void postInit() {
		// do an update to render the message and fill the page list, then
		// initialize the caret and mouse cursors
		update();
		initCaret();
		initCursors();
		
		// also give the sign panel focus so the user can immediately start
		// typing
		signPanel.requestFocusInWindow();
	}
	
	/** Set the sign being used */
	public void setSign(DMS d) {
		sign = d;
		
		// generate the MultiConfig for the sign
		if (sign != null) {
			try {
				setMultiConfig(MultiConfig.from(sign));
			} catch (TMSException e1) {
				// TODO what to do??
			}
		} else {
			multiConfig = null;
		}
		update();
	}
	
	/** Set the sign group being used */
	public void setSignGroup(SignGroup g) {
		sg = g;
		
		if (sg != null) {
			// generate the MultiConfig for the sign group
			setMultiConfig(MultiConfig.from(sg));
			
			// generate the list of signs in the group
			makeSignListForGroup(true);
		} else {
			multiConfig = null;
			sign = null;
		}
		update();
	}
	
	/** Set the quick message being edited */
	public void setQuickMessage(QuickMessage q) {
		qm = q;
		
		// get the MULTI string text from the quick message
		if (qm != null)
			multiStringText = qm.getMulti();
		else
			multiStringText = "";
//		println("From QuickMessage: " + multiStringText);
		
		update();
	}
	
	/** Return a new WPoint object created from the mouse event object and the
	 *  selected page's WRaster.
	 */
	private WPoint getWPoint(MouseEvent e) {
		return new WPoint(e, getActiveRaster());
	}
	
	/** Handle a click on the main editor panel */
	public void handleClick(MouseEvent e) {
//		int b = e.getButton();
		
		// TODO do we need this?
		update();
		
		// create a WPoint for this click
		WPoint p = getWPoint(e);
		
		// first set any active rectangles or graphics that might have been
		// selected
		if (inTextRectMode() || inColorRectMode())
			setSelectedRectangle(findRectangle(p));
		else if (inGraphicMode())
			setSelectedGraphic(findGraphic(p));
		
		// in text mode place the caret based on the mouse coordinates
		if (inTextMode())
			moveCaret(p);
		else if (inTextRectMode()) {
			// in text rectangle mode, we only place the caret if the user
			// clicked IN the text rectangle
			if (selectedTextRect != null && !selectedTextRect.isOnBorder(p))
				moveCaret(p);
			else
				clearCaret();
		} else if (inColorRectMode() || inGraphicMode())
			clearCaret();
		
		// TODO Graphic mode, MULTI tag mode
		
		// get focus for the sign panel when someone clicks on it
		signPanel.requestFocusInWindow();
	}
	
	/** Handle a mouse move event on the main editor panel */
	public void handleMouseMove(MouseEvent e) {
		// create a WPoint for this mouse event
		WPoint p = getWPoint(e);
		
		// set the drag mode (which sets the mouse cursor)
		setDragMode(p);
		
		// get focus for this component when someone clicks on it
		signPanel.requestFocusInWindow();
	}
	
	/** Handle a mouse pressed event  */
	public void handleMousePressed(MouseEvent e) {
		// TODO only handling left-click drag for now
		if (e.getButton() == MouseEvent.BUTTON1) {
			// create a WPoint for this mouse event
			WPoint p = getWPoint(e);
			if (inTextRectMode() || inColorRectMode())
				// first set (or clear) the selected rectangle
				setSelectedRectangle(findRectangle(p));
			else if (inGraphicMode())
				setSelectedGraphic(findGraphic(p));
			
			// set the drag mode given what the user just clicked on and save
			// these coordinates
			setDragMode(p);
			lastPress = p;
			
			if (inTextSelectionMode()) {
				// get the closest token to start the selection handler
				startTok = findClosestTextToken(lastPress);
				moveCaret(lastPress);
			}
		} else {
			lastPress = null;
			startTok = null;
		}
		// get focus for this component when someone clicks on it
		signPanel.requestFocusInWindow();
	}

	/** Handle a mouse drag event on the main editor panel */
	public void handleMouseDrag(MouseEvent e) {
		// TODO only handling left-click drag for now
		if (SwingUtilities.isLeftMouseButton(e)) {
			// TODO only doing basic text selection for now
			// update which token is under the cursor
			int x = e.getX();
			int y = e.getY();
			
			// create a WPoint for this event
			WPoint p = getWPoint(e);
			
			// record the drag as having started (creates an undo point)
			if (!dragStarted)
				startDrag();
			
			// if we're in text selection mode (text mode or text rectangle
			// mode), update the selection
			if (inTextSelectionMode())
				updateTextSelection(p);
			else if (inMoveMode())
				// if we're in move mode, move the active region
				updateMoveOperation(p);
			else if (inResizeMode())
				updateResizeOperation(p);
			else if (inNewRectMode())
				updateNewRectangleOperation(p);
		}
		// get focus for this component when someone clicks on it
		signPanel.requestFocusInWindow();
	}

	/** Finalize any active drag motion. */
	public void handleMouseReleased(MouseEvent e) {
		// construct a WPoint from the mouse event
		WPoint p = getWPoint(e);
				
		// if the x and y are the same as the last press, do nothing
		// (handleClick SHOULD take care of it...)
		if (lastPress != null &&
				lastPress.getWysiwygPoint() != p.getWysiwygPoint()) {
			if (inTextSelectionMode())
				updateTextSelection(p);
			else if (inMoveMode())
				updateMoveOperation(p);
			else if (inResizeMode())
				updateResizeOperation(p);
			else if (inNewRectMode())
				finishNewRectangleOperation(p);
		}
		
		if (inTextSelectionMode())
			printCaretTokens();
		
		// end any active drag operation
		endDrag();
		
		// get focus for this component when someone clicks on it
		signPanel.requestFocusInWindow();
	}
	
	/** Start a drag operation. This creates an undo point depending on the
	 *  current mode. This is called the first time a new drag event is
	 *  received, not on a mouse pressed event, so anything that needs to be
	 *  set then must be set elsewhere.
	 */
	private void startDrag() {
		if (!dragStarted) {
			dragStarted = true;
			if (inMoveMode() || inResizeMode() || inGraphicMode())
				saveState();
		}
	}
	
	/** End a drag operation. */
	private void endDrag() {
		// reset the drag handler
		dragStarted = false;
		lastPress = null;
		startTok = null;
		endTok = null;
		resizeDir = null;
	}
	
	private void updateTextSelection(WPoint p) {
		endTok = findClosestTextToken(p);
		
		// update the selection if they're not the same token
		if ((startTok != endTok) && startTok != null && endTok != null) {
			// get the indices and figure out which is first
			int start = trTokens.indexOf(startTok);
			int end = trTokens.indexOf(endTok);
			
			boolean includeEnd = false;
			boolean forwards = true;
			int si;
			int ei;
			if (start < end) {
				si = start;
				ei = end;
				includeEnd = rightHalf(p, endTok);
			} else {
				// if they are selecting backwards, we need to use the initial
				// click and token and reverse the indices
				si = end;
				ei = start;
				includeEnd = rightHalf(lastPress, startTok);
				forwards = false;
			}
			
			// update the token lists
			updateTokenListsSelection(si, ei, includeEnd);
			
			// tell the image panel what the selection is
			signPanel.setTextSelection(tokensSelected);
			
			// move the caret too - do it "manually" to avoid changing token
			// lists
			// TODO the caret will always appear at the end of the selection,
			// regardless of the "direction" - not a big issue but fix if it
			// comes
			caretIndx = si;
			if (forwards)
				signPanel.setCaretLocation(tokensSelected.getLast(), true);
			else
				signPanel.setCaretLocation(tokensSelected.get(0), false);
			
			// update the toolbar
			updateTextToolbar();
		}
	}
	
	/** Update the move operation given the current cursor position. */
	private void updateMoveOperation(WPoint p) {
		// try to get the token to move
		WToken mTok = null;
		if (inTextRectMode() || inColorRectMode()) {
			// TODO for some reason text rectangles can't be moved all the way
			// to the bottom-right... not sure why since CR are fine
			if (selectedRectangle != null)
				mTok = selectedRectangle.getRectToken();
		} else if (inGraphicMode())
			mTok = selectedGraphic;
		
		if (mTok != null && lastPress != null) {
			// calculate the safe change in position
			int ox = p.getSignX() - lastPress.getSignX();
			int oy = p.getSignY() - lastPress.getSignY();
			int offsetX = checkMoveOffsetX(ox, mTok);
			int offsetY = checkMoveOffsetY(oy, mTok);
			
			// move the token and update the last press
			mTok.moveTok(offsetX, offsetY);
			lastPress = p;
		}
		// update everything
		update();
	}
	
	/** Check that the X offset value ox will not move the rectangle or
	 *  graphic token t beyond the sign border. If it won't, ox is returned
	 *  unchanged, otherwise the value will be reduced until it will fit on
	 *  the sign.
	 */
	private int checkMoveOffsetX(int ox, WToken t) throws RuntimeException {
		if (!(t instanceof Wt_Rectangle || t instanceof WtGraphic))
			throw new RuntimeException("Invalid token type");
		int x = t.getParamX();
		int rx = t.getRightEdge();
		
		WRaster wr = getActiveRaster();
		if (x + ox < 1)
			return 1 - x;
		else if (rx + ox > wr.getWidth())
			return Math.max(rx - wr.getWidth(), 0);
		return ox;
	}
		
	/** Check that the Y offset value oy will not move the rectangle or
	 *  graphic token t beyond the sign border. If it won't, oy is returned
	 *  unchanged, otherwise the value will be reduced until it will fit on
	 *  the sign.
	 */
	private int checkMoveOffsetY(int oy, WToken t) throws RuntimeException {
		if (!(t instanceof Wt_Rectangle || t instanceof WtGraphic))
			throw new RuntimeException("Invalid token type");
		int y = t.getParamY();
		int by = t.getBottomEdge();
		
		WRaster wr = getActiveRaster();
		if (y + oy < 1)
			return 1 - y;
		else if (by + oy > wr.getHeight())
			return Math.max(by - wr.getHeight(), 0);
		return oy;
	}
	
	/** Update the resize operation given the current cursor position. */
	private void updateResizeOperation(WPoint p) {
		if (inTextRectMode() || inColorRectMode()) {
			if (selectedRectangle != null && lastPress != null) {
				// calculate the change in position
				int offsetX = p.getSignX() - lastPress.getSignX();
				int offsetY = p.getSignY() - lastPress.getSignY();
				
				// resize the rectangle
				// TODO need to make this safe
				selectedRectangle.resize(resizeDir, offsetX, offsetY);
				
				// update the last press
				lastPress = p;
			}
		}
		
		// update everything
		update();
	}
	
	/** Update the new rectangle operation given the current cursor position.
	 *  This only provides feedback for the user.
	 */
	private void updateNewRectangleOperation(WPoint p) {
		if (inTextRectMode() || inColorRectMode()) {
			// get coordinates of the rectangle in WYSIWYG coordinates
			// make sure the (x,y) coordinates are top-left
			int x = Math.min(lastPress.getWysiwygX(), p.getWysiwygX());
			int y = Math.min(lastPress.getWysiwygY(), p.getWysiwygY());
			int rx = Math.max(lastPress.getWysiwygX(), p.getWysiwygX());
			int by = Math.max(lastPress.getWysiwygY(), p.getWysiwygY());
			
			// calculate the size of the new rectangle
			int w = rx - x;
			int h = by - y;
			
			// pass the parameters to the sign panel for drawing
			signPanel.setRectangleInProgress(x, y, w, h);
		}
	}
	
	/** Finish the new rectangle operation. This actually creates the
	 *  rectangle (text or color).
	 *  
	 *  Note that to simplify things, the rectangle that is created will
	 *  differ very slightly from the one that was being drawn due to the
	 *  position of LED separators. This will not be very noticeable and can
	 *  be corrected with moving/resizing as needed (and saves many additional
	 *  calculations).
	 */
	private void finishNewRectangleOperation(WPoint p) {
		if (inTextRectMode() || inColorRectMode()) {
			// get coordinates of the rectangle in sign coordinates
			// make sure the (x,y) coordinates are top-left
			int x = Math.min(lastPress.getSignX(), p.getSignX());
			int y = Math.min(lastPress.getSignY(), p.getSignY());
			int rx = Math.max(lastPress.getSignX(), p.getSignX());
			int by = Math.max(lastPress.getSignY(), p.getSignY());
			
			// calculate the size of the new rectangle
			int w = rx - x + 1;
			int h = by - y + 1;
			
			// don't create 1-width/height rectangles (invalid)
			// note that others will be invalid too, but this is more common
			// because of coordinate conversion idiosyncrasies
			if (w > 1 && h > 1) {
				// create a new rectangle token
				Wt_Rectangle rt;
				if (inTextRectMode())
					rt = new WtTextRectangle(x, y, w, h);
				else {
					rt = new WtColorRectangle(x, y, w, h, colorRectColor.red,
							colorRectColor.green, colorRectColor.blue);
				}
				
				// make an undo point then add it to the page - it doesn't
				// really matter where (at least for now)
				saveState();
				selectedPage.addToken(rt);
				
				// update rectangles and get the WgRectangle object for the
				// rectangle we just created
				updateRectangles();
				for (WgRectangle r: modeRects) {
					if (r.getRectToken() == rt) {
						setSelectedRectangle(r);
						break;
					}
				}
				
				// update so the new rectangle shows up
				update();
			}
			
			// clear any temporary rectangle we were using for feedback,
			// whether we were successful or not
			signPanel.clearRectangleInProgress();
		}
	}
	
	/** Add a graphic to the current page. The graphic will be added to (1, 1)
	 *  (in sign coordinates), but it can be moved later.
	 */
	public void addGraphic(Graphic g) {
		// create a WtGraphic token for this graphic and add it to the end
		// of the page
		saveState();
		WtGraphic gt = new WtGraphic(g.getGNumber(), 1, 1, null);
		selectedPage.addToken(gt);
		update();
	}
	
	/** Get the resize handle direction (N/S/E/W/NE/NW/SE/SW) given the mouse
	 *  coordinates in p.
	 */
	private String getResizeHandleDir(WPoint p) {
		if (resizeHandles != null) {
			for (String d: resizeHandles.keySet()) {
				Rectangle r = resizeHandles.get(d);
				if (r.contains(p.getWysiwygPoint()))
					return d;
			}
		}
		return null;
	}
	
	/** Find the closest token in the active text rectangle given a set of
	 *  click coordinates.
	 */
	public WToken findClosestTextToken(WPoint p) {
		// find the closest token on this page and return it
		if (trTokens != null)
			return trTokens.findClosestTextToken(p, stepNonTextTags);
		return null;
	}
	
	/** Find the rectangle (text or color, depending on the current mode)
	 *  under the WPoint p.
	 */
	private WgRectangle findRectangle(WPoint p) {
		if (inTextRectMode()) {
			// find the closest text rectangle on the selected page and return
			for (WgTextRect tr: textRects) {
				if (!tr.isWholeSign() && tr.isNear(p))
					return tr;
			}
		} else if (inColorRectMode()) {
			for (WgColorRect cr: colorRects) {
				if (cr.isNear(p))
					return cr;
			}
		}
		return null;
	}
	
	/** Find the graphic under the WPoint p. */
	private WtGraphic findGraphic(WPoint p) {
		if (inGraphicMode()) {
			for (WToken t: graphics) {
				WtGraphic g = (WtGraphic) t;
				if (g.isInside(p)) {
					println("Found %s", g.toString());
					return g;
				}
			}
		}
		return null;
	}
	
	/** Set the drag mode based on the current editor mode and the position of
	 *  the mouse pointer p (and what it's over/in). The current drag mode is
	 *  indicated by the current cursor.
	 */
	private void setDragMode(WPoint p) {
		// check the current mode
		if (inTextRectMode() || inColorRectMode()) {
			String hDir = null;
			
			// if we have a selected rectangle, we can set the cursor to show
			// resize icons
			if (selectedRectangle != null)
				// if we have a selected rectangle, check where the mouse
				// pointer is WRT the resize handles
				hDir = getResizeHandleDir(p);
			
			// either way, we can use any rectangle to set the other cursor
			// types
			if (hDir != null) {
				// if we're on a resize handle, change the mouse cursor to
				// this direction
				setCursor(resizeCursors.get(hDir));
				resizeDir = hDir;
			} else {
				WgRectangle r = findRectangle(p);
				if (r != null) {
					if (r.isOnBorder(p) || inColorRectMode())
						// if the point is on the border but not a handle, use
						// the move cursor
						setCursor(moveCursor);
					else if (inTextRectMode())
						// if it's not on the border it must be near
						// (otherwise r would be null)
						setCursor(textCursor);
				} else
					// otherwise use the default for this mode - crosshair to
					// create a new rectangle
					setCursorFromMode();
			}
		} else if (inGraphicMode()) {
			// check if we're on a graphic - if so, use the move cursor
			if (selectedGraphic != null)
				setCursor(moveCursor);
			else
				setCursorFromMode();
		} else { // MULTI tag mode or text mode
			// NOTE - MULTI tag mode will probably just be like text mode (text
			// cursor only and always)
			setCursor(textCursor);
		}
		
		
	}
	
	/** Determine if the x-coordinate of p is on the left half of token tok.*/
	private boolean leftHalf(WPoint p, WToken tok) {
		return p.getSignX() < tok.getCentroidX();
	}	
	
	/** Determine if the x-coordinate of p is on the right half of token tok.*/
	private boolean rightHalf(WPoint p, WToken tok) {
		return p.getSignX() >= tok.getCentroidX();
	}
	
	/** Toggle non-text char mode for handling of non-text tag */
	public Action toggleNonTextTagMode = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			stepNonTextTags = !stepNonTextTags;
			updateNonTextTagInfo();
			signPanel.requestFocusInWindow();
		}
	};
	
	/** Initialize mouse cursors available to the GUI. */
	private void initCursors() {
		textCursor = new Cursor(Cursor.TEXT_CURSOR);
		graphicCursor = new Cursor(Cursor.HAND_CURSOR);
		newRectCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
		multiTagCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		moveCursor = new Cursor(Cursor.MOVE_CURSOR);
		
		// initialize resize cursors in a HashMap to make them easy to access
		resizeCursors = new HashMap<String, Cursor>();
		resizeCursors.put(WgRectangle.N, new Cursor(Cursor.N_RESIZE_CURSOR));
		resizeCursors.put(WgRectangle.S, new Cursor(Cursor.S_RESIZE_CURSOR));
		resizeCursors.put(WgRectangle.E, new Cursor(Cursor.E_RESIZE_CURSOR));
		resizeCursors.put(WgRectangle.W, new Cursor(Cursor.W_RESIZE_CURSOR));
		resizeCursors.put(WgRectangle.NE, new Cursor(Cursor.NE_RESIZE_CURSOR));
		resizeCursors.put(WgRectangle.NW, new Cursor(Cursor.NW_RESIZE_CURSOR));
		resizeCursors.put(WgRectangle.SE, new Cursor(Cursor.SE_RESIZE_CURSOR));
		resizeCursors.put(WgRectangle.SW, new Cursor(Cursor.SW_RESIZE_CURSOR));
		
		// set the cursor mode based on the current editor mode
		setCursorFromMode();
	}
	
	/** Initialize the caret. If there is text in the message, it is placed at
	 *  the beginning of the message (before the first printable character).
	 *  If there is no text, it goes at the end of the page.
	 */
	private void initCaret() {
		// if the page isn't empty, put the caret the first text character
		// (note that it is always text mode when this is called)
		if (!trTokens.isEmpty()) {
			WToken tok = trTokens.findFirstTextToken();
			if (tok != null)
				moveCaret(tok);
			else
				// put caret after last token if no text characters
				moveCaret(trTokens.getLast(), true);
		} else {
			// if it is, just update and it will go to EOP
			updateCaret();
		}
	}
	
	/** Update text selection given the token indices provided (set by mouse
	 *  events). 
	 */
	private void updateTokenListsSelection(int si, int ei,
			boolean includeEnd) {
		// slice the token list into 3
		if (includeEnd)
			++ei;
		tokensBefore = trTokens.slice(0, si);
		tokensAfter = trTokens.slice(ei, trTokens.size());
		tokensSelected = trTokens.slice(si, ei);
		
//		printCaretTokens();
	}

	/** Return the next printable text token in the list after index si 
	 *  (inclusive - if the token at si is printable text, it is returned).
	 *  If no tokens are found, null is returned.
	 */
	public WToken findNextTextToken(int si) {
		if (trTokens != null)
			return trTokens.findNextTextToken(si);
		return null;
	}
	
	/** Return the previous printable text token in the list after index si
	 *  (inclusive - if the token at si is printable text, it is returned).
	 *  If no tokens are found, null is returned.
	 */
	public WToken findPrevTextToken(int si) {
		if (trTokens != null)
			return trTokens.findPrevTextToken(si);
		return null;
	}
	
	/** Move the caret given a mouse pointer coordinate indicated by the
	 *  WPoint p. 
	 */
	public void moveCaret(WPoint p) {
		// find the closest text token
		WToken tok = findClosestTextToken(p);
		
		// move the caret based on the token we got
		if (tok != null) {
			println("Selected token: %s", tok.toString());
			
			// if the page is empty, just update and it will go to EOP
			if (selectedPage.isEmpty()) {
				updateCaret();
			} else if (rightHalf(p, tok)) {
				// if they clicked on the right half of the token, put the
				// to the right of that token
				moveCaret(trTokens.indexOf(tok) + 1);
			} else
				// otherwise just move the caret to this token
				moveCaret(tok);
		}
	}
	
	/** Move the caret to the spot just before the specified token. Note that
	 *  this doesn't select the token.
	 */
	public void moveCaret(WToken tok) {
		// get a list of tokens on this page and find this token in the list
		int tokIndx = trTokens.indexOf(tok);
		
		// dispatch to the other moveCaret method
		moveCaret(tokIndx);
	}

	/** Move the caret to the spot just after the specified token. Note that
	 *  this doesn't select the token.
	 */
	public void moveCaret(WToken tok, boolean toRight) {
		// get a list of tokens on this page and find this token in the list
		int tokIndx = trTokens.indexOf(tok);
		
		// dispatch to the other moveCaret method
		moveCaret(tokIndx+1);
	}
	
	/** Move the caret to the spot just before the specified token index. Note 
	 *  that this does not select the token.
	 *  
	 *  If tokIndx is -1, the caret is moved to the end of the page.
	 */
	public void moveCaret(int tokIndx) {
		caretIndx = tokIndx;
		updateCaret();
	}
	
	/** Update the caret location by moving it to the location defined by
	 *  caretIndx and caretAtEnd.
	 */
	public void updateCaret() {
		// only place the caret if we have a selected text rectangle
		if (selectedTextRect != null) {
			WToken cTok = null;
			boolean toRight = false;
			
			println("Caret at %d", caretIndx);
			
			// slice the list
			if (caretIndx >= trTokens.size())
				caretIndx = trTokens.size();
			tokensBefore = trTokens.slice(0, caretIndx);
			tokensAfter = trTokens.slice(caretIndx, trTokens.size());
			
			// set the new caret location
			cTok = getCaretToken();
			
			if (cTok != null)
				toRight = tokensAfter.isEmpty();
			else if (!selectedTextRect.isWholeSign())
				// if we didn't get a token, the current text rectangle is
				// empty - if this is an actual text rectangle, use the
				// WtTextRectangle token
				cTok = selectedTextRect.getRectToken();
			
			if (cTok != null)
				signPanel.setCaretLocation(cTok, toRight);
			else {
				println("Caret at EOP");
				tokensBefore = trTokens;
				tokensAfter.clear();;
				int x = selectedPage.getEOPX();
				int y = selectedPage.getEOPY();
				int h = selectedPage.getEOPH();
				signPanel.setCaretLocation(x, y, h);
			}
			
			// a click resets the selection
			tokensSelected.clear();
			signPanel.clearTextSelection();
			
			printCaretTokens();
			
			// TODO need to organize this better somehow
			updateTextToolbar();
			updateNonTextTagInfo();
		} else
			// if there is no selected text rectangle, there is no caret
			clearCaret();
	}
	
	/** Clear the caret from the screen and reset relevant token lists. */
	private void clearCaret() {
		tokensBefore.clear();
		tokensAfter.clear();
		tokensSelected.clear();
		signPanel.hideCaret();
		signPanel.clearTextSelection();
	}
	
	/** Get the token associated with the current caret position. This is
	 *  either the first token after the caret or the last token before
	 *  the caret, or null if neither is valid. */
	private WToken getCaretToken() {
		if (tokensAfter.isEmpty() && !tokensBefore.isEmpty()) {
			return tokensBefore.getLast();
		} else if (!tokensAfter.isEmpty()) {
			return tokensAfter.get(0);
		}
		return null;
	}
	
	/** Get the index of the token associated with the current caret position
	 *  (obeying stepNonTextTokens) on the selected page.
	 */
	private int getCaretIndexOnPage(boolean obeyStepNonText) {
		int i = getCaretIndexInList(
				selectedPage.getTokenList(), obeyStepNonText);
		
		// if we get -1, the current text rectangle is empty
		if (i == -1) {
			if (selectedTextRect.isWholeSign())
				// the entire page is empty - 0 is fine
				i = 0;
			else {
				// otherwise add after the WtTextRectangle token
				i = selectedPage.getTokenIndex(
						selectedTextRect.getRectToken()) + 1;
			}
		}
		return i;
	}
	
	/** Get the index of the token associated with the current caret position
	 *  (obeying stepNonTextTokens) in the active text rectangle.
	 */
	private int getCaretIndexInTextRect(boolean obeyStepNonText) {
		int i = getCaretIndexInList(trTokens, obeyStepNonText);
		
		// if we get -1, the current text rectangle is empty
		if (i == -1)
			i = 0;
		
		return i;
	}
	
	/** Get the index of the token associated with the current caret position
	 *  (obeying stepNonTextTokens if requested) in the given list. Returns -1
	 *  if no suitable reference token can be found.
	 */
	private int getCaretIndexInList(WTokenList tokList,
			boolean obeyStepNonText) {
		// figure out where on the page to add the token
		int i = -1;
		
		// try to find a reference token after the caret
		if (!tokensAfter.isEmpty()) {
			if (stepNonTextTags || !obeyStepNonText)
				i = tokList.indexOf(tokensAfter.get(0));
			else {
				// this *should* be the first token in the list, but maybe not
				WToken t = tokensAfter.findFirstTextToken();
				if (t != null)
					i = tokList.indexOf(t);
			}
		}
		
		// if we didn't get one, try before the caret
		if (i == -1 && !tokensBefore.isEmpty())
			i = tokList.indexOf(tokensBefore.getLast()) + 1;
		
		// if we still didn't get one and we're restricting to text tokens,
		// try before the first non-text token after
		if (i == -1 && !tokensAfter.isEmpty())
			// try before the first non-text token
			i = tokList.indexOf(tokensAfter.get(0));
		
		return i;
	}
	
	/** Action to move caret to left (using left arrow key) */
	public Action moveCaretLeft = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (!trTokens.isEmpty()) {
				// check the navigation mode
				if (stepNonTextTags) {  // go through all tokens
					if (caretIndx >= 1)
						moveCaret(caretIndx-1);
				} else {  // skip any non-text tokens
					int nextIndx = Math.max(caretIndx-1, 0);
					WToken textTok = findPrevTextToken(nextIndx);
					if (textTok != null)
						moveCaret(textTok);
					else
						updateCaret();
				}
			} else
				updateCaret();
		}
	};
	
	/** Action to move caret to right (using right arrow key) */
	public Action moveCaretRight = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (!trTokens.isEmpty()) {
				// check the navigation mode
				if (stepNonTextTags) {  // go through all tokens
					if (caretIndx < trTokens.size())
						moveCaret(caretIndx+1);
				} else {  // skip any non-text tokens
					// find the next text token, if there is one
					WToken textTok = findNextTextToken(caretIndx+1);
					println("Found %s", textTok);
					if (textTok != null)
						moveCaret(textTok);
					else if (findNextTextToken(caretIndx) != null)
						// if there's nothing remaining, go to the right of
						// the last text token
						moveCaret(caretIndx+1);
				}
			}
		}
	};
	
	/** Action to move caret up one line (using up arrow key) */
	public Action moveCaretUp = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the current token
			WToken tok = getCaretToken();
			
			// get the line this token is on
			int li = trTokens.getLineIndex(tok);
			if (li > 0) {
				// if this isn't the first line, find the closest token (based
				// on the X coordinate) on the next line up
				WToken upTok = getClosestTokenOnLine(li-1, tok.getCoordX());
				
				// if we got a valid token, move the caret up (otherwise do
				// nothing)
				if (upTok != null) {
					moveCaret(upTok);
				}
			}
		}
	};
	
	/** Action to move caret up one line (using up arrow key) */
	public Action moveCaretDown = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the current token
			WToken tok = getCaretToken();
			
			// get the line this token is on
			int li = trTokens.getLineIndex(tok);
			if (li < trTokens.getNumLines()-1) {
				// if this isn't the last line, find the closest token (based
				// on the X coordinate) on the next line down
				WToken downTok = getClosestTokenOnLine(li+1, tok.getCoordX());
				
				// if we got a valid token, move the caret up (otherwise do
				// nothing)
				if (downTok != null) {
					moveCaret(downTok);
				}
			}
		}
	};
	
	/** Action to move caret to the beginning of the current line (home key) */
	public Action moveCaretLineBeginning = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the current token and find what line it's on
			WToken tok = getCaretToken();
			WTokenList lineTokens = trTokens.getTokenLine(tok);
			
			if (lineTokens != null) {
				WToken homeTok;
				if (stepNonTextTags)
					// grab the first token on the line
					homeTok = lineTokens.get(0);
				else
					// grab the first text token
					homeTok = lineTokens.findFirstTextToken();
				
				// move the caret to that token
				moveCaret(homeTok);
			}
		}
	};
	
	/** Action to move caret to the end of the current line (end key) */
	public Action moveCaretLineEnd = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the current token and find what line it's on
			WToken tok = getCaretToken();
			WTokenList lineTokens = trTokens.getTokenLine(tok);
			
			if (lineTokens != null) {
				WToken endTok;
				if (stepNonTextTags)
					// grab the last token on the line
					endTok = lineTokens.get(lineTokens.size()-1);
				else {
					endTok = lineTokens.findLastTextToken();
				}
				
				// move the caret to the right of that token (unless it's a
				// newline)
				moveCaret(endTok, !endTok.isType(WTokenType.newLine));
			}
		}
	};
	
	/** Return the closest token on the line with index lineIndx to the x
	 *  coordinate provided (in sign coordinates.
	 */
	private WToken getClosestTokenOnLine(int lineIndx, int sx) {
		WTokenList line = trTokens.getLines().get(lineIndx);
		Iterator<WToken> it = line.iterator();
		int minDist = 999999;
		WToken tok = null;
		while (it.hasNext()) {
			WToken t = it.next();
			int xd = Math.abs(t.getCoordX() - sx);
			if ((t.isPrintableText() || stepNonTextTags) && xd < minDist) {
				minDist = xd;
				tok = t;
			}
		}
		return tok;
	}
	
	/** Action triggered with the backspace key. */
	public Action backspace = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (!tokensSelected.isEmpty()) {
				// if we have a selection, delete the selection
				deleteSelection(true);
			} else {
				// if we don't have any selection, delete the token just
				// before the caret
				// delete the last token in this list if possible
				// (otherwise just don't do anything)
				if (!tokensBefore.isEmpty()) {
					// get the previous token and it's index in the page's list
					WToken tok = null;
					if (stepNonTextTags)
						tok = tokensBefore.getLast();
					else
						tok = tokensBefore.findLastTextToken();
					
					if (tok != null) {
						int i = selectedPage.getTokenIndex(tok);
						println("Deleting prev token '%s' at index %d",
								tok.toString(), i);
						
						// save the current MULTI string then remove the token
						saveState();
						selectedPage.removeToken(i);
						
						// NOTE that we don't need to mess with finding the
						// "next" token - whatever happens next will deal
						if (caretIndx > 0)
							--caretIndx;
						
						// update everything
						update();
						updateCaret();
					}
				}
			}
		}
	};
	
	/** Action triggered with the delete key. */
	public Action delete = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (!tokensSelected.isEmpty()) {
				// if we have a selection, delete the selection
				deleteSelection(true);
			} else if ((inTextRectMode() || inColorRectMode()) &&
					selectedRectangle != null && tokensAfter.isEmpty()
					&& tokensBefore.isEmpty()) {
				// if we have a selected rectangle and no caret (indicated by
				// the empty token lists), delete the rectangle
				deleteSelectedRectangle();
			} else if (inGraphicMode() && selectedGraphic != null) {
				deleteSelectedGraphic();
			} else {
				// if we don't have any selection, delete the token just
				// after the caret
				// delete the first token in this list if possible
				// (otherwise just don't do anything)
				if (!tokensAfter.isEmpty()) {
					// get the next token and it's index in the page's list
					WToken tok = null;
					if (stepNonTextTags)
						tok = tokensAfter.remove(0);
					else
						tok = tokensAfter.findFirstTextToken();
					
					if (tok != null) {
						int i = selectedPage.getTokenIndex(tok);
						println("Deleting token '%s' at index %d",
								tok.toString(), i);
						
						// save the current MULTI string then remove the token
						saveState();
						selectedPage.removeToken(i);
						
						// update everything
						update();
						updateCaret();
					}
				}
			}
			
		}
	};
	
	/** Delete all tokens in the selection. If saveForUndo is true, the state
	 *  is saved before deleting the tokens. */
	private void deleteSelection(boolean saveForUndo) {
		// TODO save the selection to reset it after undoing
		if (saveForUndo)
			saveState();
		
		// now find and delete each token, then clear the selection and update
		for (WToken tok: tokensSelected) {
			int i = selectedPage.getTokenIndex(tok);
			println("Removing token '%s' from %d", tok.toString(), i);
			selectedPage.removeToken(i);
		}
		tokensSelected.clear();
		
		// figure out where to put the caret
//		caretIndx = getCaretIndexInTextRect();
		
		// update then move the caret
		update();
		updateCaret();
	}
	
	/** Delete the selected graphic. */
	private void deleteSelectedGraphic() {
		if (inGraphicMode() && selectedGraphic != null) {
			saveState();
			selectedPage.removeToken(selectedGraphic);
			update();
			updateCaret();
		}
	}
	
	/** Delete the selected rectangle. If the selected rectangle is a text
	 *  rectangle, the tokens inside the rectangle will be deleted as well.
	 */
	private void deleteSelectedRectangle() {
		if (inTextRectMode() || inColorRectMode()
				&& removeSelectedRectangleTokens(true) != null) {
			update();
			updateCaret();
		}
	}
	
	/** Move the selected region (rectangle or graphic) forwards on the page
	 *  by moving it later in the MULTI string.
	 */
	public Action moveSelectedRegionForward = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the token that needs to move
			WToken movTok = null;
			if (selectedRectangle != null
					&& (inTextRectMode() || inColorRectMode())) {
				movTok = selectedRectangle.getRectToken();
			} else if (selectedGraphic != null && inGraphicMode())
				movTok = selectedGraphic;
			
			if (movTok != null) {
				// now figure out where to move it - get the index of the next
				// text rectangle, color rectangle or graphic after this one
				int ti = selectedPage.getTokenIndex(movTok);
				WToken nt = selectedPage.findNextTokenOfType(ti + 1,
						WTokenType.textRectangle, WTokenType.colorRectangle,
						WTokenType.graphic);
				
				println("Moving region %s forward from %d in %s to after %s",
						movTok.toString(), ti,
						selectedPage.getTokenList().toString(),
						nt != null ? nt.toString() : "end");
				
				// get the tokens for moving (removing from previous location)
				// do it now so the indices we get next are correct
				WTokenList mToks = removeSelectedRegionTokens();
				println("Moving token(s) %s", mToks.toString());
				
				// if there is a next token, move after it
				int ni = -1;
				if (nt != null) {
					if (nt.isType(WTokenType.textRectangle)) {
						// if it's a text rectangle, we need to get the last
						// token in that text rectangle
						for (WgTextRect tr: textRects) {
							if (tr.getRectToken() == nt) {
								nt = tr.getLastToken();
								break;
							}
						}
					}
					// get the index of the token
					ni = selectedPage.getTokenIndex(nt);
				} else
					// if there isn't, add at the end of the page
					ni = selectedPage.getNumTokens() - 1;
				
				println("Moving to %d in %s \n", ni,
						selectedPage.getTokenList().toString());
				
				// now put them in the new place
				if (mToks != null && !mToks.isEmpty() && ni != -1) {
					for (WToken t: mToks)
						selectedPage.addToken(++ni, t);
					
					// update everything
					updateAfterRegionMove(mToks.get(0));
				}
			}
		}
	};

	/** Move the selected region (rectangle or graphic) backwards on the page
	 *  by moving it earlier in the MULTI string.
	 */
	public Action moveSelectedRegionBackward = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the token that needs to move
			WToken movTok = null;
			if (selectedRectangle != null
					&& (inTextRectMode() || inColorRectMode())) {
				movTok = selectedRectangle.getRectToken();
			} else if (selectedGraphic != null && inGraphicMode())
				movTok = selectedGraphic;
			
			if (movTok != null) {
				// now figure out where to move it - get the index of the next
				// text rectangle, color rectangle or graphic before this one
				int ti = selectedPage.getTokenIndex(movTok);
				WToken pt = selectedPage.findPrevTokenOfType(ti - 1,
						WTokenType.textRectangle, WTokenType.colorRectangle,
						WTokenType.graphic);

				println("Moving region %s backward from %d in %s to before %s",
						movTok.toString(), ti,
						selectedPage.getTokenList().toString(),
						pt != null ? pt.toString() : "beginning");
				
				// get the tokens for moving (removing from previous location)
				WTokenList mToks = removeSelectedRegionTokens();
				println("Moving token(s) %s", mToks.toString());
				
				// if there is a previous token, move before it
				int ni = -1;
				if (pt != null)
					// get the index of the token
					ni = selectedPage.getTokenIndex(pt);
				else
					// if there isn't, add at the beginning of the page
					ni = 0;
				
				println("Moving to %d in %s \n", ni,
						selectedPage.getTokenList().toString());
				
				// now put them in the new place
				if (mToks != null && !mToks.isEmpty() && ni != -1) {
					for (WToken t: mToks)
						selectedPage.addToken(ni++, t);
					
					// update everything
					updateAfterRegionMove(mToks.get(0));
				}
			}
		}
	};
	
	/** Remove and return all tokens associated with the currently selected
	 *  region (rectangle or graphic). Returns a WTokenList of the tokens that
	 *  were removed, or null if no tokens were removed.
	 */
	private WTokenList removeSelectedRegionTokens() {
		WTokenList mToks = null;
		if (selectedRectangle != null)
			// rectangles and stuff inside (also saves state)
			mToks = removeSelectedRectangleTokens(true);
		else if (selectedGraphic != null) {
			saveState();
			mToks = new WTokenList();
			mToks.add(selectedGraphic);
			selectedPage.removeToken(selectedGraphic);
		}
		return mToks;
	}
	
	/** Remove and return all tokens associated with the currently selected
	 *  rectangle. Returns a WTokenList of the tokens that were removed,
	 *  or null if no tokens were removed.
	 */
	private WTokenList removeSelectedRectangleTokens(boolean saveForUndo) {
		if (selectedRectangle != null) {
			Wt_Rectangle rt = selectedRectangle.getRectToken();
			// only do this for real rectangles (not the "whole-sign" TR)
			
			if (rt != null) {
				// make an undo point if requested
				if (saveForUndo)
					saveState();
				
				WTokenList rToks = new WTokenList();
				
				// remove the rectangle token
				selectedPage.removeToken(rt);
				rToks.add(rt);
				
				// if it's a text rectangle, delete any tokens inside
				if (selectedRectangle instanceof WgTextRect) {
					for (WToken t: selectedTextRect.getTokenList()) {
						selectedPage.removeToken(t);
						rToks.add(t);
					}
				}
				return rToks;
			}
		}
		return null;
	}
	
	/** Update the selected rectangle or selected graphic index after moving
	 *  forward/backward using the text rectangle, color rectangle, or graphic
	 *  token rt. Also updates everything after finding the new index.
	 */
	private void updateAfterRegionMove(WToken rt) {
		// update the rectangles and graphics on the page so we
		// can get the updated index
		updateRectangles();
		updateGraphics();
		
		// use the [tr/cr/g...] token to figure out the new index
		if (rt instanceof Wt_Rectangle) {
			// find the WgRectangle for this token
			for (WgRectangle r: modeRects) {
				if (r.getRectToken() == rt) {
					setSelectedRectangle(r);
					break;
				}
			}
		} else if (rt instanceof WtGraphic)
			setSelectedGraphic((WtGraphic) rt);
		
		// now we can update everything
		update();
		updateCaret();
	}
	
	/** Add a single ASCII character to the message at the caret location. */
	public void typeChar(char c) {
		// only add if there is an active text rectangle, otherwise do nothing
		if (selectedTextRect != null) {
			println("Typed: '%c'", c);
			
			// TODO handle overwrite mode somehow (default is insert mode)
			
			// save our state so we can undo
			saveState();
			
			// if there was a selection, delete it first
			if (!tokensSelected.isEmpty())
				deleteSelection(false);
			
			// first create a token from the character
			WtTextChar t = new WtTextChar(c);
			if (t.isValid()) {
				addToken(t);
			}
		}
	}
	
	/** Add a newline character at the current position */
	public Action addNewLine = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// save state to allow undo
			saveState();
			
			// create a newline token
			// TODO how to deal with spacing?? just doing default for now
			WtNewLine t = new WtNewLine(null);
			addToken(t);
		}
	};
	
	/** Add a page after the selected page. */
	public Action pageAdd = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// save state to allow undoing
			saveState();
			
			// add a page to the wmsg after the selected page then update
			// increment the selected page index so the new page is selected 
			WPage pg = new WPage(wmsg);
			++selectedPageIndx;
			wmsg.addPage(selectedPageIndx, pg);
			update();
			initCaret();
		}
	};
		
	/** Delete the selected page. */
	public Action pageDelete = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// save state to allow undoing
			saveState();
			
			// delete the selected page from the wmsg then update
			wmsg.removePage(selectedPageIndx);
			update();
			initCaret();
		}
	};
	
	/** Delete the selected page. */
	public Action pageMoveUp = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (selectedPageIndx > 0) {
				// save state to allow undoing
				saveState();
				
				// remove the selected page from the wmsg then move it up one
				WPage pg = wmsg.removePage(selectedPageIndx);
				
				// decrement the page index so the same page stays selected
				--selectedPageIndx;
				wmsg.addPage(selectedPageIndx, pg);
				update();
			}
		}
	};
	
	/** Delete the selected page. */
	public Action pageMoveDown = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (selectedPageIndx < wmsg.getNumPages()-1) {
				// save state to allow undoing
				saveState();
				
				// remove the selected page from the wmsg then move it up one
				WPage pg = wmsg.removePage(selectedPageIndx);

				// increment the page index so the same page stays selected
				++selectedPageIndx;
				
				wmsg.addPage(selectedPageIndx, pg);
				update();
			}
		}
	};
	
	/** Get the last token of type tokType (i.e. closest token behind the
	 *  caret). Used for for determining the active font/color/justification
	 *  used to set the respective button on the toolbar. If one isn't found,
	 *  null is returned (and the caller should use the default).
	 */
	public WToken getPrecedingTokenOfType(WTokenType tokType) {
		// get the list of tokens before the caret anywhere on the page (since
		// the tags we're looking for persist across text rectangles)
		int pgi = getCaretIndexOnPage(false);
		if (pgi >= selectedPage.getNumTokens())
			pgi = selectedPage.getNumTokens() - 1;
		
		if (pgi > 0) {
			// look through the tokens to find any tokens of this type
			for (int i = pgi; i >= 0; --i) {
				WToken tok = selectedPage.getTokenList().get(i);
				if (tok.isType(tokType))
					// return the first one we find
					return tok;
			}
		}
		// or null otherwise
		return null;
	}
	
	/** Get the list of tokens matching type tokType in the current selection.
	 *  Used for determining the active font/color/justification used to set
	 *  the respective buttons on the toolbar. If none are found, an empty
	 *  list is returned.
	 */
	public WTokenList getTokensOfTypeInSelection(WTokenType tokType) {
		// look in the tokensSelected to find any tokens of this type
		WTokenList toks = new WTokenList();
		for (WToken tok: tokensSelected) {
			if (tok.isType(tokType))
				toks.add(tok);
		}
		return toks;
	}
	
	/** Get the active page font given the current caret location. Uses font
	 *  tags in the preceding and/or selected tokens and the default to
	 *  determine what the "active" font value is.
	 */
	public Integer getActiveFont() {
		// first look in the preceding tokens
		WtFont pfTag = (WtFont) getPrecedingTokenOfType(
				WTokenType.font);
		
		// use the font number to determine likeness
		// TODO do we need to include the version ID??
		int pfNum = (pfTag != null) ? pfTag.getFontNum()
				: defaultFont.getNumber();
		
		// now look in the selected tokens
		WTokenList selJusts = getTokensOfTypeInSelection(
				WTokenType.font);
		
		// now check that every token is the same type
		HashSet<Integer> jpvals = new HashSet<Integer>();
		jpvals.add(pfNum);
		for (WToken tok: selJusts)
			jpvals.add(((WtFont) tok).getFontNum());
		
		if (jpvals.size() == 1)
			return pfNum;
		return null;
	}
	
	/** Add a font tag at the current location. */
	public void setFont(Font f) {
		// set the font
		font = f;
		
		// create the appropriate font tag token then add it
		// TODO I think it's ok to do this with the font ID
		WtFont fTok = new WtFont(font.getNumber(),
				String.valueOf(font.getVersionID()));
		println("Adding font tag at %d", caretIndx);
		addTextOptionToken(fTok);
		
		// update the toolbar
		updateTextToolbar();
	}
	
	/** Add a foreground color token at the current location. */
	public void setForegroundColor(DmsColor c) {
		// TODO do something with default colors?
		
		// set the foreground color
		fgColor = c;
		
		// create the appropriate WtColorForeground token then add it
		WtColorForeground cfTok = new WtColorForeground(
				c.red, c.green, c.blue);
		addTextOptionToken(cfTok);
	}
	
	/** Add a background color token at the beginning of the page. Uses a page
	 *  background color tag and not the deprecated color background tag. */
	public void setBackgroundColor(DmsColor c) {
		// TODO do something with default colors?
		
		// set the background color
		bgColor = c;
		
		// clear any page background tokens that may already be on the page
		clearPageTokenType(WTokenType.pageBackground);
		
		// save state, create the appropriate WtColorForeground token and
		// add it, then update and move the caret to reflect the change
		saveState();
		WtPageBackground cbTok = new WtPageBackground(
				c.red, c.green, c.blue);
		selectedPage.addToken(0, cbTok);
		update();
		moveCaretRight.actionPerformed(null);
	}
	
	/** Set the color value of the currently selected color rectangle. Also
	 *  sets the active color rectangle color so new ones created after this
	 *  use the same color
	 */
	public void setColorRectangleColor(DmsColor c) {
		colorRectColor = c;
		
		// check if we have an active color rectangle
		if (selectedRectangle instanceof WgColorRect) {
			// if we do, save state, set the color, and update
			saveState();
			((WgColorRect) selectedRectangle).setColor(colorRectColor);
			update();
		}
	}
	
	/** Get the active page justification value given the current caret
	 *  location. Uses page justification tags in the preceding and/or
	 *  selected tokens and the default to determine what the "active" page
	 *  justification value is.
	 */
	public JustificationPage getActivePageJustification() {
		// first look in the preceding tokens
		WtJustPage pjTag = (WtJustPage) getPrecedingTokenOfType(
				WTokenType.justificationPage);
		
		// the just. based on this is either the tag value or the default
		JustificationPage pj = (pjTag != null) ? pjTag.getJustification()
				: JustificationPage.TOP;
		
		// now look in the selected tokens
		WTokenList selJusts = getTokensOfTypeInSelection(
				WTokenType.justificationPage);
		
		// now check that every token is the same type
		HashSet<JustificationPage> jpvals = new HashSet<JustificationPage>();
		jpvals.add(pj);
		for (WToken tok: selJusts)
			jpvals.add(((WtJustPage) tok).getJustification());
		
		if (jpvals.size() == 1)
			return pj;
		return null;
	}
	
	/** Get the active page justification value given the current caret
	 *  location. Uses page justification tags in the preceding and/or
	 *  selected tokens and the default to determine what the "active" page
	 *  justification value is.
	 */
	public JustificationLine getActiveLineJustification() {
		// first look in the preceding tokens
		WtJustLine pjTag = (WtJustLine) getPrecedingTokenOfType(
				WTokenType.justificationLine);
		
//		println("Preceding token: %s", (pjTag != null) ? pjTag.toString() : "null");
		
		// the just. based on this is either the tag value or the default
		JustificationLine pj = (pjTag != null) ? pjTag.getJustification()
				: JustificationLine.CENTER;
		
		// now look in the selected tokens
		WTokenList selJusts = getTokensOfTypeInSelection(
				WTokenType.justificationLine);
		
		// now check that every token is the same type
		HashSet<JustificationLine> jlvals = new HashSet<JustificationLine>();
		jlvals.add(pj);
		for (WToken tok: selJusts)
			jlvals.add(((WtJustLine) tok).getJustification());
		
		if (jlvals.size() == 1)
			return pj;
		return null;
	}
	
	// TODO will remove this and do something else instead
	/** Get the active foreground color given the current caret location. Uses
	 *  foreground color tags in the preceding and/or selected tokens and the
	 *  default to determine what the "active" color is.
	 */
//	public Color getActiveForegroundColor() {
//		// first look in the preceding tokens
//		WtColorForeground pcTag = (WtColorForeground) getPrecedingTokenOfType(
//				WTokenType.colorForeground);
//		
//		// the color based on this is either the tag value or the default
//		Color pfgColor = (pcTag != null) ? pcTag.getColor()
//				: fgColorDefault.color;
//		
//		// now look in the selected tokens
//		WTokenList selColors = getTokensOfTypeInSelection(
//				WTokenType.colorForeground);
//		
//		// now check that every token is the same type
//		HashSet<Color> colors = new HashSet<Color>();
//		colors.add(pfgColor);
//		for (WToken tok: selColors) {
//			colors.add(((WtColorForeground) tok).getColor());
//		}
//		if (colors.size() == 1)
//			return pfgColor;
//		return null;
//	}

	/** Add a line justify left token at the current location. */
	public Action lineJustifyLeft = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// create the appropriate WtJustLine token then pass it to
			// addTextOptionToken
			WtJustLine jlTok = new WtJustLine(JustificationLine.LEFT);
			
			// this needs to be the first token on the line - move the caret
			// to the beginning of the line then add "manually"
			WToken tok = getCaretToken();
			WTokenList lineTokens = trTokens.getTokenLine(tok);
			
			if (lineTokens != null) {
				WToken homeTok = lineTokens.get(0);
				moveCaret(homeTok);
			}
			
			addLineJustifyToken(jlTok);
		}
	};

	/** Add a line justify center token at the current location. */
	public Action lineJustifyCenter = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// create the appropriate WtJustLine token then pass it to
			// addTextOptionToken
			WtJustLine jlTok = new WtJustLine(JustificationLine.CENTER);
			addLineJustifyToken(jlTok);
		}
	};
	
	/** Add a line justify center token at the current location. */
	public Action lineJustifyRight = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// create the appropriate WtJustLine token then pass it to
			// addTextOptionToken
			WtJustLine jlTok = new WtJustLine(JustificationLine.RIGHT);
			addLineJustifyToken(jlTok);
		}
	};
	
	/** Add a line justify tag at the current location. Checks for preceding
	 *  and following line justify tags on the same line and removes any 
	 *  conflicting tags them to avoid an error (assuming THIS is the token
	 *  the user wants).
	 */
	private void addLineJustifyToken(WtJustLine jlTok) {
		// first save the state so we can undo this
		saveState();
		
		// sanitize then add the token and update
		sanitizeLineJustifyTokens(jlTok);
		addToken(jlTok);
		updateTextToolbar();
	}
	
	/** Clear the area around the current caret of any conflicting line
	 *  justification tokens.
	 */
	private void sanitizeLineJustifyTokens(WtJustLine jlTok) {
		// look through the tokens BEFORE the caret and make sure there are no
		// line justification tags with a justification value HIGHER than this
		// one
		for (int i = 0; i < tokensBefore.size(); ++i) {
			if (i >= tokensBefore.size())
				break;
			
			WToken tok = tokensBefore.reversed().get(i);

//					println("BEFORE - on token: %s", tok.toString());
			
			// if we hit a newline, stop immediately
			if (tok.isType(WTokenType.newLine))
				break;
			
			if (tok.isType(WTokenType.justificationLine)) {
				// cast to a WtJustLine token to tell Java/Eclipse it's OK
				WtJustLine jlt = (WtJustLine) tok;
				
				// check the value against the tag we're trying to add
				int thisJL = jlt.getJustification().ordinal();
				int addJL = jlTok.getJustification().ordinal();
				if (thisJL >= addJL) {
					// if it's greater or the same, assume the user wants to
					// remove it
					selectedPage.removeToken(tok);
					if (caretIndx > 0)
						moveCaret(caretIndx-1);
				}
			}
		}

		// look through the tokens AFTER the caret and make sure there are no
		// line justification tags with a justification value LOWER than this
		// one
		for (int i = 0; i < tokensAfter.size(); ++i) {
			if (i >= tokensAfter.size())
				break;
			
			WToken tok = tokensAfter.get(i);
			
			// if we hit a newline, stop immediately
			if (tok.isType(WTokenType.newLine))
				break;
			
			if (tok.isType(WTokenType.justificationLine)) {
				// cast to a WtJustLine token to tell Java/Eclipse it's OK
				WtJustLine jlt = (WtJustLine) tok;
				
				// check the value against the tag we're trying to add
				int thisJL = jlt.getJustification().ordinal();
				int addJL = jlTok.getJustification().ordinal();
				if (thisJL <= addJL) {
					// if it's greater or the same, assume the user wants to
					// remove it
					selectedPage.removeToken(tok);
					updateCaret();
				}
			}
		}
		
		// now trim any line justification tokens in the immediate vicinity
		trimTextOptionTokens(WTokenType.justificationLine);
	}
	
	/** Add a page justify top token at the current location. */
	public Action pageJustifyTop = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// create the appropriate WtJustPage token then pass it to
			// addTextOptionToken
			WtJustPage jpTok = new WtJustPage(JustificationPage.TOP);
			addPageJustifyToken(jpTok);
		}
	};
	
	/** Add a page justify middle token at the current location. */
	public Action pageJustifyMiddle = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// create the appropriate WtJustLine token then pass it to
			// addTextOptionToken
			WtJustPage jpTok = new WtJustPage(JustificationPage.MIDDLE);
			addPageJustifyToken(jpTok);
		}
	};
	
	/** Add a page justify bottom token at the current location. */
	public Action pageJustifyBottom = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// create the appropriate WtJustLine token then pass it to
			// addTextOptionToken
			WtJustPage jpTok = new WtJustPage(JustificationPage.BOTTOM);
			addPageJustifyToken(jpTok);
		}
	};
	
	/** Add a page justify tag at the current location. Checks for preceding
	 *  and following page justify tags on the same page and removes any 
	 *  conflicting tags them to avoid an error (assuming THIS is the token
	 *  the user wants).
	 */
	private void addPageJustifyToken(WtJustPage jpTok) {
		// first save the state so we can undo this
		saveState();
		
		// look through the tokens BEFORE the caret and make sure there are no
		// page justification tags with a justification value HIGHER than this
		// one
		for (int i = 0; i < tokensBefore.size(); ++i) {
			if (i >= tokensBefore.size())
				break;
			
			WToken tok = tokensBefore.get(i);
			if (tok.isType(WTokenType.justificationPage)) {
				// cast to a WtJustPage token to tell Java/Eclipse it's OK
				WtJustPage jpt = (WtJustPage) tok;
				
				// check the value against the tag we're trying to add
				int thisJP = jpt.getJustification().ordinal();
				int addJP = jpTok.getJustification().ordinal();
				if (thisJP >= addJP) {
					// if it's greater or the same, assume the user wants to
					// remove it
					selectedPage.removeToken(tok);
					moveCaretLeft.actionPerformed(null);
				}
			}
		}
		
		// now look through the tokens AFTER the caret and make sure there
		// are no page justification tags with a justification value LOWER
		// than this one
		for (int i = 0; i < tokensAfter.size(); ++i) {
			if (i >= tokensAfter.size())
				break;
			
			WToken tok = tokensAfter.get(i);
			if (tok.isType(WTokenType.justificationPage)) {
				// cast to a WtJustPage token to tell Java/Eclipse it's OK
				WtJustPage jpt = (WtJustPage) tok;
				
				// check the value against the tag we're trying to add
				int thisJP = jpt.getJustification().ordinal();
				int addJP = jpTok.getJustification().ordinal();
				if (thisJP <= addJP) {
					// if it's lower or the same, assume the user wants to
					// remove it
					selectedPage.removeToken(tok);
					updateCaret();
				}
			}
		}
		
		// now trim any page justification tokens in the immediate vicinity
		trimTextOptionTokens(WTokenType.justificationPage);
		
		// finally, add the token and update everything
		addToken(jpTok);
		updateTextToolbar();
	}
	
	/** Clear the selected page of all tokens of type tokType. */
	private void clearPageTokenType(WTokenType tokType) {
		// loop over at most the number of tokens on the page
		for (int i = 0; i < selectedPage.getNumTokens(); ++i) {
			if (i >= selectedPage.getNumTokens())
				// account for the removal of tokens
				break;
			
			// get the token at this index and check the type
			WToken tok = selectedPage.getTokenList().get(i);
			if (tok.isType(tokType)) {
				// if it's the same type, remove it
				selectedPage.removeToken(i);
				
				// now adjust the caret appropriately
				if (i < caretIndx)
					// if we're to the left of the caret, move one to the left
					moveCaretLeft.actionPerformed(null);
				else
					// otherwise just update
					updateCaret();
			}
		}
	}

	/** Clear the selected text rectangle of all tokens of type tokType. */
	private void clearTrTokenType(WTokenType tokType) {
		// loop over at most the number of tokens in the text rectangle
		for (int i = 0; i < trTokens.size(); ++i) {
			if (i >= trTokens.size())
				// account for the removal of tokens
				break;
			
			// get the token at this index and check the type
			WToken tok = trTokens.get(i);
			if (tok.isType(tokType)) {
				// if it's the same type, remove it from the list and page
				trTokens.remove(tok);
				selectedPage.removeToken(tok);
				
				// now adjust the caret appropriately
				if (i < caretIndx)
					// if we're to the left of the caret, move one to the left
					moveCaretLeft.actionPerformed(null);
				else
					// otherwise just update
					updateCaret();
			}
		}
	}
	
	/** Clear the selected tokens of all tokens of type tokType.
	 *  @returns the number of tokens removed
	 */
	private void clearSelectionTokenType(WTokenType tokType) {
		// loop over at most the number of tokens in the selection
		int nRemoved = 0;
		for (int i = 0; i < tokensSelected.size(); ++i) {
			if (i >= tokensSelected.size())
				// account for the removal of tokens
				break;
			
			// get the token at this index and check the type
			WToken tok = tokensSelected.get(i);
			if (tok.isType(tokType)) {
				// if it's the same type, remove it
				tokensSelected.remove(i);
				
				// also remove it from the page
				selectedPage.removeToken(tok);
			}
		}
	}
	
	/** Add a text option token (justification, font, color) at the current
	 *  caret location. If the token(s) immediately preceding is/are the same
	 *  type of text option, it/they are replaced by the specified token
	 *  (otherwise it is just added). Also if any tokens in the current
	 *  selection are the same type, they are removed (since we assume that's
	 *  what the user wanted).
     */
	private void addTextOptionToken(WToken toTok) {
		// save state here so we can undo this whole process
		saveState();
		
		// trim any tokens of the same type around the current caret location
		// and/or in the selection
		trimTextOptionTokens(toTok.getType());
				
		// now add the token
		addToken(toTok);
	}
	
	/** Trim (remove) any tokens of the type tokType that are immediately
	 *  before the caret. If there is a selection, the selection is cleared of
	 *  tokens of this type as well. If there is no selection, tokens after
	 *  the caret will be trimmed instead.
	 */
	private void trimTextOptionTokens(WTokenType tokType) {
		// we will adjust how this works if there is a selection
		boolean haveSelection = !tokensSelected.isEmpty();
		
		// check the type of the preceding token to remove all immediately-
		// preceding tokens of the same type
		while (tokensBefore.size() > 0 &&
				tokensBefore.getLast().isType(tokType)) {
			WToken tok = tokensBefore.getLast();
			println("Last token: '%s'", tok.toString());
			
			// if it's the same, delete it (but don't create a separate undo
			// step)
			selectedPage.removeToken(tok);
			tokensBefore.remove(tok);
			
			// each token removed here will require the caret to move one to
			// the left
			if (caretIndx > 0)
				--caretIndx;
		}
		
		// if we have a selection, just clear it of these tokens
		if (!tokensSelected.isEmpty())
			clearSelectionTokenType(tokType);
		else {
			// if not, trim forwards
			while (tokensAfter.size() > 0 &&
					tokensAfter.get(0).isType(tokType)) {
				WToken tok = tokensAfter.get(0);
				println("Last token: '%s'", tok.toString());
				
				// if it's the same, delete it (but don't create a separate
				// undo step)
				selectedPage.removeToken(tok);
				
				// delete from the tokensAfter too
				tokensAfter.remove(tok);
				
			}
		}
	}
	
	/** Add the token to the selected page at the caret index. */
	private void addToken(WToken tok) {
		// figure out where on the page to add the token
//		int pgi = getCaretIndexOnPage(tok.isPrintableText());
		int pgi = getCaretIndexOnPage(false);
		
		// add the token at this location
		selectedPage.addToken(pgi, tok);
		
		// update then move the caret one more token (just after the
		// character that was just added)
		update();
		
		if (tok.isPrintableText())
			// this will follow the stepNonTextTags mode
			moveCaretRight.actionPerformed(null);
		else {
			// just added non-printable text - ignore stepNonTextTags
			if (caretIndx < trTokens.size())
				moveCaret(caretIndx+1);
		}
	}
	
	/** Save the current state on the stack for undoing. Resets the redo stack. */
	private void saveState() {
		WHistory wh = new WHistory(multiStringText, caretIndx,
				tokensSelected.size(), selectedPageIndx, fgColor, bgColor);
		undoStack.add(wh);
		
		// reset the redo stack, since reapplying those changes is not trivial
		// after another change is made
		redoStack.clear();
	}
	
	/** Undo the last message change by reverting to the previous MULTI string. */
	public Action undo = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
//			println("Starting undo...");
			if (!undoStack.isEmpty()) {
				// save the current state on the redo stack so the action can
				// be redone
				WHistory wh = new WHistory(multiStringText, caretIndx,
						tokensSelected.size());
				redoStack.add(wh);
				
				// pop the last string off the stack and apply it
				wh = undoStack.remove(undoStack.size()-1);
				applyHistory(wh);
			}
		}
	};
		
	/** Redo the last message change by reverting to the previous MULTI string. */
	public Action redo = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
//			println("Starting redo...");
			if (!redoStack.isEmpty()) {
				// put the current state back on the undo stack so the action can
				// be undone
				WHistory wh = new WHistory(multiStringText, caretIndx,
						tokensSelected.size());
				undoStack.add(wh);
				
				// pop the last string off the stack and apply it
				wh = redoStack.remove(redoStack.size()-1);
				applyHistory(wh);
			}
		}
	};
	
	/** Apply the historical state to the controller. */
	private void applyHistory(WHistory wh) {
		// apply the old MULTI string - this will change the re
		setMultiString(wh.getMultiString(), false);
		
		// TODO handle selection and selected page
		
		// TODO font
		
		// now the color
		
		update();
		
		moveCaret(wh.getCaretIndex());
	}
	
	/** Return a WPageList with selection disabled for previewing a message */
	public WPageList getPagePreviewList() {
		return new WPageList(false);
	}
	
	/** Return this controller's WPageList which is used for selecting pages */
	public WPageList getPageList() {
		if (pageList == null)
			makePageList();
		return pageList;
	}
	
	/** Create a WPageList for displaying a list of pages from the message */
	private void makePageList() {
		// create and save our page list with a selection handler
		class PageSelectionHandler implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
					int indx = lsm.getMinSelectionIndex();
					
					if (indx != -1) {
						selectedPageIndx = indx;
						updateSelectedPage();
					}
				}
			}
		}
		pageList = new WPageList(new PageSelectionHandler());
	}
	
	/** Get any message text in the controller */
	public String getMultiText() {
		return multiStringText;
	}
	
	public WEditorErrorManager getErrorManager() {
		return errMan;
	}
	
	/** Show the dynamic error panel for displaying current MULTI/renderer
	 *  errors on the editor form. This tab only appears when there are
	 *  errors.
	 */
	private void updateErrorPanel() {
		if (editor != null) {
			if (!editor.hasErrorPanel())
				editor.addErrorPanel(errMan);
			else
				editor.updateErrorPanel();
		}
	}
	
	/** Declare a "no error" state, i.e. a state with no MULTI/renderer
	 *  errors. Removes the error panel (if it is displayed) and saves a
	 *  record of this state to allow restoring.
	 */
	private void noErrors() {
		if (editor != null)
			editor.removeErrorPanel();
		
		// update the index of the last good state (the current size of the
		// undo stack)
		lastGoodState = undoStack.size();
//		println("No errors at %d", lastGoodState);
	}
	
	/** An action to restore the last good state of the editor (i.e. the last
	 *  state that had no MULTI/renderer errors) in a way that can be redone.
	 *  Lets the user to return to a good state if they are confused, while
	 *  allowing them to step back through their changes to help them find
	 *  the error to achieve whatever they wanted to do.
	 *  
	 *  Note that if there wasn't a good state, we're hosed. MULTI mode is the
	 *  only way back in that case (perhaps we should display an message
	 *  then??).
	 */
	public IAction restoreLastGoodState = new IAction("wysiwyg.epanel.restore") {
		public void doActionPerformed(ActionEvent e) {
			println("Restoring");
			if (lastGoodState >= 0) {
				println("Last good state: %d", lastGoodState);
				while (undoStack.size() > lastGoodState) {
					undo.actionPerformed(e);
				}
			}
		}
	};
	
	/** Render the message using the current MULTI String and MultiConfig */
	private void renderMsg() {
		// update the WMessage object and re-render if we have a MultiConfig
//		println("In renderMsg: " + multiStringText);
		if (wmsg == null) {
			if  (multiStringText != null)
//			println("Making wmsg with: " + multiStringText);
				wmsg = new WMessage(multiStringText);
		} else {
			// if we already have a WMessage object, use it to update the
			// MULTI string then use that to re-render
			multiStringText = wmsg.toString();
//			println("Remaking wmsg with: " + multiStringText);
			wmsg = new WMessage(multiStringText);
		}
//		System.out.println(multiStringText);
		
		// clear any errors before re-rendering
		errMan.clearErrors();
		
		if (multiConfig != null && wmsg != null) {
			wmsg.renderMsg(multiConfig, errMan);
			
			// set the WYSIWYG image size on the pages
			if (signPanel != null) {
				try {
					wmsg.setWysiwygImageSize(
							signPanel.getWidth(), signPanel.getHeight());
				} catch (InvalidMsgException e) {
					// TODO do something with this?
					e.printStackTrace();
				}
			}
		}
		
		// check for errors from the renderer
		if (errMan.hasErrors()) {
//			println("Renderer errors!");
//			errMan.printErrors();
			
			// show or update the dynamic error panel to tell the user what's
			// wrong
			updateErrorPanel();
		} else
			// if there were no errors, make a note of it
			noErrors();
	}
	
	/** Save the current MULTI string in the quick message */
	public Action saveMessage = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the MULTI string from the wmsg and save it
			multiStringText = wmsg.toString();
			qm.setMulti(multiStringText);
		}
	};
	
	/** Save the current MULTI string in the quick message */
	WController wc = this; // TODO do something better
	public Action saveMessageAs = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// open a form to get the new message name
			// the new form will trigger the rest
			session.getDesktop().show(new WMsgNewMsgForm(session, wc,
					qm.getName()));
		}
	};
	
	/** Save a new quick message with the given name (using the current MULTI
	 *  string) and start editing it.
	 */
	public void saveNewQuickMessage(String msgName) {
		// NOTE we know there is a sign/sign group associated with us, so we
		// don't have to make those
		
		// update the current MULTI string
		multiStringText = wmsg.toString();
		
		// make the quick message
		TypeCache<QuickMessage> qmCache = session.getSonarState().
				getDmsCache().getQuickMessages();
		HashMap<String, Object> qmAttrs = new HashMap<String, Object>();
		qmAttrs.put("multi", multiStringText);
		if (sg != null)
			qmAttrs.put("sign_group", sg);
		else {
			// if we're not editing a sign group, get the single-sign group
			SignGroup ssg = SignGroupHelper.lookup(sign.getName());
			qmAttrs.put("sign_group", ssg);
		}
		// save the object, then get the object and set our quick message
		qmCache.createObject(msgName, qmAttrs);
		qm = QuickMessageHelper.lookup(msgName);
		
		// TODO maybe don't do this...
		while (qm == null)
			qm = QuickMessageHelper.lookup(msgName);			
		
		// update the window title too
		editor.setWindowTitle(qm);
	}
	
	/** Update the controller with a MULTI string (this action can be undone) */
	public void setMultiString(String ms, boolean undoable) {
		if (undoable)
			// save state to allow undoing
			saveState();
		
		multiStringText = ms;
		wmsg.parseMulti(multiStringText);
		update();
	}
	
	/** Update everything that needs updating */
	public void update() {
//		println("In update: " + multiStringText);
		renderMsg();
		updateMultiPanel();
		updatePageListModel();
		updateCursor();
		
		// TODO add more stuff here eventually
	}
	
	/** Update the text toolbar if one is available. */
	private void updateTextToolbar() {
		if (editor != null)
			editor.updateTextToolbar();
	}
	
	/** Update the non-text tag info label with information about the current
	 *  tag (printable text or not) when in the proper mode.
	 */
	private void updateNonTextTagInfo() {
		// default is to show nothing
		String s = "";
		Color c = null;
		if (stepNonTextTags) {
			WToken tok = getCaretToken();
			if (tok != null) {
				// TODO add methods to token types to provide better description
				s = tok.getDescription();
				
				// check if there is a color we can include too
				if (tok.isType(WTokenType.colorForeground))
					c = ((WtColorForeground) tok).getColor();
				else if (tok.isType(WTokenType.pageBackground))
					c = ((WtPageBackground) tok).getColor();
			}
		}
		editor.updateNonTextTagInfo(s, c);
	}
	
	/** Update the MULTI panel with the current MULTI string. */
	private void updateMultiPanel() {
		if (multiPanel != null && multiStringText != null) {
			multiPanel.setText(multiStringText);
			
			// highlight any error-producing tokens in the MULTI panel
			if (errMan.hasErrors())
				multiPanel.highlightErrors(errMan);
		}
	}
	
	/** Update the WPageList given the current MULTI string and MultiConfig. */
	private void updatePageListModel() {
		// update the page list and the selected page
		if (pageList != null) {
			pageList.updatePageList(multiStringText, multiConfig);
			updateSelectedPage();
		}
	}
	
	/** Update the selected page to use one in the current pageList. */
	private void updateSelectedPage() {
		// make sure the selected page still exists
		if (selectedPageIndx >= pageList.getNumPages()) 
			selectedPageIndx = pageList.getNumPages()-1;
		else if (selectedPageIndx < 0)
			selectedPageIndx = 0;
		
		// rerender the messsage and get the page from our wmsg
		renderMsg();
		
		if (wmsg.isValid())
			setSelectedPage(wmsg.getPage(selectedPageIndx+1));
	}

	/** Set the currently selected page */
	public void setSelectedPage(WPage pg) {
		selectedPage = pg;
		
		// set the selected page on the WYSIWYG panel
		if (editor != null) {
			editor.setPageNumberLabel(getPageNumberLabel(selectedPageIndx));
			editor.setPage(selectedPage);
			
			// give focus to the sign panel
			signPanel.requestFocusInWindow();
		}
		
		// update rectangle and graphics handling for the selected page
		updateRectangles();
		updateGraphics();
	}
	
	public WPage getSelectedPage() {
		return selectedPage;
	}
	
	/** Get the currently active raster from the selected page. Ensures the
	 *  raster object has been initialized with the dimensions of the sign
	 *  panel.
	 */
	public WRaster getActiveRaster() {
		WRaster wr = null;
		if (selectedPage != null) {
			// get the raster for the selected page
			wr = selectedPage.getRaster();
			
			// make sure it's initialized
			if (!wr.isWysiwygInitialized() && signPanel != null)
				signPanel.initRaster(wr);
		}
		return wr;
	}
	
	/** Update rectangle handling on the selected page. */
	public void updateRectangles() {
		// make color and text rectangle GUI objects for this page
		selectedPage.makeGuiRectangles(rThreshold);
		
		// get GUI text rectangles and set the active one
		textRects = selectedPage.getTextRects();

		// get color rectangles
		colorRects = selectedPage.getColorRects();
		
		// make generic "rectangles"
		if (inColorRectMode())
			modeRects = new ArrayList<WgRectangle>(colorRects);
		else
			modeRects = new ArrayList<WgRectangle>(textRects);
		
		// re-select the selected rectangle from our index
		updateSelectedRectangle();
		
		// update the sign panel
		if (signPanel != null) {
			if (modeRects != null && (inTextRectMode() || inColorRectMode()))
				signPanel.setRectangles(modeRects);
			else if (!inGraphicMode())
				signPanel.clearRectangles();
		}
	}
	
	/** Update the selected rectangle based on the index. */
	private void updateSelectedRectangle() {
		// check if this rectangle exists - if it doesn't, de-select
		if (modeRects != null && selectedRectIndx >= modeRects.size())
			selectedRectIndx = -1;
		
		// in text mode, default to the whole-sign rectangle
		if (inTextMode() && selectedRectIndx == -1)
			selectedRectIndx = 0;
		
		// get the WgRectangle object (or null) and set the selected rectangle 
		if (selectedRectIndx < 0 || selectedRectIndx >= modeRects.size())
			setSelectedRectangle(null);
		else if (inTextMode() || inTextRectMode() || inColorRectMode())
			setSelectedRectangle(modeRects.get(selectedRectIndx));
		else
			setSelectedRectangle(null);
	}
	
	/** Set the selected rectangle (text or color). If in text rectangle mode,
	 *  the selectedRectangle is always the same as the selectedTextRectangle.
	 */
	private void setSelectedRectangle(WgRectangle r) {
		selectedRectangle = r;
		
		if (selectedRectangle != null) {
			// set the index for maintaining selection across updates
			selectedRectIndx = modeRects.indexOf(selectedRectangle);
			
			// initialize the geometry used by this rectangle to determine
			// relative cursor placement
			if (!selectedRectangle.geomInitialized())
				selectedRectangle.initGeom(getActiveRaster(), rThreshold);
			resizeHandles = selectedRectangle.getResizeHandles();
			signPanel.setSelectedRectangle(selectedRectangle);
			
			// if this is a text rectangle, set the selected text rectangle
			if (selectedRectangle instanceof WgTextRect) {
				setSelectedTextRectangle((WgTextRect) selectedRectangle);
			}
		} else {
			selectedRectIndx = -1;
			selectedRectangle = null;
			resizeHandles = null;
			signPanel.clearSelectedRegion();
			setSelectedTextRectangle(null);
		}
	}
	
	/** Set the selected/active text rectangle. Also sets the selectedTrIndx
	 *  to maintain selection across updates (if it's not null).
	 */
	private void setSelectedTextRectangle(WgTextRect tr) {
		selectedTextRect = tr;
		if (selectedTextRect != null) {
			selectedTrIndx = textRects.indexOf(selectedTextRect);
			trTokens = selectedTextRect.getTokenList();
		} else {
			// reset the selected text rectangle
			selectedTrIndx = -1;
			selectedTextRect = null;
			trTokens = new WTokenList();
		}
	}
	
	/** Update graphic handling on the selected page. */
	private void updateGraphics() {
		// get any graphics on the page - reverse the list compared to the
		// order in MULTI so the search goes from top to bottom
		graphics = selectedPage.getTokensOfType(WTokenType.graphic);
		graphics.reverse();
		
		// highlight them on the sign panel
		if (signPanel != null) {
			if (inGraphicMode())
				signPanel.setGraphics(graphics);
			else if (!(inTextRectMode() || inColorRectMode()))
				signPanel.clearRectangles();
		}
		
		// reset the selected graphic handle based on the index
		updateSelectedGraphic();
	}
	
	/** Update the selected graphic based on the index. */
	private void updateSelectedGraphic() {
		if (inGraphicMode()) {
			if (selectedGraphicIndx < 0
					|| selectedGraphicIndx >= graphics.size()) {
				setSelectedGraphic(null);
			} else {
				setSelectedGraphic(
						(WtGraphic) graphics.get(selectedGraphicIndx));
			}
		} else
			setSelectedGraphic(null);
	}
	
	/** Set the selected graphic */
	private void setSelectedGraphic(WtGraphic g) {
		if (g != null) {
			selectedGraphic = g;
			selectedGraphicIndx = graphics.indexOf(selectedGraphic);
			signPanel.setSelectedGraphic(selectedGraphic);
		} else {
			selectedGraphic = null;
			selectedGraphicIndx = -1;
			
			if (!(inTextRectMode() || inColorRectMode()))
				signPanel.clearSelectedRegion();
		}
	}
	
	/** Get the label indicating the page number */
	public static String getPageNumberLabel(int pn) {
		try {
			return String.format(I18N.get("wysiwyg.editor.page_number"),
					pn+1);
		} catch (IllegalFormatException e) {
			return "Page" + pn+1;
		}
	}
	
	/** Update the cursor that is active over the sign pixel panel */
	private void updateCursor() {
		if (signPanel != null) {
			signPanel.setCursor(cursor);
		}
	}
	
	/** Set the cursor that is active over the sign pixel panel */
	private void setCursor(Cursor c) {
		cursor = c;
		updateCursor();
	}
	
	/** Set the cursor type based on the editing mode */
	private void setCursorFromMode() {
		if (inTextMode())
			setCursor(textCursor);
		else if (inGraphicMode())
			setCursor(graphicCursor);
		else if (inColorRectMode())
			setCursor(newRectCursor);
		else if (inTextRectMode())
			setCursor(newRectCursor);
		else if (inMultiTagMode())
			setCursor(multiTagCursor);
	}
	
	/** Return whether or not we are in text selection mode (for drag
	 *  operations), indicated by the current cursor.
	 */
	private boolean inTextSelectionMode() {
		return cursor == textCursor;
	}
	
	/** Return whether or not we are in move mode (for drag operations) */
	private boolean inMoveMode() {
		return cursor == moveCursor;
	}

	/** Return whether or not we are in any resize mode (for drag operations) */
	private boolean inResizeMode() {
		return resizeCursors.containsValue(cursor);
	}
	
	/** Return whether or not we are in new rectangle mode */
	private boolean inNewRectMode() {
		return cursor == newRectCursor;
	}
	
	/** Get a JComboBox containing a list of sign names (only applies when
	 * editing a group).
	 * TODO same note as with JList above - should maybe abstract this more
	 * but whatever... 
	 */
	public JComboBox<String> getSignGroupComboBox() {
		if (sg != null) {
			// generate a list of signs in the sign group
			makeSignListForGroup(true);
			
			// define a selection handler for the combo box
			class SignSelectionListener implements ActionListener {
				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
					JComboBox<String> cb = (JComboBox<String>) e.getSource();
					String dmsName = (String) cb.getSelectedItem();
					// TODO - do we need to catch any exceptions here?
					sign = dmsList.get(dmsName);
					
					// update the page list and selected page given the change
					updatePageListModel();
					updateSelectedPage();
				}
			}
			
			// setup and return the combo box
			dms_list = new JComboBox<String>(dmsNames);
			dms_list.addActionListener(new SignSelectionListener());
		}
		return dms_list;
	}
	
	/** Get a list of signs in the group. If setSign is true, the controller's
	 *  "sign" attribute be set to the first sign in the group.
	 */
	public void makeSignListForGroup(boolean setSign) {
		// get the list of signs in the sign group
		// look through the DmsSignGroups to find all signs with this group
		dmsList = new HashMap<String,DMS>();
		Iterator<DmsSignGroup> dsgit = DmsSignGroupHelper.iterator();
		while (dsgit.hasNext()) {
			DmsSignGroup dsg = dsgit.next();
			if (dsg.getSignGroup() == sg) {
				DMS dms = dsg.getDms();
				dmsList.put(dms.getName(), dms);
			}
		}
		
		// get the list of sign names and sort them alphabetically
		dmsNames = Arrays.stream(dmsList.keySet().toArray()).
				toArray(String[]::new);
		Arrays.sort(dmsNames);
		
		// set the current sign to the first one in the group if desired
		if (setSign && dmsNames.length > 0)
			sign = dmsList.get(dmsNames[0]);
	}
	
	public boolean inTextMode() {
		return editingMode == MODE_TEXT;
	}
	
	public void activateTextMode() {
		// put the cursor in text mode then update everything
		editingMode = MODE_TEXT;
		setCursorFromMode();
		update();
		updateCaret();
	}

	public boolean inGraphicMode() {
		return editingMode == MODE_GRAPHIC;
	}
	
	public void activateGraphicMode() {
		// put the cursor in ?? hand ?? mode then update everything
		editingMode = MODE_GRAPHIC;
		setCursorFromMode();
		update();
		updateCaret();
	}

	public boolean inTextRectMode() {
		return editingMode == MODE_TEXTRECT;
	}
	
	public void activateTextRectangleMode() {
		// put the cursor in crosshair mode then update everything
		editingMode = MODE_TEXTRECT;
		setCursorFromMode();
		update();
		updateCaret();
	}

	public boolean inColorRectMode() {
		return editingMode == MODE_COLORRECT;
	}
	
	public void activateColorRectangleMode() {
		// put the cursor in crosshair mode then update everything
		editingMode = MODE_COLORRECT;
		setCursorFromMode();
		update();
		updateCaret();
	}
	
	public boolean inMultiTagMode() {
		return editingMode == MODE_MULTITAG;
	}
	
	public void activateMultiTagMode() {
		// put the cursor in ?? default ?? mode then update everything
		editingMode = MODE_MULTITAG;
		setCursorFromMode();
		update();
		updateCaret();
	}
	
	public Cursor getCursor() {
		return cursor;
	}
	
	public MultiConfig getMultiConfig() {
		return multiConfig;
	}
	
	public ProxyListModel<Font> getFontModel() {
		if (session != null) {
			return session.getSonarState().getDmsCache().getFontModel();
		} return null;
	}
	
	/** Update the list model of graphics supported by the current MultiConfig
	 *  based on the dimensions and color scheme of the sign.
	 *  
	 *  TODO (as everywhere) - SignGroups... 
	 */
	public void updateGraphicModel() {
		// get sign parameters from the MultiConfig
		int maxHeight = multiConfig.getPixelHeight();
		int maxWidth = multiConfig.getPixelWidth();
		int maxColorScheme = multiConfig.getColorScheme().ordinal();
		
		// clear the list we have before updating
		supportedGraphics.removeAllElements();
		
		// filter the list of all graphics available to the ones we support
		Iterator<Graphic> it = GraphicHelper.iterator();
		while (it.hasNext()) {
			Graphic g = it.next();
			if (g.getHeight() <= maxHeight && g.getWidth() <= maxWidth
					&& g.getColorScheme() <= maxColorScheme)
				supportedGraphics.addElement(g);
		}
	}
	
	public DefaultComboBoxModel<Graphic> getGraphicModel() {
		updateGraphicModel();
		return supportedGraphics;
	}
	
	public Font getCurrentFont() {
		return font;
	}

	public Font getDefaultFont() {
		return defaultFont;
	}
	
	public void setMultiPanel(WMsgMultiPanel mp) {
		multiPanel = mp;
	}
	
	public void setSignPanel(WImagePanel sp) {
		signPanel = sp;
//		update();
	}
	
	private void setMultiConfig(MultiConfig mc) {
		multiConfig = mc;
		setFontFromConfig();
		setColorsFromConfig();
	}
	
	private void setFontFromConfig() {
		if (multiConfig != null) {
			font = multiConfig.getDefaultFont();
			defaultFont = font;
		}
	}
	
	private void setColorsFromConfig() {
		if (multiConfig != null) {
			fgColor = multiConfig.getDefaultFG();
			bgColor = multiConfig.getDefaultBG();
			fgColorDefault = fgColor;
			bgColorDefault = bgColor;
			
			// use the default foreground color for color rectangles (so we
			// always have one)
			colorRectColor = multiConfig.getDefaultFG();
		}
	}
	
	public DmsColor getForegroundColor() {
		return fgColor;
	}
		
	public DmsColor getBackgroundColor() {
		return bgColor;
	}
	
	public Session getSession() {
		return session;
	}
		
	public SmartDesktop getDesktop() {
		return desktop;
	}
	
	public WMsgEditorForm getEditorForm() {
		return editor;
	}
	
	/* Get the current DMS object */
	public DMS getSign() {
		return sign;
	}
	
}