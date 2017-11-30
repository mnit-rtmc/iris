/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PlayListPanel is a UI for entering and editing play lists.
 *
 * @author Douglas Lau
 */
public class PlayListPanel extends JPanel {

	/** Parse an integer */
	static private Integer parseInt(String t) {
		try {
			return Integer.parseInt(t);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** User Session */
	private final Session session;

	/** Play list proxy */
	private final PlayList play_list;

	/** Num label */
	private final JLabel num_lbl = new JLabel(I18N.get("play.list.num"));

	/** Play list number text */
	private final JTextField num_txt = new JTextField("", 8);

	/** Camera label */
	private final JLabel cam_lbl = new JLabel(I18N.get("camera"));

	/** Camera text field (for adding) */
	private final JTextField cam_txt = new JTextField(20);

	/** Camera list model */
	private final DefaultListModel<Camera> cam_mdl =
		new DefaultListModel<Camera>();

	/** Camera list */
	private final JList<Camera> cam_lst = new JList<Camera>(cam_mdl);

	/** Camera scroll pane */
	private final JScrollPane cam_scrl = new JScrollPane(cam_lst);

	/** Insert camera button */
	private final JButton insert_btn = new JButton(new IAction(
		"play.list.insert")
	{
		protected void doActionPerformed(ActionEvent e) {
			insertCamera();
		}
	});

	/** Insert a camera */
	private void insertCamera() {
		Camera c = lookupCamera();
		if (c != null) {
			insertIntoModel(c);
			changeCameras();
		}
		cam_txt.setText("");
	}

	/** Insert a camera into the model */
	private void insertIntoModel(Camera c) {
		int s = cam_lst.getSelectedIndex();
		if (s >= 0) {
			cam_mdl.add(s, c);
			cam_lst.setSelectedIndex(s);
		} else
			cam_mdl.addElement(c);
	}

	/** Remove camera button */
	private final JButton remove_btn = new JButton(new IAction(
		"play.list.remove")
	{
		protected void doActionPerformed(ActionEvent e) {
			int s = cam_lst.getSelectedIndex();
			if (s >= 0) {
				cam_mdl.remove(s);
				changeCameras();
			}
		}
	});

	/** Up button */
	private final JButton up_btn = new JButton(new IAction(
		"play.list.up")
	{
		protected void doActionPerformed(ActionEvent e) {
			moveCameraUp();
		}
	});

	/** Move selected camera up */
	private void moveCameraUp() {
		int s = cam_lst.getSelectedIndex();
		if (s > 0) {
			Camera c0 = cam_mdl.get(s - 1);
			Camera c1 = cam_mdl.get(s);
			cam_mdl.set(s - 1, c1);
			cam_mdl.set(s, c0);
			cam_lst.setSelectedIndex(s - 1);
			changeCameras();
		}
	}

	/** Down action */
	private final JButton down_btn = new JButton(new IAction(
		"play.list.down")
	{
		protected void doActionPerformed(ActionEvent e) {
			moveCameraDown();
		}
	});

	/** Move selected camera down */
	private void moveCameraDown() {
		int s = cam_lst.getSelectedIndex();
		if (s > 0 && s < cam_mdl.size() - 1) {
			Camera c0 = cam_mdl.get(s);
			Camera c1 = cam_mdl.get(s + 1);
			cam_mdl.set(s, c1);
			cam_mdl.set(s + 1, c0);
			cam_lst.setSelectedIndex(s + 1);
			changeCameras();
		}
	}

	/** Change cameras in list */
	private void changeCameras() {
		Camera[] cams = new Camera[cam_mdl.size()];
		cam_mdl.copyInto(cams);
		play_list.setCameras(cams);
	}

	/** Create a new play list panel */
	public PlayListPanel(Session s, PlayList pl) {
		session = s;
		play_list = pl;
	}

	/** Initialize the widgets */
	public void initialize() {
		setBorder(UI.border);
		cam_lst.setVisibleRowCount(12);
		layoutPanel();
		createJobs();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		gl.linkSize(insert_btn, remove_btn, up_btn, down_btn);
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup p0 = gl.createParallelGroup(
			GroupLayout.Alignment.TRAILING);
		p0.addComponent(num_lbl);
		p0.addComponent(cam_lbl);
		p0.addComponent(insert_btn);
		p0.addComponent(remove_btn);
		p0.addComponent(up_btn);
		p0.addComponent(down_btn);
		hg.addGroup(p0);
		hg.addGap(UI.hgap);
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		p1.addComponent(num_txt);
		p1.addComponent(cam_txt);
		p1.addComponent(cam_scrl);
		gl.linkSize(SwingConstants.HORIZONTAL, cam_txt, cam_scrl);
		hg.addGroup(p1);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup p0 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		p0.addComponent(num_lbl);
		p0.addComponent(num_txt);
		vg.addGroup(p0);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		p1.addComponent(cam_lbl);
		p1.addComponent(cam_txt);
		vg.addGroup(p1);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup p2 = gl.createParallelGroup(
			GroupLayout.Alignment.LEADING);
		GroupLayout.SequentialGroup v1 = gl.createSequentialGroup();
		v1.addComponent(insert_btn);
		v1.addGap(UI.vgap);
		v1.addComponent(remove_btn);
		v1.addGap(UI.vgap);
		v1.addComponent(up_btn);
		v1.addGap(UI.vgap);
		v1.addComponent(down_btn);
		p2.addGroup(v1);
		p2.addComponent(cam_scrl);
		vg.addGroup(p2);
		return vg;
	}

	/** Create jobs */
	private void createJobs() {
		num_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    Integer n = parseInt(num_txt.getText());
			    num_txt.setText((n != null) ? n.toString() : "");
			    play_list.setNum(n);
			}
		});
		cam_txt.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					insertCamera();
			}
			@Override public void keyReleased(KeyEvent ke) {
				updateButtons();
			}
		});
		ListSelectionModel s = cam_lst.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				updateButtons();
			}
		});
	}

	/** Update the edit mode */
	public void updateEditMode() {
		num_txt.setEnabled(canWrite("num"));
		boolean ud = canWrite("cameras");
		cam_txt.setEnabled(ud);
		cam_lst.setEnabled(ud);
		updateButtons();
	}

	/** Update buttons */
	private void updateButtons() {
		boolean ud = canWrite("cameras");
		insert_btn.setEnabled(ud && hasCamId());
		remove_btn.setEnabled(ud && isSelected());
		up_btn.setEnabled(ud && canMoveUp());
		down_btn.setEnabled(ud && canMoveDown());
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String attr) {
		return session.canWrite(play_list, attr);
	}

	/** Check if text field has a valid camera ID */
	private boolean hasCamId() {
		return lookupCamera() != null;
	}

	/** Lookup camera by ID */
	private Camera lookupCamera() {
		return CameraHelper.lookup(cam_txt.getText());
	}

	/** Check if a camera is selected */
	private boolean isSelected() {
		return cam_lst.getSelectedIndex() >= 0;
	}

	/** Check if selected camera can be moved up */
	private boolean canMoveUp() {
		return cam_lst.getSelectedIndex() > 0;
	}

	/** Check if selected camera can be moved down */
	private boolean canMoveDown() {
		int s = cam_lst.getSelectedIndex();
		return (s >= 0 && s < cam_mdl.size() - 1);
	}

	/** Update one attribute on the form */
	public void updateAttribute(String a) {
		if (a == null || a.equals("num")) {
			Integer n = play_list.getNum();
			num_txt.setText((n != null) ? n.toString() : "");
		}
		if (a == null || a.equals("cameras"))
			updateCameras();
	}

	/** Update the cameras */
	private void updateCameras() {
		cam_txt.setText("");
		Camera[] cams = play_list.getCameras();
		for (int i = 0; i < cams.length; i++) {
			Camera c = cams[i];
			if (i < cam_mdl.size())
				cam_mdl.set(i, c);
			else
				cam_mdl.addElement(c);
		}
		for (int i = cams.length; i < cam_mdl.size(); i++)
			cam_mdl.remove(i);
	}
}
