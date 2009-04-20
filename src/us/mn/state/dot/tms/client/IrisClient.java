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
import java.rmi.NotBoundException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import us.mn.state.dot.log.TmsLogFactory;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.tms.client.security.IrisPermission;
import us.mn.state.dot.tms.client.security.IrisUser;
import us.mn.state.dot.tms.client.security.LoginListener;
import us.mn.state.dot.tms.client.security.UserManager;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.utils.Screen;
import us.mn.state.dot.tms.utils.ScreenLayout;
import us.mn.state.dot.tms.utils.I18NMessages;


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

	/** Desktop pane */
	protected final SmartDesktop desktop;

	/** Handles all user authentication */
	protected final UserManager userManager;

	/** Hostname of TMS server */
	protected final String hostName;

	/** Contains information about the current connection */
	protected final TmsConnection tmsConnection;

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
	public IrisClient(Properties props) throws Exception {
		super("IRIS: Login to Start");
		this.props = props;
		logger = TmsLogFactory.createLogger("IRIS", Level.WARNING,
			null);
		setName( "IRIS" );
		I18NMessages.initialize (props);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		screens = Screen.getAllScreens();
		s_panes = new ScreenPane[screens.length];
		desktop = new SmartDesktop(screens[0]);
		for(int s = 0; s < s_panes.length; s++) {
			s_panes[s] = new ScreenPane();
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
		tmsConnection = new TmsConnection(desktop, userManager, props);
		hostName = props.getProperty( "TMSIpAddress" ) + ":" +
			props.getProperty("TMSPort");
		layout = new ScreenLayout(desktop);
		getContentPane().add(desktop);
		buildMenus( userManager );
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
	protected void buildMenus(UserManager a) {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new SessionMenu(a));
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
			for(IrisTab tab: s.getTabs()) {
				int p = tab.getNumber() % visible.size();
				ScreenPane pane = visible.get(p);
				pane.addTab(tab);
			}
		}
	}

	/** Set the logged in user */
	protected void setUser(IrisUser user) throws IOException,
		TdxmlException, NotBoundException
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setTitle("IRIS: User = " + user.getName() + " (" +
			user.getFullName() + ")");
		tmsConnection.open(hostName, user.getFullName());
		session = new Session(tmsConnection,
			userManager.getSonarState(), props, logger);
		arrangeTabs();
		if(user.hasPermission(IrisPermission.VIEW)) {
			viewMenu = new ViewMenu(tmsConnection,
				userManager.getSonarState());
			getJMenuBar().add(viewMenu, 1);
		}

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
		tmsConnection.close();
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
		for(ScreenPane p: s_panes)
			p.removeTabs();
	}
}

