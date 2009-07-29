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
package us.mn.state.dot.tms.client;

import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import us.mn.state.dot.log.TmsLogFactory;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.trafmap.BaseLayers;
import us.mn.state.dot.trafmap.ViewLayer;
import us.mn.state.dot.tms.client.security.LoginListener;
import us.mn.state.dot.tms.client.security.UserManager;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.widget.Screen;
import us.mn.state.dot.tms.client.widget.ScreenLayout;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The Main class for IrisClient.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class IrisClient extends JFrame {

	/** Array of screens to display client */
	protected final Screen[] screens;

	/** Array of screen panes */
	protected final ScreenPane[] s_panes;

	/** Base layers */
	protected final List<Layer> baseLayers;

	/** Desktop pane */
	protected final SmartDesktop desktop;

	/** Handles all user authentication */
	protected final UserManager userManager;

	/** Screen layout for desktop pane */
	protected final ScreenLayout layout;

	/** Message logger */
	protected final Logger logger;

	/** Client properties */
	protected final Properties props;

	/** View menu */
	protected JMenu viewMenu;

	/** Login session information */
	protected Session session;

	/** the help menu changes after login */
	protected HelpMenu m_helpmenu;

	/** Create a new Iris client */
	public IrisClient(Properties props) throws IOException {
		super("IRIS: Login to Start");
		this.props = props;
		logger = TmsLogFactory.createLogger("IRIS", Level.WARNING,
			null);
		setName( "IRIS" );
		I18N.initialize(props);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		screens = Screen.getAllScreens();
		s_panes = new ScreenPane[screens.length];
		desktop = new SmartDesktop(screens[0]);
		baseLayers = new BaseLayers().getLayers();
		ViewLayer vlayer = new ViewLayer();
		for(int s = 0; s < s_panes.length; s++) {
			s_panes[s] = new ScreenPane(vlayer);
			s_panes[s].addComponentListener(new ComponentAdapter() {
				public void componentHidden(ComponentEvent e) {
					arrangeTabs();
				}
				public void componentShown(ComponentEvent e) {
					arrangeTabs();
				}
			});
			desktop.add(s_panes[s], JLayeredPane.DEFAULT_LAYER);
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				userManager.quit();
			}
		});
		userManager = new UserManager(desktop, props);
		layout = new ScreenLayout(desktop);
		getContentPane().add(desktop);
		buildMenus();
		userManager.addLoginListener(new LoginListener() {
			public void login() throws Exception {
				setUser(userManager.getUser());
			}
			public void logout() {
				loggedout();
			}
		});

		// auto login if enabled
		userManager.autoLogin();
	}

	/** Make the frame displayable (called by window toolkit) */
	public void addNotify() {
		super.addNotify();
		setBounds(Screen.getMaximizedBounds());
		if(screens.length < 2)
			setExtendedState(MAXIMIZED_BOTH);
	}

	/** Build all the menus */
	protected void buildMenus() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new SessionMenu(userManager));
		m_helpmenu = new HelpMenu(desktop);
		menuBar.add(m_helpmenu);
		this.setJMenuBar(menuBar);
	}

	/** Get a list of all visible screen panes */
	protected LinkedList<ScreenPane> getVisiblePanes() {
		LinkedList<ScreenPane> visible = new LinkedList<ScreenPane>();
		for(ScreenPane s: s_panes)
			if(s.isVisible())
				visible.add(s);
		return visible;
	}

	/** Arrange the tabs on the visible screen panes */
	protected void arrangeTabs() {
		removeTabs();
		Session s = session;
		if(s != null) {
			LinkedList<ScreenPane> visible = getVisiblePanes();
			for(MapTab tab: s.getTabs()) {
				int p = tab.getNumber() % visible.size();
				ScreenPane pane = visible.get(p);
				pane.addTab(tab);
				tab.setMap(pane.getMap());
			}
			for(ScreenPane sp: visible)
				sp.createToolPanels(s);
		}
	}

	/** Set the logged in user */
	protected void setUser(User user) throws IOException,
		TdxmlException
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setTitle("IRIS: User = " + user.getName() + " (" +
			user.getFullName() + ")");
		session = new Session(userManager, desktop, props, logger,
			baseLayers);
		arrangeTabs();
		viewMenu = session.getViewMenu();
		getJMenuBar().add(viewMenu, 1);

		// post-login additions to help menu
		if(m_helpmenu != null)
			m_helpmenu.add(desktop);

		setCursor(null);
		validate();
	}

	/** Clean up when the user logs out */
	protected void loggedout() {
		clearViewMenu();
		removeTabs();
		closeSession();
		setTitle("IRIS: Login to Start");
		validate();
	}

	/** Close the session */
	protected void closeSession() {
		Session s = session;
		if(s != null)
			s.dispose();
		session = null;
	}

	/** Clear the view menu */
	protected void clearViewMenu() {
		JMenu v = viewMenu;
		if(v != null)
			getJMenuBar().remove(v);
		viewMenu = null;
	}

	/** Removed all the tabs */
	protected void removeTabs() {
		for(ScreenPane sp: s_panes)
			sp.removeTabs();
	}
}
