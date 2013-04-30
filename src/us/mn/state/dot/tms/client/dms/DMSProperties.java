/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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

import java.awt.CardLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a dynamic message
 * sign (DMS).
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSProperties extends SonarObjectForm<DMS> {

	/** Ok status label color */
	static private final Color OK = new Color(0f, 0.5f, 0f);

	/** Unknown value string */
	static private final String UNKNOWN = "???";

	/** Format a string field */
	static private String formatString(String s) {
		if(s != null && s.length() > 0)
			return s;
		else
			return UNKNOWN;
	}

	/** Generic sign make */
	static private final String MAKE_GENERIC = "Generic";

	/** Ledstar sign make */
	static private final String MAKE_LEDSTAR = "Ledstar";

	/** Skyline sign make */
	static private final String MAKE_SKYLINE = "Skyline";

	/** Location panel */
	private final PropLocation location_pnl;

	/** Messages panel */
	private final PropMessages messages_pnl;

	/** Configuration panel */
	private final PropConfiguration config_pnl;

	/** Status panel */
	private final PropStatus status_pnl;

	/** Pixel panel */
	private final PropPixels pixel_pnl;

	/** Photocell status table */
	private final ZTable photocellTable = new ZTable();

	/** Light output label */
	private final JLabel lightOutput = new JLabel();

	/** Current brightness low feedback action */
	private final IAction bright_low = new IAction("dms.brightness.low") {
		@Override protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				BRIGHTNESS_TOO_DIM.ordinal());
		}
	};

	/** Current brightness good feedback action */
	private final IAction bright_good = new IAction("dms.brightness.good") {
		@Override protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				BRIGHTNESS_GOOD.ordinal());
		}
	};

	/** Current brightness high feedback action */
	private final IAction bright_high = new IAction("dms.brightness.high") {
		@Override protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				BRIGHTNESS_TOO_BRIGHT.ordinal());
		}
	};

	/** Card layout for manufacturer panels */
	private final CardLayout cards = new CardLayout();

	/** Card panel for manufacturer panels */
	private final JPanel card_panel = new JPanel(cards);

	/** Make label */
	private final JLabel make = new JLabel();

	/** Model label */
	private final JLabel model = new JLabel();

	/** Version label */
	private final JLabel version = new JLabel();

	/** Spinner to adjuct LDC pot base */
	private final JSpinner ldcPotBaseSpn = new JSpinner(
		new SpinnerNumberModel(20, 20, 65, 5));

	/** Pixel current low threshold spinner */
	private final JSpinner currentLowSpn = new JSpinner(
		new SpinnerNumberModel(5, 0, 100, 1));

	/** Pixel current high threshold spinner */
	private final JSpinner currentHighSpn = new JSpinner(
		new SpinnerNumberModel(40, 0, 100, 1));

	/** Heat tape status label */
	private final JLabel heatTapeStatus = new JLabel();

	/** Sonar state */
	private final SonarState state;

	/** SONAR user */
	private final User user;

	/** Create a new DMS properties form */
	public DMSProperties(Session s, DMS sign) {
		super(I18N.get("dms") + ": ", s, sign);
		setHelpPageName("help.dmsproperties");
		state = s.getSonarState();
		user = s.getUser();
		location_pnl = new PropLocation(s, sign);
		messages_pnl = new PropMessages(s, sign);
		config_pnl = new PropConfiguration(s, sign);
		status_pnl = new PropStatus(s, sign);
		pixel_pnl = new PropPixels(s, sign);
	}

	/** Get the SONAR type cache */
	@Override protected TypeCache<DMS> getTypeCache() {
		return state.getDmsCache().getDMSs();
	}

	/** Initialize the widgets on the form */
	@Override protected void initialize() {
		super.initialize();
		location_pnl.initialize();
		config_pnl.initialize();
		status_pnl.initialize();
		pixel_pnl.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), location_pnl);
		tab.add(I18N.get("dms.messages"), messages_pnl);
		tab.add(I18N.get("dms.config"), config_pnl);
		tab.add(I18N.get("device.status"), status_pnl);
		if(SystemAttrEnum.DMS_PIXEL_STATUS_ENABLE.getBoolean())
			tab.add(I18N.get("dms.pixels"), pixel_pnl);
		if(SystemAttrEnum.DMS_BRIGHTNESS_ENABLE.getBoolean()) {
			tab.add(I18N.get("dms.brightness"),
				createBrightnessPanel());
		}
		if(SystemAttrEnum.DMS_MANUFACTURER_ENABLE.getBoolean()) {
			tab.add(I18N.get("dms.manufacturer"),
				createManufacturerPanel());
		}
		add(tab);
		updateAttribute(null);
		if(canUpdate())
			createUpdateJobs();
		if(!canRequest())
			disableRequestWidgets();
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	@Override protected void dispose() {
		location_pnl.dispose();
		messages_pnl.dispose();
		super.dispose();
	}

	/** Create the widget jobs */
	private void createUpdateJobs() {
		ldcPotBaseSpn.addChangeListener(new ChangeJob(WORKER) {
			@Override public void perform() {
				Number n = (Number)ldcPotBaseSpn.getValue();
				proxy.setLdcPotBase(n.intValue());
			}
		});
		currentLowSpn.addChangeListener(new ChangeJob(WORKER) {
			@Override public void perform() {
				Number n = (Number)currentLowSpn.getValue();
				proxy.setPixelCurrentLow(n.intValue());
			}
		});
		currentHighSpn.addChangeListener(new ChangeJob(WORKER) {
			@Override public void perform() {
				Number n = (Number)currentHighSpn.getValue();
				proxy.setPixelCurrentHigh(n.intValue());
			}
		});
	}

	/** Disable the device request widgets */
	private void disableRequestWidgets() {
		bright_low.setEnabled(false);
		bright_good.setEnabled(false);
		bright_high.setEnabled(false);
	}

	/** Create brightness panel */
	private JPanel createBrightnessPanel() {
		photocellTable.setAutoCreateColumnsFromModel(false);
		photocellTable.setVisibleRowCount(6);
		lightOutput.setForeground(OK);
		JPanel feedback = new JPanel();
		feedback.add(new JButton(bright_low));
		feedback.add(new JButton(bright_good));
		feedback.add(new JButton(bright_high));
		FormPanel panel = new FormPanel(true);
		panel.addRow(I18N.get("dms.brightness.photocells"),
			photocellTable);
		panel.addRow(I18N.get("dms.brightness.output"), lightOutput);
		panel.addRow(I18N.get("dms.brightness.feedback"), feedback);
		return panel;
	}

	/** Create manufacturer-specific panel */
	private JPanel createManufacturerPanel() {
		make.setForeground(OK);
		model.setForeground(OK);
		version.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.addRow(I18N.get("dms.make"), make);
		panel.addRow(I18N.get("dms.model"), model);
		panel.addRow(I18N.get("dms.version"), version);
		panel.addRow(card_panel);
		card_panel.add(createGenericPanel(), MAKE_GENERIC);
		card_panel.add(createLedstarPanel(), MAKE_LEDSTAR);
		card_panel.add(createSkylinePanel(), MAKE_SKYLINE);
		return panel;
	}

	/** Create generic manufacturer panel */
	private JPanel createGenericPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setTitle(I18N.get("dms.manufacturer.unknown"));
		panel.addRow(new JLabel(UNKNOWN));
		return panel;
	}

	/** Create Ledstar-specific panel */
	private JPanel createLedstarPanel() {
		FormPanel panel = new FormPanel(canUpdate());
		panel.setTitle(MAKE_LEDSTAR);
		panel.addRow(I18N.get("dms.ledstar.pot.base"), ldcPotBaseSpn);
		panel.addRow(I18N.get("dms.ledstar.current.low"),currentLowSpn);
		panel.addRow(I18N.get("dms.ledstar.current.high"),
			currentHighSpn);
		return panel;
	}

	/** Create Skyline-specific panel */
	private JPanel createSkylinePanel() {
		heatTapeStatus.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.setTitle(MAKE_SKYLINE);
		panel.addRow(I18N.get("dms.skyline.heat.tape"), heatTapeStatus);
		return panel;
	}

	/** Update one attribute on the form */
	@Override protected void doUpdateAttribute(String a) {
		location_pnl.updateAttribute(a);
		messages_pnl.updateAttribute(a);
		config_pnl.updateAttribute(a);
		status_pnl.updateAttribute(a);
		pixel_pnl.updateAttribute(a);
		if(a == null || a.equals("make")) {
			String m = formatString(proxy.getMake());
			make.setText(m);
			updateMake(m.toUpperCase());
		}
		if(a == null || a.equals("model"))
			model.setText(formatString(proxy.getModel()));
		if(a == null || a.equals("version"))
			version.setText(formatString(proxy.getVersion()));
		if(a == null || a.equals("messageCurrent"))
			updateFeedback();
		if(a == null || a.equals("ldcPotBase")) {
			Integer b = proxy.getLdcPotBase();
			if(b != null)
				ldcPotBaseSpn.setValue(b);
		}
		if(a == null || a.equals("pixelCurrentLow")) {
			Integer c = proxy.getPixelCurrentLow();
			if(c != null)
				currentLowSpn.setValue(c);
		}
		if(a == null || a.equals("pixelCurrentHigh")) {
			Integer c = proxy.getPixelCurrentHigh();
			if(c != null)
				currentHighSpn.setValue(c);
		}
		if(a == null || a.equals("heatTapeStatus"))
			heatTapeStatus.setText(proxy.getHeatTapeStatus());
		if(a == null || a.equals("photocellStatus"))
			updatePhotocellStatus();
		if(a == null || a.equals("lightOutput")) {
			Integer o = proxy.getLightOutput();
			if(o != null)
				lightOutput.setText("" + o + "%");
			else
				lightOutput.setText(UNKNOWN);
			updateFeedback();
		}
	}

	/** Select card on manufacturer panel for the given make */
	private void updateMake(String m) {
		if(m.contains(MAKE_LEDSTAR.toUpperCase()))
			cards.show(card_panel, MAKE_LEDSTAR);
		else if(m.contains(MAKE_SKYLINE.toUpperCase()))
			cards.show(card_panel, MAKE_SKYLINE);
		else
			cards.show(card_panel, MAKE_GENERIC);
	}

	/** Update the photocell status */
	private void updatePhotocellStatus() {
		String[] s = proxy.getPhotocellStatus();
		if(s != null) {
			PhotocellTableModel m = new PhotocellTableModel(s);
			photocellTable.setColumnModel(m.createColumnModel());
			photocellTable.setModel(m);
		}
	}

	/** Update the feedback buttons */
	private void updateFeedback() {
		boolean enable = canRequest() && !SignMessageHelper.isBlank(
			proxy.getMessageCurrent());
		bright_low.setEnabled(enable);
		bright_good.setEnabled(enable);
		bright_high.setEnabled(enable);
	}

	/** Check if the user can make device requests */
	private boolean canRequest() {
		return canUpdate("deviceRequest");
	}
}
