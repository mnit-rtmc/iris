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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignRequest;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.schedule.TimingPlanModel;
import us.mn.state.dot.tms.client.toast.ControllerForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.LocationPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.tms.client.toast.ZTable;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * This is a form for viewing and editing the properties of a dynamic message
 * sign (DMS).
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSProperties extends SonarObjectForm<DMS> {

	/** Format milimeter units for display */
	static protected String formatMM(Integer i) {
		if(i != null && i > 0)
			return i + " mm";
		else
			return UNKNOWN;
	}

	/** Format pixel units for display */
	static protected String formatPixels(Integer i) {
		if(i != null) {
			if(i > 0)
				return i + " pixels";
			else if(i == 0)
				return "Variable";
		}
		return UNKNOWN;
	}

	/** Format the temperature */
	static protected String formatTemp(Integer minTemp, Integer maxTemp) {
		if(minTemp == null || minTemp == maxTemp)
			return Temperature.formatCelsius(maxTemp);
		if(maxTemp == null)
			return Temperature.formatCelsius(minTemp);
		return Temperature.formatCelsius(minTemp) + " ... " +
		       Temperature.formatCelsius(maxTemp);
	}

	/** Frame title */
	static protected String TITLE = 
		I18NMessages.get("dms.abbreviation") + ": ";

	/** Generic sign make */
	static protected final String MAKE_GENERIC = "Generic";

	/** Ledstar sign make */
	static protected final String MAKE_LEDSTAR = "Ledstar";

	/** Skyline sign make */
	static protected final String MAKE_SKYLINE = "Skyline";

	/** Location panel */
	protected LocationPanel location;

	/** Notes text area */
	protected final JTextArea notes = new JTextArea(3, 24);

	/** Camera combo box */
	protected final JComboBox camera = new JComboBox();

	/** Controller button */
	protected final JButton controllerBtn = new JButton("Controller");

	/** Sign group model */
	protected final SignGroupModel sign_group_model;

	/** Sign text table model */
	protected SignTextTableModel sign_text_model;

	/** Sign group table component */
	protected final ZTable group_table = new ZTable();

	/** Button to delete a sign group */
	protected final JButton delete_group = new JButton("Delete Group");

	/** Sign text table component */
	protected final ZTable sign_text_table = new ZTable();

	/** Button to delete sign text message */
	protected final JButton delete_text = new JButton("Delete Message");

	/** Sign pixel panel */
	protected final SignPixelPanel pixel_panel = new SignPixelPanel();

	/** Travel time template string field */
	protected final JTextArea travel = new JTextArea(10, 24);

	/** Timing plan table component */
	protected final ZTable plan_table = new ZTable();

	/** AWS allowed component */
	protected final JCheckBox awsAllowed = new JCheckBox(
		I18NMessages.get("dms.aws_allowed"));

	/** AWS controlled component */
	protected final JCheckBox awsControlled = new JCheckBox(
		I18NMessages.get("dms.aws_controlled"));

	/** Make label */
	protected final JLabel make = new JLabel();

	/** Model label */
	protected final JLabel model = new JLabel();

	/** Version label */
	protected final JLabel version = new JLabel();

	/** Sign access label */
	protected final JLabel access = new JLabel();

	/** Sign type label */
	protected final JLabel type = new JLabel();

	/** Sign face height label */
	protected final JLabel faceHeight = new JLabel();

	/** Sign face width label */
	protected final JLabel faceWidth = new JLabel();

	/** Horizontal border label */
	protected final JLabel hBorder = new JLabel();

	/** Vertical border label */
	protected final JLabel vBorder = new JLabel();

	/** Sign legend label */
	protected final JLabel legend = new JLabel();

	/** Beacon label */
	protected final JLabel beacon = new JLabel();

	/** Sign technology label */
	protected final JLabel tech = new JLabel();

	/** Character height label */
	protected final JLabel cHeight = new JLabel();

	/** Character width label */
	protected final JLabel cWidth = new JLabel();

	/** Sign height (pixels) label */
	protected final JLabel pHeight = new JLabel();

	/** Sign width (pixels) label */
	protected final JLabel pWidth = new JLabel();

	/** Horizontal pitch label */
	protected final JLabel hPitch = new JLabel();

	/** Vertical pitch label */
	protected final JLabel vPitch = new JLabel();

	/** Spinner to adjuct LDC pot base */
	protected final JSpinner ldcPotBaseSpn = new JSpinner(
		new SpinnerNumberModel(20, 20, 65, 5));

	/** Pixel current low threshold spinner */
	protected final JSpinner currentLowSpn = new JSpinner(
		new SpinnerNumberModel(5, 0, 100, 1));

	/** Pixel current high threshold spinner */
	protected final JSpinner currentHighSpn = new JSpinner(
		new SpinnerNumberModel(40, 0, 100, 1));

	/** Power supply status table */
	protected final ZTable powerTable = new ZTable();

	/** Heat tape status label */
	protected final JLabel heatTapeStatus = new JLabel();

	/** Stuck off pixel panel */
	protected final SignPixelPanel stuck_off_pnl = new SignPixelPanel();

	/** Stuck on pixel panel */
	protected final SignPixelPanel stuck_on_pnl = new SignPixelPanel();

	/** Bad pixel count label */
	protected final JLabel badPixels = new JLabel();

	/** Lamp status table */
	protected final ZTable lampTable = new ZTable();

	/** Light output label */
	protected final JLabel lightOutput = new JLabel();

	/** Brightness feedback combo box */
	protected final JComboBox feedback = new JComboBox(
		new SignRequest[] {
			SignRequest.NO_REQUEST,
			SignRequest.BRIGHTNESS_GOOD,
			SignRequest.BRIGHTNESS_TOO_DIM,
			SignRequest.BRIGHTNESS_TOO_BRIGHT
		}
	);

	/** Cabinet temperature label */
	protected final JLabel cabinetTemp = new JLabel();

	/** Ambient temperature label */
	protected final JLabel ambientTemp = new JLabel();

	/** Housing temperature label */
	protected final JLabel housingTemp = new JLabel();

	/** Operation description label */
	protected final JLabel operation = new JLabel();

	/** User Note */
	protected final JLabel userNote = new JLabel();

	/** Card layout for manufacturer panels */
	protected final CardLayout cards = new CardLayout();

	/** Card panel for manufacturer panels */
	protected final JPanel card_panel = new JPanel(cards);

	/** Sonar state */
	protected final SonarState state;

	/** SONAR user */
	protected final User user;

	/** Create a new DMS properties form 
	 * @param tc TmsConnection
	 * @param sign DMS proxy object
	 */
	public DMSProperties(TmsConnection tc, DMS sign) {
		super(TITLE, tc, sign);
		state = tc.getSonarState();
		user = state.lookupUser(tc.getUser().getName());
		sign_group_model = new SignGroupModel(sign,
			state.getDmsSignGroups(), state.getSignGroups(),
			connection.isAdmin());
	}

	/** Get the SONAR type cache */
	protected TypeCache<DMS> getTypeCache() {
		return state.getDMSs();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add("Location", createLocationPanel());
		tab.add("Messages", createMessagePanel());
		tab.add("Travel Time", createTravelTimePanel());
		tab.add("Configuration", createConfigurationPanel());
		tab.add("Status", createStatusPanel());
		if(SystemAttributeHelper.isDmsPixelStatusEnabled())
			tab.add("Pixels", createPixelPanel());
		if(SystemAttributeHelper.isDmsBrightnessEnabled())
			tab.add("Brightness", createBrightnessPanel());
		if(SystemAttributeHelper.isDmsManufacturerEnabled())
			tab.add("Manufacturer", createManufacturerPanel());
		add(tab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		location.dispose();
		sign_group_model.dispose();
		if(sign_text_model != null)
			sign_text_model.dispose();
		super.dispose();
	}

	/** Add a actions for the delete buttons */
	protected void addDeleteActions() {
		new ActionJob(this, delete_group) {
			public void perform() {
				SignGroup group = getSelectedGroup();
				if(group != null)
					group.destroy();
			}
		};
		new ActionJob(this, delete_text) {
			public void perform() throws Exception {
				SignText sign_text = getSelectedSignText();
				if(sign_text != null)
					sign_text.destroy();
			}
		};
	}

	/** Create the location panel */
	protected JPanel createLocationPanel() {
		new FocusJob(notes) {
			public void perform() {
				proxy.setNotes(notes.getText());
			}
		};
		new ActionJob(this, camera) {
			public void perform() {
				proxy.setCamera(
					(Camera)camera.getSelectedItem());
			}
		};
		new ActionJob(this, awsAllowed) {
			public void perform() {
				proxy.setAwsAllowed(awsAllowed.isSelected());
			}
		};
		new ActionJob(this, awsControlled) {
			public void perform() {
				proxy.setAwsControlled(
					awsControlled.isSelected());
			}
		};
		new ActionJob(this, controllerBtn) {
			public void perform() {
				controllerPressed();
			}
		};
		location = new LocationPanel(true, proxy.getGeoLoc(), state);
		location.initialize();
		location.addRow("Notes", notes);
		camera.setModel(new WrapperComboBoxModel(
			state.getCameraModel()));
		location.add("Camera", camera);
		location.finishRow();
		location.setCenter();
		location.addRow(controllerBtn);
		return location;
	}

	/** Controller lookup button pressed */
	protected void controllerPressed() {
		Controller c = proxy.getController();
		if(c == null)
			controllerBtn.setEnabled(false);
		else {
			connection.getDesktop().show(
				new ControllerForm(connection, c));
		}
	}

	/** Create the message panel */
	protected JPanel createMessagePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = 5;
		bag.insets.left = 5;
		bag.insets.right = 5;
		bag.insets.bottom = 5;
		bag.fill = GridBagConstraints.BOTH;
		initGroupTable();
		JScrollPane scroll = new JScrollPane(group_table);
		bag.gridx = 0;
		bag.gridy = 0;
		panel.add(scroll, bag);
		initSignTextTable();
		scroll = new JScrollPane(sign_text_table);
		bag.gridx = 1;
		bag.gridy = 0;
		panel.add(scroll, bag);
		bag.fill = GridBagConstraints.NONE;
		// FIXME: check SONAR roles here
		if(admin) {
			bag.gridx = 0;
			bag.gridy = 1;
			delete_group.setEnabled(false);
			panel.add(delete_group, bag);
			bag.gridx = 1;
			delete_text.setEnabled(false);
			panel.add(delete_text, bag);
			addDeleteActions();
		}
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 2;
		bag.fill = GridBagConstraints.BOTH;
		JPanel pnl = new JPanel();
		pnl.setBorder(BorderFactory.createTitledBorder(
			"Message Preview"));
		pnl.add(pixel_panel);
		panel.add(pnl, bag);
		bag.gridy = 3;
		bag.gridwidth = 1;
		bag.fill = GridBagConstraints.NONE;
		panel.add(awsAllowed, bag);
		bag.gridx = 1;
		panel.add(awsControlled, bag);
		return panel;
	}

	/** Initialize the sign group table */
	protected void initGroupTable() {
		final ListSelectionModel s = group_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectGroup();
			}
		};
		group_table.setAutoCreateColumnsFromModel(false);
		group_table.setColumnModel(SignGroupModel.createColumnModel());
		group_table.setModel(sign_group_model);
		group_table.setVisibleRowCount(12);
	}

	/** Select a new sign group */
	protected void selectGroup() {
		SignGroup group = getSelectedGroup();
		if(group != null) {
			if(sign_text_model != null)
				sign_text_model.dispose();
			sign_text_model = new SignTextTableModel(group,
				state.getSignText(), user);
			sign_text_table.setModel(sign_text_model);
			delete_group.setEnabled(isGroupDeletable(group));
		} else {
			sign_text_table.setModel(new DefaultTableModel());
			delete_group.setEnabled(false);
		}
	}

	/** Check if a sign group is deletable */
	protected boolean isGroupDeletable(SignGroup group) {
		return !(hasMembers(group) || hasSignText(group));
	}

	/** Check if a sign group has any members */
	protected boolean hasMembers(final SignGroup group) {
		TypeCache<DmsSignGroup> dms_sign_groups =
			state.getDmsSignGroups();
		return null != dms_sign_groups.findObject(
			new Checker<DmsSignGroup>()
		{
			public boolean check(DmsSignGroup g) {
				return g.getSignGroup() == group;
			}
		});
	}

	/** Check if a sign group has any sign text messages */
	protected boolean hasSignText(final SignGroup group) {
		TypeCache<SignText> sign_text = state.getSignText();
		return null != sign_text.findObject(new Checker<SignText>() {
			public boolean check(SignText t) {
				return t.getSignGroup() == group;
			}
		});
	}

	/** Get the selected sign group */
	protected SignGroup getSelectedGroup() {
		ListSelectionModel s = group_table.getSelectionModel();
		return sign_group_model.getProxy(s.getMinSelectionIndex());
	}

	/** Initialize the sign text table */
	protected void initSignTextTable() {
		final ListSelectionModel s =
			sign_text_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectSignText();
			}
		};
		sign_text_table.setAutoCreateColumnsFromModel(false);
		sign_text_table.setColumnModel(
			SignTextTableModel.createColumnModel());
		sign_text_table.setVisibleRowCount(12);
	}

	/** Select a new sign text message */
	protected void selectSignText() {
		Integer w = proxy.getFaceWidth();
		Integer lh = getLineHeightPixels();
		Integer hp = proxy.getHorizontalPitch();
		Integer vp = proxy.getVerticalPitch();
		Integer hb = proxy.getHorizontalBorder();
		if(w != null && lh != null && hp != null && vp != null &&
		   hb != null)
		{
			int h = lh * vp;
			pixel_panel.setPhysicalDimensions(w, h, hb, 0, hp, vp);
		}
		Integer wp = proxy.getWidthPixels();
		Integer cw = proxy.getCharWidthPixels();
		if(wp != null && lh != null && cw != null)
			pixel_panel.setLogicalDimensions(wp, lh, cw, 0);
		pixel_panel.verifyDimensions();
		SignText st = getSelectedSignText();
		if(st != null)
			pixel_panel.setGraphic(renderMessage(st));
		else
			pixel_panel.setGraphic(null);
		delete_text.setEnabled(st != null);
	}

	/** Get the line height of the sign */
	protected Integer getLineHeightPixels() {
		Integer w = proxy.getWidthPixels();
		Integer h = proxy.getHeightPixels();
		Integer cw = proxy.getCharWidthPixels();
		Integer ch = proxy.getCharHeightPixels();
		if(w == null || h == null || cw == null || ch == null)
			return null;
		PixelMapBuilder b = new PixelMapBuilder(state.getNamespace(),
			w, h, cw, ch);
		return b.getLineHeightPixels();
	}

	/** Render a message to a bitmap graphic */
	protected BitmapGraphic renderMessage(SignText st) {
		MultiString multi = new MultiString(st.getMessage());
		BitmapGraphic[] pages = renderPages(multi);
		if(pages.length > 0)
			return pages[0];
		else
			return null;
	}

	/** Render the pages of a text message */
	protected BitmapGraphic[] renderPages(MultiString multi) {
		Integer w = proxy.getWidthPixels();
		Integer h = getLineHeightPixels();
		Integer cw = proxy.getCharWidthPixels();
		Integer ch = proxy.getCharHeightPixels();
		if(w == null || h == null || cw == null || ch == null)
			return new BitmapGraphic[0];
		PixelMapBuilder b = new PixelMapBuilder(state.getNamespace(),
			w, h, cw, ch);
		multi.parse(b);
		return b.getPixmaps();
	}

	/** Get the selected sign text message */
	protected SignText getSelectedSignText() {
		SignTextTableModel m = sign_text_model;
		if(m == null)
			return null;
		ListSelectionModel s = sign_text_table.getSelectionModel();
		return m.getProxy(s.getMinSelectionIndex());
	}

	/** Create the travel time panel */
	protected JPanel createTravelTimePanel() {
		new FocusJob(travel) {
			public void perform() {
				proxy.setTravel(travel.getText());
			}
		};
		plan_table.setAutoCreateColumnsFromModel(false);
		plan_table.setModel(new TimingPlanModel(state.getTimingPlans(),
			proxy));
		plan_table.setColumnModel(TimingPlanModel.createColumnModel());
		plan_table.setVisibleRowCount(4);
		FormPanel panel = new FormPanel(true);
		panel.addRow("Travel template", travel);
		panel.addRow(plan_table);
		return panel;
	}

	/** Create the configuration panel */
	protected JPanel createConfigurationPanel() {
		make.setForeground(OK);
		model.setForeground(OK);
		version.setForeground(OK);
		access.setForeground(OK);
		type.setForeground(OK);
		faceHeight.setForeground(OK);
		pHeight.setForeground(OK);
		faceWidth.setForeground(OK);
		pWidth.setForeground(OK);
		hBorder.setForeground(OK);
		vBorder.setForeground(OK);
		legend.setForeground(OK);
		beacon.setForeground(OK);
		tech.setForeground(OK);
		cHeight.setForeground(OK);
		cWidth.setForeground(OK);
		hPitch.setForeground(OK);
		vPitch.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.addRow("Make", make);
		panel.addRow("Model", model);
		panel.addRow("Version", version);
		panel.addRow("Access", access);
		panel.addRow("Type", type);
		panel.addRow("Face height", faceHeight);
		panel.addRow("Sign height", pHeight);
		panel.addRow("Face width", faceWidth);
		panel.addRow("Sign width", pWidth);
		panel.addRow("Horizontal border", hBorder);
		panel.addRow("Vertical border", vBorder);
		panel.addRow("Legend", legend);
		panel.addRow("Beacon", beacon);
		panel.addRow("Technology", tech);
		panel.addRow("Character height", cHeight);
		panel.addRow("Character width", cWidth);
		panel.addRow("Horizontal pitch", hPitch);
		panel.addRow("Vertical pitch", vPitch);
		return panel;
	}

	/** Create status panel */
	protected JPanel createStatusPanel() {
		cabinetTemp.setForeground(OK);
		ambientTemp.setForeground(OK);
		housingTemp.setForeground(OK);
		operation.setForeground(OK);
		userNote.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.addRow("Cabinet temp", cabinetTemp);
		panel.addRow("Ambient temp", ambientTemp);
		panel.addRow("Housing temp", housingTemp);
		panel.add("Operation", operation);
		if(SystemAttributeHelper.isDmsStatusEnabled()) {
			JButton btn = new JButton(I18NMessages.get(
				"dms.query_status"));
			btn.setToolTipText(I18NMessages.get(
				"dms.query_status.tooltip"));
			panel.add(btn);
			new ActionJob(this, btn) {
				public void perform() throws Exception {
					proxy.setSignRequest(SignRequest.
						QUERY_MESSAGE.ordinal());
				}
			};
		}
		panel.finishRow();
		panel.addRow("User Note", userNote);
		if(SystemAttributeHelper.isDmsResetEnabled()) {
			JButton btn = new JButton(I18NMessages.get(
				"dms.reset"));
			btn.setToolTipText(I18NMessages.get(
				"dms.reset.tooltip"));
			panel.addRow(btn);
			new ActionJob(this, btn) {
				public void perform() {
					proxy.setSignRequest(SignRequest.
						RESET_DMS.ordinal());
				}
			};
		}
		return panel;
	}

	/** Create pixel panel */
	protected JPanel createPixelPanel() {
		badPixels.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.addRow("Stuck Off", stuck_off_pnl);
		panel.addRow("Stuck On", stuck_on_pnl);
		panel.add("Pixel errors", badPixels);
		JButton btn = new JButton("Test Pixels");
		panel.add(btn);
		new ActionJob(this, btn) {
			public void perform() {
				proxy.setSignRequest(
					SignRequest.TEST_PIXELS.ordinal());
			}
		};
		panel.finishRow();
		return panel;
	}

	/** Create brightness panel */
	protected JPanel createBrightnessPanel() {
		lightOutput.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.addRow("Lamp status", lampTable);
		panel.addRow("Light output", lightOutput);
		panel.addRow("Feedback", feedback);
		new ActionJob(this, feedback) {
			public void perform() {
				SignRequest sr =
					(SignRequest)feedback.getSelectedItem();
				proxy.setSignRequest(sr.ordinal());
				feedback.setEnabled(false);
			}
		};
		return panel;
	}

	/** Create manufacturer-specific panel */
	protected JPanel createManufacturerPanel() {
		card_panel.add(createGenericPanel(), MAKE_GENERIC);
		card_panel.add(createLedstarPanel(), MAKE_LEDSTAR);
		card_panel.add(createSkylinePanel(), MAKE_SKYLINE);
		return card_panel;
	}

	/** Create generic manufacturer panel */
	protected JPanel createGenericPanel() {
		FormPanel panel = new FormPanel(true);
		panel.setTitle("Unknown manufacturer");
		panel.addRow(new JLabel("Nothing to see here"));
		return panel;
	}

	/** Create Ledstar-specific panel */
	protected JPanel createLedstarPanel() {
		new ChangeJob(this, ldcPotBaseSpn) {
			public void perform() {
				Number n = (Number)ldcPotBaseSpn.getValue();
				proxy.setLdcPotBase(n.intValue());
			}
		};
		new ChangeJob(this, currentLowSpn) {
			public void perform() {
				Number n = (Number)currentLowSpn.getValue();
				proxy.setPixelCurrentLow(n.intValue());
			}
		};
		new ChangeJob(this, currentHighSpn) {
			public void perform() {
				Number n = (Number)currentHighSpn.getValue();
				proxy.setPixelCurrentHigh(n.intValue());
			}
		};
		FormPanel panel = new FormPanel(true);
		panel.setTitle(MAKE_LEDSTAR);
		panel.addRow("LDC pot base", ldcPotBaseSpn);
		panel.addRow("Pixel current low threshold", currentLowSpn);
		panel.addRow("Pixel current high threshold", currentHighSpn);
		return panel;
	}

	/** Create Skyline-specific panel */
	protected JPanel createSkylinePanel() {
		powerTable.setAutoCreateColumnsFromModel(false);
		powerTable.setVisibleRowCount(8);
		heatTapeStatus.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.setTitle(MAKE_SKYLINE);
		panel.addRow(powerTable);
		panel.addRow("Heat tape", heatTapeStatus);
		return panel;
	}

	/** Update one attribute on the form */
	protected void updateAttribute(String a) {
		if(a == null || a.equals("notes"))
			notes.setText(proxy.getNotes());
		if(a == null || a.equals("camera"))
			camera.setSelectedItem(proxy.getCamera());
		if(a == null || a.equals("travel"))
			travel.setText(proxy.getTravel());
		if(a == null || a.equals("awsAllowed"))
			awsAllowed.setSelected(proxy.getAwsAllowed());
		if(a == null || a.equals("awsControlled"))
			awsControlled.setSelected(proxy.getAwsControlled());
		if(a == null || a.equals("make")) {
			String m = proxy.getMake();
			make.setText(m);
			updateMake(m.toUpperCase());
		}
		if(a == null || a.equals("model"))
			model.setText(proxy.getModel());
		if(a == null || a.equals("version"))
			version.setText(proxy.getVersion());
		if(a == null || a.equals("signAccess"))
			access.setText(proxy.getSignAccess());
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
			legend.setText(proxy.getLegend());
		if(a == null || a.equals("beaconType"))
			beacon.setText(proxy.getBeaconType());
		if(a == null || a.equals("technology"))
			tech.setText(proxy.getTechnology());
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
			selectSignText();
			updatePixelStatus();
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
		if(a == null || a.equals("lampStatus"))
			updateLampStatus();
		if(a == null || a.equals("lightOutput")) {
			Integer o = proxy.getLightOutput();
			if(o != null)
				lightOutput.setText("" + o + "%");
			else
				lightOutput.setText(UNKNOWN);
			// FIXME: should check SONAR roles
			feedback.setEnabled(true);
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
		if(a == null || a.equals("userNote"))
			userNote.setText(proxy.getUserNote());
	}

	/** Select card on manufacturer panel for the given make */
	protected void updateMake(String m) {
		if(m.contains(MAKE_LEDSTAR.toUpperCase()))
			cards.show(card_panel, MAKE_LEDSTAR);
		else if(m.contains(MAKE_SKYLINE.toUpperCase()))
			cards.show(card_panel, MAKE_SKYLINE);
		else
			cards.show(card_panel, MAKE_GENERIC);
	}

	/** Update the power status */
	protected void updatePowerStatus() {
		String[] s = proxy.getPowerStatus();
		if(s != null && s.length == 3) {
			try {
				PowerTableModel m = new PowerTableModel(s);
				powerTable.setColumnModel(
					m.createColumnModel());
				powerTable.setModel(m);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** Update the pixel status */
	protected void updatePixelStatus() {
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
	protected void updatePixelStatus(String[] pixels) throws IOException {
		BitmapGraphic stuckOff = createBlankBitmap();
		BitmapGraphic stuckOn = createBlankBitmap();
		BitmapGraphic errors = createBlankBitmap();
		stuckOff.setBitmap(Base64.decode(pixels[DMS.STUCK_OFF_BITMAP]));
		stuck_off_pnl.setGraphic(stuckOff);
		stuckOn.setBitmap(Base64.decode(pixels[DMS.STUCK_ON_BITMAP]));
		stuck_on_pnl.setGraphic(stuckOn);
		errors.copy(stuckOff);
		errors.copy(stuckOn);
		badPixels.setText(String.valueOf(errors.getLitCount()));
	}

	/** Create a blank bitmap */
	protected BitmapGraphic createBlankBitmap() {
		Integer w = proxy.getWidthPixels();	// Avoid race
		Integer h = proxy.getHeightPixels();	// Avoid race
		if(w != null && h != null)
			return new BitmapGraphic(w, h);
		else
			return null;
	}

	/** Update the dimensions of a sign pixel panel */
	protected void updatePixelPanel(SignPixelPanel p) {
		updatePixelPhysical(p);
		updatePixelLogical(p);
		p.verifyDimensions();
	}

	/** Update the physical dimensions of a sign pixel panel */
	protected void updatePixelPhysical(SignPixelPanel p) {
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
	protected void updatePixelLogical(SignPixelPanel p) {
		Integer wp = proxy.getWidthPixels();
		Integer hp = proxy.getHeightPixels();
		Integer cw = proxy.getCharWidthPixels();
		Integer ch = proxy.getCharHeightPixels();
		if(wp != null && hp != null && cw != null && ch != null)
			p.setLogicalDimensions(wp, hp, cw, ch);
	}

	/** Update the lamp status */
	protected void updateLampStatus() {
		String[] s = proxy.getLampStatus();
		if(s != null && s.length == 2) {
			try {
				LampTableModel m = new LampTableModel(s);
				lampTable.setAutoCreateColumnsFromModel(false);
				lampTable.setColumnModel(m.createColumnModel());
				lampTable.setModel(m);
				lampTable.setVisibleRowCount(8);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
