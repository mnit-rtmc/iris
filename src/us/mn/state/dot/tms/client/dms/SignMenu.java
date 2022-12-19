/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
 * Copyright (C) 2015  Iteris Inc.
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

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.wysiwyg.selector.WMsgSelectorForm;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * SignMenu is a menu for DMS-related items.
 *
 * @author Douglas Lau
 */
public class SignMenu extends IMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new sign menu */
	public SignMenu(final Session s) {
		super("sign.menu");
		session = s;
		desktop = s.getDesktop();
		addItem(session.createTableAction(DMS.SONAR_TYPE));
		addItem(createSignConfigItem());
		addItem(createSignDetailItem());
		addItem(createSignGroupItem());
		addItem(createFontItem());
		addItem(createGraphicItem());
		addItem(createMsgPatternItem());
		addItem(createWysiwygSelectorItem());
		addItem(createWordItem());
		addItem(session.createTableAction(Beacon.SONAR_TYPE));
	}

	/** Create a sign config menu item action */
	private IAction createSignConfigItem() {
		return SignConfigForm.isPermitted(session) ?
		    new IAction("dms.config") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new SignConfigForm(session));
			}
		    } : null;
	}

	/** Create a sign detail menu item action */
	private IAction createSignDetailItem() {
		return SignDetailForm.isPermitted(session) ?
		    new IAction("dms.detail") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new SignDetailForm(session));
			}
		    } : null;
	}

	/** Create a sign group menu item action */
	private IAction createSignGroupItem() {
		return SignGroupForm.isPermitted(session) ?
		    new IAction("dms.group.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new SignGroupForm(session));
			}
		    } : null;
	}

	/** Create a font menu item action */
	private IAction createFontItem() {
		return FontForm.isPermitted(session) ?
		    new IAction("font.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new FontForm(session));
			}
		    } : null;
	}

	/** Create a graphics menu item action */
	private IAction createGraphicItem() {
		return GraphicForm.isPermitted(session) ?
		    new IAction("graphics") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new GraphicForm(session));
			}
		    } : null;
	}

	/** Create a message pattern menu item action */
	private IAction createMsgPatternItem() {
		return MsgPatternForm.isPermitted(session) ?
		    new IAction("msg.patterns") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new MsgPatternForm(session));
			}
		    } : null;
	}

	/** Create a WYSIWYG Selector menu item action */
	private IAction createWysiwygSelectorItem() {
		return WMsgSelectorForm.isPermitted(session) ?
		    new IAction("wysiwyg.menu") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new WMsgSelectorForm(session));
			}
		    } : null;
	}

	/** Create a word menu item action */
	private IAction createWordItem() {
		return WordForm.isPermitted(session) ?
		    new IAction("word.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new WordForm(session));
			}
		    } : null;
	}
}
