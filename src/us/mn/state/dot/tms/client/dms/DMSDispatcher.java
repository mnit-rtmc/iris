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
import java.awt.CardLayout;
import java.io.IOException;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.widget.IButton;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.dms.quicklib.QLibCBox;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The DMSDispatcher is a GUI component for creating and deploying DMS messages.
 * It uses a number of optional controls which appear or do not appear on screen
 * as a function of system attributes.
 * @see SignMessage, DMSPanelPager
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

	/** Selection model */
	protected final ProxySelectionModel<DMS> selectionModel;

	/** Selection tab pane */
	protected final JTabbedPane tabPane = new JTabbedPane();

	/** Single sign tab */
	protected final SingleSignTab singleTab = new SingleSignTab(this);

	/** Multiple sign tab */
	protected final MultipleSignTab multipleTab;

	/** Panel used for drawing a DMS */
	protected final SignPixelPanel currentPnl;

	/** Panel used for drawing a preview DMS */
	protected final SignPixelPanel previewPnl;

	/** Message composer widget */
	protected final SignMessageComposer composer;

	/** Used to select the expires time for a message (optional) */
	protected final JComboBox durationCmb =
		new JComboBox(Expiration.values());

	/** Combobox used to select a quick library message (optional). */
	protected final QLibCBox qlibCmb;

	/** Button used to send a message to the DMS */
	protected final IButton sendBtn = new IButton("dms.send");

	/** Button used to clear the DMS */
	protected final JButton clearBtn = new IButton("dms.clear");

	/** Action to clear selected DMS */
	protected final ClearDmsAction clearAction;

	/** Card layout for alert panel */
	protected final CardLayout cards = new CardLayout();

	/** Card panel for alert panels */
	protected final JPanel card_panel = new JPanel(cards);

	/** AMBER Alert checkbox */
	protected final JCheckBox alertCbx =
		new JCheckBox(I18N.get("dms.alert"));

	/** Currently logged in user */
	protected final User user;

	/** Sign message creator */
	protected final SignMessageCreator creator;

	/** Pager for current DMS panel */
	protected DMSPanelPager currentPnlPager;

	/** Pager for preview DMS panel */
	protected DMSPanelPager previewPnlPager;

	/** Pixel map builder */
	protected PixelMapBuilder builder;

	/** Currently watching DMS */
	protected DMS watching;

	/** Create a new DMS dispatcher */
	public DMSDispatcher(Session session, DMSManager manager) {
		super(new BorderLayout());
		SonarState st = session.getSonarState();
		DmsCache dms_cache = st.getDmsCache();
		namespace = st.getNamespace();
		cache = dms_cache.getDMSs();
		user = session.getUser();
		creator = new SignMessageCreator(st, user);
		selectionModel = manager.getSelectionModel();
		clearAction = new ClearDmsAction(selectionModel, this, user);
		qlibCmb = new QLibCBox(this, dms_cache.getQuickMessages());
		clearBtn.setAction(clearAction);
		manager.setClearAction(clearAction);
		composer = new SignMessageComposer(this, st, user);
		currentPnl = singleTab.getCurrentPanel();
		previewPnl = singleTab.getPreviewPanel();
		multipleTab = new MultipleSignTab(dms_cache, selectionModel);
		tabPane.addTab("Single", singleTab);
		tabPane.addTab("Multiple", multipleTab);
		add(tabPane, BorderLayout.CENTER);
		add(createDeployBox(), BorderLayout.SOUTH);
		clearSelected();
		cache.addProxyListener(this);
		selectionModel.addProxySelectionListener(this);
	}

	/** Create a component to deploy signs */
	protected Box createDeployBox() {
		durationCmb.setSelectedIndex(0);
		FormPanel panel = new FormPanel(true);
		if(SystemAttrEnum.DMS_DURATION_ENABLE.getBoolean())
			panel.addRow("Duration", durationCmb);
		panel.addRow(card_panel);
		card_panel.add(new JLabel(), "Blank");
		card_panel.add(alertCbx, "Alert");
		panel.setCenter();
		if(QLibCBox.getIEnabled())
			panel.addRow(buildQuickLibPanel());
		panel.addRow(buildButtonPanel());
		Box deployBox = Box.createHorizontalBox();
		deployBox.add(composer);
		deployBox.add(panel);
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
		multipleTab.dispose();
		selectionModel.removeProxySelectionListener(this);
		cache.removeProxyListener(this);
		if(watching != null) {
			cache.ignoreObject(watching);
			watching = null;
		}
		clearSelected();
		clearCurrentPager();
		clearPreviewPager();
		composer.dispose();
		qlibCmb.dispose();
		removeAll();
	}

	/** Clear the current DMS panel pager */
	protected void clearCurrentPager() {
		DMSPanelPager pager = currentPnlPager;
		if(pager != null) {
			pager.dispose();
			currentPnlPager = null;
		}
	}

	/** Clear the preview DMS panel pager */
	protected void clearPreviewPager() {
		DMSPanelPager pager = previewPnlPager;
		if(pager != null) {
			pager.dispose();
			previewPnlPager = null;
		}
	}

	/** Build the quick lib panel */
	protected Box buildQuickLibPanel() {
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		JLabel label = new JLabel();
		label.setLabelFor(qlibCmb);
		label.setDisplayedMnemonic('Q');
		label.setText("<html><p align=\"right\"><u>Q</u>uick" +
			"<br>Message</p></html>");
		box.add(label);
		box.add(box.createHorizontalStrut(4));
		box.add(qlibCmb);
		return box;
	}

	/** Build the button panel */
	protected Box buildButtonPanel() {
		new ActionJob(sendBtn) {
			public void perform() {
				sendMessage();
			}
		};
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(sendBtn);
		box.add(Box.createHorizontalStrut(4));
		box.add(clearBtn);
		box.add(Box.createHorizontalGlue());
		return box;
	}

	/** Called whenever a sign is added to the selection */
	public void selectionAdded(DMS s) {
		updateSelected();
	}

	/** Called whenever a sign is removed from the selection */
	public void selectionRemoved(DMS s) {
		updateSelected();
	}

	/** Update the selected sign(s) */
	protected void updateSelected() {
		List<DMS> selected = selectionModel.getSelected();
		if(selected.size() == 0)
			clearSelected();
		else if(selected.size() == 1) {
			for(DMS dms: selected)
				setSelected(dms);
		} else {
			singleTab.clearSelected();
			enableWidgets();
			selectMultipleTab();
		}
	}

	/** Clear the selection */
	protected void clearSelected() {
		disableWidgets();
		singleTab.clearSelected();
		selectSingleTab();
	}

	/** Set a single selected DMS */
	protected void setSelected(DMS dms) {
		if(watching != null)
			cache.ignoreObject(watching);
		watching = dms;
		cache.watchObject(watching);
		if(DMSHelper.isActive(dms)) {
			builder = createPixelMapBuilder(dms);
			updateAttribute(dms, null);
			enableWidgets();
		} else {
			disableWidgets();
			singleTab.updateAttribute(dms, null);
		}
		selectSingleTab();
	}

	/** Select the single selection tab */
	protected void selectSingleTab() {
		if(tabPane.getSelectedComponent() != singleTab) {
			alertCbx.setSelected(false);
			tabPane.setSelectedComponent(singleTab);
		}
		cards.show(card_panel, "Blank");
	}

	/** Select the multiple selection tab */
	protected void selectMultipleTab() {
		if(tabPane.getSelectedComponent() != multipleTab)
			tabPane.setSelectedComponent(multipleTab);
		cards.show(card_panel, "Alert");
		qlibCmb.setSelectedItem("");
	}

	/** Disable the dispatcher widgets */
	protected void disableWidgets() {
		clearCurrentPager();
		clearPreviewPager();
		currentPnl.clear();
		previewPnl.clear();
		composer.setEnabled(false);
		composer.clearSelections();
		durationCmb.setEnabled(false);
		durationCmb.setSelectedItem(null);
		sendBtn.setEnabled(false);
		clearBtn.setEnabled(false);
		qlibCmb.setEnabled(false);
		qlibCmb.setSelectedItem("");
		builder = null;
	}

	/** Enable the dispatcher widgets */
	protected void enableWidgets() {
		composer.setEnabled(true);
		durationCmb.setEnabled(true);
		durationCmb.setSelectedIndex(0);
		sendBtn.setEnabled(true);
		clearBtn.setEnabled(true);
		qlibCmb.setEnabled(true);
		updateTextQLibCBox(true);
		selectPreview(false);
	}

	/** True to indicate updateWidgetsUsingQuickLib() is updating 
	 *  the widgets, which is used by SignMessageComposer. */
	protected boolean m_updating_widgets = false;

	/** Update widgets based on current quick library message. This
	 *  method is called by QLibCBox when its state changes. */
	public void updateWidgetsUsingQuickLib() {
		if(!QLibCBox.getIEnabled())
			return;

		// get qlib multi
		String qlib_multi = "";
		{
			QuickMessage qlib_qm = qlibCmb.getSelectedProxy();
			if(qlib_qm == null)
				return;
			qlib_multi = new MultiString(qlib_qm.getMulti()).
				normalize();
			if(qlib_multi.isEmpty())
				return;
		}

		boolean set = true;

		// compare MULTI strings
		SignMessage widg_sm = createMessage();
		if(widg_sm != null) {
			String widg_multi = new MultiString(
				widg_sm.getMulti()).normalize();
			set = !widg_multi.equals(qlib_multi);
		}

		// set sign message if MULTIs are different or no widget multi
		if(set) {
			SignMessage sm = createMessage(qlib_multi);
			m_updating_widgets = true; // see SignMessageComposer
			setMessage(sm);
			m_updating_widgets = false;
		}
	}

	/** Set the composer's current sign message. Called by quick 
	 *  library cbox when the user changes the quick message. */
	public void setMessage(SignMessage m) {
		composer.setMessage(m, getLineCount());
	}

	/** Update the quick library cbox using the current sign message 
	 *  on either the DMS or from the widgets.
	 *  @param useDms True to use the DMS as the message source else false
	 *		  to use the sign message widgets as the source. */
	public void updateTextQLibCBox(final boolean useDms) {
		if(!QLibCBox.getIEnabled())
			return;

		// get sign message from dms
		final SignMessage sm_dms = (watching == null ? null : 
			watching.getMessageCurrent());

		// this is invoked later because the model probably hasn't 
		// been updated with the free-form text in the editor's 
		// edit field.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				handleUpdateTextQLibCBox(useDms, sm_dms);
			}
		});
	}

	/** handle update to quick lib combobox */
	protected void handleUpdateTextQLibCBox(boolean useDms, 
		SignMessage sm_dms) 
	{
		if(useDms) {
			if(qlibCmb != null)
				qlibCmb.setSelectedItem(sm_dms);
		} else {
			final SignMessage sm = createMessage();
			if(sm != null && qlibCmb != null)
				qlibCmb.setSelectedItem(sm);
		}
	}

	/** Create the pixel map builder */
	protected PixelMapBuilder createPixelMapBuilder(DMS dms) {
		Integer wp = dms.getWidthPixels();
		Integer hp = dms.getHeightPixels();
		Integer cw = dms.getCharWidthPixels();
		Integer ch = dms.getCharHeightPixels();
		if(wp != null && hp != null && cw != null && ch != null)
			return new PixelMapBuilder(namespace, wp, hp, cw, ch);
		else
			return null;
	}

	/** Get the bitmap graphic for all pages */
	protected BitmapGraphic[] getBitmaps(DMS dms) {
		if(dms == null)
			return null;
		SignMessage m = dms.getMessageCurrent();
		if(m == null)
			return null;
		byte[] bmaps = decodeBitmaps(m.getBitmaps());
		if(bmaps == null)
			return null;
		BitmapGraphic bg = createBitmapGraphic(dms);
		if(bg == null)
			return null;
		int blen = bg.length();
		if(blen == 0 || bmaps.length % blen != 0)
			return null;
		int n_pages = bmaps.length / blen;
		BitmapGraphic[] bitmaps = new BitmapGraphic[n_pages];
		for(int i = 0; i < n_pages; i++) {
			bitmaps[i] = createBitmapGraphic(dms);
			byte[] b = new byte[blen];
			System.arraycopy(bmaps, i * blen, b, 0, blen);
			bitmaps[i].setPixels(b);
		}
		return bitmaps;
	}

	/** Decode the bitmaps */
	protected byte[] decodeBitmaps(String bitmaps) {
		try {
			return Base64.decode(bitmaps);
		}
		catch(IOException e) {
			return null;
		}
	}

	/** Create a bitmap graphic */
	protected BitmapGraphic createBitmapGraphic(DMS dms) {
		Integer wp = dms.getWidthPixels();
		Integer hp = dms.getHeightPixels();
		if(wp != null && hp != null)
			return new BitmapGraphic(wp, hp);
		else
			return null;
	}

	/** Send a new message to the selected DMS */
	protected void sendMessage() {
		List<DMS> sel = selectionModel.getSelected();
		if(sel.size() > 0) {
			SignMessage m = createMessage();
			if(m != null)
				sendMessage(m, sel);
			composer.updateMessageLibrary();
			selectPreview(false);
		}
	}

	/** Send a message to a list of signs */
	protected void sendMessage(SignMessage m, List<DMS> sel) {
		for(DMS dms: sel) {
			if(checkDimensions(dms)) {
				dms.setOwnerNext(user);
				dms.setMessageNext(m);
			} else {
				// NOTE: this sign does not match the proper
				//       dimensions, so deselect it.
				selectionModel.removeSelected(dms);
			}
		}
	}

	/** Create a new message from the widgets */
	protected SignMessage createMessage() {
		if(composer == null)
			return null;
		return createMessage(composer.getMessage());
	}

	/** Create a new message using the specified MULTI */
	protected SignMessage createMessage(String multi) {
		if(creator == null || multi == null)
			return null;
		String bitmaps = createBitmaps(multi);
		if(bitmaps != null)
			return creator.create(multi, bitmaps,
			       getPriority(), getDuration());
		return null;
	}

	/** Create a new blank message */
	protected SignMessage createBlankMessage() {
		String multi = "";
		String bitmaps = createBitmaps(multi);
		if(bitmaps != null) {
			return creator.create(multi, bitmaps,
				DMSMessagePriority.CLEAR, 0);
		} else
			return null;
	}

	/** Create bitmap graphics for a MULTI string */
	protected String createBitmaps(String multi) {
		PixelMapBuilder b = builder;
		if(b != null) {
			b.clear();
			MultiString m = new MultiString(multi);
			m.parse(b, b.getDefaultFontNumber());
			return encodeBitmaps(b.getPixmaps());
		} else
			return null;
	}

	/** Encode the bitmaps to Base64 */
	protected String encodeBitmaps(BitmapGraphic[] bmaps) {
		int blen = bmaps[0].length();
		byte[] bitmaps = new byte[bmaps.length * blen];
		for(int i = 0; i < bmaps.length; i++) {
			byte[] pix = bmaps[i].getPixels();
			System.arraycopy(pix, 0, bitmaps, i * blen, blen);
		}
		return Base64.encode(bitmaps);
	}

	/** Get the selected priority */
	protected DMSMessagePriority getPriority() {
		if(alertCbx.isSelected())
		       return DMSMessagePriority.ALERT;
		else
		       return DMSMessagePriority.OPERATOR;
	}

	/** Get the selected duration */
	protected Integer getDuration() {
		Expiration e = (Expiration)durationCmb.getSelectedItem();
		if(e != null)
			return e.duration;
		else
			return null;
	}

	/** Check the dimensions of a sign against the pixel map builder */
	protected boolean checkDimensions(DMS dms) {
		PixelMapBuilder b = builder;
		if(b != null) {
			Integer w = dms.getWidthPixels();
			Integer h = dms.getHeightPixels();
			if(w != null && h != null)
				return b.width == w && b.height == h;
		}
		return false;
	}

	/** Update one attribute on the form */
	protected void updateAttribute(DMS dms, String a) {
		singleTab.updateAttribute(dms, a);
		if(a == null || a.equals("messageCurrent")) {
			clearCurrentPager();
			BitmapGraphic[] bmaps = getBitmaps(dms);
			currentPnlPager = new DMSPanelPager(currentPnl, dms,
				bmaps);
			if(a == null) {
				composer.setSign(dms, getLineCount(dms),
					builder);
			}
			composer.setMessage();
		}
	}

	/** Get the number of lines on the current sign */
	public int getLineCount() {
		if(watching == null)
			return 0;
		return getLineCount(watching);
	}

	/** Get the number of lines on a sign */
	public int getLineCount(DMS dms) {
		int ml = SystemAttrEnum.DMS_MAX_LINES.getInt();
		int lh = getLineHeightPixels();
		Integer h = dms.getHeightPixels();
		if(h != null && h > 0 && lh >= h) {
			int nl = h / lh;
			return Math.min(nl, ml);
		} else
			return ml;
	}

	/** Get the line height */
	protected int getLineHeightPixels() {
		PixelMapBuilder b = builder;
		if(b != null)
			return b.getLineHeightPixels();
		else
			return 7;
	}

	/** Select the preview mode */
	public void selectPreview(boolean p) {
		if(p)
			updatePreviewPanel();
		composer.selectPreview(p);
		singleTab.selectPreview(p);
	}

	/** Update the preview panel */
	protected void updatePreviewPanel() {
		clearPreviewPager();
		DMS dms = getSingleSelection();
		if(dms != null) {
			String multi = composer.getMessage();
			BitmapGraphic[] bmaps = getBitmaps(multi);
			previewPnlPager = new DMSPanelPager(previewPnl, dms,
				bmaps);
		}
	}

	/** Get the bitmap graphic for the given message */
	protected BitmapGraphic[] getBitmaps(String m) {
		PixelMapBuilder b = builder;
		if(b != null) {
			b.clear();
			MultiString multi = new MultiString();
			if(m != null)
				multi.addText(m);
			multi.parse(b, b.getDefaultFontNumber());
			return b.getPixmaps();
		} else
			return null;
	}

	/** Check if AWS is allowed and user has permission to change */
	public boolean isAwsPermitted(DMS dms) {
		Name name = new Name(dms, "awsControlled");
		return dms.getAwsAllowed() && namespace.canUpdate(user, name);
	}
}
