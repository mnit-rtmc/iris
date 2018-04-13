/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Catalog;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * CatalogPanel is a UI for entering and editing catalogs.
 *
 * @author Douglas Lau
 */
public class CatalogPanel extends JPanel {

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

	/** Catalog proxy */
	private final Catalog catalog;

	/** Sequence num label */
	private final JLabel seq_lbl = new JLabel(I18N.get("catalog.seq"));

	/** Sequence number text */
	private final JTextField seq_txt = new JTextField("", 8);

	/** Play list label */
	private final JLabel play_lbl = new JLabel(I18N.get("play.list"));

	/** Play list text field (for adding) */
	private final JTextField play_txt = new JTextField(20);

	/** Play List list model */
	private final DefaultListModel<PlayList> play_mdl =
		new DefaultListModel<PlayList>();

	/** List of Play Lists */
	private final JList<PlayList> play_lst = new JList<PlayList>(play_mdl);

	/** Play list scroll pane */
	private final JScrollPane play_scrl = new JScrollPane(play_lst);

	/** Insert play list button */
	private final JButton insert_btn = new JButton(new IAction(
		"catalog.insert")
	{
		protected void doActionPerformed(ActionEvent e) {
			insertPlayList();
		}
	});

	/** Insert a play list */
	private void insertPlayList() {
		PlayList pl = lookupPlayList();
		if (pl != null) {
			insertIntoModel(pl);
			changePlayLists();
		}
		play_txt.setText("");
	}

	/** Insert a play list into the model */
	private void insertIntoModel(PlayList pl) {
		int s = play_lst.getSelectedIndex();
		if (s >= 0) {
			play_mdl.add(s, pl);
			play_lst.setSelectedIndex(s);
		} else
			play_mdl.addElement(pl);
	}

	/** Remove play list button */
	private final JButton remove_btn = new JButton(new IAction(
		"catalog.remove")
	{
		protected void doActionPerformed(ActionEvent e) {
			int s = play_lst.getSelectedIndex();
			if (s >= 0) {
				play_mdl.remove(s);
				changePlayLists();
			}
		}
	});

	/** Up button */
	private final JButton up_btn = new JButton(new IAction("catalog.up") {
		protected void doActionPerformed(ActionEvent e) {
			movePlayListUp();
		}
	});

	/** Move selected play list up */
	private void movePlayListUp() {
		int s = play_lst.getSelectedIndex();
		if (s > 0) {
			PlayList pl0 = play_mdl.get(s - 1);
			PlayList pl1 = play_mdl.get(s);
			play_mdl.set(s - 1, pl1);
			play_mdl.set(s, pl0);
			play_lst.setSelectedIndex(s - 1);
			changePlayLists();
		}
	}

	/** Down action */
	private final JButton down_btn = new JButton(new IAction("catalog.down")
	{
		protected void doActionPerformed(ActionEvent e) {
			movePlayListDown();
		}
	});

	/** Move selected play list down */
	private void movePlayListDown() {
		int s = play_lst.getSelectedIndex();
		if (s >= 0 && s < play_mdl.size() - 1) {
			PlayList pl0 = play_mdl.get(s);
			PlayList pl1 = play_mdl.get(s + 1);
			play_mdl.set(s, pl1);
			play_mdl.set(s + 1, pl0);
			play_lst.setSelectedIndex(s + 1);
			changePlayLists();
		}
	}

	/** Change play lists in list */
	private void changePlayLists() {
		PlayList[] pls = new PlayList[play_mdl.size()];
		play_mdl.copyInto(pls);
		catalog.setPlayLists(pls);
	}

	/** Create a new catalog panel */
	public CatalogPanel(Session s, Catalog c) {
		session = s;
		catalog = c;
	}

	/** Initialize the widgets */
	public void initialize() {
		setBorder(UI.border);
		play_lst.setVisibleRowCount(12);
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
		p0.addComponent(seq_lbl);
		p0.addComponent(play_lbl);
		p0.addComponent(insert_btn);
		p0.addComponent(remove_btn);
		p0.addComponent(up_btn);
		p0.addComponent(down_btn);
		hg.addGroup(p0);
		hg.addGap(UI.hgap);
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		p1.addComponent(seq_txt);
		p1.addComponent(play_txt);
		p1.addComponent(play_scrl);
		gl.linkSize(SwingConstants.HORIZONTAL, play_txt, play_scrl);
		hg.addGroup(p1);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup p0 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		p0.addComponent(seq_lbl);
		p0.addComponent(seq_txt);
		vg.addGroup(p0);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		p1.addComponent(play_lbl);
		p1.addComponent(play_txt);
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
		p2.addComponent(play_scrl);
		vg.addGroup(p2);
		return vg;
	}

	/** Create jobs */
	private void createJobs() {
		seq_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    Integer n = parseInt(seq_txt.getText());
			    seq_txt.setText((n != null) ? n.toString() : "");
			    catalog.setSeqNum(n);
			}
		});
		play_txt.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					insertPlayList();
			}
			@Override public void keyReleased(KeyEvent ke) {
				updateButtons();
			}
		});
		ListSelectionModel s = play_lst.getSelectionModel();
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
		seq_txt.setEnabled(canWrite("seqNum"));
		boolean ud = canWrite("playLists");
		play_txt.setEnabled(ud);
		play_lst.setEnabled(ud);
		updateButtons();
	}

	/** Update buttons */
	private void updateButtons() {
		boolean ud = canWrite("playLists");
		insert_btn.setEnabled(ud && isPlayListValid());
		remove_btn.setEnabled(ud && isSelected());
		up_btn.setEnabled(ud && canMoveUp());
		down_btn.setEnabled(ud && canMoveDown());
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String attr) {
		return session.canWrite(catalog, attr);
	}

	/** Check if text field has a valid play list */
	private boolean isPlayListValid() {
		return lookupPlayList() != null;
	}

	/** Lookup play list by ID (or number) */
	private PlayList lookupPlayList() {
		return PlayListHelper.find(play_txt.getText());
	}

	/** Check if a play list is selected */
	private boolean isSelected() {
		return play_lst.getSelectedIndex() >= 0;
	}

	/** Check if selected play list can be moved up */
	private boolean canMoveUp() {
		return play_lst.getSelectedIndex() > 0;
	}

	/** Check if selected play list can be moved down */
	private boolean canMoveDown() {
		int s = play_lst.getSelectedIndex();
		return (s >= 0 && s < play_mdl.size() - 1);
	}

	/** Update one attribute on the form */
	public void updateAttribute(String a) {
		if (null == a || a.equals("seqNum")) {
			Integer n = catalog.getSeqNum();
			seq_txt.setText((n != null) ? n.toString() : "");
		}
		if (null == a || a.equals("playLists"))
			updatePlayLists();
	}

	/** Update the play lists */
	private void updatePlayLists() {
		play_txt.setText("");
		PlayList[] pls = catalog.getPlayLists();
		for (int i = 0; i < pls.length; i++) {
			PlayList p = pls[i];
			if (i < play_mdl.size())
				play_mdl.set(i, p);
			else
				play_mdl.addElement(p);
		}
		for (int i = pls.length; i < play_mdl.size(); i++)
			play_mdl.remove(i);
	}
}
