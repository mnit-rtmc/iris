/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import java.awt.Dimension;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.IButton;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A SingleSignTab is a GUI component for displaying the status of a single
 * selected DMS within the DMS dispatcher.  One instance of this class is
 * created on client startup by DMSDispatcher. 
 * @see DMSDispatcher.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SingleSignTab extends FormPanel implements ProxyListener<DMS> {

	/** Empty text field */
	static protected final String EMPTY_TXT = "    ";

	/** Get the controller status */
	static protected String getControllerStatus(DMS proxy) {
		Controller c = proxy.getController();
		if(c == null)
			return "???";
		else
			return c.getStatus();
	}

	/** Formatter for displaying the hour and minute */
	static protected final SimpleDateFormat HOUR_MINUTE =
		new SimpleDateFormat("HH:mm");

	/** Milliseconds per day */
	static protected final long MS_PER_DAY = 24 * 60 * 60 * (long)1000;

	/** Format the message deployed time */
	static protected String formatDeploy(DMS dms) {
		long deploy = dms.getDeployTime();
		if(System.currentTimeMillis() < deploy + MS_PER_DAY)
			return HOUR_MINUTE.format(deploy);
		else
			return "";
	}

	/** Format the message expriation */
	static protected String formatExpires(DMS dms) {
		SignMessage m = dms.getMessageCurrent();
		Integer duration = m.getDuration();
		if(duration == null)
			return EMPTY_TXT; 
		if(duration <= 0 || duration >= 65535)
			return EMPTY_TXT;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(dms.getDeployTime());
		c.add(Calendar.MINUTE, duration);
		return HOUR_MINUTE.format(c.getTime());
	}

	/** Displays the id of the DMS */
	protected final JTextField nameTxt = createTextField();

	/** Displays the brightness of the DMS */
	protected final JTextField brightnessTxt = createTextField();

	/** Displays the verify camera for the DMS */
	protected final JTextField cameraTxt = createTextField();

	/** Displays the location of the DMS */
	protected final JTextField locationTxt = createTextField();

	/** AWS controlled checkbox (optional) */
	protected final JCheckBox awsControlledCbx = new JCheckBox();

	/** Displays the current operation of the DMS */
	protected final JTextField operationTxt = createTextField();

	/** Displays the controller status (optional) */
	protected final JTextField statusTxt = createTextField();

	/** Displays the controller operation status (optional) */
	protected final JTextField opStatusTxt = createTextField();

	/** Displays the current message deploy time */
	protected final JTextField deployTxt = createTextField();

	/** Displays the current message expiration */
	protected final JTextField expiresTxt = createTextField();

	/** DMS dispatcher */
	protected final DMSDispatcher dispatcher;

	/** Panel for drawing current pixel status */
	protected final SignPixelPanel currentPnl = new SignPixelPanel(true);

	/** Panel for drawing preview pixel status */
	protected final SignPixelPanel previewPnl = new SignPixelPanel(true,
		new Color(0, 0, 0.4f));

	/** Pager for selected DMS panel */
	protected DMSPanelPager pnlPager;

	/** Tabbed pane for current/preview panels */
	protected final JTabbedPane tab = new JTabbedPane();

	/** Cache of DMS proxy objects */
	protected final TypeCache<DMS> cache;

	/** Currently selected DMS.  This will be null if there are zero or
	 * multiple DMS selected. */
	protected DMS watching;

	/** Preview mode */
	protected boolean preview;

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	protected int adjusting = 0;

	/** Create a new single sign tab. */
	public SingleSignTab(DMSDispatcher d, TypeCache<DMS> tc) {
		super(true);
		dispatcher = d;
		cache = tc;
		cache.addProxyListener(this);
		currentPnl.setPreferredSize(new Dimension(390, 108));
		previewPnl.setPreferredSize(new Dimension(390, 108));
		add("Name", nameTxt);
		if(SystemAttrEnum.DMS_BRIGHTNESS_ENABLE.getBoolean()) {
			add("Brightness", brightnessTxt);
			addRow("Camera", cameraTxt);
		} else
			addRow("Camera", cameraTxt);
		addRow("Location", locationTxt);
		addRow(I18N.get("SingleSignTab.OperationTitle"), operationTxt);
		statusTxt.setColumns(10);
		if(SystemAttrEnum.DMS_QUERYMSG_ENABLE.getBoolean())
			addRow(I18N.get("SingleSignTab.ControllerStatus"), statusTxt);
		opStatusTxt.setColumns(10);
		if(SystemAttrEnum.DMS_OP_STATUS_ENABLE.getBoolean())
			addRow("Operation Status", opStatusTxt);
		add("Deployed", deployTxt);
		if(SystemAttrEnum.DMS_DURATION_ENABLE.getBoolean())
			add("Expires", expiresTxt);
		if(SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			setWest();
			final String mid = "dms.aws.controlled";
			awsControlledCbx.setText(I18N.get(mid));
			awsControlledCbx.setHorizontalTextPosition(
				SwingConstants.LEFT);
			awsControlledCbx.setToolTipText(
				I18N.get(mid + ".tooltip"));
			addRow(awsControlledCbx);
		} else
			finishRow();
		tab.add("Current", currentPnl);
		tab.add("Preview", previewPnl);
		addRow(tab);
		tab.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				selectPreview(!preview);
				if((adjusting == 0) && !preview) {
					DMS dms = watching;
					if(dms != null)
						updateMessageCurrent(dms);
				}
			}
		});
		new ActionJob(awsControlledCbx) {
			public void perform() {
				DMS proxy = watching;
				if(proxy != null) {
					proxy.setAwsControlled(
						awsControlledCbx.isSelected());
				}
			}
		};
	}

	/** Dispose of the sign tab */
	public void dispose() {
		setSelected(null);
		cache.removeProxyListener(this);
		clearPager();
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
		if(proxy == watching) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateAttribute(proxy, a);
				}
			});
		}
	}

	/** Select the preview (or current) tab */
	public void selectPreview(boolean p) {
		if(adjusting == 0) {
			preview = p;
			adjusting++;
			setMessage();
			adjusting--;
		}
	}

	/** Set the displayed message */
	public void setMessage() {
		if(preview) {
			updatePreviewPanel(watching);
			tab.setSelectedComponent(previewPnl);
		} else {
			updateCurrentPanel(watching);
			tab.setSelectedComponent(currentPnl);
		}
	}

	/** Set a single selected DMS */
	public void setSelected(DMS dms) {
		if(watching != null)
			cache.ignoreObject(watching);
		if(dms != null)
			cache.watchObject(dms);
		watching = dms;
		if(dms != null)
			updateAttribute(dms, null);
		else
			clearSelected();
	}

	/** Clear the selected DMS */
	protected void clearSelected() {
		clearPager();
		currentPnl.clear();
		previewPnl.clear();
		nameTxt.setText(EMPTY_TXT);
		brightnessTxt.setText(EMPTY_TXT);
		cameraTxt.setText(EMPTY_TXT);
		locationTxt.setText("");
		awsControlledCbx.setSelected(false);
		awsControlledCbx.setEnabled(false);
		operationTxt.setText("");
		statusTxt.setText("");
		opStatusTxt.setText("");
		deployTxt.setText("");
		expiresTxt.setText(EMPTY_TXT);
	}

	/** Update one (or all) attribute(s) on the form.
	 * @param dms The newly selected DMS. May not be null.
	 * @param a Attribute to update, null for all attributes. */
	protected void updateAttribute(DMS dms, String a) {
		if(a == null || a.equals("name"))
			nameTxt.setText(dms.getName());
		if(a == null || a.equals("lightOutput")) {
			Integer o = dms.getLightOutput();
			if(o != null)
				brightnessTxt.setText("" + o + "%");
			else
				brightnessTxt.setText("");
		}
		if(a == null || a.equals("camera"))
			cameraTxt.setText(DMSHelper.getCameraName(dms));
		// FIXME: this won't update when geoLoc attributes change
		if(a == null || a.equals("geoLoc")) {
			locationTxt.setText(GeoLocHelper.getDescription(
				dms.getGeoLoc()));
		}
		if(a == null || a.equals("operation")) {
			String status = getControllerStatus(dms);
			boolean cok = status.isEmpty();
			if(cok) {
				operationTxt.setForeground(null);
				operationTxt.setBackground(null);
			} else {
				operationTxt.setForeground(Color.WHITE);
				operationTxt.setBackground(Color.GRAY);
			}
			operationTxt.setText(dms.getOperation());
			statusTxt.setText(status);
			opStatusTxt.setText(dms.getOpStatus());
		}
		if(a == null || a.equals("messageCurrent")) {
			deployTxt.setText(formatDeploy(dms));
			expiresTxt.setText(formatExpires(dms));
			if(!preview) {
				updateCurrentPanel(dms);
				updateMessageCurrent(dms);
			}
		}
		if(a == null || a.equals("awsAllowed")) {
			awsControlledCbx.setEnabled(
				dispatcher.isAwsPermitted(dms));
		}
		if(a == null || a.equals("awsControlled"))
			awsControlledCbx.setSelected(dms.getAwsControlled());
	}

	/** Update the current panel */
	protected void updateCurrentPanel(DMS dms) {
		clearPager();
		if(dms != null) {
			BitmapGraphic[] bmaps = getBitmaps(dms);
			if(bmaps != null) {
				pnlPager = new DMSPanelPager(currentPnl, dms,
					bmaps, getPgOnTime(dms));
			}
		}
	}

	/** Update the current message */
	protected void updateMessageCurrent(DMS dms) {
		adjusting++;
		dispatcher.setMessage(getMultiString(dms));
		adjusting--;
	}

	/** Get the current bitmap graphic for all pages of the specified DMS.
	 * @param DMS with the graphic.
	 * @return Array of bitmaps, one for each page, or null on error. */
	protected BitmapGraphic[] getBitmaps(DMS dms) {
		SignMessage sm = dms.getMessageCurrent();
		if(sm == null)
			return null;
		byte[] bmaps = decodeBitmaps(sm.getBitmaps());
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

	/** Get the page on-time for the current message on the 
	 * specified DMS. */
	protected DmsPgTime getPgOnTime(DMS dms) {
		return getPgOnTime(getMultiString(dms));
	}

	/** Get the page-on time for the specified multi string */
	protected DmsPgTime getPgOnTime(String ms) {
		if(ms.isEmpty())
			return DmsPgTime.getDefaultOn(true);
		else
			return new MultiString(ms).getPageOnTime();
	}

	/** Get the MULTI string currently on the specified dms.
	 * @param dms DMS to lookup, may not be null. */
	protected String getMultiString(DMS dms) {
		SignMessage sm = dms.getMessageCurrent();
		if(sm != null)
			return sm.getMulti();
		else
			return "";
	}

	/** Update the preview panel */
	protected void updatePreviewPanel(DMS dms) {
		clearPager();
		if(dms != null) {
			String ms = dispatcher.getMessage();
			BitmapGraphic[] bmaps = dispatcher.getBitmaps();
			pnlPager = new DMSPanelPager(previewPnl, dms, bmaps,
				getPgOnTime(ms));
		}
	}

	/** Clear the DMS panel pager */
	protected void clearPager() {
		DMSPanelPager pager = pnlPager;
		if(pager != null) {
			pager.dispose();
			pnlPager = null;
		}
	}
}
