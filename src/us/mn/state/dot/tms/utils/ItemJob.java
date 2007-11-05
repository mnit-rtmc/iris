/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.utils;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractButton;
import javax.swing.JComponent;

/**
 * ItemJob is a simple extension/replacement for ItemListener
 * which passes off a job to a scheduler.
 *
 * @author Douglas Lau
 */
abstract public class ItemJob extends GuiJob implements ItemListener {

	/** Most recent item event */
	protected ItemEvent event;

	/** Create a new item job */
	public ItemJob(JComponent f, AbstractButton c) {
		super(f, c);
		c.addItemListener(this);
	}

	/** Item state changed (from ItemListener interface) */
	public void itemStateChanged(ItemEvent e) {
		start();
		event = e;
		addToScheduler();
	}
}
