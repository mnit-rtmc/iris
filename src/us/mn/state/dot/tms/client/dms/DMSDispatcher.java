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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.rmi.RemoteException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.TmsSelectionEvent;
import us.mn.state.dot.tms.client.TmsSelectionListener;
import us.mn.state.dot.tms.client.TmsSelectionModel;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.dms.FontComboBox;
import us.mn.state.dot.tms.utils.Agency;
import us.mn.state.dot.tms.utils.I18NMessages;
import us.mn.state.dot.sonar.client.TypeCache;

/**
 * The DMSDispatcher is a GUI component for creating and deploying DMS messages.
 * @see FontComboBox, Font, FontImpl, SignMessage, DMSPanel, TmsSelectionModel
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class DMSDispatcher extends JPanel implements TmsSelectionListener {

	/** Panel used for drawing a DMS */
	protected final DMSPanel pnlSign;

	/** Displays the id of the DMS */
	protected final JTextField txtId = new JTextField();

	/** Displays the verify camera for the DMS */
	protected final JTextField txtCamera = new JTextField();

	/** Displays the location of the DMS */
	protected final JTextField txtLocation = new JTextField();

	/** Displays the current operation of the DMS */
	protected final JTextField txtOperation = new JTextField();

	/** Displays the brightness of the DMS */
	protected final JTextField txtBrightness = new JTextField();

	/** Used to select the expires time for a message (optional) */
	protected JComboBox cmbExpire = null;

	/** Used to select the DMS font for a message (optional) */
	protected FontComboBox cmbFont = null;

	/** Button used to send a message to the DMS */
	protected final JButton btnSend = 
		new JButton(I18NMessages.get("DMSDispatcher.SendButton"));

	/** Button used to clear the DMS. Text also set in ClearDmsAction */
	protected final JButton btnClear = 
		new JButton(I18NMessages.get("DMSDispatcher.ClearButton"));

	/** Button used to get the DMS status (optional) */
	protected JButton btnGetStatus = null;

	protected final TmsSelectionModel selectionModel;

	/** Currently logged in user name */
	protected final String userName;

	/** The currently selected DMS */
	protected DMSProxy selectedSign = null;

	/** Message selector widget */
	protected final MessageSelector messageSelector;

	/** Create a new DMS dispatcher */
	public DMSDispatcher(DMSHandler handler, final SonarState st, TmsConnection tmsConnection) {
		super( new GridBagLayout() );
		messageSelector = new MessageSelector(st.getDmsSignGroups(),
			st.getSignText(),tmsConnection);
		userName = handler.getUser().getName();
		selectionModel = handler.getSelectionModel();
		pnlSign = new DMSPanel(st.getSystemPolicy());
		setBorder(BorderFactory.createTitledBorder(
			I18NMessages.get("DMSDispatcher.GroupBoxTitle")));
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets = new Insets(2, 4, 2, 4);
		bag.anchor = GridBagConstraints.EAST;
		add(new JLabel("ID"), bag);
		bag.gridx = 2;
		add(new JLabel("Camera"), bag);
		bag.gridx = 0;
		bag.gridy = 1;
		add(new JLabel("Location"), bag);
		bag.gridx = 2;
		add(new JLabel("Brightness"), bag);
		bag.gridx = 0;
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
		txtLocation.setEditable(false);
		add(txtLocation, bag);
		bag.gridx = 3;
		txtBrightness.setEditable(false);
		add(txtBrightness, bag);
		bag.gridx = 1;
		bag.gridy = 2;
		bag.gridwidth = 3;
		txtOperation.setEditable(false);
		add(txtOperation, bag);
		bag.gridx = 0;
		bag.gridy = 3;
		bag.gridwidth = 4;
		bag.insets.top = 6;
		add(pnlSign, bag);
		Box boxRight = Box.createVerticalBox();
		boxRight.add(Box.createVerticalGlue());

		// add optional duration combobox
		if(!Agency.isId(Agency.CALTRANS_D10))
			boxRight.add(buildDurationBox());

		// add optional font selection combo box
		if(Agency.isId(Agency.CALTRANS_D10)) {
			JPanel fjp=buildFontSelectorBox(st.getFonts());
			if(fjp != null)
				boxRight.add(fjp);
		}

		boxRight.add(Box.createVerticalStrut(4));
		boxRight.add(buildButtonPanel());
		boxRight.add(Box.createVerticalGlue());
		Box deployBox = Box.createHorizontalBox();
		deployBox.add(messageSelector);
		deployBox.add(boxRight);
		bag.gridy = 4;
		add(deployBox, bag);
		clearSelected();
		selectionModel.addTmsSelectionListener(this);
	}

	/** Dispose of the dispatcher */
	public void dispose() {
		removeAll();
		selectedSign = null;
		selectionModel.removeTmsSelectionListener(this);
	}

	protected Box buildButtonPanel() {
		new ActionJob(btnSend) {
			public void perform() throws Exception {
				sendMessage();
			}
		};
		btnSend.setToolTipText(I18NMessages.get(
			"DMSDispatcher.SendButton.ToolTip"));
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(btnSend);
		box.add(Box.createHorizontalGlue());
		box.add(btnClear);
		box.add(Box.createHorizontalGlue());

		// add optional 'get status' button
		if(Agency.isId(Agency.CALTRANS_D10)) {
			btnGetStatus = new JButton(I18NMessages.get(
				"DMSDispatcher.GetStatusButton"));
			btnGetStatus.setToolTipText(I18NMessages.get(
				"DMSDispatcher.GetStatusButton.ToolTip"));
			new ActionJob(this, btnGetStatus) {
				public void perform() throws Exception {
					selectedSign.dms.getSignMessage();
				}
			};
			box.add(btnGetStatus);
			box.add(Box.createHorizontalGlue());
		}
		return box;
	}

	/** Build the optional message duration box */
	protected JPanel buildDurationBox() {
		cmbExpire = new JComboBox();
		JPanel p = new JPanel(new FlowLayout());
		p.add(new JLabel("Duration"));
		p.add(cmbExpire);
		cmbExpire.addItem(new Expiration("Indefinite",
			SignMessage.DURATION_INFINITE));
		cmbExpire.addItem( new Expiration( "5 Minutes", 5 ) );
		cmbExpire.addItem( new Expiration( "15 Minutes", 15 ) );
		cmbExpire.addItem( new Expiration( "30 Minutes", 30 ) );
		cmbExpire.addItem( new Expiration( "45 Minutes", 45 ) );
		cmbExpire.addItem( new Expiration( "1 Hour", 60 ) );
		cmbExpire.addItem( new Expiration( "1.5 Hours", 90 ) );
		cmbExpire.addItem( new Expiration( "2 Hours", 120 ) );
		cmbExpire.addItem( new Expiration( "2.5 Hours", 150 ) );
		cmbExpire.addItem( new Expiration( "3 Hours", 180 ) );
		cmbExpire.addItem( new Expiration( "3.5 Hours", 210 ) );
		cmbExpire.addItem( new Expiration( "4 Hours", 240 ) );
		cmbExpire.addItem( new Expiration( "4.5 Hours", 270 ) );
		cmbExpire.addItem( new Expiration( "5 Hours", 300 ) );
		cmbExpire.addItem( new Expiration( "5.5 Hours", 330 ) );
		cmbExpire.addItem( new Expiration( "6 Hours", 360 ) );
		cmbExpire.addItem( new Expiration( "6.5 Hours", 390 ) );
		cmbExpire.addItem( new Expiration( "7 Hours", 420 ) );
		cmbExpire.addItem( new Expiration( "7.5 Hours", 420 ) );
		cmbExpire.addItem( new Expiration( "8 Hours", 480 ) );
		cmbExpire.addItem( new Expiration( "9 Hours", 540 ) );
		cmbExpire.addItem( new Expiration( "10 Hours", 600 ) );
		cmbExpire.addItem( new Expiration( "11 Hours", 660 ) );
		cmbExpire.addItem( new Expiration( "12 Hours", 720 ) );
		cmbExpire.addItem( new Expiration( "13 Hours", 780 ) );
		cmbExpire.addItem( new Expiration( "14 Hours", 840 ) );
		cmbExpire.addItem( new Expiration( "15 Hours", 900 ) );
		cmbExpire.addItem( new Expiration( "16 Hours", 960 ) );
		cmbExpire.addItem( new Expiration( "17 Hours", 1020 ) );
		cmbExpire.setSelectedIndex( 0 );
		return p;
	}

	/** Build the font selector combo box */
	protected JPanel buildFontSelectorBox(TypeCache<Font> tcf) {
		assert tcf != null;
		if(tcf == null)
			return null;
		cmbFont = new FontComboBox(this, tcf);
		JPanel p = new JPanel(new FlowLayout());
		p.add(new JLabel("Font"));
		p.add(cmbFont);
		return p;
	}

	/** Set the selected DMS */
	protected void setSelected(DMSProxy proxy) throws RemoteException {
		selectedSign = proxy;
		if(proxy == null)
			clearSelected();
		else {
			btnSend.setEnabled(true);
			btnClear.setEnabled(true);
			btnClear.setAction(new ClearDmsAction(proxy, userName));
			if(btnGetStatus != null)
				btnGetStatus.setEnabled(true);
			if(cmbExpire != null) {
				cmbExpire.setEnabled(true);
				cmbExpire.setSelectedIndex(0);
			}
			if(cmbFont != null) {
				cmbFont.setEnabled(true);
				cmbFont.setDefaultSelection();
			}
			proxy.updateUpdateInfo(); // update global messages
			pnlSign.setSign(proxy);
			refreshUpdate();
			refreshStatus();
		}
	}

	/** Clear the selected DMS */
	protected void clearSelected() {
		selectedSign = null;
		txtId.setText("");
		txtCamera.setText("");
		txtLocation.setText("");
		txtBrightness.setText("");
		if(cmbExpire != null) {
			cmbExpire.setEnabled(false);
			cmbExpire.setSelectedIndex(0);
		}
		if(cmbFont != null) {
			cmbFont.setEnabled(false);
			cmbFont.setDefaultSelection();
		}
		messageSelector.setEnabled(false);
		messageSelector.clearSelections();
		btnSend.setEnabled(false);
		btnClear.setEnabled(false);
		if(btnGetStatus!=null)
			btnGetStatus.setEnabled(false);
		pnlSign.setSign(null);
	}

	/** Get the selected duration */
	protected int getDuration() {
		if(cmbExpire == null)
			return SignMessage.DURATION_INFINITE;

		Expiration e = (Expiration)cmbExpire.getSelectedItem();
		if(e != null)
			return e.getDuration();
		else
			return SignMessage.DURATION_INFINITE;
	}

	/** 
	 *  Get the selected font. The font selector is an optional control
	 *  and may be null.
	 *  @return Null if no font is selected or cmbFont is null, else the
	 *	    selected Font.
	 */
	protected Font getSelectedFont() {
		return (cmbFont == null ? null : 
			(Font)cmbFont.getSelectedItem());
	}

	/** Send a new message to the selected DMS object */
	protected void sendMessage() throws TMSException, RemoteException {
		DMSProxy proxy = selectedSign;	// Avoid NPE race
		String message = messageSelector.getMessage();
		if(proxy != null && message != null)
			proxy.dms.setMessage(userName, message, getDuration());
	}

	/** Called whenever the selected DMS changes */
	public void selectionChanged(TmsSelectionEvent e) {
		final TMSObject o = e.getSelected();
		if(o instanceof DMSProxy) {
			new AbstractJob() {
				public void perform() throws RemoteException {
					setSelected((DMSProxy)o);
				}
			}.addToScheduler();
		}
	}

	/** Refresh the update status of the device */
	public void refreshUpdate() {
		DMSProxy proxy = selectedSign;	// Avoid NPE race
		if(proxy != null) {
			txtId.setText(proxy.getId());
			txtLocation.setText(proxy.getDescription());
			txtCamera.setText(proxy.getCameraId());
			messageSelector.updateModel(proxy);
		}
	}

	/** Refresh the status of the device */
	public void refreshStatus() {
		DMSProxy dms = selectedSign;	// Avoid NPE race
		if(dms == null)
			return;
		SignMessage m = dms.getMessage();
		pnlSign.setMessage(m);
		messageSelector.setMessage(dms);
		txtOperation.setText(dms.getOperation());
		if(dms.isFailed()) {
			txtOperation.setForeground(Color.WHITE);
			txtOperation.setBackground(Color.GRAY);
		} else {
			txtOperation.setForeground(null);
			txtOperation.setBackground(null);
		}
		txtBrightness.setText("" + Math.round(
			dms.getLightOutput() / 655.35f));
	}

	/** get the currently selected DMS */
	public DMSProxy getSelectedDms() {
		return selectedSign;
	}

}
