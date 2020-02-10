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

import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import us.mn.state.dot.tms.client.widget.IAction;

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
	private JTextArea text_box;
	
	/** The MULTI string itself */
	private String multiText;
	
	/** Scroll pane in case the MULTI string gets long */
	private JScrollPane scroll_pn;
	
	/** Update button for applying changes to the MULTI string */
	private JButton update_btn;
	
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
	}
	
	/** Create and add components to the panel */
	private void initPanel() {
		// use a box layout to put components in a column
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// create the text area with a scroll pane
		text_box = new JTextArea();
		scroll_pn = new JScrollPane(text_box);
		
		// make the text area editable and turn line wrapping on
		text_box.setEditable(true);
		text_box.setLineWrap(true);
		
		// add to the panel
		add(scroll_pn);
		
		// set up the button and add it to the panel
		update_btn = new JButton(update_rendering);
		add(update_btn);
	}
	
	/* Set the text in the text box */
	public void setText(String t) {
		text_box.setText(t);
	}
	
	/** Text horizontal justify left action */
	private final IAction update_rendering = new IAction("wysiwyg.epanel.multi_tab_update_btn") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// use the controller to update the rendering
			multiText = text_box.getText();
			controller.editMulti(multiText);
		}
	};
	
}