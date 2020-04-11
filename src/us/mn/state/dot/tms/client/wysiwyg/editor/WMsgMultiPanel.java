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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;

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
			multiText = removeInvalidChars(textBox.getText());
			controller.setMultiString(multiText, true);
		}
	};
	
	private static String removeInvalidChars(String txt) {
		return txt.replace("\n", "").replace("\r", "").replace("\t", "");
	}
	
	/** Add newlines before [ and after ] characters.
	 *  
	 *  TODO add indentation for text in text rectangles (will probably never
	 *  happen, but it would be nice...).
	 */
	private static String addNewlines(String txt) {
		// replace [[ and ]] (escaped [ and ]) with { or } (invalid characters)
		// as a hack to NOT add newlines there, then add newlines, then add [[
		// and ]] back
		return txt.replace("]]", "}").replace("[[", "{").replace("]", "]\n")
				.replace("[", "\n[").replace("}", "]]").replace("{", "[[")
				.replace("\n\n", "\n").trim();
	}
	
}