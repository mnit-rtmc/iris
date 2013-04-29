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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusLostJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.units.Temperature;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a dynamic message
 * sign (DMS).
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSProperties extends SonarObjectForm<DMS> {

	/** Get temperature units to use for display */
	static private Temperature.Units tempUnits() {
		return SystemAttrEnum.CLIENT_UNITS_SI.getBoolean()
		     ? Temperature.Units.CELSIUS
		     : Temperature.Units.FAHRENHEIT;
	}

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

	/** Format millimeter units for display */
	static private String formatMM(Integer i) {
		if(i != null && i > 0)
			return i + " " + I18N.get("units.mm");
		else
			return UNKNOWN;
	}

	/** Format pixel units for display */
	static private String formatPixels(Integer i) {
		if(i != null) {
			if(i > 0)
				return i + " " + I18N.get("units.pixels");
			else if(i == 0)
				return I18N.get("units.pixels.variable");
		}
		return UNKNOWN;
	}

	/** Format a temperature.
	 * @param temp Temperature in degrees Celsius. */
	static private String formatTemp(Integer temp) {
		if(temp != null) {
			Temperature.Formatter tf = new Temperature.Formatter(0);
			return tf.format(new Temperature(temp).convert(
				tempUnits()));
		} else
			return "???";
	}

	/** Format a temperature range */
	static private String formatTemp(Integer minTemp, Integer maxTemp) {
		if(minTemp == null || minTemp == maxTemp)
			return formatTemp(maxTemp);
		else if(maxTemp == null)
			return formatTemp(minTemp);
		else
			return formatTemp(minTemp) + "..." +formatTemp(maxTemp);
	}

	/** Generic sign make */
	static private final String MAKE_GENERIC = "Generic";

	/** Ledstar sign make */
	static private final String MAKE_LEDSTAR = "Ledstar";

	/** Skyline sign make */
	static private final String MAKE_SKYLINE = "Skyline";

	/** Location panel */
	private final LocationPanel location;

	/** Notes text area */
	private final JTextArea notes = new JTextArea(3, 24);

	/** Camera combo box */
	private final JComboBox camera_cbx = new JComboBox();

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		@Override protected void do_perform() {
			controllerPressed();
		}
	};

	/** Messages tab */
	private final MessagesTab messagesTab;

	/** Sign type label */
	private final JLabel type = new JLabel();

	/** Sign technology label */
	private final JLabel tech = new JLabel();

	/** Sign access label */
	private final JLabel access = new JLabel();

	/** Sign legend label */
	private final JLabel legend = new JLabel();

	/** Beacon label */
	private final JLabel beacon = new JLabel();

	/** Sign face width label */
	private final JLabel faceWidth = new JLabel();

	/** Sign face height label */
	private final JLabel faceHeight = new JLabel();

	/** Horizontal border label */
	private final JLabel hBorder = new JLabel();

	/** Vertical border label */
	private final JLabel vBorder = new JLabel();

	/** Horizontal pitch label */
	private final JLabel hPitch = new JLabel();

	/** Vertical pitch label */
	private final JLabel vPitch = new JLabel();

	/** Sign width (pixels) label */
	private final JLabel pWidth = new JLabel();

	/** Sign height (pixels) label */
	private final JLabel pHeight = new JLabel();

	/** Character width label */
	private final JLabel cWidth = new JLabel();

	/** Character height label */
	private final JLabel cHeight = new JLabel();

	/** Button to query configuration */
	private final IAction config = new IAction("dms.query.config") {
		@Override protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				QUERY_CONFIGURATION.ordinal());
		}
	};

	/** Cabinet temperature label */
	private final JLabel cabinetTemp = new JLabel();

	/** Ambient temperature label */
	private final JLabel ambientTemp = new JLabel();

	/** Housing temperature label */
	private final JLabel housingTemp = new JLabel();

	/** Power supply status table */
	private final ZTable powerTable = new ZTable();

	/** Operation description label */
	private final JLabel operation = new JLabel();

	/** Query message action */
	private final IAction query_msg = new IAction("dms.query.msg",
		SystemAttrEnum.DMS_QUERYMSG_ENABLE)
	{
		@Override protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				QUERY_MESSAGE.ordinal());
		}
	};

	/** Reset DMS action */
	private final IAction reset = new IAction("dms.reset",
		SystemAttrEnum.DMS_RESET_ENABLE)
	{
		@Override protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				RESET_DEVICE.ordinal());
		}
	};

	/** Query status action */
	private final IAction query_status = new IAction("dms.query.status") {
		@Override protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				QUERY_STATUS.ordinal());
		}
	};

	/** Send settings action */
	private final IAction settings = new IAction("device.send.settings") {
		@Override protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				SEND_SETTINGS.ordinal());
		}
	};

	/** Bad pixel count label */
	private final JLabel badPixels = new JLabel();

	/** Stuck off pixel panel */
	private final SignPixelPanel stuck_off_pnl = new SignPixelPanel(100,
		400, true);

	/** Stuck on pixel panel */
	private final SignPixelPanel stuck_on_pnl = new SignPixelPanel(100,
		400, true);

	/** Action to query pixel failures */
	private final IAction query_pixels = new IAction("dms.query.pixels") {
		@Override protected void do_perform() {
			proxy.setDeviceRequest(DeviceRequest.
				QUERY_PIXEL_FAILURES.ordinal());
		}
	};

	/** Action to test pixel failures */
	private final IAction test_pixels = new IAction("dms.test.pixels") {
		@Override protected void do_perform() {
			proxy.setDeviceRequest(
				DeviceRequest.TEST_PIXELS.ordinal());
		}
	};

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
		location = new LocationPanel(s);
		messagesTab = new MessagesTab(s, sign);
	}

	/** Get the SONAR type cache */
	@Override protected TypeCache<DMS> getTypeCache() {
		return state.getDmsCache().getDMSs();
	}

	/** Initialize the widgets on the form */
	@Override protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("dms.messages"), messagesTab);
		tab.add(I18N.get("dms.config"), createConfigurationPanel());
		tab.add(I18N.get("device.status"), createStatusPanel());
		if(SystemAttrEnum.DMS_PIXEL_STATUS_ENABLE.getBoolean())
			tab.add(I18N.get("dms.pixels"), createPixelPanel());
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
		location.dispose();
		messagesTab.dispose();
		super.dispose();
	}

	/** Create the widget jobs */
	private void createUpdateJobs() {
		notes.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setNotes(notes.getText());
			}
		});
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
		config.setEnabled(false);
		query_msg.setEnabled(false);
		reset.setEnabled(false);
		query_status.setEnabled(false);
		settings.setEnabled(false);
		query_pixels.setEnabled(false);
		test_pixels.setEnabled(false);
		bright_low.setEnabled(false);
		bright_good.setEnabled(false);
		bright_high.setEnabled(false);
	}

	/** Controller lookup button pressed */
	private void controllerPressed() {
		Controller c = proxy.getController();
		if(c != null) {
			session.getDesktop().show(
				new ControllerForm(session, c));
		}
	}

	/** Create the location panel */
	private JPanel createLocationPanel() {
		camera_cbx.setAction(new IAction("camera") {
			@Override protected void do_perform() {
				proxy.setCamera(
					(Camera)camera_cbx.getSelectedItem());
			}
		});
		camera_cbx.setModel(new WrapperComboBoxModel(
			state.getCamCache().getCameraModel()));
		location.setGeoLoc(proxy.getGeoLoc());
		location.initialize();
		location.addRow(I18N.get("device.notes"), notes);
		location.add(I18N.get("camera"), camera_cbx);
		location.finishRow();
		location.setCenter();
		location.addRow(new JButton(controller));
		return location;
	}

	/** Create the configuration panel */
	private JPanel createConfigurationPanel() {
		type.setForeground(OK);
		tech.setForeground(OK);
		access.setForeground(OK);
		legend.setForeground(OK);
		beacon.setForeground(OK);
		faceWidth.setForeground(OK);
		faceHeight.setForeground(OK);
		hBorder.setForeground(OK);
		vBorder.setForeground(OK);
		hPitch.setForeground(OK);
		vPitch.setForeground(OK);
		pWidth.setForeground(OK);
		pHeight.setForeground(OK);
		cWidth.setForeground(OK);
		cHeight.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.addRow(I18N.get("dms.type"), type);
		panel.addRow(I18N.get("dms.technology"), tech);
		panel.addRow(I18N.get("dms.access"), access);
		panel.addRow(I18N.get("dms.legend"), legend);
		panel.addRow(I18N.get("dms.beacon"), beacon);
		panel.addRow(I18N.get("dms.face.width"), faceWidth);
		panel.addRow(I18N.get("dms.face.height"), faceHeight);
		panel.addRow(I18N.get("dms.border.horiz"), hBorder);
		panel.addRow(I18N.get("dms.border.vert"), vBorder);
		panel.addRow(I18N.get("dms.pitch.horiz"), hPitch);
		panel.addRow(I18N.get("dms.pitch.vert"), vPitch);
		panel.addRow(I18N.get("dms.pixel.width"), pWidth);
		panel.addRow(I18N.get("dms.pixel.height"), pHeight);
		panel.addRow(I18N.get("dms.char.width"), cWidth);
		panel.addRow(I18N.get("dms.char.height"), cHeight);
		panel.addRow(new JButton(config));
		return panel;
	}

	/** Create status panel */
	private JPanel createStatusPanel() {
		powerTable.setAutoCreateColumnsFromModel(false);
		powerTable.setVisibleRowCount(6);
		cabinetTemp.setForeground(OK);
		ambientTemp.setForeground(OK);
		housingTemp.setForeground(OK);
		operation.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.addRow(I18N.get("dms.temp.cabinet"), cabinetTemp);
		panel.addRow(I18N.get("dms.temp.ambient"), ambientTemp);
		panel.addRow(I18N.get("dms.temp.housing"), housingTemp);
		panel.addRow(I18N.get("dms.power.supplies"), powerTable);
		panel.add(I18N.get("device.operation"), operation);
		if(query_msg.getIEnabled())
			panel.add(new JButton(query_msg));
		panel.finishRow();
		if(reset.getIEnabled())
			panel.addRow(new JButton(reset));
		panel.addRow(new JButton(query_status));
		panel.addRow(new JButton(settings));
		return panel;
	}

	/** Create pixel panel */
	private JPanel createPixelPanel() {
		JPanel buttonPnl = new JPanel();
		buttonPnl.add(new JButton(query_pixels));
		buttonPnl.add(new JButton(test_pixels));
		badPixels.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.addRow(I18N.get("dms.pixel.errors"), badPixels);
		panel.setFill();
		panel.addRow(createTitledPanel("dms.pixel.errors.off",
			stuck_off_pnl));
		panel.setFill();
		panel.addRow(createTitledPanel("dms.pixel.errors.on",
			stuck_on_pnl));
		panel.setCenter();
		panel.add(buttonPnl);
		return panel;
	}

	/** Create a panel with a titled border */
	private JPanel createTitledPanel(String text_id, JPanel p) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(I18N.get(
			text_id)));
		panel.add(p, BorderLayout.CENTER);
		return panel;
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
		messagesTab.updateAttribute(a);
		if(a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if(a == null || a.equals("notes")) {
			notes.setEnabled(canUpdate("notes"));
			notes.setText(proxy.getNotes());
		}
		if(a == null || a.equals("camera")) {
			camera_cbx.setEnabled(canUpdate("camera"));
			camera_cbx.setSelectedItem(proxy.getCamera());
		}
		if(a == null || a.equals("make")) {
			String m = formatString(proxy.getMake());
			make.setText(m);
			updateMake(m.toUpperCase());
		}
		if(a == null || a.equals("model"))
			model.setText(formatString(proxy.getModel()));
		if(a == null || a.equals("version"))
			version.setText(formatString(proxy.getVersion()));
		if(a == null || a.equals("signAccess"))
			access.setText(formatString(proxy.getSignAccess()));
		if(a == null || a.equals("dmsType")) {
			DMSType t = DMSType.fromOrdinal(proxy.getDmsType());
			type.setText(t.description);
		}
		if(a == null || a.equals("faceHeight"))
			faceHeight.setText(formatMM(proxy.getFaceHeight()));
		if(a == null || a.equals("faceWidth"))
			faceWidth.setText(formatMM(proxy.getFaceWidth()));
		if(a == null || a.equals("heightPixels"))
			pHeight.setText(formatPixels(proxy.getHeightPixels()));
		if(a == null || a.equals("widthPixels"))
			pWidth.setText(formatPixels(proxy.getWidthPixels()));
		if(a == null || a.equals("horizontalBorder"))
			hBorder.setText(formatMM(proxy.getHorizontalBorder()));
		if(a == null || a.equals("verticalBorder"))
			vBorder.setText(formatMM(proxy.getVerticalBorder()));
		if(a == null || a.equals("legend"))
			legend.setText(formatString(proxy.getLegend()));
		if(a == null || a.equals("beaconType"))
			beacon.setText(formatString(proxy.getBeaconType()));
		if(a == null || a.equals("technology"))
			tech.setText(formatString(proxy.getTechnology()));
		if(a == null || a.equals("charHeightPixels")) {
			cHeight.setText(formatPixels(
				proxy.getCharHeightPixels()));
		}
		if(a == null || a.equals("charWidthPixels")) {
			cWidth.setText(formatPixels(
				proxy.getCharWidthPixels()));
		}
		if(a == null || a.equals("horizontalPitch"))
			hPitch.setText(formatMM(proxy.getHorizontalPitch()));
		if(a == null || a.equals("verticalPitch"))
			vPitch.setText(formatMM(proxy.getVerticalPitch()));
		// NOTE: messageCurrent attribute changes after all sign
		//       dimension attributes are updated.
		if(a == null || a.equals("messageCurrent")) {
			updatePixelStatus();
			updateFeedback();
		}
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
		if(a == null || a.equals("powerStatus"))
			updatePowerStatus();
		if(a == null || a.equals("heatTapeStatus"))
			heatTapeStatus.setText(proxy.getHeatTapeStatus());
		if(a == null || a.equals("pixelStatus"))
			updatePixelStatus();
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
		if(a == null || a.equals("minCabinetTemp") ||
		   a.equals("maxCabinetTemp"))
		{
			cabinetTemp.setText(formatTemp(
				proxy.getMinCabinetTemp(),
				proxy.getMaxCabinetTemp()));
		}
		if(a == null || a.equals("minAmbientTemp") ||
		   a.equals("maxAmbientTemp"))
		{
			ambientTemp.setText(formatTemp(
				proxy.getMinAmbientTemp(),
				proxy.getMaxAmbientTemp()));
		}
		if(a == null || a.equals("minHousingTemp") ||
		   a.equals("maxHousingTemp"))
		{
			housingTemp.setText(formatTemp(
				proxy.getMinHousingTemp(),
				proxy.getMaxHousingTemp()));
		}
		if(a == null || a.equals("operation"))
			operation.setText(proxy.getOperation());
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

	/** Update the power status */
	private void updatePowerStatus() {
		String[] s = proxy.getPowerStatus();
		if(s != null) {
			PowerTableModel m = new PowerTableModel(s);
			powerTable.setColumnModel(m.createColumnModel());
			powerTable.setModel(m);
		}
	}

	/** Update the pixel status */
	private void updatePixelStatus() {
		updatePixelPanel(stuck_off_pnl);
		updatePixelPanel(stuck_on_pnl);
		String[] pixels = proxy.getPixelStatus();
		if(pixels != null && pixels.length == 2) {
			try {
				updatePixelStatus(pixels);
				return;
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		stuck_off_pnl.setGraphic(null);
		stuck_on_pnl.setGraphic(null);
		badPixels.setText(UNKNOWN);
	}

	/** Update the pixel status */
	private void updatePixelStatus(String[] pixels) throws IOException {
		BitmapGraphic stuckOff = createBlankBitmap();
		BitmapGraphic stuckOn = createBlankBitmap();
		if(stuckOff == null || stuckOn == null)
			return;
		byte[] b_off = Base64.decode(pixels[DMS.STUCK_OFF_BITMAP]);
		if(b_off.length == stuckOff.length())
			stuckOff.setPixels(b_off);
		stuck_off_pnl.setGraphic(stuckOff);
		byte[] b_on = Base64.decode(pixels[DMS.STUCK_ON_BITMAP]);
		if(b_on.length == stuckOn.length())
			stuckOn.setPixels(b_on);
		stuck_on_pnl.setGraphic(stuckOn);
		int n_off = stuckOff.getLitCount();
		int n_on = stuckOn.getLitCount();
		badPixels.setText(String.valueOf(n_off + n_on));
	}

	/** Create a blank bitmap */
	private BitmapGraphic createBlankBitmap() {
		Integer w = proxy.getWidthPixels();	// Avoid race
		Integer h = proxy.getHeightPixels();	// Avoid race
		if(w != null && h != null)
			return new BitmapGraphic(w, h);
		else
			return null;
	}

	/** Update the dimensions of a sign pixel panel */
	private void updatePixelPanel(SignPixelPanel p) {
		updatePixelPhysical(p);
		updatePixelLogical(p);
		p.repaint();
	}

	/** Update the physical dimensions of a sign pixel panel */
	private void updatePixelPhysical(SignPixelPanel p) {
		Integer w = proxy.getFaceWidth();
		Integer h = proxy.getFaceHeight();
		Integer hp = proxy.getHorizontalPitch();
		Integer vp = proxy.getVerticalPitch();
		Integer hb = proxy.getHorizontalBorder();
		Integer vb = proxy.getVerticalBorder();
		if(w != null && h != null && hp != null && vp != null &&
		   hb != null && vb != null)
		{
			p.setPhysicalDimensions(w, h, hb, vb, hp, vp);
		}
	}

	/** Update the logical dimensions of a sign pixel panel */
	private void updatePixelLogical(SignPixelPanel p) {
		Integer wp = proxy.getWidthPixels();
		Integer hp = proxy.getHeightPixels();
		Integer cw = proxy.getCharWidthPixels();
		Integer ch = proxy.getCharHeightPixels();
		if(wp != null && hp != null && cw != null && ch != null)
			p.setLogicalDimensions(wp, hp, cw, ch);
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
