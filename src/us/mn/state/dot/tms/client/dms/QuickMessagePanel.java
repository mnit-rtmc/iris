/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextArea;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * A panel for editing the properties of a quick message.
 *
 * @author Douglas Lau
 */
public class QuickMessagePanel extends IPanel
	implements ProxyView<QuickMessage>
{
	/** MULTI text area */
	private final JTextArea multi_txt = new JTextArea();

	/** User session */
	private final Session session;

	/** Proxy watcher */
	private final ProxyWatcher<QuickMessage> watcher;

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

	/** Quick message being edited */
	private QuickMessage quick_msg;

	/** Set the quick message */
	public void setQuickMsg(QuickMessage qm) {
		watcher.setProxy(qm);
	}

	/** Create the detector panel */
	public QuickMessagePanel(Session s, boolean r) {
		session = s;
		TypeCache<QuickMessage> cache =
			s.getSonarState().getDmsCache().getQuickMessages();
		watcher = new ProxyWatcher<QuickMessage>(cache, this, false);
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		add("quick.message.multi");
		add(multi_txt, Stretch.FULL);
		multi_txt.setWrapStyleWord(false);
		createJobs();
		watcher.initialize();
		clear();
		session.addEditModeListener(edit_lsnr);
	}

	/** Create the jobs */
	private void createJobs() {
		multi_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setMulti(multi_txt.getText());
			}
		});
	}

	/** Set the MULTI string */
	private void setMulti(String m) {
		QuickMessage qm = quick_msg;
		if (qm != null)
			qm.setMulti(new MultiString(m.trim()).normalize());
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		clear();
		session.removeEditModeListener(edit_lsnr);
		watcher.dispose();
		super.dispose();
	}

	/** Update the edit mode */
	public void updateEditMode() {
		QuickMessage qm = quick_msg;
		multi_txt.setEnabled(session.canWrite(qm, "multi"));
	}

	/** Called when all proxies have been enumerated (from ProxyView). */
	@Override
	public void enumerationComplete() { }

	/** Update one attribute (from ProxyView). */
	@Override
	public void update(QuickMessage qm, String a) {
		if (null == a) {
			quick_msg = qm;
			updateEditMode();
		}
		if (null == a || a.equals("multi"))
			multi_txt.setText(qm.getMulti());
	}

	/** Clear all attributes (from ProxyView). */
	@Override
	public void clear() {
		quick_msg = null;
		multi_txt.setEnabled(false);
		multi_txt.setText("");
	}
}
