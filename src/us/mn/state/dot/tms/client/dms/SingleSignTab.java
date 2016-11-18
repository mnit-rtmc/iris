/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraPresetAction;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.units.Interval;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * A SingleSignTab is a GUI component for displaying the status of a single
 * selected DMS within the DMS dispatcher.  One instance of this class is
 * created on client startup by DMSDispatcher.
 * @see DMSDispatcher.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SingleSignTab extends IPanel implements ProxyListener<DMS> {

	/** Displays the id of the DMS */
	private final JLabel name_lbl = createValueLabel();

	/** Displays the brightness of the DMS */
	private final JLabel brightness_lbl = createValueLabel();

	/** Displays the camera preset for the DMS */
	private final JButton preset_btn = new JButton();

	/** Displays the location of the DMS */
	private final JLabel location_lbl = createValueLabel();

	/** AWS controlled checkbox (optional) */
	private final JCheckBox aws_control_chk = new JCheckBox(
		new IAction("item.style.aws.controlled")
	{
		protected void doActionPerformed(ActionEvent e) {
			DMS proxy = watching;
			if (proxy != null) {
				proxy.setAwsControlled(
					aws_control_chk.isSelected());
			}
		}
	});

	/** Displays the controller status */
	private final JLabel status_lbl = createValueLabel();

	/** Displays the current operation of the DMS */
	private final JLabel operation_lbl = createValueLabel();

	/** Client session */
	private final Session session;

	/** DMS dispatcher */
	private final DMSDispatcher dispatcher;

	/** Panel for drawing current pixel status */
	private final SignPixelPanel current_pnl = new SignPixelPanel(100, 400,
		true);

	/** Panel for drawing preview pixel status */
	private final SignPixelPanel preview_pnl = new SignPixelPanel(100, 400,
		true);

	/** Pager for selected DMS panel */
	private DMSPanelPager pnlPager;

	/** Tabbed pane for current/preview panels */
	private final JTabbedPane tab = new JTabbedPane();

	/** Mouse listener for popup menus */
	private final MouseListener popper = new MouseAdapter() {
		public void mousePressed(MouseEvent me) {
			doPopup(me);
		}
		// NOTE: needed for cross-platform functionality
		public void mouseReleased(MouseEvent me) {
			doPopup(me);
		}
	};

	/** Cache of DMS proxy objects */
	private final TypeCache<DMS> cache;

	/** Currently selected DMS.  This will be null if there are zero or
	 * multiple DMS selected. */
	private DMS watching;

	/** Watch a DMS */
	private void watch(final DMS nw) {
		final DMS ow = watching;
		if (ow != null)
			cache.ignoreObject(ow);
		watching = nw;
		if (nw != null)
			cache.watchObject(nw);
	}

	/** Preview mode */
	private boolean preview;

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	private int adjusting = 0;

	/** Create a new single sign tab */
	public SingleSignTab(Session s, DMSDispatcher d) {
		session = s;
		dispatcher = d;
		cache = s.getSonarState().getDmsCache().getDMSs();
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		preset_btn.setBorder(UI.buttonBorder());
		// Make label opaque so that we can set the background color
		status_lbl.setOpaque(true);
		preview_pnl.setFilterColor(new Color(0, 0, 255, 48));

		add("device.name");
		add(name_lbl);
		if (SystemAttrEnum.DMS_BRIGHTNESS_ENABLE.getBoolean()) {
			add("dms.brightness");
			add(brightness_lbl);
		}
		add("camera");
		add(preset_btn, Stretch.LAST);
		add("location");
		add(location_lbl, Stretch.LAST);
		add("device.status");
		add(status_lbl, Stretch.LAST);
		add("device.operation");
		add(operation_lbl, Stretch.LAST);
		if (SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			aws_control_chk.setHorizontalTextPosition(
				SwingConstants.LEFT);
			add(aws_control_chk, Stretch.LEFT);
		}
		tab.add(I18N.get("dms.msg.current"), current_pnl);
		tab.add(I18N.get("dms.msg.preview"), preview_pnl);
		add(tab, Stretch.CENTER);
		createJobs();
		cache.addProxyListener(this);
	}

	/** Create the widget jobs */
	private void createJobs() {
		tab.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				selectPreview(!preview);
				if ((0 == adjusting) && !preview) {
					DMS dms = watching;
					if (dms != null)
						updateMessageCurrent(dms);
				}
			}
		});
		current_pnl.addMouseListener(popper);
		preview_pnl.addMouseListener(popper);
	}

	/** Process a popup menu event */
	private void doPopup(MouseEvent me) {
		if (me.isPopupTrigger())
			session.getDMSManager().showPopupMenu(me);
	}

	/** Dispose of the sign tab */
	@Override
	public void dispose() {
		current_pnl.removeMouseListener(popper);
		preview_pnl.removeMouseListener(popper);
		setSelected(null);
		cache.removeProxyListener(this);
		setPager(null);
		super.dispose();
	}

	/** A new proxy has been added */
	@Override
	public void proxyAdded(DMS proxy) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	@Override
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	@Override
	public void proxyRemoved(DMS proxy) {
		// Note: the DMSManager will remove the proxy from the
		//       ProxySelectionModel, so we can ignore this.
	}

	/** A proxy has been changed */
	@Override
	public void proxyChanged(final DMS proxy, final String a) {
		if (proxy == watching) {
			runSwing(new Runnable() {
				public void run() {
					updateAttribute(proxy, a);
				}
			});
		}
	}

	/** Select the preview (or current) tab */
	public void selectPreview(boolean p) {
		if (0 == adjusting) {
			preview = p;
			adjusting++;
			setMessage();
			adjusting--;
		}
	}

	/** Set the displayed message */
	public void setMessage() {
		if (preview) {
			updatePreviewPanel(watching);
			tab.setSelectedComponent(preview_pnl);
		} else {
			updateCurrentPanel(watching);
			tab.setSelectedComponent(current_pnl);
		}
	}

	/** Set a single selected DMS */
	public void setSelected(DMS dms) {
		watch(dms);
		if (dms != null)
			updateAttribute(dms, null);
		else
			clearSelected();
	}

	/** Clear the selected DMS */
	private void clearSelected() {
		setPager(null);
		current_pnl.setFilterColor(null);
		current_pnl.clear();
		preview_pnl.clear();
		name_lbl.setText("");
		brightness_lbl.setText("");
		setPresetAction(null);
		location_lbl.setText("");
		aws_control_chk.setEnabled(false);
		aws_control_chk.setSelected(false);
		status_lbl.setText("");
		status_lbl.setForeground(null);
		status_lbl.setBackground(null);
		operation_lbl.setText("");
	}

	/** Set the camera preset action */
	private void setPresetAction(DMS dms) {
		CameraPreset cp = DMSHelper.getPreset(dms);
		preset_btn.setAction(new CameraPresetAction(session, cp));
	}

	/** Update one (or all) attribute(s) on the form.
	 * @param dms The newly selected DMS. May not be null.
	 * @param a Attribute to update, null for all attributes. */
	private void updateAttribute(DMS dms, String a) {
		if (a == null || a.equals("name"))
			name_lbl.setText(dms.getName());
		if (a == null || a.equals("lightOutput")) {
			Integer o = dms.getLightOutput();
			if (o != null)
				brightness_lbl.setText("" + o + "%");
			else
				brightness_lbl.setText("");
		}
		if (a == null || a.equals("preset"))
			setPresetAction(dms);
		// FIXME: this won't update when geoLoc attributes change
		if (a == null || a.equals("geoLoc")) {
			location_lbl.setText(GeoLocHelper.getDescription(
				dms.getGeoLoc()));
		}
		if (a == null || a.equals("operation"))
			updateStatus(dms);
		if (a == null || a.equals("messageCurrent")) {
			if (!preview) {
				updateCurrentPanel(dms);
				updateMessageCurrent(dms);
			}
		}
		if (a == null || a.equals("awsAllowed")) {
			aws_control_chk.setEnabled(
				dispatcher.isAwsPermitted(dms));
		}
		if (a == null || a.equals("awsControlled"))
			aws_control_chk.setSelected(dms.getAwsControlled());
	}

	/** Update the status widgets */
	private void updateStatus(DMS dms) {
		current_pnl.setFilterColor(SignPixelPanel.filterColor(dms));
		if (DMSHelper.isFailed(dms)) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.GRAY);
			status_lbl.setText(DMSHelper.getStatus(dms));
		} else
			updateCritical(dms);
		operation_lbl.setText(dms.getOperation());
	}

	/** Update the critical error status */
	private void updateCritical(DMS dms) {
		String critical = DMSHelper.getCriticalError(dms);
		if (critical.isEmpty())
			updateMaintenance(dms);
		else {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.BLACK);
			status_lbl.setText(critical);
		}
	}

	/** Update the maintenance error status */
	private void updateMaintenance(DMS dms) {
		String maintenance = DMSHelper.getMaintenance(dms);
		if (maintenance.isEmpty()) {
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
	private void updateCurrentPanel(DMS dms) {
		RasterGraphic[] rg = DMSHelper.getRasters(dms);
		if (rg != null) {
			String ms = DMSHelper.getMultiString(dms);
			current_pnl.setDimensions(dms);
			setPager(new DMSPanelPager(current_pnl, rg, ms));
		} else {
			setPager(null);
			current_pnl.clear();
		}
	}

	/** Update the current message */
	private void updateMessageCurrent(DMS dms) {
		adjusting++;
		dispatcher.setMessage(DMSHelper.getMultiString(dms));
		adjusting--;
	}

	/** Update the preview panel */
	private void updatePreviewPanel(DMS dms) {
		DMSPanelPager p = createPreviewPager();
		if (p != null && dms != null) {
			preview_pnl.setDimensions(dms);
			setPager(p);
		} else {
			setPager(null);
			preview_pnl.clear();
		}
	}

	/** Create a preview panel pager */
	private DMSPanelPager createPreviewPager() {
		RasterGraphic[] rg = dispatcher.getPixmaps();
		if (rg != null)
			return new DMSPanelPager(preview_pnl, rg, getMulti());
		else
			return null;
	}

	/** Get the preview MULTI string */
	private String getMulti() {
		String ms = dispatcher.getMessage();
		String prefix = dispatcher.getPagePrefix();
		if (prefix.isEmpty())
			return ms;
		else
			return new MultiString(ms).addPagePrefix(prefix);
	}

	/** Set the DMS panel pager */
	private void setPager(DMSPanelPager p) {
		DMSPanelPager pager = pnlPager;
		if (pager != null)
			pager.dispose();
		pnlPager = p;
	}
}
