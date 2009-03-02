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
package us.mn.state.dot.tms.client.toast;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.rmi.RemoteException;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import us.mn.state.dot.tms.utils.Screen;

/**
 * SmartDesktop
 *
 * @author Douglas Lau
 */
public class SmartDesktop extends JDesktopPane {

	/** Layer which contains all internal frames */
	static protected final Integer FRAME_LAYER = new Integer(1);

	/** Select the given frame */
	static protected void selectFrame(JInternalFrame frame) {
		try {
			frame.setIcon(false);
			frame.setSelected(true);
		}
		catch(PropertyVetoException e) {
			// Do nothing
		}
	}

	/** Main desktop screen */
	protected final Screen screen;

	/** Create a new smart desktop */
	public SmartDesktop(Screen s) {
		screen = s;
	}

	/** Create a new internal frame */
	protected JInternalFrame createFrame(final AbstractForm form) {
		final JInternalFrame frame = new JInternalFrame();
		frame.setTitle(form.getTitle());
		frame.setClosable(true);
		frame.setIconifiable(true);
		frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		frame.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosed(InternalFrameEvent e) {
				form.dispose();
			}
		});
		frame.setContentPane(form);
		form.addFormCloseListener(new FormCloseListener() {
			public void formClosed(FormCloseEvent e) {
				frame.dispose();
			}
		});
		return frame;
	}

	/** Add an abstract form to the desktop pane */
	protected JInternalFrame addForm(AbstractForm form) {
		form.initialize();
		if(form instanceof TMSObjectForm) {
			TMSObjectForm of = (TMSObjectForm)form;
			try {
				of.doUpdate();
				of.doStatus();
			}
			catch(RemoteException e) {
				e.printStackTrace();
			}
		}
		JInternalFrame frame = createFrame(form);
		frame.pack();
		super.add(frame, FRAME_LAYER);
		return frame;
	}

	/** Find a frame with a specific title */
	protected JInternalFrame find(String title) {
		for(JInternalFrame frame: getAllFrames()) {
			if(title.equals(frame.getTitle()))
				return frame;
		}
		return null;
	}

	/** Show the specified form */
	public Component show(AbstractForm form) {
		JInternalFrame frame = find(form.getTitle());
		if(frame != null)
			selectFrame(frame);
		else
			frame = addForm(form);
		Point p = screen.getCenteredLocation(frame.getSize());
		Point o = Screen.getLocation(this);
		frame.setLocation(p.x - o.x, p.y - o.y);
		frame.show();
		return frame;
	}

	/** Close all internal frames */
	public void closeFrames() {
		for(JInternalFrame frame: getAllFrames()) {
			try {
				frame.setClosed(true);
			}
			catch(PropertyVetoException e) {
				// Do nothing
			}
		}
	}

	/** Dispose of the desktop */
	public void dispose() {
		closeFrames();
	}
}
