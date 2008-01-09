/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import us.mn.state.dot.tms.LCSModule;

/**
 * GUI for selecting signals to send to a LaneControlSignal.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LcsMessageSelector extends JPanel {

	/** Maximum number of lanes for a lane control signal */
	static protected final int LANES = 4;

	/** Array of selectors to use. One for each lane. */
	protected final SignalSelector[] selectors = new SignalSelector[LANES];

	/** The number of lanes to show */
	protected int lanes;

	/** Create a new LCS message selector */
	public LcsMessageSelector() {
		super(new GridLayout(1, LANES + 2, 1, 0));
		Font font = new Font(null, Font.BOLD, 24);
		JLabel label = new JLabel("L", SwingConstants.CENTER);
		label.setFont(font);
		add(label);
		for(int i = 0; i < selectors.length; i++) {
			SignalSelector selector = new SignalSelector();
			selectors[i] = selector;
			add(selector);
		}
		label = new JLabel("R", SwingConstants.CENTER);
		label.setFont(font);
		add(label);
	}

	/** Enable/disable the toggle buttons */
	public void setEnabled(boolean enabled) {
		for(int i = 0; i < selectors.length; i++)
			selectors[i].setEnabled(enabled);
	}

	/** Set the selected signals */
	public void setSignals(int[] signals) {
		if(signals == null)
			lanes = 0;
		else
			lanes = signals.length;
		for(int i = 0; i < selectors.length; i++) {
			if(i < lanes) {
				selectors[i].setVisible(true);
				selectors[i].setSignal(signals[lanes - i - 1]);
			} else
				selectors[i].setVisible(false);
		}
	}

	/** Get the selected signals */
	public int[] getSignals() {
		int[] signals = new int[lanes];
		for(int i = 0; i < lanes; i++)
			signals[lanes - i - 1] = selectors[i].getSignal();
		return signals;
	}

	/** Clear all toggle button selections */
	public void clearSelections() {
		for(int i = 0; i < selectors.length; i++)
			selectors[i].clear();
	}

	/**
	 * Signal offers a GUI to select between the available signals to send
	 * to a LaneControlSignal.
	 */
	static protected class SignalSelector extends JPanel {

		static protected final int MODULE_SIZE = 30;

		/** Toggle buttons used to give the user a choice */
		protected final LcsToggle[] buttons = new LcsToggle[4];

		/** Button group for toggle buttons */
		protected final ButtonGroup group = new ButtonGroup();

		/** Create a new signal selector */
		public SignalSelector() {
			super(new GridLayout(3, 1, 0, 1));
			LcsToggle button = new LcsToggle(
				new LcsModule(MODULE_SIZE, LCSModule.RED));
			group.add(button);
			buttons[0] = button;
			add(button);
			button = new LcsToggle(new LcsModule(
				MODULE_SIZE, LCSModule.YELLOW));
			group.add(button);
			buttons[1] = button;
			add(button);
			button = new LcsToggle(new LcsModule(
				MODULE_SIZE, LCSModule.GREEN));
			group.add(button);
			buttons[2] = button;
			add(button);
			button = new LcsToggle(new LcsModule(
				MODULE_SIZE, LCSModule.DARK));
			group.add(button);
			buttons[3] = button;
			button.setSelected(true);
		}

		/** Set the enabled attribute */
		public void setEnabled(boolean enabled) {
			for(int i = 0; i < buttons.length; i++)
				buttons[i].setEnabled(enabled);
		}

		/** Set the currently selected signal */
		public void setSignal(int signal) {
			for(int i = 0; i < buttons.length; i++) {
				LcsToggle b = buttons[i];
				if(b.getSignal() == signal)
					b.setSelected(true);
			}
		}

		/** Get the currently selected signal */
		public int getSignal() {
			for(int i = 0; i < buttons.length; i++) {
				LcsToggle b = buttons[i];
				if(b.isSelected())
					return b.getSignal();
			}
			return LCSModule.DARK;
		}

		/** Clear the selection */
		public void clear() {
			for(int i = 0; i < buttons.length; i++) {
				if(buttons[i].getSignal() == LCSModule.DARK)
					buttons[i].setSelected(true);
			}
		}
	}

	/** LcsToggle represents one state of a LCS module */
	static protected class LcsToggle extends JToggleButton {

		protected final LcsModule module;

		/** Create a new LCS toggle button */
		public LcsToggle(LcsModule mod) {
			module = mod;
			setIcon(mod);
			setMargin(new Insets(5, 5, 5, 5));
			setBackground(Color.LIGHT_GRAY);
		}

		/** Get the signal for the toggle button */
		public int getSignal() {
			return module.getSignal();
		}
	}
}
