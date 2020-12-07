/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.editor.tags;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IllegalFormatException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;

/**
 * WYSIWYG DMS Message Editor MULTI tag dialog form base class.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
abstract public class WMultiTagDialog extends AbstractForm {

	/** User session */
	protected Session session;
	
	/** Handle to controller */
	protected WController controller;
	
	/** Token type */
	protected WTokenType tokenType;
	
	/** Token being edited (if applicable) */
	protected WToken oldTok;
	
	/** List of fields in the form (for validating input) */
	private ArrayList<WTagParamComponent> fields =
			new ArrayList<WTagParamComponent>();
	
	/** Label containing warning text */
	private JLabel warningMsg;
	private String warningMsgText;
	private static final Color WARNING_MSG_COLOR = Color.RED;
	
	/** Warning color */
	protected static final Color WARNING_FIELD_COLOR =
			new Color(255, 204, 203, 127);
	
	/** i in circle character appended to field labels that have tooltips */
	private static final char TOOLTIP_ICON_CHAR = '\u24D8';
	
	/** Buttons */
	private JButton commitBtn;
	private JButton cancelBtn;
	
	/** Constructor. If tok is null, a new tag will be added, otherwise it
	 *  will be replaced.
	 */
	public WMultiTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, true);
		controller = c;
		session = c.getSession();
		tokenType = tokType;
		oldTok = tok;
		warningMsgText = I18N.get("wysiwyg.multi_tag_dialog.warning");
		warningMsg = new JLabel(warningMsgText);
		cancelBtn = new JButton(cancel);
		
		if (tok != null && tok.isType(tokenType)) {
			commitBtn = new JButton(editTag);
			loadFields(tok);
		} else
			commitBtn = new JButton(addTag);
	}
	
	public static WMultiTagDialog construct(
			WController c, WTokenType tokType, WToken tok) {
		String t = formatTitle(tokType);
		switch (tokType) {
		case feedMsg:
			return new WMsgFeedTagDialog(t, c, tokType, tok);
		case parkingAvail:
			return new WParkingAvailTagDialog(t, c, tokType, tok);
		case slowWarning:
			return new WSlowWarningTagDialog(t, c, tokType, tok);
		case travelTime:
			return new WTravelTimeTagDialog(t, c, tokType, tok);
		case tolling:
			return new WTollingTagDialog(t, c, tokType, tok);
		case incidentLoc:
			return new WIncidentLocatorTagDialog(t, c, tokType, tok);
		case speedAdvisory:
			return new WSpeedAdvisoryTagDialog(t, c, tokType, tok);
		case spacingChar:
			return new WCharSpacingTagDialog(t, c, tokType, tok);
		case newLine:
			return new WNewLineTagDialog(t, c, tokType, tok);
		case pageTime:
			return new WPageTimingTagDialog(t, c, tokType, tok);
		case pageBackground:
			return new WPageBackgroundTagDialog(t, c, tokType, tok);
		case colorForeground:
			return new WForegroundColorTagDialog(t, c, tokType, tok);
		case font:
			return new WFontTagDialog(t, c, tokType, tok);
		case justificationPage:
			return new WPageJustTagDialog(t, c, tokType, tok);
		case justificationLine:
			return new WLineJustTagDialog(t, c, tokType, tok);
		case colorRectangle:
			return new WColorRectangleTagDialog(t, c, tokType, tok);
		case textRectangle:
			return new WTextRectangleTagDialog(t, c, tokType, tok);
		case graphic:
			return new WGraphicTagDialog(t, c, tokType, tok);
		case capTime:
			return new WCapTimeTagDialog(t, c, tokType, tok);
		case capResponse:
			return new WCapResponseTagDialog(t, c, tokType, tok);
		case capUrgency:
			return new WCapUrgencyTagDialog(t, c, tokType, tok);
		default:
			return null;
		}
	}
	
	private static String formatTitle(WTokenType tokType) {
		try {
			return String.format(I18N.get("wysiwyg.multi_tag_dialog.title"),
					tokType.getLabel());
		} catch (IllegalFormatException e) {
			return String.format("Add/edit %s tag", tokType.getLabel());
		}
	}
	
	protected static boolean validateFields(
			Collection<WTagParamComponent> fields) {
		boolean valid = true;
		for (WTagParamComponent f: fields)
			valid = f.contentsValid() && valid;
		return valid;
	}
	
	protected static boolean validateFields(WTagParamComponent... fields) {
		boolean valid = true;
		for (WTagParamComponent f: fields)
			valid = f.contentsValid() && valid;
		return valid;	
	}
	
	/** Validate all fields in the form. */
	protected boolean validateForm() {
		return validateFields(fields);
	}
	
	/** Initialize the form */
	@Override
	protected void initialize() {
		// use a Y-axis box layout to arrange top-down
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// dispatch to the child form class to add form fields
		addTagForm();
		
		// add a label that will display a warning for invalid input
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(warningMsg);
		add(p);
		
		// put both buttons centered on the bottom row 
		p = new JPanel();
		p.add(commitBtn);
		p.add(cancelBtn);
		add(p);
		
		// set preferred width based on the warning message that may appear
		Dimension d = getPreferredSize();
		d.width = Math.max(warningMsg.getPreferredSize().width + 50, d.width);
		setPreferredSize(d);
		
		// clear the warning message - we will only show it if needed
		warningMsg.setText("");
	}
	
	/** Create a left-aligned JPanel with a JLabel using the I18N textId, and
	 *  the WTagPagamComponent provided to the current panel. If the textId
	 *  with ".tooltip" added exists, that text is added as a tooltip to the
	 *  label.
	 */
	protected JPanel makeFieldPanel(String textId, WTagParamComponent field) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		// get the I18N string for this field
		String lblStr = I18N.get(textId);
		JLabel lbl = new JLabel(lblStr);
		
		// add a tooltip if we can find one
		String tooltip = I18N.getSilent(textId + ".tooltip");
		if (tooltip != null) {
			// also add a small icon if there is a tooltip
			lbl = new JLabel(lblStr + " " + TOOLTIP_ICON_CHAR);
			lbl.setToolTipText(tooltip);
		}
		p.add(lbl);
		p.add((JComponent) field);
		return p;
	}
	
	/** Add a left-aligned JPanel with a JLabel using the I18N textId, and the
	 *  WTagPagamComponent provided to the current panel. If the textId with
	 *  ".tooltip" added exists, that text is added as a tooltip to the label.
	 */
	protected void addField(String textId, WTagParamComponent field) {
		// create the field panel and add to the main panel
		JPanel p = makeFieldPanel(textId, field);
		add(p);
		
		// add the field to the list for validating later
		fields.add(field);
	}
	
	/** Cancel action */
	private final IAction cancel = new IAction(
		"wysiwyg.multi_tag_dialog.cancel") {
		protected void doActionPerformed(ActionEvent e)
			throws Exception {
			close(session.getDesktop());
		}
	};
	
	/** Add new tag action */
	private final IAction addTag = new IAction(
			"wysiwyg.multi_tag_dialog.update") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			// validate the form before doing anything
			if (validateForm()) {
				// get the new tag from the form data and pass to the
				// controller
				WToken newTok = makeNewTag();
				controller.saveState();
				controller.addToken(newTok);
				close(session.getDesktop());
			} else {
				warningMsg.setText(warningMsgText);
				warningMsg.setForeground(WARNING_MSG_COLOR);
			}
		}
	};

	/** Edit existing tag action */
	private final IAction editTag = new IAction(
			"wysiwyg.multi_tag_dialog.update") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			if (validateForm()) {
				WToken newTok = makeNewTag();
				controller.saveState();
				controller.replaceToken(oldTok, newTok);
				close(session.getDesktop());
			} else {
				warningMsg.setText(warningMsgText);
				warningMsg.setForeground(WARNING_MSG_COLOR);
			}
		}
	};
	
	//===========================================
	// Abstract methods
	//===========================================
	
	/** Fill any fields from the existing token being edited. Only called if
	 *  a token of the correct type was provided at construction.
	 */
	abstract protected void loadFields(WToken tok);
	
	/** Add tag-specific form fields. Child classes should add JPanels using
	 *  the addField() method as necessary.
	 */
	abstract protected void addTagForm();
	
	/** Create and return the token that should be inserted (for new tags) or
	 *  replace (for editing tags) the current tag. */
	abstract protected WToken makeNewTag();
	
}
