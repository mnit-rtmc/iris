/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PlayListEntryPanel is a UI for entering and editing play lists.
 *
 * @author Douglas Lau
 */
public class PlayListEntryPanel extends JPanel {

	/** User Session */
	private final Session session;

	/** Play list proxy */
	private final PlayList play_list;

	/** Entry label */
	private final JLabel ent_lbl;

	/** Entry text field (for adding) */
	private final JTextField ent_txt = new JTextField(20);

	/** Entry list model */
	private final DefaultListModel<String> ent_mdl =
		new DefaultListModel<String>();

	/** Entry list */
	private final JList<String> ent_lst = new JList<String>(ent_mdl);

	/** Entry scroll pane */
	private final JScrollPane ent_scrl = new JScrollPane(ent_lst);

	/** Insert entry button */
	private final JButton insert_btn = new JButton(new IAction(
		"play.list.insert")
	{
		protected void doActionPerformed(ActionEvent e) {
			insertEntry();
		}
	});

	/** Insert an entry */
	private void insertEntry() {
		String e = lookupEntry();
		if (e != null) {
			insertIntoModel(e);
			changeEntries();
		}
		ent_txt.setText("");
	}

	/** Insert an entry into the model */
	private void insertIntoModel(String e) {
		int s = ent_lst.getSelectedIndex();
		if (s >= 0) {
			ent_mdl.add(s, e);
			ent_lst.setSelectedIndex(s);
		} else
			ent_mdl.addElement(e);
	}

	/** Remove entry button */
	private final JButton remove_btn = new JButton(new IAction(
		"play.list.remove")
	{
		protected void doActionPerformed(ActionEvent e) {
			int s = ent_lst.getSelectedIndex();
			if (s >= 0) {
				ent_mdl.remove(s);
				changeEntries();
			}
		}
	});

	/** Up button */
	private final JButton up_btn = new JButton(new IAction(
		"play.list.up")
	{
		protected void doActionPerformed(ActionEvent e) {
			moveEntryUp();
		}
	});

	/** Move selected entry up */
	private void moveEntryUp() {
		int s = ent_lst.getSelectedIndex();
		if (s > 0) {
			String e0 = ent_mdl.get(s - 1);
			String e1 = ent_mdl.get(s);
			ent_mdl.set(s - 1, e1);
			ent_mdl.set(s, e0);
			ent_lst.setSelectedIndex(s - 1);
			changeEntries();
		}
	}

	/** Down action */
	private final JButton down_btn = new JButton(new IAction(
		"play.list.down")
	{
		protected void doActionPerformed(ActionEvent e) {
			moveEntryDown();
		}
	});

	/** Move selected entry down */
	private void moveEntryDown() {
		int s = ent_lst.getSelectedIndex();
		if (s >= 0 && s < ent_mdl.size() - 1) {
			String e0 = ent_mdl.get(s);
			String e1 = ent_mdl.get(s + 1);
			ent_mdl.set(s, e1);
			ent_mdl.set(s + 1, e0);
			ent_lst.setSelectedIndex(s + 1);
			changeEntries();
		}
	}

	/** Change entries in list */
	private void changeEntries() {
		String[] ents = new String[ent_mdl.size()];
		ent_mdl.copyInto(ents);
		play_list.setEntries(ents);
	}

	/** Create a new play list entry panel */
	public PlayListEntryPanel(Session s, PlayList pl) {
		session = s;
		play_list = pl;
	 	String entry = pl.getMeta() ? "play.list.sub.list" : "camera";
	 	ent_lbl = new JLabel(I18N.get(entry));
	}

	/** Initialize the widgets */
	public void initialize() {
		setBorder(UI.border);
		ent_lst.setVisibleRowCount(12);
		ent_lst.setCellRenderer(new EntryCellRenderer(play_list));
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
		p0.addComponent(ent_lbl);
		p0.addComponent(insert_btn);
		p0.addComponent(remove_btn);
		p0.addComponent(up_btn);
		p0.addComponent(down_btn);
		hg.addGroup(p0);
		hg.addGap(UI.hgap);
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		p1.addComponent(ent_txt);
		p1.addComponent(ent_scrl);
		gl.linkSize(SwingConstants.HORIZONTAL, ent_txt, ent_scrl);
		hg.addGroup(p1);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup p0 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		p0.addComponent(ent_lbl);
		p0.addComponent(ent_txt);
		vg.addGroup(p0);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup(
			GroupLayout.Alignment.LEADING);
		GroupLayout.SequentialGroup v1 = gl.createSequentialGroup();
		v1.addComponent(insert_btn);
		v1.addGap(UI.vgap);
		v1.addComponent(remove_btn);
		v1.addGap(UI.vgap);
		v1.addComponent(up_btn);
		v1.addGap(UI.vgap);
		v1.addComponent(down_btn);
		p1.addGroup(v1);
		p1.addComponent(ent_scrl);
		vg.addGroup(p1);
		return vg;
	}

	/** Create jobs */
	private void createJobs() {
		ent_txt.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					insertEntry();
			}
			@Override public void keyReleased(KeyEvent ke) {
				updateButtons();
			}
		});
		ListSelectionModel s = ent_lst.getSelectionModel();
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
		boolean ud = canWrite("entries");
		ent_txt.setEnabled(ud);
		ent_lst.setEnabled(ud);
		updateButtons();
	}

	/** Update buttons */
	private void updateButtons() {
		boolean ud = canWrite("entries");
		insert_btn.setEnabled(ud && hasEntry());
		remove_btn.setEnabled(ud && isSelected());
		up_btn.setEnabled(ud && canMoveUp());
		down_btn.setEnabled(ud && canMoveDown());
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String attr) {
		return session.canWrite(play_list, attr);
	}

	/** Check if text field has a valid entry */
	private boolean hasEntry() {
		return lookupEntry() != null;
	}

	/** Lookup entry by name/number */
	private String lookupEntry() {
		String e = ent_txt.getText();
		if (play_list.getMeta()) {
			PlayList pl = PlayListHelper.lookup(e);
			return (pl != null) ? pl.getName() : null;
		} else {
			Camera c = CameraHelper.findNum(e);
			return (c != null) ? c.getName() : null;
		}
	}

	/** Check if an entry is selected */
	private boolean isSelected() {
		return ent_lst.getSelectedIndex() >= 0;
	}

	/** Check if selected entry can be moved up */
	private boolean canMoveUp() {
		return ent_lst.getSelectedIndex() > 0;
	}

	/** Check if selected entry can be moved down */
	private boolean canMoveDown() {
		int s = ent_lst.getSelectedIndex();
		return (s >= 0 && s < ent_mdl.size() - 1);
	}

	/** Update one attribute on the form */
	public void updateAttribute(String a) {
		if (null == a || a.equals("entries"))
			updateEntries();
	}

	/** Update the entries */
	private void updateEntries() {
		ent_txt.setText("");
		String[] ents = play_list.getEntries();
		for (int i = 0; i < ents.length; i++) {
			String e = ents[i];
			if (i < ent_mdl.size())
				ent_mdl.set(i, e);
			else
				ent_mdl.addElement(e);
		}
		for (int i = ents.length; i < ent_mdl.size(); i++)
			ent_mdl.remove(i);
	}
}
