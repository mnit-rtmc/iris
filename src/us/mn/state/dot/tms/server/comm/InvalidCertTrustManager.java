/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/*
 * Ignore invalid Https certificate from OPAM.
 *
 * @author Michael Darter
 */
public class InvalidCertTrustManager implements X509TrustManager{

	/** Get array of trusted cert authority certs */
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	/** Build a cert path to trusted root */
	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authtype) 
		throws CertificateException 
	{
	}

	/** Build a cert path to a trusted root */
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authtype)
		throws CertificateException
	{
	}
}
