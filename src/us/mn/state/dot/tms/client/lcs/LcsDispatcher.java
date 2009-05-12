/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsSelectionEvent;
import us.mn.state.dot.tms.client.TmsSelectionListener;

/**
 * GUI for controlling a LaneControlSignal object.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LcsDispatcher extends JPanel implements TmsSelectionListener {

	/** Panel used for drawing an LCS */
	protected final LcsPanel lcsPanel = new LcsPanel(45);

	/** Displays the name of the selected LCS */
	protected final JTextField txtId = new JTextField();

	/** The texfield used to display the verify camera */
	protected final JTextField txtCamera = new JTextField();

	/** Displays the location of the LCS */
	protected final JTextField txtLocation = new JTextField();

	/** Displays the current operation of the selected LCS */
	protected final JTextField txtOperation = new JTextField();

	/** Button used to send a new command to the LCS */
	protected final JButton sendBtn = new JButton("Send");

	/** Button used to make the LCS go dark */
	protected final JButton clearBtn = new JButton("Clear");

	/** Currently logged in user name */
	protected final String userName;

	/** Device handler for LCS devices */
	protected final LcsHandler handler;

	protected LcsProxy selectedLcs = null;

	protected final LcsMessageSelector messageSelector =
		new LcsMessageSelector();

	/** Create a new LCS dispatcher */
	public LcsDispatcher(LcsHandler handler) {
		super( new GridBagLayout() );
		this.handler = handler;
		handler.getSelectionModel().addTmsSelectionListener( this );
		userName = handler.getUser().getName();
		setBorder(BorderFactory.createTitledBorder(
			"Selected Lane Control Signal"));
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets = new Insets(2, 4, 2, 4);
		bag.anchor = GridBagConstraints.EAST;
		add(new JLabel("ID"), bag);
		bag.gridx = 2;
		add(new JLabel("Camera"), bag);
		bag.gridx = 0;
		bag.gridy = 1;
		add(new JLabel("Location"), bag);
		bag.gridy = 2;
		add(new JLabel("Operation"), bag);
		bag.gridx = 1;
		bag.gridy = 0;
		bag.fill = GridBagConstraints.HORIZONTAL;
		bag.weightx = 1;
		txtId.setEditable(false);
		add(txtId, bag);
		bag.gridx = 3;
		txtCamera.setEditable(false);
		add(txtCamera, bag);
		bag.gridx = 1;
		bag.gridy = 1;
		bag.gridwidth = 3;
		txtLocation.setEditable(false);
		add(txtLocation, bag);
		bag.gridy = 2;
		txtOperation.setEditable(false);
		add(txtOperation, bag);
		JPanel panel = new JPanel();
		panel.add( lcsPanel );
		bag.gridx = 0;
		bag.gridy = 3;
		bag.gridwidth = 4;
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.NONE;
		add(panel, bag);
		bag.gridy = 4;
		add(messageSelector, bag);
		bag.gridy = 5;
		add(buildButtonPanel(), bag);
		clear();
	}

	/** Dispose of the LCS dispatcher */
	public void dispose() {
		removeAll();
		messageSelector.removeAll();
		lcsPanel.removeAll();
		selectedLcs = null;
	}

	/** Set the selected LaneControlSignal */
	public void setSelected(LcsProxy lcs) {
		selectedLcs = lcs;
		if(lcs != null) {
			messageSelector.setEnabled(true);
			messageSelector.setSignals(lcs.getSignals());
			sendBtn.setEnabled(true);
			clearBtn.setEnabled(true);
			clearBtn.setAction(new ClearLcsAction(lcs,
				handler.getConnection()));
		} else
			clear();
		refreshUpdate();
		refreshStatus();
	}

	/** Called whenever the selected TMS object changes */
	public void selectionChanged(TmsSelectionEvent e) {
		final TMSObject o = e.getSelected();
		if(o instanceof LcsProxy)
			setSelected((LcsProxy)o);
	}

	/** Refresh the update status of the LCS */
	public void refreshUpdate() {
		LcsProxy lcs = selectedLcs;	// Avoid NPE
		if(lcs != null) {
			txtCamera.setText(lcs.getCameraId());
			txtId.setText(lcs.getId());
			txtLocation.setText(lcs.getDescription());
		}
	}

	/** Refresh the status of the LCS */
	public void refreshStatus() {
		LcsProxy lcs = selectedLcs;	// Avoid NPE
		lcsPanel.setLcs(lcs);
		if(lcs != null) {
			txtOperation.setText(lcs.getOperation());
			if(lcs.isFailed()) {
				txtOperation.setForeground(Color.WHITE);
				txtOperation.setBackground(Color.GRAY);
			} else {
				txtOperation.setForeground(null);
				txtOperation.setBackground(null);
			}
		}
	}

	/** Build the panel that holds the send and clear buttons */
	protected Component buildButtonPanel() {
		Box pnlButtons = Box.createHorizontalBox();
		new ActionJob(sendBtn) {
			public void perform() throws Exception {
				sendSignals();
			}
		};
		pnlButtons.add(sendBtn);
		pnlButtons.add(Box.createHorizontalStrut(4));
		pnlButtons.add(clearBtn);
		return pnlButtons;
	}

	/** Clear all of the fields */
	protected void clear() {
		txtId.setText("");
		txtCamera.setText("");
		txtLocation.setText("");
		txtOperation.setText("");
		txtOperation.setForeground(null);
		txtOperation.setBackground(null);
		messageSelector.clearSelections();
		messageSelector.setEnabled(false);
		sendBtn.setEnabled(false);
		clearBtn.setEnabled(false);
		lcsPanel.clear();
	}

	/** Send the selected signals to the selected LCS object */
	protected void sendSignals() {
		LcsProxy lcs = selectedLcs;	// Avoid NPE
		if(lcs != null) {
			int[] signals = messageSelector.getSignals();
			if(signals != null)
				lcs.setSignals(signals, userName);
		}
	}
}
