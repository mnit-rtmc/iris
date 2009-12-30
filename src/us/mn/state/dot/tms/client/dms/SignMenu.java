/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * SignMenu is a menu for DMS-related items.
 *
 * @author Douglas Lau
 */
public class SignMenu extends JMenu {

	/** User Session */
	protected final Session session;

	/** Desktop */
	protected final SmartDesktop desktop;

	/** Create a new sign menu */
	public SignMenu(final Session s) {
		super("Message Signs");
		session = s;
		desktop = s.getDesktop();
		JMenuItem item = createDmsItem();
		if(item != null)
			add(item);
		item = createFontItem();
		if(item != null)
			add(item);
		item = createQuickMessageItem();
		if(item != null)
			add(item);
	}

	/** Create the DMS menu item */
	protected JMenuItem createDmsItem() {
		if(!DMSForm.isPermitted(session))
			return null;
		String dms_name = I18N.get("dms.abbreviation");
		JMenuItem item = new JMenuItem(dms_name);
		if(dms_name.length() > 0)
			item.setMnemonic(dms_name.charAt(0));
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(createDMSForm());
			}
		};
		return item;
	}

	/** Create the DMS form */
	protected AbstractForm createDMSForm() {
		if(SystemAttrEnum.DMS_FORM.getInt() == 2)
			return new DMSForm2(session);
		else
			return new DMSForm(session);
	}

	/** Create the font menu item */
	protected JMenuItem createFontItem() {
		if(!FontForm.isPermitted(session))
			return null;
		JMenuItem item = new JMenuItem("Fonts");
		item.setMnemonic('F');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new FontForm(session));
			}
		};
		return item;
	}

	/** Create the quick message menu item */
	protected JMenuItem createQuickMessageItem() {
		if(!QuickMessageForm.isPermitted(session))
			return null;
		JMenuItem item = new JMenuItem("Quick Messages");
		item.setMnemonic('Q');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new QuickMessageForm(session));
			}
		};
		return item;
	}
}
