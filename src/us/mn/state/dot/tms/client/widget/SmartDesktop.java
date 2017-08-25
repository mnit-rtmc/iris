/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
 * Copyright (C) 2010 AHMCT, University of California
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import us.mn.state.dot.tms.client.IrisClient;

/**
 * SmartDesktop is a JDesktopPane which manages JInternalFrames automatically.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SmartDesktop extends JDesktopPane {

	/** Layer which contains all internal frames */
	static private final Integer FRAME_LAYER = new Integer(1);

	/** Select the given frame */
	static private void selectFrame(JInternalFrame frame) {
		try {
			frame.setIcon(false);
			frame.setSelected(true);
		}
		catch (PropertyVetoException e) {
			// Do nothing
		}
	}

	/** Main desktop screen */
	private final Screen screen;

	/** Iris client */
	public final IrisClient client;

	/** Create a new smart desktop */
	public SmartDesktop(Screen s, IrisClient ic) {
		screen = s;
		client = ic;
		setFocusable(true); // required to receive focus notification
		registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invokeHelp();
			}
		}, Help.getSystemHelpKey(),
			JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	/** Invoke the help system */
	private void invokeHelp() {
		AbstractForm cf = focusedForm();
		Help.invokeHelp((cf != null) ? cf.getHelpPageName() : null);
	}

	/** Find the form with focus */
	private AbstractForm focusedForm() {
		for (JInternalFrame f: getAllFrames()) {
			if (f.getFocusOwner() != null) {
				Container c = f.getContentPane();
				if (c instanceof AbstractForm)
					return (AbstractForm)c;
			}
		}
		return null;
	}

	/** Show the specified form */
	public void show(AbstractForm form) {
		JInternalFrame frame = findFrame(form.getTitle());
		if (frame != null)
			selectFrame(frame);
		else
			frame = addForm(form);
		frame.setLocation(screen.getCenteredLocation(this,
			frame.getSize()));
		frame.show();
	}

	/** Find a frame with a specific title */
	private JInternalFrame findFrame(String title) {
		for (JInternalFrame frame: getAllFrames()) {
			if (title.equals(frame.getTitle()))
				return frame;
		}
		return null;
	}

	/** Add an abstract form to the desktop pane */
	private JInternalFrame addForm(AbstractForm form) {
		form.initialize();
		JInternalFrame frame = createFrame(form);
		add(frame, FRAME_LAYER);
		frame.pack();
		return frame;
	}

	/** Create a new internal frame */
	private JInternalFrame createFrame(final AbstractForm form) {
		final JInternalFrame frame = new JInternalFrame();
		frame.setTitle(form.getTitle());
		frame.setClosable(true);
		frame.setIconifiable(true);
		frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		frame.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameClosed(InternalFrameEvent e) {
				smartDesktopRequestFocus();	// see note
				form.dispose();
			}
		});
		frame.setContentPane(form);
		return frame;
	}

	/** This method is being invoked to solve a subtle problem with
	 * help system invocation--if an JInternalFrame is created, then
	 * closed, then the help key pressed, the Help.actionPerformed()
	 * listener doesn't get notification, and the help page isn't
	 * opened. The solution is to set the focus to the JDesktopPane
	 * explicitly as each JInternalFrame is closing. This seems to
	 * work. */
	private void smartDesktopRequestFocus() {
		this.requestFocus();
	}

	/** Close the given form */
	public void closeForm(AbstractForm form) {
		JInternalFrame f = findFrame(form.getTitle());
		if (f != null)
			f.dispose();
	}

	/** Dispose of the desktop */
	public void dispose() {
		closeFrames();
	}

	/** Close all internal frames */
	private void closeFrames() {
		for (JInternalFrame frame: getAllFrames()) {
			try {
				frame.setClosed(true);
			}
			catch (PropertyVetoException e) {
				// Do nothing
			}
		}
	}
}
