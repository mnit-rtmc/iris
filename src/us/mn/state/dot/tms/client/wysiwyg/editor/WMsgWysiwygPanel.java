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

package us.mn.state.dot.tms.client.wysiwyg.editor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.MultiTag;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;

/**
 * WYSIWYG DMS Message Editor WYSIWYG Tab - This one has the good stuff...
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")


public class WMsgWysiwygPanel extends JPanel {
	
	/** Handle to the controller */
	private WController controller;
	
	/** Mode buttons - Button group and toggle buttons */
	private JPanel mode_btn_pnl;
	private ButtonGroup mode_btn_grp;
	private JToggleButton text_mode_btn;
	private JToggleButton graphic_mode_btn;
	private JToggleButton colorrect_mode_btn;
	private JToggleButton textrect_mode_btn;
	private JToggleButton multitag_mode_btn;
	
	/** Non-text tag handling button for working with non-text tags */
	private JToggleButton nonTextTagBtn;
	
	/** Also a label for displaying some info about non-text tags */
	private JLabel nonTextTagInfo;
	private JPanel nonTextTagInfoPnl;
	
	/** Restore button, for restoring the most recent non-error state */
	private JButton restoreBtn;
	
	/** Master toolbar panel (changes) */
	private JPanel toolbar_pnl;
	
	/** Panel for font drop down, color pickers, text justification buttons */
	final static private String TEXT_TOOLBAR = "Text Mode Toolbar";
	private WMsgTextToolbar text_toolbar_pnl;
	
	/** Panel for graphics toolbar */
	final static private String GRAPHIC_TOOLBAR = "Graphic Mode Toolbar";
	private WMsgGraphicToolbar graphic_toolbar_pnl;
	
	/** Panel for color rectangle toolbar */
	final static private String COLOR_RECTANGLE_TOOLBAR =
			"Color Rectangle Toolbar";
	private WMsgColorRectangleToolbar colorrect_toolbar_pnl;
	
	/** Panel for graphics toolbar */
	final static private String TEXT_RECTANGLE_TOOLBAR =
			"Text Rectangle Toolbar";
	private WMsgTextToolbar textrect_toolbar_pnl;
	
	/** Panel for graphics toolbar */
	final static private String MULTI_TAG_TOOLBAR = "MULTI Tag Toolbar";
	private WMsgMultiTagToolbar multitag_toolbar_pnl;
	
	/** Sign pixel panel to display the current sign message page */
//	private SignPixelPanel pixel_pnl;
	private WImagePanel signPanel;
	
	public WMsgWysiwygPanel(WController c) {
		controller = c;
		
		// use a box layout to put components in a column
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// create the mode button group (default is text mode) 
		mode_btn_grp = new ButtonGroup();
		text_mode_btn = new JToggleButton(activate_text_mode);
		text_mode_btn.setSelected(true);
		graphic_mode_btn = new JToggleButton(activate_graphic_mode);
		colorrect_mode_btn = new JToggleButton(activate_colorrect_mode);
		textrect_mode_btn = new JToggleButton(activate_textrect_mode);
		multitag_mode_btn = new JToggleButton(activate_multitag_mode);
		mode_btn_grp.add(text_mode_btn);
		mode_btn_grp.add(graphic_mode_btn);
		mode_btn_grp.add(colorrect_mode_btn);
		mode_btn_grp.add(textrect_mode_btn);
		mode_btn_grp.add(multitag_mode_btn);
		
		// now we need to create a panel too...
		mode_btn_pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mode_btn_pnl.add(text_mode_btn);
		mode_btn_pnl.add(graphic_mode_btn);
		mode_btn_pnl.add(colorrect_mode_btn);
		mode_btn_pnl.add(textrect_mode_btn);
		mode_btn_pnl.add(multitag_mode_btn);
		
		// add a separator, then a button for enabling/disabling of
		// direct handling of non-text tags with a pilcrow
		nonTextTagBtn = new JToggleButton(controller.toggleNonTextTagMode);
		nonTextTagBtn.setText(String.valueOf('\u00B6'));
		nonTextTagBtn.setFont(
				new java.awt.Font("Arial", java.awt.Font.PLAIN, 18));
		nonTextTagBtn.setToolTipText(
				I18N.get("wysiwyg.epanel.non_text_tag_tooltip"));
		nonTextTagBtn.setMargin(new Insets(0,5,0,5));
		mode_btn_pnl.add(Box.createHorizontalStrut(40));
		mode_btn_pnl.add(nonTextTagBtn);
		
		// add another separator, then another button - this will be for
		// restoring a good state when errors are encountered (disabled by
		// default)
		restoreBtn = new JButton(controller.restoreLastGoodState);
		mode_btn_pnl.add(Box.createHorizontalStrut(40));
		mode_btn_pnl.add(restoreBtn);
		disableRestoreButton();
		add(mode_btn_pnl);
		
		// disable any buttons for unsupported tags
		MultiConfig mc = controller.getMultiConfig();
		if (mc != null)
			checkSupportedFuncs(mc);
		
		/** Option Panel - changes depending on the mode */
		toolbar_pnl = new JPanel(new CardLayout(10,10));
		
		// Text toolbar panel
		text_toolbar_pnl = new WMsgTextToolbar(controller, true);
		toolbar_pnl.add(text_toolbar_pnl, TEXT_TOOLBAR);
		
		// Graphic toolbar panel - used in graphic mode
		graphic_toolbar_pnl = new WMsgGraphicToolbar(controller);
		toolbar_pnl.add(graphic_toolbar_pnl, GRAPHIC_TOOLBAR);
		
		// Color rectangle toolbar panel
		colorrect_toolbar_pnl = new WMsgColorRectangleToolbar(controller);
		toolbar_pnl.add(colorrect_toolbar_pnl, COLOR_RECTANGLE_TOOLBAR);
		
		// Text rectangle toolbar panel
		textrect_toolbar_pnl = new WMsgTextRectangleToolbar(controller);
		toolbar_pnl.add(textrect_toolbar_pnl, TEXT_RECTANGLE_TOOLBAR);
		
		// MULTI Tag toolbar panel
		multitag_toolbar_pnl = new WMsgMultiTagToolbar(controller);
		toolbar_pnl.add(multitag_toolbar_pnl, MULTI_TAG_TOOLBAR);
		
		// add the toolbars to the panel
		add(toolbar_pnl);
		
		// sign face panel - the main show
		signPanel = new WImagePanel(controller, 650, 300);
		JPanel spPanel = new JPanel(new BorderLayout());
		spPanel.add(signPanel, BorderLayout.CENTER);
		add(spPanel);

		// also add a JLabel for displaying non-text tag info
		nonTextTagInfo = new JLabel();
		nonTextTagInfoPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		nonTextTagInfo.setText("TAG");
		nonTextTagInfoPnl.add(nonTextTagInfo);
		Dimension d = nonTextTagInfoPnl.getPreferredSize();
		nonTextTagInfo.setText("");
		nonTextTagInfoPnl.setPreferredSize(d);
		add(nonTextTagInfoPnl);
		
		// mouse input adapter for handling mouse events
		WMsgMouseInputAdapter mouseHandler = 
				new WMsgMouseInputAdapter(controller);
		signPanel.addMouseListener(mouseHandler);
		signPanel.addMouseMotionListener(mouseHandler);
	}
	
	/** Enable or disable functions that correspond to any tags not supported
	 *  by the MultiConfig provided.
	 *  
	 *  TODO we may want to pass this further down the chain (e.g. to some
	 *  toolbars). 
	 */
	public void checkSupportedFuncs(MultiConfig mc) {
		if (mc != null) {
			graphic_mode_btn.setEnabled(mc.supportsTag(MultiTag.g));
			colorrect_mode_btn.setEnabled(mc.supportsTag(MultiTag.cr));
			textrect_mode_btn.setEnabled(mc.supportsTag(MultiTag.tr));
		} else {
			graphic_mode_btn.setEnabled(false);
			colorrect_mode_btn.setEnabled(false);
			textrect_mode_btn.setEnabled(false);
		}
	}
	
	/** Set the currently selected page to display */
	public void setPage(WPage sp) {
		// update the rendering on the pixel panel
		signPanel.setPage(sp);
	}
	
	/** Get the pixel panel from the WYSIWYG editor panel */
	public WImagePanel getWImagePanel() {
		return signPanel; 
	}
	
	/** Enable the restore button for restoring the last non-error state. */
	public void enableRestoreButton() {
		restoreBtn.setEnabled(true);
	}

	/** Disable the restore button. Called when errors have been cleared. */
	public void disableRestoreButton() {
		restoreBtn.setEnabled(false);
	}
	
	/** Update the text toolbars */
	public void updateTextToolbar() {
		text_toolbar_pnl.updateToolbar();
		textrect_toolbar_pnl.updateToolbar();
	}
	
	/** Enable or disable tag edit button */
	public void updateTagEditButton(boolean state) {
		multitag_toolbar_pnl.updateTagEditButton(state);
	}
	
	/** Update the non-text tag button state to state */
	public void updateNonTextTagButton(boolean state) {
		nonTextTagBtn.setSelected(state);
	}
	
	/** Update the non-text tag info label, optionally including a color icon
	 *  (if c is null, no color is shown).
	 */
	public void updateNonTextTagInfo(String s, Color c) {
		if (c != null)
			nonTextTagInfo.setIcon(WMsgColorChooser.createColorIcon(c, 16, 16));
		else
			nonTextTagInfo.setIcon(null);
		nonTextTagInfo.setText(s);
		repaint();
	}
	
	/***** Button Actions *****/
	
	/** Text mode action */
	private final IAction activate_text_mode = 
			new IAction("wysiwyg.epanel.text_mode") {
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			System.out.println("Text mode...");
			
			// change toolbar panel to text options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, TEXT_TOOLBAR);
			
			// tell the controller we've changed modes
			controller.activateTextMode();
			
			// update the mouse cursor based on the controller's decisions
			setCursor(controller.getCursor());
		}
	};
	
	/** Graphic mode action */
	private final IAction activate_graphic_mode = 
			new IAction("wysiwyg.epanel.graphic_mode") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Graphic mode...");

			// change toolbar panel to graphic options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, GRAPHIC_TOOLBAR);

			// tell the controller we've changed modes
			controller.activateGraphicMode();
		}
	};
	
	
	/** Color Rectangle mode action */
	private final IAction activate_colorrect_mode = 
			new IAction("wysiwyg.epanel.colorrect_mode") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Color Rectangle mode...");

			// change toolbar panel to color rectangle options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, COLOR_RECTANGLE_TOOLBAR);

			// tell the controller we've changed modes
			controller.activateColorRectangleMode();
		}
	};
	
	
	/** Text Rectangle mode action */
	private final IAction activate_textrect_mode = 
			new IAction("wysiwyg.epanel.textrect_mode") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Text Rectangle mode...");

			// change toolbar panel to text rectangle options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, TEXT_RECTANGLE_TOOLBAR);

			// tell the controller we've changed modes
			controller.activateTextRectangleMode();
		}
	};
	
	
	/** MULTI Tag mode action */
	private final IAction activate_multitag_mode = 
			new IAction("wysiwyg.epanel.multitag_mode") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("MULTI Tag mode...");

			// change toolbar panel to MULTI tag options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, MULTI_TAG_TOOLBAR);
			
			// tell the controller we've changed modes
			controller.activateMultiTagMode();
		}
	};
	
}


