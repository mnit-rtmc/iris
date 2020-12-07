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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JInternalFrame;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.GraphicHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IWorker;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.client.wysiwyg.editor.tags.WMultiTagDialog;
import us.mn.state.dot.tms.utils.wysiwyg.WMessage;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;
import us.mn.state.dot.tms.utils.wysiwyg.WPoint;
import us.mn.state.dot.tms.utils.wysiwyg.WRaster;
import us.mn.state.dot.tms.utils.wysiwyg.WgRectangle;
import us.mn.state.dot.tms.utils.wysiwyg.WgTextRect;
import us.mn.state.dot.tms.utils.wysiwyg.WEditorErrorManager;
import us.mn.state.dot.tms.utils.wysiwyg.WFont;
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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtPageTime;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextChar;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextRectangle;
import us.mn.state.dot.tms.utils.wysiwyg.token.Wt_Rectangle;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.Multi.JustificationPage;
import us.mn.state.dot.tms.utils.MultiConfig;

/**
 * WYSIWYG DMS Message Editor Controller for handling exchanges between the
 * editor GUI form and the renderer.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WController {

	/** Flag to enable/disable verbose logging output */
	private final static boolean DEBUG = false; 
	
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
	
	/** Keep a handle to the editor form and sign pixel panel for any updates
	 *  we need to make from here */
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
	private String multiString = null;
	private boolean prefixPage = false;

	/** Amount of time in nanoseconds to wait for a message to be created */
	private final static long MAX_WAIT = 10000000000L;
	
	/** MultiConfigs for config-related stuff  */
	/** "Active" MultiConfig */
	private MultiConfig multiConfig;
	
	/** "Master" Sign Group MultiConfig (largest sign group) */
	private MultiConfig signGroupMultiConfig;
	
	/** MultiConfig ComboBox for sign groups */
	private WMultiConfigComboBox multiConfigList;
	
	/** Whether or not MULTI mode is forced */
	private boolean forceMULTI = false;
	
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
	private WgTextRect selectedTextRect;
	private WTokenList trTokens = new WTokenList();
	
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
	private int caretPageIndx = 0;
	private boolean caretOn = true;
	public final static int CARET_EOP = -1;
	
	/** Toggle for how non-text tags are handled. If false (default), non-text
	 *  tags are skipped, otherwise they can be manipulated through caret
	 *  navigation and a status bar below the sign panel.
	 */
	private boolean stepNonTextTags = false;
	
	/** Keep track of stepNonTextTags outside of MULTI mode */
	private boolean snttNonMultiMode = false;
	
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
	private DmsColor colorRectColor;
	
	/** Page list */
	private WPageList pageList;
	
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
		if (DEBUG)
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
	
	/** Initialize some things that need to take place after all elements of
	 *  the editor are in place. Called by the WMsgEditorForm at the end of
	 *  initialize().
	 */
	public void postInit() {
		// do an update to render the message and fill the page list, then
		// initialize the caret and mouse cursors
		update();
		
		if (!forceMULTI) {
			initCaret();
			initCursors();
			
			// also give the sign panel focus so the user can immediately start
			// typing
			signPanel.requestFocusInWindow();
		}
	}
	
	/** Set the sign being used */
	public void setSign(DMS d) {
		sign = d;
		
		// generate the MultiConfig for the sign
		if (sign != null) {
			try {
				initFromMultiConfig(MultiConfig.from(sign));
			} catch (Exception e) {
				// with a null MultiConfig, we will force MULTI mode
				forceMULTI = true;
				if (DEBUG)
					e.printStackTrace();
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
			try {
				signGroupMultiConfig = MultiConfig.from(sg);
			} catch (Exception e) {
				// with a null MultiConfig, we will force MULTI mode
				if (DEBUG)
					e.printStackTrace();
			}
			
			if (signGroupMultiConfig != null) {
				// use the "primary" MultiConfig to initialize
				MultiConfig mc = signGroupMultiConfig;
				if (signGroupMultiConfig != null) {
					if (signGroupMultiConfig.getConfigList() != null
							&& !signGroupMultiConfig.getConfigList().isEmpty())
						mc = signGroupMultiConfig.getConfigList().get(0);
					else if (signGroupMultiConfig.getSignList() != null
							&& !signGroupMultiConfig.getSignList().isEmpty())
						mc = signGroupMultiConfig.getSignList().get(0);
				}
				initFromMultiConfig(mc);
				
				// create the ComboBox for displaying/selecting different
				// configs and signs
				multiConfigList = WMultiConfigComboBox.
						fromSignGroupMultiConfig(signGroupMultiConfig, this);
			} else
				forceMULTI = true;
		} else {
			multiConfig = null;
			sign = null;
		}
		update();
	}
	
	/** Initialize some necessary defaults from the MultiConfig provided. This
	 *  sets the "Active" MultiConfig.
	 */
	private void initFromMultiConfig(MultiConfig mc) {
		multiConfig = mc;
		if (multiConfigUseable()) {
			// initialize the font and colors from the MultiConfig
			font = multiConfig.getDefaultFont();
			defaultFont = font;
			
			fgColor = multiConfig.getDefaultFG();
			bgColor = multiConfig.getDefaultBG();
			
			// use the default foreground color for color rectangles (so we
			// always have one)
			colorRectColor = multiConfig.getDefaultFG();
		} else
			// if our MultiConfig is null, we can't use WYSIWYG mode
			forceMULTI = true;
	}
	
	public WMultiConfigComboBox getConfigComboBox() {
		return multiConfigList;
	}
	
	/** Set the "active" MultiConfig used for rendering the sign panel. */
	public void setActiveMultiConfig(MultiConfig mc) {
		multiConfig = mc;
		
		// check functions supported by this MultiConfig
		editor.setActiveMultiConfig(multiConfig);
		
		update(false);
	}
	
	/** Set the quick message being edited */
	public void setQuickMessage(QuickMessage q) {
		qm = q;
		
		// get the MULTI string text from the quick message
		if (qm != null) {
			multiString = qm.getMulti();
			prefixPage = qm.getPrefixPage();
		} else
			multiString = "";
		
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
		if (inTextMode() || inMultiTagMode())
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
		
		// get focus for the sign panel when someone clicks on it
		signPanel.requestFocusInWindow();
	}
	
	/** Handle a mouse move event on the main editor panel */
	public void handleMouseMove(MouseEvent e) {
		// create a WPoint for this mouse event
		WPoint p = getWPoint(e);
		
		// set the drag mode (which sets the mouse cursor)
		setDragMode(p);
	}
	
	/** Handle a mouse pressed event  */
	public void handleMousePressed(MouseEvent e) {
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
		if (SwingUtilities.isLeftMouseButton(e)) {
			
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
		println("Mouse released");
		
		// construct a WPoint from the mouse event
		WPoint p = getWPoint(e);
				
		// if the x and y are the same as the last press, do nothing
		// (handleClick SHOULD take care of it...)
		if (lastPress != null &&
				!lastPress.getWysiwygPoint().equals(p.getWysiwygPoint())) {
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
	
	/** Update the text selection from mouse movement. */
	private void updateTextSelection(WPoint p) {
		endTok = findClosestTextToken(p);
		
		// update the selection if they're not the same token
		if ((startTok != endTok) && startTok != null && endTok != null) {
			// get the indices and figure out which is first
			int start = trTokens.indexOf(startTok);
			int end = trTokens.indexOf(endTok);
			
			boolean includeEnd = false;
			selectingForwards = true;
			int si;
			int ei;
			if (start < end) {
				si = start;
				ei = end;
				includeEnd = rightHalf(p, endTok);
				caretIndx = end;
				caretPageIndx = selectedPage.getTokenIndex(startTok);
			} else {
				// if they are selecting backwards, we need to use the initial
				// click and token and reverse the indices
				si = end;
				ei = start;
				includeEnd = rightHalf(lastPress, startTok);
				selectingForwards = false;
				caretIndx = si;
				caretPageIndx = selectedPage.getTokenIndex(endTok);
			}
			updateTextSelection(si, ei, includeEnd, selectingForwards);
		}
	}
	
	/** Update the text selection from indices and directions. */
	private void updateTextSelection(int si, int ei,
			boolean includeEnd, boolean forwards) {
		// update the token lists
		updateTokenListsSelection(si, ei, includeEnd);
		
		// tell the image panel what the selection is
		signPanel.setTextSelection(tokensSelected);
		
		// move the caret too - do it "manually" to avoid changing token
		// lists (the caret page index is set above)
		if (forwards)
			signPanel.setCaretLocation(tokensSelected.getLast(), true);
		else
			signPanel.setCaretLocation(tokensSelected.get(0), false);
		
		// update the toolbar
		updateTextToolbar();
	}
	
	/** Update the move operation given the current cursor position. */
	private void updateMoveOperation(WPoint p) {
		// try to get the token to move
		WToken mTok = null;
		if (inTextRectMode() || inColorRectMode()) {
			if (selectedRectangle != null)
				mTok = selectedRectangle.getRectToken();
		} else if (inGraphicMode())
			mTok = selectedGraphic;
		
		if (mTok != null && lastPress != null) {
			// hide the caret before a move
			clearCaret();
			
			// calculate the safe change in position
			int ox = p.getSignX() - lastPress.getSignX();
			int oy = p.getSignY() - lastPress.getSignY();
			int offsetX = checkMoveOffsetX(ox, mTok);
			int offsetY = checkMoveOffsetY(oy, mTok);
			
			// move the token and update the last press
			mTok.moveTok(offsetX, offsetY);
			lastPress = p;

			// update everything
			update();
		}
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
		if (wr != null) {
			if (x + ox < 1)
				return 1 - x;
			else if (rx + ox > wr.getWidth())
				return Math.max(rx - wr.getWidth(), 0);
		}
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
		if (wr != null) {
			if (y + oy < 1)
				return 1 - y;
			else if (by + oy > wr.getHeight())
				return Math.max(by - wr.getHeight(), 0);
		}
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
				
				// update so the new rectangle shows up with the caret inside
				update();
				updateCaret();
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
	
	/** Determine if the x-coordinate of p is on the right half of token tok.*/
	private boolean rightHalf(WPoint p, WToken tok) {
		return p.getSignX() >= tok.getCentroidX();
	}
	
	/** Toggle non-text char mode for handling of non-text tag */
	public Action toggleNonTextTagMode = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (!inMultiTagMode()) {
				stepNonTextTags = !stepNonTextTags;
				snttNonMultiMode = stepNonTextTags;
			} else {
				// in MULTI tag mode we always do this
				stepNonTextTags = true;
			}
			editor.updateNonTextTagButton(stepNonTextTags);
			updateNonTextTagInfo();
			signPanel.requestFocusInWindow();
		}
	};
	
	/** Initialize mouse cursors available to the GUI. */
	private void initCursors() {
		textCursor = new Cursor(Cursor.TEXT_CURSOR);
		graphicCursor = new Cursor(Cursor.HAND_CURSOR);
		newRectCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
		multiTagCursor = new Cursor(Cursor.TEXT_CURSOR);
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
		if (trTokens != null && !trTokens.isEmpty()) {
			WToken tok = trTokens.findFirstTextToken(true, true);
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
		
		if (toRight)
			++tokIndx;
		
		// dispatch to the other moveCaret method
		moveCaret(tokIndx);
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
			else if (caretIndx < 0)
				caretIndx = 0;
			tokensBefore = trTokens.slice(0, caretIndx);
			tokensAfter = trTokens.slice(caretIndx, trTokens.size());
			
			// set the new caret location
			cTok = getCaretToken();
			toRight = tokensAfter.isEmpty();
			
//			if (cTok != null)
			if (cTok == null && !selectedTextRect.isWholeSign())
				// if we didn't get a token, the current text rectangle is
				// empty - if this is an actual text rectangle, use the
				// WtTextRectangle token
				cTok = selectedTextRect.getRectToken();
			
			if (cTok != null) {
				// most characters and left of newline - left of character 
				if (!(cTok.isType(WTokenType.newLine) && toRight)) {
					signPanel.setCaretLocation(cTok, toRight);
				} else if (cTok.isType(WTokenType.newLine) && toRight) {
					// if right of newline, use the "next line" parameters
					println("Caret on new line");
					WtNewLine nlTok = (WtNewLine) cTok;
					int x = nlTok.getNextLineLeft();
					int y = nlTok.getNextLineTop();
					int h = nlTok.getNextLineHeight();
					signPanel.setCaretLocation(x, y, h);
				}
				caretPageIndx = selectedPage.getTokenIndex(cTok);
				if (toRight) {
					caretPageIndx = Math.min(caretPageIndx + 1,
							selectedPage.getNumTokens());
				}
			} else {
				println("Caret at EOP");
				tokensBefore = trTokens;
				tokensAfter.clear();
				int x = selectedPage.getEOPX();
				int y = selectedPage.getEOPY();
				int h = selectedPage.getEOPH();
				signPanel.setCaretLocation(x, y, h);
				if (selectedTextRect != null
						&& selectedTextRect.isWholeSign())
					// at end of whole-sign text rectangle
					caretPageIndx = trTokens.size();
				else
					// at end of page
					caretPageIndx = selectedPage.getNumTokens();
			}
			caretOn = true;
			
			// a click resets the selection
			tokensSelected.clear();
			signPanel.clearTextSelection();
			selectingForwards = null;
			
			// for debugging (only in effect if DEBUG is true)
			printCaretTokens();
			
			updateTextToolbar();
			updateNonTextTagInfo();
		} else
			// if there is no selected text rectangle, there is no caret
			clearCaret();
	}
	
	/** Clear the caret from the screen and reset relevant token lists. */
	private void clearCaret() {
		caretOn = false;
		tokensBefore.clear();
		tokensAfter.clear();
		tokensSelected.clear();
		signPanel.hideCaret();
		signPanel.clearTextSelection();
	}
	
	/** Get the token associated with the current caret position. This is
	 *  either the first token after the caret or the last token before
	 *  the caret, or null if neither is valid. */
	public WToken getCaretToken() {
		if (!tokensAfter.isEmpty()) {
			println("Caret token from after");
			return tokensAfter.get(0);
		} else if (!tokensBefore.isEmpty()) {
			println("Caret token from before");
			return tokensBefore.getLast();
		}
		println("Caret token null");
		return null;
	}
	
	/** Action to move caret to left (using left arrow key) */
	public Action moveCaretLeft = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (!tokensSelected.isEmpty()) {
				moveCaret(caretIndx);
			} else if (trTokens != null && !trTokens.isEmpty()) {
				// check the navigation mode
				if (stepNonTextTags) {  // go through all tokens
					if (caretIndx >= 1)
						moveCaret(caretIndx-1);
				} else {  // skip any non-text tokens
					int nextIndx = Math.max(caretIndx-1, 0);
					WToken textTok = trTokens.findPrevTextToken(
							nextIndx, true, true);
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
			if (!tokensSelected.isEmpty()) {
				moveCaret(caretIndx);
			} else if (trTokens != null && !trTokens.isEmpty()) {
				// check the navigation mode
				if (stepNonTextTags) {  // go through all tokens
					if (caretIndx < trTokens.size())
						moveCaret(caretIndx+1);
				} else {  // skip any non-text tokens
					// find the next text token, if there is one
					WToken textTok = trTokens.findNextTextToken(
							caretIndx+1, true, true);
					println("Found %s", textTok);
					if (textTok != null)
						moveCaret(textTok);
					else if (trTokens.findNextTextToken(
							caretIndx, true, true) != null) {
						// if there's nothing remaining, go to the right of
						// the last text token
						moveCaret(caretIndx+1);
					}
				}
			}
		}
	};
	
	/** Direction of selection (with keyboard) */
	private Boolean selectingForwards;
	
	/** Action to select text to left (using shift + left arrow key) */
	public Action selectTextLeft = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the start and end indices
				int si, ei;
				if (tokensSelected.isEmpty()) {
					// use the last token before as the selection end if
					// there is no selection yet
					if (!tokensBefore.isEmpty())
						ei = trTokens.indexOf(tokensBefore.getLast());
					else
						ei = 0;
					
					// select one token
					si = ei;
					
					// set the selection direction
					selectingForwards = false;
				} else {
					// otherwise use the tokens in the selection
					// check the direction in which we were going
					if (!selectingForwards) {
						// add to selection
						si = trTokens.indexOf(tokensSelected.get(0)) - 1;
						if (si < 0)
							si = 0;
						ei = trTokens.indexOf(tokensSelected.getLast());
					} else {
						// remove from selection
						si = trTokens.indexOf(tokensSelected.get(0));
						ei = trTokens.indexOf(tokensSelected.getLast()) - 1;
					}
				}
				
				// if we pick the last token, use includeEnd
				if (ei == trTokens.size())
					--ei;
				
				if (ei >= si)
					updateTextSelection(si, ei, true, selectingForwards);
				else {
					updateCaret();
				}
			}
		}
	};
	
	/** Action to select text to right (using shift + right arrow key) */
	public Action selectTextRight = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the start and end indices
				int si, ei;
				if (tokensSelected.isEmpty()) {
					// use the first token after as the selection start if
					// there is no selection yet
					if (!tokensAfter.isEmpty())
						si = trTokens.indexOf(tokensAfter.get(0));
					else
						si = trTokens.size() - 1;
					
					// select one token
					ei = si + 1;

					// set the selection direction
					selectingForwards = true;
				} else {
					// otherwise use the tokens in the selection
					// check the direction in which we were going
					if (selectingForwards) {
						si = trTokens.indexOf(tokensSelected.get(0));
						ei = trTokens.indexOf(tokensSelected.getLast()) + 2;
						
					} else {
						// remove from selection
						si = trTokens.indexOf(tokensSelected.get(0)) + 1;
						ei = trTokens.indexOf(tokensSelected.getLast()) + 1;
					}
				}
				
				// if we pick the last token, use includeEnd
				boolean includeEnd = false;
				if (ei >= trTokens.size()) {
					ei = trTokens.size() - 1;
					includeEnd = true;
				}
				
				if (ei > si)
					updateTextSelection(si, ei, includeEnd, selectingForwards);
				else {
					moveCaret(caretIndx);
				}
			}
		}
	};
	
	/** Action to select text to left (using ctrl + shift + left arrow key) */
	public Action selectWordLeft = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the end index
				int si, ei;
				if (tokensSelected.isEmpty()) {
					// use the first token after as the selection end if
					// there is no selection yet
					if (!tokensBefore.isEmpty())
						ei = trTokens.indexOf(tokensBefore.getLast());
					else
						ei = 0;

					// find the previous word searching from the caret
					si = findWordStartBackwards();
					
					// set the selection direction
					selectingForwards = false;
				} else {
					// otherwise use the tokens in the selection
					// check the direction in which we were going
					if (!selectingForwards) {
						// add to selection - hold
						ei = trTokens.indexOf(tokensSelected.getLast());
						
						// find the next word searching before the selection
						if (!tokensBefore.isEmpty()) {
							si = findWordStartBackwards(
									tokensBefore.getLast()) - 1;
							if (si < 0)
								si = 0;
						} else
							si = 0;
					} else {
						// remove from selection
						si = trTokens.indexOf(tokensSelected.get(0));
						ei = findWordStartBackwards(tokensSelected.getLast()) - 1;
					}
				}
				
				if (ei == trTokens.size())
					--ei;
				
				if (ei >= si)
					updateTextSelection(si, ei, true, selectingForwards);
				else {
					updateCaret();
				}
			}
		}
	};
	
	/** Action to select word to right (using ctrl + shift + right arrow key) */
	public Action selectWordRight = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the start index
				int si, ei;
				if (tokensSelected.isEmpty()) {
					// use the first token after as the selection start if
					// there is no selection yet
					if (!tokensAfter.isEmpty())
						si = trTokens.indexOf(tokensAfter.get(0));
					else
						si = trTokens.size() - 1;
					
					// find the next word searching from the caret
					ei = findWordStartForwards();
					
					// set the selection direction
					selectingForwards = true;
				} else {
					// otherwise use the tokens in the selection
					// check the direction in which we were going
					if (selectingForwards) {
						// add to selection - hold
						si = trTokens.indexOf(tokensSelected.get(0));
						
						// find the next word searching after the selection
						if (!tokensAfter.isEmpty())
							ei = findWordStartForwards(tokensAfter.get(0));
						else
							ei = trTokens.size();
					} else {
						// remove from selection
						si = findWordStartForwards(tokensSelected.get(0));
						ei = trTokens.indexOf(tokensSelected.getLast()) + 1;
					}
				}
				
				// if we pick the last token, use includeEnd
				boolean includeEnd = false;
				if (ei >= trTokens.size()) {
					ei = trTokens.size() - 1;
					includeEnd = true;
				}
				
				if (ei > si)
					updateTextSelection(si, ei, includeEnd, selectingForwards);
				else {
					if (!tokensAfter.isEmpty())
						moveCaret(tokensAfter.get(0));
					else
						moveCaret(trTokens.size());
				}
			}
		}
	};
	
	/** Jump the caret to the beginning of the next word */
	public Action jumpCaretRight = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			int i = findWordStartForwards();
			if (i != -1)
				moveCaret(i);
		}
	};	
	
	/** Jump the caret to the beginning of the previous word */
	public Action jumpCaretLeft = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			int i = findWordStartBackwards();
			if (i != -1)
				moveCaret(i);
		}
	};
	
	/** Return the index of the token that starts the current or previous word
	 *  (or the previous tag).
	 */
	private int findWordStartBackwards() {
		if (trTokens != null && !trTokens.isEmpty()) {
			// get the current token
			WToken tok = getCaretToken();
			if (tok != null)
				return findWordStartBackwards(tok);
		}
		return -1;
	}

	/** Return the index of the token that starts the current or previous word
	 *  (or the previous tag) searching from the token tok.
	 */
	private int findWordStartBackwards(WToken tok) {
		if (trTokens != null && !trTokens.isEmpty()) {
			// find the word this token is in
			WTokenList word = trTokens.getTokenWord(tok);
			if (word != null) {
				// check if this is the start of the word or not
				if (word.indexOf(tok) == 0) {
					// if it is, go to first token of the previous word
					// (obeying stepNonTextTags)
					int wi = trTokens.getWordIndex(word);
					while (wi >= 1) {
						WTokenList pWord = trTokens.getWords().get(--wi);
						if (!pWord.isEmpty()) {
							WToken pTok = pWord.get(0);
							if (pTok.isPrintableText() || stepNonTextTags)
								return trTokens.indexOf(pTok);
						}
					}
				} else {
					// if it's not, go to the start of this word
					if (!word.isEmpty())
						return trTokens.indexOf(word.get(0));
				}
			}
		}
		return -1;
	}
	
	/** Return the index of the token that starts the current or previous word
	 *  (or the previous tag).
	 */
	private int findWordStartForwards() {
		if (trTokens != null && !trTokens.isEmpty()) {
			// get the current token
			WToken tok = getCaretToken();
			if (tok != null)
				return findWordStartForwards(tok);
		}
		return -1;
	}
	
	/** Return the index of the token that starts the next word or tag. */
	private int findWordStartForwards(WToken tok) {
		if (trTokens != null && !trTokens.isEmpty()) {
			// find the word this token is in
			WTokenList word = trTokens.getTokenWord(tok);
			if (word != null) {
				// go to first token of the next word (obeying
				// stepNonTextTags)
				int wi = trTokens.getWordIndex(word);
				while (wi < trTokens.getNumWords()-1) {
					WTokenList nWord = trTokens.getWords().get(++wi);
					if (!nWord.isEmpty()) {
						WToken pTok = nWord.get(0);
						if (pTok.isPrintableText() || stepNonTextTags)
							return trTokens.indexOf(pTok);
					}
				}
				// if we don't get anything, we need to go to the end of the
				// last word (which should be the current word)
				if (!word.isEmpty())
					return trTokens.indexOf(word.getLast()) + 1;
			}
		}
		return -1;
	}
	
	/** Up Arrow action. */
	public Action upArrow = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (inTextMode() || (getCaretToken() != null
					&& (inTextRectMode() || inMultiTagMode()))) {
				moveCaretUp();
			} else
				selectPrevRegion();
		}
	};
	
	/** Down Arrow action. */
	public Action downArrow = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (inTextMode() || (getCaretToken() != null
					&& (inTextRectMode() || inMultiTagMode()))) {
				moveCaretDown();
			} else
				selectNextRegion();
		}
	};
	
	/** Move caret up one line (using up arrow key) */
	public void moveCaretUp() {
		// get the token above the caret - if it's valid, move the caret up
		// (otherwise do nothing)
		WToken upTok = getTokenLineUp();
		if (upTok != null)
			moveCaret(upTok);
	}

	/** Select from caret position line above. */
	public Action selectUp = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the start and end indices
				int si, ei;
				if (tokensSelected.isEmpty()) {
					WToken tok = getCaretToken();
					WToken upTok = getTokenLineUp();
					
					ei = trTokens.indexOf(tok);
					selectingForwards = false;
					
					if (upTok != null)
						si = trTokens.indexOf(upTok);
					else
						// go to beginning of line if nothing above
						si = 0;
				} else {
					if (!selectingForwards) {
						// have a selection - keep going up
						ei = trTokens.indexOf(tokensSelected.getLast()) + 1;
						WToken upTok = getTokenLineUp(tokensSelected.get(0));
						if (upTok != null)
							si = trTokens.indexOf(upTok);
						else
							si = 0;
					} else {
						// remove from selection
						si = trTokens.indexOf(tokensSelected.get(0));
						ei = trTokens.indexOf(
								getTokenLineUp(tokensSelected.getLast()))+1;
					}
				}
				if (ei > trTokens.size() - 1)
					ei = trTokens.size() - 1;
				if (ei > si)
					updateTextSelection(si, ei, false, selectingForwards);
				else
					updateCaret();
			}
		}
	};
	
	/** Return the token above the caret */
	private WToken getTokenLineUp() {
		// get the current token
		WToken tok = getCaretToken();
		return getTokenLineUp(tok);
	}
	
	/** Return the token above the token provided */
	private WToken getTokenLineUp(WToken tok) {
		if (tok != null) { 
			// get the line this token is on
			int li = trTokens.getLineIndex(tok);
			if (li > 0) {
				// if this isn't the first line, find the closest token (based
				// on the X coordinate) on the next line up
				return getClosestTokenOnLine(li-1, tok.getCoordX());
			}
		}
		return null;
	}
	
	/** Move caret down one line (using up arrow key) */
	public void moveCaretDown() {
		// get the token below the caret - if it's valid, move the caret down
		// (otherwise do nothing)
		WToken downTok = getTokenLineDown();
		if (downTok != null)
			moveCaret(downTok);
	}

	/** Select from caret position line below. */
	public Action selectDown = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the start and end indices
				int si, ei;
				if (tokensSelected.isEmpty()) {
					// get the current token and the one below
					WToken tok = getCaretToken();
					WToken downTok = getTokenLineDown();
					
					si = trTokens.indexOf(tok);
					selectingForwards = true;
					
					if (downTok != null)
						ei = trTokens.indexOf(downTok);
					else
						// go to end of TR if nothing above
						ei = trTokens.size() - 1;
				} else {
					if (selectingForwards) {
						// have a selection - keep going down
						si = trTokens.indexOf(tokensSelected.get(0));
						WToken downTok = getTokenLineDown(
								tokensSelected.getLast());
						if (downTok != null)
							ei = trTokens.indexOf(downTok);
						else
							ei = trTokens.size() - 1;
					} else {
						// remove from selection
						ei = trTokens.indexOf(tokensSelected.getLast());
						si = trTokens.indexOf(
								getTokenLineDown(tokensSelected.get(0)));
					}
				}
				if (ei > si)
					updateTextSelection(si, ei, true, selectingForwards);
				else {
					if (!tokensAfter.isEmpty())
						moveCaret(tokensAfter.get(0));
					else
						moveCaret(trTokens.getLast());
				}
			}
		}
	};
	
	/** Return the token below the caret */
	private WToken getTokenLineDown() {
		// get the current token
		WToken tok = getCaretToken();
		return getTokenLineDown(tok);
	}
	
	/** Return the token below the token provided */
	private WToken getTokenLineDown(WToken tok) {
		if (tok != null) {
			// get the line this token is on
			int li = trTokens.getLineIndex(tok);
			if (li < trTokens.getNumLines()-1) {
				// if this isn't the last line, find the closest token (based
				// on the X coordinate) on the next line down
				return getClosestTokenOnLine(li+1, tok.getCoordX());
			}
		}
		return null;
	}
	
	/** Select the previous region in the region list for the current mode. */
	public void selectPrevRegion() {
		println("spr");
		if (inTextRectMode() || inColorRectMode()) {
			println("Rect: %d", selectedRectIndx);
			if (selectedRectIndx > 0) {
				--selectedRectIndx;
				updateSelectedRectangle();
			}
		} else if (inGraphicMode()) {
			println("Graphic: %d", selectedGraphicIndx);
			if (selectedGraphicIndx > 0) {
				--selectedGraphicIndx;
				updateSelectedGraphic();
			}
		}
	}
	
	/** Select the next region in the region list for the current mode. */
	public void selectNextRegion() {
		println("snr");
		if (inTextRectMode() || inColorRectMode()) {
			println("Rect: %d", selectedRectIndx);
			if (selectedRectIndx < modeRects.size()-1) {
				++selectedRectIndx;
				updateSelectedRectangle();
			}
		} else if (inGraphicMode()) {
			println("Graphic: %d", selectedGraphicIndx);
			if (selectedGraphicIndx < graphics.size()-1) {
				++selectedGraphicIndx;
				updateSelectedGraphic();
			}
		}
	}
	
	/** Action to move caret to the beginning of the current line (home key) */
	public Action moveCaretLineBeginning = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the home token and move there
			WToken homeTok = getHomeToken();
			if (homeTok != null)
				moveCaret(homeTok);
		}
	};
	
	/** Select from caret position to start of line. */
	public Action selectToLineStart = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the start and end indices
				int si, ei;
				if (tokensSelected.isEmpty()) {
					if (!tokensBefore.isEmpty())
						ei = trTokens.indexOf(tokensBefore.getLast());
					else
						ei = 0;
					
					si = trTokens.indexOf(getHomeToken());
					
					// set the selection direction
					selectingForwards = false;
				} else {
					if (!selectingForwards) {
						// have a selection - go to line start from selection
						// start
						ei = trTokens.indexOf(tokensSelected.getLast()) + 1;
						WToken homeTok = getHomeToken(tokensSelected.get(0));
						if (homeTok != null)
							si = trTokens.indexOf(homeTok);
						else
							si = 0;
					} else {
						// remove from selection
						si = trTokens.indexOf(tokensSelected.get(0));
						WToken homeTok = getHomeToken(
								tokensSelected.getLast());
						if (homeTok != null)
							ei = trTokens.indexOf(homeTok);
						else
							ei = 0;
					}
				}
				if (ei > trTokens.size() - 1)
					ei = trTokens.size() - 1;
				if (ei > si)
					updateTextSelection(si, ei, true, selectingForwards);
				else
					updateCaret();
			}
		}
	};
	
	/** Return the token at the beginning of this line. */
	private WToken getHomeToken() {
		// get the current token and find what line it's on
		WToken tok = getCaretToken();
		return getHomeToken(tok);
	}

	/** Return the token at the beginning of the line on which the provided
	 *  token is located. */
	private WToken getHomeToken(WToken tok) {
		// get the current token and find what line it's on
		if (tok != null) {
			WTokenList lineTokens = trTokens.getTokenLine(tok);
			
			if (lineTokens != null) {
				if (stepNonTextTags)
					// grab the first token on the line
					return lineTokens.get(0);
				else
					// grab the first text token
					return lineTokens.findFirstTextToken(true, true);
			}
		}
		return null;
	}
	
	/** Action to move caret to the end of the current line (end key) */
	public Action moveCaretLineEnd = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			WToken endTok = getEndToken();
			if (endTok != null)
				// move the caret to the right of that token (unless it's a
				// newline)
				moveCaret(endTok, !endTok.isType(WTokenType.newLine));
		}
	};
	
	/** Select from caret position to end of line. */
	public Action selectToLineEnd = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the start and end indices
				int si, ei;
				if (tokensSelected.isEmpty()) {
					if (!tokensAfter.isEmpty())
						si = trTokens.indexOf(tokensAfter.get(0));
					else
						si = 0;
					
					ei = trTokens.indexOf(getEndToken());
					
					// set the selection direction
					selectingForwards = true;
				} else {
					if (selectingForwards) {
						// have a selection - go to line end from selection
						// end
						si = trTokens.indexOf(tokensSelected.get(0));
						WToken endTok = getEndToken(tokensSelected.getLast());
						if (endTok != null)
							ei = trTokens.indexOf(endTok);
						else
							ei = trTokens.size() - 1;
					} else {
						// remove from selection
						ei = trTokens.indexOf(tokensSelected.getLast());
						WToken endTok = getEndToken(tokensSelected.get(0));
						if (endTok != null)
							si = trTokens.indexOf(endTok);
						else
							si = trTokens.size() - 1;
					}
				}
				if (ei > si)
					updateTextSelection(si, ei, true, selectingForwards);
				else {
					if (!tokensAfter.isEmpty())
						moveCaret(tokensAfter.get(0));
					else
						moveCaret(trTokens.getLast());
				}
			}
		}
	};
	
	/** Return the token at the end of this line. */
	private WToken getEndToken() {
		// get the current token and find what line it's on
		WToken tok = getCaretToken();
		return getEndToken(tok);
	}

	/** Return the token at the end of the line on which the provided token is
	 *  located. */
	private WToken getEndToken(WToken tok) {
		// get the current token and find what line it's on
		if (tok != null) {
			WTokenList lineTokens = trTokens.getTokenLine(tok);
			
			if (lineTokens != null) {
				if (stepNonTextTags)
					// grab the last token on the line
					return lineTokens.get(lineTokens.size()-1);
				else {
					return lineTokens.findLastTextToken(true, true);
				}
			}
		}
		return null;	
	}
	
	/** Select all text in the current text rectangle. */
	public Action selectAll = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (!trTokens.isEmpty()) {
				int si = 0;
				int ei = trTokens.size() - 1;
				selectingForwards = true;
				updateTextSelection(si, ei, true, selectingForwards);
			}
		}
	};
	
	/** Select from caret position to start of text rectangle. */
	public Action selectToTrStart = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the start and end indices
				int si, ei;
				
				WToken firstTok;
				if (stepNonTextTags)
					firstTok = trTokens.get(0);
				else
					firstTok = trTokens.findFirstTextToken(true, true);
				
				if (firstTok != null)
					si = trTokens.indexOf(firstTok);
				else
					si = 0;
				
				if (tokensSelected.isEmpty()) {
					if (!tokensBefore.isEmpty())
						ei = trTokens.indexOf(tokensBefore.getLast());
					else
						ei = 0;
					
					// set the selection direction
					selectingForwards = false;
				} else {
					if (!selectingForwards) {
						// have a selection - go to start from selection end
						ei = trTokens.indexOf(tokensSelected.getLast()) + 1;
					} else {
						// this will deselect all
						ei = si;
					}
				}
				if (ei > trTokens.size() - 1)
					ei = trTokens.size() - 1;
				if (ei > si)
					updateTextSelection(si, ei, true, selectingForwards);
				else
					updateCaret();
			}
		}
	};

	/** Select from caret position to end of text rectangle. */
	public Action selectToTrEnd = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (trTokens != null && !trTokens.isEmpty()) {
				// get the start and end indices
				int si, ei;
				
				WToken lastTok;
				if (stepNonTextTags)
					lastTok = trTokens.getLast();
				else
					lastTok = trTokens.findLastTextToken(true, true);
				
				if (lastTok != null)
					ei = trTokens.indexOf(lastTok);
				else
					ei = trTokens.size() - 1;
				
				if (tokensSelected.isEmpty()) {
					if (!tokensAfter.isEmpty())
						si = trTokens.indexOf(tokensAfter.get(0));
					else
						si = 0;
					
					// set the selection direction
					selectingForwards = true;
				} else {
					if (selectingForwards) {
						// have a selection - go to end from selection start
						si = trTokens.indexOf(tokensSelected.get(0));
					} else {
						// this will deselect all
						si = ei;
					}
				}
				if (ei > si)
					updateTextSelection(si, ei, true, selectingForwards);
				else {
					if (!tokensAfter.isEmpty())
						moveCaret(tokensAfter.get(0));
					else
						moveCaret(trTokens.getLast());
				}
			}
		}
	};
	
	/** Action to move caret to the beginning of the current text rectangle
	 *  (Ctrl + Home) */
	public Action moveCaretTrBeginning = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the first token in the text rectangle (type depends on mode)
			if (trTokens.size() > 0) {
				WToken firstTok;
				if (stepNonTextTags)
					firstTok = trTokens.get(0);
				else
					firstTok = trTokens.findFirstTextToken(true, true);
				moveCaret(firstTok);
			}
		}
	};
	
	/** Action to move caret to the end of the current text rectangle (Ctrl +
	 *  End) */
	public Action moveCaretTrEnd = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the first token in the text rectangle (type depends on mode)
			if (trTokens.size() > 0) {
				WToken lastTok;
				if (stepNonTextTags)
					lastTok = trTokens.getLast();
				else
					lastTok = trTokens.findLastTextToken(true, true);
				moveCaret(lastTok, true);
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
						tok = tokensBefore.findLastTextToken(true, false);
					
					if (tok != null) {
						int i = selectedPage.getTokenIndex(tok);
						println("Deleting prev token '%s' at index %d",
								tok.toString(), i);
						
						// save the current MULTI string then remove the token
						saveState();
						selectedPage.removeToken(i);
						
						// update everything
						update();
						moveCaret(caretIndx-1);
					}
				}
			}
		}
	};
	
	/** Action triggered with the delete key (or delete button). */
	public IAction delete = new IAction("wysiwyg.multi_tag_dialog.delete") {
		public void doActionPerformed(ActionEvent e) {
			if (!tokensSelected.isEmpty()) {
				// if we have a selection, delete the selection
				println("Deleting selection");
				deleteSelection(true);
			} else if ((inTextRectMode() || inColorRectMode()) &&
					selectedRectangle != null && !caretOn) {
				// if we have a selected rectangle and no caret (indicated by
				// the empty token lists), delete the rectangle
				println("Deleting selected rectangle");
				deleteSelectedRectangle();
			} else if (inGraphicMode() && selectedGraphic != null) {
				println("Deleting selected graphic");
				deleteSelectedGraphic();
			} else {
				// if we don't have any selection, delete the token just
				// after the caret
				// delete the first token in this list if possible
				// (otherwise just don't do anything)
				println("Deleting a token");
				if (!tokensAfter.isEmpty()) {
					// get the next token and it's index in the page's list
					WToken tok = null;
					if (stepNonTextTags)
						tok = tokensAfter.remove(0);
					else
						tok = tokensAfter.findFirstTextToken(true, false);
					
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
		if (saveForUndo)
			saveState();
		
		// now find and delete each token, then clear the selection and update
		if (!tokensSelected.isEmpty())
			caretIndx = trTokens.indexOf(tokensSelected.get(0));
		for (WToken tok: tokensSelected) {
			// pay attention to our current mode
			if (tok.isPrintableText() || stepNonTextTags) {
				int i = selectedPage.getTokenIndex(tok);
				println("Removing token '%s' from %d", tok.toString(), i);
				selectedPage.removeToken(i);
			}
		}
		tokensSelected.clear();
		
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
		if ((inTextRectMode() || inColorRectMode())
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

			// if there was a selection, delete it first
			if (!tokensSelected.isEmpty())
				deleteSelection(false);
			
			// create a newline token
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

	/** Edit tag action */
	public IAction editTag = new IAction(
			"wysiwyg.multi_tag_dialog.edit") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// get the caret token from the controller
			WToken t = getCaretToken();
			
			if (t == null) {
				// try for a rectangle
				if (selectedRectangle != null)
					t = selectedRectangle.getRectToken();
			}
			if (t == null)
				// or a graphic
				t = selectedGraphic;
			
			if (t != null && canEditTag(t)) {
				// get the token type and open the proper form
				WTokenType tokType = t.getType();
				WMultiTagDialog d = WMultiTagDialog.construct(
						wc, tokType, t);
				if (d != null) {
					JInternalFrame f = desktop.show(d);
					f.requestFocusInWindow();
					d.requestFocusInWindow();
				}
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
		int pgi = caretPageIndx;
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
		
		// create the appropriate font tag token
		WtFont fTok = new WtFont(font.getNumber(),
				String.valueOf(font.getVersionID()));
		
		// if there is a selection, get the font of the first token after
		// if there are no tokens after, don't do anything
		WtFont afTok = null;
		if (!tokensSelected.isEmpty() && !tokensAfter.isEmpty()) {
			// get the next text token (not including newlines)
			WtTextChar tc = (WtTextChar)
					tokensAfter.findFirstTextToken(false, false);
			if (tc != null) {
				WFont wf = tc.getFont();
				afTok = new WtFont(wf.getNumber(),
						String.valueOf(wf.getVersionID()));
			}
		}
		addTextOptionToken(fTok, afTok);
		
		// TODO update the text selection on the sign panel since the font
		// size probably changed - we need to update the caret so the tokens
		// in tokensSelected have the correct sizes
		
		// update the toolbar
		updateTextToolbar();
	}
	
	/** Add a foreground color token at the current location. */
	public void setForegroundColor(DmsColor c) {
		// set the foreground color
		fgColor = c;
		
		// create the appropriate WtColorForeground token
		WtColorForeground cfTok = new WtColorForeground(
				c.red, c.green, c.blue);
		
		// if there is a selection, get the color of the first token after
		// if there are no tokens after, don't do anything
		WtColorForeground acfTok = null;
		if (!tokensSelected.isEmpty() && !tokensAfter.isEmpty()) {
			// get the next text token (not including newlines)
			WtTextChar tc = (WtTextChar)
					tokensAfter.findFirstTextToken(false, false);
			if (tc != null) {
				DmsColor ac = tc.getColor();
				acfTok = new WtColorForeground(ac.red, ac.green, ac.blue);
			}
		}
		addTextOptionToken(cfTok, acfTok);
	}
	
	/** Add a background color token at the beginning of the page. Uses a page
	 *  background color tag and not the deprecated color background tag. */
	public void setBackgroundColor(DmsColor c) {
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
		trimTextOptionTokens(WTokenType.justificationLine, false);
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
		trimTextOptionTokens(WTokenType.justificationPage, false);
		
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
	
	/** Clear the selected tokens of all tokens of type tokType.
	 *  @returns the number of tokens removed
	 */
	private void clearSelectionTokenType(WTokenType tokType) {
		// loop over at most the number of tokens in the selection
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
	 *  If a second token is provided, it is added after the current selection
	 *  (e.g. to change only the font/color in the selection).
	 */
	private void addTextOptionToken(WToken toTok, WToken afterTok) {
		// save state here so we can undo this whole process
		saveState();
		
		// trim any tokens of the same type around the current caret location
		// and/or in the selection
		// we will force trimming after too even if there is a selection if we
		// are going to add a token after the selection
		boolean trimAfter = afterTok != null;
		trimTextOptionTokens(toTok.getType(), trimAfter);
		
		// add the first token at the caret
		selectedPage.addToken(caretPageIndx, toTok);
		++caretIndx;
		++caretPageIndx;
		
		// if there is a selection and we got an afterTok, add that token 
		// after the selection (to set the option back to what it was before
		if (!tokensSelected.isEmpty() && afterTok != null)
			addTokenAfterSelection(afterTok);
		
		update();
		updateCaret();
	}
	
	/** Trim (remove) any tokens of the type tokType that are immediately
	 *  before the caret. If there is a selection, the selection is cleared of
	 *  tokens of this type as well. If there is no selection or if forceAfter
	 *  is true, tokens after the caret will be trimmed.
	 */
	private void trimTextOptionTokens(WTokenType tokType, boolean forceAfter) {
		// trim backwards
		trimTextOptToksBw(tokType);
		
		// if we have a selection, just clear it of these tokens
		if (!tokensSelected.isEmpty()) {
			clearSelectionTokenType(tokType);
			if (forceAfter)
				trimTextOptToksFw(tokType);
		} else {
			trimTextOptToksFw(tokType);
		}
	}
	
	/** Trim (remove) any tokens of the type tokType that are immediately
	 *  before the caret.
	 */
	private void trimTextOptToksBw(WTokenType tokType) {
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
			// the left - do it "manually" since we don't want to change token
			// lists yet
			if (caretIndx > 0)
				--caretIndx;
			if (caretPageIndx > 0)
				--caretPageIndx;
		}
	}
	
	/** Trim (remove) any tokens of the type tokType that are immediately
	 *  after the caret.
	 */
	private void trimTextOptToksFw(WTokenType tokType) {
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
	
	/** Add the token to the selected page at the caret index. */
	public void addToken(WToken tok) {
		// add the token at the caret
		selectedPage.addToken(caretPageIndx, tok);
		
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
		updateNonTextTagInfo();
		editor.requestFocusInWindow();
		signPanel.requestFocusInWindow();
	}
	
	/** Replace the token oldTok with token newTok. If the first token cannot
	 *  be found, nothing happens (?).
	 */
	public void replaceToken(WToken oldTok, WToken newTok) {
		println("Replacing %s with %s", oldTok.toString(), newTok.toString());
		
		// remove the old token from the page
		int ti = selectedPage.getTokenIndex(oldTok);
		
		if (ti >= 0) {
			// if it existed remove it then add the new one at ti
			selectedPage.removeToken(oldTok);
			selectedPage.addToken(ti, newTok);
			update();
			updateCaret();
		} else
			println("%s not found!", oldTok.toString());
		updateNonTextTagInfo();
		editor.requestFocusInWindow();
		signPanel.requestFocusInWindow();
	}
	
	/** Add the token to the selected page after the selection. Does not
	 *  update anything.
	 */
	private void addTokenAfterSelection(WToken tok) {
		// calculate where the token will be added, then add it
		selectedPage.addToken(Math.min(caretPageIndx + tokensSelected.size(),
				selectedPage.getNumTokens()), tok);
	}
	
	/** Save the current state on the stack for undoing. Resets the redo stack. */
	public void saveState() {
		WHistory wh = new WHistory(multiString, caretIndx,
				tokensSelected.size(), selectedPageIndx, fgColor, bgColor);
		undoStack.add(wh);
		
		// reset the redo stack, since reapplying those changes is not trivial
		// after another change is made
		redoStack.clear();
	}
	
	/** Undo the last message change by reverting to the previous MULTI string. */
	public Action undo = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (!undoStack.isEmpty()) {
				// save the current state on the redo stack so the action can
				// be redone
				WHistory wh = new WHistory(multiString, caretIndx,
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
			if (!redoStack.isEmpty()) {
				// put the current state back on the undo stack so the action can
				// be undone
				WHistory wh = new WHistory(multiString, caretIndx,
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
		// apply the old MULTI string - this will change the rendering
		setMultiString(wh.getMultiString(), false);
		
		update();
		
		moveCaret(wh.getCaretIndex());
	}
	
	/** Toggle focus between editor and page list */
	public Action toggleFocus = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (signPanel.isFocusOwner())
				pageList.requestFocusInWindow();
			else if (pageList.isFocusOwner())
				signPanel.requestFocusInWindow();
		}
	};
	
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
		return multiString;
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
		if (wmsg == null) {
			if  (multiString != null) {
				wmsg = new WMessage(multiString);
			}
		} else {
			// if we already have a WMessage object, use it to update the
			// MULTI string then use that to re-render
			multiString = wmsg.toString();
		}
		
		// clear any errors before re-rendering
		errMan.clearErrors();
		
		if (multiConfigUseable() && wmsg != null) {
			wmsg.renderMsg(multiConfig, errMan);
			
			// set the WYSIWYG image size on the pages
			if (signPanel != null) {
				try {
					wmsg.setWysiwygImageSize(
							signPanel.getWidth(), signPanel.getHeight());
				} catch (InvalidMsgException e) {
					if (DEBUG)
						e.printStackTrace();
				}
			}
		}
		
		// check for errors from the renderer
		if (errMan.hasErrors()) {
			// show or update the dynamic error panel to tell the user what's
			// wrong
			updateErrorPanel();
		} else
			// if there were no errors, make a note of it
			noErrors();
	}
	
	/** Update our current MULTI string, either from the WMessage (in WYSIWYG
	 *  mode) or from the MULTI panel (if MULTI mode is forced).
	 */
	public void updateMultiString() {
		if (forceMULTI)
			// get the MULTI string from the MULTI panel
			multiString = editor.getMultiPanelContents();
		else
			// get the MULTI string from the wmsg and save it
			multiString = wmsg.toString();
	}
	
	/** Return whether or not the current message text has been saved. */
	public boolean isMessageSaved() {
		// check the MULTI string against the current QuickMessage
		updateMultiString();
		boolean isSaved = multiString.equals(qm.getMulti());
		
		// also check prefix flag
		isSaved = (editor.getPrefixPage() == qm.getPrefixPage()) && isSaved;
		
		return isSaved;
	}
	
	/** Save the current MULTI string in the quick message */
	public Action saveMessage = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// update our MULTI string then save the message
			updateMultiString();
			qm.setMulti(multiString);
			qm.setPrefixPage(editor.getPrefixPage());
		}
	};
	
	/** Save the current MULTI string in the quick message */
	WController wc = this;
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
		
		// first update our MULTI string
		updateMultiString();
		
		// use a worker to do this part since it could take a bit
		IWorker<QuickMessage> worker = new IWorker<QuickMessage>() {
			@Override
			protected QuickMessage doInBackground() {
				// make the quick message
				TypeCache<QuickMessage> qmCache = session.getSonarState().
						getDmsCache().getQuickMessages();
				HashMap<String, Object> qmAttrs =
						new HashMap<String, Object>();
				qmAttrs.put("multi", multiString);
				qmAttrs.put("prefix_page", editor.getPrefixPage());
				if (sg != null)
					qmAttrs.put("sign_group", sg);
				else {
					// if we're not editing a sign group, get the single-sign
					// group
					SignGroup ssg = SignGroupHelper.lookup(sign.getName());
					qmAttrs.put("sign_group", ssg);
				}
				// save the object, then get the object and set our quick message
				qmCache.createObject(msgName, qmAttrs);
				QuickMessage q = null;
				
				// wait for SONAR to create the new message
				long tStart = System.nanoTime();
				while (q == null) {
					q = QuickMessageHelper.lookup(msgName);
					long tElapsed = System.nanoTime() - tStart;
					if (tElapsed > MAX_WAIT)
						break;
				}
				return q;
			}
			
			@Override
			public void done() {
				QuickMessage q = getResult();
				if (q != null) {
					// if we get a quick message, set it as the current one 
					qm = q;
					
					// update the window title too
					editor.setWindowTitle(qm);
				} else {
					// if we didn't, pop up a dialog telling the user that
					// there was an error and they are still editing the same
					// message
					desktop.show(new WMsgErrorDialog(
							"wysiwyg.new_message.saveas_error"));
				}
			}
		};
		worker.execute();
	}
	
	/** Update the controller with a MULTI string (this action can be undone) */
	public void setMultiString(String ms, boolean undoable) {
		if (undoable)
			// save state to allow undoing
			saveState();
		
		multiString = ms;
		wmsg.parseMulti(multiString);
		update();
	}
	
	public void update() {
		update(true);
	}
	
	public void update(boolean focus) {
		renderMsg();
		updateMultiPanel();
		
		if (!forceMULTI) {
			updatePageListModel();
			updateCursor();
			
			if (signPanel != null && focus)
				// give focus to the sign panel if requested
				signPanel.requestFocusInWindow();
		}
	}
	
	/** Update the text toolbar if one is available. */
	private void updateTextToolbar() {
		if (editor != null && !forceMULTI)
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
				
				// if we're in MULTI mode, check the token type and see if we
				// should enable the Edit Tag button
				if (inMultiTagMode())
					editor.updateTagEditButton(canEditTag(tok));
			}
		} else {
			editor.updateTagEditButton(false);
		}
		editor.updateNonTextTagInfo(s, c);
	}
	
	/** Return whether or not this tag can be edited (any tag besides a text
	 *  character).
	 */
	private boolean canEditTag(WToken tok) {
		return !tok.isPrintableText() || tok.isType(WTokenType.newLine);
	}
	
	/** Update the MULTI panel with the current MULTI string. */
	private void updateMultiPanel() {
		if (multiPanel != null && multiString != null) {
			multiPanel.setText(multiString);
			
			// highlight any error-producing tokens in the MULTI panel
			if (errMan.hasErrors())
				multiPanel.highlightErrors(errMan);
		}
	}
	
	/** Update the WPageList given the current MULTI string and MultiConfig. */
	private void updatePageListModel() {
		// update the page list and the selected page
		if (pageList != null && wmsg != null && wmsg.isValid()) {
			pageList.updatePageList(wmsg);
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
		}
		
		// update rectangle and graphics handling for the selected page
		updateRectangles();
		updateGraphics();
	}
	
	/** Edit the page timing for the selected page. Inserts a page timing tag
	 *  at the beginning of the page (replacing any that existed before).
	 */
	public void editSelectedPageTiming() {
		// move the caret to the beginning of the page
		caretPageIndx = 0;
		
		// find any page timing tokens on the page and clean them up
		WtPageTime oldTimeTok = null;
		WTokenList pgTimeToks = selectedPage.getTokensOfType(
				WTokenType.pageTime);
		if (!pgTimeToks.isEmpty()) {
			// get the last one (it's the one that applies)
			oldTimeTok = (WtPageTime) pgTimeToks.getLast();
			
			// remove all from the page and move the active one to the start
			for (WToken t: pgTimeToks)
				selectedPage.removeToken(t);
			selectedPage.addToken(0, oldTimeTok);
		}
		
		// open a tag edit dialog
		WMultiTagDialog ptDialog = WMultiTagDialog.construct(
				this, WTokenType.pageTime, oldTimeTok);
		desktop.show(ptDialog);
	}
	
	/** Action to edit selected page timing */
	public Action editPageTimingAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			editSelectedPageTiming();
		}
	};
	
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
			if (wr != null && !wr.isWysiwygInitialized() && signPanel != null)
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
		
		// in text mode, edit the whole-sign rectangle
		// note that modeRects is reversed, so the whole-sign rect. is last
		if (inTextMode())
			selectedRectIndx = modeRects.size()-1;
		else if (inMultiTagMode() && selectedRectIndx == -1)
			// in MULTI tag mode default to whole-sign if nothing is selected
			selectedRectIndx = modeRects.size()-1;
		
		// get the WgRectangle object (or null) and set the selected rectangle 
		if (selectedRectIndx < 0 || selectedRectIndx >= modeRects.size())
			setSelectedRectangle(null);
		else if (inTextMode() || inMultiTagMode()
				|| inTextRectMode() || inColorRectMode())
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
			trTokens = selectedTextRect.getTokenList();
		} else {
			// reset the selected text rectangle
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
	
	public boolean inTextMode() {
		return editingMode == MODE_TEXT;
	}
	
	public void activateTextMode() {
		// put the cursor in text mode then update everything
		editingMode = MODE_TEXT;
		setCursorFromMode();
		
		// set step mode back to previous value (if we were in MULTI mode)
		stepNonTextTags = snttNonMultiMode;
		editor.updateNonTextTagButton(stepNonTextTags);
		
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
		
		// set step mode back to previous value (if we were in MULTI mode)
		stepNonTextTags = snttNonMultiMode;
		editor.updateNonTextTagButton(stepNonTextTags);
		
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
		
		// set step mode back to previous value (if we were in MULTI mode)
		stepNonTextTags = snttNonMultiMode;
		editor.updateNonTextTagButton(stepNonTextTags);
		
		update();
		updateCaret();
	}

	public boolean inColorRectMode() {
		return editingMode == MODE_COLORRECT;
	}
	
	public void activateColorRectangleMode() {
		editingMode = MODE_COLORRECT;
		setCursorFromMode();
		
		// set step mode back to previous value (if we were in MULTI mode)
		stepNonTextTags = snttNonMultiMode;
		editor.updateNonTextTagButton(stepNonTextTags);
		
		update();
		updateCaret();
	}
	
	public boolean inMultiTagMode() {
		return editingMode == MODE_MULTITAG;
	}
	
	public void activateMultiTagMode() {
		editingMode = MODE_MULTITAG;
		setCursorFromMode();
		
		// check the step mode and save it for later
		snttNonMultiMode = stepNonTextTags;
		
		// in this mode we always step through non-text tags
		stepNonTextTags = true;
		editor.updateNonTextTagButton(stepNonTextTags);
		
		update();
		updateCaret();
	}
	
	public Cursor getCursor() {
		return cursor;
	}
	
	public MultiConfig getMultiConfig() {
		return multiConfig;
	}
	
	public MultiConfig getSignGroupMultiConfig() {
		return signGroupMultiConfig;
	}
	
	public boolean multiConfigUseable() {
		return multiConfig != null && multiConfig.isUseable();
	}
	
	/** Return the color scheme associated with the current MultiConfig. If
	 *  there is no active MultiConfig, UNKNOWN is returned.
	 */
	public ColorScheme getColorScheme() {
		if (multiConfigUseable())
			return multiConfig.getColorScheme();
		return ColorScheme.UNKNOWN;
	}
	
	public ProxyListModel<Font> getFontModel() {
		if (session != null) {
			return session.getSonarState().getDmsCache().getFontModel();
		} return null;
	}
	
	/** Update the list model of graphics supported by the current MultiConfig
	 *  based on the dimensions and color scheme of the sign.
	 */
	public void updateGraphicModel() {
		// get sign parameters from the MultiConfig
		if (multiConfigUseable()) {
			int maxHeight = multiConfig.getPixelHeight();
			int maxWidth = multiConfig.getPixelWidth();
			int maxColorScheme = multiConfig.getColorScheme().ordinal();
			
			// clear the list we have before updating
			supportedGraphics.removeAllElements();
			
			// filter the list of all graphics available to ones we support
			Iterator<Graphic> it = GraphicHelper.iterator();
			while (it.hasNext()) {
				Graphic g = it.next();
				if (g.getHeight() <= maxHeight && g.getWidth() <= maxWidth
						&& g.getColorScheme() <= maxColorScheme)
					supportedGraphics.addElement(g);
			}
		} else
			supportedGraphics.removeAllElements();
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
	
	/** Get the current DMS object */
	public DMS getSign() {
		return sign;
	}
	
	/** Return whether or not the current message is a prefix page. */
	public boolean getPrefixPage() {
		return prefixPage;
	}
	
}
