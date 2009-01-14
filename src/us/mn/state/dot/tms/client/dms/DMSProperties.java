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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
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
import us.mn.state.dot.tms.client.toast.ControllerForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.LocationPanel;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
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
	static protected String formatMM(int i) {
		if(i > 0)
			return i + " mm";
		else
			return UNKNOWN;
	}

	/** Format pixel units for display */
	static protected String formatPixels(int i) {
		if(i > 0)
			return i + " pixels";
		else if(i == 0)
			return "Variable";
		else
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
	protected final JButton controller = new JButton("Controller");

	/** Sign group model */
	protected final SignGroupModel sign_group_model;

	/** Sign text table model */
	protected SignTextTableModel sign_text_model;

	/** Sign group table component */
	protected final JTable group_table = new JTable();

	/** Button to delete a sign group */
	protected final JButton delete_group = new JButton("Delete Group");

	/** Sign text table component */
	protected final JTable sign_text_table = new JTable();

	/** Button to delete sign text message */
	protected final JButton delete_text = new JButton("Delete Message");

	/** Sign pixel panel */
	protected final SignPixelPanel pixel_panel = new SignPixelPanel();

	/** Travel time template string field */
	protected final JTextArea travel = new JTextArea(3, 24);

	/** Timing plan table component */
	protected final JTable plan_table = new JTable();

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

	/** Heat tape status label */
	protected final JLabel heatTapeStatus = new JLabel();

	/** Power supply status table */
	protected final JTable power_table = new JTable();

	/** Pixel test activation button (optional) */
	protected final JButton pixelTest;

	/** Get sign status button (optional) */
	protected final JButton getStatusButton;

	/** Reset sign button (optional) */
	protected final JButton resetButton;

	/** Bad pixel count label */
	protected final JLabel badPixels = new JLabel();

	/** Lamp status label */
	protected final JLabel lamp = new JLabel();

	/** Fan status label */
	protected final JLabel fan = new JLabel();

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
		sign_group_model = new SignGroupModel(sign.getName(),
			state.getDmsSignGroups(), state.getSignGroups(),
			connection.isAdmin());
		pixelTest = new JButton(I18NMessages.get(
			"DMSProperties.PixelTestButton"));
		getStatusButton = new JButton(I18NMessages.get(
			"DMSProperties.GetStatusButton"));
		resetButton = new JButton(I18NMessages.get(
			"DMSProperties.ResetButton"));
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
		tab.add("Manufacturer", createManufacturerPanel());
		tab.add("Status", createStatusPanel());
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
		location = new LocationPanel(true, proxy.getGeoLoc(), state);
		location.initialize();
		location.addRow("Notes", notes);
		new FocusJob(notes) {
			public void perform() {
				proxy.setNotes(notes.getText());
			}
		};
		camera.setModel(new WrapperComboBoxModel(
			state.getCameraModel()));
		location.addRow("Camera", camera);
		new ActionJob(this, camera) {
			public void perform() {
				proxy.setCamera(
					(Camera)camera.getSelectedItem());
			}
		};
		location.setCenter();
		location.addRow(controller);
		new ActionJob(this, controller) {
			public void perform() throws Exception {
				controllerPressed();
			}
		};
		return location;
	}

	/** Controller lookup button pressed */
	protected void controllerPressed() {
		Controller c = proxy.getController();
		if(c == null)
			controller.setEnabled(false);
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
		bag.weightx = 1;
		bag.weighty = 1;
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
		bag.weightx = 0;
		bag.weighty = 0;
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
		pixel_panel.setMinimumSize(new Dimension(540, 40));
		pixel_panel.setPreferredSize(new Dimension(540, 40));
		pixel_panel.setSize(new Dimension(540, 40));
		JPanel pnl = new JPanel();
		pnl.setBorder(BorderFactory.createTitledBorder(
			"Message Preview"));
		pnl.add(pixel_panel);
		panel.add(pnl, bag);
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
		group_table.setPreferredScrollableViewportSize(
			new Dimension(260, 200));
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
		sign_text_table.setPreferredScrollableViewportSize(
			new Dimension(280, 200));
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
		FormPanel panel = new FormPanel(true);
		panel.addRow("Travel template", travel);
		new FocusJob(travel) {
			public void perform() {
				proxy.setTravel(travel.getText());
			}
		};

		// FIXME: this is b0rked
		TimingPlanModel plan_model = new TimingPlanModel(
			(TimingPlanList)tms.getTimingPlans().getList(), proxy);
		plan_table.setModel(plan_model);
		plan_table.setColumnModel(TimingPlanModel.createColumnModel());
		plan_table.setAutoCreateColumnsFromModel(false);
		plan_table.setPreferredScrollableViewportSize(
			new Dimension(300, 200));
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
		heatTapeStatus.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.setTitle(MAKE_SKYLINE);
		power_table.setAutoCreateColumnsFromModel(false);
		power_table.setPreferredScrollableViewportSize(
			new Dimension(300, 200));
		panel.addRow(power_table);
		panel.addRow("Heat tape", heatTapeStatus);
		return panel;
	}

	/** Create status panel */
	protected JPanel createStatusPanel() {
		badPixels.setForeground(OK);
		lamp.setForeground(OK);
		fan.setForeground(OK);
		cabinetTemp.setForeground(OK);
		ambientTemp.setForeground(OK);
		housingTemp.setForeground(OK);
		operation.setForeground(OK);
		userNote.setForeground(OK);
		FormPanel panel = new FormPanel(true);
		panel.add("Bad pixels", badPixels);
		if(SystemAttributeHelper.isDmsPixelTestEnabled()) {
			panel.add(pixelTest);
			new ActionJob(this, pixelTest) {
				public void perform() {
					proxy.setSignRequest(SignRequest.
						TEST_PIXELS.ordinal());
				}
			};
		}
		panel.finishRow();
		panel.addRow("Lamp status", lamp);
		panel.addRow("Cabinet temp", cabinetTemp);
		panel.addRow("Ambient temp", ambientTemp);
		panel.addRow("Housing temp", housingTemp);
		panel.add("Operation", operation);
		if(SystemAttributeHelper.isDmsStatusEnabled()) {
			getStatusButton.setToolTipText(I18NMessages.get(
				"DMSProperties.GetStatusButton.ToolTip"));
			panel.add(getStatusButton);
			new ActionJob(this, getStatusButton) {
				public void perform() throws Exception {
					proxy.setSignRequest(SignRequest.
						QUERY_MESSAGE.ordinal());
				}
			};
		}
		panel.finishRow();
		panel.addRow("User Note", userNote);
		if(SystemAttributeHelper.isDmsResetEnabled()) {
			resetButton.setToolTipText(I18NMessages.get(
				"DMSProperties.ResetButton.ToolTip"));
			panel.addRow(resetButton);
			new ActionJob(this, resetButton) {
				public void perform() {
					proxy.setSignRequest(SignRequest.
						RESET_DMS.ordinal());
				}
			};
		}
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
		if(a == null || a.equals("messageCurrent"))
			selectSignText();
		if(a == null || a.equals("ldcPotBase"))
			ldcPotBaseSpn.setValue(proxy.getLdcPotBase());
		if(a == null || a.equals("pixelCurrentLow"))
			currentLowSpn.setValue(proxy.getPixelCurrentLow());
		if(a == null || a.equals("pixelCurrentHigh"))
			currentHighSpn.setValue(proxy.getPixelCurrentHigh());
		if(a == null || a.equals("powerStatus"))
			updatePowerStatus();
		if(a == null || a.equals("heatTapeStatus"))
			heatTapeStatus.setText(proxy.getHeatTapeStatus());
		if(a == null || a.equals("pixelStatus"))
			updatePixelStatus();
		if(a == null || a.equals("lampStatus"))
			updateLampStatus();
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
		// FIXME: decode Base64 values
		StatusTableModel m = new StatusTableModel(
			proxy.getPowerStatus());
		power_table.setColumnModel(m.createColumnModel());
		power_table.setDefaultRenderer(Object.class,
			m.getRenderer());
		power_table.setModel(m);
	}

	/** Update the pixel status */
	protected void updatePixelStatus() {
		// FIXME: count errors in stuck on and stuck off bitmaps
		badPixels.setText(String.valueOf(proxy.getPixelFailureCount()));
	}

	/** Update the lamp status */
	protected void updateLampStatus() {
		// FIXME: decode Base64 values
		lamp.setText(proxy.getLampStatus());
	}
}
