/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toolbar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IWorker;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A tool panel that blinks when attention is required.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AttentionPanel extends ToolPanel {

	/** Is this panel IRIS enabled? */
	static public boolean getIEnabled() {
		return true;
	}

	/** Color to show blink */
	static private final Color BLINK_COLOR = Color.YELLOW;

	/** User session */
	private final Session session;

	/** Action to open attention GUI */
	private final IAction attention_act = new IAction("attention") {
		@Override
		protected void doActionPerformed(ActionEvent ev) {
			showPopupMenu();
		}
	};

	/** Button to open attention GUI */
	private final JButton attention_btn = new JButton(attention_act);

	/** Show the popup menu */
	private void showPopupMenu() {
		JPopupMenu popup = createPopup();
		if (popup != null)
			popup.show(attention_btn, 0, 0);
	}

	/** Create a popup menu */
	protected JPopupMenu createPopup() {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel(I18N.get("attention.select")));
		p.addSeparator();
		for (AlertInfo ai : alert_cache) {
			if (ai.getAlertState() == AlertState.PENDING.ordinal())
				p.add(createAction(ai));
		}
		return p;
	}

	/** Create an action to select an alert info */
	private IAction createAction(AlertInfo ai) {
		String desc = session.getAlertManager().getDescription(ai);
		return new IAction(null, desc) {
			@Override
			protected void doActionPerformed(ActionEvent ev) {
				session.getAlertManager().getSelectionModel().
					setSelected(ai);
			}
		};
	}

	/** Alert info cache */
	private final TypeCache<AlertInfo> alert_cache;

	/** Alert Info proxy listener */
	private final SwingProxyAdapter<AlertInfo> alert_listener =
		new SwingProxyAdapter<AlertInfo>()
	{
		protected void proxyAddedSwing(AlertInfo proxy) {
			checkShouldBlink();
		}
		protected void proxyRemovedSwing(AlertInfo proxy) {
			checkShouldBlink();
		}
		protected boolean checkAttributeChange(String attr) {
			return true;
		}
		protected void proxyChangedSwing(AlertInfo proxy, String attr) {
			checkShouldBlink();
		}
		protected void enumerationCompleteSwing(
			Collection<AlertInfo> proxies)
		{
			checkShouldBlink();
		}
	};

	/** Create a new attention panel */
	public AttentionPanel(Session s) {
		session = s;
		alert_cache = s.getSonarState().getAlertInfos();
		alert_cache.addProxyListener(alert_listener);
		attention_act.setEnabled(false);
		add(attention_btn);
	}

	/** Blinking state (blinking or not) */
	private boolean blinking;

	/** Current button blink state (on or off) */
	private boolean blink_on;

	/** Timer for blinking the attention button */
	private Timer blinkTimer = new Timer(2000, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (blinking)
				blinkButton();
			else {
				attention_btn.setBackground(null);
				((Timer) e.getSource()).stop();
			}
		}
	});

	/** Blink the button by toggling the color */
	private void blinkButton() {
		attention_btn.setBackground(blink_on ? BLINK_COLOR : null);
		blink_on = !blink_on;
	}

	/** Background job to check if the attention button should blink */
	private void checkShouldBlink() {
		IWorker<Void> blinkWorker = new IWorker<Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				checkBlink();
				return null;
			}
		};
		blinkWorker.execute();
	}

	/** Check if the attention button should blink */
	private void checkBlink() {
		if (shouldBlink() != blinking) {
			if (blinking)
				stopBlinking();
			else
				startBlinking();
		}
	}

	/** Check if the button should blink */
	private boolean shouldBlink() {
		for (AlertInfo ai : alert_cache) {
			if (ai.getAlertState() == AlertState.PENDING.ordinal())
				return true;
		}
		return false;
	}

	/** Start blinking the button */
	private void startBlinking() {
		blinking = true;
		blink_on = true;
		attention_act.setEnabled(true);	
		blinkButton();
		blinkTimer.restart();
	}

	/** Stop blinking the button */
	private void stopBlinking() {
		blinking = false;
		blinkTimer.stop();
		attention_act.setEnabled(false);	
		attention_btn.setBackground(null);
	}
}
