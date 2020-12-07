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

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.wysiwyg.WEditorError;
import us.mn.state.dot.tms.utils.wysiwyg.WEditorErrorManager;

/**
 * WYSIWYG DMS Message Editor MULTI Mode Tab for direct editing of MULTI
 * strings, which may be forced for some types of messages.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")


public class WMsgMultiPanel extends JPanel {
	/** Controller for updating renderings */
	private WController controller;
	
	/** Text area (multiline text box) for manipulating MULTI string */
	private JTextArea textBox;
	
	/** The MULTI string itself */
	private String multiText;
	
	/** Scroll pane in case the MULTI string gets long */
	private JScrollPane scrollPane;
	
	private JPanel toolPanel;
	
	/** Update button for applying changes to the MULTI string */
	private JButton updateBtn;
	
	/** Check Box to enable/disable use of newlines in the message box to
	 *  separate non-text MULTI tags and make them more readable
	 */
	private JCheckBox addNewlinesBox;
	
	/** Create a new MULTI-mode panel */
	public WMsgMultiPanel(WController c) {
		controller = c;
		
		// initialize the panel
		initPanel();
		
		// add text if there is already some in the controller
		multiText = controller.getMultiText();
		if (multiText != null) {
			setText(multiText);
		}
		
		// give the controller a handle so it can update the text
		controller.setMultiPanel(this);
	}
	
	/** Create and add components to the panel */
	private void initPanel() {
		// use a box layout to put components in a column
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// create the text area with a scroll pane
		textBox = new JTextArea();
		scrollPane = new JScrollPane(textBox);
		
		// make the text area editable and turn line wrapping on
		textBox.setEditable(true);
		textBox.setLineWrap(true);
		
		// add to the panel
		add(scrollPane);
		
		// make a FlowLayout panel for the next two things
		toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		// set up the button and add it to the panel
		updateBtn = new JButton(updateRendering);
		toolPanel.add(updateBtn);
		toolPanel.add(Box.createHorizontalStrut(10));
		
		// disable update button if we get a bad MultiConfig
		if (!controller.multiConfigUseable())
			updateBtn.setEnabled(false);
		
		// also add the check box, and enable it by default
		addNewlinesBox = new JCheckBox(
				I18N.get("wysiwyg.epanel.multi_tab_newline_box"), true);
		addNewlinesBox.addActionListener(handleCheckBox);
		toolPanel.add(addNewlinesBox);
		toolPanel.setMaximumSize(new Dimension(
				toolPanel.getPreferredSize().width,
				updateBtn.getHeight() + 10));
		add(toolPanel);
	}
	
	/** Highlight any error-producing tokens */
	public void highlightErrors(WEditorErrorManager errMan) {
		if (errMan.hasErrors()) {
			Highlighter h = textBox.getHighlighter();
			HighlightPainter hp =
					new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
			
			String txt = textBox.getText();
			Iterator<WEditorError> it = errMan.iterator();
			while (it.hasNext()) {
				WEditorError e = it.next();
				if (e.hasToken()) {
					String ts = e.getToken().toString();
					
					// if this error has a token, highlight it
					int a = txt.indexOf(ts);
					if (a < 0)
						continue;
					int b = a + ts.length();
					String s = txt.substring(a, b);
					
					// TODO this is highlighting more than it should...
					// somehow we should incorporate the wmsg to get a more
					// accurate index for highlighting
					
					if (s == ts) {
						try {
							h.addHighlight(a, b, hp);
						} catch (BadLocationException ex) {
							// just ignore this
							ex.printStackTrace();
						}
					}
				}
			}
		}
	}
	
//	public void clearErrors()
	
	/** Action listener for handling check box */
	private ActionListener handleCheckBox = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// update the text in the text box as needed without talking to
			// the controller
			setText(textBox.getText());
		}
	};
	
	/** Set the text in the text box. If addNewlinesBox is checked, newlines
	 *  are added at the end of each non-text tag (i.e. after any ]) to
	 *  improve readability of raw MULTI.
	 */
	public void setText(String t) {
		if (addNewlinesBox.isSelected())
			textBox.setText(addNewlines(t));
		else
			textBox.setText(removeInvalidChars(t));
	}
	
	/** Update rendering action */
	private final IAction updateRendering = new IAction(
			"wysiwyg.epanel.multi_tab_update_btn") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// get the text from the box and remove any newlines or tabs
			multiText = getMulti();
			controller.setMultiString(multiText, true);
		}
	};
	
	/** Get the current MULTI string from the text box, removing any newlines
	 *  or tabs.
	 */
	public String getMulti() {
		return removeInvalidChars(textBox.getText());
	}
	
	private static String removeInvalidChars(String txt) {
		return txt.replace("\n", "").replace("\r", "").replace("\t", "");
	}
	
	/** Add newlines before [ and after ] characters.
	 *  
	 *  TODO add indentation for text in text rectangles (will probably never
	 *  happen, but it would be nice...).
	 */
	private static String addNewlines(String txt) {
		// replace [[ and ]] (escaped [ and ]) with ` or ~ (invalid characters)
		// as a hack to NOT add newlines there, then add newlines, then add [[
		// and ]] back
		return txt.replace("]]", "~").replace("[[", "`").replace("]", "]\n")
				.replace("[", "\n[").replace("~", "]]").replace("`", "[[")
				.replace("\n\n", "\n").trim();
	}
	
}