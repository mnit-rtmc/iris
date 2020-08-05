/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.client;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Properties;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Conduit;
import us.mn.state.dot.sonar.ConfigurationError;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.Security;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;

/**
 * The SONAR client processes all data transfers with the server.
 *
 * @author Douglas Lau
 */
public class Client {

	/** Exception handler */
	private final ExceptionHandler handler;

	/** Selector for non-blocking I/O */
	private final Selector selector;

	/** SSL context */
	private final SSLContext context;

	/** Client conduit */
	private final ClientConduit conduit;

	/** Task processor thread */
	private final Scheduler processor;

	/** Message processor task */
	private final MessageProcessor m_proc = new MessageProcessor();

	/** Flag to indicate the client is quitting */
	private boolean quitting = false;

	/** Client selector thread */
	private final Thread thread = new Thread("SONAR Client") {
		@Override
		public void run() {
			selectLoop();
		}
	};

	/** Join on selector thread */
	public void join() throws InterruptedException {
		thread.join();
	}
	
	/** Get the conduit name */
	public String getName() {
		return conduit.getName();
	}
	
	/** Get the connection name */
	public String getConnection() {
		return conduit.getConnection();
	}

	/** Get the namespace */
	public Namespace getNamespace() {
		return conduit.getNamespace();
	}

	/** Create a new SONAR client */
	public Client(Properties props, final ExceptionHandler h)
		throws IOException, ConfigurationError
	{
		selector = Selector.open();
		context = Security.createContext(props);
		conduit = new ClientConduit(props, this, selector,
			createSSLEngine(), h);
		handler = new ExceptionHandler() {
			public boolean handle(Exception e) {
				conduit.disconnect();
				return h.handle(e);
			}
		};
		processor = new Scheduler("sonar_proc", handler);
		thread.setDaemon(true);
		thread.setPriority(Thread.MAX_PRIORITY);
	}

	/** Create an SSL engine in the client context */
	private SSLEngine createSSLEngine() {
		SSLEngine engine = context.createSSLEngine();
		engine.setUseClientMode(true);
		return engine;
	}

	/** Client loop to perfrom socket I/O */
	private void selectLoop() {
		try {
			while (selector.isOpen())
				doSelect();
		}
		catch (Exception e) {
			if (!quitting)
				handler.handle(e);
		}
		conduit.dispose();
		processor.dispose();
	}

	/** Select and perform I/O on ready channels */
	private void doSelect() throws IOException {
		selector.select();
		Set<SelectionKey> ready = selector.selectedKeys();
		for (SelectionKey key: ready) {
			if (key.isConnectable())
				conduit.doConnect();
			if (key.isWritable())
				doWrite();
			if (key.isReadable())
				doRead();
		}
		ready.clear();
	}

	/** Write data to the conduit */
	private void doWrite() throws IOException {
		if (conduit.doWrite()) {
			// NOTE: delay was added as a workaround for a login
			//       problem on Windows using a slow link.  The
			//       connection would get "stuck" during SSL
			//       handshaking, and the login request would never
			//       get sent to the server.
			processor.addJob(new Job(500) {
				public void perform() throws IOException {
					conduit.flush();
				}
			});
		}
	}

	/** Read data from the conduit */
	private void doRead() throws IOException {
		if (conduit.doRead())
			processor.addJob(m_proc);
	}

	/** Populate the specified type cache */
	public void populate(final TypeCache tc) {
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.queryAll(tc);
			}
		});
	}

	/** Populate the specified type cache */
	@SuppressWarnings("unchecked")
	public void populate(TypeCache tc, boolean wait) {
		if (wait) {
			EnumerationWaiter<SonarObject> ew =
				new EnumerationWaiter<SonarObject>();
			tc.addProxyListener(ew);
			populate(tc);
			while (!ew.complete)
				TimeSteward.sleep_well(100);
			tc.removeProxyListener(ew);
		} else
			populate(tc);
	}

	/** Simple class to wait for enumeration of a type to complete */
	private class EnumerationWaiter<T extends SonarObject>
		implements ProxyListener<T>
	{
		private boolean complete = false;
		public void proxyAdded(T proxy) { }
		public void enumerationComplete() {
			complete = true;
		}
		public void proxyRemoved(T proxy) { }
		public void proxyChanged(T proxy, String a) { }
	}

	/** Login to the SONAR server.
	 * @param user Name of user.
	 * @param password Password of user.
	 * @return true on success, false on failure.
	 * @throws SonarException Thrown on error. */
	public boolean login(final String user, final String password)
		throws SonarException
	{
		thread.start();
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.login(user, password);
			}
		});
		waitLogin();
		return conduit.isLoggedIn();
	}

	/** Wait for login success or failure */
	private void waitLogin() throws SonarException {
		if (conduit.waitLogin()) {
			disconnect();
			throw new SonarException("Login timed out");
		}
	}

	/** Disconnect the client conduit */
	public void disconnect() {
		quitting = true;
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.disconnect();
			}
		});
	}

	/** Send a password change request */
	public void changePassword(final String pwd_current,
		final String pwd_new)
	{
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.changePassword(pwd_current, pwd_new);
			}
		});
	}

	/** Message processor for handling incoming messages */
	private class MessageProcessor extends Job {
		public void perform() throws IOException, SonarException {
			conduit.processMessages();
		}
	}

	/** Quit the client connection */
	public void quit() {
		quitting = true;
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.quit();
			}
		});
	}

	/** Request an attribute change */
	void setAttribute(final Name name, final String[] params) {
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.setAttribute(name, params);
			}
		});
	}

	/** Create the specified object name */
	void createObject(final Name name) {
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.createObject(name);
			}
		});
	}

	/** Remove the specified object name */
	void removeObject(final Name name) {
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.removeObject(name);
			}
		});
	}

	/** Enumerate the specified name */
	void enumerateName(final Name name) {
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.enumerateName(name);
			}
		});
	}

	/** Ignore the specified name */
	void ignoreName(final Name name) {
		processor.addJob(new Job() {
			public void perform() throws IOException {
				conduit.ignoreName(name);
			}
		});
	}
}
