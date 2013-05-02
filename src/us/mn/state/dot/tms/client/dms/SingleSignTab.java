/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static us.mn.state.dot.sched.SwingRunner.runSwing;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PageTimeHelper;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraSelectAction;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.units.Interval;
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

	/** Displays the id of the DMS */
	private final JLabel name_lbl = createValueLabel();

	/** Displays the brightness of the DMS */
	private final JLabel brightness_lbl = createValueLabel();

	/** Displays the verify camera for the DMS */
	protected final JButton cameraBtn = new JButton();

	/** Displays the location of the DMS */
	private final JLabel location_lbl = createValueLabel();

	/** AWS controlled checkbox (optional) */
	private final JCheckBox aws_control_chk = new JCheckBox(
		new IAction("item.style.aws.controlled")
	{
		protected void do_perform() {
			DMS proxy = watching;
			if(proxy != null) {
				proxy.setAwsControlled(
					aws_control_chk.isSelected());
			}
		}
	});

	/** Displays the controller status */
	private final JLabel status_lbl = createValueLabel();

	/** Displays the current operation of the DMS */
	private final JLabel operation_lbl = createValueLabel();

	/** Displays the controller operation status (optional) */
	private final JLabel op_status_lbl = createValueLabel();

	/** Client session */
	protected final Session session;

	/** DMS dispatcher */
	protected final DMSDispatcher dispatcher;

	/** Panel for drawing current pixel status */
	private final SignPixelPanel currentPnl = new SignPixelPanel(100, 400,
		true);

	/** Panel for drawing preview pixel status */
	private final SignPixelPanel previewPnl = new SignPixelPanel(100, 400,
		true, new Color(0, 0, 0.4f));

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
	private DMS watching;

	/** Watch a DMS */
	private void watch(final DMS nw) {
		final DMS ow = watching;
		if(ow != null)
			cache.ignoreObject(ow);
		watching = nw;
		if(nw != null)
			cache.watchObject(nw);
	}

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
		setHeavy(true);
		add(I18N.get("device.name"), name_lbl);
		if(SystemAttrEnum.DMS_BRIGHTNESS_ENABLE.getBoolean())
			add(I18N.get("dms.brightness"), brightness_lbl);
		setHeavy(false);
		cameraBtn.setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.LOWERED));
		addRow(I18N.get("camera"), cameraBtn);
		addRow(I18N.get("location"), location_lbl);
		addRow(I18N.get("device.status"), status_lbl);
		// Make label opaque so that we can set the background color
		status_lbl.setOpaque(true);
		addRow(I18N.get("device.operation"), operation_lbl);
		if(SystemAttrEnum.DMS_OP_STATUS_ENABLE.getBoolean())
			addRow(I18N.get("device.op.status"), op_status_lbl);
		if(SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			setWest();
			aws_control_chk.setHorizontalTextPosition(
				SwingConstants.LEFT);
			addRow(aws_control_chk);
		}
		tab.add(I18N.get("dms.msg.current"), currentPnl);
		tab.add(I18N.get("dms.msg.preview"), previewPnl);
		setCenter();
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
			runSwing(new Runnable() {
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
		watch(dms);
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
		name_lbl.setText("");
		brightness_lbl.setText("");
		setCameraAction(null);
		location_lbl.setText("");
		aws_control_chk.setEnabled(false);
		aws_control_chk.setSelected(false);
		status_lbl.setText("");
		status_lbl.setForeground(null);
		status_lbl.setBackground(null);
		operation_lbl.setText("");
		op_status_lbl.setText("");
	}

	/** Set the camera action */
	protected void setCameraAction(DMS dms) {
		Camera cam = DMSHelper.getCamera(dms);
		cameraBtn.setAction(new CameraSelectAction(cam, cam_sel_model));
		cameraBtn.setEnabled(cam != null);
	}

	/** Update one (or all) attribute(s) on the form.
	 * @param dms The newly selected DMS. May not be null.
	 * @param a Attribute to update, null for all attributes. */
	protected void updateAttribute(DMS dms, String a) {
		if(a == null || a.equals("name"))
			name_lbl.setText(dms.getName());
		if(a == null || a.equals("lightOutput")) {
			Integer o = dms.getLightOutput();
			if(o != null)
				brightness_lbl.setText("" + o + "%");
			else
				brightness_lbl.setText("");
		}
		if(a == null || a.equals("camera"))
			setCameraAction(dms);
		// FIXME: this won't update when geoLoc attributes change
		if(a == null || a.equals("geoLoc")) {
			location_lbl.setText(GeoLocHelper.getDescription(
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
			aws_control_chk.setEnabled(
				dispatcher.isAwsPermitted(dms));
		}
		if(a == null || a.equals("awsControlled"))
			aws_control_chk.setSelected(dms.getAwsControlled());
		if(a == null || a.equals("opStatus"))
			op_status_lbl.setText(dms.getOpStatus());
	}

	/** Update the status widgets */
	protected void updateStatus(DMS dms) {
		if(DMSHelper.isFailed(dms)) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.GRAY);
			status_lbl.setText(DMSHelper.getStatus(dms));
		} else
			updateCritical(dms);
		operation_lbl.setText(dms.getOperation());
		op_status_lbl.setText(dms.getOpStatus());
	}

	/** Update the critical error status */
	protected void updateCritical(DMS dms) {
		String critical = DMSHelper.getCriticalError(dms);
		if(critical.isEmpty())
			updateMaintenance(dms);
		else {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.BLACK);
			status_lbl.setText(critical);
		}
	}

	/** Update the maintenance error status */
	protected void updateMaintenance(DMS dms) {
		String maintenance = DMSHelper.getMaintenance(dms);
		if(maintenance.isEmpty()) {
			status_lbl.setForeground(null);
			status_lbl.setBackground(null);
			status_lbl.setText("");
		} else {
			status_lbl.setForeground(Color.BLACK);
			status_lbl.setBackground(Color.YELLOW);
			status_lbl.setText(maintenance);
		}
	}

	/** Update the current panel */
	protected void updateCurrentPanel(DMS dms) {
		clearPager();
		if(dms != null) {
			RasterGraphic[] rg = DMSHelper.getRasters(dms);
			if(rg != null) {
				String ms = getMultiString(dms);
				pnlPager = new DMSPanelPager(currentPnl, dms,
					rg, pageOnIntervals(ms),
					pageOffIntervals(ms));
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

	/** Get the page-on intervals for the specified MULTI string.
	 * @param m MULTI string.
	 * @return Array of page-on intervals (for each page). */
	private Interval[] pageOnIntervals(String m) {
		MultiString ms = new MultiString(m);
		return ms.pageOnIntervals(PageTimeHelper.defaultPageOnInterval(
			ms.singlePage()));
	}

	/** Get the page-off intervals for the specified MULTI string.
	 * @param ms MULTI string.
	 * @return Array of page-off intervals (for each page). */
	private Interval[] pageOffIntervals(String ms) {
		return new MultiString(ms).pageOffIntervals(
			PageTimeHelper.defaultPageOffInterval());
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
			RasterGraphic[] rg = dispatcher.getPixmaps();
			pnlPager = new DMSPanelPager(previewPnl, dms, rg,
				pageOnIntervals(ms), pageOffIntervals(ms));
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
