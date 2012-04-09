/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPopupMenu;
import us.mn.state.dot.log.TmsLogFactory;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.MapModel;
import us.mn.state.dot.map.PointSelector;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.MapExtentHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.system.LoginForm;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.widget.Screen;
import us.mn.state.dot.tms.client.widget.ScreenLayout;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The main window for the IRIS client application.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class IrisClient extends JFrame {

	/** Login scheduler */
	static protected final Scheduler LOGIN = new Scheduler("LOGIN");

	/** Window title login message */
	static protected final String TITLE_NOT_LOGGED_IN = "Not logged in";

	/** Window title login message */
	static protected final String TITLE_LOGIN_IN_PROGRESS =
		"Logging in ... please wait";

	/** Create the window title */
	static protected String createTitle(String suffix) {
		return SystemAttrEnum.WINDOW_TITLE.getString() + suffix;
	}

	/** Create the window title.
	 * @param s Current session, or null if not logged in. */
	static protected String createTitle(Session s) {
		if(s != null) {
			User u = s.getUser();
			return createTitle(u.getName() + " (" +
				u.getFullName() + ")");
		}
		return createTitle(TITLE_NOT_LOGGED_IN);
	}

	/** Array of screens to display client */
	protected final Screen[] screens;

	/** Array of screen panes */
	protected final ScreenPane[] s_panes;

	/** Desktop pane */
	protected final SmartDesktop desktop;

	/** Screen layout for desktop pane */
	protected final ScreenLayout layout;

	/** Message logger */
	protected final Logger logger;

	/** Client properties */
	protected final Properties props;

	/** Exception handler */
	protected final SimpleHandler handler;

	/** Menu bar */
	private final IMenuBar menu_bar;

	/** Login session information */
	protected Session session;

	/** Mutable user properties stored on client workstation */
	private UserProperties user_props;

	/** Create a new Iris client */
	public IrisClient(Properties props, SimpleHandler h) throws IOException{
		super(createTitle(TITLE_NOT_LOGGED_IN));
		this.props = props;
		handler = h;
		logger = TmsLogFactory.createLogger("IRIS", Level.WARNING,
			null);
		I18N.initialize(props);
		user_props =  new UserProperties(
			props.getProperty(UserProperties.FNAME_PROP_NAME));
		user_props.read();
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		screens = Screen.getAllScreens();
		s_panes = new ScreenPane[screens.length];
		desktop = new SmartDesktop(screens[0], this);
		initializeScreenPanes();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				quit();
			}
		});
		layout = new ScreenLayout(desktop);
		getContentPane().add(desktop);
		menu_bar = new IMenuBar(this, desktop);
		setMenuBar();
		autoLogin();
	}

	/** Log out the current session and quit the client */
	public void quit() {
		logout();
		new AbstractJob(LOGIN) {
			public void perform() {
				System.exit(0);
			}
		}.addToScheduler();
	}

	/** Set the currently selected tab in each screen pane, using the
	 * persistently stored tab index. */
	private void setSelectedTabViaPersist() {
		Object[] sti = user_props.getSelectedTabs();
		for(int i = 0; i < s_panes.length && i < sti.length; ++i) {
			int ti = ((Integer)sti[i]).intValue();
			s_panes[i].setSelectedTabIndex(ti);
		}
	}

	/** Get the currently selected tab in each screen pane */
	public int[] getSelectedTabIndex() {
		int[] sti = new int[s_panes.length];
		for(int i = 0; i < sti.length; ++i)
			sti[i] = s_panes[i].getSelectedTabIndex();
		return sti;
	}

	/** Update and write user properties file */
	private void writeUserProperties() {
		user_props.setWindowProperties(this);
		user_props.write();
	}

	/** Initialize the screen panes */
	protected void initializeScreenPanes() {
		for(int s = 0; s < s_panes.length; s++)
			s_panes[s] = new ScreenPane();
		for(ScreenPane sp: s_panes) {
			sp.addComponentListener(new ComponentAdapter() {
				public void componentHidden(ComponentEvent e) {
					arrangeTabs();
				}
				public void componentShown(ComponentEvent e) {
					arrangeTabs();
				}
			});
			desktop.add(sp, JLayeredPane.DEFAULT_LAYER);
		}
	}

	/** Make the frame displayable (called by window toolkit) */
	public void addNotify() {
		super.addNotify();
		setPosition();
	}

	/** Set position of frame window using properties values. */
	private void setPosition() {
		if(user_props.getUsed() && user_props.haveWindowPosition()) {
			setBounds(user_props.getWindowPosition());
			setExtendedState(user_props.getWindowState());
		} else {
			setBounds(Screen.getMaximizedBounds());
			if(screens.length < 2)
				setExtendedState(MAXIMIZED_BOTH);
		}
	}

	/** Auto-login the user if enabled */
	protected void autoLogin() {
		String user = props.getProperty("autologin.username");
		String pws = props.getProperty("autologin.password");
		if(user != null && pws != null) {
			char[] pwd = pws.toCharArray();
			pws = null;
			if(user.length() > 0 && pwd.length > 0)
				login(user, pwd);
		}
	}

	/** Get a list of all visible screen panes. Will return an empty
	 * list if IRIS is minimized. */
	public LinkedList<ScreenPane> getVisiblePanes() {
		LinkedList<ScreenPane> visible = new LinkedList<ScreenPane>();
		for(ScreenPane s: s_panes)
			if(s.isVisible())
				visible.add(s);
		return visible;
	}

	/** Arrange the tabs on the visible screen panes */
	protected void arrangeTabs() {
		setMenuBar();
		removeTabs();
		Session s = session;
		if(s != null)
			arrangeTabs(s);
	}

	/** Set the menu bar to the first visible screen pane */
	private void setMenuBar() {
		IMenuBar mb = menu_bar;
		for(ScreenPane sp: getVisiblePanes()) {
			sp.setMenuBar(mb);
			mb = null;
		}
	}

	/** Arrange the tabs on the visible screen panes */
	protected void arrangeTabs(Session s) {
		LinkedList<ScreenPane> visible = getVisiblePanes();
		if(visible.isEmpty())
			return;
		for(MapTab tab: s.getTabs()) {
			int p = tab.getNumber() % visible.size();
			ScreenPane sp = visible.get(p);
			sp.addTab(tab);
		}
		setSelectedTabViaPersist();
	}

	/** Show the login form */
	public void login() {
		if(session == null)
			desktop.show(new LoginForm(this, desktop));
	}

	/** Login a user */
	public void login(final String user, final char[] pwd) {
		new AbstractJob(LOGIN) {
			public void perform() throws Exception {
				doLogin(user, pwd);
			}
		}.addToScheduler();
	}

	/** Login a user */
	protected void doLogin(String user, char[] pwd) throws Exception {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setTitle(createTitle(TITLE_LOGIN_IN_PROGRESS));
		menu_bar.disableSessionMenu();
		closeSession();
		try {
			session = createSession(user, pwd);
		}
		finally {
			for(int i = 0; i < pwd.length; ++i)
				pwd[i] = ' ';
			menu_bar.setSession(session);
			updateMaps(session);
			arrangeTabs();
			setTitle(createTitle(session));
			setCursor(null);
			invalidate();
		}
	}

	/** Create a new session */
	protected Session createSession(String user, char[] pwd)
		throws IOException, TdxmlException, SonarException,
		NoSuchFieldException, IllegalAccessException
	{
		SonarState state = createSonarState();
		state.login(user, new String(pwd));
		if(state.isLoggedIn()) {
			state.populateCaches();
			Session s = new Session(state, desktop, props, logger);
			s.initialize();
			return s;
		} else
			return null;
	}

	/** Create a new SONAR state */
	protected SonarState createSonarState() throws IOException,
		SonarException, NoSuchFieldException, IllegalAccessException
	{
		return new SonarState(props, handler);
	}

	/** Update the maps on all screen panes */
	protected void updateMaps(Session s) {
		for(ScreenPane sp: s_panes) {
			MapBean mb = sp.getMap();
			mb.setModel(createMapModel(mb, s));
			if(s != null)
				sp.createToolPanels(s);
			else
				sp.clearToolPanels();
		}
		setInitExtent();
	}

	/** Set initial map extent */
	public void setInitExtent() {
		MapExtent me = MapExtentHelper.lookup("Home");
		if(me != null) {
			for(ScreenPane sp: s_panes)
				sp.setMapExtent(me);
		}
 	}

	/** Create a new map model */
	protected MapModel createMapModel(MapBean mb, Session s) {
		MapModel mm = new MapModel();
		if(s != null)
			s.createLayers(mb, mm);
		return mm;
	}

	/** Get the map model for the first screen pane */
	public MapModel getMapModel() {
		for(ScreenPane sp: s_panes)
			return sp.getMap().getModel();
		return null;
	}

	/** Logout of the current session */
	public void logout() {
		new AbstractJob(LOGIN) {
			public void perform() {
				doLogout();
			}
		}.addToScheduler();
	}

	/** Clean up when the user logs out */
	protected void doLogout() {
		writeUserProperties();
		menu_bar.setSession(null);
		removeTabs();
		closeSession();
		updateMaps(null);
		setTitle(createTitle(TITLE_NOT_LOGGED_IN));
		invalidate();
	}

	/** Close the session */
	protected void closeSession() {
		Session s = session;
		if(s != null)
			s.dispose();
		session = null;
	}

	/** Removed all the tabs */
	protected void removeTabs() {
		for(ScreenPane sp: s_panes)
			sp.removeTabs();
	}

	/** Set the point selector for all map beans */
	public void setPointSelector(PointSelector ps) {
		for(ScreenPane sp: getVisiblePanes())
			sp.getMap().addPointSelector(ps);
	}
}
