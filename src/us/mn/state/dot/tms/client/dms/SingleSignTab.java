/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
 * Copyright (C) 2010 AHMCT, University of California, Davis
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
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
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraSelectAction;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
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

	/** Displays the id of the DMS */
	protected final JTextField nameTxt = createTextField();

	/** Displays the brightness of the DMS */
	protected final JTextField brightnessTxt = createTextField();

	/** Displays the verify camera for the DMS */
	protected final JButton cameraBtn = new JButton();

	/** Displays the location of the DMS */
	protected final JTextField locationTxt = createTextField();

	/** AWS controlled checkbox (optional) */
	protected final JCheckBox awsControlledCbx = new JCheckBox();

	/** Displays the controller status */
	protected final JTextField statusTxt = createTextField();

	/** Displays the current operation of the DMS */
	protected final JTextField operationTxt = createTextField();

	/** Displays the controller operation status (optional) */
	protected final JTextField opStatusTxt = createTextField();

	/** Client session */
	protected final Session session;

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

	/** Mouse listener for popup menus */
	protected final MouseListener popper = new MouseAdapter() {
		public void mousePressed(MouseEvent me) {
			doPopup(me);
		}
		// NOTE: needed for cross-platform functionality
		public void mouseReleased(MouseEvent me) {
			doPopup(me);
		}
	};

	/** Cache of DMS proxy objects */
	protected final TypeCache<DMS> cache;

	/** Camera selection model */
	protected final ProxySelectionModel<Camera> cam_sel_model;

	/** Currently selected DMS.  This will be null if there are zero or
	 * multiple DMS selected. */
	protected DMS watching;

	/** Preview mode */
	protected boolean preview;

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	protected int adjusting = 0;

	/** Create a new single sign tab */
	public SingleSignTab(Session s, DMSDispatcher d) {
		super(true);
		session = s;
		dispatcher = d;
		cache = s.getSonarState().getDmsCache().getDMSs();
		cache.addProxyListener(this);
		cam_sel_model = s.getCameraManager().getSelectionModel();
		nameTxt.setMinimumSize(new Dimension(36, 20));
		add("Name", nameTxt);
		if(SystemAttrEnum.DMS_BRIGHTNESS_ENABLE.getBoolean())
			add("Brightness", brightnessTxt);
		cameraBtn.setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.LOWERED));
		addRow("Camera", cameraBtn);
		locationTxt.setMinimumSize(new Dimension(260, 20));
		addRow("Location", locationTxt);
		addRow(I18N.get("SingleSignTab.ControllerStatus"), statusTxt);
		addRow(I18N.get("SingleSignTab.OperationTitle"), operationTxt);
		if(SystemAttrEnum.DMS_OP_STATUS_ENABLE.getBoolean()) {
			opStatusTxt.setColumns(10);
			addRow("Operation Status", opStatusTxt);
		}
		if(SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			setWest();
			final String mid = "dms.aws.controlled";
			awsControlledCbx.setText(I18N.get(mid));
			awsControlledCbx.setHorizontalTextPosition(
				SwingConstants.LEFT);
			awsControlledCbx.setToolTipText(
				I18N.get(mid + ".tooltip"));
			addRow(awsControlledCbx);
		}
		tab.add("Current", currentPnl);
		tab.add("Preview", previewPnl);
		addRow(tab);
		createJobs();
	}

	/** Create the widget jobs */
	protected void createJobs() {
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
		currentPnl.addMouseListener(popper);
		previewPnl.addMouseListener(popper);
	}

	/** Process a popup menu event */
	protected void doPopup(MouseEvent me) {
		if(me.isPopupTrigger())
			session.getDMSManager().showPopupMenu(me);
	}

	/** Dispose of the sign tab */
	public void dispose() {
		setSelected(null);
		cache.removeProxyListener(this);
		clearPager();
		super.dispose();
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
		setCameraAction(null);
		locationTxt.setText("");
		awsControlledCbx.setSelected(false);
		awsControlledCbx.setEnabled(false);
		statusTxt.setText("");
		statusTxt.setForeground(null);
		statusTxt.setBackground(null);
		operationTxt.setText("");
		opStatusTxt.setText("");
	}

	/** Set the camera action */
	protected void setCameraAction(DMS dms) {
		Camera cam = DMSHelper.getCamera(dms);
		if(cam != null) {
			cameraBtn.setAction(new CameraSelectAction(cam,
				cam_sel_model));
		} else
			cameraBtn.setAction(null);
		cameraBtn.setEnabled(cam != null);
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
			setCameraAction(dms);
		// FIXME: this won't update when geoLoc attributes change
		if(a == null || a.equals("geoLoc")) {
			locationTxt.setText(GeoLocHelper.getDescription(
				dms.getGeoLoc()));
		}
		if(a == null || a.equals("operation"))
			updateStatus(dms);
		if(a == null || a.equals("messageCurrent")) {
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
		if(a == null || a.equals("opStatus"))
			opStatusTxt.setText(dms.getOpStatus());
	}

	/** Update the status widgets */
	protected void updateStatus(DMS dms) {
		String status = DMSHelper.getStatus(dms);
		if(status.isEmpty())
			updateCritical(dms);
		else {
			statusTxt.setForeground(Color.WHITE);
			statusTxt.setBackground(Color.GRAY);
			statusTxt.setText(status);
		}
		operationTxt.setText(dms.getOperation());
		opStatusTxt.setText(dms.getOpStatus());
	}

	/** Update the critical error status */
	protected void updateCritical(DMS dms) {
		String critical = DMSHelper.getCriticalError(dms);
		if(critical.isEmpty())
			updateMaintenance(dms);
		else {
			statusTxt.setForeground(Color.WHITE);
			statusTxt.setBackground(Color.BLACK);
			statusTxt.setText(critical);
		}
	}

	/** Update the maintenance error status */
	protected void updateMaintenance(DMS dms) {
		String maintenance = DMSHelper.getMaintenance(dms);
		if(maintenance.isEmpty()) {
			statusTxt.setForeground(null);
			statusTxt.setBackground(null);
		} else {
			statusTxt.setForeground(Color.BLACK);
			statusTxt.setBackground(Color.YELLOW);
		}
		statusTxt.setText(maintenance);
	}

	/** Update the current panel */
	protected void updateCurrentPanel(DMS dms) {
		clearPager();
		if(dms != null) {
			BitmapGraphic[] bmaps = DMSHelper.getBitmaps(dms);
			if(bmaps != null) {
				pnlPager = new DMSPanelPager(currentPnl, dms,
					bmaps, getPgOnTime(dms));
			} else
				currentPnl.clear();
		}
	}

	/** Update the current message */
	protected void updateMessageCurrent(DMS dms) {
		adjusting++;
		dispatcher.setMessage(getMultiString(dms));
		adjusting--;
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
