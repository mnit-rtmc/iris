/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
package us.mn.state.dot.tms.client.alert;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.json.JSONObject;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IpawsAlertHelper;
import us.mn.state.dot.tms.IpawsDeployer;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An alert dispatcher is a GUI panel for dispatching and reviewing automated
 * alerts for deployment on DMS.
 *
 * NOTE this would need changing to let the alert tab handle other types of
 * alerts (we would need a new Alert parent SONAR object - not sure what that
 * would look like yet).
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class AlertDispatcher extends IPanel {

	/** Client session */
	private final Session session;

	/** Alert manager */
	private final AlertManager manager;

	/** Alert selection model */
	private final ProxySelectionModel<IpawsDeployer> alertSelMdl;

	/** Alert selection listener */
	private final ProxySelectionListener alertSelLstnr =
			new ProxySelectionListener() {
		public void selectionChanged() {
			selectAlert();
		}
	};

	/** Currently selected alert */
	private IpawsAlert selectedAlert;

	/** Labels */
	/** Name label */
	private final JLabel idLbl = createValueLabel();

	/** Event label */
	private final JLabel eventLbl = createValueLabel();

	/** Combined urgency/severity/certainty label */
	private final JLabel urgSevCertLbl = createValueLabel();

	/** Headline label */
	private final JLabel headlineLbl = createValueLabel();

	/** Description label */
	private final JLabel descriptionLbl = createValueLabel();

	/** Instruction label */
	private final JLabel instructionLbl = createValueLabel();

	/** Status label */
	private final JLabel statusLbl = createValueLabel();

	/** Onset label */
	private final JLabel onsetLbl = createValueLabel();

	/** Expires label */
	private final JLabel expiresLbl = createValueLabel();

	/** Treat the area description label label a little differently */
	private final ILabel areaDescKeyLbl;

	/** Area description label */
	private final JLabel areaDescLbl = createValueLabel();

	/** Available width for area description label */
	private Integer longLblWidth;

	/** Edit deployment button */
	private JButton editDeployBtn;

	/** Discard edits button */
	private JButton discardBtn;

	/** Cancel deployment button */
	private JButton cancelBtn;

	/** Alert DMS dispatcher for deploying/reviewing DMS used for this alert*/
	private final AlertDmsDispatcher dmsDispatcher;

	/** Create a new alert dispatcher. */
	public AlertDispatcher(Session s, AlertManager m) {
		super();
		session = s;
		manager = m;
		alertSelMdl = manager.getSelectionModel();
		alertSelMdl.setAllowMultiple(false);
		alertSelMdl.addProxySelectionListener(alertSelLstnr);
		areaDescKeyLbl = new ILabel("alert.area_desc");
		editDeployBtn = new JButton(editDeploy);
		discardBtn = new JButton(discardEdits);
		cancelBtn = new JButton(cancelAlert);

		// make all buttons the same size (it looks nicer...)
		editDeployBtn.setPreferredSize(discardBtn.getPreferredSize());
		cancelBtn.setPreferredSize(discardBtn.getPreferredSize());

		// buttons are disabled by default
		editDeployBtn.setEnabled(false);
		discardBtn.setEnabled(false);
		cancelBtn.setEnabled(false);

		dmsDispatcher = new AlertDmsDispatcher(session, manager);
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		setTitle(I18N.get("alert.selected"));
		add(editDeployBtn, Stretch.TALL);
		add("alert.id");
		add(idLbl, Stretch.LAST);
		add(discardBtn, Stretch.TALL);
		add("alert.event");
		add(eventLbl, Stretch.LAST);
		add(cancelBtn, Stretch.TALL);
		add(new JLabel(I18N.get("alert.urgency") + "/" +
				I18N.get("alert.severity") + "/" +
				I18N.get("alert.certainty")), Stretch.NONE);
		add(urgSevCertLbl, Stretch.LAST);
		add("alert.status");
		add(statusLbl, Stretch.LAST);
		add("alert.onset");
		add(onsetLbl, Stretch.LAST);
		add("alert.expires");
		add(expiresLbl, Stretch.LAST);
		add(areaDescKeyLbl, Stretch.NONE);
		add(areaDescLbl, Stretch.LAST);
		add("alert.headline");
		add(headlineLbl, Stretch.LAST);
		add("alert.description");
		add(descriptionLbl, Stretch.LAST);
		add("alert.instruction");
		add(instructionLbl, Stretch.LAST);

		dmsDispatcher.initialize();
		add(dmsDispatcher, Stretch.DOUBLE);

		alertSelMdl.addProxySelectionListener(alertSelLstnr);
	}

	/** Action to edit or deploy an alert. */
	private IAction editDeploy = new IAction("alert.deploy.edit") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			dmsDispatcher.processEditDeploy();
			setEditDeployBtnText();

			// enable the discard button if in edit mode
			discardBtn.setEnabled(manager.getEditing());
		}
	};

	private void setEditDeployBtnText() {
		if (manager.getEditing())
			editDeployBtn.setText(I18N.get("alert.deploy.deploy"));
		else
			editDeployBtn.setText(I18N.get("alert.deploy.edit"));
	}

	/** Action to cancel editing an alert deployment. */
	private IAction discardEdits = new IAction("alert.deploy.discard_edit") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			dmsDispatcher.cancelEdit();
			setEditDeployBtnText();
			discardBtn.setEnabled(manager.getEditing());
		}
	};

	/** Action to cancel an alert deployment. */
	private IAction cancelAlert = new IAction("alert.deploy.cancel_alert") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			dmsDispatcher.cancelAlert();
		}
	};

	/** Set the selected alert in the list (for calling from other code). */
	public void selectAlert(IpawsDeployer iad) {
		alertSelMdl.setSelected(iad);
	}

	/** Update the display to reflect the alert selected. */
	private void selectAlert() {
		IpawsDeployer iad = alertSelMdl.getSingleSelection();
		if (iad != null) {
			IpawsAlert ia = IpawsAlertHelper.lookupByIdentifier(
				iad.getAlertId());
			if (ia != null) {
				setSelectedAlert(iad, ia);
				return;
			}
		}
		clearSelectedAlert();
	}

	/** Set the selected alert */
	private void setSelectedAlert(IpawsDeployer iad, IpawsAlert ia) {
		selectedAlert = ia;
		manager.setSelectedAlert(iad);

		// fill out the value labels
		// FIXME this way of wrapping is kind of hacky
		idLbl.setText("<html><div style=\"width:200px;\">" +
				selectedAlert.getIdentifier() + "</div></html>");
		idLbl.setText("<html><div style=\"width:200px;\">" +
				selectedAlert.getIdentifier() + "</div></html>");
		eventLbl.setText(selectedAlert.getEvent());
		headlineLbl.setText("<html><div style=\"width:200px;\">" +
				selectedAlert.getHeadline() + "</div></html>");
		urgSevCertLbl.setText(selectedAlert.getUrgency() + "/" +
			selectedAlert.getSeverity() + "/" + selectedAlert.getCertainty());
		descriptionLbl.setText("<html><div style=\"width:200px;\">" +
			selectedAlert.getAlertDescription() + "</div></html>");
		instructionLbl.setText("<html><div style=\"width:200px;\">" +
			selectedAlert.getInstruction() + "</div></html>");

		AlertState state = AlertState.fromOrdinal(iad.getAlertState());
		statusLbl.setText(getState(state));

		onsetLbl.setText(iad.getAlertStart().toString());
		expiresLbl.setText(iad.getAlertEnd().toString());

		// get a JSON object from the area string (which is in JSON syntax)
		String area = selectedAlert.getArea();
		JSONObject jo = new JSONObject(area);
		String areaDesc = jo.has("areaDesc") ? jo.getString("areaDesc") : "";

		// make long labels wrap
		areaDescLbl.setText("<html><div style=\"width:200px;\">" +
				areaDesc + "</div></html>");

		// set the alert in the DMS dispatcher so sign list updates
		dmsDispatcher.setSelectedAlert(iad, selectedAlert);

		// set the button text and disable buttons if alert is in past
		setEditDeployBtnText();
		boolean npast = state != AlertState.EXPIRED;
		editDeployBtn.setEnabled(npast);
		cancelBtn.setEnabled(npast);
	}

	/** Get the alert state */
	static private String getState(AlertState st) {
		switch (st) {
			case PENDING:
				return I18N.get("alert.status.pending");
			case DEPLOYED:
				return I18N.get("alert.status.deployed");
			case INACTIVE:
				return I18N.get("alert.status.scheduled");
			default:
				return I18N.get("alert.status.not_deployed");
		}
	}

	/** Clear the selected alert. */
	private void clearSelectedAlert() {
		// null the selected alert
		selectedAlert = null;
		manager.setSelectedAlert(null);

		// make sure editing is off and tell the DMS dispatcher
		manager.setEditing(false);
		dmsDispatcher.clearSelectedAlert();

		// disable the buttons
		editDeployBtn.setEnabled(false);
		discardBtn.setEnabled(false);
		cancelBtn.setEnabled(false);

		// clear the value labels
		idLbl.setText("");
		eventLbl.setText("");
		urgSevCertLbl.setText("");
		headlineLbl.setText("");
		descriptionLbl.setText("");
		instructionLbl.setText("");
		statusLbl.setText("");
		onsetLbl.setText("");
		expiresLbl.setText("");
		areaDescLbl.setText("");
	}

	/** Get the selected alert */
	public IpawsAlert getSelectedAlert() {
		return selectedAlert;
	}

	/** Get the AlertDmsDispatcher */
	public AlertDmsDispatcher getDmsDispatcher() {
		return dmsDispatcher;
	}
}
