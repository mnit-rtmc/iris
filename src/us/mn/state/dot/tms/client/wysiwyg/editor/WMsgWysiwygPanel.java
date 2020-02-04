/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.SignFacePanel;
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
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
	
	/* TODO for now we're taking the editor form - think we're going to change
	 * that to the controller (eventually), which should be a better state
	 * manager
	 */
	private WMsgEditorForm form;
	
	/* Mode buttons - Button group and toggle buttons */
	private JPanel mode_btn_pnl;
	private ButtonGroup mode_btn_grp;
	private JToggleButton text_mode_btn;
	private JToggleButton graphic_mode_btn;
	private JToggleButton colorrect_mode_btn;
	private JToggleButton textrect_mode_btn;
	private JToggleButton multitag_mode_btn;
	
	/* Panel for font drop down, color pickers, text justification buttons */
	private JPanel text_option_pnl;
	private JComboBox<String> font_options;
	
	/* Color Pickers */
	private JButton fg_color_btn;
	private Color fgColor;
	private JButton bg_color_btn;
	private Color bgColor;
	
	/* Justify Buttons */
	private JPanel text_vjust_btn_pnl;
	private ButtonGroup text_vjust_btn_grp;
	private JToggleButton text_vjust_top_btn;
	private JToggleButton text_vjust_center_btn;
	private JToggleButton text_vjust_bottom_btn;
	private JPanel text_hjust_btn_pnl;
	private ButtonGroup text_hjust_btn_grp;
	private JToggleButton text_hjust_left_btn;
	private JToggleButton text_hjust_center_btn;
	private JToggleButton text_hjust_right_btn;
	
	/* The Panel */
	/** Sign pixel panel to display the current sign message page */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(250, 550);
	
	public WMsgWysiwygPanel(WMsgEditorForm f) {
		form = f;
		
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
		
		// now I guess we need to create a panel too...
		mode_btn_pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mode_btn_pnl.setLayout(new BoxLayout(mode_btn_pnl, BoxLayout.X_AXIS));
		mode_btn_pnl.add(text_mode_btn);
		mode_btn_pnl.add(graphic_mode_btn);
		mode_btn_pnl.add(colorrect_mode_btn);
		mode_btn_pnl.add(textrect_mode_btn);
		mode_btn_pnl.add(multitag_mode_btn);
		add(mode_btn_pnl);
		
		/** text option panel */
		text_option_pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		// fonts
		font_options = new JComboBox<String>();
		text_option_pnl.add(font_options);

		// color pickers
		fgColor = Color.decode("#FFD000");
		fg_color_btn = new JButton(open_fg_color_picker);
		fg_color_btn.setIcon(createColorIcon(fgColor, 16, 16));
		fg_color_btn.setMargin(new Insets(0,0,0,0));
		text_option_pnl.add(fg_color_btn);
		
		bgColor = Color.decode("#000000");
		bg_color_btn = new JButton(open_bg_color_picker);
		bg_color_btn.setIcon(createColorIcon(bgColor, 16, 16));
		bg_color_btn.setMargin(new Insets(0,0,0,0));
		text_option_pnl.add(bg_color_btn);
		
		// justification buttons
		text_vjust_btn_grp = new ButtonGroup();
		text_vjust_top_btn = new JToggleButton(text_vjust_top);
		text_vjust_center_btn = new JToggleButton(text_vjust_center);
		text_vjust_bottom_btn = new JToggleButton(text_vjust_bottom);
		text_vjust_center_btn.setSelected(true);
		text_vjust_btn_grp.add(text_vjust_top_btn);
		text_vjust_btn_grp.add(text_vjust_center_btn);
		text_vjust_btn_grp.add(text_vjust_bottom_btn);
		
		// set icons for justification buttons
		ImageIcon text_vjust_top_icon = Icons.getIconByPropName("wysiwyg.epanel.text_vjust_top");
		text_vjust_top_btn.setIcon(text_vjust_top_icon);
		text_vjust_top_btn.setHideActionText(true);
		text_vjust_top_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_vjust_center_icon = Icons.getIconByPropName("wysiwyg.epanel.text_vjust_center");
		text_vjust_center_btn.setIcon(text_vjust_center_icon);
		text_vjust_center_btn.setHideActionText(true);
		text_vjust_center_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_vjust_bottom_icon = Icons.getIconByPropName("wysiwyg.epanel.text_vjust_bottom");
		text_vjust_bottom_btn.setIcon(text_vjust_bottom_icon);
		text_vjust_bottom_btn.setHideActionText(true);
		text_vjust_bottom_btn.setMargin(new Insets(0,0,0,0));
		
		text_vjust_btn_pnl = new JPanel();
		text_vjust_btn_pnl.setLayout(new BoxLayout(text_vjust_btn_pnl, BoxLayout.X_AXIS));
		text_vjust_btn_pnl.add(text_vjust_top_btn);
		text_vjust_btn_pnl.add(text_vjust_center_btn);
		text_vjust_btn_pnl.add(text_vjust_bottom_btn);
		text_option_pnl.add(text_vjust_btn_pnl);
		
		text_hjust_btn_grp = new ButtonGroup();
		text_hjust_left_btn = new JToggleButton(text_hjust_left);
		text_hjust_center_btn = new JToggleButton(text_hjust_center);
		text_hjust_right_btn = new JToggleButton(text_hjust_right);
		text_hjust_center_btn.setSelected(true);
		text_hjust_btn_grp.add(text_hjust_left_btn);
		text_hjust_btn_grp.add(text_hjust_center_btn);
		text_hjust_btn_grp.add(text_hjust_right_btn);
		
		ImageIcon text_hjust_left_icon = Icons.getIconByPropName("wysiwyg.epanel.text_hjust_left");
		text_hjust_left_btn.setIcon(text_hjust_left_icon);
		text_hjust_left_btn.setHideActionText(true);
		text_hjust_left_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_hjust_center_icon = Icons.getIconByPropName("wysiwyg.epanel.text_hjust_center");
		text_hjust_center_btn.setIcon(text_hjust_center_icon);
		text_hjust_center_btn.setHideActionText(true);
		text_hjust_center_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_hjust_right_icon = Icons.getIconByPropName("wysiwyg.epanel.text_hjust_right");
		text_hjust_right_btn.setIcon(text_hjust_right_icon);
		text_hjust_right_btn.setHideActionText(true);
		text_hjust_right_btn.setMargin(new Insets(0,0,0,0));
		
		text_hjust_btn_pnl = new JPanel();
		text_hjust_btn_pnl.setLayout(new BoxLayout(text_hjust_btn_pnl, BoxLayout.X_AXIS));
		text_hjust_btn_pnl.add(text_hjust_left_btn);
		text_hjust_btn_pnl.add(text_hjust_center_btn);
		text_hjust_btn_pnl.add(text_hjust_right_btn);
		text_option_pnl.add(text_hjust_btn_pnl);
		
		add(text_option_pnl);
		
		// sign face panel - the main show
		add(pixel_pnl);
	}
	
	/** Set the currently selected page to display */
	public void setPage(WMsgSignPage sp) {
		// update the rendering on the pixel panel
		sp.renderToPanel(pixel_pnl);
	}
	
	/***** Button Actions *****/
	
	/** Text mode action */
	private final IAction activate_text_mode = new IAction("wysiwyg.epanel.text_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			System.out.println("Text mode...");
		}
	};
	
	/** Graphic mode action */
	private final IAction activate_graphic_mode = new IAction("wysiwyg.epanel.graphic_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Graphic mode...");
		}
	};
	
	
	/** Color Rectangle mode action */
	private final IAction activate_colorrect_mode = new IAction("wysiwyg.epanel.colorrect_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Color Rectangle mode...");
		}
	};
	
	
	/** Text Rectangle mode action */
	private final IAction activate_textrect_mode = new IAction("wysiwyg.epanel.textrect_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Text Rectangle mode...");
		}
	};
	
	
	/** MULTI Tag mode action */
	private final IAction activate_multitag_mode = new IAction("wysiwyg.epanel.multitag_mode") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("MULTI Tag mode...");
		}
	};
	
	/** Foreground color picker action */
	private final IAction open_fg_color_picker = new IAction("wysiwyg.epanel.fg_color_picker_btn") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// TODO change this so it handles things better (whatever that means...)
			
			Color newColor = JColorChooser.showDialog(null,
					I18N.get("wysiwyg.epanel.fg_color_picker_title"),
					fgColor);
			fgColor = newColor;
			fg_color_btn.setIcon(createColorIcon(fgColor, 16, 16));
		}
	};
	
	/** Background color picker action */
	private final IAction open_bg_color_picker = new IAction("wysiwyg.epanel.bg_color_picker_btn") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// TODO change this so it handles things better (whatever that means...)
			
			Color newColor = JColorChooser.showDialog(null,
					I18N.get("wysiwyg.epanel.bg_color_picker_title"),
					bgColor);
			bgColor = newColor;
			bg_color_btn.setIcon(createColorIcon(bgColor, 16, 16));
		}
	};
	
	/** Text vertical justify top action */
	private final IAction text_vjust_top = new IAction("wysiwyg.epanel.text_vjust_top") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Vertical Justify Top...");
		}
	};
	
	/** Text vertical justify center action */
	private final IAction text_vjust_center = new IAction("wysiwyg.epanel.text_vjust_center") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Vertical Justify Center...");
		}
	};
	
	/** Text vertical justify bottom action */
	private final IAction text_vjust_bottom = new IAction("wysiwyg.epanel.text_vjust_bottom") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Vertical Justify Bottom...");
		}
	};
	
	/** Text horizontal justify left action */
	private final IAction text_hjust_left = new IAction("wysiwyg.epanel.text_hjust_left") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Horizontal Justify Left...");
		}
	};
	
	/** Text horizontal justify left action */
	private final IAction text_hjust_center = new IAction("wysiwyg.epanel.text_hjust_center") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Horizontal Justify Center...");
		}
	};
	
	/** Text horizontal justify left action */
	private final IAction text_hjust_right = new IAction("wysiwyg.epanel.text_hjust_right") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Horizontal Justify Right...");
		}
	};
	
	public static  ImageIcon createColorIcon(Color c, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(c);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, width-1, height-1);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
	
}


