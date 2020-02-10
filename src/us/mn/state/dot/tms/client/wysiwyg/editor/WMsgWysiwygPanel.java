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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.SignFacePanel;
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WYSIWYG DMS Message Editor WYSIWYG Tab - This one has the good stuff...
 *
 * @author Gordon Parikh, John L. Stanley - SRF Consulting
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
	
	/** Master toolbar panel (changes) */
	private JPanel toolbar_pnl;
	
	/** Panel for font drop down, color pickers, text justification buttons */
	final static private String TEXT_TOOLBAR = "Text Mode Toolbar";
	private JPanel text_toolbar_pnl;
	
	/** Panel for graphics toolbar */
	final static private String GRAPHIC_TOOLBAR = "Graphic Mode Toolbar";
	private JPanel graphic_toolbar_pnl;
	
	/** Panel for color rectangle toolbar */
	final static private String COLOR_RECTANGLE_TOOLBAR = "Color Rectangle Toolbar";
	private JPanel colorrect_toolbar_pnl;
	
	/** Panel for graphics toolbar */
	final static private String TEXT_RECTANGLE_TOOLBAR = "Text Rectangle Toolbar";
	private JPanel textrect_toolbar_pnl;
	
	/** Panel for graphics toolbar */
	final static private String MULTI_TAG_TOOLBAR = "MULTI Tag Toolbar";
	private JPanel multitag_toolbar_pnl;
	
	/** Sign pixel panel to display the current sign message page */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(250, 550);
	
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
		add(mode_btn_pnl);
		
		/** Option Panel - changes depending on the mode */
		toolbar_pnl = new JPanel(new CardLayout(10,10));
		
		// Text toolbar panel
		text_toolbar_pnl = new WMsgTextToolbar(controller);
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
		add(pixel_pnl);
		
		// mouse input adapter for handling mouse events
		
		/* TODO just for testing - need to figure out how to deal with
		 * component focus, etc. - we can probably reuse the same mouse
		 * listener class and give it different components or something...
		 */
		
		WMsgMouseInputAdapter mouseHandler = new WMsgMouseInputAdapter(controller);
		pixel_pnl.addMouseListener(mouseHandler);
		pixel_pnl.addMouseMotionListener(mouseHandler);
	}
	
	/** Set the currently selected page to display */
	public void setPage(WMsgSignPage sp) {
		// update the rendering on the pixel panel
		sp.renderToPanel(pixel_pnl);
	}
	
	/** Get the pixel panel from the WYSIWYG editor panel */
	public SignPixelPanel getEditorPixelPanel() {
		return pixel_pnl; 
	}
	
	/***** Button Actions *****/
	
	/** Text mode action */
	private final IAction activate_text_mode = new IAction("wysiwyg.epanel.text_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			System.out.println("Text mode...");
			
			// change toolbar panel to text options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, TEXT_TOOLBAR);
		}
	};
	
	/** Graphic mode action */
	private final IAction activate_graphic_mode = new IAction("wysiwyg.epanel.graphic_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Graphic mode...");

			// change toolbar panel to graphic options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, GRAPHIC_TOOLBAR);
		}
	};
	
	
	/** Color Rectangle mode action */
	private final IAction activate_colorrect_mode = new IAction("wysiwyg.epanel.colorrect_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Color Rectangle mode...");

			// change toolbar panel to color rectangle options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, COLOR_RECTANGLE_TOOLBAR);
		}
	};
	
	
	/** Text Rectangle mode action */
	private final IAction activate_textrect_mode = new IAction("wysiwyg.epanel.textrect_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Text Rectangle mode...");

			// change toolbar panel to text rectangle options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, TEXT_RECTANGLE_TOOLBAR);
		}
	};
	
	
	/** MULTI Tag mode action */
	private final IAction activate_multitag_mode = new IAction("wysiwyg.epanel.multitag_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("MULTI Tag mode...");

			// change toolbar panel to MULTI tag options
			CardLayout cl = (CardLayout) toolbar_pnl.getLayout();
			cl.show(toolbar_pnl, MULTI_TAG_TOOLBAR);
		}
	};
	
}


