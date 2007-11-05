/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.Calendar;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import us.mn.state.dot.tms.Circuit;
import us.mn.state.dot.tms.CommunicationLine;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.IndexedList;
import us.mn.state.dot.tms.Node;
import us.mn.state.dot.tms.NodeGroup;
import us.mn.state.dot.tms.Scheduler;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.utils.AbstractJob;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.utils.TreeSelectionJob;

/**
 * SonetRingForm is a Swing dialog for viewing the sonet system.
 *
 * @author Sandy Dinh
 * @author Douglas Lau
 */
public class SonetRingForm extends TMSObjectForm {

	/** Frame title */
	static protected final String TITLE = "Sonet System";

	/** Error table */
	protected final JTable error_table = new JTable();

	/** Nodegroup list */
	protected IndexedList nodegroupList;

	/** Communication Line list */
	protected IndexedList commLineList;

	/** Sonet ring tree */
	protected JTree sonetTree;

	/** Communication line tree */
	protected JTree lineTree;

	/** Add button */
	protected final JButton addButton = new JButton( "Add" );

	/** Delete button */
	protected final JButton deleteButton = new JButton( "Delete" );

	/** Edit button */
	protected final JButton editButton = new JButton( "Edit" );

	/** Status label */
	protected final JLabel statLabel = new JLabel( "" );

	/** Current selected tree */
	protected JTree activeTree = new JTree();

	/** Create a new sonet ring form */
	public SonetRingForm(TmsConnection tc) {
		super(TITLE, tc);
	}

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		nodegroupList = (IndexedList)tms.getGroups().getList();
		commLineList = (IndexedList)tms.getLines().getList();
		TreeNode sonetRootNode = new SonetTreeNode(nodegroupList);
		sonetTree = new JTree(new DefaultTreeModel(sonetRootNode));
		TreeNode lineRootNode = new LinesTreeNode(commLineList);
		lineTree = new JTree(new DefaultTreeModel(lineRootNode));
		activeTree = sonetTree;

		JSplitPane mainSplitPane =
			new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
		final JTabbedPane tabPane =
			new JTabbedPane( JTabbedPane.BOTTOM );
		JScrollPane linePane = createLineScrollPane();
		JScrollPane sonetPane = createRingScrollPane();
		tabPane.add( "Sonet Ring", sonetPane );
		tabPane.add( "Comm Line", linePane );
		tabPane.addChangeListener(new ChangeJob() {
			public void perform() throws RemoteException {
				if(tabPane.getSelectedIndex() == 0)
					activeTree = sonetTree;
				else if(tabPane.getSelectedIndex() == 1)
					activeTree = lineTree;
				updateStatus(activeTree);
			}
		});
		mainSplitPane.setLeftComponent( tabPane );
		Box statusBox = createStatusBox();
		mainSplitPane.setRightComponent(statusBox);
		mainSplitPane.setDividerLocation( 400 );
		mainSplitPane.setPreferredSize(new Dimension(700, 500));
		sonetPane.setMinimumSize(new Dimension(300, 300));
		statusBox.setMinimumSize(new Dimension(300, 300));
		add(mainSplitPane, BorderLayout.CENTER);
		setTreeProperties( sonetTree );
		setTreeProperties( lineTree );
		if(admin) {
			new ActionJob(this, addButton) {
				public void perform() throws Exception {
					addPressed();
				}
			};
			new ActionJob(this, deleteButton) {
				public void perform() throws Exception {
					deletePressed();
				}
			};
		}
		new ActionJob( this, editButton ) {
			public void perform() throws RemoteException {
				editPressed();
			}
		};
		AbstractJob.addJob(new Scheduler.Job(Calendar.SECOND, 30) {
			public void perform() throws RemoteException {
				updateStatus(activeTree);
				activeTree.repaint();
			}
		} );
		updateStatus(activeTree);
		setBackground(Color.LIGHT_GRAY);
		setMinimumSize(new Dimension(600, 300));
	}

	/** Set tree properties */
	protected void setTreeProperties(final JTree tree) {
		tree.getSelectionModel().setSelectionMode
			( TreeSelectionModel.SINGLE_TREE_SELECTION );
		tree.setCellRenderer( new TreeIconRenderer() );
		tree.setSelectionRow( 0 );
		// Expande the root node after the selection stuff happens...
		AbstractJob.addJob(new Scheduler.Job(500) {
			public void perform() {
				tree.expandRow(0);
			}
		});
	}

	/** Create the action panel */
	protected Box createActionBox() {
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		if(admin) {
			box.add(addButton);
			box.add(Box.createHorizontalStrut(8));
			box.add(editButton);
			box.add(Box.createHorizontalStrut(8));
			box.add(deleteButton);
		}
		else
			box.add(editButton);
		box.add(Box.createHorizontalGlue());
		return box;
	}

	/** Update all buttons status */
	public void updateButtons() {
		TreeNode selectedNode =
			(TreeNode)activeTree.getLastSelectedPathComponent();
		if(selectedNode == null) {
			addButton.setEnabled(false);
			editButton.setEnabled(false);
			deleteButton.setEnabled(false);
		} else {
			Object object = selectedNode.getUserObject();
			if(object instanceof IndexedList) {
				addButton.setEnabled(true);
				editButton.setEnabled(false);
				deleteButton.setEnabled(false);
			} else if(object instanceof Controller) {
				addButton.setEnabled(false);
				deleteButton.setEnabled(true);
				editButton.setEnabled(true);
			} else if(object instanceof Circuit) {
				addButton.setEnabled(false);
				editButton.setEnabled(false);
				deleteButton.setEnabled(selectedNode.isLeaf());
			} else {
				addButton.setEnabled(true);
				editButton.setEnabled(true);
				deleteButton.setEnabled(selectedNode.isLeaf());
			}
		}
	}

	/** Remove the current selected node. */
	public void removeCurrentNode() {
		TreePath currentPath = activeTree.getSelectionPath();
		int row = activeTree.getRowForPath( currentPath );
		DefaultTreeModel model =
			( DefaultTreeModel )activeTree.getModel();
		if ( currentPath != null ) {
			TreeNode currentNode = ( TreeNode )
				( currentPath.getLastPathComponent() );
			TreeNode parent =
				( TreeNode )( currentNode.getParent() );
			TreeNode nextSelectedNode =
				( TreeNode )currentNode.getNextSibling();
			if( nextSelectedNode == null ) row -= 1;
			if ( parent != null ) {
				model.removeNodeFromParent( currentNode );
				activeTree.setSelectionRow( row );
				return;
			}
		}
	}

	/** This is called when the 'delete' button is pressed */
	protected void deletePressed() throws TMSException, RemoteException {
		TreeNode selectedNode =
			(TreeNode)activeTree.getLastSelectedPathComponent();
		if(selectedNode != null)
			selectedNode.remove();
	}

	/** This is called when the 'edit' button is pressed */
	protected void editPressed() throws RemoteException {
		TreeNode selectedNode = (TreeNode)
			activeTree.getLastSelectedPathComponent();
		if(selectedNode != null)
			selectedNode.showProperties(connection);
	}

	/** This is called when the 'add' button is pressed */
	protected void addPressed() throws TMSException, RemoteException {
		final JTree tree = activeTree;
		TreeNode selectedNode = (TreeNode)
			tree.getLastSelectedPathComponent();
		if(selectedNode != null) {
			selectedNode.addChild(connection);
			DefaultTreeModel model =
				(DefaultTreeModel)tree.getModel();
			model.nodeStructureChanged(selectedNode);
		}
	}

	/** Create the status box */
	protected Box createStatusBox() {
		Box box = Box.createVerticalBox();
		box.add(Box.createVerticalStrut(8));
		box.add(createActionBox());
		box.add(Box.createVerticalStrut(8));
		error_table.setPreferredScrollableViewportSize(
			new Dimension(200, 150));
		error_table.setAutoCreateColumnsFromModel(false);
		error_table.setColumnModel(CounterModel.createColumnModel());
		Box b = Box.createHorizontalBox();
		b.add(Box.createHorizontalStrut(8));
		b.add(new JScrollPane(error_table));
		b.add(Box.createHorizontalStrut(8));
		box.add(b);
		box.add(Box.createVerticalStrut(8));
		b = Box.createHorizontalBox();
		statLabel.setAlignmentX( Component.CENTER_ALIGNMENT );
		b.add(statLabel);
		box.add(b);
		box.add(Box.createVerticalGlue());
		return box;
	}

	/** Update the error table status */
	protected void updateStatus(JTree tree) throws RemoteException {
		TreeNode selectedNode = (TreeNode)
			tree.getLastSelectedPathComponent();
		if(selectedNode != null) {
			statLabel.setText(selectedNode.getStatus());
			CounterModel counterModel =
				selectedNode.getCounterModel();
			error_table.setModel(counterModel);
			error_table.setDefaultRenderer(Object.class,
				counterModel.getRenderer());
		}
		updateButtons();
	}

	/** Select a new tree node */
	protected void selectNewNode(JTree tree) throws RemoteException {
		TreeNode selectedNode = (TreeNode)
			tree.getLastSelectedPathComponent();
		if(selectedNode != null) {
			TMSObject o = (TMSObject)selectedNode.getUserObject();
			changeObserver(o);
			selectedNode.select();
			if(!selectedNode.isRoot()) {
				DefaultTreeModel model =
					(DefaultTreeModel)tree.getModel();
				model.nodeStructureChanged(selectedNode);
			}
		}
		updateStatus(tree);
	}

	/** Create treePane panel for the sonet tree */
	protected JScrollPane createRingScrollPane() {
		new TreeSelectionJob(this, sonetTree) {
			public void perform() throws Exception {
				try {
					selectNewNode(sonetTree);
				}
				catch(NoSuchObjectException e) {
					removeCurrentNode();
				}
			}
		};
		return new JScrollPane(sonetTree);
	}

	/** Create treePane panel for the commline tree */
	protected JScrollPane createLineScrollPane() throws RemoteException {
		new TreeSelectionJob(this, lineTree) {
			public void perform() throws Exception {
				try {
					selectNewNode(lineTree);
				}
				catch(NoSuchObjectException e) {
					removeCurrentNode();
				}
			}
		};
		return new JScrollPane(lineTree);
	}

	/** Change the observer to current selected node */
	protected void changeObserver(TMSObject o) throws RemoteException {
		stopObserving(obj, observer);
		observer = null;
		obj = o;
		startObserving(obj);
	}

	/** Called when the observed object is being deleted */
	protected void doDelete() {
		new AbstractJob() {
			public void perform() {
				removeCurrentNode();
			}
		}.addToScheduler();
	}
}
