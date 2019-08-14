/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Helper functions to create an SSL security context
 *
 * @author Douglas Lau
 */
public class Security {

	/** Load a KeyStore in the jks format */
	static private KeyStore loadKeyStore(String keystore)
		throws GeneralSecurityException, ConfigurationError
	{
		try {
			return loadKeyStore(createURL(keystore).openStream());
		}
		catch (IOException e) {
			throw ConfigurationError.cannotRead(keystore, e);
		}
	}

	/** Create a URL for the specified keystore */
	static private URL createURL(String keystore) throws IOException {
		File file = new File(keystore);
		if (file.exists())
			return file.toURI().toURL();
		String cwd = System.getProperty("user.dir");
		file = new File(cwd, keystore);
		if (file.exists())
			return file.toURI().toURL();
		else
			return new URL(keystore);
	}

	/** Load a KeyStore from an InputStream in the jks format */
	static private KeyStore loadKeyStore(InputStream is)
		throws IOException, GeneralSecurityException
	{
		try {
			KeyStore ks = KeyStore.getInstance("jks");
			ks.load(is, null);
			return ks;
		}
		finally {
			is.close();
		}
	}

	/** Create and configure an SSL context */
	static private SSLContext _createContext(String keystore, String pwd)
		throws GeneralSecurityException, ConfigurationError
	{
		SSLContext context = SSLContext.getInstance("TLS");
		KeyStore ks = loadKeyStore(keystore);
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(
			"SunX509");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(
			"SunX509");
		kmf.init(ks, pwd.toCharArray());
		tmf.init(ks);
		context.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
			null);
		return context;
	}

	/** Create and configure an SSL context */
	static private SSLContext _createContext(Properties props)
		throws GeneralSecurityException, ConfigurationError
	{
		String keystore = Props.getProp(props, "keystore.file");
		String pwd = Props.getProp(props, "keystore.password");
		return _createContext(keystore, pwd);
	}

	/** Create and configure an SSL context */
	static public SSLContext createContext(Properties props)
		throws ConfigurationError
	{
		try {
			return _createContext(props);
		}
		catch (GeneralSecurityException e) {
			throw ConfigurationError.generalSecurity(e);
		}
	}
}
