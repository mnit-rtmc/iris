/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Widgets;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for controlling camera pan and tilt.
 *
 * @author Douglas Lau
 */
public class PanTiltPanel extends JPanel {

	/** Center panel */
	private final JPanel center_pnl = new JPanel();

	/** Pan left button */
	private final PTZButton left_btn;

	/** Pan right button */
	private final PTZButton right_btn;

	/** Tilt up button */
	private final PTZButton up_btn;

	/** Tilt down button */
	private final PTZButton down_btn;

	/** Pan-tilt speed slider */
	private final JSlider speed_sld;

	/** Create a new pan-tilt panel */
	public PanTiltPanel(CameraPTZ cptz) {
		left_btn = new PTZButton("camera.ptz.left", cptz, -1, 0, 0);
		right_btn = new PTZButton("camera.ptz.right", cptz, 1, 0, 0);
		up_btn = new PTZButton("camera.ptz.up", cptz, 0, 1, 0);
		down_btn = new PTZButton("camera.ptz.down", cptz, 0, -1, 0);
		speed_sld = createSpeedSlider();
		speed_sld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				if (!speed_sld.getValueIsAdjusting()) {
					float s = speed_sld.getValue() / 100f;
					left_btn.setSpeed(s);
					right_btn.setSpeed(s);
					up_btn.setSpeed(s);
					down_btn.setSpeed(s);
				}
			}
		});
		layoutPanel();
	}

	/** Create pan-tilt speed slider */
	private JSlider createSpeedSlider() {
		Dimension sz = Widgets.dimension(40, 72);
		JSlider s = new JSlider(SwingConstants.VERTICAL);
		s.setMajorTickSpacing(10);
		s.setPaintTicks(true);
		s.setSnapToTicks(true);
		s.setToolTipText(I18N.get("camera.ptz.speed.tooltip"));
		s.setPreferredSize(sz);
		s.setMinimumSize(sz);
		return s;
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		gl.linkSize(left_btn, right_btn, up_btn, down_btn, center_pnl);
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		hg.addComponent(speed_sld);
		hg.addGap(UI.hgap);
		hg.addComponent(left_btn);
		GroupLayout.ParallelGroup tilt_g = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		tilt_g.addComponent(up_btn);
		tilt_g.addComponent(center_pnl);
		tilt_g.addComponent(down_btn);
		hg.addGroup(tilt_g);
		hg.addComponent(right_btn);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		vg.addComponent(speed_sld);
		vg.addComponent(left_btn);
		GroupLayout.SequentialGroup g0 = gl.createSequentialGroup();
		g0.addComponent(up_btn);
		g0.addComponent(center_pnl);
		g0.addComponent(down_btn);
		vg.addGroup(g0);
		vg.addComponent(right_btn);
		return vg;
	}

	/** Enable or disable pan-tilt panel */
	@Override
	public void setEnabled(boolean e) {
		super.setEnabled(e);
		left_btn.setEnabled(e);
		right_btn.setEnabled(e);
		up_btn.setEnabled(e);
		down_btn.setEnabled(e);
		speed_sld.setEnabled(e);
	}
}
