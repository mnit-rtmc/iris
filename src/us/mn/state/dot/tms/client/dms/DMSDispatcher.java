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
package us.mn.state.dot.tms.client.dms;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignRequest;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.sonar.ProxySelectionListener;
import us.mn.state.dot.tms.client.sonar.ProxySelectionModel;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * The DMSDispatcher is a GUI component for creating and deploying DMS messages.
 * It uses a number of optional controls which appear or do not appear on screen
 * as a function of system attributes.
 * @see Font, FontComboBoxModel, SignMessage, DMSPanelPager
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSDispatcher extends JPanel implements ProxyListener<DMS>,
	ProxySelectionListener<DMS>
{
	/** SONAR namespace */
	protected final Namespace namespace;

	/** Cache of DMS proxy objects */
	protected final TypeCache<DMS> cache;

	/** Cache of font proxy objects */
	protected final TypeCache<Font> fonts;

	/** Selection model */
	protected final ProxySelectionModel<DMS> selectionModel;

	/** Single sign tab */
	protected final SingleSignTab singleTab = new SingleSignTab();

	/** Panel used for drawing a DMS */
	protected final SignPixelPanel dmsPanel;

	/** Message composer widget */
	protected final SignMessageComposer composer;

	/** Used to select the expires time for a message (optional) */
	protected final JComboBox durationCmb =
		new JComboBox(Expiration.values());

	/** Used to select the DMS font for a message (optional) */
	protected final JComboBox fontCmb = new JComboBox();

	/** Font combo box model */
	protected FontComboBoxModel fontModel;

	/** Button used to send a message to the DMS */
	protected final JButton sendBtn =
		new JButton(I18NMessages.get("dms.send"));

	/** Button used to clear the DMS.
	 * FIXME: should just use ClearDmsAction */
	protected final JButton clearBtn =
		new JButton(I18NMessages.get("dms.clear"));

	/** Button used to get the DMS status (optional) */
	protected final JButton queryStatusBtn = new JButton(I18NMessages.get(
		"dms.query_status"));

	/** AWS controlled checkbox (optional) */
	protected final JCheckBox awsControlledCbx = new JCheckBox(
		I18NMessages.get("dms.aws_controlled"));

	/** Currently logged in user */
	protected final User user;

	/** Pager for DMS panel */
	protected DMSPanelPager dmsPanelPager;

	/** Create a new DMS dispatcher */
	public DMSDispatcher(DMSManager manager, TmsConnection tc) {
		setLayout(new BorderLayout());
		SonarState st = tc.getSonarState();
		namespace = st.getNamespace();
		cache = st.getDMSs();
		fonts = st.getFonts();
		user = st.lookupUser(tc.getUser().getName());
		selectionModel = manager.getSelectionModel();
		composer = new SignMessageComposer(st.getDmsSignGroups(),
			st.getSignText(), user);
		dmsPanel = singleTab.getCurrentPanel();
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Single", singleTab);
		add(tab, BorderLayout.CENTER);
		add(createDeployBox(), BorderLayout.SOUTH);
		setSelected(null);
		cache.addProxyListener(this);
		selectionModel.addProxySelectionListener(this);
	}

	/** Create a component to deploy signs */
	protected Box createDeployBox() {
		Box boxRight = Box.createVerticalBox();
		boxRight.add(Box.createVerticalGlue());
		if(SystemAttributeHelper.isDmsDurationEnabled())
			boxRight.add(buildDurationBox());
		if(SystemAttributeHelper.isDmsFontSelectionEnabled())
			boxRight.add(buildFontSelectorBox());
		if(SystemAttributeHelper.isAwsEnabled())
			boxRight.add(buildAwsControlledBox());
		boxRight.add(Box.createVerticalStrut(4));
		boxRight.add(buildButtonPanel());
		boxRight.add(Box.createVerticalGlue());
		Box deployBox = Box.createHorizontalBox();
		deployBox.add(composer);
		deployBox.add(boxRight);
		return deployBox;
	}

	/** A new proxy has been added */
	public void proxyAdded(DMS proxy) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	public void proxyRemoved(DMS proxy) {
		// Note: the DMSManager will remove the proxy from the
		//       ProxySelectionModel, so we can ignore this.
	}

	/** A proxy has been changed */
	public void proxyChanged(final DMS proxy, final String a) {
		if(proxy == getSingleSelection()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateAttribute(proxy, a);
				}
			});
		}
	}

	/** Get the selected DMS (if a single sign is selected) */
	protected DMS getSingleSelection() {
		if(selectionModel.getSelectedCount() == 1) {
			for(DMS dms: selectionModel.getSelected())
				return dms;
		}
		return null;
	}

	/** Dispose of the dispatcher */
	public void dispose() {
		selectionModel.removeProxySelectionListener(this);
		cache.removeProxyListener(this);
		setSelected(null);
		clearPager();
		composer.dispose();
		removeAll();
	}

	/** Clear the DMS panel pager */
	protected void clearPager() {
		DMSPanelPager pager = dmsPanelPager;
		if(pager != null) {
			pager.dispose();
			dmsPanelPager = null;
		}
	}

	/** Build the optional message duration box */
	protected JPanel buildDurationBox() {
		JPanel p = new JPanel(new FlowLayout());
		p.add(new JLabel("Duration"));
		p.add(durationCmb);
		durationCmb.setSelectedIndex(0);
		return p;
	}

	/** Build the font selector combo box */
	protected JPanel buildFontSelectorBox() {
		JPanel p = new JPanel(new FlowLayout());
		p.add(new JLabel("Font"));
		p.add(fontCmb);
		return p;
	}

	/** Build the AWS controlled box */
	protected JPanel buildAwsControlledBox() {
		new ActionJob(awsControlledCbx) {
			public void perform() {
				DMS dms = getSingleSelection();
				if(dms != null) {
					dms.setAwsControlled(
						awsControlledCbx.isSelected());
				}
			}
		};
		JPanel p = new JPanel(new FlowLayout());
		p.add(awsControlledCbx);
		return p;
	}

	/** Build the button panel */
	protected Box buildButtonPanel() {
		new ActionJob(sendBtn) {
			public void perform() {
				sendMessage();
			}
		};
		sendBtn.setToolTipText(I18NMessages.get("dms.send.tooltip"));
		new ActionJob(this, queryStatusBtn) {
			public void perform() {
				queryStatus();
			}
		};
		queryStatusBtn.setToolTipText(I18NMessages.get(
			"dms.query_status.tooltip"));
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(sendBtn);
		box.add(Box.createHorizontalGlue());
		box.add(clearBtn);
		box.add(Box.createHorizontalGlue());
		if(SystemAttributeHelper.isDmsStatusEnabled()) {
			box.add(queryStatusBtn);
			box.add(Box.createHorizontalGlue());
		}
		return box;
	}

	/** Query the status for the selected sign */
	protected void queryStatus() {
		DMS dms = getSingleSelection();
		if(dms != null)
			dms.setSignRequest(SignRequest.QUERY_STATUS.ordinal());
	}

	/** Called whenever a sign is added to the selection */
	public void selectionAdded(DMS s) {
		setSelected(getSingleSelection());
	}

	/** Called whenever a sign is removed from the selection */
	public void selectionRemoved(DMS s) {
		setSelected(getSingleSelection());
	}

	/** Set a single selected DMS */
	protected void setSelected(DMS dms) {
		if(dms == null)
			clearSelected();
		else {
			sendBtn.setEnabled(true);
			clearBtn.setEnabled(true);
			clearBtn.setAction(new ClearDmsAction(dms,
				user.getName()));
			queryStatusBtn.setEnabled(true);
			durationCmb.setEnabled(true);
			durationCmb.setSelectedIndex(0);
			PixelMapBuilder builder = createPixelMapBuilder(dms);
			FontComboBoxModel m = new FontComboBoxModel(fonts,
				builder);
			fontCmb.setModel(m);
			if(fontModel != null)
				fontModel.dispose();
			fontModel = m;
			fontCmb.setEnabled(true);
			fontCmb.setSelectedIndex(0);
			awsControlledCbx.setEnabled(true);
			clearPager();
			BitmapGraphic[] bmaps = getBitmaps(builder);
			dmsPanelPager = new DMSPanelPager(dmsPanel, dms, bmaps);
			composer.setSign(dms, builder.getLineHeightPixels());
			updateAttribute(dms, null);
		}
	}

	/** Create the pixel map builder */
	protected PixelMapBuilder createPixelMapBuilder(DMS dms) {
		Integer wp = dms.getWidthPixels();
		Integer hp = dms.getHeightPixels();
		Integer cw = dms.getCharWidthPixels();
		Integer ch = dms.getCharHeightPixels();
		SignMessage m = dms.getMessageCurrent();
		if(wp != null && hp != null && cw != null && ch != null &&
		   m != null)
		{
			PixelMapBuilder builder = new PixelMapBuilder(namespace,
				wp, hp, cw, ch);
			MultiString multi = new MultiString(m.getMulti());
			multi.parse(builder);
			return builder;
		} else
			return null;
	}

	/** Get the bitmap graphic for all pages */
	protected BitmapGraphic[] getBitmaps(PixelMapBuilder builder) {
		if(builder != null)
			return builder.getPixmaps();
		else
			return null;
	}

	/** Clear the selected DMS */
	protected void clearSelected() {
		singleTab.clearSelected();
		durationCmb.setEnabled(false);
		durationCmb.setSelectedItem(null);
		fontCmb.setEnabled(false);
		fontCmb.setSelectedItem(null);
		if(fontModel != null) {
			fontModel.dispose();
			fontModel = null;
		}
		awsControlledCbx.setEnabled(false);
		sendBtn.setEnabled(false);
		clearBtn.setEnabled(false);
		queryStatusBtn.setEnabled(false);
		clearPager();
		composer.setEnabled(false);
		composer.clearSelections();
	}

	/** Get the selected duration */
	protected Integer getDuration() {
		if(SystemAttributeHelper.isDmsDurationEnabled()) {
			Expiration e =(Expiration)durationCmb.getSelectedItem();
			if(e != null)
				return e.duration;
		}
		return null;
	}

	/** Send a new message to the selected DMS */
	protected void sendMessage() {
		DMS dms = getSingleSelection();
		if(dms != null)
			sendMessage(dms);
	}

	/** Send a new message to the specified DMS */
	protected void sendMessage(DMS dms) {
		assert dms != null;
		Font font = (Font)fontCmb.getSelectedItem();
		if(font != null) {
			String multi = composer.getMessage(font.getNumber());
			if(multi != null) {
				// FIXME: this is asynchronous
				SignMessage message = creator.create(user,
					 multi, getDuration());
				dms.setMessageNext(message);
				composer.updateMessageLibrary();
			}
		}
	}

	/** Update one attribute on the form */
	protected void updateAttribute(DMS dms, String a) {
		singleTab.updateAttribute(dms, a);
		if(a == null || a.equals("messageCurrent")) {
			clearPager();
			PixelMapBuilder builder = createPixelMapBuilder(dms);
			BitmapGraphic[] bmaps = getBitmaps(builder);
			dmsPanelPager = new DMSPanelPager(dmsPanel, dms, bmaps);
			composer.setMessage(dms);
		}
	}
}
