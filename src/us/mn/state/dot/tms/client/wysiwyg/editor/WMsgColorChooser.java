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
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiConfig;

/**
 * WYSIWYG DMS Message Editor Color Chooser
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgColorChooser extends AbstractForm {
	
	/** Handle to the controller managing everything */
	private WController controller;
	
	/** SmartDesktop handle for closing the form */
	private SmartDesktop desktop;
	
	/** Handle to the toolbar that created us */
	private WToolbar toolbar;
	
	/** Color scheme - determines what the color chooser looks like */
	private ColorScheme colorScheme;
	
	/** Selected color */
	private Color color;
	
	/** DMS (classic) color */
	private DmsColor classicColor;
	
	/** Monochrome color (for 8-bit monochrome) */
	private Integer monoColor = 255;
	
	/** "Mode" of color chooser so we know whether we're editing the fore- or
	 *  background color */
	public final static String FOREGROUND="foreground";
	public final static String BACKGROUND="background";
	public final static String COLOR_RECT="color rectangle";
	private String mode;
	
	/** Color chooser components - which one used changes depending on the
	 *  color scheme */
	private JColorChooser fullColorChooser;
	private JList<String> classicColorChooser;
	private DefaultListModel<String> classicColorModel;
	private Map<String, DmsColor> classicColorMap;
	private JList<String> mono1ColorChooser;
	private DefaultListModel<String> mono1ColorModel;
	private Map<String, DmsColor> mono1ColorMap;
	private JSlider monochrome8BitSlider;

	/** Buttons */
	private JPanel btn_pnl;
	private JButton ok_btn;
	private JButton cancel_btn;
	
	public WMsgColorChooser(WController c, WToolbar tb, String title,
			Color col, String md) {
		super(title, true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		controller = c;
		desktop = controller.getDesktop();
		toolbar = tb;
		MultiConfig mc = controller.getMultiConfig();
		colorScheme = (mc != null) ? mc.getColorScheme()
				: ColorScheme.UNKNOWN;
		color = col;
		mode = md;
		
		// create and add the color chooser panes
		initColorChooser();
		
		// now the buttons
		addButtons();
	}
	
	public WMsgColorChooser(WController c, WToolbar tb, String title,
			Integer mnc, String md) {
		super(title, true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		controller = c;
		desktop = controller.getDesktop();
		toolbar = tb;
		MultiConfig mc = controller.getMultiConfig();
		colorScheme = (mc != null) ? mc.getColorScheme()
				: ColorScheme.UNKNOWN;
		monoColor = (mnc != null) ? mnc : 255;
		mode = md;
		
		// create and add the color chooser panes
		initColorChooser();
		
		// now the buttons
		addButtons();
	}
	
	/** Add buttons to the color chooser. */
	private void addButtons() {
		ok_btn = new JButton(chooseColor);
		cancel_btn = new JButton(cancel);
		btn_pnl = new JPanel();
		btn_pnl.add(ok_btn);
		btn_pnl.add(cancel_btn);
		add(btn_pnl);
	}
	
	/** Initialize the color chooser based on the provided color scheme. */
	private void initColorChooser() {
		// for full color, use Swing's JColorChooser which provides a full-
		// featured color chooser with all the bells and whistles
		if (colorScheme == ColorScheme.COLOR_24_BIT) {
			initFullColorChooser();
			add(fullColorChooser);
		} else if (colorScheme == ColorScheme.COLOR_CLASSIC) {
			// for classic color scheme, use a JList filled with the classic
			// colors as options
			initClassicColorChooser();
			add(classicColorChooser);
		} else if (colorScheme == ColorScheme.MONOCHROME_8_BIT) {
			// for 8-bit monochrome, use a slider from 0 to 255
			monochrome8BitSlider = new JSlider(0, 255, monoColor);
			add(monochrome8BitSlider);
		} else if (colorScheme == ColorScheme.MONOCHROME_1_BIT) {
			// for 1-bit monochrome, use a list with 2 colors in it
			initMono1ColorChooser();
			add(mono1ColorChooser);
		}
	}
	
	private void initFullColorChooser() {
		// start with a JColorChooser
		fullColorChooser = new JColorChooser(color);
		
		// remove everything besides "Swatches" and "RGB"
		for (AbstractColorChooserPanel c:
			fullColorChooser.getChooserPanels()) {
			String n = c.getDisplayName();
			if (n.equals("Swatches") || n.equals("RGB")) {
				continue;
			} else
				fullColorChooser.removeChooserPanel(c);
		}
		
		initClassicColorMap();
		fullColorChooser.addChooserPanel(new ClassicColorChooser());
	}
	
	private void initClassicColorMap() {
		classicColorMap = new LinkedHashMap<String, DmsColor>();
		classicColorMap.put("Black", DmsColor.BLACK);
		classicColorMap.put("Red", DmsColor.RED);
		classicColorMap.put("Yellow", DmsColor.YELLOW);
		classicColorMap.put("Green", DmsColor.GREEN);
		classicColorMap.put("Cyan", DmsColor.CYAN);
		classicColorMap.put("Blue", DmsColor.BLUE);
		classicColorMap.put("Magenta", DmsColor.MAGENTA);
		classicColorMap.put("White", DmsColor.WHITE);
		classicColorMap.put("Orange", DmsColor.ORANGE);
		classicColorMap.put("Amber", DmsColor.AMBER);
	}
	
	private void initClassicColorChooser() {
		// use a hashmap to store classic colors (we use a linked hash map
		// to preserve order)
		initClassicColorMap();
		
		// create the list model and fill it with colors
		classicColorModel = new DefaultListModel<String>();
		for (Map.Entry<String, DmsColor> el: classicColorMap.entrySet()) {
			classicColorModel.addElement(el.getKey());
		}
		
		// create the JList with the list model and add a custom renderer
		classicColorChooser = new JList<String>(classicColorModel);
		classicColorChooser.setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		classicColorChooser.setCellRenderer(new ColorListCellRenderer());
	}
	
	private class ClassicColorChooser extends AbstractColorChooserPanel
				implements ListSelectionListener {
		
		private DefaultListModel<String> ccModel;
		private JList<String> ccList;
		
		@Override
		protected void buildChooser() {
			// create the list model and fill it with colors
			ccModel = new DefaultListModel<String>();
			for (Map.Entry<String, DmsColor> el: classicColorMap.entrySet())
				ccModel.addElement(el.getKey());
			
			// create the JList with the list model and add a custom renderer
			ccList = new JList<String>(ccModel);
			ccList.setSelectionMode(
					ListSelectionModel.SINGLE_SELECTION);
			ccList.setCellRenderer(new ColorListCellRenderer());
			ccList.getSelectionModel().addListSelectionListener(this);
			
			// add the list to the panel
			add(ccList);
		}

		@Override
		public String getDisplayName() {
			return I18N.get("wysiwyg.color_chooser.classic");
		}
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				int indx = lsm.getMinSelectionIndex();
				if (indx != -1) {
					String cName = ccModel.get(indx);
					Color c = classicColorMap.get(cName).color;
					getColorSelectionModel().setSelectedColor(c);
				}
			}
		}
		
		/** Currently not used */
		@Override
		public Icon getLargeDisplayIcon() { return null; }
		@Override
		public Icon getSmallDisplayIcon() { return null; }
		@Override
		public void updateChooser() { }
	}
	
	/** Initialize a color chooser for 1-bit monochrome signs. This just
	 *  displays a list with 2 options - the sign's default foreground (lit)
	 *  and background (unlit) colors.
	 */
	private void initMono1ColorChooser() {
		// get the default colors from the MultiConfig
		MultiConfig mc = controller.getMultiConfig();
		if (mc != null) {
			DmsColor lit = mc.getDefaultFG();
			DmsColor unlit = mc.getDefaultBG();
			
			mono1ColorMap = new LinkedHashMap<String, DmsColor>();
			mono1ColorMap.put("Lit", lit);
			mono1ColorMap.put("Unlit", unlit);
			
			// create the list model and fill it with colors
			mono1ColorModel = new DefaultListModel<String>();
			for (Map.Entry<String, DmsColor> el : mono1ColorMap.entrySet()) {
				mono1ColorModel.addElement(el.getKey());
			}
			
			// create the JList with the list model
			mono1ColorChooser = new JList<String>(mono1ColorModel);
			mono1ColorChooser.setSelectionMode(
					ListSelectionModel.SINGLE_SELECTION);
			
			mono1ColorChooser.setCellRenderer(new ColorListCellRenderer());
		}
	}

	// setup the list cell renderer to include color icons
	class ColorListCellRenderer extends JLabel implements 
			ListCellRenderer<String> {
		public ColorListCellRenderer() {
			setOpaque(true);
		}
		
		@Override
		public Component getListCellRendererComponent(
				JList<? extends String> list, String cName, int index,
				boolean isSelected, boolean hasFocus) {
			// get the DmsColor object from the hash map
			DmsColor c;
			if (colorScheme == ColorScheme.MONOCHROME_1_BIT)
				c = mono1ColorMap.get(cName);
			else
				c = classicColorMap.get(cName);
			
			// use the color icon and the name of the color in the list
			setIcon(createColorIcon(c.color, 16,16));
			setText(cName);
			
			// change the color if selected
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			return this;
		}
	}
	
	/** Set the chosen color and close the form. */
	private final IAction chooseColor = new IAction("wysiwyg.color_chooser.ok_btn") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			color = null;
			classicColor = null;
			monoColor = null;
			if (colorScheme == ColorScheme.COLOR_24_BIT) {
				color = fullColorChooser.getColor();
			} else if (colorScheme == ColorScheme.COLOR_CLASSIC) {
				int i = classicColorChooser.getSelectedIndex();
				if (i != -1) {
					String cName = classicColorModel.get(i);
					classicColor = classicColorMap.get(cName);
				}
			} else if (colorScheme == ColorScheme.MONOCHROME_8_BIT) {
				monoColor = monochrome8BitSlider.getValue();
			} else if (colorScheme == ColorScheme.MONOCHROME_1_BIT) {
				int i = mono1ColorChooser.getSelectedIndex();
				if (i != -1) {
					String cName = mono1ColorModel.get(i);
					monoColor = mono1ColorMap.get(cName).isLit() ? 1 : 0;
				}
			} else {
				System.out.println("Didn't get a color! Using default foreground!");
				color = controller.getMultiConfig().getDefaultFG().color;
			}
			
			if (color != null)
				toolbar.setColor(color, mode);
			else if (classicColor != null)
				toolbar.setColor(classicColor, mode);
			else if (monoColor != null)
				toolbar.setColor(monoColor, mode);
			close(desktop);
		}
	};
	
	/** Close the form without doing anything. */
	private final IAction cancel = new IAction("wysiwyg.color_chooser.cancel") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			close(desktop);
		}
	};
	
	public static  ImageIcon createColorIcon(Color c, int width, int height) {
		BufferedImage image = new BufferedImage(width, height,
				java.awt.image.BufferedImage.TYPE_INT_RGB);
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