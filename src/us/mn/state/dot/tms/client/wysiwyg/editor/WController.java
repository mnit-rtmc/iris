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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.tms.Font;
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
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.client.wysiwyg.editor.action.WDeleteToken;
import us.mn.state.dot.tms.utils.wysiwyg.WFontCache;
import us.mn.state.dot.tms.utils.wysiwyg.WGraphicCache;
import us.mn.state.dot.tms.utils.wysiwyg.WMessage;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;
import us.mn.state.dot.tms.utils.wysiwyg.WRaster;
import us.mn.state.dot.tms.utils.wysiwyg.WState;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenList;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtJustLine;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtNewLine;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTextChar;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
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
	
	/** Keep a list of actions that have been performed and undone */
	// TODO make a new list type if it seems like it would be useful
	private ArrayList<WAction> actionsDone = new ArrayList<WAction>();
	private ArrayList<WAction> actionsUnDone = new ArrayList<WAction>();
	
	/** Keep a list of MULTI strings for undoing/redoing */
	private ArrayList<WHistory> undoStack = new ArrayList<WHistory>();
	private ArrayList<WHistory> redoStack = new ArrayList<WHistory>();
	
	/** Cursor that will change depending on mode, etc. */
	// TODO should we make these final or have an initCursors method???
	private final Cursor textCursor = new Cursor(Cursor.TEXT_CURSOR);
	private final Cursor graphicCursor = new Cursor(Cursor.HAND_CURSOR);
	private final Cursor colorRectCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
	private final Cursor textRectCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
	private final Cursor multiTagCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	private final Cursor moveCursor = new Cursor(Cursor.MOVE_CURSOR);
	private Cursor cursor = textCursor;
	
	/** Mouse selection parameters */
	private int lastPressX;
	private int lastPressY;
	private WToken startTok;
	private WToken endTok;
	
	/** Sign/Group and Message being edited */
	private DMS sign;
	private SignGroup sg;
	private QuickMessage qm;
	private MultiString multiString;
	private String multiStringText = null;
	
	/** MultiConfig for config-related stuff  */
	private MultiConfig multiConfig;
	
	/** Token lists for helping with caret placement and stuff */
	private WTokenList tokensBefore = new WTokenList();
	private WTokenList tokensSelected = new WTokenList();
	private WTokenList tokensAfter = new WTokenList();
	private int caretIndx = 0;
//	private boolean caretAtEnd = false;
	public final static int CARET_EOP = -1;
	
	/** Font and graphics caches */
	private WFontCache fontCache = new WFontCache();
	private WGraphicCache graphicCache = new WGraphicCache();
	
	/** WMessage for working with rendered message */
	private WMessage wmsg = null;
	
	/** Current Font
	 *  TODO need some model for this, I don't think it can just be one */
	private Font currentFont;
	
	/** Current Colors */
	private DmsColor fgColor;
	private DmsColor bgColor;
	
	/** Page list */
	private WPageList pageList;
	
	/** DMS List (for sign groups) */
	private Map<String,DMS> dmsList;
	private JComboBox<String> dms_list;
	String[] dmsNames;
	
	/** Currently selected page (defaults to first available) */
	// TODO need to initialize this so there is always a selected page (blank
	// if the message is empty)
	private int selectedPageIndx = 0;
	private WPage selectedPage;
	
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
	
	/** Perform some initialization on the controller. Sets the editor form
	 *  handle, sets up the mouse cursor, etc.
	 */
	public void init(WMsgEditorForm e) {
		editor = e;
		session = editor.getSession();
		desktop = session.getDesktop();
		
		// initialize the cursor, starting in text mode
		cursor = new Cursor(Cursor.TEXT_CURSOR);

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
		// initialize the caret
		update();
		initCaret();
		
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
//		System.out.println("From QuickMessage: " + multiStringText);
		
		update();
	}

	/** Handle a click on the main editor panel */
	public void handleClick(MouseEvent e) {
		// calculate the adjusted coordinates of the click
//		Point2D pSign = transformSignCoordinates(e.getX(), e.getY());
		
		// just print for now
//		if (pSign != null) {
		int b = e.getButton();
		int x = e.getX();
		int y = e.getY();
		System.out.println(String.format(
				"Mouse clicked at (%d, %d) ...", x, y));
		update();
		WToken tok = findClosestToken(x, y);
		
		// move the caret based on the token we got
		if (tok != null) {
			// if this is the last token, check if they clicked towards the right
			// of it - they probably wanted the end of the page
			if (selectedPage.isLast(tok) && rightHalf(x, tok)) {
				moveCaret(CARET_EOP);
			} else
				// otherwise just move the caret to this token
				moveCaret(tok);
		}
		
		// get focus for this component when someone clicks on it
		signPanel.requestFocusInWindow();
//		}
	}
	
	/** Handle a mouse move event on the main editor panel */
	public void handleMouseMove(MouseEvent e) {
		// calculate the adjusted coordinates of the mouse pointer
//		Point2D pSign = transformSignCoordinates(e.getX(), e.getY());
		
		// just print for now
//		if (pSign != null) {
		int x = e.getX();
		int y = e.getY();
		
		// TODO test code for cursor changing
//		if (y >= 100 && y <= 160) {
//			cursor = moveCursor;
//		} else {
//			setCursorFromMode();
//		}
//		update();
			
			// TODO hook this into token finding and mouse cursor changing
			
//		System.out.println(String.format(
//				"Mouse moved to (%d, %d) ...", x, y));
//		}
	}
	
	/** Handle a mouse pressed event  */
	public void handleMousePressed(MouseEvent e) {
		// TODO only handling left-click drag for now
		if (e.getButton() == MouseEvent.BUTTON1) {
			// set the press coordinates and get the closest token
			lastPressX = e.getX();
			lastPressY = e.getY();
			startTok = findClosestToken(lastPressX, lastPressY);
		} else {
			lastPressX = -1;
			lastPressY = -1;
			startTok = null;
		}
	}

	/** Handle a mouse drag event on the main editor panel */
	public void handleMouseDrag(MouseEvent e) {
		// TODO only handling left-click drag for now
		if (SwingUtilities.isLeftMouseButton(e)) {
			// TODO only doing basic text selection for now
			// update which token is under the cursor
			int x = e.getX();
			int y = e.getY();
			updateMouseDrag(x, y);
		}
	}
	
	private void updateMouseDrag(int x, int y) {
		endTok = findClosestToken(x, y);
		
		// update the selection if they're not the same token
		if ((startTok != endTok) && startTok != null && endTok != null) {
			// get the indeces and figure out which is first
			int start = selectedPage.getTokenIndex(startTok);
			int end = selectedPage.getTokenIndex(endTok);
			
			boolean includeEnd = false;
			int si;
			int ei;
			if (start < end) {
				si = start;
				ei = end;
				includeEnd = rightHalf(x, endTok);
			} else {
				// if they are selecting backwards, we need to use the initial
				// click and token and reverse the indeces
				si = end;
				ei = start;
				includeEnd = rightHalf(lastPressX, startTok);
			}
			updateTextSelection(si, ei, includeEnd);
		}
	}
	
	public void handleMouseReleased(MouseEvent e) {
		// finalize the drag motion
		int x = e.getX();
		int y = e.getY();
		updateMouseDrag(x, y);
		
		// if the x and y are the same as the last press, do nothing
		// (handleClick SHOULD take care of it...)
		if ((x != lastPressX && y != lastPressY)
				&& (lastPressX != -1 && lastPressY != -1)) {
//			System.out.println(String.format(
//					"Mouse dragged from (%d, %d) to (%d, %d) ...",
//					lastPressX, lastPressY, x, y));
		}
		
		// reset the drag handler
		lastPressX = -1;
		lastPressY = -1;
		startTok = null;
		endTok = null;
	}
	
	/** Find the closest token on the page given a set of click coordinates. */
	public WToken findClosestToken(int x, int y) {
		// get the sign coordinates for working with the message
		WRaster wr = selectedPage.getRaster();
		int sx = wr.cvtWysiwygToSignX(x);
		int sy = wr.cvtWysiwygToSignY(y);
		
		// find the closest token on this page and return it
		WToken tok = selectedPage.findClosestToken(sx, sy);
		return tok;
	}
	
	/** Determine if the coordinate x is on the left half of token tok. */
	private boolean leftHalf(int x, WToken tok) {
		WRaster wr = selectedPage.getRaster();
		int sx = wr.cvtWysiwygToSignX(x);
		return sx < tok.getCentroidX();
	}	
	
	/** Determine if the coordinate x is on the right half of token tok. */
	private boolean rightHalf(int x, WToken tok) {
		WRaster wr = selectedPage.getRaster();
		int sx = wr.cvtWysiwygToSignX(x);
		return sx > tok.getCentroidX();
	}
	
	/** Initialize the caret. If there is text in the message, it is placed at
	 *  the beginning of the message (before the first printable character).
	 *  If there is no text, it goes...somewhere. */
	private void initCaret() {
		// if the page isn't empty, put the caret at 0
		if (!selectedPage.getTokenList().isEmpty())
			moveCaret(0);
		else {
			// if it is, put it at the end of the page
			moveCaret(CARET_EOP);
		}
		// TODO what to do if not???
	}
	
	/** Update text selection given the token indeces provided (set by mouse
	 *  events). 
	 */
	private void updateTextSelection(int si, int ei, boolean includeEnd) {
		// slice the token list into 3
		if (includeEnd)
			++ei;
		WTokenList pgTokens = selectedPage.getTokenList();
		tokensBefore = pgTokens.slice(0, si);
		tokensAfter = pgTokens.slice(ei, pgTokens.size());
		tokensSelected = pgTokens.slice(si, ei);
//		System.out.println(String.format("Before: %s", tokensBefore.toString()));
//		System.out.println(String.format("Selected: %s", tokensSelected.toString()));
//		System.out.println(String.format("After: %s", tokensAfter.toString()));
		
		// tell the image panel what the selection is
		signPanel.setTextSelection(tokensSelected);
		
		// TODO move the caret too
	}
	
	/** Move the caret to the spot just before the specified token. Note that
	 *  this doesn't select the token.
	 */
	public void moveCaret(WToken tok) {
		// get a list of tokens on this page and find this token in the list
		int tokIndx = selectedPage.getTokenIndex(tok);
		
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
		WTokenList pgTokens = selectedPage.getTokenList();
		if (caretIndx != CARET_EOP) {
//			System.out.println(String.format("Caret at %d", caretIndx));
			
			// slice the list at the token
			tokensBefore = pgTokens.slice(0, caretIndx);
			tokensAfter = pgTokens.slice(caretIndx, pgTokens.size());
			
			// set the new caret location
			WToken tok = getCaretToken();
			
			// put the caret at that token
			if (tok != null) {
				signPanel.setCaretLocation(tok);
			}
		} else {
//			System.out.println("Caret at end");
			tokensBefore = pgTokens;
			tokensAfter = new WTokenList();
			int x = selectedPage.getEOPX();
			int y = selectedPage.getEOPY();
			int h = selectedPage.getEOPH();
			signPanel.setCaretLocation(x, y, h);
		}
		// either way reset the selection
		tokensSelected.clear();
		signPanel.clearTextSelection();
	}
	
	/** Get the token associated with the current caret position. This is
	 *  either the first token after the caret or the last token before
	 *  the caret, or null if neither is valid. */
	private WToken getCaretToken() {
		WToken tok = null;
		if (tokensAfter.isEmpty() && !tokensBefore.isEmpty()) {
			tok = tokensBefore.getLast();
		} else if (!tokensAfter.isEmpty()) {
			tok = tokensAfter.get(0);
		}
		return tok;
	}
	
	/** Action to move caret to left (using left arrow key) */
	public Action moveCaretLeft = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// if the caret is at the end of the page, put it before the
			// last token (if there are any)
			if (caretIndx == CARET_EOP && selectedPage.getNumTokens() > 0) {
				moveCaret(selectedPage.getNumTokens()-1);
			} else {
				// otherwise decrement the caret index, but don't go below 0
				if (caretIndx >= 1)
					moveCaret(caretIndx-1);
			}
		}
	};
	
	/** Action to move caret to right (using right arrow key) */
	public Action moveCaretRight = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// if we're on the last token, just go to the end of the message
			if (caretIndx == selectedPage.getNumTokens()-1) {
				moveCaret(CARET_EOP);
			} else {
				// otherwise increment the caret index
				moveCaret(caretIndx+1);
			}
		}
	};
	
	/** Action to move caret up one line (using up arrow key) */
	public Action moveCaretUp = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// get the current token
			WToken tok = getCaretToken();
			
			// get the line this token is on
			int li = selectedPage.getLineIndex(tok);
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
			int li = selectedPage.getLineIndex(tok);
			if (li < selectedPage.getNumLines()-1) {
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
	
	/** Return the closest token on the line with index lineIndx to the x
	 *  coordinate provided (in sign coordinates.
	 */
	private WToken getClosestTokenOnLine(int lineIndx, int sx) {
		WTokenList line = selectedPage.getLines().get(lineIndx);
		Iterator<WToken> it = line.iterator();
		int minDist = 999999;
		WToken tok = null;
		while (it.hasNext()) {
			WToken t = it.next();
			int xd = Math.abs(t.getCoordX() - sx);
			if (xd < minDist) {
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
					// get the last token and it's index in the page's list
					WToken tok = tokensBefore.getLast();
					int i = selectedPage.getTokenIndex(tok);
//					System.out.println(String.format("Deleting token '%s' at index %d",
//							tok.toString(), i));
					
					// save the current MULTI string then remove the token
					saveState();
					selectedPage.removeToken(i);
										
					// update everything
					update();
					
					// only move the caret if it's not at the end
					if (caretIndx != CARET_EOP)
						moveCaret(i);
					else
						// otherwise just update
						updateCaret();
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
			} else {
				// if we don't have any selection, delete the token just
				// after the caret
				// delete the first token in this list if possible
				// (otherwise just don't do anything)
				if (!tokensAfter.isEmpty()) {
					// get the last token and it's index in the page's list
					WToken tok = tokensAfter.remove(0);
					int i = selectedPage.getTokenIndex(tok);
//					System.out.println(String.format("Deleting token '%s' at index %d",
//							tok.toString(), i));
					
					// if we're going to delete the last token, put the caret
					// at the end of the message
					if (tok == selectedPage.getTokenList().getLast())
						caretIndx = CARET_EOP;
					
					// save the current MULTI string then remove the token
					saveState();
					selectedPage.removeToken(i);
					
					// update everything
					update();
					
					// only move the caret if it's not at the end
					if (caretIndx != CARET_EOP)
						moveCaret(i);
					else
						// otherwise just update
						updateCaret();
				}
			}
			
		}
	};
	
	/** Delete all tokens in the selection. If saveForUndo is true, the state
	 *  is saved before deleting the tokens. */
	public void deleteSelection(boolean saveForUndo) {
		// TODO save the selection to reset it after undoing
		if (saveForUndo)
			saveState();
		
		// now find and delete each token, then clear the selection and update
		for (WToken tok: tokensSelected) {
			int i = selectedPage.getTokenIndex(tok);
			System.out.println(String.format("Removing token '%s' from %d", tok.toString(), i));
			selectedPage.removeToken(i);
		}
		tokensSelected.clear();
		
		// figure out where to put the caret
		if (!tokensBefore.isEmpty()) {
			// if there are tokens before, put after the last one
			if (!tokensAfter.isEmpty()) {
				WToken tok = tokensBefore.getLast();
				caretIndx = selectedPage.getTokenIndex(tok) + 1;
			} else
				// if there are no tokens after, it's the EOP
				caretIndx = CARET_EOP;
		} else if (!tokensAfter.isEmpty()) {
			// if there are no tokens before but there are after, go to 0
			caretIndx = 0;
		} else {
			// if the page is empty, go to EOP
			caretIndx = CARET_EOP;
		}
		
		// update then move the caret
		update();
		updateCaret();
	}
	
	/** Add a single ASCII character to the message at the caret location. */
	public void typeChar(char c) {
//		System.out.println(String.format("Typed: '%c'", c));
		
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
	
	/** Add a newline character at the current position */
	public Action addNewLine = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
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
	
	/** Add a line justify left token at the current location. */
	public Action lineJustifyLeft = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// create the appropriate WtJustLine token then pass it to
			// addJustLineToken
			WtJustLine jlTok = new WtJustLine(JustificationLine.LEFT);
			addJustLineToken(jlTok);
		}
	};

	/** Add a line justify center token at the current location. */
	public Action lineJustifyCenter = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// create the appropriate WtJustLine token then pass it to
			// addJustLineToken
			WtJustLine jlTok = new WtJustLine(JustificationLine.CENTER);
			addJustLineToken(jlTok);
		}
	};
	
	/** Add a line justify center token at the current location. */
	public Action lineJustifyRight = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// create the appropriate WtJustLine token then pass it to
			// addJustLineToken
			WtJustLine jlTok = new WtJustLine(JustificationLine.RIGHT);
			addJustLineToken(jlTok);
		}
	};
	
	/** Add the line justify token provided at the current caret location. If
	 *  the token immediately preceding is also a line justify token it is
	 *  replaced, otherwise it is added.
	 */
	private void addJustLineToken(WtJustLine jlTok) {
		// save state here so we can undo this whole process
		saveState();
		
		// check the type of the preceding token
		if (tokensBefore.size() > 0) {
			WToken tok = tokensBefore.getLast();
			
			if (tok.getClass() == WtJustLine.class) {
				// if it's a line justify, delete it (but don't create a
				// separate undo step)
				int i = selectedPage.getTokenIndex(tok);
				selectedPage.removeToken(i);
			}
		}
		
		// now add the token
		addToken(jlTok);
	}
	
	/** Add the token to the selected page at the caret index */
	private void addToken(WToken tok) {
		// if we're already at the end of the page, add the token there
		// TODO TEMPORARY - FIGURE OUT CARET PLACEMENT (DON'T THINK WE NEED caretAtEnd)
		if (caretIndx == CARET_EOP)
			selectedPage.addToken(tok);
		else
			// otherwise put it at the caret index
			selectedPage.addToken(caretIndx, tok);
		
		// either way update then move the caret one more token (just behind
		// the character that was just added)
		update();
		
		// TODO we may change this into something else (checking a parameter)
		// TODO we need to be more careful than this...
		if (tok.isType(WTokenType.textChar)) {
			if (caretIndx == CARET_EOP)
				moveCaret(CARET_EOP);
			else
				moveCaret(caretIndx+1);
			
		}
	}
	
	/** Save the current state on the stack for undoing. Resets the redo stack. */
	private void saveState() {
		WHistory wh = new WHistory(multiStringText, caretIndx,
				tokensSelected.size());
		undoStack.add(wh);
		
		// reset the redo stack, since reapplying those changes is not trivial
		// after another change is made
		redoStack.clear();
	}
	
	/** Undo the last message change by reverting to the previous MULTI string. */
	public Action undo = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
//			System.out.println("Starting undo...");
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
//			System.out.println("Starting redo...");
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
		setMultiString(wh.getMultiString());
		
		// TODO handle selection and selected page
		
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
	
	/** Render the message using the current MULTI String and MultiConfig */
	private void renderMsg() {
		// update the WMessage object and re-render if we have a MultiConfig
//		System.out.println("In renderMsg: " + multiStringText);
		if (wmsg == null) {
			if  (multiStringText != null)
//			System.out.println("Making wmsg with: " + multiStringText);
				wmsg = new WMessage(multiStringText);
		} else {
			// if we already have a WMessage object, use it to update the
			// MULTI string then use that to re-render
			multiStringText = wmsg.toString();
//			System.out.println("Remaking wmsg with: " + multiStringText);
			wmsg = new WMessage(multiStringText);
		}
//		System.out.println(multiStringText);
		if (multiConfig != null && wmsg != null)
			wmsg.renderMsg(multiConfig);
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
	
	/** Update the controller with a MULTI string */
	public void setMultiString(String ms) {
		multiStringText = ms;
		wmsg.parseMulti(multiStringText);
		update();
	}
	
	/** Update everything that needs updating */
	public void update() {
//		System.out.println("In update: " + multiStringText);
		renderMsg();
		updateMultiPanel();
		updatePageListModel();
		updateCursor();
		
		// TODO add more stuff here eventually
	}
	
	/** Update the MULTI panel with the current MULTI string. */
	private void updateMultiPanel() {
		if (multiPanel != null && multiStringText != null)
			multiPanel.setText(multiStringText);
	}
	
	/** Update the WPageList given the current MULTI string and MultiConfig. */
	private void updatePageListModel() {
		// update the page list and the selected page
		if (pageList != null) {
			pageList.updatePageList(multiStringText, multiConfig);
			updateSelectedPage();
		}
	}
	
	/** Update the selected page to use one in the current page_list_model. */
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
		
		if (editor != null) {
			editor.setPageNumberLabel(getPageNumberLabel(selectedPageIndx));
			editor.updateWysiwygPanel();
			
			// give focus to the sign panel
			signPanel.requestFocusInWindow();
		}
	}

	/** Set the currently selected page */
	public void setSelectedPage(WPage pg) {
		selectedPage = pg;
	}
	
	public WPage getSelectedPage() {
		return selectedPage;
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
	
	/** Set the cursor type based on the editing mode */
	private void setCursorFromMode() {
		if (editingMode == MODE_TEXT)
			cursor = textCursor;
		else if (editingMode == MODE_GRAPHIC)
			cursor = graphicCursor;
		else if (editingMode == MODE_COLORRECT)
			cursor = colorRectCursor;
		else if (editingMode == MODE_TEXTRECT)
			cursor = textRectCursor;
		else if (editingMode == MODE_MULTITAG)
			cursor = multiTagCursor;
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
					
					// TODO a bunch more needs to happen here in addition to this
					// (need to remake page list and stuff)
					updatePageListModel();
					if (editor != null) {
						editor.updateWysiwygPanel();
					}
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
	
	public void activateTextMode() {
		// put the cursor in text mode then update everything
		editingMode = MODE_TEXT;
		setCursorFromMode();
		update();
	}
	
	public void activateGraphicMode() {
		// put the cursor in ?? hand ?? mode then update everything
		editingMode = MODE_GRAPHIC;
		setCursorFromMode();
		update();
	}
	
	public void activateTextRectangleMode() {
		// put the cursor in crosshair mode then update everything
		editingMode = MODE_TEXTRECT;
		setCursorFromMode();
		update();
	}
	
	public void activateColorRectangleMode() {
		// put the cursor in crosshair mode then update everything
		editingMode = MODE_COLORRECT;
		setCursorFromMode();
		update();
	}
		
	public void activateMultiTagMode() {
		// put the cursor in ?? default ?? mode then update everything
		editingMode = MODE_MULTITAG;
		setCursorFromMode();
		update();
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
	
	public void setCurrentFont(Font f) {
		currentFont = f;
	}
	
	public Font getCurrentFont() {
		return currentFont;
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
			currentFont = multiConfig.getDefaultFont();
		}
	}
	
	private void setColorsFromConfig() {
		if (multiConfig != null) {
			fgColor = multiConfig.getDefaultFG();
			bgColor = multiConfig.getDefaultBG();
		}
	}
	
	public void setForegroundColor(DmsColor c) {
		fgColor = c;
	}
		
	public void setBackgroundColor(DmsColor c) {
		bgColor = c;
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