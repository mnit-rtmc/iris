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
import java.awt.Graphics2D;
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

import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WYSIWYG DMS Message Editor Text Option Panel containing buttons with
 * various options for text editing mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgTextToolbar extends JPanel {
	
	/** Handle to the controller */
	private WController controller;
	
	/** Font option menu */
	private JComboBox<Font> font_options;
	
	/** Color Pickers */
	private JButton fg_color_btn;
	private Color fgColor;
	private JButton bg_color_btn;
	private Color bgColor;
	
	/** Justify Buttons */
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
	
	
	public WMsgTextToolbar(WController c) {
		controller = c;
		
		// use a FlowLayout with no margins to give more control of separation
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		// fonts
		font_options = new JComboBox<Font>();
		font_options.setAction(new IAction("font") {
			protected void doActionPerformed(ActionEvent e) {
				controller.setCurrentFont(
					(Font) font_options.getSelectedItem());
			}
		});
		
		font_options.setModel(
				new IComboBoxModel<Font>(controller.getFontModel()));
		font_options.setSelectedItem(controller.getCurrentFont());
		add(font_options);
		
		// color pickers
		fg_color_btn = new JButton(open_fg_color_picker);
		DmsColor fgc = controller.getForegroundColor();
		if (fgc != null) {
			fgColor = fgc.color;
			fg_color_btn.setIcon(
					WMsgColorChooser.createColorIcon(fgColor, 16, 16));
			fg_color_btn.setMargin(new Insets(0,0,0,0));
		}
		add(fg_color_btn);
		
		bg_color_btn = new JButton(open_bg_color_picker);
		DmsColor bgc = controller.getBackgroundColor();
		if (bgc != null) {
			bgColor = bgc.color;
			bg_color_btn.setIcon(
					WMsgColorChooser.createColorIcon(bgColor, 16, 16));
			bg_color_btn.setMargin(new Insets(0,0,0,0));
		}
		add(bg_color_btn);
		
		// disable color picker buttons for 1-bit and unknown color schemes
		ColorScheme cs = controller.getMultiConfig().getColorScheme();
		if (cs == ColorScheme.MONOCHROME_1_BIT || cs == ColorScheme.UNKNOWN) {
			fg_color_btn.setEnabled(false);
			bg_color_btn.setEnabled(false);
		}
		
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
		ImageIcon text_vjust_top_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_vjust_top");
		text_vjust_top_btn.setIcon(text_vjust_top_icon);
		text_vjust_top_btn.setHideActionText(true);
		text_vjust_top_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_vjust_center_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_vjust_center");
		text_vjust_center_btn.setIcon(text_vjust_center_icon);
		text_vjust_center_btn.setHideActionText(true);
		text_vjust_center_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_vjust_bottom_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_vjust_bottom");
		text_vjust_bottom_btn.setIcon(text_vjust_bottom_icon);
		text_vjust_bottom_btn.setHideActionText(true);
		text_vjust_bottom_btn.setMargin(new Insets(0,0,0,0));
		
		text_vjust_btn_pnl = new JPanel();
		text_vjust_btn_pnl.setLayout(
				new BoxLayout(text_vjust_btn_pnl, BoxLayout.X_AXIS));
		text_vjust_btn_pnl.add(text_vjust_top_btn);
		text_vjust_btn_pnl.add(text_vjust_center_btn);
		text_vjust_btn_pnl.add(text_vjust_bottom_btn);
		add(text_vjust_btn_pnl);
		
		text_hjust_btn_grp = new ButtonGroup();
		text_hjust_left_btn = new JToggleButton(text_hjust_left);
		text_hjust_center_btn = new JToggleButton(text_hjust_center);
		text_hjust_right_btn = new JToggleButton(text_hjust_right);
		text_hjust_center_btn.setSelected(true);
		text_hjust_btn_grp.add(text_hjust_left_btn);
		text_hjust_btn_grp.add(text_hjust_center_btn);
		text_hjust_btn_grp.add(text_hjust_right_btn);
		
		ImageIcon text_hjust_left_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_hjust_left");
		text_hjust_left_btn.setIcon(text_hjust_left_icon);
		text_hjust_left_btn.setHideActionText(true);
		text_hjust_left_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_hjust_center_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_hjust_center");
		text_hjust_center_btn.setIcon(text_hjust_center_icon);
		text_hjust_center_btn.setHideActionText(true);
		text_hjust_center_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_hjust_right_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_hjust_right");
		text_hjust_right_btn.setIcon(text_hjust_right_icon);
		text_hjust_right_btn.setHideActionText(true);
		text_hjust_right_btn.setMargin(new Insets(0,0,0,0));
		
		text_hjust_btn_pnl = new JPanel();
		text_hjust_btn_pnl.setLayout(
				new BoxLayout(text_hjust_btn_pnl, BoxLayout.X_AXIS));
		text_hjust_btn_pnl.add(text_hjust_left_btn);
		text_hjust_btn_pnl.add(text_hjust_center_btn);
		text_hjust_btn_pnl.add(text_hjust_right_btn);
		add(text_hjust_btn_pnl);
	}
	
	/** Set the foreground or background color to c. The color to set is
	 *  specified by passing "foreground" or "background" in the fgbg 
	 *  parameter.
     */
	public void setColor(Color c, String fgbg) {
		if (fgbg.equalsIgnoreCase("foreground"))
			setForegroundColor(c);
		else if (fgbg.equalsIgnoreCase("background"))
			setBackgroundColor(c);
	}
	
	/** Set the foreground color on the controller and change the button
	 *  appearance
	 */
	public void setForegroundColor(Color c) {
		fgColor = c;
		controller.setForegroundColor(new DmsColor(fgColor));
		fg_color_btn.setIcon(WMsgColorChooser.createColorIcon(fgColor, 16, 16));
	}
	
	/** Set the background color on the controller and change the button
	 *  appearance
	 */
	public void setBackgroundColor(Color c) {
		bgColor = c;
		controller.setBackgroundColor(new DmsColor(bgColor));
		bg_color_btn.setIcon(WMsgColorChooser.createColorIcon(bgColor, 16, 16));
	}
	
	WMsgTextToolbar tb = this;
	
	/** Foreground color picker action */
	private final IAction open_fg_color_picker =
			new IAction("wysiwyg.epanel.fg_color_picker_btn") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			String title = I18N.get("wysiwyg.epanel.fg_color_picker_title");
			WMsgColorChooser ccForm = new WMsgColorChooser(
					controller.getDesktop(), tb,
					controller.getMultiConfig().getColorScheme(),
					title, fgColor, "foreground");
			SmartDesktop desktop = controller.getDesktop();
			desktop.show(ccForm);
		}
	};
	
	/** Background color picker action */
	private final IAction open_bg_color_picker =
			new IAction("wysiwyg.epanel.bg_color_picker_btn") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			String title = I18N.get("wysiwyg.epanel.bg_color_picker_title");
			WMsgColorChooser ccForm = new WMsgColorChooser(
					controller.getDesktop(), tb,
					controller.getMultiConfig().getColorScheme(),
					title, fgColor, "background");
			SmartDesktop desktop = controller.getDesktop();
			desktop.show(ccForm);
		}
	};
	
	/** Text vertical justify top action */
	private final IAction text_vjust_top =
			new IAction("wysiwyg.epanel.text_vjust_top") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Vertical Justify Top...");
		}
	};
	
	/** Text vertical justify center action */
	private final IAction text_vjust_center =
			new IAction("wysiwyg.epanel.text_vjust_center") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Vertical Justify Center...");
		}
	};
	
	/** Text vertical justify bottom action */
	private final IAction text_vjust_bottom =
			new IAction("wysiwyg.epanel.text_vjust_bottom") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Vertical Justify Bottom...");
		}
	};
	
	/** Text horizontal justify left action */
	private final IAction text_hjust_left =
			new IAction("wysiwyg.epanel.text_hjust_left") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Horizontal Justify Left...");
		}
	};
	
	/** Text horizontal justify left action */
	private final IAction text_hjust_center =
			new IAction("wysiwyg.epanel.text_hjust_center") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Horizontal Justify Center...");
		}
	};
	
	/** Text horizontal justify left action */
	private final IAction text_hjust_right =
			new IAction("wysiwyg.epanel.text_hjust_right") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Horizontal Justify Right...");
		}
	};
}