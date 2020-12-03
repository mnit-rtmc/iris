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
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;

import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.Multi.JustificationPage;
import us.mn.state.dot.tms.utils.MultiConfig;

/**
 * WYSIWYG DMS Message Editor Text Option Panel containing buttons with
 * various options for text editing mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgTextToolbar extends WToolbar {
	
	/** Font option menu */
	protected JComboBox<Font> fontOptions;
	
	/** Color Pickers */
	protected JButton fg_color_btn;
	protected Color fgColor;
	protected JButton bg_color_btn;
	protected Color bgColor;
	
	/** Justify Buttons */
	protected JPanel text_vjust_btn_pnl;
	protected ButtonGroup text_pg_just_btn_grp;
	protected JToggleButton text_pg_just_top_btn;
	protected JToggleButton text_pg_just_middle_btn;
	protected JToggleButton text_pg_just_bottom_btn;
	protected JPanel text_hjust_btn_pnl;
	protected ButtonGroup text_hjust_btn_grp;
	protected JToggleButton text_ln_just_left_btn;
	protected JToggleButton text_ln_just_center_btn;
	protected JToggleButton text_ln_just_right_btn;
	
	
	public WMsgTextToolbar(WController c, boolean includeBG) {
		super(c);
		
		// use a FlowLayout with no margins to give more control of separation
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

		// color pickers
		if (includeBG) {
			bg_color_btn = new JButton(open_bg_color_picker);
			bg_color_btn.setToolTipText(
					I18N.get("wysiwyg.epanel.bg_color_picker_title"));
			DmsColor bgc = controller.getBackgroundColor();
			if (bgc != null) {
				bgColor = bgc.color;
				bg_color_btn.setIcon(
						WMsgColorChooser.createColorIcon(bgColor, 16, 16));
				bg_color_btn.setMargin(new Insets(0,0,0,0));
			}
			add(bg_color_btn);
			add(Box.createHorizontalStrut(30));
		}
		
		fg_color_btn = new JButton(open_fg_color_picker);
		fg_color_btn.setToolTipText(
				I18N.get("wysiwyg.epanel.fg_color_picker_title"));
		DmsColor fgc = controller.getForegroundColor();
		if (fgc != null) {
			fgColor = fgc.color;
			fg_color_btn.setIcon(
					WMsgColorChooser.createColorIcon(fgColor, 16, 16));
			fg_color_btn.setMargin(new Insets(0,0,0,0));
		}
		add(fg_color_btn);
		add(Box.createHorizontalStrut(10));
		
		// fonts
		fontOptions = new JComboBox<Font>();		
		fontOptions.setModel(
				new IComboBoxModel<Font>(controller.getFontModel()));
		fontOptions.setSelectedItem(controller.getDefaultFont());
		fontOptions.addActionListener(setFont);
		add(fontOptions);
		add(Box.createHorizontalStrut(10));
		
		// disable color picker buttons for unknown color schemes or if no
		// MultiConfig
		ColorScheme cs = controller.getColorScheme();
		if (cs == ColorScheme.UNKNOWN) {
			fg_color_btn.setEnabled(false);
			if (includeBG)
				bg_color_btn.setEnabled(false);
		}
		
		// justification buttons
		text_pg_just_btn_grp = new ButtonGroup();
		text_pg_just_top_btn = new JToggleButton(controller.pageJustifyTop);
		text_pg_just_middle_btn = new JToggleButton(controller.pageJustifyMiddle);
		text_pg_just_bottom_btn = new JToggleButton(controller.pageJustifyBottom);
		text_pg_just_top_btn.setSelected(true);
		text_pg_just_btn_grp.add(text_pg_just_top_btn);
		text_pg_just_btn_grp.add(text_pg_just_middle_btn);
		text_pg_just_btn_grp.add(text_pg_just_bottom_btn);
		
		// set icons for justification buttons
		ImageIcon text_vjust_top_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_vjust_top");
		text_pg_just_top_btn.setIcon(text_vjust_top_icon);
		text_pg_just_top_btn.setHideActionText(true);
		text_pg_just_top_btn.setToolTipText(
				I18N.get("wysiwyg.epanel.text_vjust_top"));
		text_pg_just_top_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_vjust_center_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_vjust_center");
		text_pg_just_middle_btn.setIcon(text_vjust_center_icon);
		text_pg_just_middle_btn.setHideActionText(true);
		text_pg_just_middle_btn.setToolTipText(
				I18N.get("wysiwyg.epanel.text_vjust_middle"));
		text_pg_just_middle_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_vjust_bottom_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_vjust_bottom");
		text_pg_just_bottom_btn.setIcon(text_vjust_bottom_icon);
		text_pg_just_bottom_btn.setHideActionText(true);
		text_pg_just_bottom_btn.setToolTipText(
				I18N.get("wysiwyg.epanel.text_vjust_bottom"));
		text_pg_just_bottom_btn.setMargin(new Insets(0,0,0,0));
		
		text_vjust_btn_pnl = new JPanel();
		text_vjust_btn_pnl.setLayout(
				new BoxLayout(text_vjust_btn_pnl, BoxLayout.X_AXIS));
		text_vjust_btn_pnl.add(text_pg_just_top_btn);
		text_vjust_btn_pnl.add(text_pg_just_middle_btn);
		text_vjust_btn_pnl.add(text_pg_just_bottom_btn);
		add(text_vjust_btn_pnl);
		add(Box.createHorizontalStrut(10));
		
		text_hjust_btn_grp = new ButtonGroup();
		text_ln_just_left_btn = new JToggleButton(controller.lineJustifyLeft);
		text_ln_just_center_btn = new JToggleButton(
				controller.lineJustifyCenter);
		text_ln_just_right_btn = new JToggleButton(controller.lineJustifyRight);
		text_ln_just_center_btn.setSelected(true);
		text_hjust_btn_grp.add(text_ln_just_left_btn);
		text_hjust_btn_grp.add(text_ln_just_center_btn);
		text_hjust_btn_grp.add(text_ln_just_right_btn);
		
		ImageIcon text_hjust_left_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_hjust_left");
		text_ln_just_left_btn.setIcon(text_hjust_left_icon);
		text_ln_just_left_btn.setHideActionText(true);
		text_ln_just_left_btn.setToolTipText(
				I18N.get("wysiwyg.epanel.text_hjust_left"));
		text_ln_just_left_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_hjust_center_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_hjust_center");
		text_ln_just_center_btn.setIcon(text_hjust_center_icon);
		text_ln_just_center_btn.setHideActionText(true);
		text_ln_just_center_btn.setToolTipText(
				I18N.get("wysiwyg.epanel.text_hjust_center"));
		text_ln_just_center_btn.setMargin(new Insets(0,0,0,0));
		
		ImageIcon text_hjust_right_icon = Icons.getIconByPropName(
				"wysiwyg.epanel.text_hjust_right");
		text_ln_just_right_btn.setIcon(text_hjust_right_icon);
		text_ln_just_right_btn.setHideActionText(true);
		text_ln_just_right_btn.setToolTipText(
				I18N.get("wysiwyg.epanel.text_hjust_right"));
		text_ln_just_right_btn.setMargin(new Insets(0,0,0,0));
		
		text_hjust_btn_pnl = new JPanel();
		text_hjust_btn_pnl.setLayout(
				new BoxLayout(text_hjust_btn_pnl, BoxLayout.X_AXIS));
		text_hjust_btn_pnl.add(text_ln_just_left_btn);
		text_hjust_btn_pnl.add(text_ln_just_center_btn);
		text_hjust_btn_pnl.add(text_ln_just_right_btn);
		add(text_hjust_btn_pnl);
	}
	
	/** ActionListener to set the current font */
	private ActionListener setFont = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			controller.setFont((Font) fontOptions.getSelectedItem());
		}
	};
	
	/** Set the foreground or background color to c. The color to set is
	 *  specified by passing "foreground" or "background" in the fgbg 
	 *  parameter.
	 */
	public void setColor(Color c, String mode) {
		if (mode.equalsIgnoreCase(WMsgColorChooser.FOREGROUND))
			setForegroundColor(c);
		else if (mode.equalsIgnoreCase(WMsgColorChooser.BACKGROUND))
			setBackgroundColor(c);
	}
	
	/** Set the foreground color on the controller and change the button
	 *  appearance
	 */
	public void setForegroundColor(Color c) {
		fgColor = c;
		controller.setForegroundColor(new DmsColor(fgColor));
		applyForegroundColorIcon();
	}
	
	/** Set the icon color on the foreground color chooser button using our
	 *  current foreground color.
	 */
	public void applyForegroundColorIcon() {
		fg_color_btn.setIcon(WMsgColorChooser.createColorIcon(fgColor, 16, 16));
	}
	
	/** Set the background color on the controller and change the button
	 *  appearance
	 */
	public void setBackgroundColor(Color c) {
		bgColor = c;
		controller.setBackgroundColor(new DmsColor(bgColor));
		applyBackgroundColorIcon();
	}

	/** Set the icon color on the background color chooser button using our
	 *  current background color.
	 */
	public void applyBackgroundColorIcon() {
		if (bg_color_btn != null)
			bg_color_btn.setIcon(WMsgColorChooser.createColorIcon(bgColor, 16, 16));
	}
	
	/** Update the font combo-box given the font number value fNum. */
	public void updateFontMenu(Integer fNum) {
//		String s = (fNum != null) ? fNum.toString() : "null";
//		controller.println("Updating font with: %s", s);
		// first disable the action listener on the combo-box to avoid
		// triggering an event when we change the value
		fontOptions.removeActionListener(setFont);
		
		if (fNum != null) {
			// if we got a non-null value, find it in the list
			ComboBoxModel<Font> fontModel = fontOptions.getModel();
			for (int i = 0; i < fontModel.getSize(); ++i) {
				Font f = fontModel.getElementAt(i);
				if (f != null && f.getNumber() == fNum) {
					fontOptions.setSelectedIndex(i);
					break;
				}
			}
		} else
			// if we didn't get a font, go to no selection
			fontOptions.setSelectedIndex(0);
		
		// reactivate the listener
		fontOptions.addActionListener(setFont);
	}
	
	/** Update the page justification buttons given the value jp. */
	public void updatePageJustBtns(JustificationPage jp) {
//		String s = (jp != null) ? jp.toString() : "null";
//		controller.println("Updating page just with: %s", s);
		if (jp == JustificationPage.TOP)
			text_pg_just_top_btn.setSelected(true);
		else if (jp == JustificationPage.MIDDLE)
			text_pg_just_middle_btn.setSelected(true);
		else if (jp == JustificationPage.BOTTOM)
			text_pg_just_bottom_btn.setSelected(true);
		else {
			// null - deselect all
			text_pg_just_top_btn.setSelected(false);
			text_pg_just_middle_btn.setSelected(false);
			text_pg_just_bottom_btn.setSelected(false);
		}
	}
	
	/** Update the line justification buttons given the value jl. */
	public void updateLineJustBtns(JustificationLine jl) {
//		String s = (jl != null) ? jl.toString() : "null";
//		controller.println("Updating line just with: %s", s);
		if (jl == JustificationLine.LEFT)
			text_ln_just_left_btn.setSelected(true);
		else if (jl == JustificationLine.CENTER)
			text_ln_just_center_btn.setSelected(true);
		else if (jl == JustificationLine.RIGHT)
			text_ln_just_right_btn.setSelected(true);
		else {
			// null - deselect all
			text_ln_just_left_btn.setSelected(false);
			text_ln_just_center_btn.setSelected(false);
			text_ln_just_right_btn.setSelected(false);
		}
	}
	
	/** Update the toolbar buttons/combo-box from the controller state. */
	public void updateToolbar() {
		// font
		updateFontMenu(controller.getActiveFont());
		
		// TODO colors ?? (should we?)
		
		// page and line justification
		updatePageJustBtns(controller.getActivePageJustification());
		updateLineJustBtns(controller.getActiveLineJustification());
	}
	
	/** Save the current toolbar button/combobox state into the given WHistory
	 *  object (allows the toolbar state to be reset when undoing/redoing).
	 */
	public void saveToolbarState(WHistory wh) {
		// TODO font combo-box
		
		// foreground and background color
	}
	
	WMsgTextToolbar tb = this;
	
	/** Foreground color picker action */
	private final IAction open_fg_color_picker =
			new IAction("wysiwyg.epanel.fg_color_picker_btn") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			String title = I18N.get("wysiwyg.epanel.fg_color_picker_title");
			WMsgColorChooser ccForm = new WMsgColorChooser(controller, tb,
					title, fgColor, WMsgColorChooser.FOREGROUND);
			SmartDesktop desktop = controller.getDesktop();
			desktop.show(ccForm);
		}
	};
	
	/** Background color picker action */
	private final IAction open_bg_color_picker =
			new IAction("wysiwyg.epanel.bg_color_picker_btn") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			String title = I18N.get("wysiwyg.epanel.bg_color_picker_title");
			WMsgColorChooser ccForm = new WMsgColorChooser(controller, tb,
					title, fgColor, WMsgColorChooser.BACKGROUND);
			SmartDesktop desktop = controller.getDesktop();
			desktop.show(ccForm);
		}
	};
}